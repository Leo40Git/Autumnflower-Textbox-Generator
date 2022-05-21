package adudecalledleo.aftbg.json;

import java.io.IOException;

import org.quiltmc.json5.JsonWriter;

@FunctionalInterface
public interface JsonWriteDelegate<T> {
    void write(JsonWriter writer, T value) throws IOException;
}
