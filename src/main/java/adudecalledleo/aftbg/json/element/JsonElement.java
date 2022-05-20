package adudecalledleo.aftbg.json.element;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public abstract sealed class JsonElement permits JsonArray, JsonBoolean, JsonNull, JsonNumber, JsonObject, JsonString {
    public enum Type {
        NULL, BOOLEAN, NUMBER, STRING, ARRAY, OBJECT;
    }

    public abstract Type getType();

    public boolean isNull() {
        return getType() == Type.NULL;
    }

    public boolean isBoolean() {
        return getType() == Type.BOOLEAN;
    }

    public boolean isNumber() {
        return getType() == Type.NUMBER;
    }

    public boolean isString() {
        return getType() == Type.STRING;
    }

    public boolean isArray() {
        return getType() == Type.ARRAY;
    }

    public boolean isObject() {
        return getType() == Type.OBJECT;
    }

    public static JsonElement read(JsonReader reader) throws IOException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY -> {
                reader.beginArray();
                ArrayList<JsonElement> values = new ArrayList<>();
                while (reader.hasNext()) {
                    values.add(read(reader));
                }
                reader.endArray();
                yield new JsonArray(values, true);
            }
            case BEGIN_OBJECT -> {
                reader.beginObject();
                LinkedHashMap<String, JsonElement> values = new LinkedHashMap<>();
                while (reader.hasNext()) {
                    values.put(reader.nextName(), read(reader));
                }
                reader.endObject();
                yield new JsonObject(values, true);
            }
            case STRING -> new JsonString(reader.nextString());
            case NUMBER -> JsonNumber.valueOf(reader.nextNumber());
            case BOOLEAN -> JsonBoolean.valueOf(reader.nextBoolean());
            case NULL -> {
                reader.nextNull();
                yield JsonNull.get();
            }
            case END_DOCUMENT -> throw new EOFException("End of input" + reader.locationString());
            default -> throw new IOException("Unexpected token " + reader.peek() + reader.locationString());
        };
    }

    public static void write(JsonWriter writer, JsonElement elem) throws IOException {
        switch (elem) {
            case JsonNull ignored -> writer.nullValue();
            case JsonArray arr -> {
                writer.beginArray();
                for (var value : arr) {
                    write(writer, value);
                }
                writer.endArray();
            }
            case JsonObject obj -> {
                writer.beginObject();
                for (var entry : obj.getValues().entrySet()) {
                    writer.name(entry.getKey());
                    write(writer, entry.getValue());
                }
                writer.endObject();
            }
            case JsonNumber num -> writer.value(num.getValue());
            case JsonString str -> writer.value(str.getValue());
            case JsonBoolean bool -> writer.value(bool.getValue());
        }
    }
}
