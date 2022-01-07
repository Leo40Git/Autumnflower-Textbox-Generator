package adudecalledleo.aftbg.app.component;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

public class WindowBackgroundPanel extends JPanel {
    protected final WindowContext winCtx;

    public WindowBackgroundPanel(WindowContext winCtx) {
        this.winCtx = winCtx;
        setOpaque(true);
        setBackground(ColorUtils.TRANSPARENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setBackground(ColorUtils.TRANSPARENT);
        g2d.clearRect(0, 0, getWidth(), getHeight());
        winCtx.drawBackground(g2d, 0, 0, getWidth(), getHeight(), null);
    }
}
