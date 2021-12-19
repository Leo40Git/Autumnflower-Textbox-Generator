package adudecalledleo.aftbg.app.dialog;

import java.awt.*;

import javax.swing.*;

public abstract class ModalDialog extends JDialog {
    public ModalDialog(Component owner) {
        super(getWindowAncestor(owner), ModalityType.APPLICATION_MODAL);
    }

    private static Window getWindowAncestor(Component c) {
        if (c instanceof Window w) {
            return w;
        } else if (c == null) {
            return null;
        } else {
            return SwingUtilities.getWindowAncestor(c);
        }
    }
}
