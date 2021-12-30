package adudecalledleo.aftbg.app;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;

public final class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String[] OPTIONS = { "Continue", "Abort" };
    private static final int OPTION_CONTINUE = 0;
    private static final int OPTION_ABORT = 1;

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.error("Uncaught exception in thread \"" + t.getName() + "\"", e);
        List<Boolean> oldAOTValues = new ArrayList<>();
        for (var frame : LoadFrame.ACTIVE_FRAMES) {
            oldAOTValues.add(frame.isAlwaysOnTop());
            frame.setAlwaysOnTop(false);
        }
        int option = DialogUtils.showCustomConfirmDialog(null,
                "An uncaught exception has occurred!\n" + DialogUtils.logFileInstruction(),
                "Uncaught Exception!", OPTIONS, JOptionPane.ERROR_MESSAGE);
        switch (option) {
        default:
        case OPTION_CONTINUE:
            for (int i = 0, size = LoadFrame.ACTIVE_FRAMES.size(); i < size; i++) {
                LoadFrame.ACTIVE_FRAMES.get(i).setAlwaysOnTop(oldAOTValues.get(i));
            }
            break;
        case OPTION_ABORT:
            System.exit(1);
        }
    }
}
