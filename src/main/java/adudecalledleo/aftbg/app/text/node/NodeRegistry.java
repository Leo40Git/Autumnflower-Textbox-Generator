package adudecalledleo.aftbg.app.text.node;

import java.util.HashMap;
import java.util.Map;

import adudecalledleo.aftbg.app.text.node.color.ColorNode;
import adudecalledleo.aftbg.app.text.node.gimmick.GimmickNode;
import adudecalledleo.aftbg.app.text.node.style.*;
import org.jetbrains.annotations.Nullable;

public final class NodeRegistry {
    private static final Map<String, NodeHandler<?>> HANDLERS;

    static {
        HANDLERS = new HashMap<>();
        register(Document.NAME, Document.HANDLER);
        register(TextNode.NAME, TextNode.HANDLER);
        register(BoldNode.NAME, BoldNode.HANDLER);
        register(ItalicNode.NAME, ItalicNode.HANDLER);
        register(UnderlineNode.NAME, UnderlineNode.HANDLER);
        register(StrikethroughNode.NAME, StrikethroughNode.HANDLER);
        register(SuperscriptNode.NAME, SuperscriptNode.HANDLER);
        register(SubscriptNode.NAME, SubscriptNode.HANDLER);
        register(ColorNode.NAME, ColorNode.HANDLER);
        register(StyleNode.NAME, StyleNode.HANDLER);
        register(GimmickNode.NAME, GimmickNode.HANDLER);
    }

    public static void init() { /* <clinit> */ }

    public static void register(String name, NodeHandler<?> handler) {
        if (HANDLERS.containsKey(name)) {
            throw new IllegalArgumentException("Node \"" + name + "\" is already registered");
        }
        HANDLERS.put(name, handler);
    }

    public static @Nullable NodeHandler<?> getHandler(String name) {
        return HANDLERS.get(name);
    }

    private NodeRegistry() { }
}
