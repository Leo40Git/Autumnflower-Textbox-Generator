package adudecalledleo.aftbg.text.modifier;

import java.util.HashMap;
import java.util.Map;

public final class ModifierRegistry {
    private final Map<Character, ModifierParser> map;

    public ModifierRegistry() {
        map = new HashMap<>();

        register('c', new ColorModifierNode.Parser());
    }

    public void register(char c, ModifierParser parser) {
        if (map.put(c, parser) != null) {
            throw new IllegalStateException("Tried to register modifier with already-used key '" + c + "'!");
        }
    }

    public ModifierParser get(char c) {
        return map.get(c);
    }
}
