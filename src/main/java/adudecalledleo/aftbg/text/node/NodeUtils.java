package adudecalledleo.aftbg.text.node;

public final class NodeUtils {
    private NodeUtils() { }

    public static String getTruncatedDisplay(NodeList nodes, int maxLength) {
        if (nodes.hasErrors()) {
            return "(has errors)";
        }
        if (nodes.isEmpty()) {
            return "(empty)";
        }
        int len = 0;
        StringBuilder sb = new StringBuilder();
        for (var node : nodes) {
            if (node instanceof TextNode textNode) {
                sb.append(textNode.getContents());
                len += textNode.getLength();
            } else if (node instanceof LineBreakNode) {
                sb.append(' ');
                len++;
            }
            if (len >= maxLength) {
                sb.setLength(maxLength - 3);
                sb.append("...");
                break;
            }
        }
        return '"' + sb.toString() + '"';
    }
}
