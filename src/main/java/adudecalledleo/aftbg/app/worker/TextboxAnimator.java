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
import adudecalledleo.aftbg.text.modifier.InterruptModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.util.GifFactory;
import adudecalledleo.aftbg.window.WindowColors;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxAnimator extends AbstractTextboxWorker {
    public static final int DEFAULT_TEXT_SPEED = 5;

    private static final int ARROW_FRAME_LENGTH = GifFactory.toFrames(0.1, 1);
    private static final int ARROW_MAX_LOOPS = 4;
    private static final int LAST_FRAME_REPEAT = GifFactory.toFrames(2, 1);

    private final FacePool facePool;

    public TextboxAnimator(Component parent, LoadFrame loadFrame, WindowContext winCtx, FacePool facePool, List<Textbox> textboxes) {
        super(parent, loadFrame, winCtx, textboxes);
        this.facePool = facePool;
    }

    @Override
    protected Void doInBackground() {
        TextParser.Context ctx = new TextParser.Context()
                .put(WindowColors.class, winCtx.getColors())
                .put(FacePool.class, facePool);

        final int textboxCount = textboxes.size();

        TextAnimator animator = null;
        boolean success = true;
        List<BufferedImage> frames = new ArrayList<>();

        for (int i = 0; i < textboxes.size(); i++) {
            Textbox textbox = textboxes.get(i);

            NodeList sourceNodes = parser.parse(ctx, textbox.getText());
            if (sourceNodes.hasErrors()) {
                success = false;
                break;
            }

            if (animator == null) {
                animator = new TextAnimator(sourceNodes);
            } else {
                animator.reset(sourceNodes);
            }

            int textSpeed = DEFAULT_TEXT_SPEED;

            Face face = textbox.getFace();
            int frame = 0;
            AnimationCommand command;
            boolean drawFrame = false;
            while ((command = animator.nextCommand()) != AnimationCommand.endOfTextbox()) {
                if (command == AnimationCommand.drawFrame()) {
                    drawFrame = textSpeed > 0;
                } else if (command instanceof AnimationCommand.SetFace setFaceCmd) {
                    face = setFaceCmd.getFace();
                    drawFrame = true;
                    Logger.trace("Setting face to %s".formatted(face.getPath()));
                } else if (command instanceof AnimationCommand.AddDelay addDelayCmd) {
                    // repeat last frame X times
                    Logger.trace("Repeating last frame %d times".formatted(addDelayCmd.getLength()));
                    BufferedImage lastFrame;
                    if (textSpeed > 0) {
                        lastFrame = frames.get(frames.size() - 1);
                    } else {
                        // need to create new frame to repeat
                        lastFrame = new BufferedImage(816, 180, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = lastFrame.createGraphics();
                        g.setBackground(Color.BLACK);
                        g.clearRect(0, 0, 816, 180);
                        drawTextbox(g, 0, 0, face, animator.getNodes(), -1);
                        g.dispose();
                        frames.add(lastFrame);
                    }
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
                if (!(sourceNodes.asList().get(sourceNodes.asList().size() - 1) instanceof InterruptModifierNode)) {
                    int arrowLoops = 0, arrowFrame = 0;
                    while (arrowLoops < ARROW_MAX_LOOPS) {
                        Logger.trace("loop %d, frame %d".formatted(arrowLoops, arrowFrame));
                        BufferedImage img = new BufferedImage(816, 180, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = img.createGraphics();
                        g.setBackground(Color.BLACK);
                        g.clearRect(0, 0, 816, 180);
                        drawTextbox(g, 0, 0, face, sourceNodes, arrowFrame++);
                        g.dispose();
                        for (int j = 0; j <= ARROW_FRAME_LENGTH; j++) {
                            frames.add(img);
                        }
                        if (arrowFrame >= 4) {
                            Logger.trace("NEXT LOOP");
                            arrowFrame = 0;
                            arrowLoops++;
                        }
                    }
                }
            } else {
                BufferedImage img = new BufferedImage(816, 180, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setBackground(Color.BLACK);
                g.clearRect(0, 0, 816, 180);
                drawTextbox(g, 0, 0, face, sourceNodes, -1);
                g.dispose();
                for (int j = 0; j < LAST_FRAME_REPEAT; j++) {
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
