package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.util.ColorUtils;

public final class DefaultListColors {
    private static Color background, darkerBackground, selectionBackground, hoveredBackground,
            disabledBackground, darkerDisabledBackground;

    public static void update() {
        background = UIManager.getColor("List.background");
        darkerBackground = ColorUtils.darker(background, 0.9);
        selectionBackground = UIManager.getColor("List.selectionBackground");
        hoveredBackground = ColorUtils.withAlpha(selectionBackground, 127);
        disabledBackground = ColorUtils.darker(background, 0.6);
        darkerDisabledBackground = ColorUtils.darker(disabledBackground, 0.9);
    }

    public static Color getBackground() {
        return background;
    }

    public static Color getDarkerBackground() {
        return darkerBackground;
    }

    public static Color getSelectionBackground() {
        return selectionBackground;
    }

    public static Color getHoveredBackground() {
        return hoveredBackground;
    }

    public static Color getDisabledBackground() {
        return disabledBackground;
    }

    public static Color getDarkerDisabledBackground() {
        return darkerDisabledBackground;
    }
}
