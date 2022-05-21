package adudecalledleo.aftbg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.face.Face;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public class ScriptingTest {
    public static ScriptEngine createScriptEngine() {
        return new NashornScriptEngineFactory().getScriptEngine("--no-java");
    }

    public static void main(String[] args) {
        ScriptEngine engine = createScriptEngine();


        List<Textbox> textboxes = List.of(
                new Textbox(Face.BLANK, "Test1"),
                new Textbox(Face.BLANK, "Test2"),
                new Textbox(Face.BLANK, "Test3")
        );

        Bindings bindings = engine.createBindings();
        bindings.put("textboxes", textboxes);
        bindings.put("currentTextbox", textboxes.get(1));

        try (InputStream in = openResourceStream("/test.js");
             InputStreamReader reader = new InputStreamReader(in)) {
            engine.eval(reader, bindings);
        } catch (IOException | ScriptException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        System.out.println(textboxes.get(2).getText());

        var thing = bindings.get("doAThing");
        if (thing instanceof ScriptObjectMirror mirror && mirror.isFunction()) {
            System.out.println(mirror.call(null));
        }
    }

    private static InputStream openResourceStream(String path) throws IOException {
        var in = ScriptingTest.class.getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException(path);
        }
        return in;
    }
}
