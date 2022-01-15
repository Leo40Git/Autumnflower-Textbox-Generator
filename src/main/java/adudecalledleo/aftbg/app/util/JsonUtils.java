package adudecalledleo.aftbg.app.util;

import java.net.URISyntaxException;
import java.nio.file.Path;

import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

public final class JsonUtils {
    private JsonUtils() { }

    public enum ElementType {
        NULL("null"), OBJECT("object"), ARRAY("array"),
        BOOLEAN("boolean"), NUMBER("number"), STRING("string");

        private final String friendlyName;

        ElementType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        @Override
        public String toString() {
            return friendlyName;
        }

        public static ElementType of(@Nullable JsonElement elem) {
            if (elem == null || elem.isJsonNull()) {
                return NULL;
            } else if (elem.isJsonObject()) {
                return OBJECT;
            } else if (elem.isJsonArray()) {
                return ARRAY;
            } else if (elem instanceof JsonPrimitive prim) {
                if (prim.isBoolean()) {
                    return BOOLEAN;
                } else if (prim.isNumber()) {
                    return NUMBER;
                } else if (prim.isString()) {
                    return STRING;
                }
            }
            throw new IllegalArgumentException("Unknown element " + elem);
        }
    }

    public static class StructureException extends Exception {
        public StructureException(String message) {
            super(message);
        }

        public StructureException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static StructureException createMissingKeyException(String key, ElementType expectedType) {
        return new StructureException("Expected property \"%s\" of type %s, but was missing!"
                .formatted(key, expectedType.getFriendlyName()));
    }

    public static StructureException createWrongTypeException(String key, ElementType expectedType, JsonElement elem) {
        return new StructureException("Expected property \"%s\" to be of type %s, but was of type %s!"
                .formatted(key, expectedType.getFriendlyName(), ElementType.of(elem).getFriendlyName()));
    }

    public static StructureException createWrongTypeException(String key, int index, ElementType expectedType, JsonElement elem) {
        return new JsonUtils.StructureException(("Expected element at index %d of array property \"%s\" to be of type %s, "
                + "but was of type %s!").formatted(index, key, expectedType.getFriendlyName(), ElementType.of(elem).getFriendlyName()));
    }

    public static boolean getBoolean(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, ElementType.BOOLEAN);
        } else if (elem instanceof JsonPrimitive prim && prim.isBoolean()) {
            return prim.getAsBoolean();
        }
        throw createWrongTypeException(key, ElementType.BOOLEAN, elem);
    }

    public static boolean getBoolean(JsonObject obj, String key, boolean def) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return def;
        } else if (elem instanceof JsonPrimitive prim && prim.isBoolean()) {
            return prim.getAsBoolean();
        }
        throw createWrongTypeException(key, ElementType.BOOLEAN, elem);
    }

    public static int getInt(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, ElementType.NUMBER);
        } else if (elem instanceof JsonPrimitive prim && prim.isNumber()) {
            return prim.getAsInt();
        }
        throw createWrongTypeException(key, ElementType.NUMBER, elem);
    }

    public static int getInt(JsonObject obj, String key, int def) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return def;
        } else if (elem instanceof JsonPrimitive prim && prim.isNumber()) {
            return prim.getAsInt();
        }
        throw createWrongTypeException(key, ElementType.NUMBER, elem);
    }

    public static String getString(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, ElementType.STRING);
        } else if (elem instanceof JsonPrimitive prim && prim.isString()) {
            return prim.getAsString();
        }
        throw createWrongTypeException(key, ElementType.STRING, elem);
    }

    public static @Nullable String getStringNullable(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return null;
        } else if (elem instanceof JsonPrimitive prim && prim.isString()) {
            return prim.getAsString();
        }
        throw createWrongTypeException(key, ElementType.STRING, elem);
    }

    public static JsonArray getArray(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, ElementType.ARRAY);
        } else if (elem instanceof JsonArray arr) {
            return arr;
        }
        throw createWrongTypeException(key, ElementType.ARRAY, elem);
    }

    public static @Nullable JsonArray getArrayNullable(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return null;
        } else if (elem instanceof JsonArray arr) {
            return arr;
        }
        throw createWrongTypeException(key, ElementType.ARRAY, elem);
    }

    public static Path getPath(JsonObject obj, String key) throws StructureException {
        String rawUri = getString(obj, key);
        try {
            return PathUtils.fromRawUri(rawUri);
        } catch (URISyntaxException | PathUtils.InvalidPathURIException e) {
            throw new StructureException(("Expected property \"%s\" to be a path URI, "
                    + "but failed to convert it into a path!").formatted(key), e);
        }
    }

    public static @Nullable Path getPathNullable(JsonObject obj, String key) throws StructureException {
        String rawUri = getStringNullable(obj, key);
        if (rawUri == null) {
            return null;
        } else {
            try {
                return PathUtils.fromRawUri(rawUri);
            } catch (URISyntaxException | PathUtils.InvalidPathURIException e) {
                throw new StructureException(("Expected property \"%s\" to be a path URI, "
                        + "but failed to convert it into a path!").formatted(key), e);
            }
        }
    }

    public static void putPath(JsonObject obj, String key, @Nullable Path path) {
        if (path == null) {
            obj.add(key, JsonNull.INSTANCE);
        } else {
            obj.addProperty(key, path.toUri().toString());
        }
    }
}
