package adudecalledleo.aftbg.app.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public final class DialogUtils {
    public static final FileNameExtensionFilter FILTER_IMAGE_FILES
            = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
    public static final FileNameExtensionFilter FILTER_JSON_FILES
            = new FileNameExtensionFilter("JSON files", "json");

    private static final JFileChooser FC_OPEN = createFileChooser(), FC_SAVE = createFileChooser();

    private static JFileChooser createFileChooser() {
        var fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
        return fc;
    }

    public static File fileOpenDialog(Component parent, String title, FileNameExtensionFilter filter) {
        FC_OPEN.setDialogTitle(title);
        FC_OPEN.setFileFilter(filter);
        final int ret = FC_OPEN.showOpenDialog(parent);
        if (ret == JFileChooser.APPROVE_OPTION)
            return FC_OPEN.getSelectedFile();
        return null;
    }

    public static File fileSaveDialog(Component parent, String title, FileNameExtensionFilter filter) {
        FC_SAVE.setDialogTitle(title);
        FC_SAVE.setFileFilter(filter);
        final int ret = FC_SAVE.showSaveDialog(parent);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File sel = FC_SAVE.getSelectedFile();
            String selName = sel.getName();
            String ext = filter.getExtensions()[0];
            if (!selName.contains(".")
                    || !selName.substring(selName.lastIndexOf(".") + 1).equalsIgnoreCase(ext)) {
                selName += "." + ext;
                sel = new File(sel.getParentFile().getPath() + "/" + selName);
            }
            return sel;
        }
        return null;
    }

}

