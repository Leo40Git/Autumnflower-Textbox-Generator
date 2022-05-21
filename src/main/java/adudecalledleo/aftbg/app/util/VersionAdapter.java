package adudecalledleo.aftbg.app.util;

import java.io.IOException;

import adudecalledleo.aftbg.json.MalformedJsonException;
import de.skuzzle.semantic.Version;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class VersionAdapter {
    public static Version read(JsonReader in) throws IOException {
        var s = in.nextString();
        try {
            return Version.parseVersion(in.nextString(), true);
        } catch (Version.VersionFormatException e) {
            throw new MalformedJsonException(in, "Invalid version \"%s\"".formatted(s), e);
        }
    }

    public static void write(JsonWriter out, Version value) throws IOException {
        out.value(value.toString());
    }
}
