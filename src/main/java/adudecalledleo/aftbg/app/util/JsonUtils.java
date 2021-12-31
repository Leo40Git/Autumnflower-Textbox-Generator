package adudecalledleo.aftbg.app.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

public final class JsonUtils {
    private JsonUtils() { }
    
    public static final String TYPESTR_NULL = "null";
    public static final String TYPESTR_OBJECT = "object";
    public static final String TYPESTR_ARRAY = "array";
    public static final String TYPESTR_BOOLEAN = "boolean";
    public static final String TYPESTR_NUMBER = "number";
    public static final String TYPESTR_STRING = "string";
    
    public static String typeString(JsonElement elem) {
        if (elem == null || elem.isJsonNull()) {
            return TYPESTR_NULL;
        } else if (elem.isJsonObject()) {
            return TYPESTR_OBJECT;
        } else if (elem.isJsonArray()) {
            return TYPESTR_ARRAY;
        } else if (elem instanceof JsonPrimitive prim) {
            if (prim.isBoolean()) {
                return TYPESTR_BOOLEAN;
            } else if (prim.isNumber()) {
                return TYPESTR_NUMBER;
            } else if (prim.isString()) {
                return TYPESTR_STRING;
            }
        }
        throw new IllegalArgumentException("Unknown element " + elem);
    }

    public static class StructureException extends Exception {
        public StructureException(String message) {
            super(message);
        }

        public StructureException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static StructureException createMissingKeyException(String key, String expectedTypeStr) {
        return new StructureException("Expected property \"%s\" of type %s, but was missing!"
                .formatted(key, expectedTypeStr));
    }

    public static StructureException createWrongTypeException(String key, String expectedTypeStr, JsonElement elem) {
        return new StructureException("Expected property \"%s\" to be of type %s, but was of type %s!"
                .formatted(key, expectedTypeStr, typeString(elem)));
    }

    public static StructureException createWrongArrayElemTypeException(String key, int i, String expectedTypeStr, JsonElement elem) {
        return new JsonUtils.StructureException(("Expected element at index %d of array property \"%s\" to be of type %s, "
                + "but was of type %s!").formatted(i, key, expectedTypeStr, JsonUtils.typeString(elem)));
    }

    public static boolean getBoolean(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, TYPESTR_BOOLEAN);
        } else if (elem instanceof JsonPrimitive prim && prim.isBoolean()) {
            return prim.getAsBoolean();
        }
        throw createWrongTypeException(key, TYPESTR_BOOLEAN, elem);
    }

    public static boolean getBoolean(JsonObject obj, String key, boolean def) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return def;
        } else if (elem instanceof JsonPrimitive prim && prim.isBoolean()) {
            return prim.getAsBoolean();
        }
        throw createWrongTypeException(key, TYPESTR_BOOLEAN, elem);
    }

    public static int getInt(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, TYPESTR_NUMBER);
        } else if (elem instanceof JsonPrimitive prim && prim.isNumber()) {
            return prim.getAsInt();
        }
        throw createWrongTypeException(key, TYPESTR_NUMBER, elem);
    }

    public static int getInt(JsonObject obj, String key, int def) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return def;
        } else if (elem instanceof JsonPrimitive prim && prim.isNumber()) {
            return prim.getAsInt();
        }
        throw createWrongTypeException(key, TYPESTR_NUMBER, elem);
    }

    public static @Nullable String getString(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return null;
        } else if (elem instanceof JsonPrimitive prim && prim.isString()) {
            return prim.getAsString();
        }
        throw createWrongTypeException(key, TYPESTR_STRING, elem);
    }

    public static @Nullable JsonArray getArray(JsonObject obj, String key) throws StructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            return null;
        } else if (elem instanceof JsonArray arr) {
            return arr;
        }
        throw createWrongTypeException(key, TYPESTR_ARRAY, elem);
    }

    public static @Nullable Path getPath(JsonObject obj, String key) throws StructureException {
        String rawUri = getString(obj, key);
        if (rawUri == null) {
            return null;
        } else {
            URI uri;
            try {
                uri = new URI(rawUri);
            } catch (URISyntaxException e) {
                throw new StructureException(("Expected property \"%s\" to be a URI, "
                        + "but it couldn't be parsed as such!").formatted(key), e);
            }
            try {
                return Paths.get(uri);
            } catch (Exception e) {
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
