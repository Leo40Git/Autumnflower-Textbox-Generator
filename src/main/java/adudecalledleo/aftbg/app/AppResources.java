package adudecalledleo.aftbg.app;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;

import adudecalledleo.aftbg.util.ResourceUtils;

public final class AppResources {
    public enum Icons {
        TEXTBOX_ADD, TEXTBOX_REMOVE, TEXTBOX_INSERT_BEFORE, TEXTBOX_INSERT_AFTER, TEXTBOX_CLONE, EDIT_FACE_POOL,
        MOD_STYLE, MOD_COLOR, CUT, COPY, PASTE, MOD_FACE, MOD_DELAY, MOD_TEXT_SPEED, MOD_GIMMICK, PREVIEW,
        PREFS, PROJECT_NEW, PROJECT_LOAD, PROJECT_SAVE, PROJECT_SAVE_AS, ABOUT;

        private ImageIcon imageIcon;

        public ImageIcon get() {
            if (imageIcon == null) {
                throw new IllegalStateException("Icons haven't been loaded!");
            }
            return imageIcon;
        }

        public Image getAsImage() {
            return get().getImage();
        }
    }

    private static Font font;

    private AppResources() { }

    public static void load() throws IOException {
        loadFont();
        loadIcons();
    }

    private static void loadFont() throws IOException {
        try (InputStream in = ResourceUtils.getResourceAsStream(AppResources.class, "/font/VL-Gothic-Regular.ttf")) {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException e) {
            throw new IOException("Embedded font is invalid", e);
        }

        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
    }

    private static void loadIcons() throws IOException {
        BufferedImage iconSheet;
        try (InputStream in = ResourceUtils.getResourceAsStream(AppResources.class, "/icons.png")) {
            iconSheet = ImageIO.read(in);
        }
        int ix = 0, iy = 0;
        for (Icons icon : Icons.values()) {
            icon.imageIcon = new ImageIcon(iconSheet.getSubimage(ix, iy, 16, 16), icon.name());
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
