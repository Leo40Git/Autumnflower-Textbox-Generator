package adudecalledleo.aftbg.app.text.node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public <T, R> Optional<R> visit(NodeVisitor<T, R> visitor, T data) {
        var result = super.visit(visitor, data);
        if (result.isPresent()) {
            return result;
        }
        for (var child : this.children) {
            result = child.visit(visitor, data);
            if (result.isPresent()) {
                break;
            }
        }
        return result;
    }
}
