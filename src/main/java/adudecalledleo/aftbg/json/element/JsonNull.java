package adudecalledleo.aftbg.json.element;

public final class JsonNull extends JsonElement {
    private static final JsonNull INSTANCE = new JsonNull();

    public static JsonNull get() {
        return JsonNull.INSTANCE;
    }

    private JsonNull() { }

    @Override
    public Type getType() {
        return Type.NULL;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
