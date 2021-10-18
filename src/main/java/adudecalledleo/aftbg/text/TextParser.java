package adudecalledleo.aftbg.text;

import adudecalledleo.aftbg.text.modifier.ModifierParser;
import adudecalledleo.aftbg.text.modifier.ModifierRegistry;
import adudecalledleo.aftbg.text.node.LineBreakNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.Node;
import adudecalledleo.aftbg.text.node.TextNode;

import java.util.ArrayList;
import java.util.List;

public final class TextParser {
    private final ModifierRegistry modifierRegistry;
    private final StringBuilder sb;
    private char[] chars;
    private List<Node> nodes;

    public TextParser() {
        modifierRegistry = new ModifierRegistry();
        sb = new StringBuilder();
    }

    public void registerModifier(char c, ModifierParser parser) {
        modifierRegistry.register(c, parser);
    }

    public List<Node> parse(String text) throws TextParserException {
        chars = text.toCharArray();
        sb.setLength(0);
        nodes = new ArrayList<>();

        boolean backslash = false;
        for (int pos = 0; pos < chars.length; pos++) {
            char c = chars[pos];
            if (backslash) {
                backslash = false;
                if (c == '\\')
                    sb.append('\\');
                else {
                    flushTextNode();
                    pos++;
                    var parser = modifierRegistry.get(c, pos);
                    if (pos < chars.length && chars[pos] == '[') {
                        final int start = pos++;
                        boolean end = false;
                        while (pos < chars.length) {
                            char c2 = chars[pos];
                            if (c2 == ']') {
                                end = true;
                                break;
                            } else {
                                sb.append(c2);
                                pos++;
                            }
                        }
                        if (!end) {
                            throw new TextParserException("Unclosed argument brackets []", start);
                        }
                        nodes.add(new ModifierNode(parser.parse(sb.toString(), start)));
                        sb.setLength(0);
                    } else
                        nodes.add(new ModifierNode(parser.parse("", pos)));
                }
            } else {
                if (c == '\\')
                    backslash = true;
                else if (c == '\n') {
                    flushTextNode();
                    nodes.add(LineBreakNode.INSTANCE);
                } else
                    sb.append(c);
            }
        }
        if (backslash) {
            throw new TextParserException("Backslash at end of text", chars.length - 1);
        }
        flushTextNode();
        mergeTextNodes();
        return nodes;
    }

    private void flushTextNode() {
        nodes.add(new TextNode(sb.toString()));
        sb.setLength(0);
    }

    private void mergeTextNodes() {
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node node = nodes.get(i);
            if (node instanceof TextNode t1) {
                Node nextNode = nodes.get(i + 1);
                if (nextNode instanceof TextNode t2) {
                    nodes.set(i, new TextNode(t1.contents() + t2.contents()));
                    nodes.remove(i + 1);
                } else if (t1.contents().isEmpty()) {
                    nodes.remove(i);
                    i--;
                }
            }
        }
    }
}
