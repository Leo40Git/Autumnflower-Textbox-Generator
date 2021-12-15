package adudecalledleo.aftbg.app;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import adudecalledleo.aftbg.logging.Logger;

public final class AppPrefs {
    private AppPrefs() { }

    private static final long VERSION = 1;

    private static final String KEY_VERSION = "version";
    private static final String KEY_AUTO_UPDATE_CHECK = "auto_update_check";

    private static Preferences root;

    public static void init() {
        root = Preferences.userNodeForPackage(AppPrefs.class);
        long version = root.getLong(KEY_VERSION, 0);
        if (version == 0) {
            root.putLong(KEY_VERSION, VERSION);
        }
    }

    public static void deleteAll() {
        try {
            root.removeNode();
            root = null;
        } catch (BackingStoreException e) {
            Logger.error("Failed to delete preferences", e);
        }
    }

    public static boolean isAutoUpdateCheckEnabled() {
        return root.getBoolean(KEY_AUTO_UPDATE_CHECK, true);
    }

    public static void setAutoUpdateCheckEnabled(boolean autoUpdateCheckEnabled) {
        root.putBoolean(KEY_AUTO_UPDATE_CHECK, autoUpdateCheckEnabled);
    }
}
