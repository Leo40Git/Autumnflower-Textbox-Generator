package adudecalledleo.aftbg.app.ui.worker;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.app.AppUpdateCheck;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;

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
            AppUpdateCheck.doCheck(parent, loadFrame, false);
        } catch (AppUpdateCheck.CheckFailedException e) {
            Main.logger().error("Update check failed!", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(parent, "Failed to check for updates:\n" + e,
                    "Failed to check for updates");
            loadFrame.setAlwaysOnTop(true);
        } finally {
            loadFrame.dispose();
        }
        return null;
    }
}
