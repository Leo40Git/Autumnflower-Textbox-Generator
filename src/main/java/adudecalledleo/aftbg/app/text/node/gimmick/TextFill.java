package adudecalledleo.aftbg.app.text.node.gimmick;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public enum TextFill {
    COLOR("color", "colour"), RAINBOW("rainbow");

    private static final Map<String, TextFill> BY_NAME = new HashMap<>();

    static {
        for (var fill : values()) {
            BY_NAME.put(fill.label, fill);
            for (var alias : fill.aliases) {
                BY_NAME.put(alias, fill);
            }
        }
    }

    public static @Nullable TextFill getByName(String name) {
        return BY_NAME.get(name);
    }

    private final String label;
    private final String[] aliases;

    TextFill(String label, String... aliases) {
        this.label = label;
        this.aliases = aliases;
    }

    public String getLabel() {
        return label;
    }

    public String[] getAliases() {
        return aliases.clone();
    }
}
