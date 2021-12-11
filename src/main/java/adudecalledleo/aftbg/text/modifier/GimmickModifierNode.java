package adudecalledleo.aftbg.text.modifier;

import java.util.Arrays;

import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;

public final class GimmickModifierNode extends ModifierNode {
    public static final char KEY = 'g';

    private final GimmickSpec spec;

    public GimmickModifierNode(int start, int length, GimmickSpec spec, Span... argSpans) {
        super(start, length, KEY, argSpans);
        this.spec = spec;
    }

    public GimmickSpec getSpec() {
        return spec;
    }

    public static final class Parser implements ModifierParser {
        private static final String ERROR_PREFIX = "Gimmick modifier: ";

        @Override
        public void parse(TextParser.Context ctx, int start, int argsStart, String args, NodeList nodes) {
            if (args == null) {
                nodes.add(new GimmickModifierNode(start, 2, GimmickSpec.DEFAULT));
                return;
            }

            var pair = GimmickSpec.fromModArgs(ERROR_PREFIX, argsStart, args, nodes);

            nodes.add(new GimmickModifierNode(start, 2 + args.length() + 2,
                    pair.left(), pair.right()));
        }
    }

    @Override
    public String toString() {
        return "GimmickModifierNode{" +
                "spec=" + spec +
                ", start=" + start +
                ", length=" + length +
                ", argSpans=" + Arrays.toString(argSpans) +
                '}';
    }
}
