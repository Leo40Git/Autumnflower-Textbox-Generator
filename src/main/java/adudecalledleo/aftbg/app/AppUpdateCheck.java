package adudecalledleo.aftbg.app;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.dialog.UpdateAvailableDialog;
import adudecalledleo.aftbg.app.util.VersionAdapter;
import adudecalledleo.aftbg.logging.Logger;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import de.skuzzle.semantic.Version;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.ins.Ins;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;

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

    // apparently Swing won't style unknown tags, so this class makes <del> and <ins> tags
    //  render as <span> tags with the del/ins class
    private static final class InsDelWorkaround implements HtmlNodeRendererFactory {
        @Override
        public NodeRenderer create(HtmlNodeRendererContext context) {
            return new Renderer(context);
        }

        private record Renderer(HtmlNodeRendererContext ctx) implements NodeRenderer {
            private static final Set<Class<? extends Node>> NODE_TYPES = Set.of(Strikethrough.class, Ins.class);

            @Override
            public Set<Class<? extends Node>> getNodeTypes() {
                return NODE_TYPES;
            }

            @Override
            public void render(Node node) {
                String tagName;
                if (node instanceof Strikethrough) {
                    tagName = "del";
                } else if (node instanceof Ins) {
                    tagName = "ins";
                } else {
                    return;
                }

                Map<String, String> attrs = new HashMap<>();
                attrs.put("class", tagName);
                ctx.extendAttributes(node, tagName, attrs);
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

    public static void doCheck(Component parent, LoadFrame loadFrame) throws CheckFailedException {
        if (BuildInfo.updateJsonUrl() == null) {
            return;
        }

        JsonRep jsonRep;
        try (InputStreamReader isr = new InputStreamReader(BuildInfo.updateJsonUrl().openStream(), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            jsonRep = new GsonBuilder()
                    .setLenient()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(Version.class, new VersionAdapter())
                    .create()
                    .fromJson(reader, JsonRep.class);
        } catch (Exception e) {
            throw new CheckFailedException("Failed to download JSON from " + BuildInfo.updateJsonUrl(), e);
        }

        if (jsonRep.latestVersion == null) {
            throw new CheckFailedException("Latest version is null?!");
        }

        if (BuildInfo.version().compareTo(jsonRep.latestVersion) < 0) {
            /// we have a new version!

            // parse and render changelogs for dialog
            List<Extension> extensions = List.of(
                    AutolinkExtension.create(),
                    StrikethroughExtension.create(),
                    InsExtension.create()
            );
            Parser parser = Parser.builder()
                    .extensions(extensions)
                    .build();
            HtmlRenderer renderer = HtmlRenderer.builder()
                    .nodeRendererFactory(new InsDelWorkaround()) // must be higher than the actual ins/del extensions
                    .extensions(extensions)
                    .escapeHtml(true)
                    .sanitizeUrls(true)
                    .build();

            // FIXME remove this!
            jsonRep.changelogs.put(Version.create(9, 9, 9), new String[] {
                    "~~testing addons~~  ",
                    "++abcd++  ",
                    "https://google.com"
            });

            String renderedBlock = renderer.render(parser.parse(jsonRep.changelogs.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> "### " + entry.getKey() + "\n" + String.join("\n", entry.getValue()))
                    .collect(Collectors.joining("\n---\n"))));
            renderedBlock = "<html><body>\n" + renderedBlock + "</html></body>";

            boolean wasAOT = loadFrame.isAlwaysOnTop();
            loadFrame.setAlwaysOnTop(false);
            Logger.trace(renderedBlock);
            var dialog = new UpdateAvailableDialog(parent, renderedBlock);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            loadFrame.setAlwaysOnTop(wasAOT);
        }
    }

    private static final class JsonRep {
        public Version latestVersion;
        public Map<Version, String[]> changelogs;
    }
}
