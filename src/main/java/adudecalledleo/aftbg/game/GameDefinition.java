package adudecalledleo.aftbg.game;

import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.util.PathAdapter;
import adudecalledleo.aftbg.util.WindowTintAdapter;
import adudecalledleo.aftbg.window.WindowTint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

import java.nio.file.Path;

@SuppressWarnings("ClassCanBeRecord")
public final class GameDefinition {
    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .registerTypeAdapter(GameDefinition.class,
                    (InstanceCreator<Object>) type -> new GameDefinition(null, null, null, null))
            .registerTypeAdapter(Path.class, new PathAdapter())
            .registerTypeAdapter(WindowTint.class, new WindowTintAdapter())
            .registerTypeAdapter(FacePool.class, new FacePool.Adapter())
            .create();

    private final String name;
    private final Path windowPath;
    private final WindowTint windowTint;
    private final Path facesPath;

    public GameDefinition(String name, Path windowPath, WindowTint windowTint, Path facesPath) {
        this.name = name;
        this.windowPath = windowPath;
        this.windowTint = windowTint;
        this.facesPath = facesPath;
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

    public Path getFacesPath() {
        return facesPath;
    }
}
