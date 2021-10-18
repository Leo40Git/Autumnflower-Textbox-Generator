package adudecalledleo.aftbg.text.node;

public final class LineBreakNode implements Node {
    public static final LineBreakNode INSTANCE = new LineBreakNode();

    private LineBreakNode() { }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
