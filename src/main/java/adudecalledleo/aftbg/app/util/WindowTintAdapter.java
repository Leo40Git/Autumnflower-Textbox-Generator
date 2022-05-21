package adudecalledleo.aftbg.app.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adudecalledleo.aftbg.window.WindowTint;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class WindowTintAdapter {
    private WindowTintAdapter() { }

    public static WindowTint read(JsonReader in) throws IOException {
        boolean gotRed = false, gotGreen = false, gotBlue = false;
        int red = 0, green = 0, blue = 0;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "red" -> {
                    red = in.nextInt();
                    gotRed = true;
                }
                case "green" -> {
                    green = in.nextInt();
                    gotGreen = true;
                }
                case "blue" -> {
                    blue = in.nextInt();
                    gotBlue = true;
                }
                default -> in.skipValue();
            }
        }
        in.endObject();

        List<String> missingFields = new ArrayList<>();
        if (!gotRed) {
            missingFields.add("red");
        }
        if (!gotGreen) {
            missingFields.add("green");
        }
        if (!gotBlue) {
            missingFields.add("blue");
        }

        if (!missingFields.isEmpty()) {
            throw new IOException("Window tint is missing following fields: %s".formatted(String.join(", ", missingFields)));
        }

        return new WindowTint(red, green, blue);
    }

    public static void write(JsonWriter out, WindowTint value) throws IOException {
        out.beginObject();
        out.name("red");
        out.value(value.red());
        out.name("green");
        out.value(value.green());
        out.name("blue");
        out.value(value.blue());
        out.endObject();
    }
}
