package adudecalledleo.aftbg.text.modifier;

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
