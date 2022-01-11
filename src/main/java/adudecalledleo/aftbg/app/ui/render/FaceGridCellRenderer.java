package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;

public final class FaceGridCellRenderer extends BaseListCellRenderer<Face> {
    private static final Dimension SIZE = new Dimension(72, 72);

    public FaceGridCellRenderer() {
        super();
        setPreferredSize(SIZE);
        setMinimumSize(SIZE);
        setText(null);
        setToolTipText(null);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Face> list, Face value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return this;
        }

        updateColors(list, index, isSelected, cellHasFocus);
        setIcon(value.getIcon());
        return this;
    }
}
