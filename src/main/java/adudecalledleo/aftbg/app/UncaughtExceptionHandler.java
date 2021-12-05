package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.logging.Logger;

import javax.swing.*;

public final class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String[] OPTIONS = { "Continue", "Abort" };
    private static final int OPTION_CONTINUE = 0;
    private static final int OPTION_ABORT = 1;

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.error("Uncaught exception in thread \"" + t.getName() + "\"", e);
        int option = DialogUtils.showCustomConfirmDialog(null,
                "An uncaught exception has occurred!\nSee \"" + Main.LOG_NAME + "\" for more details.",
                "Uncaught Exception!", OPTIONS, JOptionPane.ERROR_MESSAGE);
        switch (option) {
        default:
        case OPTION_CONTINUE:
            break;
        case OPTION_ABORT:
            System.exit(1);
        }
    }
}
