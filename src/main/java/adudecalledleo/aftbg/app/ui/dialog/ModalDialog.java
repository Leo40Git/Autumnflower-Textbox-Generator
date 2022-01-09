package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;

import javax.swing.*;

public abstract class ModalDialog extends JDialog {
    public ModalDialog(Component owner) {
        super(getWindowAncestor(owner), DEFAULT_MODALITY_TYPE);
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
