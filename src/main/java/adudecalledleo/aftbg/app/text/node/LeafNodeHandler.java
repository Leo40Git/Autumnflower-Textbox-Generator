package adudecalledleo.aftbg.app.text.node;

import java.util.List;
import java.util.Map;

import adudecalledleo.aftbg.app.text.DOMParser;
import org.jetbrains.annotations.Nullable;

public abstract class LeafNodeHandler<T extends Node> extends NodeHandler<T> {
    @Override
    public final boolean isLeaf() {
        return true;
    }

    @Override
    public @Nullable T parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                             Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
        return create(errors, openingSpan, attributes);
    }

    public abstract @Nullable T create(List<DOMParser.Error> errors, Span span, Map<String, Attribute> attributes);
}
