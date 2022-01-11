package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;

public final class FaceGridCellRenderer extends BaseListCellRenderer<Face> {
    private static final Dimension SIZE = new Dimension(72, 72);
    static final Color SELECTED_FACE_BACKGROUND = new Color(0, 175, 0);

    private Face selectedFace;

    public FaceGridCellRenderer() {
        super();
        setPreferredSize(SIZE);
        setMinimumSize(SIZE);
        setText(null);
        setToolTipText(null);
    }

    public Face getSelectedFace() {
        return selectedFace;
    }

    public void setSelectedFace(Face selectedFace) {
        this.selectedFace = selectedFace;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Face> list, Face value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return this;
        }

        updateColors(list, index, isSelected, cellHasFocus);
        if (!isSelected && selectedFace == value) {
            setBackground(SELECTED_FACE_BACKGROUND);
        }
        setIcon(value.getIcon());
        return this;
    }
}
