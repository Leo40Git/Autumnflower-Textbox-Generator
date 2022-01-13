package adudecalledleo.aftbg.app.ui.util;

import java.awt.*;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.*;

import adudecalledleo.aftbg.logging.Logger;

public final class DialogUtils {
    private DialogUtils() { }

    public static String logFileInstruction() {
        return "See \"" + Logger.logFile() + "\" for more details.";
    }

    public static void showErrorDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent,
                message + "\n" + logFileInstruction(),
                title, JOptionPane.ERROR_MESSAGE);
    }

    public static int showCustomConfirmDialog(Component parent, Object message, String title,
                                              String[] options, int messageType) {
        return JOptionPane.showOptionDialog(parent, message, title, JOptionPane.DEFAULT_OPTION, messageType,
                null, options, null);
    }

    public static final FileNameExtensionFilter FILTER_IMAGE_FILES
            = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
    public static final FileNameExtensionFilter FILTER_JSON_FILES
            = new FileNameExtensionFilter("JSON files", "json");

    private static final JFileChooser FC_OPEN = createFileChooser(), FC_SAVE = createFileChooser();
    private static final JFileChooser FC_OPEN_FOLDER = createFolderChooser();

    private static JFileChooser createFileChooser() {
        var fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
        return fc;
    }

    private static JFileChooser createFolderChooser() {
        var fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return fc;
    }

    public static File fileOpenDialog(Component parent, String title, FileNameExtensionFilter filter) {
        FC_OPEN.setDialogTitle(title);
        FC_OPEN.setFileFilter(filter);
        final int ret = FC_OPEN.showOpenDialog(parent);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return FC_OPEN.getSelectedFile();
        }
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

    public static File folderOpenDialog(Component parent, String title) {
        FC_OPEN_FOLDER.setDialogTitle(title);
        final int ret = FC_OPEN_FOLDER.showOpenDialog(parent);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return FC_OPEN_FOLDER.getSelectedFile();
        }
        return null;
    }
}

