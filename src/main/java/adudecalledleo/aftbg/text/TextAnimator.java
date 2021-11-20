package adudecalledleo.aftbg.text;

import adudecalledleo.aftbg.text.node.Node;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.TextNode;

import java.util.List;

public final class TextAnimator {
    private final NodeList workingNodes;
    private NodeList sourceNodes;
    private int currentNodeIdx;
    private TextNode currentTextNode;
    private int currentTextNodeChar;

    public TextAnimator(NodeList sourceNodes) {
        workingNodes = new NodeList();
        reset(sourceNodes);
    }

    public void reset(NodeList sourceNodes) {
        this.sourceNodes = sourceNodes;
        currentNodeIdx = -1;
        currentTextNode = null;
        currentTextNodeChar = -1;
    }

    /**
     * @return {@code true} if finished animating this node list, {@code false} otherwise
     */
    public boolean nextChar() {
        List<Node> srcNodes = sourceNodes.asList();
        while (currentTextNode == null) {
            if (currentNodeIdx >= srcNodes.size() - 1) {
                break;
            }
            Node node = srcNodes.get(++currentNodeIdx);
            if (node instanceof TextNode textNode) {
                currentTextNode = textNode;
                currentTextNodeChar = -1;
                break;
            } else {
                workingNodes.add(node);
            }
        }
        if (currentTextNode == null) {
            return true;
        }
        List<Node> nodes = workingNodes.asList();
        int lastNode = nodes.size() - 1;
        if (lastNode >= 0) {
            if (nodes.get(lastNode) instanceof TextNode) {
                nodes.remove(lastNode);
            }
        }
        currentTextNodeChar++;
        if (currentTextNode.getContents().length() == currentTextNodeChar) {
            nodes.add(currentTextNode);
            currentTextNode = null;
        } else {
            nodes.add(new TextNode(0, 0, currentTextNode.getContents().substring(0, currentTextNodeChar)));
        }
        return false;
    }

    public NodeList getNodes() {
        return workingNodes;
    }
}
