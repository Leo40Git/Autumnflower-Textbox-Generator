package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Composite for properly drawing the window background:<ul>
 *     <li>Tints the background using the supplied color.</li>
 *     <li>Reduces background alpha by 25%, since RPG Maker draws it at 75% alpha.</li>
 * </ul>
 */
final class WindowBackgroundComposite implements Composite {
    private final Context context;

    public WindowBackgroundComposite(WindowTint color) {
        context = new Context(color);
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return context;
    }

    private record Context(WindowTint color) implements CompositeContext {
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
