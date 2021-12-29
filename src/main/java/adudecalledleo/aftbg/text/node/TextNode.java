package adudecalledleo.aftbg.text.node;

public sealed class TextNode extends Node {
    public static final class Escaped extends TextNode {
        private final String originalContents;

        public Escaped(int start, int length, String contents, String originalContents) {
            super(start, length, contents);
            this.originalContents = originalContents;
        }

        public String getOriginalContents() {
            return originalContents;
        }

        @Override
        public String toString() {
            return "TextNode.Escaped{" +
                    "start=" + start +
                    ", length=" + length +
                    ", contents='" + contents + '\'' +
                    ", originalContents='" + originalContents + '\'' +
                    '}';
        }
    }

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

    protected final String contents;

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
