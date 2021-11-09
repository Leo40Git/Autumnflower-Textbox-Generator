package adudecalledleo.aftbg.logging;

public enum Level {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL;

    private String paddedString;

    public boolean isLessSignificantThan(Level other) {
        return ordinal() <= other.ordinal();
    }

    public boolean isMoreSignificantThan(Level other) {
        return ordinal() >= other.ordinal();
    }

    public boolean isInRange(Level min, Level max) {
        return isLessSignificantThan(max) && isMoreSignificantThan(min);
    }

    @Override
    public String toString() {
        if (paddedString == null) {
            paddedString = name();
            int len = paddedString.length();
            if (len < 5) {
                paddedString += " ".repeat(5 - len);
            }
        }
        return paddedString;
    }
}
