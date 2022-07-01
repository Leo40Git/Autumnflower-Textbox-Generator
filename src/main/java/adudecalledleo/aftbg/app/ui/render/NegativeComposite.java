package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;
import java.awt.image.*;

public final class NegativeComposite implements Composite {
    public static final NegativeComposite INSTANCE = new NegativeComposite();

    private final Context context;

    private NegativeComposite() {
        context = new Context();
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return context;
    }

    private static final class Context implements CompositeContext {
        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int w = Math.min(src.getWidth(), Math.min(dstIn.getWidth(), dstOut.getWidth()));
            int h = Math.min(src.getHeight(), Math.min(dstIn.getWidth(), dstOut.getHeight()));

            int[] srcRgba = new int[4];
            int[] dstRgba = new int[4];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    src.getPixel(src.getMinX() + x, src.getMinY() + y, srcRgba);
                    if (srcRgba[3] == 0) {
                        continue;
                    }

                    dstIn.getPixel(dstIn.getMinX() + x, dstIn.getMinY() + y, dstRgba);

                    dstRgba[0] = 255 - dstRgba[0]; // R
                    dstRgba[1] = 255 - dstRgba[1]; // G
                    dstRgba[2] = 255 - dstRgba[2]; // B
                    // alpha ignored

                    dstOut.setPixel(dstOut.getMinX() + x, dstOut.getMinY() + y, dstRgba);
                }
            }
        }

        @Override
        public void dispose() { }
    }
}
