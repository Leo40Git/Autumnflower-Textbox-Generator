package adudecalledleo.aftbg.window;

import adudecalledleo.aftbg.text.modifier.ColorModifier;
import adudecalledleo.aftbg.text.node.LineBreakNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.Node;
import adudecalledleo.aftbg.text.node.TextNode;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class WindowText {
    public static final Font FONT;

    private static final String FONT_PATH = "font/VL-Gothic-Regular.ttf";
    static {
        Font base;
        try (InputStream in = WindowText.class.getResourceAsStream("/" + FONT_PATH)) {
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

    private WindowText() { }

    public static void draw(Graphics2D g, List<Node> nodes, WindowColors colors, int x, int y) {
        var oldColor = g.getColor();
        var oldFont = g.getFont();
        var oldAA = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setFont(FONT);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        var fm = g.getFontMetrics();
        final int ma = fm.getMaxAscent();
        final int startX = x;

        for (Node node : nodes) {
            if (node instanceof TextNode textNode) {
                var c = g.getColor();
                // TODO figure out how to properly replicate the RPG Maker drop shadow
                g.setColor(Color.BLACK);
                for (int yo = -1; yo < 2; yo++) {
                    for (int xo = -1; xo < 2; xo++) {
                        g.drawString(textNode.contents(), x + xo, y + ma + yo);
                    }
                }
                g.setColor(c);
                g.drawString(textNode.contents(), x, y + ma);
                x += fm.stringWidth(textNode.contents());
            } else if (node instanceof ModifierNode modifierNode) {
                var mod = modifierNode.modifier();
                if (mod instanceof ColorModifier colorMod) {
                    g.setColor(colorMod.getColor(colors));
                }
            } else if (node instanceof LineBreakNode) {
                x = startX;
                y += 36;
            }
        }

        g.setColor(oldColor);
        g.setFont(oldFont);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAA);
    }
}
