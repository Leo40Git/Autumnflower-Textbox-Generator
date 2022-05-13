package adudecalledleo.aftbg.app.text.node.style;

import adudecalledleo.aftbg.app.text.util.FontStyle;

@FunctionalInterface
public interface FontStyleModifyingNode {
    FontStyle updateStyle(FontStyle style);
}
