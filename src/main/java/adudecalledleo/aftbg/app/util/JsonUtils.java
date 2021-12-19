package adudecalledleo.aftbg.app.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
        if (elem == null) {
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
        if (elem == null) {
            return def;
        } else if (elem instanceof JsonPrimitive prim && prim.isNumber()) {
            return prim.getAsInt();
        }
        throw createWrongTypeException(key, TYPESTR_NUMBER, elem);
    }
}
