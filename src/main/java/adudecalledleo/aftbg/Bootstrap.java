package adudecalledleo.aftbg;

import adudecalledleo.aftbg.app.AppFrame;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.UncaughtExceptionHandler;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.text.TextAnimator;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.util.GIFFactory;
import adudecalledleo.aftbg.util.PathUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.error("Failed to set system L&F", e);
        }

        LoadFrame loadFrame = new LoadFrame("Loading...", true);

        SwingUtilities.invokeLater(() -> {
            try {
                AppResources.load();
            } catch (IOException e) {
                loadFrame.setAlwaysOnTop(false);
                Logger.error("Failed to load app resources!", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to load app resources!\nSee \"" + LOG_NAME + "\" for more details.",
                        "Failed to launch", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }

            Path basePath = Paths.get("scratch").toAbsolutePath();

            TextboxResources rsrc;
            try {
                rsrc = TextboxResources.load(basePath);
            } catch (TextboxResources.LoadException e) {
                loadFrame.setAlwaysOnTop(false);
                Logger.error("Failed to load textbox resources!", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to load textbox resources!\nSee \"" + LOG_NAME + "\" for more details.",
                        "Failed to launch", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }

            launchApp(loadFrame, basePath, rsrc);
        });
    }

    private static void launchApp(LoadFrame loadFrame, Path basePath, TextboxResources rsrc) {
        AppFrame frame = new AppFrame(basePath, rsrc.gameDefinition(), rsrc.windowContext(), rsrc.facePool(), new TextParser());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        loadFrame.setVisible(false);
        loadFrame.dispose();
        frame.requestFocus();
    }

    private static void testGifCreation(LoadFrame loadFrame, Path basePath, TextboxResources rsrc) {
        loadFrame.setVisible(false);
        loadFrame.dispose();

        Path outPath = basePath.resolveSibling("output");

        Face merciaNeutral = rsrc.facePool().getByPath("Mercia/Neutral");
        if (merciaNeutral == null) {
            Logger.error("Face \"Mercia/Neutral\" was not found!");
            System.exit(1);
            return;
        }

        TextParser parser = new TextParser();
        NodeList nodes = parser.parse("Mercia:\n\\c[25]Hold on.\n\\g[flip=horizontal,fill=rainbow]This probably looks funky.");
        TextAnimator animator = new TextAnimator(nodes);

        WindowContext winCtx = rsrc.windowContext();
        List<BufferedImage> frames = new ArrayList<>();

        while (!animator.nextChar()) {
            BufferedImage img = new BufferedImage(816, 180, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setBackground(Color.BLACK);
            g.clearRect(0, 0, 816, 180);
            winCtx.drawBackground(g, 4, 4, 808, 172, null);
            g.drawImage(merciaNeutral.getImage(), 18, 18, null);
            TextRenderer.draw(g, animator.getNodes(), winCtx.getColors(), 186, 21);
            winCtx.drawBorder(g, 0, 0, 816, 180, null);
            g.dispose();
            frames.add(img);
        }

        Path gifPath = outPath.resolve("output.gif");
        try (OutputStream out = Files.newOutputStream(gifPath)) {
            out.write(GIFFactory.create(frames, 5));
        } catch (IOException e) {
            Logger.error("Failed to write GIF", e);
            System.exit(1);
        }
    }
}
