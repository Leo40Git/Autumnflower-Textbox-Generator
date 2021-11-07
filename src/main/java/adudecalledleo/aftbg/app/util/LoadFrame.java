package adudecalledleo.aftbg.app.util;

import adudecalledleo.aftbg.Bootstrap;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public final class LoadFrame extends JFrame {
    private final JLabel loadLabel;

    public LoadFrame(String loadString, boolean important) {
        setDefaultCloseOperation(important ? JFrame.EXIT_ON_CLOSE : WindowConstants.DO_NOTHING_ON_CLOSE);
        setAlwaysOnTop(important);
        setUndecorated(true);
        final Dimension size = new Dimension(240, 80);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        setResizable(false);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        loadLabel = new JLabel(loadString);
        loadLabel.setFont(loadLabel.getFont().deriveFont(Font.BOLD, 20));
        loadLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadLabel.setVerticalAlignment(SwingConstants.CENTER);
        JLabel brandLabel = new JLabel(Bootstrap.NAME + " v" + Bootstrap.VERSION);
        brandLabel.setFont(brandLabel.getFont().deriveFont(Font.PLAIN, 10));
        Color brandFg = brandLabel.getForeground();
        brandLabel.setForeground(new Color(brandFg.getRed(), brandFg.getGreen(), brandFg.getBlue(), 158));
        brandLabel.setHorizontalAlignment(SwingConstants.CENTER);
        brandLabel.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(loadLabel, BorderLayout.CENTER);
        panel.add(brandLabel, BorderLayout.PAGE_END);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        requestFocus();
    }

    public void setLoadString(String loadString) {
        loadLabel.setText(loadString);
    }
}
