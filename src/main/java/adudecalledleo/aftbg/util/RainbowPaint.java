package adudecalledleo.aftbg.util;

import java.awt.*;
import java.awt.geom.Point2D;

public final class RainbowPaint {
    private RainbowPaint() { }

    private static final int COLOR_STOPS = 36;

    private static Paint paint;

    public static Paint get() {
        if (paint == null) {
            paint = create();
        }
        return paint;
    }

    private static Paint create() {
        Color[] colors = new Color[COLOR_STOPS];
        float[] fractions = new float[COLOR_STOPS];

        float f = 1 / (float) (COLOR_STOPS - 1);

        for (int i = 0; i < COLOR_STOPS; i++) {
            colors[i] = new Color(Color.HSBtoRGB(i * f, 1, 1));
            fractions[i] = i * f;
        }

        return new LinearGradientPaint(new Point2D.Double(0, 0), new Point2D.Double(50, 50),
                fractions, colors, MultipleGradientPaint.CycleMethod.REPEAT);
    }
}
