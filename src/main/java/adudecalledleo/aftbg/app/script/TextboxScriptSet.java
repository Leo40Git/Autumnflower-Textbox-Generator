package adudecalledleo.aftbg.app.script;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adudecalledleo.aftbg.util.PathUtils;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public final class TextboxScriptSet {
    private final List<TextboxScript> scripts;
    private final List<TextboxScript> scriptsU;

    public TextboxScriptSet() {
        scripts = new ArrayList<>();
        scriptsU = Collections.unmodifiableList(scripts);
    }

    public List<TextboxScript> getScripts() {
        return scriptsU;
    }

    public void addFrom(TextboxScriptSet other) {
        scripts.addAll(other.scripts);
    }

    public void loadAll(Path basePath) throws ScriptLoadException {
        for (var script : scripts) {
            script.load(basePath);
        }
    }

    public void clear() {
        scripts.clear();
    }

    public static final class Adapter extends TypeAdapter<TextboxScriptSet> {
        @Override
        public TextboxScriptSet read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.skipValue();
                return null;
            }
            TextboxScriptSet set = new TextboxScriptSet();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                String path = null;
                String desc = null;
                switch (in.peek()) {
                case STRING -> path = in.nextString();
                case BEGIN_OBJECT -> {
                    in.beginObject();
                    while (in.hasNext()) {
                        String name2 = in.nextName();
                        switch (name2) {
                        case "path" -> path = in.nextString();
                        case "desc" -> desc = in.nextString();
                        }
                    }
                    in.endObject();
                }
                default ->
                        throw new IllegalStateException("Expected string or object for script declaration '" + name + "', "
                                + "instead got " + in.peek());
                }
                if (path == null) {
                    throw new IllegalStateException("Script declaration '" + name + "' missing required value 'path'");
                }
                set.scripts.add(new TextboxScript(name, path, desc));
            }
            in.endObject();
            return set;
        }

        @Override
        public void write(JsonWriter out, TextboxScriptSet value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            for (var script : value.scripts) {
                out.name(script.getName());
                String path = PathUtils.sanitize(script.getPath());
                String desc = script.getDescription();
                if (desc == null) {
                    out.value(path);
                } else {
                    out.beginObject();
                    out.name("path");
                    out.value(path);
                    out.name("desc");
                    out.value(desc);
                    out.endObject();
                }
            }
            out.endObject();
        }
    }
}
