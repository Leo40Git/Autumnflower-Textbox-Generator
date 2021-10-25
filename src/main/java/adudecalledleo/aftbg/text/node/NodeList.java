package adudecalledleo.aftbg.text.node;

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
    
    public void optimize() {
        for (int i = 0; i < wrapped.size() - 1; i++) {
            Node node = wrapped.get(i);
            if (node instanceof TextNode t1) {
                Node nextNode = wrapped.get(i + 1);
                if (nextNode instanceof TextNode t2) {
                    wrapped.set(i, new TextNode(t1.getStart(), t1.getContents() + t2.getContents()));
                    wrapped.remove(i + 1);
                } else if (t1.getContents().isEmpty()) {
                    wrapped.remove(i);
                    i--;
                }
            }
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ErrorNode> getErrors() {
        return errors;
    }

    @Override
    public Iterator<Node> iterator() {
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
