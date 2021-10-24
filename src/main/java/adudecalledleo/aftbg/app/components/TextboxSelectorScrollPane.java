package adudecalledleo.aftbg.app.components;

import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;

public final class TextboxSelectorScrollPane extends JScrollPane {
    public TextboxSelectorScrollPane(Component view, WindowContext winCtx) {
        super();
        setViewport(new ViewportImpl(winCtx));
        setViewportView(view);
    }

    private static final class ViewportImpl extends JViewport {
        private final WindowContext winCtx;

        public ViewportImpl(WindowContext winCtx) {
            this.winCtx = winCtx;
            setOpaque(false);
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
}
