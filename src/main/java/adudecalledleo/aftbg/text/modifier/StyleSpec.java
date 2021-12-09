package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.NodeList;
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

    public static StyleSpec fromModArgs(String errorPrefix, int argsStart, String args, NodeList nodes) {
        boolean reset = false;
        boolean invert = false;
        TriState bold = TriState.DEFAULT;
        TriState italic = TriState.DEFAULT;
        TriState underline = TriState.DEFAULT;
        TriState strikethrough = TriState.DEFAULT;
        Superscript superscript = Superscript.DEFAULT;
        int sizeAdjust = 0;

        char[] chars = args.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
            case 'R', 'r' -> reset = true;
            case '!' -> {
                if (reset) {
                    nodes.add(new ErrorNode(argsStart + i, 1,
                            errorPrefix + "Invert not supported when resetting"));
                } else {
                    invert = true;
                }
            }
            case 'B', 'b' -> {
                bold = invert ? TriState.FALSE : TriState.TRUE;
                invert = false;
            }
            case 'I', 'i' -> {
                italic = invert ? TriState.FALSE : TriState.TRUE;
                invert = false;
            }
            case 'U', 'u' -> {
                underline = invert ? TriState.FALSE : TriState.TRUE;
                invert = false;
            }
            case 'S', 's' -> {
                strikethrough = invert ? TriState.FALSE : TriState.TRUE;
                invert = false;
            }
            case '^' -> {
                if (invert) {
                    nodes.add(new ErrorNode(argsStart + i - 1, 1,
                            errorPrefix + "Invert not supported for superscript '^' (did you mean subscript 'v'?)"));
                    invert = false;
                }
                superscript = Superscript.SUPER;
            }
            case '-' -> {
                if (invert) {
                    nodes.add(new ErrorNode(argsStart + i - 1, 1,
                            errorPrefix + "Invert not supported for mid script '-'"));
                    invert = false;
                }
                superscript = Superscript.MID;
            }
            case 'v' -> {
                if (invert) {
                    nodes.add(new ErrorNode(argsStart + i - 1, 1,
                            errorPrefix + "Invert not supported for subscript 'v' (did you mean superscript '^'?)"));
                    invert = false;
                }
                superscript = Superscript.SUB;
            }
            case '>' -> {
                if (invert) {
                    nodes.add(new ErrorNode(argsStart + i - 1, 1,
                            errorPrefix + "Invert not supported for font size up '>' (did you mean font size down '<'?)"));
                    invert = false;
                }
                sizeAdjust++;
            }
            case '<' -> {
                if (invert) {
                    nodes.add(new ErrorNode(argsStart + i - 1, 1,
                            errorPrefix + "Invert not supported for font size down '<' (did you mean font size up '>'?)"));
                    invert = false;
                }
                sizeAdjust--;
            }
            default -> nodes.add(new ErrorNode(argsStart + i, 1,
                    errorPrefix + "Unknown style specifier '" + chars[i] + "'"));
            }
        }

        if (invert) {
            nodes.add(new ErrorNode(argsStart + args.length() - 1, 1,
                    errorPrefix + "Invert? Invert what?"));
        }

        sizeAdjust = Math.max(-4, Math.min(4, sizeAdjust));

        return new StyleSpec(reset, bold, italic, underline, strikethrough, superscript, sizeAdjust);
    }

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

    public int getRealSizeAdjust() {
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
                    ss, Math.max(-4, Math.min(4, this.sizeAdjust + other.sizeAdjust)));
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
