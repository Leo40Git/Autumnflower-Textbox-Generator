package adudecalledleo.aftbg.app.util.json;

import java.net.URISyntaxException;
import java.nio.file.Path;

import adudecalledleo.aftbg.app.util.InvalidPathURIException;
import adudecalledleo.aftbg.app.util.PathUtils;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

public final class JsonUtils {
    private JsonUtils() { }

    public static JsonStructureException createMissingKeyException(String key, JsonType expectedType) {
        return new JsonStructureException("Expected property \"%s\" of type %s, but was missing!"
                .formatted(key, expectedType.getFriendlyName()));
    }

    public static JsonStructureException createWrongTypeException(String key, JsonType expectedType, JsonElement elem) {
        return new JsonStructureException("Expected property \"%s\" to be of type %s, but was of type %s!"
                .formatted(key, expectedType.getFriendlyName(), JsonType.of(elem).getFriendlyName()));
    }

    public static JsonStructureException createWrongTypeException(String key, int index, JsonType expectedType, JsonElement elem) {
        return new JsonStructureException(("Expected element at index %d of array property \"%s\" to be of type %s, "
                + "but was of type %s!").formatted(index, key, expectedType.getFriendlyName(), JsonType.of(elem).getFriendlyName()));
    }

    public static boolean getBoolean(JsonObject obj, String key) throws JsonStructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, JsonType.BOOLEAN);
        } else if (elem instanceof JsonPrimitive prim && prim.isBoolean()) {
            return prim.getAsBoolean();
        }
        throw createWrongTypeException(key, JsonType.BOOLEAN, elem);
    }

    public static boolean getBoolean(JsonObject obj, String key, boolean def) throws JsonStructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            return def;
        } else if (elem instanceof JsonPrimitive prim && prim.isBoolean()) {
            return prim.getAsBoolean();
        }
        throw createWrongTypeException(key, JsonType.BOOLEAN, elem);
    }

    public static int getInt(JsonObject obj, String key) throws JsonStructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, JsonType.NUMBER);
        } else if (elem instanceof JsonPrimitive prim && prim.isNumber()) {
            return prim.getAsInt();
        }
        throw createWrongTypeException(key, JsonType.NUMBER, elem);
    }

    public static int getInt(JsonObject obj, String key, int def) throws JsonStructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            return def;
        } else if (elem instanceof JsonPrimitive prim && prim.isNumber()) {
            return prim.getAsInt();
        }
        throw createWrongTypeException(key, JsonType.NUMBER, elem);
    }

    public static String getString(JsonObject obj, String key) throws JsonStructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, JsonType.STRING);
        } else if (elem instanceof JsonPrimitive prim && prim.isString()) {
            return prim.getAsString();
        }
        throw createWrongTypeException(key, JsonType.STRING, elem);
    }

    public static @Nullable String getStringNullable(JsonObject obj, String key) throws JsonStructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return null;
        } else if (elem instanceof JsonPrimitive prim && prim.isString()) {
            return prim.getAsString();
        }
        throw createWrongTypeException(key, JsonType.STRING, elem);
    }

    public static JsonArray getArray(JsonObject obj, String key) throws JsonStructureException {
        JsonElement elem = obj.get(key);
        if (elem == null) {
            throw createMissingKeyException(key, JsonType.ARRAY);
        } else if (elem instanceof JsonArray arr) {
            return arr;
        }
        throw createWrongTypeException(key, JsonType.ARRAY, elem);
    }

    public static @Nullable JsonArray getArrayNullable(JsonObject obj, String key) throws JsonStructureException {
        JsonElement elem = obj.get(key);
        if (elem == null || elem.isJsonNull()) {
            return null;
        } else if (elem instanceof JsonArray arr) {
            return arr;
        }
        throw createWrongTypeException(key, JsonType.ARRAY, elem);
    }

    public static Path getPath(JsonObject obj, String key) throws JsonStructureException {
        String rawUri = getString(obj, key);
        try {
            return PathUtils.fromRawUri(rawUri);
        } catch (URISyntaxException | InvalidPathURIException e) {
            throw new JsonStructureException(("Expected property \"%s\" to be a path URI, "
                    + "but failed to convert it into a path!").formatted(key), e);
        }
    }

    public static @Nullable Path getPathNullable(JsonObject obj, String key) throws JsonStructureException {
        String rawUri = getStringNullable(obj, key);
        if (rawUri == null) {
            return null;
        } else {
            try {
                return PathUtils.fromRawUri(rawUri);
            } catch (URISyntaxException | InvalidPathURIException e) {
                throw new JsonStructureException(("Expected property \"%s\" to be a path URI, "
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
