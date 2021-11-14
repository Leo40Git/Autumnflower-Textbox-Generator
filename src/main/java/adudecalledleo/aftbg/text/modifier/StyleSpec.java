package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.util.TriState;

public record StyleSpec(boolean reset,
                        TriState bold, TriState italic, TriState underline, TriState strikethrough,
                        Superscript superscript, int sizeAdjust) {
    public enum Superscript {
        DEFAULT, SUPER, MID, SUB;

        public boolean sameAs(Superscript other) {
            if (this == DEFAULT) {
                return other == DEFAULT || other == MID;
            } else if (other == DEFAULT) {
                return this == MID;
            }
            return this == other;
        }
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
        if (other.reset) {
            return other;
        } else {
            Superscript ss = other.superscript;
            if (ss == Superscript.DEFAULT) {
                ss = this.superscript;
            }
            return new StyleSpec(false,
                    this.bold.orElse(other.bold), this.italic.orElse(other.italic),
                    this.underline.orElse(other.underline), this.strikethrough.orElse(other.strikethrough),
                    ss, this.sizeAdjust + other.sizeAdjust);
        }
    }

    public StyleSpec difference(StyleSpec other) {
        if (other.reset) {
            return other;
        } else {
            TriState sBold = diffTri(this.bold, other.bold);
            TriState sItalic = diffTri(this.italic, other.italic);
            TriState sUnderline = diffTri(this.underline, other.underline);
            TriState sStrikethrough = diffTri(this.strikethrough, other.strikethrough);
            Superscript sSuperscript = Superscript.DEFAULT;
            if (!other.superscript.sameAs(this.superscript)) {
                sSuperscript = other.superscript;
            }
            int sSizeAdjust = other.sizeAdjust - this.sizeAdjust;
            return new StyleSpec(false, sBold, sItalic, sUnderline, sStrikethrough, sSuperscript, sSizeAdjust);
        }
    }

    private TriState diffTri(TriState current, TriState target) {
        if (current.toBoolean(false) == target.toBoolean(false)) {
            return TriState.DEFAULT;
        }
        return target;
    }

    public String toModifier() {
        if (DEFAULT.equals(this)) {
            return "\\" + StyleModifierNode.KEY;
        }
        StringBuilder sb = new StringBuilder("\\" + StyleModifierNode.KEY + "[");
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
        if (sizeAdjust != 0) {
            if (sizeAdjust > 0) {
                sb.append(">".repeat(sizeAdjust));
            } else {
                sb.append("<".repeat(-sizeAdjust));
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
