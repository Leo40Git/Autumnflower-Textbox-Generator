package adudecalledleo.aftbg.util;

public enum TriState {
    DEFAULT, TRUE, FALSE;

    public TriState and(TriState other) {
        if (this == DEFAULT) {
            return other;
        } else if (other == DEFAULT) {
            return this;
        } else if (this == TRUE && other == TRUE) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public boolean toBoolean(boolean defaultValue) {
        return switch (this) {
            case DEFAULT -> defaultValue;
            case TRUE -> true;
            case FALSE -> false;
        };
    }
}
