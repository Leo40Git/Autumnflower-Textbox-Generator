package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.FaceCategory;

public final class FaceCategoryListCellRenderer extends BaseListCellRenderer<FaceCategory> {
    public FaceCategoryListCellRenderer() {
        super();
        setPreferredSize(new Dimension(72 * 3 + 4, 72));
        setMinimumSize(new Dimension(72 * 3 + 4, 72));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends FaceCategory> list, FaceCategory value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        updateColors(list, index, isSelected, cellHasFocus);
        setText(value.getName());
        var icon = value.getIcon();
        if (icon == null) {
            setIcon(null);
        } else {
            setIcon(icon.getIcon());
        }
        return this;
    }
}
