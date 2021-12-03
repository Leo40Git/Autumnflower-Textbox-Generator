package adudecalledleo.aftbg.app.dialog;

import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.TransferableImage;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.util.OperatingSystem;

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
        setModal(true);
        setResizable(false);
        setContentPane(new PreviewPanel());
        pack();

        var dim = getSize();
        setSize(new Dimension(dim.width + 24, Math.min(dim.height, 182 * 4 + 40)));
    }

    private final class PreviewPanel extends JPanel implements ActionListener {
        private static final String AC_BGCOLOR = "bgcolor";
        private static final String AC_COPY = "copy";
        private static final String AC_SAVE = "save";

        private final JLabel previewLabel;
        private Color backgroundColor;

        public PreviewPanel() {
            super();
            backgroundColor = Color.BLACK;

            ImageIcon icon = new ImageIcon(image, "textbox(es) preview");
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JPanel previewPanel = new JPanel();
            previewLabel = new JLabel(icon);
            previewLabel.setMinimumSize(new Dimension(0, icon.getIconHeight()));
            previewLabel.setOpaque(true);
            previewLabel.setBackground(backgroundColor);
            previewPanel.add(previewLabel);
            previewPanel.setMinimumSize(new Dimension(icon.getIconWidth(), 0));
            JScrollPane previewScroll = new JScrollPane(previewPanel);
            add(previewScroll, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 3));
            JButton bgcolorButton = new JButton("Change Background Color");
            bgcolorButton.addActionListener(this);
            bgcolorButton.setActionCommand(AC_BGCOLOR);
            bgcolorButton.setToolTipText("<html>Set the preview's background color<br>" +
                    "<b>NOTE:</b> On Windows, this will also set the background color of the image on the clipboard</html>");
            buttonPanel.add(bgcolorButton);
            JButton copyButton = new JButton("Copy to Clipboard");
            copyButton.addActionListener(this);
            copyButton.setActionCommand(AC_COPY);
            copyButton.setToolTipText("<html>Copy these textbox(es) to the clipboard<br>" +
                    "<b>NOTE:</b> The clipboard image might not have transparency!</html>");
            buttonPanel.add(copyButton);
            JButton saveButton = new JButton("Save to File");
            saveButton.addActionListener(this);
            saveButton.setActionCommand(AC_SAVE);
            saveButton.setToolTipText("<html>Save these textbox(es) as an image<br>" +
                    "This image will have transparency</html>");
            saveButton.setPreferredSize(copyButton.getPreferredSize());
            buttonPanel.add(saveButton);
            add(buttonPanel, BorderLayout.PAGE_END);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case AC_BGCOLOR -> {
                    Color newColor = JColorChooser.showDialog(PreviewDialog.this,
                            "Set new background color", backgroundColor, false);
                    if (newColor != null) {
                        backgroundColor = newColor;
                        previewLabel.setBackground(backgroundColor);
                    }
                }
                case AC_COPY -> {
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    if (cb == null) {
                        JOptionPane.showMessageDialog(this,
                                "Java does not support accessing this operating system's clipboard!",
                                "Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
                        break;
                    }

                    BufferedImage imageToCopy = image;
                    if (OperatingSystem.isWindows()) {
                        // in Windows, transparent images aren't supported on the clipboard
                        imageToCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = imageToCopy.createGraphics();
                        g.setBackground(backgroundColor);
                        g.clearRect(0, 0, image.getWidth(), image.getHeight());
                        g.drawImage(image, 0, 0, null);
                        g.dispose();
                    }

                    try {
                        cb.setContents(new TransferableImage(imageToCopy), null);
                    } catch (Exception ex) {
                        Logger.error("Error while copying image to clipboard!", ex);
                        JOptionPane.showMessageDialog(this,
                                "An exception occured while copying the image to the clipboard:\n" + ex,
                                "Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    JOptionPane.showMessageDialog(this, "Successfully copied the image to the clipboard.",
                            "Success!", JOptionPane.INFORMATION_MESSAGE);
                }
                case AC_SAVE -> {
                    File sel = DialogUtils.fileSaveDialog(this, "Save textbox(es) image",
                            new FileNameExtensionFilter("PNG files", "png"));
                    if (sel == null)
                        return;
                    if (sel.exists()) {
                        final int confirm = JOptionPane.showConfirmDialog(this,
                                "File \"" + sel.getName() + "\" already exists?\nOverwrite it?",
                                "Overwrite existing file?",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (confirm != JOptionPane.YES_OPTION)
                            return;
                        if (!sel.delete()) {
                            JOptionPane.showMessageDialog(this, "Could not delete file.",
                                    "Could not overwrite file", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    try {
                        ImageIO.write(image, "png", sel);
                    } catch (IOException ex) {
                        Logger.error("Error while saving image!", ex);
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
