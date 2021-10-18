package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public final class WindowBorder {
    // modified "nine patch"
    // center/middle pieces are thinner, 32x24 for center-left/right and 24x32 for top/bottom-middle
    // (RPG Maker puts some menu-related arrows here, for some reason)
    // also, center-middle piece doesn't exist, so technically this is "eight patch"

    private static final int PIECE_TL = 0;
    private static final int PIECE_TM = 1;
    private static final int PIECE_TR = 2;
    private static final int PIECE_CL = 3;
    private static final int PIECE_CR = 4;
    private static final int PIECE_BL = 5;
    private static final int PIECE_BM = 6;
    private static final int PIECE_BR = 7;
    private static final int PIECE_MAX = 8;

    private final int pieceWidth = 32, pieceHeight = 32;
    private final int pieceWidthC = 24, pieceHeightM = 24;

    private final BufferedImage[] pieces = new BufferedImage[PIECE_MAX];

    public WindowBorder(BufferedImage window) {
        final int startX = 96;

        pieces[PIECE_TL] = window.getSubimage(startX, 0, pieceWidth, pieceHeight);
        pieces[PIECE_TM] = window.getSubimage(startX + pieceWidth, 0, pieceWidth, pieceHeightM);
        pieces[PIECE_TR] = window.getSubimage(startX + pieceWidth * 2, 0, pieceWidth, pieceHeight);

        pieces[PIECE_CL] = window.getSubimage(startX, pieceHeight, pieceWidthC, pieceHeight);
        pieces[PIECE_CR] = window.getSubimage(startX + pieceWidth * 2 + (pieceWidth - pieceWidthC), pieceHeight, pieceWidthC, pieceHeight);

        pieces[PIECE_BL] = window.getSubimage(startX, pieceHeight * 2, pieceWidth, pieceHeight);
        pieces[PIECE_BM] = window.getSubimage(startX + pieceWidth, pieceHeight * 2 + (pieceHeight - pieceHeightM), pieceWidth, pieceHeightM);
        pieces[PIECE_BR] = window.getSubimage(startX + pieceWidth * 2, pieceHeight * 2, pieceWidth, pieceHeight);
    }

    public void draw(Graphics g, int x, int y, int width, int height, ImageObserver observer) {
        // TOP
        g.drawImage(pieces[PIECE_TL], x, y, observer);
        g.drawImage(pieces[PIECE_TM], x + pieceWidth, y, width - pieceWidth * 2, pieceHeightM, observer);
        g.drawImage(pieces[PIECE_TR], x + width - pieceWidth, y, observer);
        // CENTER
        g.drawImage(pieces[PIECE_CL], x, y + pieceHeight, pieceWidthC, height - pieceHeight * 2, observer);
        g.drawImage(pieces[PIECE_CR], x + width - pieceWidthC, y + pieceHeight, pieceWidthC, height - pieceHeight * 2, observer);
        // BOTTOM
        g.drawImage(pieces[PIECE_BL], x, y + height - pieceHeight, observer);
        g.drawImage(pieces[PIECE_BM], x + pieceWidth, y + height - pieceHeightM, width - pieceWidth * 2, pieceHeightM, observer);
        g.drawImage(pieces[PIECE_BR], x + width - pieceWidth, y + height - pieceHeight, observer);
    }
}
