package adudecalledleo.aftbg;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.*;

import adudecalledleo.aftbg.app.*;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.text.modifier.ModifierRegistry;

public final class Main {
    public static void main(String[] args) {
        try {
            BuildInfo.load();
        } catch (Exception e) {
            System.err.println("Failed to load build info! This build is probably hosed!");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        try {
            Logger.init();
        } catch (Exception e) {
            System.err.println("Failed to initialize logger!");
            e.printStackTrace();
            System.exit(1);
            return;
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
            AppPreferences.init();
        } catch (IOException e) {
            Logger.error("Failed to initialize preferences!", e);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null,
                    "Failed to initialize preferences!\nSee \"" + Logger.logFile() + "\" for more details.",
                    "Failed to launch", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        AppUpdateCheck.init();
        if (AppUpdateCheck.isAvailable() && !BuildInfo.isDevelopment() && AppPreferences.isAutoUpdateCheckEnabled()) {
            try {
                loadFrame.setLoadString("Checking for updates...");
                AppUpdateCheck.doCheck(null, loadFrame);
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
            return;
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
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanup, "cleanup"));

        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(basePath, rsrc.gameDefinition(), rsrc.windowContext(), rsrc.facePool());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            loadFrame.setVisible(false);
            loadFrame.dispose();
            frame.requestFocus();
        });
    }

    // (hopefully) called by shutdown hook, so we're moments before the app dies
    private static void cleanup() {
        AppPreferences.flush();

        // logger is shut down last, in case things need to log errors beforehand
        Logger.shutdown();
    }
}
