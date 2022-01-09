package adudecalledleo.aftbg.app.ui;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

public final class WindowBackgroundScrollPane extends JScrollPane implements GameDefinitionUpdateListener {
    private final ViewportImpl viewport;

    public WindowBackgroundScrollPane(Component view) {
        super();
        setViewport(viewport = new ViewportImpl(view));
    }

    @Override
    public void updateGameDefinition(GameDefinition gameDef) {
        viewport.winCtx = gameDef.winCtx().copy();
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
