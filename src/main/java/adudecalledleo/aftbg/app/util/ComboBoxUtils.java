package adudecalledleo.aftbg.app.util;

import javax.swing.*;

public final class ComboBoxUtils {
    private ComboBoxUtils() { }
    
    public static void replacePopup(JComboBox<?> comboBox, JPopupMenu newPopup) {
        final var popup = (JPopupMenu) comboBox.getAccessibleContext().getAccessibleChild(0);
        popup.addPropertyChangeListener("visible", evt -> {
            if (evt.getNewValue() == Boolean.TRUE) {
                popup.setVisible(false);
                newPopup.show(comboBox, 0, comboBox.getHeight() + comboBox.getInsets().bottom);
            } else if (evt.getNewValue() == Boolean.FALSE) {
                newPopup.setVisible(false);
            }
        });
    }
}
