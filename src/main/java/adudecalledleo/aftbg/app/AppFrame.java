package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.app.component.MainPanel;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.nio.file.Path;

public final class AppFrame extends JFrame {
    public AppFrame(Path basePath, GameDefinition gameDef, WindowContext winCtx, FacePool faces, TextParser parser) {
        var panel = new MainPanel(parser);
        panel.updateGameDefinition(basePath, gameDef, faces);
        panel.updateWindowContext(winCtx);

        setTitle("Autumnflower Textbox Generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(panel);
        setJMenuBar(panel.getMenuBar());
        pack();
    }
}
