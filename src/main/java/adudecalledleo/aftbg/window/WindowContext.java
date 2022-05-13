package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.*;

/**
 * <b>NOTE:</b> This class is <em>not safe</em> for multithreading.
 *
 * <p>If you need to render the window on multiple threads, use the {@link #copy()} method to get a copy to
 * pass to another thread.
 */
public final class WindowContext {
    private final WindowBackground background;
    private final WindowBorder border;
    private final WindowPalette colors;
    private final WindowArrow arrow;

    public WindowContext(BufferedImage window, WindowTint tint) {
        background = new WindowBackground(window, tint);
        border = new WindowBorder(window);
        arrow = new WindowArrow(window);
        colors = new WindowPalette(window);
    }

    private WindowContext(WindowBackground background, WindowBorder border, WindowPalette colors, WindowArrow arrow) {
        this.background = background;
        this.border = border;
        this.colors = colors;
        this.arrow = arrow;
    }

    public WindowContext copy() {
        return new WindowContext(background.copy(), border, colors, arrow);
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

    public WindowPalette getColors() {
        return colors;
    }

    public Color getColor(int index) {
        return colors.get(index);
    }
}
