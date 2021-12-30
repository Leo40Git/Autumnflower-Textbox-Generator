package adudecalledleo.aftbg.app.util;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import adudecalledleo.aftbg.BuildInfo;

public final class LoadFrame extends JFrame {
    private static final class AnimHandler implements ActionListener {
        private static final String[] FRAMES = { "\\", "|", "/", "-" };

        private final JLabel label;
        private final Timer timer;
        private int frame;

        private AnimHandler(JLabel label) {
            this.label = label;
            timer = new Timer(250, this);
            frame = 0;
            updateLabel();
        }

        public void start() {
            timer.start();
        }

        public void stop() {
            timer.stop();
        }

        private void updateLabel() {
            label.setText(FRAMES[frame]);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            frame++;
            frame %= FRAMES.length;
            updateLabel();
        }
    }

    private static final List<LoadFrame> ACTIVE_FRAMES_INTERNAL = new ArrayList<>();
    public static final List<LoadFrame> ACTIVE_FRAMES = Collections.unmodifiableList(ACTIVE_FRAMES_INTERNAL);

    private final JLabel loadLabel;
    private final AnimHandler animHandler;

    public LoadFrame(String loadString, boolean important) {
        setDefaultCloseOperation(important ? JFrame.EXIT_ON_CLOSE : WindowConstants.DO_NOTHING_ON_CLOSE);
        setAlwaysOnTop(true);
        setUndecorated(true);
        final Dimension size = new Dimension(320, 120);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        setResizable(false);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        loadLabel = new JLabel();
        setLoadString(loadString);
        loadLabel.setFont(loadLabel.getFont().deriveFont(Font.BOLD, 24));
        loadLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadLabel.setVerticalAlignment(SwingConstants.CENTER);
        JLabel animLabel = new JLabel();
        animLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        animLabel.setHorizontalAlignment(SwingConstants.CENTER);
        animLabel.setVerticalAlignment(SwingConstants.CENTER);
        JLabel brandLabel = new JLabel(BuildInfo.name() + " v" + BuildInfo.version());
        brandLabel.setFont(brandLabel.getFont().deriveFont(Font.PLAIN, 12));
        Color brandFg = brandLabel.getForeground();
        brandLabel.setForeground(new Color(brandFg.getRed(), brandFg.getGreen(), brandFg.getBlue(), 158));
        brandLabel.setHorizontalAlignment(SwingConstants.CENTER);
        brandLabel.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(loadLabel, BorderLayout.PAGE_START);
        panel.add(animLabel, BorderLayout.CENTER);
        panel.add(brandLabel, BorderLayout.PAGE_END);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        animHandler = new AnimHandler(animLabel);
        animHandler.start();
        setVisible(true);
        requestFocus();
        ACTIVE_FRAMES_INTERNAL.add(this);
    }

    public void setLoadString(String loadString) {
        setTitle(loadString + " - " + BuildInfo.name());
        loadLabel.setText(loadString);
    }

    @Override
    public void dispose() {
        super.dispose();
        animHandler.stop();
        ACTIVE_FRAMES_INTERNAL.remove(this);
    }
}
