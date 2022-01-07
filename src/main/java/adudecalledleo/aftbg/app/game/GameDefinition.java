package adudecalledleo.aftbg.app.game;

import java.awt.image.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import adudecalledleo.aftbg.app.face.FaceLoadException;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.script.ScriptLoadException;
import adudecalledleo.aftbg.app.script.TextboxScriptSet;
import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.app.util.WindowTintAdapter;
import adudecalledleo.aftbg.window.WindowContext;
import adudecalledleo.aftbg.window.WindowTint;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

public final class GameDefinition extends Definition {
    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(WindowTint.class, new WindowTintAdapter())
            .registerTypeAdapter(FacePool.class, new FacePool.Adapter())
            .registerTypeAdapter(TextboxScriptSet.class, new TextboxScriptSet.Adapter())
            .create();

    private final WindowContext winCtx;
    private final FacePool baseFaces;
    private final TextboxScriptSet baseScripts;

    private final List<ExtensionDefinition> extensions, extensionsU;
    private final FacePool allFaces;
    private final TextboxScriptSet allScripts;

    private GameDefinition(String name, String[] description, String[] credits,
                          Path filePath, Path basePath,
                          WindowContext winCtx, FacePool baseFaces, TextboxScriptSet baseScripts) {
        super(name, description, credits, filePath, basePath);

        this.winCtx = winCtx;
        this.baseFaces = baseFaces;
        this.baseScripts = baseScripts;

        setAsSource(this.baseFaces);
        setAsSource(this.baseScripts);

        extensions = new ArrayList<>();
        extensionsU = Collections.unmodifiableList(extensions);
        allFaces = new FacePool();
        allScripts = new TextboxScriptSet();
        updateExtensions();
    }

    public static GameDefinition load(Path filePath) throws DefinitionLoadException {
        filePath = filePath.toAbsolutePath();

        Path basePath = filePath.getParent();
        if (basePath == null) {
            throw new DefinitionLoadException("File path \"%s\" has no parent(?!)"
                    .formatted(filePath));
        }

        JsonRep jsonRep;
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            jsonRep = GSON.fromJson(reader, JsonRep.class);
        } catch (Exception e) {
            throw new DefinitionLoadException("Failed to read definition from \"%s\""
                    .formatted(filePath), e);
        }

        Path facesPath = PathUtils.tryResolve(basePath, jsonRep.facesPath, "face pool definition",
                DefinitionLoadException::new);
        FacePool faces;
        try (BufferedReader reader = Files.newBufferedReader(facesPath)) {
            faces = GSON.fromJson(reader, FacePool.class);
        } catch (Exception e) {
            throw new DefinitionLoadException("Failed to read face pool definition from \"%s\""
                    .formatted(facesPath), e);
        }

        TextboxScriptSet scripts;
        if (jsonRep.scriptsPath == null) {
            scripts = new TextboxScriptSet();
        } else {
            Path scriptsPath = PathUtils.tryResolve(basePath, jsonRep.scriptsPath, "scripts definition",
                    DefinitionLoadException::new);
            try (BufferedReader reader = Files.newBufferedReader(scriptsPath)) {
                scripts = GSON.fromJson(reader, TextboxScriptSet.class);
            } catch (Exception e) {
                throw new DefinitionLoadException("Failed to read scripts definition from \"%s\""
                        .formatted(scriptsPath), e);
            }
        }

        Path windowPath = PathUtils.tryResolve(basePath, jsonRep.windowPath, "Window image",
                DefinitionLoadException::new);
        BufferedImage windowImage;
        try (InputStream input = Files.newInputStream(windowPath)) {
            windowImage = ImageIO.read(input);
        } catch (IOException e) {
            throw new DefinitionLoadException("Failed to load Window image from \"%s\""
                    .formatted(windowPath), e);
        }

        WindowContext winCtx = new WindowContext(windowImage, jsonRep.windowTint);
        try {
            faces.loadAll(basePath);
        } catch (FaceLoadException e) {
            throw new DefinitionLoadException("Failed to load face pool", e);
        }
        try {
            scripts.loadAll(basePath);
        } catch (ScriptLoadException e) {
            throw new DefinitionLoadException("Failed to load baseScripts", e);
        }

        return new GameDefinition(jsonRep.name, jsonRep.description, jsonRep.credits, filePath, basePath, winCtx, faces, scripts);
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
        allScripts.addFrom(baseScripts);

        for (var ext : extensions) {
            allFaces.addFrom(ext.faces());
            allScripts.addFrom(ext.scripts());
        }
    }

    public WindowContext winCtx() {
        return winCtx;
    }

    public FacePool faces() {
        return allFaces;
    }

    public TextboxScriptSet scripts() {
        return allScripts;
    }

    public List<ExtensionDefinition> extensions() {
        return extensionsU;
    }

    private static final class JsonRep {
        public String name;
        public String[] description;
        public String[] credits;
        public String windowPath;
        public WindowTint windowTint;
        public String facesPath;
        public @Nullable String scriptsPath;
    }
}
