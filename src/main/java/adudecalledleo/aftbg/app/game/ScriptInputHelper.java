package adudecalledleo.aftbg.app.game;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public final class ScriptInputHelper {
    private ScriptInputHelper() { }

    public static Integer getInt(String message, int def) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
                null, null, null);
        pane.setWantsInput(true);
        pane.setInitialSelectionValue(def);
        pane.selectInitialValue();
        JDialog dialog = new JDialog((Frame) null, "Script Input", true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setResizable(false);

        Integer[] resultBuffer = new Integer[] { null };

        pane.addPropertyChangeListener(evt -> {
            String prop = evt.getPropertyName();
            if (dialog.isVisible() && evt.getSource() == pane) {
                if (JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)) {
                    if (pane.getInputValue() instanceof String in) {
                        try {
                            resultBuffer[0] = Integer.parseInt(in);
                            dialog.setVisible(false);
                            dialog.dispose();
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Please enter a number!",
                                    "Script Input", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else if (JOptionPane.VALUE_PROPERTY.equals(prop)) {
                    if (pane.getValue() instanceof Integer anInt && anInt == JOptionPane.CANCEL_OPTION) {
                        resultBuffer[0] = null;
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                }
            }
        });
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resultBuffer[0] = null;
                dialog.setVisible(false);
                dialog.dispose();
            }
        });

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return resultBuffer[0];
    }

    public static Integer getInt(String message) {
        return getInt(message, 0);
    }
}
