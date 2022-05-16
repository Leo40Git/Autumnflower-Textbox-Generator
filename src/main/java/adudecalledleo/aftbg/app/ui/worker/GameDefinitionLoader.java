package adudecalledleo.aftbg.app.ui.worker;

import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.app.game.DefinitionLoadException;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.MainPanel;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;

public final class GameDefinitionLoader extends AbstractWorker {
    private final Path defPath;

    public GameDefinitionLoader(MainPanel mainPanel, LoadFrame loadFrame, Path defPath) {
        super(mainPanel, loadFrame);
        this.defPath = defPath;
    }

    @Override
    protected Void doInBackground() {
        GameDefinition gameDef;
        try {
            gameDef = GameDefinition.load(defPath);
        } catch (DefinitionLoadException e) {
            Main.logger().error("Failed to load game definition!", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(null, "Failed to load game definition!", "Load Game Definition");
            cleanup();
            return null;
        }

        SwingUtilities.invokeLater(() -> mainPanel.updateGameDefinition(gameDef));
        cleanup();
        return null;
    }
}
