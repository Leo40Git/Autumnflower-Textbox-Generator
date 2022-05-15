package adudecalledleo.aftbg.app.text.node;

import java.util.Map;

public final class TextNode extends Node {
    public static final String NAME = "text";

    private final String contents;

    public TextNode(String contents) {
        super(NAME, Span.INVALID, Span.INVALID, Map.of());
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }
}
