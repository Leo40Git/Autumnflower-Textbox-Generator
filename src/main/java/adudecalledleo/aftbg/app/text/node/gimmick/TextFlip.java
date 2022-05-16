package adudecalledleo.aftbg.app.text.node.gimmick;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public enum TextFlip {
    NONE("none", false, false),
    HORIZONTAL("horizontal", true, false, "horiz", "horz", "h"),
    VERTICAL("vertical", false, true, "vert", "v"),
    BOTH("both", true, true, "hv", "h&v");

    private static final Map<String, TextFlip> BY_NAME = new HashMap<>();

    static {
        for (var flip : values()) {
            BY_NAME.put(flip.label, flip);
            for (var alias : flip.aliases) {
                BY_NAME.put(alias, flip);
            }
        }
    }

    public static @Nullable TextFlip getByName(String name) {
        return BY_NAME.get(name);
    }

    private final String label;
    private final String[] aliases;
    private final boolean horizontal, vertical;

    TextFlip(String label, boolean horizontal, boolean vertical, String... aliases) {
        this.label = label;
        this.aliases = aliases;
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public String getLabel() {
        return label;
    }

    public String[] getAliases() {
        return aliases.clone();
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isVertical() {
        return vertical;
    }
}
