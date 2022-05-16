package adudecalledleo.aftbg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppPreferences;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.AppUpdateCheck;
import adudecalledleo.aftbg.app.UncaughtExceptionHandler;
import adudecalledleo.aftbg.app.game.DefinitionLoadException;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.text.node.NodeRegistry;
import adudecalledleo.aftbg.app.ui.AppFrame;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Main {
    private static Logger logger;

    public static Logger logger() {
        return logger;
    }

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
            logger = LogManager.getLogger(BuildInfo.abbreviatedName());
        } catch (Exception e) {
            System.err.println("Failed to initialize logger!");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        logger.info("{} v{} is now initializing...", BuildInfo.name(), BuildInfo.version().toString());
        if (BuildInfo.isDevelopment()) {
            logger.info(" === DEVELOPMENT MODE! === ");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanup, "cleanup"));

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

        NodeRegistry.init();

        if (!Boolean.getBoolean("skipSystemLookAndFeel")) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     UnsupportedLookAndFeelException e) {
                logger.error("Failed to set system L&F", e);
            }
        }

        LoadFrame loadFrame = new LoadFrame("Loading...", true);

        try {
            AppPreferences.init();
        } catch (IOException e) {
            logger.error("Failed to initialize preferences!", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(null, "Failed to initialize preferences!", "Failed to launch");
            System.exit(1);
        }

        if (AppUpdateCheck.isAvailable() && !BuildInfo.isDevelopment() && AppPreferences.isAutoUpdateCheckEnabled()) {
            try {
                loadFrame.setLoadString("Checking for updates...");
                AppUpdateCheck.doCheck(null, loadFrame);
            } catch (AppUpdateCheck.CheckFailedException e) {
                logger.error("Update check failed!", e);
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
            logger.error("Failed to load app resources!", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(null, "Failed to load app resources!", "Failed to launch");
            System.exit(1);
            return;
        }

        Path defPath = AppPreferences.getLastGameDefinition();
        if (defPath == null) {
            // TODO first start experience
            AppPreferences.getLastExtensions().clear();
            loadFrame.setAlwaysOnTop(false);
            File defFile = DialogUtils.fileOpenDialog(null, "Load game definition", DialogUtils.FILTER_JSON_FILES);
            if (defFile == null) {
                System.exit(0);
                return;
            }
            defPath = defFile.toPath().toAbsolutePath();
            loadFrame.setAlwaysOnTop(true);
        }

        GameDefinition gameDef;
        try {
            gameDef = GameDefinition.load(defPath);
        } catch (DefinitionLoadException e) {
            logger.error("Failed to load game definition from \"%s\"!".formatted(defPath), e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(null, "Failed to load game definition from\n%s!".formatted(defPath),
                    "Failed to launch");
            System.exit(1);
            return;
        }
        AppPreferences.setLastGameDefinition(gameDef.filePath());

        for (var extPath : AppPreferences.getLastExtensions()) {
            extPath = extPath.toAbsolutePath();
            try {
                gameDef.loadExtension(extPath);
            } catch (DefinitionLoadException e) {
                logger.error("Failed to auto-load extension from \"%s\"!".formatted(defPath), e);
                loadFrame.setAlwaysOnTop(false);
                DialogUtils.showErrorDialog(null, "Failed to auto-load extension from:\n%s".formatted(defPath),
                        "Failed to auto-load extension");
                loadFrame.setAlwaysOnTop(true);
            }
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
    }
}
