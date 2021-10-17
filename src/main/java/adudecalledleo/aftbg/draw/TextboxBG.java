package adudecalledleo.aftbg.draw;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public final class TextboxBG {
    // BG is made up of a base and an overlay
    // both are tiled

    private final BufferedImage base, overlay;
    private final int tileWidth = 96, tileHeight = 96;

    public TextboxBG(BufferedImage window) {
        base = window.getSubimage(0, 0, tileWidth, tileHeight);
        overlay = window.getSubimage(0, tileHeight, tileWidth, tileHeight);
    }

    public void draw(Graphics g, int x, int y, int width, int height, ImageObserver observer) {
        draw0(base, g, x, y, width, height, observer);
        draw0(overlay, g, x, y, width, height, observer);
    }

    private void draw0(BufferedImage part, Graphics g, int x, int y, int width, int height, ImageObserver observer) {
        var oldClip = g.getClip();
        g.setClip(x, y, width, height);

        // FIXME colors are off compared to the real deal.
        //  color blending? simple tinting? who knows?

        final int tilesWide = width / tileWidth, tilesHigh = height / tileHeight;
        for (int ty = 0; ty <= tilesHigh; ty++) {
            for (int tx = 0; tx <= tilesWide; tx++) {
                g.drawImage(part, x + (tx * tileWidth), y + (ty * tileHeight), observer);
            }
        }

        g.setClip(oldClip);
    }
}
