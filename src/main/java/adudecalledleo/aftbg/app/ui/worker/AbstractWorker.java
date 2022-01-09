package adudecalledleo.aftbg.app.ui.worker;

import javax.swing.*;

import adudecalledleo.aftbg.app.ui.MainPanel;
import adudecalledleo.aftbg.app.util.LoadFrame;

public abstract class AbstractWorker extends SwingWorker<Void, Void> {
    protected final MainPanel mainPanel;
    protected final LoadFrame loadFrame;

    protected AbstractWorker(MainPanel mainPanel, LoadFrame loadFrame) {
        this.mainPanel = mainPanel;
        this.loadFrame = loadFrame;
    }

    protected void cleanup() {
        loadFrame.dispose();
        mainPanel.frame.setEnabled(true);
        mainPanel.frame.requestFocus();
    }
}
