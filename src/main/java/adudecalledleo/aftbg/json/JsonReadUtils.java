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
