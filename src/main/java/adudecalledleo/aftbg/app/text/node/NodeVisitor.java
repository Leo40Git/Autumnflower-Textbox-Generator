package adudecalledleo.aftbg.app.text.node;

import java.util.Optional;

@FunctionalInterface
public interface NodeVisitor<T, R> {
    Optional<R> visit(Node node, T data);
}
