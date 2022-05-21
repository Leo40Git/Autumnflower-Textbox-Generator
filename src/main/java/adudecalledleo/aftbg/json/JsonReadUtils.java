package adudecalledleo.aftbg.json;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;

public final class JsonReadUtils {
    private JsonReadUtils() { }

    public static <T> void readArray(JsonReader reader, JsonReadDelegate<T> delegate, Consumer<T> consumer) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            consumer.accept(delegate.read(reader));
        }
        reader.endArray();
    }

    public static <T> void readNullableArray(JsonReader reader, JsonReadDelegate<T> delegate, Consumer<T> consumer) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return;
        }

        readArray(reader, delegate, consumer);
    }

    public static <T> List<T> readArray(JsonReader reader, JsonReadDelegate<T> delegate) throws IOException {
        List<T> list = new ArrayList<>();
        readArray(reader, delegate, list::add);
        return list;
    }

    public static <T> Set<T> readUniqueArray(JsonReader reader, JsonReadDelegate<T> delegate) throws IOException {
        Set<T> set = new LinkedHashSet<>(); // essentially a List that doesn't allow duplicate elements
        readArray(reader, delegate, set::add);
        return set;
    }

    public interface KeyParser<N> {
        N parse(String name) throws Exception;
    }

    /**
     * Reads a map from JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     *
     * As such, this method can only be used if the type of the key can be parsed from a string.
     *
     * @param reader JSON reader
     * @param keyParser parser of key objects
     * @param valueDelegate delegate to read value objects
     * @param consumer consumer to accept every read entry
     * @param <K> type of key
     * @param <V> type of value
     * @throws IOException if an I/O exception occurs.
     */
    public static <K, V> void readSimpleMap(JsonReader reader,
                                            KeyParser<K> keyParser, JsonReadDelegate<V> valueDelegate,
                                            BiConsumer<K, V> consumer) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            K key;
            try {
                key = keyParser.parse(name);
            } catch (Exception e) {
                throw new IOException("Failed to parse key \"%s\"%s".formatted(name, reader.locationString()), e);
            }
            consumer.accept(key, valueDelegate.read(reader));
        }
        reader.endObject();
    }

    /**
     * Reads a map from JSON. This method uses the simple object format:
     * <pre><code>
     * {
     *   "key1": "value1",
     *   "key2": "value2"
     * }
     * </code></pre>
     *
     * This method is a specialization of {@link #readSimpleMap(JsonReader, KeyParser, JsonReadDelegate, BiConsumer)},
     * for maps with string keys.
     *
     * @param reader JSON reader
     * @param valueDelegate delegate to read value objects
     * @param consumer consumer to accept entries
     * @param <V> type of value
     * @throws IOException if an I/O exception occurs.
     */
    public static <V> void readSimpleMap(JsonReader reader, JsonReadDelegate<V> valueDelegate, BiConsumer<String, V> consumer)
        throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            consumer.accept(reader.nextName(), valueDelegate.read(reader));
        }
        reader.endObject();
    }

    /**
     * Reads a map from JSON. This method uses a complex format of an array of entry objects:
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
     * @param reader JSON reader
     * @param keyDelegate delegate to read key objects
     * @param valueDelegate delegate to read value objects
     * @param consumer consumer to accept entries
     * @param <K> type of key
     * @param <V> type of value
     * @throws IOException if an I/O exception occurs.
     */
    public static <K, V> void readComplexMap(JsonReader reader,
                                             JsonReadDelegate<K> keyDelegate, JsonReadDelegate<V> valueDelegate,
                                             BiConsumer<K, V> consumer) throws IOException {
        List<String> missingFields = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            K key = null;
            boolean gotValue = false;
            V value = null;
            while (reader.hasNext()) {
                String field = reader.nextName();
                switch (field) {
                    case "key" -> key = keyDelegate.read(reader);
                    case "value" -> {
                        value = valueDelegate.read(reader);
                        gotValue = true;
                    }
                    default -> reader.skipValue();
                }
            }
            reader.endObject();

            if (key == null) {
                missingFields.add("key");
            }
            if (!gotValue) {
                missingFields.add("value");
            }

            if (!missingFields.isEmpty()) {
                throw new IOException("Map entry%sismissing following fields: %s"
                        .formatted(reader.locationString(), String.join(", ", missingFields)));
            }

            consumer.accept(key, value);
        }
        reader.endArray();
    }

    public static <T> @Nullable T readNullable(JsonReader reader, JsonReadDelegate<T> delegate) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return delegate.read(reader);
    }

    private static final String[] DUMMY_STRING_ARRAY = new String[0];

    public static String[] readStringArray(JsonReader reader) throws IOException {
        return readArray(reader, JsonReader::nextString).toArray(DUMMY_STRING_ARRAY);
    }

    public static URL readURL(JsonReader reader) throws IOException {
        String s = reader.nextString();
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new IOException("Failed to parse URL \"%s\"%s".formatted(s, reader.locationString()));
        }
    }

    public static @Nullable URL readNullableURL(JsonReader reader) throws IOException {
        return readNullable(reader, JsonReadUtils::readURL);
    }

    public static Path readPath(JsonReader reader) throws IOException {
        String s = reader.nextString();
        URI uri;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            throw new IOException("Failed to parse path URI \"%s\"%s".formatted(s, reader.locationString()), e);
        }
        return Paths.get(uri);
    }
}