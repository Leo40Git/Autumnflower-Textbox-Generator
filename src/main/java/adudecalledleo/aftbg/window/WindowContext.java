package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public final class WindowContext {
    private final WindowBackground background;
    private final WindowBorder border;
    private final WindowColors colors;
    private final WindowArrow arrow;

    public WindowContext(BufferedImage window, WindowTint tint) {
        background = new WindowBackground(window, tint);
        border = new WindowBorder(window);
        arrow = new WindowArrow(window);
        colors = new WindowColors(window);
    }

    public void drawBackground(Graphics2D g, int x, int y, int width, int height, ImageObserver observer) {
        background.draw(g, x, y, width, height, observer);
    }

    public void drawBorder(Graphics g, int x, int y, int width, int height, ImageObserver observer) {
        border.draw(g, x, y, width, height, observer);
    }

    public void drawArrow(Graphics g, int boxX, int boxY, int boxWidth, int boxHeight, int frame, ImageObserver observer) {
        arrow.draw(g, boxX, boxY, boxWidth, boxHeight, frame, observer);
    }

    public WindowColors getColors() {
        return colors;
    }

    public Color getColor(int index) {
        return colors.get(index);
    }
}
