package adudecalledleo.aftbg.app.worker;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.AnimatedPreviewDialog;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.animate.AnimationCommand;
import adudecalledleo.aftbg.text.animate.TextAnimator;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.util.GifFactory;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxAnimator extends AbstractTextboxWorker {
    private static final int TEMP_BOX_BARRIER_LENGTH = GifFactory.toFrames(1, 1);
    private static final int LAST_FRAME_REPEAT = GifFactory.toFrames(2, 1);

    private final FacePool facePool;

    public TextboxAnimator(Component parent, LoadFrame loadFrame, TextParser parser, WindowContext winCtx, FacePool facePool, List<Textbox> textboxes) {
        super(parent, loadFrame, parser, winCtx, textboxes);
        this.facePool = facePool;
    }

    @Override
    protected Void doInBackground() {
        final int textboxCount = textboxes.size();

        TextAnimator animator = null;
        boolean success = true;
        List<BufferedImage> frames = new ArrayList<>();

        for (int i = 0; i < textboxes.size(); i++) {
            Textbox textbox = textboxes.get(i);

            NodeList sourceNodes = parser.parse(textbox.getText());
            if (sourceNodes.hasErrors()) {
                success = false;
                break;
            }

            if (animator == null) {
                animator = new TextAnimator(sourceNodes);
            } else {
                animator.reset(sourceNodes);
            }

            int textSpeed = 5;

            Face face = textbox.getFace();
            int frame = 0;
            AnimationCommand command;
            boolean drawFrame = false;
            while ((command = animator.nextCommand()) != AnimationCommand.endOfTextbox()) {
                if (command == AnimationCommand.drawFrame()) {
                    drawFrame = true;
                } else if (command instanceof AnimationCommand.SetFace setFaceCmd) {
                    Logger.trace("Setting face to %s".formatted(setFaceCmd.getFacePath()));
                    Face newFace = setFaceCmd.getFace(facePool);
                    if (newFace == null) {
                        // TODO report error
                        Logger.error("Face \"%s\" was not found!".formatted(setFaceCmd.getFacePath()));
                    } else {
                        face = newFace;
                        drawFrame = true;
                    }
                } else if (command instanceof AnimationCommand.AddDelay addDelayCmd) {
                    // repeat last frame X times
                    Logger.trace("Repeating last frame %d times".formatted(addDelayCmd.getLength()));
                    BufferedImage lastFrame = frames.get(frames.size() - 1);
                    for (int j = 0; j < addDelayCmd.getLength(); j++) {
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
                    drawTextbox(g, 0, 0, face, animator.getNodes(), -1);
                    g.dispose();
                    for (int j = 0; j <= textSpeed; j++) {
                        frames.add(img);
                    }
                }
            }

            if (i < textboxCount - 1) {
                // TODO add arrow animation between textboxes
                // for now we'll do this
                BufferedImage img = new BufferedImage(816, 180, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setBackground(Color.BLACK);
                g.clearRect(0, 0, 816, 180);
                drawTextbox(g, 0, 0, face, sourceNodes, 0);
                g.dispose();
                for (int j = 0; j <= TEMP_BOX_BARRIER_LENGTH; j++) {
                    frames.add(img);
                }
            }
        }

        if (frames.isEmpty()) {
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(parent,
                    "Ended up generating nothing. Pretty sure this is impossible.",
                    "Generate animated textbox(es)", JOptionPane.ERROR_MESSAGE);
            loadFrame.dispose();
            return null;
        }

        Logger.trace("Repeating last frame %d times".formatted(LAST_FRAME_REPEAT));
        BufferedImage lastFrame = frames.get(frames.size() - 1);
        for (int i = 0; i < LAST_FRAME_REPEAT; i++) {
            frames.add(lastFrame);
        }

        if (success) {
            Logger.trace("Creating GIF data");
            byte[] imageData;
            try {
                imageData = GifFactory.create(frames, 1,
                        "Created using %s v%s".formatted(BuildInfo.name(), BuildInfo.version()));
            } catch (IOException e) {
                Logger.error("Failed to generate GIF data", e);
                loadFrame.setAlwaysOnTop(false);
                JOptionPane.showMessageDialog(null,
                        "Failed to generate GIF data!\nSee \"" + Logger.logFile() + "\" for more details.",
                        "Animation Test", JOptionPane.ERROR_MESSAGE);
                loadFrame.dispose();
                return null;
            }
            var dialog = new AnimatedPreviewDialog((Frame) SwingUtilities.getWindowAncestor(parent), imageData);
            dialog.setLocationRelativeTo(null);
            loadFrame.dispose();
            dialog.setVisible(true);
        } else {
            loadFrame.setAlwaysOnTop(false);
            // TODO more detailed error message
            JOptionPane.showMessageDialog(parent,
                    "Seems like one or more of your textboxes have errors!\n"
                            + "Correct this, then try generating again.",
                    "Generate animated textbox(es)", JOptionPane.ERROR_MESSAGE);
            loadFrame.dispose();
        }

        return null;
    }
}
