package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;

import org.jetbrains.annotations.Nullable;

public abstract class DialogWithResult<T> extends ModalDialog implements WindowListener {
    protected T result;

    public DialogWithResult(Component owner) {
        super(owner);
        addWindowListener(this);
    }

    public @Nullable T showDialog() {
        setVisible(true);
        return this.result;
    }

    @Override
    public void windowOpened(WindowEvent e) { }

    @Override
    public void windowClosing(WindowEvent e) {
        this.result = null;
    }

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
