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

    private static final class Context implements CompositeContext {
        private final double[][] matrix;

        private Context(WindowColor color) {
            matrix = new double[][] {
                    new double[] { 1, 0, 0, color.red() / 255.0 },
                    new double[] { 0, 1, 0, color.green() / 255.0 },
                    new double[] { 0, 0, 1, color.blue() / 255.0 },
                    new double[] { 0, 0, 0, 192 / 255.0 } // NOTE: RPG Maker draws window BG at 75% alpha,
                                                          //  so we replicate that here
            };
            System.out.println("TONE MATRIX: {");
            for (int r = 0; r < 4; r++) {
                System.out.print("  ");
                for (int c = 0; c < 4; c++) {
                    System.out.print(matrix[r][c]);
                    if (c < 3)
                        System.out.print(", ");
                }
                if (r == 3)
                    System.out.println();
                else
                    System.out.println(",");
            }
            System.out.println("}");
        }

        @Override
        public void dispose() { }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int w = Math.min(src.getWidth(), dstIn.getWidth());
            int h = Math.min(src.getHeight(), dstIn.getHeight());

            int[] dstRgba = new int[4];
            double[] scratchVec = new double[4];
            double[] resultVec = new double[4];

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    dstIn.getPixel(x + dstIn.getMinX(), y + dstIn.getMinY(), dstRgba);
                    for (int i = 0; i < 4; i++) {
                        scratchVec[i] = ((double) dstRgba[i]) / 255.0;
                    }
                    for (int r = 0; r < 4; r++) {
                        double sum = 0;
                        for (int c = 0; c < 4; c++) {
                            sum += matrix[r][c] * scratchVec[c];
                        }
                        resultVec[r] = sum;
                    }
                    for (int i = 0; i < 4; i++) {
                        dstRgba[i] = Math.min(255, Math.max(0, (int) (resultVec[i] * 255)));
                    }
                    dstOut.setPixel(x + dstOut.getMinX(), y + dstOut.getMinY(), dstRgba);
                }
            }
        }
    }
}
