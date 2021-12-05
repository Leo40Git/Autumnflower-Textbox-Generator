package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.animate.AnimationCommand;
import adudecalledleo.aftbg.text.animate.AnimationCommandNode;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;

import java.util.Arrays;

public final class DelayModifierNode extends ModifierNode implements AnimationCommandNode {
    public static final char KEY = 'd';

    private final AnimationCommand.AddDelay animCmd;

    public DelayModifierNode(int start, int length, int delayLength, Span... argSpans) {
        super(start, length, KEY, argSpans);
        animCmd = new AnimationCommand.AddDelay(delayLength);
    }

    @Override
    public AnimationCommand getAnimationCommand() {
        return animCmd;
    }

    public static final class Parser implements ModifierParser {
        private static final String ERROR_PREFIX = "Delay modifier: ";

        @Override
        public void parse(int start, int argsStart, String args, NodeList nodes) {
            if (args == null) {
                nodes.add(new ErrorNode(start, 2,
                        ERROR_PREFIX + "1 argument required, delay length"));
                return;
            } else if (args.isBlank()) {
                nodes.add(new ErrorNode(start, 2 + args.length() + 2,
                        ERROR_PREFIX + "1 argument required, delay length"));
                return;
            }

            int delayLength;
            try {
                delayLength = Integer.parseUnsignedInt(args);
            } catch (NumberFormatException e) {
                nodes.add(new ErrorNode(argsStart, args.length(),
                        ERROR_PREFIX + "Couldn't parse delay length"));
                return;
            }

            nodes.add(new DelayModifierNode(start, 2 + args.length() + 2, delayLength, new Span(argsStart, args.length())));
        }
    }

    @Override
    public String toString() {
        return "DelayModifierNode{" +
                "delayLength=" + animCmd.getLength() +
                ", start=" + start +
                ", length=" + length +
                ", argSpans=" + Arrays.toString(argSpans) +
                '}';
    }
}
