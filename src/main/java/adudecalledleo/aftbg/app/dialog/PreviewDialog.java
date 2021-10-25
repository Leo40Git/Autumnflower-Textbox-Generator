package adudecalledleo.aftbg.app.dialog;

import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.TransferableImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class PreviewDialog extends JDialog {
    private final BufferedImage image;

    public PreviewDialog(Frame owner, BufferedImage image) {
        super(owner);
        this.image = image;
        setTitle("Preview generated textbox(es)");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        add(new PreviewPanel());
        pack();
    }

    private final class PreviewPanel extends JPanel implements ActionListener {
        private static final String AC_COPY = "copy";
        private static final String AC_SAVE = "save";

        public PreviewPanel() {
            super();
            ImageIcon icon = new ImageIcon(image, "textbox(es) preview");
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            final JPanel previewPanel = new JPanel();
            final JLabel previewLabel = new JLabel(icon);
            previewLabel.setMinimumSize(new Dimension(0, icon.getIconHeight()));
            previewPanel.add(previewLabel);
            previewPanel.setMinimumSize(new Dimension(icon.getIconWidth(), 0));
            final JScrollPane previewScroll = new JScrollPane(previewPanel);
            add(previewScroll, BorderLayout.CENTER);
            final JPanel buttonPanel = new JPanel();
            JButton copyButton = new JButton("Copy to Clipboard");
            copyButton.addActionListener(this);
            copyButton.setActionCommand(AC_COPY);
            copyButton.setToolTipText("Copy this textbox (or these textboxes) to the clipboard");
            buttonPanel.add(copyButton);
            JButton saveButton = new JButton("Save to File");
            saveButton.addActionListener(this);
            saveButton.setActionCommand(AC_SAVE);
            saveButton.setToolTipText("Save this textbox (or these textboxes) as an image");
            saveButton.setPreferredSize(copyButton.getPreferredSize());
            buttonPanel.add(saveButton);
            add(buttonPanel, BorderLayout.PAGE_END);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case AC_COPY -> {
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    if (cb == null) {
                        JOptionPane.showMessageDialog(this,
                                "Java does not support accessing this operating system's clipboard!",
                                "Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    try {
                        cb.setContents(new TransferableImage(image), null);
                    } catch (Exception ex) {
                        //Main.LOGGER.error("Error while copying image to clipboard!", ex);
                        JOptionPane.showMessageDialog(this,
                                "An exception occured while copying the image to the clipboard:\n" + ex,
                                "Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    JOptionPane.showMessageDialog(this, "Successfully copied the image to the clipboard.", "Success!",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                case AC_SAVE -> {
                    File sel = DialogUtils.fileSaveDialog(this, "Save textbox(es) image",
                            new FileNameExtensionFilter("PNG files", "png"));
                    if (sel == null)
                        return;
                    if (sel.exists()) {
                        final int confirm = JOptionPane.showConfirmDialog(this,
                                "File \"" + sel.getName() + "\" already exists?\nOverwrite it?", "Overwrite existing file?",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (confirm != JOptionPane.YES_OPTION)
                            return;
                        if (!sel.delete()) {
                            JOptionPane.showMessageDialog(this, "Could not delete file.", "Could not overwrite file", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    try {
                        ImageIO.write(image, "png", sel);
                    } catch (IOException ex) {
                        //Main.LOGGER.error("Error while saving image!", ex);
                        JOptionPane.showMessageDialog(this, "An exception occurred while saving the image:\n" + ex,
                                "Couldn't save image!", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    JOptionPane.showMessageDialog(this, "Successfully saved the image to:\n" + sel.getAbsolutePath(),
                            "Success!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
}
