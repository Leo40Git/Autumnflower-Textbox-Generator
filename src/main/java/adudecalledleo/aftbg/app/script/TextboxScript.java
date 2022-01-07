package adudecalledleo.aftbg.app.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.DefinedObject;
import adudecalledleo.aftbg.app.script.shim.ShimHelpers;
import adudecalledleo.aftbg.app.script.shim.TextboxShim;
import adudecalledleo.aftbg.app.util.PathUtils;
import jdk.dynalink.beans.StaticClass;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public final class TextboxScript extends DefinedObject {
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

    public void run(FacePool faces, Textbox box) {
        TextboxShim boxShim = ShimHelpers.copy(box);
        updateTextboxFunc.call(null, ShimHelpers.wrap(faces), boxShim);
        box.setFace(ShimHelpers.unwrap(boxShim.getFace()));
        box.setText(boxShim.getText());
    }

    private static ScriptEngine createScriptEngine() {
        return new NashornScriptEngineFactory().getScriptEngine("--no-java");
    }
}
