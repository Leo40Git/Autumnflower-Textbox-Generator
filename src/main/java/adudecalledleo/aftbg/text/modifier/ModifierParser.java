package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.NodeList;

@FunctionalInterface
public interface ModifierParser {
    void parse(int start, int argsStart, String args, NodeList nodes);

    abstract class NoArgsParser implements ModifierParser {
        @Override
        public void parse(int start, int argsStart, String args, NodeList nodes) {
            if (args != null) {
                nodes.add(new ErrorNode(argsStart, args.length(), hasArgsErrorMessage()));
                return;
            }
            addNodes(start, nodes);
        }

        abstract String hasArgsErrorMessage();
        abstract void addNodes(int start, NodeList nodes);
    }
}
