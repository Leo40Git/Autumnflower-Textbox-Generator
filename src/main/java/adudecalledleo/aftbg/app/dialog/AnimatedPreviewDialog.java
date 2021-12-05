package adudecalledleo.aftbg.app.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.TransferableImage;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.util.OperatingSystem;

public final class AnimatedPreviewDialog extends JDialog {
    private final byte[] imageData;
    private final Image image;

    public AnimatedPreviewDialog(Frame owner, byte[] imageData) {
        super(owner);
        this.imageData = imageData;
        image = Toolkit.getDefaultToolkit().createImage(imageData);
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
                            "An exception occurred while saving the animation:\nSee \"" + Main.LOG_NAME + "\" for more details.",
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
