package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.util.TriState;

public record GimmickSpec(boolean reset,
                          TriState rainbow, Flip flip) {
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
            TriState.DEFAULT, Flip.DEFAULT);

    public boolean isRainbow() {
        return rainbow.toBoolean(false);
    }

    public GimmickSpec add(GimmickSpec other) {
        if (other.reset) {
            return other;
        } else {
            Flip f = other.flip;
            if (f == Flip.DEFAULT) {
                f = this.flip;
            }
            return new GimmickSpec(false,
                    this.rainbow.orElse(other.rainbow), f);
        }
    }

    public GimmickSpec difference(GimmickSpec other) {
        if (other.reset) {
            return other;
        } else {
            TriState sRainbow = diffTri(this.rainbow, other.rainbow);
            Flip sFlip = Flip.DEFAULT;
            if (!other.flip.sameAs(this.flip)) {
                sFlip = other.flip;
            }
            return new GimmickSpec(false, sRainbow, sFlip);
        }
    }

    private TriState diffTri(TriState current, TriState target) {
        if (current.toBoolean(false) == target.toBoolean(false)) {
            return TriState.DEFAULT;
        }
        return target;
    }
}
