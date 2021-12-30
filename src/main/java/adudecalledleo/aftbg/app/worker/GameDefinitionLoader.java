package adudecalledleo.aftbg.app.worker;

import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.app.component.MainPanel;
import adudecalledleo.aftbg.app.game.DefinitionLoadException;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;

public final class GameDefinitionLoader extends SwingWorker<Void, Void> {
    private final MainPanel panel;
    private final LoadFrame loadFrame;
    private final Path defPath;

    public GameDefinitionLoader(MainPanel panel, LoadFrame loadFrame, Path defPath) {
        this.panel = panel;
        this.loadFrame = loadFrame;
        this.defPath = defPath;
    }

    @Override
    protected Void doInBackground() {
        Logger.trace("ABCD");

        GameDefinition gameDef;
        try {
            gameDef = GameDefinition.load(defPath);
        } catch (DefinitionLoadException e) {
            Logger.error("Failed to load game definition!", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(null, "Failed to load game definition!", "Load Game Definition");
            loadFrame.dispose();
            return null;
        }

        SwingUtilities.invokeLater(() -> panel.updateGameDefinition(gameDef));
        loadFrame.dispose();
        return null;
    }
}
