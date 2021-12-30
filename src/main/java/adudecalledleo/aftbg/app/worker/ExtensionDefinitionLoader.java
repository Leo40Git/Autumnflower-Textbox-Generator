package adudecalledleo.aftbg.app.worker;

import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.app.component.MainPanel;
import adudecalledleo.aftbg.app.game.DefinitionLoadException;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;

public final class ExtensionDefinitionLoader extends SwingWorker<Void, Void> {
    private final MainPanel panel;
    private final LoadFrame loadFrame;
    private final GameDefinition gameDef;
    private final Path extPath;

    public ExtensionDefinitionLoader(MainPanel panel, LoadFrame loadFrame, GameDefinition gameDef, Path extPath) {
        this.panel = panel;
        this.loadFrame = loadFrame;
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
            DialogUtils.showErrorDialog(null, "Failed to load extension definition!", "Load Extension Definition");
            loadFrame.dispose();
            return null;
        }

        SwingUtilities.invokeLater(() -> panel.updateGameDefinition(gameDef));
        loadFrame.dispose();
        return null;
    }
}
