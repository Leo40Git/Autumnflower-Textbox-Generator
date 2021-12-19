package adudecalledleo.aftbg.app.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.logging.Logger;

public final class AnimatedPreviewDialog extends ModalDialog {
    private final byte[] imageData;
    private final Image image;

    public AnimatedPreviewDialog(Component owner, byte[] imageData) {
        super(owner);
        this.imageData = imageData;
        image = Toolkit.getDefaultToolkit().createImage(imageData);
        setIconImage(AppResources.Icons.PREVIEW.getAsImage());
        setTitle("Preview generated animation");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(new PreviewPanel());
        pack();

        var dim = getSize();
        setSize(new Dimension(dim.width + 24, 182 + 80));
    }

    private final class PreviewPanel extends JPanel implements ActionListener {
        private static final String AC_SAVE = "save";

        private final JLabel previewLabel;
        private Color backgroundColor;

        public PreviewPanel() {
            super();
            backgroundColor = Color.BLACK;

            ImageIcon icon = new ImageIcon(image, "animation preview");
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JPanel previewPanel = new JPanel();
            previewLabel = new JLabel(icon);
            previewLabel.setMinimumSize(new Dimension(0, icon.getIconHeight()));
            previewLabel.setOpaque(true);
            previewLabel.setBackground(backgroundColor);
            previewPanel.add(previewLabel);
            previewPanel.setMinimumSize(new Dimension(icon.getIconWidth(), 0));

            JButton saveButton = new JButton("Save to File");
            saveButton.addActionListener(this);
            saveButton.setActionCommand(AC_SAVE);
            saveButton.setToolTipText("Save this animation as an image");

            add(previewPanel, BorderLayout.CENTER);
            add(saveButton, BorderLayout.PAGE_END);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (AC_SAVE.equals(e.getActionCommand())) {
                File sel = DialogUtils.fileSaveDialog(this, "Save textbox(es) image",
                        new FileNameExtensionFilter("GIF files", "gif"));
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
                try (FileOutputStream out = new FileOutputStream(sel)) {
                    out.write(imageData);
                } catch (IOException ex) {
                    Logger.error("Error while saving animation!", ex);
                    JOptionPane.showMessageDialog(this,
                            "An exception occurred while saving the animation:\nSee \"" + Logger.logFile() + "\" for more details.",
                            "Couldn't save image!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this,
                        "Successfully saved the animation to:\n" + sel.getAbsolutePath(),
                        "Success!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
