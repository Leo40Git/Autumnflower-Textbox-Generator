package adudecalledleo.aftbg.app.text.node.style;

import java.util.List;
import java.util.Map;

import javax.swing.text.*;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.node.*;
import adudecalledleo.aftbg.app.text.util.FontStyle;

public final class SubscriptNode extends ContainerNode implements FontStyleModifyingNode {
    public static final String NAME = "sub";
    public static final NodeHandler<SubscriptNode> HANDLER = new Handler();

    public SubscriptNode(Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, List<Node> children) {
        super(NAME, openingSpan, closingSpan, attributes, children);
    }

    @Override
    public FontStyle updateStyle(FontStyle style) {
        return style.withSuperscript(FontStyle.Superscript.SUB);
    }

    @Override
    public void updateSwingStyle(MutableAttributeSet style) {
        StyleConstants.setSuperscript(style, false);
        StyleConstants.setSubscript(style, true);
    }

    private static final class Handler implements NodeHandler<SubscriptNode> {
        @Override
        public SubscriptNode parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                                   Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
            return new SubscriptNode(openingSpan, closingSpan, attributes, ctx.parse(contents, offset, errors));
        }
    }
}
