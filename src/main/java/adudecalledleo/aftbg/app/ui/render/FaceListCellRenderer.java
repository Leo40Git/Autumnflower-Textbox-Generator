package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;

public final class FaceListCellRenderer extends BaseListCellRenderer<Face> {
    static final Dimension SIZE = new Dimension(72 * 4 + 4, 72);

    private final boolean showImagePath;

    public FaceListCellRenderer(boolean showImagePath) {
        super();
        this.showImagePath = showImagePath;
        setPreferredSize(SIZE);
        setMinimumSize(SIZE);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Face> list, Face value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return this;
        }

        updateColors(list, index, isSelected, cellHasFocus);
        setIcon(value.getIcon());
        if (showImagePath) {
            setText("<html>%s<br><i>%s</i></html>".formatted(value.getName(), value.getImagePath()));
            String tooltip = value.createCommentToolTip();
            if (tooltip.isEmpty()) {
                setToolTipText(null);
            } else {
                setToolTipText("<html>" + tooltip + "</html>");
            }
        } else {
            setText(value.getName());
            setToolTipText("<html>" + value.toToolTipText(false) + "</html>");
        }
        return this;
    }
}
