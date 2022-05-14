package adudecalledleo.aftbg.app.text.node.gimmick;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.node.*;
import org.jetbrains.annotations.Nullable;

public final class GimmickNode extends ContainerNode {
    public static final String NAME = "gimmick";
    public static final NodeHandler<GimmickNode> HANDLER = new Handler();

    private final @Nullable TextFill fill;
    private final @Nullable TextFlip flip;

    public GimmickNode(@Nullable TextFill fill, @Nullable TextFlip flip,
                       Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, List<Node> children) {
        super(NAME, openingSpan, closingSpan, attributes, children);
        this.fill = fill;
        this.flip = flip;
    }

    public @Nullable TextFill getFill() {
        return fill;
    }

    public @Nullable TextFlip getFlip() {
        return flip;
    }

    private static final class Handler implements NodeHandler<GimmickNode> {
        @Override
        public @Nullable GimmickNode parse(NodeParsingContext ctx, int offset, List<DOMParser.Error> errors,
                                           Span openingSpan, Span closingSpan, Map<String, Attribute> attributes, String contents) {
            @Nullable TextFill fill = null;
            @Nullable TextFlip flip = null;

            var fillAttr = attributes.get("fill");
            if (fillAttr != null) {
                if (isAttributeBlank(fillAttr, errors)) {
                    return null;
                }
                String fillStr = fillAttr.value().trim();
                fill = TextFill.getByName(fillStr.toLowerCase(Locale.ROOT));
                if (fill == null) {
                    errors.add(new DOMParser.Error(fillAttr.valueSpan().start(), fillAttr.valueSpan().length(),
                            "unknown fill type \"%s\"".formatted(fillStr)));
                    return null;
                }
            }

            var flipAttr = attributes.get("flip");
            if (flipAttr != null) {
                if (isAttributeBlank(flipAttr, errors)) {
                    return null;
                }
                String flipStr = flipAttr.value().trim();
                flip = TextFlip.getByName(flipStr.toLowerCase(Locale.ROOT));
                if (flip == null) {
                    errors.add(new DOMParser.Error(flipAttr.valueSpan().start(), flipAttr.valueSpan().length(),
                            "unknown flip type \"%s\"".formatted(flipStr)));
                    return null;
                }
            }

            return new GimmickNode(fill, flip, openingSpan, closingSpan, attributes, ctx.parse(contents, offset, errors));
        }
    }
}
