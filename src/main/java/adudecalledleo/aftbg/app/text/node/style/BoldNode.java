package adudecalledleo.aftbg.app.text.node.style;

import java.util.List;
import java.util.Map;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.node.*;

public final class BoldNode extends ContainerNode {
    public static final String NAME = "b";
    public static final NodeHandler<BoldNode> HANDLER = new Handler();

    public BoldNode(Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, List<Node> children) {
        super(NAME, openingSpan, closingSpan, attributes, children);
    }

    private static final class Handler implements NodeHandler<BoldNode> {
        @Override
        public BoldNode parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                              Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
            return new BoldNode(openingSpan, closingSpan, attributes, ctx.parse(contents, offset, errors));
        }

    }
}
