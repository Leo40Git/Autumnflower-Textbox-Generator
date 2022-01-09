package adudecalledleo.aftbg.app.ui.worker;

import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.app.game.DefinitionLoadException;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.ui.MainPanel;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;

public final class ExtensionDefinitionLoader extends AbstractWorker {
    private final GameDefinition gameDef;
    private final Path extPath;

    public ExtensionDefinitionLoader(MainPanel mainPanel, LoadFrame loadFrame, GameDefinition gameDef, Path extPath) {
        super(mainPanel, loadFrame);
        this.gameDef = gameDef;
        this.extPath = extPath;
    }

    @Override
    protected Void doInBackground() {
        try {
            gameDef.loadExtension(extPath);
        } catch (DefinitionLoadException e) {
            Logger.error("Failed to load extension definition!", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(null, "Failed to load extension!", "Load Extension");
            cleanup();
            return null;
        }

        SwingUtilities.invokeLater(() -> mainPanel.updateGameDefinition(gameDef));
        cleanup();
        return null;
    }
}
