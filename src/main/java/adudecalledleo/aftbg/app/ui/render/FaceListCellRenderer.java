package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;

public final class FaceListCellRenderer extends BaseListCellRenderer<Face> {
    public enum Mode {
        GRID, LIST_SIMPLE, LIST_DETAILED
    }

    private final Mode mode;

    public FaceListCellRenderer(Mode mode) {
        super();
        this.mode = mode;
        Dimension size;
        if (mode == Mode.GRID) {
            size = new Dimension(72, 72);
        } else {
            size = new Dimension(72 * 4 + 4, 72);
        }
        setPreferredSize(size);
        setMinimumSize(size);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Face> list, Face value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        updateColors(list, index, isSelected, cellHasFocus);
        setIcon(value.getIcon());
        switch (mode) {
            case GRID -> {
                setText(null);
                setToolTipText(null);
            }
            case LIST_SIMPLE -> {
                setText(value.getName());

                var src = value.getSource();
                if (src == null) {
                    setToolTipText(null);
                } else {
                    setToolTipText("<html><b>From:</b> %s</html>".formatted(src.qualifiedName()));
                }
            }
            case LIST_DETAILED -> {
                setText("<html>%s<br><i>%s</i></html>".formatted(value.getName(), value.getImagePath()));
                setToolTipText(null);
            }
        }
        return this;
    }
}
