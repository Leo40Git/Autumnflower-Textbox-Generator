package adudecalledleo.aftbg.text.node;

public final class ErrorNode extends Node {
    private final String message;

    public ErrorNode(int start, int length, String message) {
        super(start, length);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message + " at " + (start + 1) + ", to " + (start + 1 + length);
    }
}
