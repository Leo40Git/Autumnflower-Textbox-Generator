package adudecalledleo.aftbg;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.UncaughtExceptionHandler;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;

public record Bootstrap(LoadFrame loadFrame, Path basePath, TextboxResources textboxResources) {
    public static Bootstrap perform() {
        try {
            Logger.init();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.error("Failed to set system L&F", e);
        }

        LoadFrame loadFrame = new LoadFrame("Loading...", true);

        try {
            AppResources.load();
        } catch (IOException e) {
            Logger.error("Failed to load app resources!", e);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null,
                    "Failed to load app resources!\nSee \"" + Main.LOG_NAME + "\" for more details.",
                    "Failed to launch", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null;
        }

        Path basePath = Paths.get("scratch").toAbsolutePath();

        TextboxResources rsrc;
        try {
            rsrc = TextboxResources.load(basePath);
        } catch (TextboxResources.LoadException e) {
            Logger.error("Failed to load textbox resources!", e);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null,
                    "Failed to load textbox resources!\nSee \"" + Main.LOG_NAME + "\" for more details.",
                    "Failed to launch", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null;
        }

        return new Bootstrap(loadFrame, basePath, rsrc);
    }
}
