package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;

public final class FaceListCellRenderer extends BaseListCellRenderer<Face> {
    private static final Dimension SIZE = new Dimension(72 * 4 + 4, 72);

    private boolean showImagePath;

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
            setToolTipText(null);
        } else {
            setText(value.getName());

            var src = value.getSource();
            if (src == null) {
                setToolTipText(null);
            } else {
                setToolTipText("<html><b>From:</b> %s</html>".formatted(src.qualifiedName()));
            }
        }
        return this;
    }
}
