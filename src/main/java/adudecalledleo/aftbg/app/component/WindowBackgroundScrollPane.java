package adudecalledleo.aftbg.app.component;

import adudecalledleo.aftbg.app.util.WindowContextUpdateListener;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;

public final class WindowBackgroundScrollPane extends JScrollPane implements WindowContextUpdateListener {
    private final ViewportImpl viewport;

    public WindowBackgroundScrollPane(Component view) {
        super();
        setViewport(viewport = new ViewportImpl(view));
    }

    @Override
    public void updateWindowContext(WindowContext winCtx) {
        viewport.winCtx = winCtx;
    }

    private static final class ViewportImpl extends JViewport {
        private WindowContext winCtx;

        public ViewportImpl(Component view) {
            super();
            setOpaque(false);
            setView(view);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (winCtx != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setBackground(ColorUtils.TRANSPARENT);
                g2d.clearRect(0, 0, getWidth(), getHeight());
                winCtx.drawBackground(g2d, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }
}
