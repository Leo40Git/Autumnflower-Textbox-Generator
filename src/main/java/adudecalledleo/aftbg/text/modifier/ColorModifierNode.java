package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;
import adudecalledleo.aftbg.window.WindowColors;

import java.awt.*;
import java.util.Arrays;

public sealed abstract class ColorModifierNode extends ModifierNode {
    protected ColorModifierNode(int start, int length, Span... argSpans) {
        super(start, length, 'c', argSpans);
    }

    public abstract Color getColor(WindowColors windowColors);

    public static final class Window extends ColorModifierNode {
        private final int i;

        public Window(int start, int length, Span argSpan, int i) {
            super(start, length, argSpan);
            this.i = i;
        }

        @Override
        public Color getColor(WindowColors windowColors) {
            return windowColors.get(i);
        }

        @Override
        public String toString() {
            return "ColorModifierNode.Window{" +
                    "i=" + i +
                    ", start=" + start +
                    ", length=" + length +
                    ", argSpans=" + Arrays.toString(argSpans) +
                    '}';
        }
    }

    public static final class Constant extends ColorModifierNode {
        private final Color color;

        public Constant(int start, int length, Span argSpan, Color color) {
            super(start, length, argSpan);
            this.color = color;
        }

        @Override
        public Color getColor(WindowColors windowColors) {
            return color;
        }

        @Override
        public String toString() {
            return "ColorModifierNode.Constant{" +
                    "color=" + color +
                    ", start=" + start +
                    ", length=" + length +
                    ", argSpans=" + Arrays.toString(argSpans) +
                    '}';
        }
    }

    public static final class Parser implements ModifierParser {
        private static final String ERROR_PREFIX = "Color modifier: ";

        @Override
        public void parse(int start, int argsStart, String args, NodeList nodes) {
            if (args == null) {
                nodes.add(new ErrorNode(start, 2,
                        ERROR_PREFIX + "1 argument required, either window color ID or hex color"));
                return;
            }

            if (args.startsWith("#")) {
                String hex = args.substring(1);
                int hexlen = hex.length();
                if (hexlen != 3 && hexlen != 6) {
                    nodes.add(new ErrorNode(argsStart, hexlen + 1,
                            ERROR_PREFIX + "Invalid hex color, should be either 6 or 3 characters long"));
                    return;
                }
                int rgb;
                try {
                    rgb = Integer.parseUnsignedInt(hex, 16);
                } catch (NumberFormatException e) {
                    nodes.add(new ErrorNode(argsStart, hexlen + 1,
                            ERROR_PREFIX + "Couldn't parse hex color value"));
                    return;
                }
                int r, g, b;
                if (hexlen == 3) {
                    // CSS-style
                    r = rgb & 0xF;
                    r += r << 4;
                    g = (rgb >> 4) & 0xF;
                    g += g << 4;
                    b = (rgb >> 8) & 0xF;
                    b += b << 4;
                } else {
                    // standard
                    r = rgb & 0xFF;
                    g = (rgb >> 8) & 0xFF;
                    b = (rgb >> 16) & 0xFF;
                }
                nodes.add(new Constant(start, 2 + hexlen + 2, new Span(argsStart, hexlen + 1),
                        new Color(r | g << 8 | b << 16, false)));
            } else {
                try {
                    nodes.add(new Window(start, 2 + args.length() + 2, new Span(argsStart, args.length()),
                            Integer.parseUnsignedInt(args)));
                } catch (NumberFormatException e) {
                    nodes.add(new ErrorNode(argsStart, args.length(),
                            ERROR_PREFIX + "Couldn't parse window color ID"));
                }
            }
        }
    }
}
