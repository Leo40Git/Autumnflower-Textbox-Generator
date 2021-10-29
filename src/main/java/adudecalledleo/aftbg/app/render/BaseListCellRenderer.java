package adudecalledleo.aftbg.app.render;

import javax.swing.*;

public abstract class BaseListCellRenderer<T> extends JLabel implements ListCellRenderer<T> {
    protected BaseListCellRenderer() {
        super();
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    protected void updateColors(JList<?> list, boolean isSelected, boolean hasFocus) {
        setEnabled(list.isEnabled());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
    }
}
