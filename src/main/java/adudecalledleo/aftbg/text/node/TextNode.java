package adudecalledleo.aftbg.text.node;

public final class TextNode extends Node {
    private final String contents;

    public TextNode(int start, int length, String contents) {
        super(start, length);
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public String toString() {
        return "TextNode{" +
                "start=" + start +
                ", length=" + length +
                ", contents='" + contents + '\'' +
                '}';
    }
}
