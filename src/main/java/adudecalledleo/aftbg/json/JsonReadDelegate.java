package adudecalledleo.aftbg.json;

import java.io.IOException;

import org.quiltmc.json5.JsonReader;

@FunctionalInterface
public interface JsonReadDelegate<T> {
    T read(JsonReader reader) throws IOException;
}
