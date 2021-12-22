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
    private final String name;
    private final Path path;

    private ScriptObjectMirror updateTextboxFunc;

    public TextboxScript(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public void load(Path basePath) throws ScriptLoadException {
        Path truePath = basePath.resolve(path).toAbsolutePath();

        ScriptEngine engine = createScriptEngine();
        engine.put(ScriptEngine.FILENAME, truePath.toString());
        Bindings bindings = engine.createBindings();
        bindings.put("input", StaticClass.forClass(ScriptInputHelper.class));

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
