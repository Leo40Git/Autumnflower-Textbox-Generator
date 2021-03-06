package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.*;

public final class WindowPalette {
    // this is simple: there are 32 colored squares on the Window sheet,
    // these directly map to the available 32 preset colors
    // (according to RPG Maker MV's source code, it only samples the first pixel of each square.
    //  because that's the sane thing to do.)

    public static final int COUNT = 32;

    private final Color[] colors = new Color[COUNT];

    public WindowPalette(BufferedImage window) {
        final int colorWidth = 12, colorHeight = 12;
        final int startX = 96, startY = 144;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 8; x++) {
                colors[y * 8 + x] = new Color(window.getRGB(startX + (x * colorWidth), startY + (y * colorHeight)), false);
            }
        }
    }

    public Color get(int index) {
        return colors[index];
    }
}
