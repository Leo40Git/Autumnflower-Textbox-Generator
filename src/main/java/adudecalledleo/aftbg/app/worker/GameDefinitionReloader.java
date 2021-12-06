package adudecalledleo.aftbg.app.worker;

import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.TextboxResources;
import adudecalledleo.aftbg.app.component.MainPanel;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;

public final class GameDefinitionReloader extends SwingWorker<Void, Void> {
    private final MainPanel panel;
    private final LoadFrame loadFrame;
    private final Path basePath;

    public GameDefinitionReloader(MainPanel panel, LoadFrame loadFrame, Path basePath) {
        this.panel = panel;
        this.loadFrame = loadFrame;
        this.basePath = basePath;
    }

    @Override
    protected Void doInBackground() {
        TextboxResources rsrc;
        try {
            rsrc = TextboxResources.load(basePath);
        } catch (TextboxResources.LoadException e) {
            Logger.error("Failed to reload textbox resources!", e);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(panel,
                    "Failed to reload textbox resources!\n" + e + "\n"
                            + "See \"" + Logger.logFile() + "\" for more details.",
                    "Reload Game Definition", JOptionPane.ERROR_MESSAGE);
            loadFrame.dispose();
            panel.requestFocus();
            return null;
        }

        SwingUtilities.invokeLater(() -> {
            panel.updateGameDefinition(basePath, rsrc.gameDefinition(), rsrc.facePool());
            panel.updateWindowContext(rsrc.windowContext());
            loadFrame.dispose();
            panel.requestFocus();
        });
        return null;
    }
}
