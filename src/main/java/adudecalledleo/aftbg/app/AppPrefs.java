package adudecalledleo.aftbg.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import adudecalledleo.aftbg.logging.Logger;
import com.google.gson.*;

public final class AppPrefs {
    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private static final int CURRENT_VERSION = 1;
    private static final Path SAVE_PATH = Paths.get(".", "prefs.json").toAbsolutePath();

    private static AppPrefs instance;

    private final int version;
    private boolean autoUpdateCheck;

    public AppPrefs(int version) {
        this.version = version;
        // DEFAULT VALUES
        autoUpdateCheck = true;
    }

    public static void init() throws IOException {
        if (Files.exists(SAVE_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(SAVE_PATH)) {
                instance = GSON.fromJson(reader, AppPrefs.class);
            } catch (JsonParseException e) {
                throw new IOException("Failed to parse JSON", e);
            }
        } else {
            instance = new AppPrefs(CURRENT_VERSION);
            flush();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(AppPrefs::flush));
    }

    public static void flush() {
        try (BufferedWriter writer = Files.newBufferedWriter(SAVE_PATH)) {
            GSON.toJson(instance, writer);
        } catch (IOException | JsonIOException e) {
            Logger.error("Failed to flush preferences!", e);
        }
    }

    public static boolean isAutoUpdateCheckEnabled() {
        return instance.autoUpdateCheck;
    }

    public static void setAutoUpdateCheckEnabled(boolean autoUpdateCheckEnabled) {
        instance.autoUpdateCheck = autoUpdateCheckEnabled;
    }
}
