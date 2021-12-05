package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.animate.AnimationCommand;
import adudecalledleo.aftbg.text.animate.AnimationCommandNode;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;

public final class TextSpeedModifierNode extends ModifierNode implements AnimationCommandNode {
    public static final char KEY = 't'; // TODO find better key for this...
                                        //  maybe rename style to formatting to free up 's'?

    private final AnimationCommand.SetSpeed animCmd;

    public TextSpeedModifierNode(int start, int length, int newSpeed, Span... argSpans) {
        super(start, length, KEY, argSpans);
        animCmd = new AnimationCommand.SetSpeed(newSpeed);
    }

    @Override
    public AnimationCommand getAnimationCommand() {
        return animCmd;
    }

    public static final class Parser implements ModifierParser {
        private static final String ERROR_PREFIX = "Text speed modifier: ";

        @Override
        public void parse(int start, int argsStart, String args, NodeList nodes) {
            if (args == null) {
                nodes.add(new ErrorNode(start, 2,
                        ERROR_PREFIX + "1 argument required, new speed"));
                return;
            } else if (args.isBlank()) {
                nodes.add(new ErrorNode(start, 2 + args.length() + 2,
                        ERROR_PREFIX + "1 argument required, new speed"));
                return;
            }

            int newSpeed;
            try {
                newSpeed = Integer.parseUnsignedInt(args);
            } catch (NumberFormatException e) {
                nodes.add(new ErrorNode(argsStart, args.length(),
                        ERROR_PREFIX + "Couldn't parse new speed"));
                return;
            }
            if (newSpeed == 0) {
                nodes.add(new ErrorNode(argsStart, args.length(),
                        ERROR_PREFIX + "Speed cannot be 0"));
                return;
            }

            nodes.add(new TextSpeedModifierNode(start, 2 + args.length() + 2, newSpeed, new Span(argsStart, args.length())));
        }
    }
}
