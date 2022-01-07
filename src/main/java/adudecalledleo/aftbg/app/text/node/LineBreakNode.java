package adudecalledleo.aftbg.app.text.node;

public final class LineBreakNode extends Node {
    public LineBreakNode(int start) {
        super(start, 1);
    }

    @Override
    public String toString() {
        return "LineBreakNode{start=" + start + "}";
    }
}
