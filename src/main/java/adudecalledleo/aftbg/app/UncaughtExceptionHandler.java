package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.Bootstrap;
import adudecalledleo.aftbg.logging.Logger;

import javax.swing.*;

public final class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.error("Uncaught exception in thread \"" + t.getName() + "\"", e);
        JOptionPane.showMessageDialog(null,
                "An uncaught exception has occurred!\nSee \"" + Bootstrap.LOG_NAME + "\" for more details.",
                "Uncaught Exception!", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
