package adudecalledleo.aftbg.app.script;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.ui.util.DialogUtils;
import org.graalvm.polyglot.HostAccess;

public final class ScriptInputHelper {
    static final ScriptInputHelper INSTANCE = new ScriptInputHelper();

    private static final String DIALOG_TITLE = "Script Input";

    private ScriptInputHelper() { }

    @HostAccess.Export
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
    }

    private JDialog createDialog(JOptionPane pane) {
        JDialog dialog = new JDialog((Frame) null, DIALOG_TITLE, true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        return dialog;
    }

    @HostAccess.Export
    public String getString(String message, String def) {
        return (String) JOptionPane.showInputDialog(null, message, DIALOG_TITLE,
                JOptionPane.PLAIN_MESSAGE, null, null, def);
    }

    @HostAccess.Export
    public String getString(String message) {
        return getString(message, "");
    }

    @HostAccess.Export
    public String getMultilineString(String message, String def) {
        return DialogUtils.showMultilineInputDialog(null, message, DIALOG_TITLE,
                JOptionPane.PLAIN_MESSAGE, def);
    }

    @HostAccess.Export
    public String getMultilineString(String message) {
        return getMultilineString(message, null);
    }

    @HostAccess.Export
    public Integer getInt(String message, int def) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
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

    @HostAccess.Export
    public Integer getInt(String message) {
        return getInt(message, 0);
    }

    @HostAccess.Export
    public Boolean getBoolean(String message) {
        return switch (JOptionPane.showConfirmDialog(null, message, DIALOG_TITLE, JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE)) {
            case JOptionPane.YES_OPTION -> Boolean.TRUE;
            case JOptionPane.NO_OPTION -> Boolean.FALSE;
            default -> null;
        };
    }
}
