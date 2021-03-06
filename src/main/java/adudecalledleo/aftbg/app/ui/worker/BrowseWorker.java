package adudecalledleo.aftbg.app.ui.worker;

import java.awt.*;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;

public final class BrowseWorker extends SwingWorker<Void, Void> {
    private final URI uri;

    public BrowseWorker(URI uri) {
        this.uri = uri;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Desktop.getDesktop().browse(uri);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException | ExecutionException e) {
            Main.logger().error("Failed to browse to URI \"%s\"".formatted(uri), e);
            DialogUtils.showErrorDialog(null, "Failed to open link in your default browser!",
                    "Browse to link");
        }
    }
}
