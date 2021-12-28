package adudecalledleo.aftbg.app.game;

import adudecalledleo.aftbg.app.script.TextboxScriptSet;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.app.util.PathAdapter;
import adudecalledleo.aftbg.app.util.WindowTintAdapter;
import adudecalledleo.aftbg.window.WindowTint;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;

public final class GameDefinition {
    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Path.class, new PathAdapter())
            .registerTypeAdapter(WindowTint.class, new WindowTintAdapter())
            .registerTypeAdapter(FacePool.class, new FacePool.Adapter())
            .registerTypeAdapter(TextboxScriptSet.class, new TextboxScriptSet.Adapter())
            .create();

    private String name;
    private Path windowPath;
    private WindowTint windowTint;
    private Path facesPath;
    private TextboxScriptSet scripts;
    private String[] credits;

    public String getName() {
        return name;
    }

    public Path getWindowPath() {
        return windowPath;
    }

    public WindowTint getWindowTint() {
        return windowTint;
    }

    public Path getFacesPath() {
        return facesPath;
    }

    public TextboxScriptSet getScripts() {
        return scripts;
    }

    public String[] getCredits() {
        return credits.clone();
    }
}
