package adudecalledleo.aftbg.text.modifier;

import java.util.Arrays;

import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.animate.AnimationCommand;
import adudecalledleo.aftbg.text.animate.AnimationCommandNode;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;
import org.jetbrains.annotations.NotNull;

public final class FaceModifierNode extends ModifierNode implements AnimationCommandNode {
    public static final char KEY = '@';

    private final AnimationCommand.SetFace animCmd;

    public FaceModifierNode(int start, int length, @NotNull Face face, Span... argSpans) {
        super(start, length, KEY, argSpans);
        animCmd = new AnimationCommand.SetFace(face);
    }

    @Override
    public AnimationCommand getAnimationCommand() {
        return animCmd;
    }

    public static final class Parser implements ModifierParser {
        public static final String ERROR_PREFIX = "Face modifier: ";

        @Override
        public void parse(TextParser.Context ctx, int start, int argsStart, String args, NodeList nodes) {
            FacePool facePool = ctx.get(FacePool.class);
            if (facePool == null) {
                nodes.add(new ErrorNode(start, ModifierParser.modLen(args),
                        ERROR_PREFIX + "Missing face pool in context!"));
                return;
            }

            if (args == null) {
                nodes.add(new FaceModifierNode(start, 2, Face.NONE));
                return;
            } else if (args.isBlank()) {
                nodes.add(new ErrorNode(start, ModifierParser.modLen(args),
                        ERROR_PREFIX + "1 argument required, face path (category/name)"));
                return;
            }

            if (!args.contains("/")) {
                nodes.add(new ErrorNode(argsStart, args.length(),
                        ERROR_PREFIX + "Invalid argument, should be face path (category/name)"));
                return;
            }

            Face face = facePool.getByPath(args);
            if (face == null) {
                nodes.add(new ErrorNode(argsStart, args.length(),
                        ERROR_PREFIX + "Invalid argument, face with that path doesn't exist"));
                return;
            }

            nodes.add(new FaceModifierNode(start, ModifierParser.modLen(args), face, new Span(argsStart, args.length())));
        }
    }

    @Override
    public String toString() {
        return "FaceModifierNode{" +
                "face=" + animCmd.getFace().getPath() +
                ", start=" + start +
                ", length=" + length +
                ", argSpans=" + Arrays.toString(argSpans) +
                '}';
    }
}
