package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;

public final class StyleModifierNode extends ModifierNode {
    public enum SuperscriptSpec {
        NONE, SUPER, SUB
    }

    public record StyleSpec(boolean bold, boolean italic, boolean underline, boolean strikethrough,
                            SuperscriptSpec superscript) {
        public static final StyleSpec DEFAULT = new StyleSpec(false, false, false, false,
                SuperscriptSpec.NONE);

        public String toModifier() {
            if (DEFAULT.equals(this)) {
                return "\\s";
            }
            StringBuilder sb = new StringBuilder("\\s[");
            if (bold) {
                sb.append('b');
            }
            if (italic) {
                sb.append('i');
            }
            if (underline) {
                sb.append('u');
            }
            if (strikethrough) {
                sb.append('s');
            }
            switch (superscript) {
                case SUPER -> sb.append('^');
                case SUB -> sb.append('v');
                case NONE -> { }
            }
            sb.append(']');
            return sb.toString();
        }
    }

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

            boolean bold = false;
            boolean italic = false;
            boolean underline = false;
            boolean strikethrough = false;
            SuperscriptSpec superscript = SuperscriptSpec.NONE;

            char[] chars = args.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                switch (chars[i]) {
                    case 'B', 'b' -> bold = true;
                    case 'I', 'i' -> italic = true;
                    case 'U', 'u' -> underline = true;
                    case 'S', 's' -> strikethrough = true;
                    case '^' -> superscript = SuperscriptSpec.SUPER;
                    case 'v' -> superscript = SuperscriptSpec.SUB;
                    default -> nodes.add(new ErrorNode(argsStart + i, 1,
                            ERROR_PREFIX + "Unknown style specifier '" + chars[i] + "'"));
                }
            }

            nodes.add(new StyleModifierNode(start, 2 + args.length() + 2,
                    new StyleSpec(bold, italic, underline, strikethrough, superscript),
                    new Span(argsStart, args.length())));
        }
    }
}
