package adudecalledleo.aftbg.game;

import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.util.PathAdapter;
import adudecalledleo.aftbg.window.WindowTint;
import adudecalledleo.aftbg.util.WindowTintAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.nio.file.Path;

@SuppressWarnings("ClassCanBeRecord")
public final class GameDefinition {
    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .registerTypeAdapter(Path.class, new PathAdapter())
            .registerTypeAdapter(WindowTint.class, new WindowTintAdapter())
            .registerTypeAdapter(FacePool.class, new FacePool.Adapter())
            .create();

    public static GameDefinition read(BufferedReader reader) {
        return GSON.fromJson(reader, GameDefinition.class);
    }

    private final String name;
    private final Path windowPath;
    private final WindowTint windowTint;
    private final FacePool faces;

    public GameDefinition(String name, Path windowPath, WindowTint windowTint, FacePool faces) {
        this.name = name;
        this.windowPath = windowPath;
        this.windowTint = windowTint;
        this.faces = faces;
    }

    public String getName() {
        return name;
    }

    public Path getWindowPath() {
        return windowPath;
    }

    public WindowTint getWindowTint() {
        return windowTint;
    }

    public FacePool getFaces() {
        return faces;
    }
}
