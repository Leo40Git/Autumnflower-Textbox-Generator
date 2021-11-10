package adudecalledleo.aftbg.app.component;

import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;

public class WindowBackgroundPanel extends JPanel {
    private final WindowContext winCtx;

    public WindowBackgroundPanel(WindowContext winCtx) {
        this.winCtx = winCtx;
        setOpaque(true);
        setBackground(ColorUtils.TRANSPARENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setBackground(ColorUtils.TRANSPARENT);
        g2d.clearRect(0, 0, getWidth(), getHeight());
        winCtx.drawBackground(g2d, 0, 0, getWidth(), getHeight(), null);
        super.paintComponent(g);
    }
}