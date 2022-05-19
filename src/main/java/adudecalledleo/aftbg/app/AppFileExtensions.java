package adudecalledleo.aftbg.app;

import javax.swing.filechooser.*;

public final class AppFileExtensions {
    private AppFileExtensions() { }

    public static final String GAME_DEFINITION = "gamedef";
    public static final String EXTENSION = "extdef";

    public static final FileFilter FILTER_GAME_DEFINITION =
            new FileNameExtensionFilter("Game definitions", GAME_DEFINITION);
    public static final FileFilter FILTER_EXTENSION =
            new FileNameExtensionFilter("Extensions", EXTENSION);
}
