package adudecalledleo.aftbg.app.components;

import adudecalledleo.aftbg.app.WindowContextUpdateListener;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;

public final class TextboxSelectorScrollPane extends JScrollPane implements WindowContextUpdateListener {
    private final ViewportImpl viewport;

    public TextboxSelectorScrollPane(Component view) {
        super();
        setViewport(viewport = new ViewportImpl());
        setViewportView(view);
    }

    @Override
    public void updateWindowContext(WindowContext winCtx) {
        viewport.winCtx = winCtx;
    }

    private static final class ViewportImpl extends JViewport {
        private WindowContext winCtx;

        public ViewportImpl() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (winCtx != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setBackground(ColorUtils.TRANSPARENT);
                g2d.clearRect(0, 0, getWidth(), getHeight());
                winCtx.drawBackground(g2d, 0, 0, getWidth(), getHeight(), null);
            }
            super.paintComponent(g);
        }
    }
}
