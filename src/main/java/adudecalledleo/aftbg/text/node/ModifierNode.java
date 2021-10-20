package adudecalledleo.aftbg.text.node;

import java.util.Arrays;

public non-sealed abstract class ModifierNode extends Node {
    protected final char key;
    protected final Span[] argSpans;

    public ModifierNode(int start, int length, char key, Span... argSpans) {
        super(start, length);
        this.key = key;
        this.argSpans = argSpans;
    }

    public char getKey() {
        return key;
    }

    public Span[] getArgSpans() {
        return argSpans.clone();
    }

    @Override
    public String toString() {
        return "ModifierNode{" +
                "start=" + start +
                ", length=" + length +
                ", key=" + key +
                ", argSpans=" + Arrays.toString(argSpans) +
                '}';
    }
}
