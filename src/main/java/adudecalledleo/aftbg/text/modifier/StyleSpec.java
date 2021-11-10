package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.util.TriState;

public record StyleSpec(boolean reset,
                        TriState bold, TriState italic, TriState underline, TriState strikethrough,
                        Superscript superscript, int sizeAdjust) {
    public enum Superscript {
        DEFAULT, SUPER, MID, SUB
    }

    public static final StyleSpec DEFAULT = new StyleSpec(true,
            TriState.DEFAULT, TriState.DEFAULT, TriState.DEFAULT, TriState.DEFAULT,
            Superscript.DEFAULT, 0);

    public boolean isBold() {
        return bold.toBoolean(false);
    }

    public boolean isItalic() {
        return italic.toBoolean(false);
    }

    public boolean isUnderline() {
        return underline.toBoolean(false);
    }

    public boolean isStrikethrough() {
        return strikethrough.toBoolean(false);
    }

    public int getTrueSizeAdjust() {
        return sizeAdjust * 4;
    }

    public StyleSpec add(StyleSpec other) {
        if (other.reset()) {
            return other;
        } else {
            Superscript ss = other.superscript;
            if (ss == Superscript.DEFAULT) {
                ss = this.superscript;
            }
            return new StyleSpec(false,
                    this.bold.and(other.bold), this.italic.and(other.italic),
                    this.underline.and(other.underline), this.strikethrough.and(other.strikethrough),
                    ss, this.sizeAdjust + other.sizeAdjust);
        }
    }

    public String toModifier() {
        if (DEFAULT.equals(this)) {
            return "\\s";
        }
        StringBuilder sb = new StringBuilder("\\s[");
        if (reset) {
            sb.append('r');
        }
        appendLetter(bold, 'b', sb);
        appendLetter(italic, 'i', sb);
        appendLetter(underline, 'u', sb);
        appendLetter(strikethrough, 's', sb);
        switch (superscript) {
            case SUPER -> sb.append('^');
            case SUB -> sb.append('v');
            case MID -> {
                if (!reset) {
                    sb.append('-');
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private void appendLetter(TriState state, char c, StringBuilder sb) {
        if (state == TriState.TRUE) {
            sb.append(c);
        } else if (state == TriState.FALSE && !reset) {
            sb.append('!').append(c);
        }
    }
}
