package adudecalledleo.aftbg.text.modifier;

import java.util.Arrays;

import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;

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
        public static final String ERROR_PREFIX = "Style modifier: ";

        @Override
        public void parse(TextParser.Context ctx, int start, int argsStart, String args, NodeList nodes) {
            if (args == null) {
                nodes.add(new StyleModifierNode(start, 2, StyleSpec.DEFAULT));
                return;
            }

            nodes.add(new StyleModifierNode(start, ModifierParser.modLen(args),
                    StyleSpec.fromModArgs(ERROR_PREFIX, argsStart, args, nodes),
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
