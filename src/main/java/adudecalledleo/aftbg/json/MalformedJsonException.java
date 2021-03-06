package adudecalledleo.aftbg.json;

import java.io.IOException;

import org.quiltmc.json5.JsonReader;

public class MalformedJsonException extends IOException {
    public MalformedJsonException(String message) {
        super(message);
    }

    public MalformedJsonException(JsonReader reader, String message) {
        this(message + reader.locationString());
    }

    public MalformedJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedJsonException(JsonReader reader, String message, Throwable cause) {
        this(message + reader.locationString(), cause);
    }
}
