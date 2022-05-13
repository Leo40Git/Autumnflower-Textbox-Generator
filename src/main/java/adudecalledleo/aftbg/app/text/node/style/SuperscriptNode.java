package adudecalledleo.aftbg.app.text.node.style;

import java.util.List;
import java.util.Map;

import javax.swing.text.*;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.node.*;
import adudecalledleo.aftbg.app.text.util.FontStyle;

public final class SuperscriptNode extends ContainerNode implements FontStyleModifyingNode {
    public static final String NAME = "sup";
    public static final NodeHandler<SuperscriptNode> HANDLER = new Handler();

    public SuperscriptNode(Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, List<Node> children) {
        super(NAME, openingSpan, closingSpan, attributes, children);
    }

    @Override
    public FontStyle updateStyle(FontStyle style) {
        return style.withSuperscript(FontStyle.Superscript.SUPER);
    }

    @Override
    public void updateSwingStyle(MutableAttributeSet style) {
        StyleConstants.setSubscript(style, false);
        StyleConstants.setSuperscript(style, true);
    }

    private static final class Handler implements NodeHandler<SuperscriptNode> {
        @Override
        public SuperscriptNode parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                                     Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
            return new SuperscriptNode(openingSpan, closingSpan, attributes, ctx.parse(contents, offset, errors));
        }
    }
}
