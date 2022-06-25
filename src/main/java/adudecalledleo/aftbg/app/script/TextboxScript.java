package adudecalledleo.aftbg.app.script;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import adudecalledleo.aftbg.app.Textbox;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.DefinitionObject;
import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.json.JsonReadUtils;
import adudecalledleo.aftbg.json.JsonWriteUtils;
import adudecalledleo.aftbg.json.MissingFieldsException;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
import org.quiltmc.json5.JsonWriter;

public final class TextboxScript extends DefinitionObject {
    private final String name;
    private final String path;
    private final String[] description;

    public TextboxScript(String name, String path, String[] description) {
        this.name = name;
        this.path = path;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String[] getDescription() {
        return description;
    }

    public void load(Path basePath) throws ScriptLoadException {
        Path path = PathUtils.tryResolve(basePath, this.path, "script", ScriptLoadException::new).toAbsolutePath();

        // TODO
    }

    public static void loadAll(Path basePath, Iterable<TextboxScript> scripts) throws ScriptLoadException {
        ScriptLoadException bigE = null;
        for (var script : scripts) {
            try {
                script.load(basePath);
            } catch (ScriptLoadException e) {
                if (bigE == null) {
                    bigE = new ScriptLoadException();
                }
                bigE.addSuppressed(new ScriptLoadException("Failed to load script \"" + script.getName() + "\"", e));
            }
        }
        if (bigE != null) {
            throw bigE;
        }
    }

    public void run(FacePool faces, Textbox box) {
        // TODO
    }

    public static final class ListAdapter {
        private static final String[] EMPTY_DESCRIPTION = new String[0];

        private ListAdapter() { }

        public static List<TextboxScript> read(JsonReader in) throws IOException {
            List<TextboxScript> scripts = new LinkedList<>();
            in.beginObject();

            while (in.hasNext()) {
                String scrName = in.nextName();
                String path = null;
                String[] desc = EMPTY_DESCRIPTION;

                if (in.peek() == JsonToken.BEGIN_OBJECT) {
                    in.beginObject();
                    while (in.hasNext()) {
                        String name = in.nextName();
                        switch (name) {
                        case "path" -> path = in.nextString();
                        case "desc", "description" -> desc = JsonReadUtils.readStringArray(in);
                        default -> in.skipValue();
                        }
                    }
                    in.endObject();
                } else {
                    path = in.nextString();
                }

                if (path == null) {
                    throw new MissingFieldsException(in, "Script declaration \"%s\"".formatted(scrName), "path");
                }
                scripts.add(new TextboxScript(scrName, path, desc));
            }

            in.endObject();
            return scripts;
        }

        public static void write(JsonWriter out, List<TextboxScript> scripts) throws IOException {
            out.beginObject();
            for (var script : scripts) {
                out.name(script.getName());
                String path = PathUtils.sanitize(script.getPath());
                String[] desc = script.getDescription();
                if (desc.length == 0) {
                    out.value(path);
                } else {
                    out.beginObject();
                    out.name("path");
                    out.value(path);
                    out.name("description");
                    JsonWriteUtils.writeStringArray(out, desc);
                    out.endObject();
                }
            }
            out.endObject();
        }
    }
}
