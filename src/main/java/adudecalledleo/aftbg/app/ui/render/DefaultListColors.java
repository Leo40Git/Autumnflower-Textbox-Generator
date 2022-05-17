package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.util.ColorUtils;

public final class DefaultListColors {
    private static Color background, darkerBackground, selectionBackground, hoveredBackground;

    public static void update() {
        background = UIManager.getColor("List.background");
        darkerBackground = ColorUtils.darker(background, 0.9);
        selectionBackground = UIManager.getColor("List.selectionBackground");
        hoveredBackground = ColorUtils.withAlpha(selectionBackground, 127);
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
}
