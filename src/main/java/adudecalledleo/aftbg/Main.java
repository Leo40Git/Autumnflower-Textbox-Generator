package adudecalledleo.aftbg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.app.*;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.DefinitionLoadException;
import adudecalledleo.aftbg.app.util.DialogUtils;
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

        Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanup, "cleanup"));

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
            DialogUtils.showErrorDialog(null, "Failed to initialize preferences!", "Failed to launch");
            System.exit(1);
        }

        if (AppUpdateCheck.isAvailable() && !BuildInfo.isDevelopment() && AppPreferences.isAutoUpdateCheckEnabled()) {
            try {
                loadFrame.setLoadString("Checking for updates...");
                AppUpdateCheck.doCheck(null, loadFrame);
            } catch (AppUpdateCheck.CheckFailedException e) {
                Logger.error("Update check failed!", e);
                loadFrame.setAlwaysOnTop(false);
                DialogUtils.showErrorDialog(null, "Failed to check for updates!", "Failed to check for updates");
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
            DialogUtils.showErrorDialog(null, "Failed to load app resources!", "Failed to launch");
            System.exit(1);
            return;
        }

        loadFrame.setAlwaysOnTop(false);
        File defFile = DialogUtils.fileOpenDialog(null, "Load game definition", DialogUtils.FILTER_JSON_FILES);
        if (defFile == null) {
            System.exit(0);
            return;
        }

        Path defPath = defFile.toPath();
        loadFrame.setAlwaysOnTop(true);
        GameDefinition gameDef;
        try {
            gameDef = GameDefinition.load(defPath);
        } catch (DefinitionLoadException e) {
            Logger.error("Failed to load game definition!", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(null, "Failed to load game definition!", "Failed to launch");
            System.exit(1);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(gameDef);
            loadFrame.dispose();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
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
