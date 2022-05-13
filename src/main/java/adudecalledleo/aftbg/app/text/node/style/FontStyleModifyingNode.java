package adudecalledleo.aftbg.app.text.node.style;

import javax.swing.text.*;

import adudecalledleo.aftbg.app.text.util.FontStyle;

public interface FontStyleModifyingNode {
    FontStyle updateStyle(FontStyle style);
    void updateSwingStyle(MutableAttributeSet style);
}
