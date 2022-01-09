package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.*;
import javax.swing.filechooser.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.SizedByteArray;
import adudecalledleo.aftbg.logging.Logger;

public final class AnimatedPreviewDialog extends ModalDialog {
    private final SizedByteArray imageData;
    private final Image image;

    public AnimatedPreviewDialog(Component owner, SizedByteArray imageData) {
        super(owner);
        this.imageData = imageData;
        image = Toolkit.getDefaultToolkit().createImage(imageData.bytes(), 0, imageData.size());
        setIconImage(AppResources.Icons.PREVIEW.getAsImage());
        setTitle("Preview generated animation");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
                    try {
                        Files.delete(sel.toPath());
                    } catch (IOException ex) {
                        Logger.error("Error while deleting file!", ex);
                        DialogUtils.showErrorDialog(this,
                                "Could not delete file.",
                                "Could not overwrite file.");
                        return;
                    }
                }
                try (FileOutputStream out = new FileOutputStream(sel)) {
                    out.write(imageData.bytes(), 0, imageData.size());
                } catch (IOException ex) {
                    Logger.error("Error while saving animation!", ex);
                    DialogUtils.showErrorDialog(this,
                            "An exception occurred while saving the animation:\n" + e,
                            "Couldn't save animation!");
                    return;
                }
                JOptionPane.showMessageDialog(this,
                        "Successfully saved the animation to:\n" + sel.getAbsolutePath(),
                        "Success!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
