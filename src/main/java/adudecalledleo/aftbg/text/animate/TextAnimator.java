package adudecalledleo.aftbg.text.animate;

import adudecalledleo.aftbg.text.node.Node;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.TextNode;

import java.util.List;

public final class TextAnimator {
    private final NodeList workingNodes;
    private final TextNode.Mutable workingTextNode;

    private NodeList sourceNodes;
    private int currentNodeIdx;
    private TextNode currentTextNode;
    private String currentTextNodeContents;
    private int currentTextNodeChar;
    private boolean firstFrame;

    public TextAnimator(NodeList sourceNodes) {
        workingNodes = new NodeList();
        workingTextNode = new TextNode.Mutable("");
        reset(sourceNodes);
    }

    public void reset(NodeList sourceNodes) {
        this.sourceNodes = sourceNodes;
        currentNodeIdx = -1;
        currentTextNode = null;
        currentTextNodeContents = "";
        currentTextNodeChar = 0;
        firstFrame = true;
    }

    /**
     * Evaluates the next command.<br>
     * See {@code AnimationCommand} and its subtypes for more information.
     *
     * @return an {@code AnimationCommand}
     */
    public AnimationCommand nextCommand() {
        if (firstFrame) {
            firstFrame = false;
            return AnimationCommand.drawFrame();
        }

        List<Node> srcNodes = sourceNodes.asList();
        while (currentTextNode == null) {
            if (currentNodeIdx >= srcNodes.size() - 1) {
                break;
            }
            Node node = srcNodes.get(++currentNodeIdx);
            if (node instanceof TextNode textNode) {
                currentTextNode = textNode;
                currentTextNodeContents = textNode.getContents();
                currentTextNodeChar = 0;
                break;
            } else if (node instanceof AnimationCommandNode acNode) {
                workingNodes.add(node);
                return acNode.getAnimationCommand();
            } else {
                workingNodes.add(node);
            }
        }
        if (currentTextNode == null) {
            return AnimationCommand.endOfTextbox();
        }

        List<Node> nodes = workingNodes.asList();
        int lastNode = nodes.size() - 1;
        if (lastNode >= 0) {
            if (nodes.get(lastNode) instanceof TextNode) {
                nodes.remove(lastNode);
            }
        }

        currentTextNodeChar++;
        if (currentTextNodeContents.length() == currentTextNodeChar) {
            nodes.add(currentTextNode);
            currentTextNode = null;
        } else {
            workingTextNode.setContents(currentTextNodeContents.substring(0, currentTextNodeChar));
            nodes.add(workingTextNode);
        }

        return AnimationCommand.drawFrame();
    }

    public NodeList getNodes() {
        return workingNodes;
    }
}