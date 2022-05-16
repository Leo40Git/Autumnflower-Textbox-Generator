package adudecalledleo.aftbg.app.text.node;

public record Span(int start, int length) {
    public static final Span INVALID = new Span(-1, -1);

    public boolean isValid() {
        return start >= 0 && length > 0;
    }

    public int end() {
        return start + length;
    }

    public boolean isIn(Span outer) {
        return this.start >= outer.start() && this.end() <= outer.end();
    }
}
