package adudecalledleo.aftbg.app.render;

import adudecalledleo.aftbg.face.Face;

import javax.swing.*;
import java.awt.*;

public final class FaceListCellRenderer extends BaseListCellRenderer<Face> {
    public FaceListCellRenderer() {
        super();
        setPreferredSize(new Dimension(72 * 4 + 4, 72));
        setMinimumSize(new Dimension(72 * 4 + 4, 72));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Face> list, Face value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        updateColors(list, isSelected, cellHasFocus);
        setText(value.getName());
        setIcon(value.getIcon());
        return this;
    }
}
