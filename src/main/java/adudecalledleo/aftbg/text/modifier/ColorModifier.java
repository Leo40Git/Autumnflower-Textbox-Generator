package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.TextParserException;
import adudecalledleo.aftbg.window.WindowColors;

import java.awt.*;

public interface ColorModifier extends Modifier {
    Color getColor(WindowColors windowColors);

    record ColorIdModifier(int id) implements ColorModifier {
        @Override
        public Color getColor(WindowColors windowColors) {
            return windowColors.get(id);
        }
    }

    record HexColorModifier(Color color) implements ColorModifier {
        @Override
        public Color getColor(WindowColors windowColors) {
            return color;
        }
    }

    final class Parser implements ModifierParser {
        @Override
        public Modifier parse(String args, int pos) throws TextParserException {
            if (args.startsWith("#")) {
                String hex = args.substring(1);
                int rgb;
                try {
                    rgb = Integer.parseUnsignedInt(hex, 16);
                } catch (NumberFormatException e) {
                    throw new TextParserException("Invalid hex color", pos + 1);
                }
                int r, g, b;
                if (hex.length() == 3) {
                    // CSS-style
                    r = rgb & 0xF;
                    r += r << 4;
                    g = (rgb >> 4) & 0xF;
                    g += g << 4;
                    b = (rgb >> 8) & 0xF;
                    b += b << 4;
                } else if (hex.length() == 6) {
                    // standard
                    r = rgb & 0xFF;
                    g = (rgb >> 8) & 0xFF;
                    b = (rgb >> 16) & 0xFF;
                } else {
                    throw new TextParserException("Invalid hex color, should be 6 or 3 characters long", pos + 1);
                }
                return new HexColorModifier(new Color(r | g << 8 | b << 16, false));
            } else {
                try {
                    return new ColorIdModifier(Integer.parseUnsignedInt(args));
                } catch (NumberFormatException e) {
                    throw new TextParserException("Invalid color ID", pos);
                }
            }
        }
    }
}
