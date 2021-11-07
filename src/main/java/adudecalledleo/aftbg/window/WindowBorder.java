package adudecalledleo.aftbg.window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public final class WindowBorder {
    // in essence, this is a modified "nine patch" renderer
    //  corner pieces are 24x24
    //  the center/middle pieces are longer: 24x48 for center-left/right pieces; and 48x24 for top/bottom-middle pieces
    // also, there's no center-middle piece (RPG Maker puts some menu-related arrows where it'd be, for some reason),
    //  so technically this is an "eight patch" renderer

    private static final int PIECE_TL = 0;
    private static final int PIECE_TM = 1;
    private static final int PIECE_TR = 2;
    private static final int PIECE_CL = 3;
    private static final int PIECE_CR = 4;
    private static final int PIECE_BL = 5;
    private static final int PIECE_BM = 6;
    private static final int PIECE_BR = 7;
    private static final int PIECE_MAX = 8;

    private final int pieceWidth = 24, pieceHeight = 24;
    private final int pieceWidthM = 48, pieceHeightC = 48;

    private final BufferedImage[] pieces = new BufferedImage[PIECE_MAX];

    public WindowBorder(BufferedImage window) {
        final int startX = 96;
        final int middlePadding = 48; // padding between PIECE_CL's end and PIECE_CR's start

        pieces[PIECE_TL] = window.getSubimage(startX, 0, pieceWidth, pieceHeight);
        pieces[PIECE_TM] = window.getSubimage(startX + pieceWidth, 0, pieceWidthM, pieceHeight);
        pieces[PIECE_TR] = window.getSubimage(startX + pieceWidth + pieceWidthM, 0, pieceWidth, pieceHeight);

        pieces[PIECE_CL] = window.getSubimage(startX, pieceHeight, pieceWidth, pieceHeightC);
        pieces[PIECE_CR] = window.getSubimage(startX + pieceWidth + middlePadding, pieceHeight, pieceWidth, pieceHeightC);

        pieces[PIECE_BL] = window.getSubimage(startX, pieceHeight + pieceHeightC, pieceWidth, pieceHeight);
        pieces[PIECE_BM] = window.getSubimage(startX + pieceWidth, pieceHeight + pieceHeightC, pieceWidthM, pieceHeight);
        pieces[PIECE_BR] = window.getSubimage(startX + pieceWidth + pieceWidthM, pieceHeight + pieceHeightC, pieceWidth, pieceHeight);
    }

    public void draw(Graphics g, int x, int y, int width, int height, ImageObserver observer) {
        // TOP
        g.drawImage(pieces[PIECE_TL], x, y, observer);
        g.drawImage(pieces[PIECE_TM], x + pieceWidth, y, width - pieceWidthM, pieceHeight, observer);
        g.drawImage(pieces[PIECE_TR], x + width - pieceWidth, y, observer);
        // CENTER
        g.drawImage(pieces[PIECE_CL], x, y + pieceHeight, pieceWidth, height - pieceHeightC, observer);
        g.drawImage(pieces[PIECE_CR], x + width - pieceWidth, y + pieceHeight, pieceWidth, height - pieceHeightC, observer);
        // BOTTOM
        g.drawImage(pieces[PIECE_BL], x, y + height - pieceHeight, observer);
        g.drawImage(pieces[PIECE_BM], x + pieceWidth, y + height - pieceHeight, width - pieceWidthM, pieceHeight, observer);
        g.drawImage(pieces[PIECE_BR], x + width - pieceWidth, y + height - pieceHeight, observer);
    }
}
