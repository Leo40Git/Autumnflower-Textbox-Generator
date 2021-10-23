package adudecalledleo.aftbg.app.render;

import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FaceCategory;

import javax.swing.*;
import java.awt.*;

public final class FaceCBRenderer extends BaseCBRenderer<Face> {
    public FaceCBRenderer() {
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
