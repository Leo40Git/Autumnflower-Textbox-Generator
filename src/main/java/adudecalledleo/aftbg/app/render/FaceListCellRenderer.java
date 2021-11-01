package adudecalledleo.aftbg.app.render;

import adudecalledleo.aftbg.face.Face;

import javax.swing.*;
import java.awt.*;

public final class FaceListCellRenderer extends BaseListCellRenderer<Face> {
    private final boolean showImagePath;

    public FaceListCellRenderer(boolean showImagePath) {
        super();
        this.showImagePath = showImagePath;
        setPreferredSize(new Dimension(72 * 4 + 4, 72));
        setMinimumSize(new Dimension(72 * 4 + 4, 72));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Face> list, Face value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        updateColors(list, index, isSelected, cellHasFocus);
        setIcon(value.getIcon());
        if (showImagePath) {
            setText("<html>"
                    + value.getName() + "<br>"
                    + "<i>" + value.getImagePath() + "</i>"
                    + "</html>");
        } else {
            setText(value.getName());
        }
        return this;
    }
}
