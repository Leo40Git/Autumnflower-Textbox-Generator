package adudecalledleo.aftbg.app.ui.util;

import java.awt.*;

import javax.swing.*;

public record ColorIcon(Color color, int width, int height) implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }
}
