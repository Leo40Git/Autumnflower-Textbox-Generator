package adudecalledleo.aftbg.text.node;

public sealed class TextNode extends Node {
    public static final class Mutable extends TextNode {
        private String contents;

        public Mutable(String contents) {
            super(0, 0, null);
            this.contents = contents;
        }

        @Override
        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        @Override
        public String toString() {
            return "TextNode.Mutable{" +
                    "contents='" + contents + '\'' +
                    '}';
        }
    }

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
