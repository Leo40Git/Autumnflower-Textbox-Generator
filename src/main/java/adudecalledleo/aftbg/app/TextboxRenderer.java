package adudecalledleo.aftbg.app;

import java.awt.*;
import java.awt.image.*;
import java.util.List;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.text.TextParser;
import adudecalledleo.aftbg.app.text.TextRenderer;
import adudecalledleo.aftbg.app.text.node.NodeList;
import adudecalledleo.aftbg.app.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxRenderer {
    private TextboxRenderer() { }

    public static void renderOne(WindowContext winCtx, Graphics2D g, int x, int y, Face face, NodeList nodes, int arrowFrame) {
        winCtx.drawBackground(g, x + 4, y + 4, 808, 172, null);
        g.drawImage(face.getImage(), x + 18, y + 18, null);
        TextRenderer.draw(g, nodes,
                x + (face.isBlank() ? 18 : 186),
                y + 21);
        winCtx.drawBorder(g, x, y, 816, 180, null);
        if (arrowFrame >= 0) {
            winCtx.drawArrow(g, x, y, 816, 180, arrowFrame, null);
        }
    }
    
    public static BufferedImage render(WindowContext winCtx, TextParser parser, TextParser.Context parserCtx,
                                       List<Textbox> textboxes) {
        final int textboxCount = textboxes.size();
        var image = new BufferedImage(816, 180 * textboxCount + 2 * (textboxCount - 1), BufferedImage.TYPE_INT_ARGB);

        var g = image.createGraphics();
        g.setBackground(ColorUtils.TRANSPARENT);
        g.clearRect(0, 0, image.getWidth(), image.getHeight());

        for (int i = 0; i < textboxCount; i++) {
            var textbox = textboxes.get(i);
            var nodes = parser.parse(parserCtx, textbox.getText());
            if (nodes.hasErrors()) {
                return null;
            }

            g.setClip(0, 182 * i, 816, 180);
            renderOne(winCtx, g, 0, 182 * i, textbox.getFace(), nodes, i < textboxCount - 1 ? 0 : -1);
        }

        g.dispose();
        return image;
    }
}
