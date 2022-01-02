package adudecalledleo.aftbg.app.script;

import java.io.IOException;
import java.nio.file.Path;
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
        private static final String[] EMPTY_DESCRIPTION = new String[0];

        @Override
        public TextboxScriptSet read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            TextboxScriptSet set = new TextboxScriptSet();
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
                            case "description" -> {
                                if (in.peek() == JsonToken.BEGIN_ARRAY) {
                                    List<String> lines = new ArrayList<>();
                                    in.beginArray();
                                    while (in.hasNext()) {
                                        lines.add(in.nextString());
                                    }
                                    in.endArray();
                                    desc = lines.toArray(EMPTY_DESCRIPTION);
                                } else {
                                    desc = new String[] { in.nextString() };
                                }
                            }
                            default -> in.skipValue();
                        }
                    }
                    in.endObject();
                } else {
                    path = in.nextString();
                }

                if (path == null) {
                    throw new IllegalStateException("Script declaration '" + scrName + "' missing required value 'path'");
                }
                set.scripts.add(new TextboxScript(scrName, path, desc));
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
                String[] desc = script.getDescription();
                if (desc.length == 0) {
                    out.value(path);
                } else {
                    out.beginObject();
                    out.name("path");
                    out.value(path);
                    out.name("description");
                    if (desc.length == 1) {
                        out.value(desc[0]);
                    } else {
                        out.beginArray();
                        for (var line : desc) {
                            out.value(line);
                        }
                        out.endArray();
                    }
                    out.endObject();
                }
            }
            out.endObject();
        }
    }
}
