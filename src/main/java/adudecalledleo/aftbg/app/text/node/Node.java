package adudecalledleo.aftbg.app.text.node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Node {
    protected final String name;
    protected final Span openingSpan, closingSpan, contentSpan;
    protected final Map<String, Attribute> attributes;

    public Node(String name, Span openingSpan, Span closingSpan, Map<String, Attribute> attributes) {
        this.name = name;
        this.openingSpan = openingSpan;
        this.closingSpan = closingSpan;
        this.contentSpan = new Span(openingSpan.end(), closingSpan.start() - openingSpan.end());
        this.attributes = attributes;
    }

    public Node(String name, Span openingSpan, Span closingSpan) {
        this(name, openingSpan, closingSpan, new LinkedHashMap<>());
    }

    public String getName() {
        return name;
    }

    public Span getOpeningSpan() {
        return openingSpan;
    }

    public Span getClosingSpan() {
        return closingSpan;
    }

    public Span getContentSpan() {
        return contentSpan;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    public <T, R> Optional<R> visitSelf(NodeVisitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }

    public <T, R> Optional<R> visit(NodeVisitor<T, R> visitor, T data) {
        return this.visitSelf(visitor, data);
    }
}
