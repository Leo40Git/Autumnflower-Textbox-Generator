package adudecalledleo.aftbg.window;

import adudecalledleo.aftbg.util.AlphaMultiplicationComposite;

import java.awt.*;
import java.awt.image.*;

/**
 * <b>NOTE:</b> This class is <em>not safe</em> for multithreading.
 *
 * <p>If you need to render the window background on multiple threads, use the {@link #copy()} method to get a copy to
 * pass to another thread.
 */
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

    private WindowBackground(BufferedImage base, BufferedImage overlay, WindowTint color, BufferedImage scratchBuf) {
        this.base = base;
        this.overlay = overlay;
        this.color = color;
        this.scratchBuf = scratchBuf;
    }

    public WindowBackground copy() {
        BufferedImage scratchBufCopy = null;
        if (scratchBuf != null) {
            ColorModel cm = scratchBuf.getColorModel();
            boolean isAlphaPremul = scratchBuf.isAlphaPremultiplied();
            WritableRaster raster = scratchBuf.copyData(scratchBuf.getRaster().createCompatibleWritableRaster());
            scratchBufCopy = new BufferedImage(cm, raster, isAlphaPremul, null);
        }
        return new WindowBackground(base, overlay, color, scratchBufCopy);
    }

    public void draw(Graphics2D g, int x, int y, int width, int height, ImageObserver observer) {
        if (resizeScratchBuf(width, height)) {
            Graphics2D sg = scratchBuf.createGraphics();

            // draw stretched and tinted base
            sg.setComposite(new WindowTintComposite(color));
            sg.drawImage(base, 0, 0, width, height, 0, 0, base.getWidth(), base.getHeight(), null);
            // draw tiled overlay
            sg.setComposite(AlphaComposite.SrcOver);
            final int tilesWide = width / tileWidth, tilesHigh = height / tileHeight;
            for (int ty = 0; ty <= tilesHigh; ty++) {
                for (int tx = 0; tx <= tilesWide; tx++) {
                    g.drawImage(overlay, tx * tileWidth, ty * tileHeight, null);
                }
            }
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
}
