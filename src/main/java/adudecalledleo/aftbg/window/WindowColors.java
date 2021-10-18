package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class WindowColors {
    private final Color[] colors = new Color[32];

    public WindowColors(BufferedImage window) {
        final int colorWidth = 12, colorHeight = 12;
        final int startX = 96, startY = 144;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 8; x++) {
                colors[y * 8 + x] = new Color(window.getRGB(startX + (x * colorWidth), startY + (y * colorHeight)), false);
            }
        }
    }

    public int count() {
        return colors.length;
    }

    public Color get(int i) {
        return colors[i];
    }
}
