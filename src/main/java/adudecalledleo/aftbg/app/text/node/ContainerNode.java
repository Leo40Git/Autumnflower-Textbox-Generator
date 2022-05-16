package adudecalledleo.aftbg.app.text.node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class ContainerNode extends Node {
    protected final List<Node> children;

    public ContainerNode(String name, Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, List<Node> children) {
        super(name, openingSpan, closingSpan, attributes);
        this.children = new LinkedList<>(children);
    }

    public ContainerNode(String name, Span openingSpan, Span closingSpan, Map<String, Attribute> attributes) {
        super(name, openingSpan, closingSpan, attributes);
        this.children = new LinkedList<>();
    }

    public List<Node> getChildren() {
        return children;
    }
}
