package adudecalledleo.aftbg.app.game;

import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import adudecalledleo.aftbg.app.face.FaceLoadException;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.script.ScriptLoadException;
import adudecalledleo.aftbg.app.script.TextboxScript;
import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.app.util.WindowTintAdapter;
import adudecalledleo.aftbg.json.JsonReadUtils;
import adudecalledleo.aftbg.window.WindowContext;
import adudecalledleo.aftbg.window.WindowTint;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;

public final class GameDefinition extends Definition {
    private final WindowContext winCtx;
    private final FacePool baseFaces;
    private final List<TextboxScript> baseScripts;

    private final List<ExtensionDefinition> extensions;
    private final FacePool allFaces;
    private final List<TextboxScript> allScripts;

    private GameDefinition(String id, String name, String[] description, String[] credits,
                          Path filePath, Path basePath,
                          WindowContext winCtx, FacePool baseFaces, List<TextboxScript> baseScripts) {
        super(id, name, description, credits, filePath, basePath);

        this.winCtx = winCtx;
        this.baseFaces = baseFaces;
        this.baseScripts = baseScripts;

        setAsSource(this.baseFaces);
        setAsSource(this.baseScripts);

        extensions = new ArrayList<>();
        allFaces = new FacePool();
        allScripts = new LinkedList<>();
        updateExtensions();
    }

    public static GameDefinition load(Path filePath) throws DefinitionLoadException {
        filePath = filePath.toAbsolutePath();

        Path basePath = filePath.getParent();
        if (basePath == null) {
            throw new DefinitionLoadException("File path \"%s\" has no parent(?!)"
                    .formatted(filePath));
        }

        String id = null, name = null;
        String[] description = DEFAULT_DESCRIPTION, credits = DEFAULT_CREDITS;
        String windowPathRaw = null;
        WindowTint windowTint = null;
        String facesPathRaw = null;
        @Nullable String scriptsPathRaw = null;
        try (JsonReader reader = JsonReader.json5(filePath)) {
            reader.beginObject();
            while (reader.hasNext()) {
                String field = reader.nextName();
                switch (field) {
                    case "id" -> id = reader.nextString();
                    case "name" -> name = reader.nextString();
                    case "desc", "description" -> description = JsonReadUtils.readStringArray(reader);
                    case "credits" -> credits = JsonReadUtils.readStringArray(reader);
                    case "window_path" -> windowPathRaw = reader.nextString();
                    case "window_tint" -> windowTint = WindowTintAdapter.read(reader);
                    case "faces_path" -> facesPathRaw = reader.nextString();
                    case "scripts_path" -> scriptsPathRaw = reader.nextString();
                    default -> reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            throw new DefinitionLoadException("Failed to read definition from \"%s\"".formatted(filePath), e);
        }

        List<String> missingFields = new ArrayList<>();
        if (id == null) {
            missingFields.add("id");
        }
        if (name == null) {
            missingFields.add("name");
        }
        if (windowPathRaw == null) {
            missingFields.add("window_path");
        }
        if (windowTint == null) {
            missingFields.add("window_tint");
        }
        if (facesPathRaw == null) {
            missingFields.add("faces_path");
        }

        if (!missingFields.isEmpty()) {
            throw new DefinitionLoadException("Game definition is missing following fields: %s"
                    .formatted(String.join(", ", missingFields)));
        }

        Path facesPath = PathUtils.tryResolve(basePath, facesPathRaw, "face pool definition",
                DefinitionLoadException::new);
        FacePool faces;
        try (JsonReader reader = JsonReader.json5(facesPath)) {
            faces = FacePool.Adapter.read(reader);
        } catch (Exception e) {
            throw new DefinitionLoadException("Failed to read face pool definition from \"%s\""
                    .formatted(facesPath), e);
        }

        List<TextboxScript> scripts;
        if (scriptsPathRaw == null) {
            scripts = List.of();
        } else {
            Path scriptsPath = PathUtils.tryResolve(basePath, scriptsPathRaw, "scripts definition",
                    DefinitionLoadException::new);
            try (JsonReader reader = JsonReader.json5(scriptsPath)) {
                scripts = TextboxScript.ListAdapter.read(reader);
            } catch (Exception e) {
                throw new DefinitionLoadException("Failed to read scripts definition from \"%s\""
                        .formatted(scriptsPath), e);
            }
        }

        Path windowPath = PathUtils.tryResolve(basePath, windowPathRaw, "Window image",
                DefinitionLoadException::new);
        BufferedImage windowImage;
        try (InputStream input = Files.newInputStream(windowPath)) {
            windowImage = ImageIO.read(input);
        } catch (IOException e) {
            throw new DefinitionLoadException("Failed to load Window image from \"%s\""
                    .formatted(windowPath), e);
        }

        WindowContext winCtx = new WindowContext(windowImage, windowTint);
        try {
            faces.loadAll(basePath);
        } catch (FaceLoadException e) {
            throw new DefinitionLoadException("Failed to load face pool", e);
        }
        try {
            TextboxScript.loadAll(basePath, scripts);
        } catch (ScriptLoadException e) {
            throw new DefinitionLoadException("Failed to load scripts", e);
        }

        return new GameDefinition(id, name, description, credits, filePath, basePath, winCtx, faces, scripts);
    }

    @Override
    public String qualifiedName() {
        return name;
    }

    public ExtensionDefinition loadExtension(Path extPath) throws DefinitionLoadException {
        extPath = extPath.toAbsolutePath();
        boolean reload = false;
        int i = 0;
        for (; i < extensions.size(); i++) {
            var ext = extensions.get(i);
            if (ext.filePath().equals(extPath)) {
                reload = true;
                break;
            }
        }

        var ext = ExtensionDefinition.load(extPath);

        if (!this.id.equals(ext.baseDefinition())) {
            throw new DefinitionLoadException("Extension depends on game definition \"%s\", but we are \"%s\"!"
                    .formatted(ext.baseDefinition(), this.id));
        }

        if (reload) {
            extensions.set(i, ext);
        } else {
            extensions.add(ext);
        }
        updateExtensions();
        return ext;
    }

    public boolean unloadExtension(ExtensionDefinition ext) {
        if (extensions.remove(ext)) {
            updateExtensions();
            return true;
        }
        return false;
    }

    private void updateExtensions() {
        allFaces.clear();
        allScripts.clear();

        allFaces.addFrom(baseFaces);
        allScripts.addAll(baseScripts);

        for (var ext : extensions) {
            if (ext.faces() != null) {
                allFaces.addFrom(ext.faces());
            }
            if (ext.scripts() != null) {
                allScripts.addAll(ext.scripts());
            }
        }
    }

    public WindowContext winCtx() {
        return winCtx;
    }

    public FacePool faces() {
        return allFaces;
    }

    public List<TextboxScript> scripts() {
        return allScripts;
    }

    public List<ExtensionDefinition> extensions() {
        return extensions;
    }
}
