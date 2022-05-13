package adudecalledleo.aftbg.app.text.node;

import java.util.List;
import java.util.Map;

import adudecalledleo.aftbg.app.text.DOMParser;

public final class ImplicitNodeHandler<T extends Node> implements NodeHandler<T> {
    private final String name;

    public ImplicitNodeHandler(String name) {
        this.name = name;
    }

    @Override
    public final T parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                         Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
        throw new UnsupportedOperationException("Explicit [%s] declaration not allowed!".formatted(name));
    }
}
