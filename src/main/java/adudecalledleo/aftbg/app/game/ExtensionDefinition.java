package adudecalledleo.aftbg.app.game;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

import adudecalledleo.aftbg.app.face.FaceLoadException;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.script.ScriptLoadException;
import adudecalledleo.aftbg.app.script.TextboxScriptSet;
import adudecalledleo.aftbg.app.util.PathUtils;
import org.jetbrains.annotations.Nullable;

public final class ExtensionDefinition extends Definition {
    private final @Nullable FacePool faces;
    private final @Nullable TextboxScriptSet scripts;

    private ExtensionDefinition(String name, String[] description, String[] credits,
                                Path filePath, Path basePath,
                                @Nullable FacePool faces, @Nullable TextboxScriptSet scripts) {
        super(name, description, credits, filePath, basePath);

        this.faces = faces;
        this.scripts = scripts;

        setAsSource(this.faces);
        setAsSource(this.scripts);
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

        if (jsonRep.name == null) {
            throw new DefinitionLoadException("Name is missing!");
        }

        FacePool faces = null;
        if (jsonRep.facesPath != null) {
            Path facesPath = PathUtils.tryResolve(basePath, jsonRep.facesPath, "face pool definition",
                    DefinitionLoadException::new);
            try (BufferedReader reader = Files.newBufferedReader(facesPath)) {
                faces = GSON.fromJson(reader, FacePool.class);
            } catch (Exception e) {
                throw new DefinitionLoadException("Failed to read face pool definition from \"%s\""
                        .formatted(facesPath));
            }
        }

        TextboxScriptSet scripts = null;
        if (jsonRep.scriptsPath != null) {
            Path scriptsPath = PathUtils.tryResolve(basePath, jsonRep.scriptsPath, "scripts definition",
                    DefinitionLoadException::new);
            try (BufferedReader reader = Files.newBufferedReader(scriptsPath)) {
                scripts = GSON.fromJson(reader, TextboxScriptSet.class);
            } catch (Exception e) {
                throw new DefinitionLoadException("Failed to read scripts definition from \"%s\""
                        .formatted(scriptsPath));
            }
        }

        if (faces != null) {
            try {
                faces.loadAll(basePath);
            } catch (FaceLoadException e) {
                throw new DefinitionLoadException("Failed to load face pool", e);
            }
        }
        if (scripts != null) {
            try {
                scripts.loadAll(basePath);
            } catch (ScriptLoadException e) {
                throw new DefinitionLoadException("Failed to load scripts", e);
            }
        }

        return new ExtensionDefinition(jsonRep.name, jsonRep.description, jsonRep.credits, filePath, basePath, faces, scripts);
    }

    @Override
    public String qualifiedName() {
        return "[ext] " + name;
    }

    public @Nullable FacePool faces() {
        return faces;
    }

    public @Nullable TextboxScriptSet scripts() {
        return scripts;
    }

    private static final class JsonRep {
        public String name;
        public String[] description = DEFAULT_DESCRIPTION;
        public String[] credits = DEFAULT_CREDITS;
        public @Nullable String facesPath;
        public @Nullable String scriptsPath;
    }
}
