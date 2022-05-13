package adudecalledleo.aftbg.app.text.node;

import java.util.HashMap;
import java.util.Map;

import adudecalledleo.aftbg.app.text.node.color.ColorNode;
import adudecalledleo.aftbg.app.text.node.style.BasicStyleNodes;
import adudecalledleo.aftbg.app.text.node.style.StyleNode;
import org.jetbrains.annotations.Nullable;

public final class NodeRegistry {
    private static final NodeRegistry DEFAULT;

    static {
        DEFAULT = new NodeRegistry();
        DEFAULT.registerDefaults();
        DEFAULT.freeze();
    }

    public static NodeRegistry getDefault() {
        return DEFAULT;
    }

    private Map<String, NodeHandler<?>> handlers;
    private boolean frozen;

    public NodeRegistry() {
        this.handlers = new HashMap<>();
        this.frozen = false;
        register(Document.NAME, Document.HANDLER);
        register(TextNode.NAME, TextNode.HANDLER);
    }

    public void registerDefaults() {
        BasicStyleNodes.register(this);
        ColorNode.register(this);
        StyleNode.register(this);
    }

    public void register(String name, NodeHandler<?> handler) {
        if (frozen) {
            throw new IllegalStateException("This registry is frozen!");
        }

        if (handlers.containsKey(name)) {
            throw new IllegalArgumentException("Node \"" + name + "\" is already registered");
        }
        handlers.put(name, handler);
    }

    public @Nullable NodeHandler<?> getHandler(String name) {
        return handlers.get(name);
    }

    public void freeze() {
        handlers = Map.copyOf(handlers);
        frozen = true;
    }
}
