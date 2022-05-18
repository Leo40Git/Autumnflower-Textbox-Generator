package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import adudecalledleo.aftbg.app.util.ColorUtils;

public final class UIColor {
    public static String toCSS(Color color) {
        return "#%02x%02x%02x%02x".formatted(color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha());
    }

    private final Color value;
    private final String valueAsCSS;

    UIColor(Color value) {
        this.value = value;
        this.valueAsCSS = toCSS(value);
    }

    public Color get() {
        return value;
    }

    public String getAsCSS() {
        return valueAsCSS;
    }

    public UIColor darker(double factor) {
        return new UIColor(ColorUtils.darker(this.value, factor));
    }

    public UIColor withAlpha(int newAlpha) {
        return new UIColor(ColorUtils.withAlpha(this.value, newAlpha));
    }
}
