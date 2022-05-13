package adudecalledleo.aftbg.app.text.node.style;

import java.util.List;
import java.util.Map;

import javax.swing.text.*;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.node.*;
import adudecalledleo.aftbg.app.text.util.FontStyle;

public final class ItalicNode extends ContainerNode implements FontStyleModifyingNode {
    public static final String NAME = "i";
    public static final NodeHandler<ItalicNode> HANDLER = new Handler();

    public ItalicNode(Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, List<Node> children) {
        super(NAME, openingSpan, closingSpan, attributes, children);
    }

    @Override
    public FontStyle updateStyle(FontStyle style) {
        return style.withItalic(true);
    }

    @Override
    public void updateSwingStyle(MutableAttributeSet style) {
        StyleConstants.setItalic(style, true);
    }

    private static final class Handler implements NodeHandler<ItalicNode> {
        @Override
        public ItalicNode parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                                Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
            return new ItalicNode(openingSpan, closingSpan, attributes, ctx.parse(contents, offset, errors));
        }
    }
}
