package adudecalledleo.aftbg.json;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

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

    @FunctionalInterface
    public interface KeySerializer<K> {
        String serialize(K key);
    }

    /**
     * Writes a map to JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     *
     * As such, this method can only be used if the type of the key can be serialized as a string.
     *
     * @param writer JSON writer
     * @param keySerializer serializer of key objects
     * @param valueDelegate delegate to write value objects
     * @param values the map to write
     * @param <K> type of key
     * @param <V> type of value
     * @throws IOException if an I/O exception occurs.
     */
    public static <K, V> void writeSimpleMap(JsonWriter writer, KeySerializer<K> keySerializer,
                                             JsonWriteDelegate<V> valueDelegate, Map<K, V> values) throws IOException {
        writer.beginObject();
        for (var entry : values.entrySet()) {
            writer.name(keySerializer.serialize(entry.getKey()));
            valueDelegate.write(writer, entry.getValue());
        }
        writer.endObject();
    }

    /**
     * Writes a map to JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     *
     * This method is a specialization of {@link #writeSimpleMap(JsonWriter, KeySerializer, JsonWriteDelegate, Map)},
     * for maps with string keys.
     *
     * @param writer JSON writer
     * @param valueDelegate delegate to write value objects
     * @param values the map to write
     * @param <V> type of value
     * @throws IOException if an I/O exception occurs.
     */
    public static <V> void writeSimpleMap(JsonWriter writer, JsonWriteDelegate<V> valueDelegate, Map<String, V> values)
            throws IOException {
        writer.beginObject();
        for (var entry : values.entrySet()) {
            writer.name(entry.getKey());
            valueDelegate.write(writer, entry.getValue());
        }
        writer.endObject();
    }

    /**
     * Writes a map to JSON. This method uses a complex format of an array of entry objects:
     * <pre><code>
     * [
     *   {
     *     "key": {
     *       ...
     *     }
     *     "value": {
     *       ...
     *     }
     *   }
     * ]
     * </code></pre>
     *
     * @param writer JSON writer
     * @param keyDelegate delegate to write key objects
     * @param valueDelegate delegate to write value objects
     * @param values the map to write
     * @param <K> type of key
     * @param <V> type of value
     * @throws IOException if an I/O exception occurs.
     */
    public static <K, V> void writeComplexMap(JsonWriter writer,
                                              JsonWriteDelegate<K> keyDelegate, JsonWriteDelegate<V> valueDelegate,
                                              Map<K, V> values) throws IOException {
        writer.beginArray();
        for (var entry : values.entrySet()) {
            writer.beginObject();
            writer.name("key");
            keyDelegate.write(writer, entry.getKey());
            writer.name("value");
            valueDelegate.write(writer, entry.getValue());
            writer.endObject();
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
