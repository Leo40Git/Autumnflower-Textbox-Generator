package adudecalledleo.aftbg.app.util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class ComboBoxUtils {
    private ComboBoxUtils() { }

    public static <T> void setupGridSelectionPopup(JComboBox<T> box, ListCellRenderer<T> renderer, T prototypeCellValue,
                                                   int columns, int maxRows) {
        BasicComboPopup popup = (BasicComboPopup) box.getAccessibleContext().getAccessibleChild(0);
        JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
        @SuppressWarnings("unchecked") JList<T> list = (JList<T>) scrollPane.getViewport().getView();

        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(0);
        list.setCellRenderer(renderer);
        list.setPrototypeCellValue(prototypeCellValue);

        popup.addPropertyChangeListener("visible",
                new GridPopupResizer<>(box, popup, scrollPane, list, columns, maxRows));
    }

    // resizes the popup so it actually fits the grid
    private record GridPopupResizer<T>(JComboBox<T> box, BasicComboPopup popup,
                                       JScrollPane scrollPane, JList<T> list, int columns,
                                       int maxRows) implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == popup && "visible".equals(evt.getPropertyName())
                    && evt.getNewValue() == Boolean.TRUE) {
                var cellSize = list.getCellRenderer()
                        .getListCellRendererComponent(list, list.getPrototypeCellValue(),
                                0, false, false)
                        .getPreferredSize();

                var size = new Dimension(cellSize.width * columns + scrollPane.getVerticalScrollBar().getWidth(),
                        0);

                // don't ask me why these extra 2 pixels are needed, but this seems to solve rows sometimes breaking
                //  one column before they should
                size.width += 2;

                // calculate height
                int modelSize = Math.min(columns * maxRows, list.getModel().getSize());
                size.height = cellSize.height * (modelSize / columns);
                if (modelSize % columns > 0) {
                    size.height += cellSize.height;
                }

                scrollPane.setPreferredSize(size);
                scrollPane.setMinimumSize(size);

                Component parent = popup.getParent();
                parent.setSize(size);

                parent.validate();
                parent.repaint();

                Window mainFrame = SwingUtilities.windowForComponent(box);
                Window popupWindow = SwingUtilities.windowForComponent(popup);

                if (popupWindow != mainFrame) {
                    // for heavyweight popups, you need to pack the window
                    popupWindow.pack();
                }
            }
        }
    }
}
