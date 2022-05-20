package adudecalledleo.aftbg.json.element;

import java.util.Objects;

public final class JsonString extends JsonElement {
    private final String value;

    public JsonString(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonString that = (JsonString) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return '"' + value + '"';
    }
}
