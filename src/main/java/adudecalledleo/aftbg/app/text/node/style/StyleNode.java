package adudecalledleo.aftbg.app.text.node.style;

import java.awt.*;
import java.util.List;
import java.util.Map;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.node.*;
import adudecalledleo.aftbg.app.text.node.color.ColorParser;
import org.jetbrains.annotations.Nullable;

public final class StyleNode extends ContainerNode {
    public static final String NAME = "style";
    public static final NodeHandler<StyleNode> HANDLER = new Handler();

    public static void register(NodeRegistry registry) {
        registry.register(NAME, HANDLER);
    }

    private final @Nullable Integer size;
    private final boolean colorSet;
    private final @Nullable Color color;

    public StyleNode(@Nullable Integer size,
                     boolean colorSet, @Nullable Color color,
                     Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, List<Node> children) {
        super(NAME, openingSpan, closingSpan, attributes, children);
        this.size = size;
        this.colorSet = colorSet;
        this.color = color;
    }

    public @Nullable Integer getSize() {
        return size;
    }

    public boolean isColorSet() {
        return colorSet;
    }

    public @Nullable Color getColor() {
        return color;
    }

    private static final class Handler implements NodeHandler<StyleNode> {
        @Override
        public StyleNode parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                               Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
            @Nullable Integer size = null;
            boolean colorSet = false;
            @Nullable Color color = null;

            var sizeAttr = attributes.get("size");
            if (sizeAttr != null) {
                if (isAttributeBlank(sizeAttr, errors)) {
                    return null;
                }
                String sizeStr = sizeAttr.value().trim();
                char firstChar = sizeStr.charAt(0);
                if (firstChar != '-' && firstChar != '+') {
                    errors.add(new DOMParser.Error(sizeAttr.valueSpan().start(), sizeAttr.valueSpan().length(),
                            "size must start with + or -"));
                    return null;
                }
                try {
                    size = Integer.parseInt(sizeStr);
                } catch (NumberFormatException e) {
                    errors.add(new DOMParser.Error(sizeAttr.valueSpan().start(), sizeAttr.valueSpan().length(),
                            "size must be a valid integer"));
                    return null;
                }
            }

            var colorAttr = attributes.get("color");
            if (colorAttr != null) {
                if (isAttributeBlank(colorAttr, errors)) {
                    return null;
                }
                try {
                    color = ColorParser.parseColor(ctx.metadata(), colorAttr.value().trim());
                } catch (IllegalArgumentException e) {
                    errors.add(new DOMParser.Error(colorAttr.valueSpan().start(), colorAttr.valueSpan().length(), e.getMessage()));
                    return null;
                }
                colorSet = true;
            }

            return new StyleNode(size, colorSet, color, openingSpan, closingSpan, attributes, ctx.parse(contents, offset, errors));
        }

    }
}
