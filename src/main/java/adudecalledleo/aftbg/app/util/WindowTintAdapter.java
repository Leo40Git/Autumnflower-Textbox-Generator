package adudecalledleo.aftbg.app.util;

import adudecalledleo.aftbg.window.WindowTint;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class WindowTintAdapter extends TypeAdapter<WindowTint> {
    @Override
    public WindowTint read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.skipValue();
            return null;
        }
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
        if (!gotRed) {
            throw new IllegalStateException("Window tint missing required value 'red'");
        } else if (!gotGreen) {
            throw new IllegalStateException("Window tint missing required value 'green'");
        } else if (!gotBlue) {
            throw new IllegalStateException("Window tint missing required value 'blue'");
        } else {
            return new WindowTint(red, green, blue);
        }
    }

    @Override
    public void write(JsonWriter out, WindowTint value) throws IOException {
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
