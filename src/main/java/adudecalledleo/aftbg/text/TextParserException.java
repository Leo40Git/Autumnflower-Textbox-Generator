package adudecalledleo.aftbg.text;

public class TextParserException extends Exception {
    private final int pos;

    public TextParserException(String message, int pos) {
        super(message);
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + " at position " + pos;
    }
}
