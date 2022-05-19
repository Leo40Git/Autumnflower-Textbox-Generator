package adudecalledleo.aftbg.app.ui.util;

import javax.swing.text.html.*;

import adudecalledleo.aftbg.app.AppResources;

public final class StyledHTMLEditorKit extends HTMLEditorKit {
    private final StyleSheet styleSheet;

    public StyledHTMLEditorKit() {
        styleSheet = AppResources.getStyleSheet();
    }

    @Override
    public StyleSheet getStyleSheet() {
        return styleSheet;
    }

    @Override
    public void setStyleSheet(StyleSheet s) { }
}
