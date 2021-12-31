package adudecalledleo.aftbg.app.game;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

import adudecalledleo.aftbg.app.script.ScriptLoadException;
import adudecalledleo.aftbg.app.script.TextboxScriptSet;
import adudecalledleo.aftbg.face.FaceLoadException;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.util.PathUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import static adudecalledleo.aftbg.app.game.GameDefinition.GSON;

@SuppressWarnings("ClassCanBeRecord")
public final class ExtensionDefinition {
    private final String name;
    private final String[] description;
    private final String[] credits;
    private final Path filePath, basePath;
    private final FacePool faces;
    private final TextboxScriptSet scripts;

    private ExtensionDefinition(String name, String[] description, String[] credits,
                                Path filePath, Path basePath,
                                FacePool faces, TextboxScriptSet scripts) {
        this.name = name;
        this.description = description;
        this.credits = credits;
        this.filePath = filePath;
        this.basePath = basePath;
        this.faces = faces;
        this.scripts = scripts;
    }

    public static ExtensionDefinition load(Path filePath) throws DefinitionLoadException {
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

        FacePool faces;
        if (jsonRep.facesPath == null) {
            faces = new FacePool();
        } else {
            Path facesPath = PathUtils.tryResolve(basePath, jsonRep.facesPath, "face pool definition",
                    DefinitionLoadException::new);
            try (BufferedReader reader = Files.newBufferedReader(facesPath)) {
                faces = GSON.fromJson(reader, FacePool.class);
            } catch (Exception e) {
                throw new DefinitionLoadException("Failed to read face pool definition from \"%s\""
                        .formatted(facesPath));
            }
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

        try {
            faces.loadAll(basePath);
        } catch (FaceLoadException e) {
            throw new DefinitionLoadException("Failed to load face pool", e);
        }
        try {
            scripts.loadAll(basePath);
        } catch (ScriptLoadException e) {
            throw new DefinitionLoadException("Failed to load scripts", e);
        }

        return new ExtensionDefinition(jsonRep.name, jsonRep.description, jsonRep.credits, filePath, basePath, faces, scripts);
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

    public Path filePath() {
        return filePath;
    }

    public Path basePath() {
        return basePath;
    }

    public FacePool faces() {
        return faces;
    }

    public TextboxScriptSet scripts() {
        return scripts;
    }

    @ApiStatus.Internal
    public static final class JsonRep {
        public String name;
        public String[] description;
        public String[] credits;
        public @Nullable String facesPath;
        public @Nullable String scriptsPath;
    }
}
