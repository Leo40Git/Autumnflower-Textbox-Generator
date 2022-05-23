package adudecalledleo.aftbg.app;

import java.awt.*;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.*;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.dialog.UpdateAvailableDialog;
import adudecalledleo.aftbg.app.util.VersionAdapter;
import adudecalledleo.aftbg.json.JsonReadUtils;
import de.skuzzle.semantic.Version;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;

public final class AppUpdateCheck {
    private AppUpdateCheck() { }

    public static boolean isAvailable() {
        return BuildInfo.updateJsonUrl() != null;
    }

    public static final class CheckFailedException extends Exception {
        private CheckFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        private CheckFailedException(String message) {
            super(message);
        }
    }

    // apparently Swing won't style unknown tags, so this class makes <del> tags render as <span class="del"> tags
    private static final class DelWorkaround implements HtmlNodeRendererFactory {
        @Override
        public NodeRenderer create(HtmlNodeRendererContext context) {
            return new Renderer(context);
        }

        private record Renderer(HtmlNodeRendererContext ctx) implements NodeRenderer {
            private static final Set<Class<? extends Node>> NODE_TYPES = Set.of(Strikethrough.class);

            @Override
            public Set<Class<? extends Node>> getNodeTypes() {
                return NODE_TYPES;
            }

            @Override
            public void render(Node node) {
                Map<String, String> attrs = ctx.extendAttributes(node, "span", Map.of("class", "del"));
                var writer = ctx.getWriter();
                writer.tag("span", attrs);
                renderChildren(node);
                writer.tag("/span");
            }

            private void renderChildren(Node parent) {
                Node node = parent.getFirstChild();
                while (node != null) {
                    Node next = node.getNext();
                    ctx.render(node);
                    node = next;
                }
            }
        }
    }

    public static void doCheck(Component parent, LoadFrame loadFrame, boolean automatic) throws CheckFailedException {
        if (BuildInfo.updateJsonUrl() == null) {
            return;
        }

        Version latestVersion = null;
        @Nullable String latestVersionDownload = null;
        Map<Version, String[]> changelogs = new LinkedHashMap<>();
        try (InputStreamReader isr = new InputStreamReader(BuildInfo.updateJsonUrl().openStream(), StandardCharsets.UTF_8);
             JsonReader reader = JsonReader.json5(isr)) {
            reader.beginObject();
            while (reader.hasNext()) {
                String field = reader.nextName();
                switch (field) {
                    case "latest_version" -> latestVersion = VersionAdapter.read(reader);
                    case "latest_version_download" -> latestVersionDownload = reader.nextString();
                    case "changelogs" -> JsonReadUtils.readSimpleMap(reader, Version::parseVersion,
                            JsonReadUtils::readStringArray, changelogs::put);
                    default -> reader.skipValue();
                }
            }
            reader.endObject();
        } catch (Exception e) {
            throw new CheckFailedException("Failed to download JSON from " + BuildInfo.updateJsonUrl(), e);
        }

        if (latestVersion == null) {
            throw new CheckFailedException("Update JSON is malformed - missing field missing_versions");
        }

        if (BuildInfo.version().compareTo(latestVersion) < 0) {
            /// we have a new version!

            // get download URL
            URL dlUrl;
            if (latestVersionDownload == null) {
                // if latest version DL link isn't specified, assume homepage
                dlUrl = BuildInfo.homepageUrl();
            } else {
                try {
                    dlUrl = new URL(latestVersionDownload);
                } catch (MalformedURLException e) {
                    throw new CheckFailedException("Failed to parse latest version download URL", e);
                }
            }

            // parse and render changelogs for dialog
            List<Extension> extensions = List.of(
                    AutolinkExtension.create(),
                    StrikethroughExtension.create()
            );
            Parser parser = Parser.builder()
                    .extensions(extensions)
                    .build();
            HtmlRenderer renderer = HtmlRenderer.builder()
                    .nodeRendererFactory(new DelWorkaround()) // must be higher than the actual del extension
                    .extensions(extensions)
                    .escapeHtml(true)
                    .sanitizeUrls(true)
                    .build();

            String renderedBlock = renderer.render(parser.parse(changelogs.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                    .map(entry -> "### " + entry.getKey() + "\n" + String.join("\n", entry.getValue()))
                    .collect(Collectors.joining("\n---\n"))));
            renderedBlock = "<html><body>" + renderedBlock + "</html></body>";

            boolean wasAOT = loadFrame.isAlwaysOnTop();
            loadFrame.setAlwaysOnTop(false);
            var dialog = new UpdateAvailableDialog(parent, renderedBlock, dlUrl);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            loadFrame.setAlwaysOnTop(wasAOT);
        } else if (!automatic) {
            boolean wasAOT = loadFrame.isAlwaysOnTop();
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(parent, "Application is up to date!",
                    "Check for Updates", JOptionPane.INFORMATION_MESSAGE);
            loadFrame.setAlwaysOnTop(wasAOT);
        }
    }
}
