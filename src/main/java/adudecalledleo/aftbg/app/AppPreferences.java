package adudecalledleo.aftbg.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.json.JsonReadUtils;
import adudecalledleo.aftbg.json.JsonWriteUtils;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class AppPreferences {
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

    public void read(int version, JsonReader reader) throws IOException {
        // in the future, the "version" property will determine how to read preferences from the object
        //  to allow for backwards compatibility with older preferences files
        // currently, though, there's only one version - the current one

        lastExtensions.clear();
        while (reader.hasNext()) {
            String field = reader.nextName();
            switch (field) {
                case Key.AUTO_UPDATE_CHECK_ENABLED -> autoUpdateCheckEnabled = reader.nextBoolean();
                case Key.LAST_GAME_DEFINITION -> lastGameDefinition = JsonReadUtils.readNullable(reader, JsonReadUtils::readPath);
                case Key.LAST_EXTENSIONS -> JsonReadUtils.readNullableArray(reader, JsonReadUtils::readPath, lastExtensions::add);
                default -> reader.skipValue();
            }
        }
    }

    public void write(JsonWriter writer) throws IOException {
        writer.name(Key.VERSION);
        writer.value(CURRENT_VERSION);
        writer.name(Key.AUTO_UPDATE_CHECK_ENABLED);
        writer.value(autoUpdateCheckEnabled);
        writer.name(Key.LAST_GAME_DEFINITION);
        JsonWriteUtils.writeNullable(writer, JsonWriteUtils::writePath, lastGameDefinition);
        writer.name(Key.LAST_EXTENSIONS);
        JsonWriteUtils.writeNullable(writer,
                (writer1, value) -> JsonWriteUtils.writeArray(writer1, JsonWriteUtils::writePath, value),
                lastExtensions);
    }

    public static void init() throws IOException {
        instance = new AppPreferences();

        if (Files.exists(SAVE_PATH)) {
            boolean gotVersion = false, needToReopen = false;
            int version = 0;
            try (JsonReader reader = JsonReader.json5(SAVE_PATH)) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String field = reader.nextName();
                    if ("version".equals(field)) {
                        version = reader.nextInt();
                        gotVersion = true;
                        break;
                    }
                    // version wasn't first entry, need to reopen file after we find it
                    needToReopen = true;
                }

                if (!gotVersion) {
                    throw new IOException("Missing version field!");
                }

                if (!needToReopen) {
                    instance.read(version, reader);
                    reader.endObject();
                }
            }

            if (needToReopen) {
                try (JsonReader reader = JsonReader.json5(SAVE_PATH)) {
                    reader.beginObject();
                    instance.read(version, reader);
                    reader.endObject();
                }
            }
        } else {
            flush();
        }
    }

    public static void flush() {
        if (instance == null) {
            return;
        }

        try (JsonWriter writer = JsonWriter.json5(SAVE_PATH)) {
            writer.beginObject();
            instance.write(writer);
            writer.endObject();
        } catch (Exception e) {
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
