package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Composite for properly drawing the window background, including tinting with its color.
 * Based on <a href="https://github.com/rpgtkoolmv/corescript/blob/master/js/libs/pixi.js">PIXI's ColorMatrixFilter</a>
 * and <a href="https://github.com/rpgtkoolmv/corescript/blob/master/js/rpg_core/ToneFilter.js">rpg_core's ToneFilter</a>).
 */
final class WindowBackgroundComposite implements Composite {
    private final Context context;

    public WindowBackgroundComposite(WindowColor color) {
        context = new Context(color);
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return context;
    }

    private record Context(WindowColor color) implements CompositeContext {
        @Override
        public void dispose() { }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int w = Math.min(src.getWidth(), dstIn.getWidth());
            int h = Math.min(src.getHeight(), dstIn.getHeight());

            int[] dstRgba = new int[4];

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    dstIn.getPixel(x + dstIn.getMinX(), y + dstIn.getMinY(), dstRgba);
                    dstRgba[0] = Math.min(255, Math.max(0, dstRgba[0] + color.red()));
                    dstRgba[1] = Math.min(255, Math.max(0, dstRgba[1] + color.green()));
                    dstRgba[2] = Math.min(255, Math.max(0, dstRgba[2] + color.blue()));
                    dstRgba[3] *= 0.75; // RPG Maker draws the BG at 75% alpha
                    dstOut.setPixel(x + dstOut.getMinX(), y + dstOut.getMinY(), dstRgba);
                }
            }
        }
    }
}
