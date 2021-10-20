package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.NodeList;

public interface ModifierParser {
    void parse(int start, int argsStart, String args, NodeList nodes);
}
