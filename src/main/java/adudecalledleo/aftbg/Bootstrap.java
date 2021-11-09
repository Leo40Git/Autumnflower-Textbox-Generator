package adudecalledleo.aftbg;

import adudecalledleo.aftbg.app.AppFrame;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Level;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.text.TextParser;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public final class Bootstrap {
    public static final String NAME = "Autumnflower Textbox Generator";
    public static final String NAME_ABBR = "AFTBG";
    public static final String VERSION = "0.1.0"; // TODO replace with comparable type

    public static final String LOG_NAME = NAME_ABBR.toLowerCase(Locale.ROOT) + ".log";

    public static void main(String[] args) {
        try {
            Logger.init();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.log(Level.ERROR, "Failed to set system L&F", e);
        }

        LoadFrame loadFrame = new LoadFrame("Loading...", true);

        SwingUtilities.invokeLater(() -> {
            try {
                AppResources.load();
            } catch (IOException e) {
                Logger.log(Level.ERROR, "Failed to load app resources!", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to load app resources!\nSee \"" + LOG_NAME + " for more details.",
                        "Failed to launch", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }

            Path basePath = Paths.get("scratch").toAbsolutePath();

            TextboxResources rsrc;
            try {
                rsrc = TextboxResources.load(basePath);
            } catch (TextboxResources.LoadException e) {
                Logger.log(Level.ERROR, "Failed to load textbox resources!", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to load textbox resources!\nSee \"" + LOG_NAME + " for more details.",
                        "Failed to launch", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }

            AppFrame frame = new AppFrame(basePath, rsrc.gameDefinition(), rsrc.windowContext(), rsrc.facePool(), new TextParser());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            loadFrame.setVisible(false);
            loadFrame.dispose();
            frame.requestFocus();
        });
    }
}
