package adudecalledleo.aftbg.app.text.modifier;

import java.util.HashMap;
import java.util.Map;

public final class ModifierRegistry {
    private static final Map<Character, ModifierParser> MAP = new HashMap<>();

    private ModifierRegistry() { }

    public static void init() {
        register(ColorModifierNode.KEY, new ColorModifierNode.Parser());
        register(StyleModifierNode.KEY, new StyleModifierNode.Parser());
        register(GimmickModifierNode.KEY, new GimmickModifierNode.Parser());
        register(FaceModifierNode.KEY, new FaceModifierNode.Parser());
        register(DelayModifierNode.KEY, new DelayModifierNode.Parser());
        register(TextSpeedModifierNode.KEY, new TextSpeedModifierNode.Parser());
        register(InterruptModifierNode.KEY, new InterruptModifierNode.Parser());
    }

    public static void register(char c, ModifierParser parser) {
        if (MAP.containsKey(c)) {
            throw new IllegalStateException("Tried to register modifier with already-used key '" + c + "'!");
        }
        MAP.put(c, parser);
    }

    public static ModifierParser get(char c) {
        return MAP.get(c);
    }
}
