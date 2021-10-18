package adudecalledleo.aftbg.window;

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
