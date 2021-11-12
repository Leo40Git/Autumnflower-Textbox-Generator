package adudecalledleo.aftbg.util;

import org.jetbrains.annotations.Nullable;

public enum TriState {
    DEFAULT, TRUE, FALSE;

    public static TriState fromBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static TriState fromBoolean(@Nullable Boolean b) {
        if (b == null) {
            return DEFAULT;
        } else if (b == Boolean.TRUE) {
            return TRUE;
        } else if (b == Boolean.FALSE) {
            return FALSE;
        }
        throw new IllegalArgumentException("Boolean isn't null, TRUE or FALSE?! " + b);
    }

    public TriState orElse(TriState other) {
        if (other == DEFAULT) {
            return this;
        }
        return other;
    }

    public @Nullable Boolean toBoolean() {
        return switch (this) {
            case DEFAULT -> null;
            case TRUE -> Boolean.TRUE;
            case FALSE -> Boolean.FALSE;
        };
    }

    public boolean toBoolean(boolean defaultValue) {
        return switch (this) {
            case DEFAULT -> defaultValue;
            case TRUE -> true;
            case FALSE -> false;
        };
    }
}
