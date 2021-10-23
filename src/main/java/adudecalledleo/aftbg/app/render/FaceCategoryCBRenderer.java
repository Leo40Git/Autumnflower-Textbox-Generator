package adudecalledleo.aftbg.app.render;

import adudecalledleo.aftbg.face.FaceCategory;

import javax.swing.*;
import java.awt.*;

public final class FaceCategoryCBRenderer extends BaseCBRenderer<FaceCategory> {
    @Override
    public Component getListCellRendererComponent(JList<? extends FaceCategory> list, FaceCategory value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        updateColors(list, isSelected, cellHasFocus);
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
