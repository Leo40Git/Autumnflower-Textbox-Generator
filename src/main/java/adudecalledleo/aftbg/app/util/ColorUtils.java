package adudecalledleo.aftbg.app.util;

import java.awt.*;

public final class ColorUtils {
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private ColorUtils() { }

    public static Color darker(Color original, double factor) {
        return new Color(Math.max((int)(original.getRed() * factor), 0),
                Math.max((int)(original.getGreen() * factor), 0),
                Math.max((int)(original.getBlue() * factor), 0),
                original.getAlpha());
    }
}
