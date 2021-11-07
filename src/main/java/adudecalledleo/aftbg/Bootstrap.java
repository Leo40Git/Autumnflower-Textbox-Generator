package adudecalledleo.aftbg;

import adudecalledleo.aftbg.app.AppFrame;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.text.TextParser;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Bootstrap {
    public static final String NAME = "Autumnflower Textbox Generator";
    public static final String NAME_ABBR = "AFTBG";
    public static final String VERSION = "0.1.0"; // TODO replace with comparable type

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        LoadFrame loadFrame = new LoadFrame("Loading...", true);

        SwingUtilities.invokeLater(() -> {
            try {
                AppResources.load();
            } catch (IOException e) {
                // TODO log error to file
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to load app resources!",
                        "Failed to launch", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }

            Path basePath = Paths.get("scratch").toAbsolutePath();

            TextboxResources rsrc;
            try {
                rsrc = TextboxResources.load(basePath);
            } catch (TextboxResources.LoadException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to load textbox resources!",
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
