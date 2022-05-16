package adudecalledleo.aftbg.app;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.html.*;

import adudecalledleo.aftbg.logging.Logger;

public final class AppResources {
    public enum Icons {
        TEXTBOX_ADD, TEXTBOX_REMOVE, TEXTBOX_INSERT_BEFORE, TEXTBOX_INSERT_AFTER, TEXTBOX_CLONE, EDIT_FACE_POOL,
        TOOLBAR_BOLD, TOOLBAR_COLOR, CUT, COPY, PASTE, TOOLBAR_ITALIC, TOOLBAR_UNDERLINE, TOOLBAR_STRIKETHROUGH,
        MOD_GIMMICK, PREVIEW, PREFS, PROJECT_NEW, PROJECT_LOAD, PROJECT_SAVE, PROJECT_SAVE_AS, ABOUT,
        TOOLBAR_SUPERSCRIPT, TOOLBAR_SUBSCRIPT;

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
    private static ImageIcon arrowIcon;

    private static StyleSheet updateStyleSheet;

    private AppResources() { }

    public static void load() throws IOException {
        loadFont();
        loadIcons();
    }

    private static InputStream openResourceStream(String path) throws IOException {
        var in = AppResources.class.getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException(path);
        }
        return in;
    }

    private static void loadFont() throws IOException {
        try (InputStream in = openResourceStream("/font/VL-Gothic-Regular.ttf")) {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException e) {
            throw new IOException("Embedded font is invalid", e);
        }

        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
    }

    private static void loadIcons() throws IOException {
        BufferedImage arrowImage;
        try (InputStream in = openResourceStream("/arrow.png")) {
            arrowImage = ImageIO.read(in);
        }
        arrowIcon = new ImageIcon(arrowImage);

        BufferedImage iconSheet;
        try (InputStream in = openResourceStream("/icons.png")) {
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

    public static ImageIcon getArrowIcon() {
        if (arrowIcon == null) {
            throw new IllegalStateException("Arrow hasn't been loaded!");
        }
        return arrowIcon;
    }

    public static StyleSheet getUpdateStyleSheet() {
        if (updateStyleSheet == null) {
            loadUpdateStyleSheet();
        }
        return updateStyleSheet;
    }

    private static void loadUpdateStyleSheet() {
        updateStyleSheet = new StyleSheet();
        try (InputStream in = openResourceStream("/update.css");
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            updateStyleSheet.loadRules(reader, null);
        } catch (IOException e) {
            Logger.error("Failed to load update stylesheet, using default stylesheet instead", e);
        }
    }
}
