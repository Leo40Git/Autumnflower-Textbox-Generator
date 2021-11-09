package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;
import adudecalledleo.aftbg.util.TriState;

public final class StyleModifierNode extends ModifierNode {
    private final StyleSpec spec;

    public StyleModifierNode(int start, int length, StyleSpec spec, Span... argSpans) {
        super(start, length, 's', argSpans);
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
                    case '^' -> superscript = StyleSpec.Superscript.SUPER;
                    case 'v' -> superscript = StyleSpec.Superscript.SUB;
                    default -> nodes.add(new ErrorNode(argsStart + i, 1,
                            ERROR_PREFIX + "Unknown style specifier '" + chars[i] + "'"));
                }
            }

            if (invert) {
                nodes.add(new ErrorNode(argsStart + args.length() - 1, 1,
                        ERROR_PREFIX + "Invert? Invert what?"));
            }
            nodes.add(new StyleModifierNode(start, 2 + args.length() + 2,
                    new StyleSpec(reset, bold, italic, underline, strikethrough, superscript),
                    new Span(argsStart, args.length())));
        }
    }
}
