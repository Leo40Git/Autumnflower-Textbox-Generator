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

import adudecalledleo.aftbg.app.script.ScriptLoadException;
import adudecalledleo.aftbg.app.script.TextboxScriptSet;
import adudecalledleo.aftbg.app.util.WindowTintAdapter;
import adudecalledleo.aftbg.face.FaceLoadException;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.util.PathUtils;
import adudecalledleo.aftbg.window.WindowContext;
import adudecalledleo.aftbg.window.WindowTint;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class GameDefinition {
    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(WindowTint.class, new WindowTintAdapter())
            .registerTypeAdapter(FacePool.class, new FacePool.Adapter())
            .registerTypeAdapter(TextboxScriptSet.class, new TextboxScriptSet.Adapter())
            .create();

    private final String name;
    private final String[] description;
    private final String[] credits;
    private final Path basePath;
    private final WindowContext winCtx;
    private final FacePool baseFaces;
    private final TextboxScriptSet baseScripts;

    private final List<ExtensionDefinition> extensions, extensionsU;
    private final FacePool allFaces;
    private final TextboxScriptSet allScripts;

    public GameDefinition(String name, String[] description, String[] credits,
                          Path basePath,
                          WindowContext winCtx, FacePool baseFaces, TextboxScriptSet baseScripts) {
        this.name = name;
        this.description = description;
        this.credits = credits;
        this.basePath = basePath;
        this.winCtx = winCtx;
        this.baseFaces = baseFaces;
        this.baseScripts = baseScripts;

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
                    .formatted(filePath));
        }

        Path facesPath = PathUtils.tryResolve(basePath, jsonRep.facesPath, "face pool definition",
                DefinitionLoadException::new);
        FacePool faces;
        try (BufferedReader reader = Files.newBufferedReader(facesPath)) {
            faces = GSON.fromJson(reader, FacePool.class);
        } catch (Exception e) {
            throw new DefinitionLoadException("Failed to read face pool definition from \"%s\""
                    .formatted(facesPath));
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
                        .formatted(scriptsPath));
            }
        }

        Path windowPath = PathUtils.tryResolve(basePath, jsonRep.windowPath, "Window image",
                DefinitionLoadException::new);
        BufferedImage windowImage;
        try (InputStream input = Files.newInputStream(windowPath)) {
            windowImage = ImageIO.read(input);
        } catch (IOException e) {
            throw new DefinitionLoadException("Failed to load Window image from \"%s\""
                    .formatted(windowPath));
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

        return new GameDefinition(jsonRep.name, jsonRep.description, jsonRep.credits, basePath, winCtx, faces, scripts);
    }

    public void loadExtension(Path extPath) throws DefinitionLoadException {
        var ext = ExtensionDefinition.load(extPath);
        extensions.add(ext);
        updateExtensions();
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

    public String name() {
        return name;
    }

    public String[] description() {
        return description;
    }

    public String[] credits() {
        return credits;
    }

    public Path basePath() {
        return basePath;
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

    @ApiStatus.Internal
    public static final class JsonRep {
        public String name;
        public String[] description;
        public String[] credits;
        public String windowPath;
        public WindowTint windowTint;
        public String facesPath;
        public @Nullable String scriptsPath;
    }
}
