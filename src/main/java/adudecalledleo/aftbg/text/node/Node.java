package adudecalledleo.aftbg.text.node;

public sealed abstract class Node permits ErrorNode, LineBreakNode, ModifierNode, TextNode {
    protected final int start, length;

    public Node(int start, int length) {
        this.start = start;
        this.length = length;
    }

    public final int getStart() {
        return start;
    }

    public final int getLength() {
        return length;
    }

    public final Span asSpan() {
        return new Span(start, length);
    }

    @Override
    public String toString() {
        return "Node{" +
                "start=" + start +
                ", length=" + length +
                '}';
    }
}
