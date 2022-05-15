package adudecalledleo.aftbg.app.ui.util;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppResources;

public final class IconWithArrow implements Icon {
    private static final int GAP = 6;

    private final Icon original, arrow;

    public IconWithArrow(Icon original) {
        this.original = original;
        this.arrow = AppResources.getArrowIcon();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        int height = getIconHeight();
        original.paintIcon(c, g, x, y + (height - original.getIconHeight()) / 2);
        arrow.paintIcon(c, g, x + GAP + original.getIconWidth(), y + (height - arrow.getIconHeight()) / 2);
    }

    @Override
    public int getIconWidth() {
        return original.getIconWidth() + GAP + arrow.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return Math.max(original.getIconHeight(), arrow.getIconHeight());
    }
}
