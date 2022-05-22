package adudecalledleo.aftbg.app.game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import adudecalledleo.aftbg.app.face.FaceLoadException;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.script.ScriptLoadException;
import adudecalledleo.aftbg.app.script.TextboxScript;
import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.json.JsonReadUtils;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;

public final class ExtensionDefinition extends Definition {
    private final String baseDefinition;
    private final @Nullable FacePool faces;
    private final @Nullable List<TextboxScript> scripts;

    private ExtensionDefinition(String id, String name, String[] description, String[] credits,
                                Path filePath, Path basePath, String baseDefinition,
                                @Nullable FacePool faces, @Nullable List<TextboxScript> scripts) {
        super(id, name, description, credits, filePath, basePath);

        this.baseDefinition = baseDefinition;
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

        String id = null, baseDefinition = null, name = null;
        String[] description = DEFAULT_DESCRIPTION, credits = DEFAULT_CREDITS;
        @Nullable String facesPathRaw = null;
        @Nullable String scriptsPathRaw = null;
        try (JsonReader reader = JsonReader.json5(filePath)) {
            reader.beginObject();
            while (reader.hasNext()) {
                String field = reader.nextName();
                switch (field) {
                case "id" -> id = reader.nextString();
                case "base_def", "base_definition" -> baseDefinition = reader.nextString();
                case "name" -> name = reader.nextString();
                case "desc", "description" -> description = JsonReadUtils.readStringArray(reader);
                case "credits" -> credits = JsonReadUtils.readStringArray(reader);
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
        if (baseDefinition == null) {
            missingFields.add("base_definition");
        }

        if (!missingFields.isEmpty()) {
            throw new DefinitionLoadException("Extension definition is missing following fields: %s"
                    .formatted(String.join(", ", missingFields)));
        }

        FacePool faces = null;
        if (facesPathRaw != null) {
            Path facesPath = PathUtils.tryResolve(basePath, facesPathRaw, "face pool definition",
                    DefinitionLoadException::new);
            try (JsonReader reader = JsonReader.json5(facesPath)) {
                faces = FacePool.Adapter.read(reader);
            } catch (Exception e) {
                throw new DefinitionLoadException("Failed to read face pool definition from \"%s\""
                        .formatted(facesPath));
            }
        }

        List<TextboxScript> scripts = null;
        if (scriptsPathRaw != null) {
            Path scriptsPath = PathUtils.tryResolve(basePath, scriptsPathRaw, "scripts definition",
                    DefinitionLoadException::new);
            try (JsonReader reader = JsonReader.json5(scriptsPath)) {
                scripts = TextboxScript.ListAdapter.read(reader);
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
                TextboxScript.loadAll(basePath, scripts);
            } catch (ScriptLoadException e) {
                throw new DefinitionLoadException("Failed to load scripts", e);
            }
        }

        return new ExtensionDefinition(id, name, description, credits, filePath,
                basePath, baseDefinition, faces, scripts);
    }

    @Override
    public String qualifiedName() {
        return "[ext] " + name;
    }

    public String baseDefinition() {
        return baseDefinition;
    }

    public @Nullable FacePool faces() {
        return faces;
    }

    public @Nullable List<TextboxScript> scripts() {
        return scripts;
    }
}
