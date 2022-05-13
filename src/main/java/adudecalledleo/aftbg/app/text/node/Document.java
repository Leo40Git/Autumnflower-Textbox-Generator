package adudecalledleo.aftbg.app.text.node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class Document extends ContainerNode {
    public static final String NAME = "doc";
    public static final NodeHandler<Document> HANDLER = new ImplicitNodeHandler<>(NAME);

    public Document(List<Node> children) {
        super(NAME, Span.INVALID, Span.INVALID, Map.of(), children);
    }

    public Document() {
        this(new LinkedList<>());
    }
}
