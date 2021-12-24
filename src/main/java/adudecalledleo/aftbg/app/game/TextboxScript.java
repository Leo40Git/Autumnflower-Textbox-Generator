package adudecalledleo.aftbg.app.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.game.shim.ShimHelpers;
import adudecalledleo.aftbg.app.game.shim.TextboxShim;
import adudecalledleo.aftbg.face.FacePool;
import jdk.dynalink.beans.StaticClass;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public final class TextboxScript {
    private static final StaticClass INPUT_CLASS = StaticClass.forClass(ScriptInputHelper.class);

    private final String name;
    private final Path path;
    private final String description;

    private ScriptObjectMirror updateTextboxFunc;

    public TextboxScript(String name, Path path, String description) {
        this.name = name;
        this.path = path;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public void load(Path basePath) throws ScriptLoadException {
        Path truePath = basePath.resolve(path).toAbsolutePath();

        ScriptEngine engine = createScriptEngine();
        engine.put(ScriptEngine.FILENAME, truePath.toString());
        Bindings bindings = engine.createBindings();
        bindings.put("input", INPUT_CLASS);

        try (BufferedReader reader = Files.newBufferedReader(truePath)) {
            engine.eval(reader, bindings);
        } catch (IOException | ScriptException e) {
            throw new ScriptLoadException("Failed to load script!", e);
        }

        Object possibleFunc = bindings.get("updateTextbox");
        if (possibleFunc instanceof ScriptObjectMirror mirror && mirror.isFunction()) {
            updateTextboxFunc = mirror;
        } else {
            throw new ScriptLoadException("Failed to find function updateTextbox in script!");
        }
    }

    public TextboxShim run(FacePool faces, Textbox box) {
        TextboxShim boxShim = ShimHelpers.copy(box);
        updateTextboxFunc.call(null, ShimHelpers.wrap(faces), boxShim);
        return boxShim;
    }

    public static ScriptEngine createScriptEngine() {
        return new NashornScriptEngineFactory().getScriptEngine("--no-java");
    }
}
