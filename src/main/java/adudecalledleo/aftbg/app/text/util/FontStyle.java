package adudecalledleo.aftbg.app.text.util;

public record FontStyle(boolean bold, boolean italic, boolean underline, boolean strikethrough, Superscript superscript,
                        int sizeAdjust) {
    public enum Superscript {
        MID, SUPER, SUB
    }

    public static final FontStyle DEFAULT = new FontStyle(false, false, false, false, Superscript.MID, 0);

    public FontStyle withBold(boolean bold) {
        return new FontStyle(bold, italic, underline, strikethrough, superscript, sizeAdjust);
    }

    public FontStyle withItalic(boolean italic) {
        return new FontStyle(bold, italic, underline, strikethrough, superscript, sizeAdjust);
    }

    public FontStyle withUnderline(boolean underline) {
        return new FontStyle(bold, italic, underline, strikethrough, superscript, sizeAdjust);
    }

    public FontStyle withStrikethrough(boolean strikethrough) {
        return new FontStyle(bold, italic, underline, strikethrough, superscript, sizeAdjust);
    }

    public FontStyle withSuperscript(Superscript superscript) {
        return new FontStyle(bold, italic, underline, strikethrough, superscript, sizeAdjust);
    }

    public FontStyle withSizeAdjust(int sizeAdjust) {
        return new FontStyle(bold, italic, underline, strikethrough, superscript, sizeAdjust);
    }
}
