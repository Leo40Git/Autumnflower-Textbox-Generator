package adudecalledleo.aftbg.text.modifier;

import java.util.Locale;
import java.util.function.Supplier;

import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;
import adudecalledleo.aftbg.util.Pair;

public record GimmickSpec(boolean reset,
                          Fill fill, Flip flip) {
    public enum Fill {
        DEFAULT,
        COLOR,
        RAINBOW;

        public boolean sameAs(Fill other) {
            return this == DEFAULT && other == COLOR
                    || this == COLOR && other == DEFAULT
                    || this == other;
        }
    }

    public enum Flip {
        DEFAULT(false, false),
        NONE(false, false),
        HORIZONTAL(true, false),
        VERTICAL(false, true),
        BOTH(true, true);

        private final boolean horizontal, vertical;

        Flip(boolean horizontal, boolean vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public boolean isHorizontal() {
            return horizontal;
        }

        public boolean isVertical() {
            return vertical;
        }

        public boolean sameAs(Flip other) {
            return this == other || (this.horizontal == other.horizontal && this.vertical == other.vertical);
        }
    }

    public static final GimmickSpec DEFAULT = new GimmickSpec(true,
            Fill.DEFAULT, Flip.DEFAULT);

    public static Pair<GimmickSpec, Span[]> fromModArgs(String errorPrefix, int argsStart, String args, NodeList nodes) {
        boolean reset = false;
        Fill fill = Fill.DEFAULT;
        Flip flip = Flip.DEFAULT;

        String[] parts = args.split(",");
        Span[] spans = new Span[parts.length];
        int ao = 0;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if ("reset".equalsIgnoreCase(part)) {
                reset = true;
                int l = "reset".length();
                spans[i] = new Span(argsStart + ao, l);
                ao += l + 1;
                continue;
            }
            int eIndex = part.indexOf('=');
            if (eIndex < 0) {
                int l = part.length();
                nodes.add(new ErrorNode(argsStart + ao, l,
                        errorPrefix + "Missing = separator between key and value!"));
                spans[i] = new Span(argsStart + ao, l);
                ao += l + 1;
                continue;
            }
            String gKey = part.substring(0, eIndex);
            String value = part.substring(eIndex + 1);
            int prefixLen = gKey.length() + 1;
            int valueLen = value.length();

            final int aoF = ao;
            Supplier<ErrorNode> unkValEF = () -> new ErrorNode(argsStart + aoF + prefixLen, valueLen,
                    errorPrefix + "Unknown value!");

            switch (gKey.toLowerCase(Locale.ROOT)) {
            case "fill" -> {
                switch (value.toLowerCase(Locale.ROOT)) {
                case "default", "color", "colour" -> fill = Fill.COLOR;
                case "rainbow" -> fill = Fill.RAINBOW;
                default -> nodes.add(unkValEF.get());
                }
            }
            case "flip" -> {
                switch (value.toLowerCase(Locale.ROOT)) {
                case "none", "false", "off", "no" -> flip = Flip.NONE;
                case "h", "horz", "horiz", "horizontal" -> flip = Flip.HORIZONTAL;
                case "v", "vert", "vertical" -> flip = Flip.VERTICAL;
                case "both" -> flip = Flip.BOTH;
                default -> nodes.add(unkValEF.get());
                }
            }
            default -> nodes.add(new ErrorNode(argsStart + ao, prefixLen + valueLen,
                    errorPrefix + "Unknown gimmick key '" + gKey + "'!"));
            }
            spans[i] = new Span(argsStart + ao, prefixLen + valueLen);
            ao += prefixLen + valueLen + 1;
        }

        return new Pair<>(new GimmickSpec(reset, fill, flip), spans);
    }

    public GimmickSpec add(GimmickSpec other) {
        if (other.reset) {
            return other;
        } else {
            Fill fill = other.fill;
            if (fill == Fill.DEFAULT) {
                fill = this.fill;
            }
            Flip flip = other.flip;
            if (flip == Flip.DEFAULT) {
                flip = this.flip;
            }
            return new GimmickSpec(false, fill, flip);
        }
    }

    public GimmickSpec difference(GimmickSpec other) {
        if (other.reset) {
            return other;
        } else {
            Fill sFill = Fill.DEFAULT;
            if (!other.fill.sameAs(this.fill)) {
                sFill = other.fill;
            }
            Flip sFlip = Flip.DEFAULT;
            if (!other.flip.sameAs(this.flip)) {
                sFlip = other.flip;
            }
            return new GimmickSpec(false, sFill, sFlip);
        }
    }
}
