package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.NodeList;

@FunctionalInterface
public interface ModifierParser {
    void parse(TextParser.Context ctx, int start, int argsStart, String args, NodeList nodes);

    abstract class NoArgsParser implements ModifierParser {
        @Override
        public void parse(TextParser.Context ctx, int start, int argsStart, String args, NodeList nodes) {
            if (args != null) {
                nodes.add(new ErrorNode(argsStart, args.length(), hasArgsErrorMessage()));
                return;
            }
            addNodes(ctx, start, nodes);
        }

        protected abstract String hasArgsErrorMessage();
        protected abstract void addNodes(TextParser.Context ctx, int start, NodeList nodes);
    }

    static int modLen(String args) {
        return args == null ? 2 : 2 + args.length() + 2;
    }
}
