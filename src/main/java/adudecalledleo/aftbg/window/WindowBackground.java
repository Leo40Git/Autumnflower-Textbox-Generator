package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public final class WindowBackground {
    // there are 2 separate layers for this in the Window sheet: a "base" one and an "overlay" one
    // as far as I understand it, RPG Maker sandwiches the "base" and "overlay" layers of the BG together,
    // tints them with the editor-specified window tint, and then renders that tinted sandwich at 75% opacity
    // (here we tint the image and pre-multiply it to be at 75% opacity, just because they're convenient to do
    //  at the same time)

    private final BufferedImage base, overlay;
    private final WindowTint color;
    private final int tileWidth = 96, tileHeight = 96;
    private BufferedImage scratchBuf;

    public WindowBackground(BufferedImage window, WindowTint color) {
        base = window.getSubimage(0, 0, tileWidth, tileHeight);
        overlay = window.getSubimage(0, tileHeight, tileWidth, tileHeight);
        this.color = color;
    }

    public void draw(Graphics2D g, int x, int y, int width, int height, ImageObserver observer) {
        if (resizeScratchBuf(width, height)) {
            Graphics2D sg = scratchBuf.createGraphics();

            drawPart(base, sg, width, height);
            drawPart(overlay, sg, width, height);

            sg.setComposite(new WindowBackgroundComposite(color));
            sg.fillRect(0, 0, width, height);

            sg.dispose();
        }

        g.drawImage(scratchBuf, x, y, x + width, y + height, 0, 0, width, height, observer);
    }

    private boolean resizeScratchBuf(int width, int height) {
        if (scratchBuf == null || scratchBuf.getWidth() < width || scratchBuf.getHeight() < height) {
            scratchBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            return true;
        }
        return false;
    }

    private void drawPart(BufferedImage part, Graphics g, int width, int height) {
        final int tilesWide = width / tileWidth, tilesHigh = height / tileHeight;
        for (int ty = 0; ty <= tilesHigh; ty++) {
            for (int tx = 0; tx <= tilesWide; tx++) {
                g.drawImage(part, tx * tileWidth, ty * tileHeight, null);
            }
        }
    }
}
