package adudecalledleo.aftbg.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.app.util.InvalidPathURIException;
import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.app.util.json.JsonStructureException;
import adudecalledleo.aftbg.app.util.json.JsonType;
import adudecalledleo.aftbg.app.util.json.JsonUtils;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

public final class AppPreferences {
    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .create();
    private static final int CURRENT_VERSION = 1;
    private static final Path SAVE_PATH = Paths.get(".", "prefs.json").toAbsolutePath();

    private static final class Key {
        private static final String VERSION = "version";
        private static final String AUTO_UPDATE_CHECK_ENABLED = "auto_update_check_enabled";
        private static final String LAST_GAME_DEFINITION = "last_game_definition";
        private static final String LAST_EXTENSIONS = "last_extensions";
    }

    private static AppPreferences instance;

    private boolean autoUpdateCheckEnabled;
    private @Nullable Path lastGameDefinition;
    private final Set<Path> lastExtensions;

    public AppPreferences() {
        // DEFAULT VALUES
        autoUpdateCheckEnabled = true;
        lastGameDefinition = null;
        lastExtensions = new LinkedHashSet<>();
    }

    public void read(JsonObject obj) throws JsonStructureException {
        int version = JsonUtils.getInt(obj, Key.VERSION);
        // in the future, the "version" property will determine how to read preferences from the object
        //  to allow for backwards compatibility with older preferences files
        // currently, though, there's only one version - the current one

        autoUpdateCheckEnabled = JsonUtils.getBoolean(obj, Key.AUTO_UPDATE_CHECK_ENABLED);
        lastGameDefinition = JsonUtils.getPathNullable(obj, Key.LAST_GAME_DEFINITION);

        lastExtensions.clear();
        var lastExtsArr = JsonUtils.getArrayNullable(obj, Key.LAST_EXTENSIONS);
        if (lastExtsArr != null) {
            for (int i = 0, size = lastExtsArr.size(); i < size; i++) {
                var elem = lastExtsArr.get(i);
                if (elem instanceof JsonPrimitive prim && prim.isString()) {
                    try {
                        lastExtensions.add(PathUtils.fromRawUri(prim.getAsString()));
                    } catch (URISyntaxException | InvalidPathURIException e) {
                        throw new JsonStructureException(("Expected element at index %d of array property \"%s\" to be a path URI, "
                                + "but failed to convert it into a path!").formatted(i, Key.LAST_EXTENSIONS), e);
                    }
                } else {
                    throw JsonUtils.createWrongTypeException(Key.LAST_EXTENSIONS, i, JsonType.STRING, elem);
                }
            }
        }
    }

    public void write(JsonObject obj) {
        obj.addProperty(Key.VERSION, CURRENT_VERSION);
        obj.addProperty(Key.AUTO_UPDATE_CHECK_ENABLED, autoUpdateCheckEnabled);
        JsonUtils.putPath(obj, Key.LAST_GAME_DEFINITION, lastGameDefinition);
        JsonArray lastExtsArr = new JsonArray();
        for (var path : lastExtensions) {
            lastExtsArr.add(path.toUri().toString());
        }
        obj.add(Key.LAST_EXTENSIONS, lastExtsArr);
    }

    public static void init() throws IOException {
        if (Files.exists(SAVE_PATH)) {
            JsonObject obj;
            try (BufferedReader reader = Files.newBufferedReader(SAVE_PATH, StandardCharsets.UTF_8)) {
                obj = GSON.fromJson(reader, JsonObject.class);
            } catch (JsonParseException e) {
                throw new IOException("Failed to parse JSON", e);
            }
            try {
                instance = new AppPreferences();
                instance.read(obj);
            } catch (JsonStructureException e) {
                throw new IOException("Invalid preferences file", e);
            }
        } else {
            instance = new AppPreferences();
            flush();
        }
    }

    public static void flush() {
        if (instance == null) {
            return;
        }

        JsonObject obj = new JsonObject();
        instance.write(obj);

        try (BufferedWriter writer = Files.newBufferedWriter(SAVE_PATH, StandardCharsets.UTF_8)) {
            GSON.toJson(obj, writer);
        } catch (IOException | JsonIOException e) {
            Main.logger().error("Failed to flush preferences!", e);
        }
    }

    public static boolean isAutoUpdateCheckEnabled() {
        return instance.autoUpdateCheckEnabled;
    }

    public static void setAutoUpdateCheckEnabled(boolean autoUpdateCheckEnabled) {
        instance.autoUpdateCheckEnabled = autoUpdateCheckEnabled;
    }

    public static @Nullable Path getLastGameDefinition() {
        return instance.lastGameDefinition;
    }

    public static void setLastGameDefinition(@Nullable Path lastGameDefinition) {
        instance.lastGameDefinition = lastGameDefinition;
    }

    public static Set<Path> getLastExtensions() {
        return instance.lastExtensions;
    }
}
