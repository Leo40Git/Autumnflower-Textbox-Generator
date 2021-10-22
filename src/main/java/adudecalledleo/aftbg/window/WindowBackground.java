package adudecalledleo.aftbg.window;

import adudecalledleo.aftbg.util.AlphaMultiplicationComposite;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public final class WindowBackground {
    // there are 2 separate layers for this in the Window sheet: a "base" one and an "overlay" one
    // the "base" overlay is stretched into the correct width and height and tinted by the editor-specified window tint,
    //  while the "overlay" is tiled
    // RPG Maker also renders both layers together at 75% opacity

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

            var oldComposite = sg.getComposite();
            // draw stretched and tinted base
            sg.setComposite(new WindowTintComposite(color));
            sg.drawImage(base, 0, 0, width, height, 0, 0, base.getWidth(), base.getHeight(), null);
            sg.setComposite(oldComposite);
            // draw tiled overlay
            drawTiled(overlay, sg, width, height);

            // reduce everyone's alpha by 25%
            sg.setComposite(new AlphaMultiplicationComposite(0.75f));
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

    private void drawTiled(BufferedImage part, Graphics g, int width, int height) {
        final int tilesWide = width / tileWidth, tilesHigh = height / tileHeight;
        for (int ty = 0; ty <= tilesHigh; ty++) {
            for (int tx = 0; tx <= tilesWide; tx++) {
                g.drawImage(part, tx * tileWidth, ty * tileHeight, null);
            }
        }
    }
}
