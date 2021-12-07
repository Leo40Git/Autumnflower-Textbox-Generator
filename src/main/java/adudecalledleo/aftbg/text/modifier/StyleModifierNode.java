package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;
import adudecalledleo.aftbg.util.TriState;

import java.util.Arrays;

public final class StyleModifierNode extends ModifierNode {
    public static final char KEY = 's';

    private final StyleSpec spec;

    public StyleModifierNode(int start, int length, StyleSpec spec, Span... argSpans) {
        super(start, length, KEY, argSpans);
        this.spec = spec;
    }

    public StyleSpec getSpec() {
        return spec;
    }

    public static final class Parser implements ModifierParser {
        private static final String ERROR_PREFIX = "Style modifier: ";

        @Override
        public void parse(int start, int argsStart, String args, NodeList nodes) {
            if (args == null) {
                nodes.add(new StyleModifierNode(start, 2, StyleSpec.DEFAULT));
                return;
            }

            boolean reset = false;
            boolean invert = false;
            TriState bold = TriState.DEFAULT;
            TriState italic = TriState.DEFAULT;
            TriState underline = TriState.DEFAULT;
            TriState strikethrough = TriState.DEFAULT;
            StyleSpec.Superscript superscript = StyleSpec.Superscript.DEFAULT;
            int sizeAdjust = 0;

            char[] chars = args.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                switch (chars[i]) {
                    case 'R', 'r' -> reset = true;
                    case '!' -> {
                        if (reset) {
                            nodes.add(new ErrorNode(argsStart + i, 1,
                                    ERROR_PREFIX + "Invert not supported when resetting"));
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
                                    ERROR_PREFIX + "Invert not supported for superscript '^' (did you mean subscript 'v'?)"));
                            invert = false;
                        }
                        superscript = StyleSpec.Superscript.SUPER;
                    }
                    case '-' -> {
                        if (invert) {
                            nodes.add(new ErrorNode(argsStart + i - 1, 1,
                                    ERROR_PREFIX + "Invert not supported for mid script '-'"));
                            invert = false;
                        }
                        superscript = StyleSpec.Superscript.MID;
                    }
                    case 'v' -> {
                        if (invert) {
                            nodes.add(new ErrorNode(argsStart + i - 1, 1,
                                    ERROR_PREFIX + "Invert not supported for subscript 'v' (did you mean superscript '^'?)"));
                            invert = false;
                        }
                        superscript = StyleSpec.Superscript.SUB;
                    }
                    case '>' -> {
                        if (invert) {
                            nodes.add(new ErrorNode(argsStart + i - 1, 1,
                                    ERROR_PREFIX + "Invert not supported for font size up '>' (did you mean font size down '<'?)"));
                            invert = false;
                        }
                        sizeAdjust++;
                    }
                    case '<' -> {
                        if (invert) {
                            nodes.add(new ErrorNode(argsStart + i - 1, 1,
                                    ERROR_PREFIX + "Invert not supported for font size down '<' (did you mean font size up '>'?)"));
                            invert = false;
                        }
                        sizeAdjust--;
                    }
                    default -> nodes.add(new ErrorNode(argsStart + i, 1,
                            ERROR_PREFIX + "Unknown style specifier '" + chars[i] + "'"));
                }
            }

            if (invert) {
                nodes.add(new ErrorNode(argsStart + args.length() - 1, 1,
                        ERROR_PREFIX + "Invert? Invert what?"));
            }

            sizeAdjust = Math.max(-4, Math.min(4, sizeAdjust));

            nodes.add(new StyleModifierNode(start, 2 + args.length() + 2,
                    new StyleSpec(reset, bold, italic, underline, strikethrough, superscript, sizeAdjust),
                    new Span(argsStart, args.length())));
        }
    }

    @Override
    public String toString() {
        return "StyleModifierNode{" +
                "spec=" + spec +
                ", start=" + start +
                ", length=" + length +
                ", argSpans=" + Arrays.toString(argSpans) +
                '}';
    }
}
