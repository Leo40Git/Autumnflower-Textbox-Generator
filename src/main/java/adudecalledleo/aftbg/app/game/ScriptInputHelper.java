package adudecalledleo.aftbg.app.game;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public final class ScriptInputHelper {
    private static final String DIALOG_TITLE = "Script Input";

    private ScriptInputHelper() { }

    private static JDialog createDialog(JOptionPane pane) {
        JDialog dialog = new JDialog((Frame) null, DIALOG_TITLE, true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        return dialog;
    }

    public static String getString(String message, String def) {
        return (String) JOptionPane.showInputDialog(null, message, DIALOG_TITLE,
                JOptionPane.INFORMATION_MESSAGE, null, null, def);
    }

    public static String getString(String message) {
        return getString(message, "");
    }

    public static Integer getInt(String message, int def) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
                null, null, null);
        pane.setWantsInput(true);
        pane.setInitialSelectionValue(def);
        pane.selectInitialValue();
        JDialog dialog = createDialog(pane);

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
                                    DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
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

        dialog.setVisible(true);
        return resultBuffer[0];
    }

    public static Integer getInt(String message) {
        return getInt(message, 0);
    }

    public static Boolean getBoolean(String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = createDialog(pane);

        Boolean[] resultBuffer = new Boolean[] { null };

        pane.addPropertyChangeListener(evt -> {
            if (dialog.isVisible() && evt.getSource() == pane
                    && JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName())) {
                Object value = pane.getValue();
                if (value instanceof Integer anInt) {
                    if (anInt == JOptionPane.YES_OPTION) {
                        resultBuffer[0] = true;
                    } else {
                        resultBuffer[0] = false;
                    }
                    dialog.setVisible(false);
                    dialog.dispose();
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
        
        dialog.setVisible(true);
        return resultBuffer[0];
    }
}
