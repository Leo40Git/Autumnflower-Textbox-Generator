package adudecalledleo.aftbg.app.ui.render;

import javax.swing.*;

import adudecalledleo.aftbg.app.util.ColorUtils;

public abstract class BaseListCellRenderer<T> extends JLabel implements ListCellRenderer<T> {
    protected BaseListCellRenderer() {
        super();
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    protected void updateColors(JList<?> list, int index, boolean isSelected, boolean hasFocus) {
        setEnabled(list.isEnabled());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            var bg = list.getBackground();
            if (index % 2 == 1) {
                bg = ColorUtils.darker(bg, 0.9);
            }
            setBackground(bg);
            setForeground(list.getForeground());
        }
    }
}
