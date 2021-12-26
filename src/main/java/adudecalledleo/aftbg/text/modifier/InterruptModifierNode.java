package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;

public final class InterruptModifierNode extends ModifierNode {
    public static final char KEY = '^';

    public InterruptModifierNode(int start) {
        super(start, 2, KEY);
    }

    public static final class Parser extends ModifierParser.NoArgsParser {
        public static final String ERROR_PREFIX = "Interrupt modifier: ";

        @Override
        protected String hasArgsErrorMessage() {
            return ERROR_PREFIX + "No arguments required";
        }

        @Override
        protected void addNodes(TextParser.Context ctx, int start, NodeList nodes) {
            nodes.add(new InterruptModifierNode(start));
        }
    }
}
