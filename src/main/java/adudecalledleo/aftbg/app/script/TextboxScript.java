package adudecalledleo.aftbg.app.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import adudecalledleo.aftbg.app.Textbox;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.DefinitionObject;
import adudecalledleo.aftbg.app.script.shim.ShimHelpers;
import adudecalledleo.aftbg.app.script.shim.TextboxShim;
import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.json.MissingFieldsException;
import jdk.dynalink.beans.StaticClass;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
import org.quiltmc.json5.JsonWriter;

public final class TextboxScript extends DefinitionObject {
    private static final ScriptEngine ENGINE = createScriptEngine();
    private static final StaticClass INPUT_CLASS = StaticClass.forClass(ScriptInputHelper.class);
    private static final Random RANDOM = new Random();

    private final String name;
    private final String path;
    private final String[] description;

    private ScriptObjectMirror updateTextboxFunc;

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

        ENGINE.put(ScriptEngine.FILENAME, path.toString());
        Bindings bindings = ENGINE.createBindings();
        bindings.put("input", INPUT_CLASS);
        bindings.put("random", RANDOM);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            ENGINE.eval(reader, bindings);
        } catch (IOException | ScriptException e) {
            throw new ScriptLoadException("Failed to load script!", e);
        }

        Object func = bindings.get("updateTextbox");
        if (func instanceof ScriptObjectMirror mirror && mirror.isFunction()) {
            updateTextboxFunc = mirror;
        } else {
            throw new ScriptLoadException("Failed to find function updateTextbox in script!");
        }
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
        TextboxShim boxShim = ShimHelpers.copy(box);
        updateTextboxFunc.call(null, ShimHelpers.wrap(faces), boxShim);
        box.setFace(ShimHelpers.unwrap(boxShim.getFace()));
        box.setText(boxShim.getText());
    }

    private static ScriptEngine createScriptEngine() {
        return new NashornScriptEngineFactory().getScriptEngine("--no-java");
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
                        case "desc", "description" -> {
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
