package adudecalledleo.aftbg;

import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppFrame;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.text.TextParser;

public final class Main {
    public static void main(String[] args) {
        Bootstrap result = Bootstrap.perform();
        if (result == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> launchApp(result.loadFrame(), result.basePath(), result.textboxResources()));
    }

    private static void launchApp(LoadFrame loadFrame, Path basePath, TextboxResources rsrc) {
        AppFrame frame = new AppFrame(basePath, rsrc.gameDefinition(), rsrc.windowContext(), rsrc.facePool(), new TextParser());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        loadFrame.setVisible(false);
        loadFrame.dispose();
        frame.requestFocus();
    }
}
