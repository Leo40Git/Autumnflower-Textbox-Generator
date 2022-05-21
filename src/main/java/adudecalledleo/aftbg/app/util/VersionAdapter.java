package adudecalledleo.aftbg.app.util;

import java.io.IOException;

import de.skuzzle.semantic.Version;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class VersionAdapter {
    public static Version read(JsonReader in) throws IOException {
        var s = in.nextString();
        try {
            return Version.parseVersion(in.nextString(), true);
        } catch (Version.VersionFormatException e) {
            throw new IOException("Invalid version \"%s\"%s".formatted(s, in.locationString()), e);
        }
    }

    public static void write(JsonWriter out, Version value) throws IOException {
        out.value(value.toString());
    }
}
