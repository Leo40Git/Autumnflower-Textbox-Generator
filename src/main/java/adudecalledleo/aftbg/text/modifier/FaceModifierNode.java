package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.animate.AnimationCommand;
import adudecalledleo.aftbg.text.animate.AnimationCommandNode;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;

import java.util.Arrays;

public final class FaceModifierNode extends ModifierNode implements AnimationCommandNode {
    public static final char KEY = '@';

    private final AnimationCommand.SetFace animCmd;

    public FaceModifierNode(int start, int length, String facePath, Span... argSpans) {
        super(start, length, KEY, argSpans);
        animCmd = new AnimationCommand.SetFace(facePath);
    }

    @Override
    public AnimationCommand getAnimationCommand() {
        return animCmd;
    }

    public static final class Parser implements ModifierParser {
        private static final String ERROR_PREFIX = "Face modifier: ";

        @Override
        public void parse(int start, int argsStart, String args, NodeList nodes) {
            if (args == null) {
                nodes.add(new FaceModifierNode(start, 2, null));
                return;
            } else if (args.isBlank()) {
                nodes.add(new ErrorNode(start, 2 + args.length() + 2,
                        ERROR_PREFIX + "1 argument required, face path (category/name)"));
                return;
            } else if (!args.contains("/")) {
                nodes.add(new ErrorNode(start, 2 + args.length() + 2,
                        ERROR_PREFIX + "Invalid argument, should be face path (category/name)"));
                return;
            }

            nodes.add(new FaceModifierNode(start, 2 + args.length() + 2, args, new Span(argsStart, args.length())));
        }
    }

    @Override
    public String toString() {
        return "FaceModifierNode{" +
                "facePath=" + animCmd.getFacePath() +
                ", start=" + start +
                ", length=" + length +
                ", argSpans=" + Arrays.toString(argSpans) +
                '}';
    }
}
