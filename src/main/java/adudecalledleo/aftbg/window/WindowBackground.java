package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public final class WindowBackground {
    // BG is made up of a base and an overlay
    // both are tiled
    // additionally, there's a tone layer that's rendered over the base and overlay
    // RPG Maker uses a color matrix filter to apply it, which I replicate through a custom Composite

    private final BufferedImage base, overlay;
    private final WindowColor color;
    private final int tileWidth = 96, tileHeight = 96;
    private BufferedImage scratchBuf;

    public WindowBackground(BufferedImage window, WindowColor color) {
        base = window.getSubimage(0, 0, tileWidth, tileHeight);
        overlay = window.getSubimage(0, tileHeight, tileWidth, tileHeight);
        this.color = color;
    }

    public void draw(Graphics2D g, int x, int y, int width, int height, ImageObserver observer) {
        if (resizeScratchBuf(width, height)) {
            Graphics2D sg = scratchBuf.createGraphics();

            drawPart(base, sg, width, height, observer);
            drawPart(overlay, sg, width, height, observer);

            sg.setComposite(new WindowBackgroundComposite(color));
            sg.fillRect(0, 0, width, height);

            sg.dispose();
        }

        g.drawImage(scratchBuf, x, y, observer);
    }

    private boolean resizeScratchBuf(int width, int height) {
        if (scratchBuf == null || scratchBuf.getWidth() < width || scratchBuf.getHeight() < height) {
            scratchBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            return true;
        }
        return false;
    }

    private void drawPart(BufferedImage part, Graphics2D g, int width, int height, ImageObserver observer) {
        final int tilesWide = width / tileWidth, tilesHigh = height / tileHeight;
        for (int ty = 0; ty <= tilesHigh; ty++) {
            for (int tx = 0; tx <= tilesWide; tx++) {
                g.drawImage(part, tx * tileWidth, ty * tileHeight, observer);
            }
        }
    }
}
