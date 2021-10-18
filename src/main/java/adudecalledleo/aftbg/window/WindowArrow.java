package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public final class WindowArrow {
    private final int frameWidth = 24, frameHeight = 24;

    private final BufferedImage[] frames = new BufferedImage[4];

    public WindowArrow(BufferedImage window) {
        final int startX = 144, startY = 96;

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                frames[y * 2 + x] = window.getSubimage(startX + (x * frameWidth), startY + (y * frameHeight), frameWidth, frameHeight);
            }
        }
    }

    public void draw(Graphics g, int x, int y, int width, int height, int frame, ImageObserver observer) {
        int fx = x + (width / 2) - (frameWidth / 2);
        int fy = y + height - frameHeight;

        g.drawImage(frames[frame], fx, fy, observer);
    }
}
