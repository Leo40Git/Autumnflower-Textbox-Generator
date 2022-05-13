package adudecalledleo.aftbg.app.text.node.color;

import java.awt.*;

import adudecalledleo.aftbg.app.data.DataKey;
import adudecalledleo.aftbg.app.data.DataTracker;
import adudecalledleo.aftbg.window.WindowColors;
import org.jetbrains.annotations.Nullable;

public final class ColorParser {
    public static final DataKey<WindowColors> PALETTE = new DataKey<>(WindowColors.class, "palette");

    private ColorParser() { }

    public static @Nullable Color parseColor(DataTracker ctx, String value) {
        if (value.startsWith("#")) {
            value = value.substring(1);
            int hexLen = value.length();
            if (hexLen != 3 && hexLen != 6) {
                throw new IllegalArgumentException("unknown hex color format, should be 3 or 6 chars long");
            }
            int rgb;
            try {
                rgb = Integer.parseUnsignedInt(value, 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("failed to parse hex color", e);
            }
            int r, g, b;
            if (hexLen == 3) {
                // CSS-style
                r = rgb & 0xF;
                r += r << 4;
                g = (rgb >> 4) & 0xF;
                g += g << 4;
                b = (rgb >> 8) & 0xF;
                b += b << 4;
            } else {
                // standard
                r = rgb & 0xFF;
                g = (rgb >> 8) & 0xFF;
                b = (rgb >> 16) & 0xFF;
            }
            return new Color(r | g << 8 | b << 16, false);
        } else {
            value = value.trim();
            if ("default".equals(value)) {
                return null;
            } else if (value.contains("(") && value.contains(")")) {
                int obIdx = value.indexOf('(');
                String argsStr = value.substring(obIdx + 1, value.indexOf(')')).trim();
                value = value.substring(0, obIdx).trim();
                /// function-like - value is function name
                if ("palette".equals(value)) {
                    if (!ctx.containsKey(PALETTE)) {
                        throw new IllegalArgumentException("no palette exists");
                    }
                    @SuppressWarnings("OptionalGetWithoutIsPresent") // checked above
                    var pal = ctx.get(PALETTE).get();
                    int palIdx;
                    try {
                        palIdx = Integer.parseUnsignedInt(argsStr);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("failed to parse palette index", e);
                    }
                    if (palIdx >= WindowColors.COUNT) {
                        throw new IllegalArgumentException("palette index is out of bounds (must be below " + WindowColors.COUNT + ")");
                    }
                    return pal.get(palIdx);
                } else if ("rgb".equals(value)) {
                    String[] compsStr = argsStr.split(",");
                    if (compsStr.length != 3) {
                        throw new IllegalArgumentException("rgb function takes 3 arguments");
                    }
                    int r = parseRGBComponent(compsStr[0], "red");
                    int g = parseRGBComponent(compsStr[1], "green");
                    int b = parseRGBComponent(compsStr[2], "blue");
                    return new Color(r, g, b, 255);
                }
            }
        }
        throw new IllegalArgumentException("could not parse \"" + value + "\" as color");
    }

    private static int parseRGBComponent(String str, String name) {
        int i;
        try {
            i = Integer.parseInt(str.trim(), 10);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("failed to parse " + name + " component", e);
        }
        if (i < 0 || i > 255) {
            throw new IllegalArgumentException(name + " component must be between 0 and 255");
        }
        return i;
    }
}
