package adudecalledleo.aftbg.app;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.render.TextRenderer;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.animate.AnimationCommand;
import adudecalledleo.aftbg.text.animate.TextAnimator;
import adudecalledleo.aftbg.text.modifier.InterruptModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.util.GifFactory;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxRenderer {
    private TextboxRenderer() { }

    public static void renderOne(WindowContext winCtx, Graphics2D g, int x, int y, Face face, NodeList nodes, int arrowFrame) {
        winCtx.drawBackground(g, x + 4, y + 4, 808, 172, null);
        g.drawImage(face.getImage(), x + 18, y + 18, null);
        TextRenderer.draw(g, nodes,
                x + (face.isBlank() ? 18 : 186),
                y + 21);
        winCtx.drawBorder(g, x, y, 816, 180, null);
        if (arrowFrame >= 0) {
            winCtx.drawArrow(g, x, y, 816, 180, arrowFrame, null);
        }
    }
    
    public static BufferedImage render(WindowContext winCtx, TextParser parser, TextParser.Context parserCtx,
                                       List<Textbox> textboxes) {
        final int textboxCount = textboxes.size();
        var image = new BufferedImage(816, 180 * textboxCount + 2 * (textboxCount - 1), BufferedImage.TYPE_INT_ARGB);

        var g = image.createGraphics();
        g.setBackground(ColorUtils.TRANSPARENT);
        g.clearRect(0, 0, image.getWidth(), image.getHeight());

        for (int i = 0; i < textboxCount; i++) {
            var textbox = textboxes.get(i);
            var nodes = parser.parse(parserCtx, textbox.getText());
            if (nodes.hasErrors()) {
                return null;
            }

            g.setClip(0, 182 * i, 816, 180);
            renderOne(winCtx, g, 0, 182 * i, textbox.getFace(), nodes, i < textboxCount - 1 ? 0 : -1);
        }

        g.dispose();
        return image;
    }

    public static final int DEFAULT_TEXT_SPEED = 5;

    public static final int ARROW_FRAME_LENGTH = GifFactory.toFrames(0.1, 1);
    public static final int ARROW_MAX_LOOPS = 4;
    public static final int LAST_FRAME_REPEAT = GifFactory.toFrames(2, 1);

    public static byte[] renderAnimation(WindowContext winCtx, TextParser parser, TextParser.Context parserCtx,
                                         List<Textbox> textboxes) throws IOException {
        final int textboxCount = textboxes.size();

        TextAnimator animator = new TextAnimator(new NodeList());
        List<BufferedImage> frames = new ArrayList<>();

        for (int i = 0; i < textboxes.size(); i++) {
            Textbox textbox = textboxes.get(i);

            NodeList sourceNodes = parser.parse(parserCtx, textbox.getText());
            if (sourceNodes.hasErrors()) {
                return null;
            }

            animator.reset(sourceNodes);

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
                        renderOne(winCtx, g, 0, 0, face, animator.getNodes(), -1);
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
                    renderOne(winCtx, g, 0, 0, face, animator.getNodes(), -1);
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
                        renderOne(winCtx, g, 0, 0, face, sourceNodes, arrowFrame++);
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
                renderOne(winCtx, g, 0, 0, face, sourceNodes, -1);
                g.dispose();
                for (int j = 0; j < LAST_FRAME_REPEAT; j++) {
                    frames.add(img);
                }
            }
        }

        if (frames.isEmpty()) {
            throw new IllegalStateException("Somehow generated no frames?!");
        }

        Logger.trace("Creating GIF data");
        return GifFactory.create(frames, 1,
                "Created using %s v%s".formatted(BuildInfo.name(), BuildInfo.version()));
    }
}
