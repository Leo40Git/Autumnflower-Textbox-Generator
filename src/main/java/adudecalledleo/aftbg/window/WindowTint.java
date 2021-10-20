package adudecalledleo.aftbg.window;

/**
 * Represents a tint. Each channel of said tint can be additive or subtractive.<p>
 *
 * Appears to be called "tone" in RPG Maker, and also has an extra "gray" channel for saturation -
 * however, that isn't supported for the window background, so it's not present here.
 */
public record WindowTint(int red, int green, int blue) {
    public WindowTint {
        checkRange(red, "Red");
        checkRange(green, "Green");
        checkRange(blue, "Blue");
    }

    private static void checkRange(int c, String name) {
        if (c < -255 || c > 255) {
            throw new IllegalArgumentException(name + " component must be between -255 and 255, inclusive (was " + c + ")");
        }
    }
}
