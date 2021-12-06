package adudecalledleo.aftbg;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import adudecalledleo.aftbg.util.ResourceUtils;
import com.google.gson.Gson;
import de.skuzzle.semantic.Version;

public final class BuildInfo {
    private static boolean loaded = false;
    private static boolean isDev = false;
    private static String name, abbreviatedName;
    private static Version version;
    private static String[] credits;

    private BuildInfo() { }

    public static void load() throws IOException {
        if (loaded) {
            return;
        }

        JsonRep jsonRep;
        try (InputStream in = ResourceUtils.getResourceAsStream(BuildInfo.class, "/build_info.json");
             InputStreamReader reader = new InputStreamReader(in)) {
            jsonRep = new Gson().fromJson(reader, JsonRep.class);
        }

        name = Objects.requireNonNull(jsonRep.name, "name");
        abbreviatedName = Objects.requireNonNull(jsonRep.name_abbr, "name_abbr");
        credits = Objects.requireNonNull(jsonRep.credits, "credits");
        String verStr = Objects.requireNonNull(jsonRep.version, "version");

        if ("${version}".equals(verStr)) {
            isDev = true;
            version = Version.ZERO.withPreRelease("dev");
        } else {
            try {
                version = Version.parseVersion(verStr, true);
            } catch (Version.VersionFormatException e) {
                throw new IOException("Version is invalid", e);
            }
        }

        loaded = true;
    }

    private static void assertLoaded() {
        if (!loaded) {
            throw new IllegalStateException("Build info hasn't been loaded!");
        }
    }

    public static boolean isDevelopment() {
        assertLoaded();
        return isDev;
    }

    public static String name() {
        assertLoaded();
        return name;
    }

    public static String abbreviatedName() {
        assertLoaded();
        return abbreviatedName;
    }

    public static Version version() {
        assertLoaded();
        return version;
    }

    public static String[] credits() {
        assertLoaded();
        return credits;
    }

    private static final class JsonRep {
        public String name, name_abbr, version;
        public String[] credits;
    }
}
