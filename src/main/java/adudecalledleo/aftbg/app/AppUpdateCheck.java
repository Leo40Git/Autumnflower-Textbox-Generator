package adudecalledleo.aftbg.app;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.app.util.VersionAdapter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import de.skuzzle.semantic.Version;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public final class AppUpdateCheck {
    private AppUpdateCheck() { }

    private static URL updateJsonUrl;

    public static void init() {
        String updateJsonUrlRaw = BuildInfo.updateJsonUrl();
        if (updateJsonUrlRaw != null) {
            try {
                updateJsonUrl = new URL(updateJsonUrlRaw);
            } catch (MalformedURLException e) {
                Logger.error("Update JSON URL is malformed!", e);
            }
        }
    }

    public static boolean isAvailable() {
        return updateJsonUrl != null;
    }

    public static final class CheckFailedException extends Exception {
        private CheckFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        private CheckFailedException(String message) {
            super(message);
        }
    }

    public static void doCheck(Component parent, LoadFrame loadFrame) throws CheckFailedException {
        if (updateJsonUrl == null) {
            return;
        }

        JsonRep jsonRep;
        try (InputStreamReader isr = new InputStreamReader(updateJsonUrl.openStream());
             BufferedReader reader = new BufferedReader(isr)) {
            jsonRep = new GsonBuilder()
                    .setLenient()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(Version.class, new VersionAdapter())
                    .create()
                    .fromJson(reader, JsonRep.class);
        } catch (Exception e) {
            throw new CheckFailedException("Failed to download JSON from " + updateJsonUrl, e);
        }

        if (jsonRep.latestVersion == null) {
            throw new CheckFailedException("Latest version is null?!");
        }

        if (BuildInfo.version().compareTo(jsonRep.latestVersion) < 0) {
            /// we have a new version!

            // parse and render changelogs for dialog
            List<Extension> extensions = List.of(
                    AutolinkExtension.create(),
                    StrikethroughExtension.create()
            );
            Parser parser = Parser.builder()
                    .extensions(extensions)
                    .build();
            HtmlRenderer renderer = HtmlRenderer.builder()
                    .extensions(extensions)
                    .escapeHtml(true)
                    .sanitizeUrls(true)
                    .build();

            String renderedBlock = renderer.render(parser.parse(jsonRep.changelogs.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> "### " + entry.getKey() + "\n" + String.join("\n", entry.getValue()))
                    .collect(Collectors.joining("---\n"))));

            // TODO actually implement dialog
            Logger.trace("RENDERED BLOCK:\n" + renderedBlock);
        }
    }

    private static final class JsonRep {
        public Version latestVersion;
        public Map<Version, String[]> changelogs;
    }
}
