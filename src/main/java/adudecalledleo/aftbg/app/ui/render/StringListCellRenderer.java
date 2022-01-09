package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

public final class StringListCellRenderer extends BaseListCellRenderer<String> {
    public StringListCellRenderer(int width) {
        int height = getPreferredSize().height * 2 + 10;
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        updateColors(list, index, isSelected, cellHasFocus);
        setText(value);
        return this;
    }
}
