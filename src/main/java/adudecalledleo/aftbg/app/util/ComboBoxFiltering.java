package adudecalledleo.aftbg.app.util;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ComboBoxFiltering<E> extends KeyAdapter implements PropertyChangeListener, ListDataListener {
    public static <E> void install(JComboBox<E> comboBox) {
        new ComboBoxFiltering<>(comboBox);
    }

    private final JComboBox<E> comboBox;
    private final JTextComponent editor;

    private final DefaultComboBoxModel<E> filteredModel;
    private ComboBoxModel<E> originalModel;

    private ComboBoxFiltering(JComboBox<E> comboBox) {
        this.comboBox = comboBox;

        originalModel = comboBox.getModel();
        originalModel.addListDataListener(this);
        comboBox.addPropertyChangeListener("model", this);
        comboBox.setModel(filteredModel = new DefaultComboBoxModel<>());
        comboBox.setEditable(true);
        editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        editor.addKeyListener(this);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        SwingUtilities.invokeLater(() -> comboFilter(editor.getText()));
    }

    private void comboFilter(String enteredText) {
        if (!comboBox.isPopupVisible()) {
            comboBox.showPopup();
        }

        List<E> filteredArray = new ArrayList<>();
        for (int i = 0, n = originalModel.getSize(); i < n; i++) {
            var item = originalModel.getElementAt(i);
            if (item == null) {
                continue;
            }
            if (item.toString().toLowerCase(Locale.ROOT).contains(enteredText.toLowerCase(Locale.ROOT))) {
                filteredArray.add(item);
            }
        }

        if (filteredArray.size() > 0) {
            filteredModel.removeAllElements();
            filteredModel.addAll(filteredArray);
            editor.setText(enteredText);
            comboBox.hidePopup();
            comboBox.showPopup();
        }
    }

    private void resetFilter() {
        filteredModel.removeAllElements();
        for (int i = 0, n = originalModel.getSize(); i < n; i++) {
            filteredModel.addElement(originalModel.getElementAt(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("model".equals(evt.getPropertyName())) {
            var model = (ComboBoxModel<E>) evt.getNewValue();
            if (model != filteredModel) {
                originalModel = model;
                resetFilter();
                comboBox.setModel(filteredModel);
            }
        }
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        resetFilter();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        resetFilter();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        resetFilter();
    }
}
