package adudecalledleo.aftbg.app.ui.dialog.modifier;

import java.awt.*;
import java.awt.event.*;

import adudecalledleo.aftbg.app.ui.dialog.ModalDialog;

public abstract class ModifierDialog extends ModalDialog implements WindowListener {
    public ModifierDialog(Component owner) {
        super(owner);
        addWindowListener(this);
    }

    @Override
    public void windowOpened(WindowEvent e) { }

    @Override
    public void windowClosing(WindowEvent e) { }

    @Override
    public void windowClosed(WindowEvent e) { }

    @Override
    public void windowIconified(WindowEvent e) { }

    @Override
    public void windowDeiconified(WindowEvent e) { }

    @Override
    public void windowActivated(WindowEvent e) { }

    @Override
    public void windowDeactivated(WindowEvent e) { }
}
