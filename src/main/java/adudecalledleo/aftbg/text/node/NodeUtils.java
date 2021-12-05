package adudecalledleo.aftbg.text.node;

public final class NodeUtils {
    private NodeUtils() { }

    public static String getTruncatedDisplay(NodeList nodes, int maxLength) {
        if (nodes.hasErrors()) {
            return "(has errors)";
        } else if (nodes.isEmpty()) {
            return "(empty)";
        }
        int len = 0;
        StringBuilder sb = new StringBuilder();
        for (var node : nodes) {
            if (node instanceof TextNode textNode) {
                String contents = textNode.getContents();
                sb.append(contents);
                len += contents.length();
            } else if (node instanceof LineBreakNode) {
                sb.append(' ');
                len++;
            }
            if (len >= maxLength) {
                sb.setLength(maxLength - 1);
                sb.append('\u2026' /* "..." but one character */);
                break;
            }
        }
        if (len == 0) {
            return "(empty)";
        } else {
            return '"' + sb.toString() + '"';
        }
    }
}
