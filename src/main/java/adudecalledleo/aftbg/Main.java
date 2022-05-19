package adudecalledleo.aftbg;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.app.*;
import adudecalledleo.aftbg.app.game.DefinitionLoadException;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.text.node.NodeRegistry;
import adudecalledleo.aftbg.app.ui.AppFrame;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.render.UIColors;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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

        UIColors.update();

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
                AppUpdateCheck.doCheck(null, loadFrame, true);
            } catch (AppUpdateCheck.CheckFailedException e) {
                logger.error("Update check failed!", e);
                loadFrame.setAlwaysOnTop(false);
                DialogUtils.showErrorDialog(null, "Failed to check for updates!", "Failed to check for updates");
                loadFrame.setAlwaysOnTop(true);
            } finally {
                loadFrame.setLoadString("Loading...");
            }
        }

        loadFrame.setLoadString("Loading resources...");
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
        GameDefinition gameDef = null;
        GameDefinitionChooser gameDefChooser = null;

        while (gameDef == null) {
            if (defPath == null) {
                AppPreferences.getLastExtensions().clear();

                if (gameDefChooser == null) {
                    gameDefChooser = new GameDefinitionChooser();
                }

                loadFrame.setVisible(false);

                defPath = gameDefChooser.show();
                if (defPath == null) {
                    // user cancelled chooser
                    AppPreferences.setLastGameDefinition(null);
                    System.exit(0);
                }

                loadFrame.restartAnimation();
                loadFrame.setVisible(true);
                loadFrame.requestFocus();
            }

            loadFrame.setLoadString("Loading game definition...");
            try {
                gameDef = GameDefinition.load(defPath);
            } catch (DefinitionLoadException e) {
                logger.error("Failed to load game definition from \"%s\"!".formatted(defPath), e);
                loadFrame.setAlwaysOnTop(false);
                JOptionPane.showMessageDialog(null, ("""
                                Failed to load game definition from:
                                %s
                                %s
                                Please select another game definition to load.""")
                                .formatted(defPath, DialogUtils.logFileInstruction()),
                        "Failed to load game definition", JOptionPane.ERROR_MESSAGE);
                defPath = null;
            }
        }

        if (gameDefChooser != null) {
            gameDefChooser.dispose();
        }

        AppPreferences.setLastGameDefinition(gameDef.filePath());

        loadFrame.setLoadString("Loading extensions...");
        var extIt = AppPreferences.getLastExtensions().iterator();
        while (extIt.hasNext()) {
            var extPath = extIt.next().toAbsolutePath();
            try {
                gameDef.loadExtension(extPath);
            } catch (DefinitionLoadException e) {
                extIt.remove();
                logger.error("Failed to auto-load extension from \"%s\"!".formatted(defPath), e);
                loadFrame.setAlwaysOnTop(false);
                JOptionPane.showMessageDialog(null, ("""
                                Failed to auto-load extension from:
                                %s
                                %s
                                This extension has been removed from the auto-load list.""")
                                .formatted(defPath, DialogUtils.logFileInstruction()),
                        "Failed to auto-load extension", JOptionPane.ERROR_MESSAGE);
                loadFrame.setAlwaysOnTop(true);
            }
        }

        loadFrame.setLoadString("Opening...!");
        AppFrame frame = new AppFrame(gameDef);
        loadFrame.dispose();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocus();
    }

    private static final class GameDefinitionChooser implements ActionListener {
        private final JFrame frame;

        private volatile boolean done;
        private volatile Path defPath;

        public GameDefinitionChooser() {
            var fc = new JFileChooser();
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setMultiSelectionEnabled(false);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fc.setFileFilter(AppFileExtensions.FILTER_GAME_DEFINITION);
            fc.addActionListener(this);

            // using frame instead of just calling JFileChooser.showOpenDialog
            //  so that the "dialog" actually gets shown in the taskbar
            frame = new JFrame("Load game definition");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setComponentOrientation(fc.getComponentOrientation());
            frame.setResizable(false);
            var contentPane = frame.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(fc, BorderLayout.CENTER);

            if (JDialog.isDefaultLookAndFeelDecorated() && UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
                frame.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
            }

            frame.pack();
            frame.setLocationRelativeTo(null);
        }

        public @Nullable Path show() {
            done = false;
            defPath = null;
            frame.setVisible(true);
            frame.requestFocus();
            while (!done) { } // idle until user selects file
            frame.setVisible(false);
            return defPath;
        }

        public void dispose() {
            frame.dispose();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case JFileChooser.APPROVE_SELECTION -> {
                    defPath = ((JFileChooser) e.getSource()).getSelectedFile().toPath();
                    done = true;
                }
                case JFileChooser.CANCEL_SELECTION -> {
                    defPath = null;
                    done = true;
                }
            }
        }
    }

    // (hopefully) called by shutdown hook, so we're moments before the app dies
    private static void cleanup() {
        AppPreferences.flush();
    }
}
