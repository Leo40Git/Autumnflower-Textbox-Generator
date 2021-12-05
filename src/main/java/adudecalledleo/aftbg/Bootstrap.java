package adudecalledleo.aftbg;

import adudecalledleo.aftbg.app.AppFrame;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.UncaughtExceptionHandler;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.text.animate.AnimationCommand;
import adudecalledleo.aftbg.text.animate.TextAnimator;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.util.GifFactory;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
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
                Logger.error("Failed to load app resources!", e);
                loadFrame.setAlwaysOnTop(false);
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
                Logger.error("Failed to load textbox resources!", e);
                loadFrame.setAlwaysOnTop(false);
                JOptionPane.showMessageDialog(null,
                        "Failed to load textbox resources!\nSee \"" + LOG_NAME + "\" for more details.",
                        "Failed to launch", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }

            //launchApp(loadFrame, basePath, rsrc);
            testGifCreation(loadFrame, basePath, rsrc);
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
        loadFrame.setLoadString("Generating...");
        loadFrame.repaint();

        Path outPath = basePath.resolveSibling("output");

        Face face = rsrc.facePool().getByPath("Mercia/Neutral");
        if (face == null) {
            Logger.error("Face \"Mercia/Neutral\" was not found!");
            System.exit(1);
            return;
        }
        int textSpeed = 5;

        TextParser parser = new TextParser();
        NodeList nodes = parser.parse("Mercia:\n\\c[25]Hold on.\n\\t[20]What?");
        if (nodes.hasErrors()) {
            StringBuilder errBuilder = new StringBuilder("Got ").append(nodes.getErrors().size()).append(" error(s):");
            for (ErrorNode node : nodes.getErrors()) {
                errBuilder.append("- ").append(node).append(System.lineSeparator());
            }
            errBuilder.append("...oops?");
            Logger.error(errBuilder.toString());
            System.exit(1);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null,
                    "Failed to parse text into nodes!\nSee \"" + LOG_NAME + "\" for more details.",
                    "Animation Test", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TextAnimator animator = new TextAnimator(nodes);

        WindowContext winCtx = rsrc.windowContext();
        List<BufferedImage> frames = new ArrayList<>();

        int frame = 0;
        AnimationCommand command;
        boolean drawFrame = false;
        while ((command = animator.nextCommand()) != AnimationCommand.endOfTextbox()) {
            if (command == AnimationCommand.drawFrame()) {
                drawFrame = true;
            } else if (command instanceof AnimationCommand.SetFace setFaceCmd) {
                Logger.trace("Setting face to %s".formatted(setFaceCmd.getFacePath()));
                Face newFace = setFaceCmd.getFace(rsrc.facePool());
                if (newFace == null) {
                    Logger.error("Face \"%s\" was not found!".formatted(setFaceCmd.getFacePath()));
                } else {
                    face = newFace;
                    drawFrame = true;
                }
            } else if (command instanceof AnimationCommand.AddDelay addDelayCmd) {
                // repeat last frame X times
                Logger.trace("Repeating last frame %d times".formatted(addDelayCmd.getLength()));
                BufferedImage lastFrame = frames.get(frames.size() - 1);
                for (int i = 0; i < addDelayCmd.getLength(); i++) {
                    frames.add(lastFrame);
                }
            } else if (command instanceof AnimationCommand.SetSpeed setSpeedCmd) {
                Logger.trace("Changing speed from %d to %d".formatted(textSpeed, setSpeedCmd.getNewSpeed()));
                textSpeed = setSpeedCmd.getNewSpeed();
            }

            if (drawFrame) {
                drawFrame = false;
                Logger.trace("Drawing frame %d".formatted(++frame));

                BufferedImage img = new BufferedImage(816, 180, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setBackground(Color.BLACK);
                g.clearRect(0, 0, 816, 180);
                winCtx.drawBackground(g, 4, 4, 808, 172, null);
                g.drawImage(face.getImage(), 18, 18, null);
                TextRenderer.draw(g, animator.getNodes(), winCtx.getColors(), 186, 21);
                winCtx.drawBorder(g, 0, 0, 816, 180, null);
                g.dispose();
                for (int i = 0; i <= textSpeed; i++) {
                    frames.add(img);
                }
            }
        }

        if (frames.isEmpty()) {
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null, "Empty animation!", "Animation Test",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        // repeat last frame for 2 seconds
        final int repeatCount = GifFactory.toFrames(2, 1);
        Logger.trace("Repeating last frame %d times".formatted(repeatCount));
        BufferedImage lastFrame = frames.get(frames.size() - 1);
        for (int i = 0; i < repeatCount; i++) {
            frames.add(lastFrame);
        }

        Path gifPath = outPath.resolve("output.gif");
        try (OutputStream out = Files.newOutputStream(gifPath)) {
            GifFactory.write(frames, 1, "Created using Autumnflower Textbox Generator", out);
        } catch (IOException e) {
            Logger.error("Failed to write GIF", e);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null,
                    "Failed to write GIF!\nSee \"" + LOG_NAME + "\" for more details.",
                    "Animation Test", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        loadFrame.setLoadString("Done!");
        loadFrame.repaint();

        new Timer(3000, e -> System.exit(0)).start();
    }
}
