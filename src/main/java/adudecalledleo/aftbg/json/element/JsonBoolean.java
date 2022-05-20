package adudecalledleo.aftbg.json.element;

public final class JsonBoolean extends JsonElement {
    public static final JsonBoolean TRUE = new JsonBoolean(true);
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    public static JsonBoolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    private final boolean value;

    private JsonBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonBoolean that = (JsonBoolean) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }
}
