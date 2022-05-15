package adudecalledleo.aftbg.app.text.node;

import java.util.List;
import java.util.Map;

import adudecalledleo.aftbg.app.text.DOMParser;
import org.jetbrains.annotations.NotNull;

public final class LineBreakNode extends Node {
    public static final String NAME = "br";
    public static final NodeHandler<LineBreakNode> HANDLER = new Handler();

    public LineBreakNode(Span span) {
        super(NAME, span, Span.INVALID);
    }

    private static final class Handler extends LeafNodeHandler<LineBreakNode> {
        @Override
        public @NotNull LineBreakNode create(List<DOMParser.Error> errors, Span span, Map<String, Attribute> attributes) {
            return new LineBreakNode(span);
        }
    }
}
