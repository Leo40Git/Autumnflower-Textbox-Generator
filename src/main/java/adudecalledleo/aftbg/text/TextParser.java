package adudecalledleo.aftbg.text;

import adudecalledleo.aftbg.text.modifier.ModifierParser;
import adudecalledleo.aftbg.text.modifier.ModifierRegistry;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.LineBreakNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.TextNode;

public final class TextParser {
    private final ModifierRegistry modifierRegistry;
    private final StringBuilder sb;
    private int textStart, textLength;
    private NodeList nodes;

    public TextParser() {
        modifierRegistry = new ModifierRegistry();
        sb = new StringBuilder();
    }

    public void registerModifier(char c, ModifierParser parser) {
        modifierRegistry.register(c, parser);
    }

    public NodeList parse(String text) {
        char[] chars = text.toCharArray();
        sb.setLength(0);
        nodes = new NodeList();
        textStart = 0;
        textLength = 0;

        boolean backslash = false;
        int pos;
        for (pos = 0; pos < chars.length; pos++) {
            char c = chars[pos];
            if (backslash) {
                backslash = false;
                if (c == '\\') {
                    sb.append('\\');
                    textLength += 2;
                } else {
                    flushTextNode();
                    int modStartPos = pos++ - 1;
                    var parser = modifierRegistry.get(c);
                    if (pos < chars.length && chars[pos] == '[') {
                        final int start = pos++;
                        boolean end = false;
                        while (pos < chars.length) {
                            char c2 = chars[pos];
                            if (c2 == ']') {
                                end = true;
                                break;
                            } else {
                                if (parser != null) {
                                    sb.append(c2);
                                }
                                pos++;
                            }
                        }
                        if (!end) {
                            //throw new TextParserException("Unclosed argument brackets []", start);
                            nodes.add(new ErrorNode(start, pos - start, "Unclosed argument brackets []"));
                            continue;
                        }
                        //nodes.add(new ModifierNode(parser.parse(sb.toString(), start)));
                        if (parser == null) {
                            nodes.add(new ErrorNode(modStartPos, pos - modStartPos, "Unknown modifier key '" + c + "'"));
                        } else {
                            parser.parse(modStartPos, start + 1, sb.toString(), nodes);
                            sb.setLength(0);
                        }
                    } else {
                        if (parser == null) {
                            nodes.add(new ErrorNode(modStartPos, 2, "Unknown modifier key '" + c + "'"));
                        } else {
                            parser.parse(modStartPos, -1, null, nodes);
                        }
                    }
                    textStart = pos + 1;
                }
            } else {
                if (c == '\\')
                    backslash = true;
                else if (c == '\n') {
                    flushTextNode();
                    nodes.add(new LineBreakNode(pos));
                    textStart = pos + 1;
                } else {
                    sb.append(c);
                    textLength++;
                }
            }
        }

        flushTextNode();
        if (backslash) {
            //throw new TextParserException("Backslash at end of text", chars.length - 1);
            nodes.add(new ErrorNode(chars.length - 1, 1, "Backslash at end of text"));
        }
        nodes.optimize();
        return nodes;
    }

    private void flushTextNode() {
        if (textLength > 0) {
            nodes.add(new TextNode(textStart, textLength, sb.toString()));
            sb.setLength(0);
            textLength = 0;
        }
    }
}
