package adudecalledleo.aftbg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import de.skuzzle.semantic.Version;
import org.jetbrains.annotations.Nullable;

public final class BuildInfo {
    private static boolean loaded = false;
    private static boolean isDev = false;
    private static String name, abbreviatedName;
    private static Version version;
    private static JsonRep.URLs urls;
    private static String[] credits;

    private BuildInfo() { }

    public static void load() throws IOException {
        if (loaded) {
            return;
        }

        JsonRep jsonRep;
        try (InputStream in = openJsonStream();
             InputStreamReader reader = new InputStreamReader(in)) {
            jsonRep = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
                    .fromJson(reader, JsonRep.class);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to parse JSON", e);
        }

        name = Objects.requireNonNull(jsonRep.name, "name");
        abbreviatedName = Objects.requireNonNull(jsonRep.abbreviatedName, "abbreviated_name");
        credits = Objects.requireNonNull(jsonRep.credits, "credits");
        urls = jsonRep.urls;
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

    private static InputStream openJsonStream() throws IOException {
        var in = BuildInfo.class.getResourceAsStream("/build_info.json");
        if (in == null) {
            throw new FileNotFoundException("/build_info.json");
        }
        return in;
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

    public static @Nullable String updateJsonUrl() {
        assertLoaded();
        if (urls == null) {
            return null;
        }
        return urls.updateJson;
    }

    public static @Nullable String homepageUrl() {
        assertLoaded();
        if (urls == null) {
            return null;
        }
        return urls.homepage;
    }

    public static @Nullable String issueTrackerUrl() {
        assertLoaded();
        if (urls == null) {
            return null;
        }
        return urls.issues;
    }

    public static @Nullable String sourceCodeUrl() {
        assertLoaded();
        if (urls == null) {
            return null;
        }
        return urls.source;
    }

    public static String[] credits() {
        assertLoaded();
        return credits.clone();
    }

    private static final class JsonRep {
        public String name, abbreviatedName, version;
        public URLs urls;
        public String[] credits;

        private static final class URLs {
            public String updateJson, homepage, source, issues;
        }
    }
}
