package adudecalledleo.aftbg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import de.skuzzle.semantic.Version;
import org.jetbrains.annotations.Nullable;

public final class BuildInfo {
    private static boolean loaded = false;
    private static boolean isDevelopment = false;
    private static String name, abbreviatedName;
    private static Version version;
    private static @Nullable URL updateJsonUrl, homepageUrl, issuesUrl, sourceUrl;
    private static String[] credits;

    private BuildInfo() { }

    static void setDevelopment() {
        isDevelopment = true;
    }

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
        if (jsonRep.urls == null) {
            updateJsonUrl = null;
            homepageUrl = null;
            issuesUrl = null;
            sourceUrl = null;
        } else {
            updateJsonUrl = parseUrl("update JSON", jsonRep.urls.updateJson);
            homepageUrl = parseUrl("homepage", jsonRep.urls.homepage);
            issuesUrl = parseUrl("issue tracker", jsonRep.urls.issues);
            sourceUrl = parseUrl("source code", jsonRep.urls.source);
        }
        String verStr = Objects.requireNonNull(jsonRep.version, "version");

        if ("${version}".equals(verStr)) {
            if (isDevelopment) {
                version = Version.ZERO.withPreRelease("dev");
            } else {
                throw new IOException("Version placeholder wasn't filled in?!");
            }
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

    private static @Nullable URL parseUrl(String name, @Nullable String url) throws IOException {
        if (url == null) {
            return null;
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IOException("Failed to parse %s URL".formatted(name), e);
        }
    }

    private static void assertLoaded() {
        if (!loaded) {
            throw new IllegalStateException("Build info hasn't been loaded!");
        }
    }

    public static boolean isDevelopment() {
        assertLoaded();
        return isDevelopment;
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

    public static @Nullable URL updateJsonUrl() {
        assertLoaded();
        return updateJsonUrl;
    }

    public static @Nullable URL homepageUrl() {
        assertLoaded();
        return homepageUrl;
    }

    public static @Nullable URL issuesUrl() {
        assertLoaded();
        return issuesUrl;
    }

    public static @Nullable URL sourceUrl() {
        assertLoaded();
        return sourceUrl;
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
