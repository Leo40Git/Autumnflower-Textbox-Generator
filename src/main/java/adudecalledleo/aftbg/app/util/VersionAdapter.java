package adudecalledleo.aftbg.app.util;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.skuzzle.semantic.Version;

public final class VersionAdapter extends TypeAdapter<Version> {
    @Override
    public Version read(JsonReader in) throws IOException {
        return Version.parseVersion(in.nextString(), true);
    }

    @Override
    public void write(JsonWriter out, Version value) throws IOException {
        out.value(value.toString());
    }
}
