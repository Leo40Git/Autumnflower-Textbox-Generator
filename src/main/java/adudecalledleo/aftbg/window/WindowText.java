package adudecalledleo.aftbg.window;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// TODO "rich dialogue" support - modifiers!
public final class WindowText {
    public static final Font FONT;

    static {
        Font base;
        try (InputStream in = WindowText.class.getResourceAsStream("/font/VL-Gothic-Regular.ttf")) {
            if (in == null)
                throw new FileNotFoundException("font.tff");
            base = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FileNotFoundException e) {
            throw new InternalError("Missing embedded resource 'font.ttf'?!");
        } catch (IOException | FontFormatException e) {
            throw new InternalError("Failed to read embedded font 'font.ttf'?!", e);
        }

        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
        FONT = base.deriveFont(Font.PLAIN, 28);
    }

    private WindowText() { }

    public static void draw(Graphics2D g, String text, int x, int y) {
        var oldFont = g.getFont();
        var oldAA = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setFont(FONT);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        var fm = g.getFontMetrics();
        g.drawString(text, x, y + fm.getMaxAscent());

        g.setFont(oldFont);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAA);
    }
}
