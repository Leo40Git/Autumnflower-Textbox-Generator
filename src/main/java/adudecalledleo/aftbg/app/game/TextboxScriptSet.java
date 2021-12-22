package adudecalledleo.aftbg.app.game;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import adudecalledleo.aftbg.face.FaceLoadException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public final class TextboxScriptSet {
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
                Path path = Paths.get(in.nextString());
                set.scripts.put(name, new TextboxScript(name, path));
            }
            in.endObject();
            return set;
        }

        @Override
        public void write(JsonWriter out, TextboxScriptSet value) throws IOException {
            out.beginObject();
            for (var script : value.scripts.values()) {
                out.name(script.getName());
                out.value(script.getPath().toString());
            }
            out.endObject();
        }
    }

    private final Map<String, TextboxScript> scripts;
    private final Map<String, TextboxScript> scriptsU;

    public TextboxScriptSet() {
        scripts = new HashMap<>();
        scriptsU = Collections.unmodifiableMap(scripts);
    }

    public TextboxScript getScript(String name) {
        return scripts.get(name);
    }

    public Map<String, TextboxScript> getScripts() {
        return scriptsU;
    }

    public void loadAll(Path basePath) throws ScriptLoadException {
        for (var script : scripts.values()) {
            script.load(basePath);
        }
    }
}
