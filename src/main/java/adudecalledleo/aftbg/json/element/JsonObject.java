package adudecalledleo.aftbg.json.element;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonObject extends JsonElement {
    private final Map<String, JsonElement> values;

    JsonObject(Map<String, JsonElement> values, boolean dummy) {
        this.values = values;
    }

    public JsonObject(Map<String, JsonElement> values) {
        this.values = new LinkedHashMap<>(values);
    }

    public JsonObject() {
        this.values = new LinkedHashMap<>();
    }

    @Override
    public Type getType() {
        return Type.OBJECT;
    }

    public Map<String, JsonElement> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonObject that = (JsonObject) o;

        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
