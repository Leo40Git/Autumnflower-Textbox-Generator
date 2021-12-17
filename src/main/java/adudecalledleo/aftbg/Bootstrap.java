package adudecalledleo.aftbg;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import adudecalledleo.aftbg.app.AppPrefs;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.AppUpdateCheck;
import adudecalledleo.aftbg.app.UncaughtExceptionHandler;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.text.modifier.ModifierRegistry;

public record Bootstrap(LoadFrame loadFrame, Path basePath, TextboxResources textboxResources) {
    private static boolean done = false;

    public static Bootstrap perform() {
        if (done) {
            throw new IllegalStateException("Tried to perform bootstrap after it was already performed!");
        }

        try {
            BuildInfo.load();
        } catch (Exception e) {
            System.err.println("failed to load build info! this build is probably hosed");
            e.printStackTrace();
            System.exit(1);
            return null;
        }

        try {
            Logger.init();
        } catch (Exception e) {
            System.err.println("failed to initialize logger!");
            e.printStackTrace();
            System.exit(1);
            return null;
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

        ModifierRegistry.init();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.error("Failed to set system L&F", e);
        }

        LoadFrame loadFrame = new LoadFrame("Loading...", true);

        try {
            AppPrefs.init();
        } catch (IOException e) {
            Logger.error("Failed to initialize preferences!", e);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null,
                    "Failed to initialize preferences!\nSee \"" + Logger.logFile() + "\" for more details.",
                    "Failed to launch", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        AppUpdateCheck.init();
        if (AppUpdateCheck.isAvailable() && !BuildInfo.isDevelopment() && AppPrefs.isAutoUpdateCheckEnabled()) {
            try {
                loadFrame.setLoadString("Checking for updates...");
                AppUpdateCheck.doCheck(null);
            } catch (AppUpdateCheck.CheckFailedException e) {
                Logger.error("Update check failed!", e);
                loadFrame.setAlwaysOnTop(false);
                JOptionPane.showMessageDialog(null,
                        "Failed to check for updates!\nSee \"" + Logger.logFile() + "\" for more details.",
                        "Failed to check for updates", JOptionPane.ERROR_MESSAGE);
                loadFrame.setAlwaysOnTop(true);
            } finally {
                loadFrame.setLoadString("Loading...");
            }
        }

        try {
            AppResources.load();
        } catch (IOException e) {
            Logger.error("Failed to load app resources!", e);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null,
                    "Failed to load app resources!\nSee \"" + Logger.logFile() + "\" for more details.",
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
                    "Failed to load textbox resources!\nSee \"" + Logger.logFile() + "\" for more details.",
                    "Failed to launch", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(Bootstrap::cleanup, "cleanup"));

        done = true;
        return new Bootstrap(loadFrame, basePath, rsrc);
    }

    // (hopefully) called by shutdown hook, so we're moments before the app dies
    private static void cleanup() {
        AppPrefs.flush();

        // logger is shut down last, in case things need to log errors beforehand
        Logger.shutdown();
    }
}
