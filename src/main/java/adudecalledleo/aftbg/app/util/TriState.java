package adudecalledleo.aftbg.app.util;

public enum TriState {
    DEFAULT, TRUE, FALSE;

    public static TriState fromBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }

    public TriState orElse(TriState other) {
        if (other == DEFAULT) {
            return this;
        }
        return other;
    }

    public boolean toBoolean(boolean defaultValue) {
        return switch (this) {
            case DEFAULT -> defaultValue;
            case TRUE -> true;
            case FALSE -> false;
        };
    }
}
