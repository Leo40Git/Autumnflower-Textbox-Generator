package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

import javax.swing.*;
import javax.swing.text.*;

public class PlaceholderTextField extends JTextField {
    private static final Map<?, ?> DESKTOP_HINTS =
            (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");

    private String placeholder;

    public PlaceholderTextField() { }

    public PlaceholderTextField(String text) {
        super(text);
    }

    public PlaceholderTextField(int columns) {
        super(columns);
    }

    public PlaceholderTextField(String text, int columns) {
        super(text, columns);
    }

    public PlaceholderTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        if (!Objects.equals(this.placeholder, placeholder)) {
            String oldVal = this.placeholder;
            this.placeholder = placeholder;
            firePropertyChange("placeholder", oldVal, placeholder);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        if (DESKTOP_HINTS == null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            g2d.setRenderingHints(DESKTOP_HINTS);
        }
        g2d.setColor(getDisabledTextColor());
        g2d.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
    }
}
