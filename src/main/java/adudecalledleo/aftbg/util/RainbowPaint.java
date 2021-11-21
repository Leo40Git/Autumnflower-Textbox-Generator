package adudecalledleo.aftbg.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public final class RainbowPaint {
    private RainbowPaint() { }

    private static final int COLOR_STOPS = 36;

    private static boolean paramsGenerated = false;
    private static Color[] colors;
    private static float[] fractions;

    private static Paint identityPaint;

    public static Paint get() {
        if (identityPaint == null) {
            identityPaint = createIdentity();
        }
        return identityPaint;
    }

    public static Paint get(AffineTransform gradientTransform) {
        if (gradientTransform.isIdentity()) {
            return get();
        }
        generateParams();
        return new LinearGradientPaint(new Point2D.Double(0, 0), new Point2D.Double(50, 50),
                fractions, colors, MultipleGradientPaint.CycleMethod.REPEAT, MultipleGradientPaint.ColorSpaceType.SRGB,
                gradientTransform);
    }

    private static Paint createIdentity() {
        generateParams();
        return new LinearGradientPaint(new Point2D.Double(0, 0), new Point2D.Double(50, 50),
                fractions, colors, MultipleGradientPaint.CycleMethod.REPEAT, MultipleGradientPaint.ColorSpaceType.SRGB,
                new AffineTransform());
    }

    private static void generateParams() {
        if (!paramsGenerated) {
            colors = new Color[COLOR_STOPS];
            fractions = new float[COLOR_STOPS];

            float f = 1 / (float) (COLOR_STOPS - 1);

            for (int i = 0; i < COLOR_STOPS; i++) {
                colors[i] = new Color(Color.HSBtoRGB(i * f, 1, 1));
                fractions[i] = i * f;
            }

            paramsGenerated = true;
        }
    }
}
