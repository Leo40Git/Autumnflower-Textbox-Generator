package adudecalledleo.aftbg.app.game;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                set.scripts.add(new TextboxScript(name, path));
            }
            in.endObject();
            return set;
        }

        @Override
        public void write(JsonWriter out, TextboxScriptSet value) throws IOException {
            out.beginObject();
            for (var script : value.scripts) {
                out.name(script.getName());
                out.value(script.getPath().toString());
            }
            out.endObject();
        }
    }

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
}
