package adudecalledleo.aftbg.app;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.html.*;

public final class AppResources {
    public static final int ICON_SIZE = 16;

    public enum Icons implements Icon {
        TEXTBOX_ADD, TEXTBOX_REMOVE, TEXTBOX_INSERT_BEFORE, TEXTBOX_INSERT_AFTER, TEXTBOX_CLONE, EDIT_FACE_POOL,
        TOOLBAR_BOLD, TOOLBAR_COLOR, CUT, COPY, PASTE, TOOLBAR_ITALIC, TOOLBAR_UNDERLINE, TOOLBAR_STRIKETHROUGH,
        TOOLBAR_LINE_BREAK, PREVIEW, PREFS, PROJECT_NEW, PROJECT_LOAD, PROJECT_SAVE, PROJECT_SAVE_AS, ABOUT,
        TOOLBAR_SUPERSCRIPT, TOOLBAR_SUBSCRIPT, UNDO, REDO;

        private int sourceX, sourceY;
        private BufferedImage imageRepr;

        private void assertLoaded() {
            if (iconSheet == null) {
                throw new IllegalStateException("Icons haven't been loaded!");
            }
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            assertLoaded();
            g.drawImage(iconSheet, x, y, x + ICON_SIZE, y + ICON_SIZE,
                    sourceX, sourceY, sourceX + ICON_SIZE, sourceY + ICON_SIZE,
                    null);
        }

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override
        public int getIconHeight() {
            return ICON_SIZE;
        }

        public BufferedImage getAsImage() {
            assertLoaded();
            if (imageRepr == null) {
                imageRepr = iconSheet.getSubimage(sourceX, sourceY, ICON_SIZE, ICON_SIZE);
            }
            return imageRepr;
        }
    }

    private static Font font;
    private static BufferedImage iconSheet;
    private static ImageIcon arrowIcon;
    private static BufferedImage expandIcon, collapseIcon;
    private static StyleSheet styleSheet;
    private static String formattingHelpContents;

    private AppResources() { }

    public static void load() throws IOException {
        loadFont();
        loadIcons();
        loadStyleSheet();
        loadFormattingHelpContents();
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

        try (InputStream in = openResourceStream("/expand.png")) {
            expandIcon = ImageIO.read(in);
        }

        try (InputStream in = openResourceStream("/collapse.png")) {
            collapseIcon = ImageIO.read(in);
        }

        try (InputStream in = openResourceStream("/icons.png")) {
            iconSheet = ImageIO.read(in);
        }
        int ix = 0, iy = 0;
        for (Icons icon : Icons.values()) {
            icon.sourceX = ix;
            icon.sourceY = iy;
            ix += 16;
            if (ix == iconSheet.getWidth()) {
                ix = 0;
                iy += 16;
            }
        }
    }

    private static void loadStyleSheet() throws IOException {
        try (InputStream in = openResourceStream("/style.css");
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            styleSheet = new StyleSheet();
            styleSheet.loadRules(reader, null);
        }
    }

    private static void loadFormattingHelpContents() throws IOException {
        try (InputStream in = openResourceStream("/formatting_help.html");
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
                if (line != null) {
                    sb.append(System.lineSeparator());
                }
            }
            formattingHelpContents = sb.toString();
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
            throw new IllegalStateException("Arrow icon hasn't been loaded!");
        }
        return arrowIcon;
    }

    public static BufferedImage getExpandIcon() {
        if (expandIcon == null) {
            throw new IllegalStateException("Expand icon hasn't been loaded!");
        }
        return expandIcon;
    }

    public static BufferedImage getCollapseIcon() {
        if (collapseIcon == null) {
            throw new IllegalStateException("Collapse icon hasn't been loaded!");
        }
        return collapseIcon;
    }

    public static StyleSheet getStyleSheet() {
        if (styleSheet == null) {
            throw new IllegalStateException("Style sheet hasn't been loaded!");
        }
        return styleSheet;
    }

    public static String getFormattingHelpContents() {
        if (formattingHelpContents == null) {
            throw new IllegalStateException("Formatting help contents haven't been loaded!");
        }
        return formattingHelpContents;
    }
}
