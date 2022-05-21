package adudecalledleo.aftbg.json;

import java.io.IOException;
import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonWriter;

public final class JsonWriteUtils {
    private JsonWriteUtils() { }

    public static <T> void writeArray(JsonWriter writer, JsonWriteDelegate<T> delegate, Iterable<T> values) throws IOException {
        writer.beginArray();
        for (var value : values) {
            delegate.write(writer, value);
        }
        writer.endArray();
    }

    public static <T> void writeNullable(JsonWriter writer, JsonWriteDelegate<T> delegate, @Nullable T value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            delegate.write(writer, value);
        }
    }

    public static void writePath(JsonWriter writer, Path path) throws IOException {
        writer.value(path.toUri().toString());
    }
}
