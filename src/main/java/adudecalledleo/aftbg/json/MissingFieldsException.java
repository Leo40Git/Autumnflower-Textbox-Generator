package adudecalledleo.aftbg.json;

import org.quiltmc.json5.JsonReader;

public class MissingFieldsException extends MalformedJsonException {
    public MissingFieldsException(String name, Iterable<String> missingFields) {
        super("%s is missing following fields: %s".formatted(name, String.join(", ", missingFields)));
    }

    public MissingFieldsException(String name, String... missingFields) {
        super("%s is missing following fields: %s".formatted(name, String.join(", ", missingFields)));
    }

    public MissingFieldsException(JsonReader reader, String name, Iterable<String> missingFields) {
        super("%s%s is missing following fields: %s".formatted(name, reader.locationString(),
                String.join(", ", missingFields)));
    }

    public MissingFieldsException(JsonReader reader, String name, String... missingFields) {
        super("%s%s is missing following fields: %s".formatted(name, reader.locationString(),
                String.join(", ", missingFields)));
    }
}
