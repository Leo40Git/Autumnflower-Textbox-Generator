package adudecalledleo.aftbg.app.script;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.app.Textbox;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.DefinitionObject;
import adudecalledleo.aftbg.app.script.shim.ShimHelpers;
import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.json.JsonReadUtils;
import adudecalledleo.aftbg.json.JsonWriteUtils;
import adudecalledleo.aftbg.json.MissingFieldsException;
import org.graalvm.polyglot.*;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
import org.quiltmc.json5.JsonWriter;

public final class TextboxScript extends DefinitionObject implements Closeable {
    private final String name;
    private final String path;
    private final String[] description;

    private Context ctx;
    private Value funcValue;

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
        if (ctx != null) {
            close();
        }

        Path path = PathUtils.tryResolve(basePath, this.path, "script", ScriptLoadException::new).toAbsolutePath();

        Source source;
        try (var reader = Files.newBufferedReader(path)) {
            source = Source.newBuilder("js", reader, path.toString())
                    .build();
        } catch (IOException e) {
            throw new ScriptLoadException("Failed to load script from file", e);
        }

        ctx = Context.newBuilder("js")
                .allowExperimentalOptions(true)  // have to turn on experimental options to disable APIs...
                .option("js.load", "false")             // obviously no.
                .option("js.print", "false")            // we implement print and printErr ourselves
                .option("js.console", "false")          // in-depth logging
                .option("js.polyglot-builtin", "false") // getting/setting global variables + executing other langs
                .build();
        var bindings = ctx.getBindings("js");
        bindings.putMember("input", ScriptInputHelper.INSTANCE);
        bindings.putMember("random", ScriptRandomAccess.INSTANCE);
        final String printPrefix = "[script:" + path.getFileName() + "] ";
        bindings.putMember("print", new ScriptPrintFunction() {
            @Override
            @HostAccess.Export
            public void print(String msg) {
                Main.logger().info(printPrefix + msg);
            }
        });
        bindings.putMember("printErr", new ScriptPrintFunction() {
            @Override
            @HostAccess.Export
            public void print(String msg) {
                Main.logger().error(printPrefix + msg);
            }
        });

        try {
            funcValue = ctx.eval(source);
            if (!funcValue.canExecute()) {
                funcValue = null;
                yeetContext();
                throw new ScriptLoadException("Script did not return function");
            }
        } catch (PolyglotException e) {
            yeetContext();
            if (e.isSyntaxError()) {
                throw new ScriptLoadException("Failed to evaluate script", e);
            } else {
                throw new ScriptLoadException("Unknown polyglot failure?!", e);
            }
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
        var copy = ShimHelpers.copy(box);
        funcValue.executeVoid(ShimHelpers.wrap(faces), copy);
        ShimHelpers.apply(copy, box);
    }

    @Override
    public void close() {
        funcValue = null;
        yeetContext();
    }

    private void yeetContext() {
        try {
            ctx.close(true);
        } catch (PolyglotException ignored) { }
        ctx = null;
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
