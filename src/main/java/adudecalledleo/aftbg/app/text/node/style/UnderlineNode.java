package adudecalledleo.aftbg.app.text.node.style;

import java.util.List;
import java.util.Map;

import javax.swing.text.*;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.node.*;
import adudecalledleo.aftbg.app.text.util.FontStyle;

public final class UnderlineNode extends ContainerNode implements FontStyleModifyingNode {
    public static final String NAME = "u";
    public static final NodeHandler<UnderlineNode> HANDLER = new Handler();

    public UnderlineNode(Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, List<Node> children) {
        super(NAME, openingSpan, closingSpan, attributes, children);
    }

    @Override
    public FontStyle updateStyle(FontStyle style) {
        return style.withUnderline(true);
    }

    @Override
    public void updateSwingStyle(MutableAttributeSet style) {
        StyleConstants.setUnderline(style, true);
    }

    private static final class Handler extends NodeHandler<UnderlineNode> {
        @Override
        public UnderlineNode parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                                   Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
            return new UnderlineNode(openingSpan, closingSpan, attributes, ctx.parse(contents, offset, errors));
        }
    }
}
