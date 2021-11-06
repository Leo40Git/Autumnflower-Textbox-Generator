package adudecalledleo.aftbg.app;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class AppResources {
    public enum Icons {
        TEXTBOX_ADD, TEXTBOX_REMOVE, TEXTBOX_INSERT_BEFORE, TEXTBOX_INSERT_AFTER, TEXTBOX_CLONE, EDIT_FACE_POOL,
        MOD_STYLE, MOD_COLOR, CUT, COPY, PASTE;

        public ImageIcon get() {
            if (icons == null) {
                throw new IllegalStateException("Icons haven't been loaded!");
            }
            return icons.get(this);
        }
    }

    private static Font font;
    private static Map<Icons, ImageIcon> icons;

    private AppResources() { }

    public static void load() throws IOException {
        loadFont();
        loadIcons();
    }

    private static InputStream getResource(String path) throws IOException {
        var in = AppResources.class.getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException(path);
        }
        return in;
    }

    private static void loadFont() throws IOException {
        try (InputStream in = getResource("/font/VL-Gothic-Regular.ttf")) {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException e) {
            throw new IOException("Embedded font is invalid", e);
        }

        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
    }

    private static void loadIcons() throws IOException {
        BufferedImage iconSheet;
        try (InputStream in = getResource("/icons.png")) {
            iconSheet = ImageIO.read(in);
        }
        icons = new HashMap<>();
        int ix = 0, iy = 0;
        for (Icons icon : Icons.values()) {
            icons.put(icon, new ImageIcon(iconSheet.getSubimage(ix, iy, 16, 16), icon.name()));
            ix += 16;
            if (ix == iconSheet.getWidth()) {
                ix = 0;
                iy += 16;
            }
        }
    }

    public static Font getFont() {
        if (font == null) {
            throw new IllegalStateException("Font hasn't been loaded!");
        }
        return font;
    }
}
