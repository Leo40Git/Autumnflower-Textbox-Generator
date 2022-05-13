package adudecalledleo.aftbg.app.text.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

public final class NodeList implements Iterable<Node> {
    private final List<Node> wrapped;
    private final List<ErrorNode> errors;

    public NodeList() {
        wrapped = new ArrayList<>();
        errors = new ArrayList<>();
    }

    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    public void add(Node node) {
        if (node instanceof ErrorNode err)
            errors.add(err);
        wrapped.add(node);
    }

    public void clear() {
        wrapped.clear();
        errors.clear();
    }

    /**
     * Concatenates consecutive {@link TextNode}s, and removes empty ones.
     */
    public void optimizeTextNodes() {
        optimizeTextNodes0(TextNode.class, (t1, t2) ->
                new TextNode(t1.getStart(), t1.getLength() + t2.getLength(),
                        t1.getContents() + t2.getContents()));
        optimizeTextNodes0(TextNode.Escaped.class, (t1, t2) ->
                new TextNode.Escaped(t1.getStart(), t1.getLength() + t2.getLength(),
                        t1.getContents() + t2.getContents(),
                        t1.getOriginalContents() + t2.getOriginalContents()));
    }

    private <N extends TextNode> void optimizeTextNodes0(Class<N> clazz, BinaryOperator<N> concatFunc) {
        for (int i = 0; i < wrapped.size() - 1; i++) {
            Node node = wrapped.get(i);
            if (node.getClass() == clazz) {
                N t1 = clazz.cast(node);
                Node nextNode = wrapped.get(i + 1);
                if (nextNode.getClass() == clazz) {
                    N t2 = clazz.cast(nextNode);
                    wrapped.set(i, concatFunc.apply(t1, t2));
                    wrapped.remove(i + 1);
                    i--;
                } else if (t1.getContents().isEmpty()) {
                    wrapped.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Checks for errors which cannot be detected during parsing, and can only be detected reliably after everything
     * has been parsed.
     */
    public void checkForAdditionalErrors() {
        // use this instead of wrapped.size() so we don't pointlessly iterate over error nodes we add
        final int limit = wrapped.size() - 1;
    }

    public List<Node> asList() {
        return wrapped;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ErrorNode> getErrors() {
        return errors;
    }

    @Override
    public @NotNull Iterator<Node> iterator() {
        return wrapped.iterator();
    }

    @Override
    public void forEach(Consumer<? super Node> action) {
        wrapped.forEach(action);
    }

    @Override
    public Spliterator<Node> spliterator() {
        return wrapped.spliterator();
    }
}
