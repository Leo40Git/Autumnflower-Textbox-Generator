package adudecalledleo.aftbg.text;

import adudecalledleo.aftbg.text.modifier.ColorModifierNode;
import adudecalledleo.aftbg.text.node.LineBreakNode;
import adudecalledleo.aftbg.text.node.Node;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.TextNode;
import adudecalledleo.aftbg.window.WindowColors;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class TextRenderer {
    public static final Color OUTLINE_COLOR = new Color(0, 0, 0, 127);
    public static final int OUTLINE_WIDTH = 4;
    private static final Stroke OUTLINE_STROKE = new BasicStroke(OUTLINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    // this secondary outline is drawn with the actual text color, to try and replicate RPG Maker MV (AKA browser) AA
    public static final int OUTLINE2_WIDTH = 1;
    public static final float OUTLINE2_OPAQUENESS = 0.275f;
    private static final Stroke OUTLINE2_STROKE = new BasicStroke(OUTLINE2_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
    public static final Composite OUTLINE2_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OUTLINE2_OPAQUENESS);

    public static final Font FONT;

    private static final String FONT_PATH = "/font/VL-Gothic-Regular.ttf";
    // region Font loading stuff
    static {
        Font base;
        try (InputStream in = TextRenderer.class.getResourceAsStream(FONT_PATH)) {
            if (in == null)
                throw new FileNotFoundException(FONT_PATH);
            base = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FileNotFoundException e) {
            throw new InternalError("Missing embedded resource '" + FONT_PATH + "'?!");
        } catch (IOException | FontFormatException e) {
            throw new InternalError("Failed to read embedded font '" + FONT_PATH + "'?!", e);
        }

        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
        FONT = base.deriveFont(Font.PLAIN, 28);
    }
    // endregion

    private TextRenderer() { }

    public static void loadFont() { /* <clinit> */ }

    public static void draw(Graphics2D g, NodeList nodes, WindowColors colors, int x, int y) {
        // region Save current state
        final var oldColor = g.getColor();
        final var oldStroke = g.getStroke();
        final var oldFont = g.getFont();
        final var oldComposite = g.getComposite();
        final var oldHints = g.getRenderingHints();
        // endregion

        g.setFont(FONT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        final var frc = g.getFontRenderContext();
        final int ma = g.getFontMetrics().getMaxAscent();
        final int startX = x;

        for (Node node : nodes) {
            if (node instanceof TextNode textNode) {
                if (textNode.getContents().isEmpty()) {
                    continue;
                }

                // generate an outline of the text
                var layout = new TextLayout(textNode.getContents(), FONT, frc);
                var outline = layout.getOutline(AffineTransform.getTranslateInstance(x, y + ma));

                // draw a transparent outline of the text...
                var c = g.getColor(); // (save the actual text color for later)
                g.setStroke(OUTLINE_STROKE);
                g.setColor(OUTLINE_COLOR);
                g.draw(outline);

                g.setColor(c);
                // ...then draw a secondary outline...
                g.setComposite(OUTLINE2_COMPOSITE);
                g.setStroke(OUTLINE2_STROKE);
                g.draw(outline);
                // ...and then, fill in the text!
                g.setComposite(oldComposite);
                g.fill(outline);
                // advance X by "advance" (text width + padding, I think?)
                x += layout.getAdvance();
            } else if (node instanceof ColorModifierNode colorModNode) {
                g.setColor(colorModNode.getColor(colors));
            } else if (node instanceof LineBreakNode) {
                x = startX;
                y += 36;
            }
        }

        // region Restore old state
        g.setColor(oldColor);
        g.setStroke(oldStroke);
        g.setFont(oldFont);
        g.setComposite(oldComposite);
        g.setRenderingHints(oldHints);
        // endregion
    }
}
