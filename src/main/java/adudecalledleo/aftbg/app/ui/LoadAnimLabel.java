package adudecalledleo.aftbg.app.ui;

import java.awt.event.*;

import javax.swing.*;

public final class LoadAnimLabel extends JLabel {
    private static final String[] FRAMES = { "\\", "|", "/", "-" };

    private final Timer timer;
    private int frame;

    public LoadAnimLabel() {
        super(FRAMES[0]);
        addHierarchyListener(this::stopAnimatingWhenHidden);
        timer = new Timer(250, this::advanceFrame);
        frame = 0;
    }

    public void startAnimating() {
        SwingUtilities.invokeLater(() -> {
            frame = 0;
            updateFrame();
            timer.start();
        });
    }

    public void stopAnimating() {
        timer.stop();
        SwingUtilities.invokeLater(() -> {
            frame = 0;
            updateFrame();
        });
    }

    private void advanceFrame(ActionEvent e) {
        frame = (frame + 1) % FRAMES.length;
        SwingUtilities.invokeLater(this::updateFrame);
    }

    private void updateFrame() {
        setText(FRAMES[frame]);
    }

    private void stopAnimatingWhenHidden(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            if (!isShowing()) {
                stopAnimating();
            }
        }
    }
}
