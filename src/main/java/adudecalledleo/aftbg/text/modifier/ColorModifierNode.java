package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;
import adudecalledleo.aftbg.window.WindowColors;

import java.awt.*;

public final class ColorModifierNode extends ModifierNode {
    public static final char KEY = 'c';

    private final Color color;

    public ColorModifierNode(int start, int length, Color color, Span... argSpans) {
        super(start, length, KEY, argSpans);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public static final class Parser implements ModifierParser {
        public static final String ERROR_PREFIX = "Color modifier: ";

        @Override
        public void parse(TextParser.Context ctx, int start, int argsStart, String args, NodeList nodes) {
            WindowColors winCols = ctx.get(WindowColors.class);
            if (winCols == null) {
                nodes.add(new ErrorNode(start, ModifierParser.modLen(args),
                        ERROR_PREFIX + "Missing window colors in context!"));
                return;
            }

            if (args == null) {
                nodes.add(new ErrorNode(start, 2,
                        ERROR_PREFIX + "1 argument required, either window color ID or hex color"));
                return;
            } else if (args.isBlank()) {
                nodes.add(new ErrorNode(start, ModifierParser.modLen(args),
                        ERROR_PREFIX + "1 argument required, either window color ID or hex color"));
                return;
            }

            if (args.startsWith("#")) {
                String hex = args.substring(1);
                int hexLen = hex.length();
                if (hexLen != 3 && hexLen != 6) {
                    nodes.add(new ErrorNode(argsStart, hexLen + 1,
                            ERROR_PREFIX + "Invalid hex color, should be either 6 or 3 characters long"));
                    return;
                }
                int rgb;
                try {
                    rgb = Integer.parseUnsignedInt(hex, 16);
                } catch (NumberFormatException e) {
                    nodes.add(new ErrorNode(argsStart, hexLen + 1,
                            ERROR_PREFIX + "Couldn't parse hex color value"));
                    return;
                }
                int r, g, b;
                if (hexLen == 3) {
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
                nodes.add(new ColorModifierNode(start, 2 + hexLen + 3,
                        new Color(r | g << 8 | b << 16, false),
                        new Span(argsStart, hexLen + 1)));
            } else {
                int index;
                try {
                    index = Integer.parseUnsignedInt(args);
                } catch (NumberFormatException e) {
                    nodes.add(new ErrorNode(argsStart, args.length(),
                            ERROR_PREFIX + "Couldn't parse window color ID"));
                    return;
                }
                if (index >= WindowColors.COUNT) {
                    nodes.add(new ErrorNode(argsStart, args.length(),
                            ERROR_PREFIX + "Window color ID is too high, max is 31"));
                    return;
                }
                nodes.add(new ColorModifierNode(start, 2 + args.length() + 3,
                        winCols.get(index),
                        new Span(argsStart, args.length())));
            }
        }
    }
}
