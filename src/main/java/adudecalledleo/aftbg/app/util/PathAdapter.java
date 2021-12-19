package adudecalledleo.aftbg.app.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathAdapter extends TypeAdapter<Path> {
    @Override
    public Path read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.skipValue();
            return null;
        }
        return Paths.get(in.nextString());
    }

    @Override
    public void write(JsonWriter out, Path value) throws IOException {
        out.value(value.toString().replaceAll("\\\\", "/"));
    }
}
