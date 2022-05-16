package adudecalledleo.aftbg.app.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

public enum JsonType {
    NULL("null"), OBJECT("object"), ARRAY("array"),
    BOOLEAN("boolean"), NUMBER("number"), STRING("string");

    private final String friendlyName;

    JsonType(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public String toString() {
        return friendlyName;
    }

    public static JsonType of(@Nullable JsonElement elem) {
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
