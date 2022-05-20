package adudecalledleo.aftbg.json;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;

public final class JsonReadUtils {
    private JsonReadUtils() { }

    public static String[] readStringArray(JsonReader reader) throws IOException {
        List<String> list = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(reader.nextString());
        }
        reader.endArray();
        return list.toArray(String[]::new);
    }

    public static URL readURL(JsonReader reader) throws IOException {
        String s = reader.nextString();
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new IOException("Failed to parse URL \"%s\"%s".formatted(s, reader.locationString()));
        }
    }

    public static <T> @Nullable T readNullable(JsonReader reader, JsonReadDelegate<T> delegate) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return delegate.read(reader);
    }

    public static @Nullable URL readNullableURL(JsonReader reader) throws IOException {
        return readNullable(reader, JsonReadUtils::readURL);
    }
}
