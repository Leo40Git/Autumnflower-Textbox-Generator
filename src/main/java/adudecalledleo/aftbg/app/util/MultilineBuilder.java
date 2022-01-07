package adudecalledleo.aftbg.app.util;

public final class MultilineBuilder {
    private final StringBuilder sb;
    private boolean addBr;

    public MultilineBuilder() {
        sb = new StringBuilder();
        addBr = false;
    }

    public MultilineBuilder line(String line) {
        if (addBr) {
            sb.append("<br/>");
        }
        sb.append(line);
        addBr = true;
        return this;
    }

    public MultilineBuilder lines(String... lines) {
        for (String line : lines) {
            line(line);
        }
        return this;
    }

    @Override
    public String toString() {
        return "<html>" + sb + "</html>";
    }
}
