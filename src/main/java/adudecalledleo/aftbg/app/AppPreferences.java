package adudecalledleo.aftbg.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import adudecalledleo.aftbg.app.util.JsonUtils;
import adudecalledleo.aftbg.logging.Logger;
import com.google.gson.*;

public final class AppPreferences {
    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .create();
    private static final int CURRENT_VERSION = 1;
    private static final Path SAVE_PATH = Paths.get(".", "prefs.json").toAbsolutePath();

    private static final class Key {
        private static final String VERSION = "version";
        private static final String AUTO_UPDATE_CHECK = "auto_update_check_enabled";
    }

    private static AppPreferences instance;

    private boolean autoUpdateCheck;

    public AppPreferences() {
        // DEFAULT VALUES
        autoUpdateCheck = true;
    }

    public void read(JsonObject obj) throws JsonUtils.StructureException {
        int version = JsonUtils.getInt(obj, Key.VERSION);
        // in the future, the "version" property will determine how to read preferences from the object
        //  to allow for backwards compatibility with older preferences files
        // currently, though, there's only one version - the current one
        autoUpdateCheck = JsonUtils.getBoolean(obj, Key.AUTO_UPDATE_CHECK);
    }

    public void write(JsonObject obj) {
        obj.addProperty(Key.VERSION, CURRENT_VERSION);
        obj.addProperty(Key.AUTO_UPDATE_CHECK, autoUpdateCheck);
    }

    public static void init() throws IOException {
        if (Files.exists(SAVE_PATH)) {
            JsonObject obj;
            try (BufferedReader reader = Files.newBufferedReader(SAVE_PATH)) {
                obj = GSON.fromJson(reader, JsonObject.class);
            } catch (JsonParseException e) {
                throw new IOException("Failed to parse JSON", e);
            }
            try {
                instance = new AppPreferences();
                instance.read(obj);
            } catch (JsonUtils.StructureException e) {
                throw new IOException("Invalid preferences file", e);
            }
        } else {
            instance = new AppPreferences();
            flush();
        }
    }

    public static void flush() {
        JsonObject obj = new JsonObject();
        instance.write(obj);

        try (BufferedWriter writer = Files.newBufferedWriter(SAVE_PATH)) {
            GSON.toJson(obj, writer);
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
