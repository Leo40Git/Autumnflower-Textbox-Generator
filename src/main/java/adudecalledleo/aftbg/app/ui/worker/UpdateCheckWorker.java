package adudecalledleo.aftbg.app.ui.worker;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppUpdateCheck;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;

public final class UpdateCheckWorker extends SwingWorker<Void, Void> {
    private final Component parent;
    private final LoadFrame loadFrame;

    public UpdateCheckWorker(Component parent, LoadFrame loadFrame) {
        this.parent = parent;
        this.loadFrame = loadFrame;
    }

    @Override
    protected Void doInBackground() {
        try {
            loadFrame.setLoadString("Checking for updates...");
            AppUpdateCheck.doCheck(parent, loadFrame);
        } catch (AppUpdateCheck.CheckFailedException e) {
            Logger.error("Update check failed!", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(parent, "Failed to check for updates!", "Failed to check for updates");
            loadFrame.setAlwaysOnTop(true);
        } finally {
            loadFrame.dispose();
        }
        return null;
    }
}
