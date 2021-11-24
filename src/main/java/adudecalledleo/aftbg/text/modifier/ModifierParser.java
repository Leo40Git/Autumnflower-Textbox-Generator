package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.NodeList;

@FunctionalInterface
public interface ModifierParser {
    void parse(int start, int argsStart, String args, NodeList nodes);
}
