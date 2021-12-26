package adudecalledleo.aftbg.text.node;

import adudecalledleo.aftbg.text.modifier.InterruptModifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

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
        for (int i = 0; i < wrapped.size() - 1; i++) {
            Node node = wrapped.get(i);
            if (node instanceof TextNode t1) {
                Node nextNode = wrapped.get(i + 1);
                if (nextNode instanceof TextNode t2) {
                    wrapped.set(i, new TextNode(t1.getStart(), t1.getLength() + t2.getLength(),
                            t1.getContents() + t2.getContents()));
                    wrapped.remove(i + 1);
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

        // region Interrupt: check if at end of textbox
        InterruptModifierNode lastInterruptNode = null;
        for (int i = 0; i < limit; i++) {
            Node node = wrapped.get(i);
            if (node instanceof InterruptModifierNode interruptNode) {
                if (lastInterruptNode != null) {
                    add(new ErrorNode(lastInterruptNode.getStart(), lastInterruptNode.getLength(),
                            InterruptModifierNode.Parser.ERROR_PREFIX + "Must be at end of textbox!"));
                }
                lastInterruptNode = interruptNode;
            }
        }

        if (lastInterruptNode != null && wrapped.get(limit) != lastInterruptNode) {
            add(new ErrorNode(lastInterruptNode.getStart(), lastInterruptNode.getLength(),
                    InterruptModifierNode.Parser.ERROR_PREFIX + "Must be at end of textbox!"));
        }
        // endregion
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
