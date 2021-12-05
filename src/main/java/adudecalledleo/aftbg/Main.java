package adudecalledleo.aftbg;

import java.nio.file.Path;
import java.util.Locale;

import javax.swing.SwingUtilities;

import adudecalledleo.aftbg.app.AppFrame;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.text.TextParser;

public final class Main {
    public static final String NAME = "Autumnflower Textbox Generator";
    public static final String NAME_ABBR = "AFTBG";
    public static final String VERSION = "0.1.0"; // TODO replace with comparable type

    public static final String LOG_NAME = NAME_ABBR.toLowerCase(Locale.ROOT) + ".log";

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
