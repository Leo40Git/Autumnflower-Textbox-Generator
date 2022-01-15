package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;
import adudecalledleo.aftbg.logging.Logger;
import org.jetbrains.annotations.Nullable;

public final class UpdateAvailableDialog extends ModalDialog {
    public UpdateAvailableDialog(Component owner, String changelogHtml, @Nullable URL dlUrl) {
        super(owner);
        setTitle("Update available!");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(new ContentPane(changelogHtml, dlUrl));
        pack();

        var dim = getSize();
        setSize(Math.max(dim.width, 480), Math.max(dim.height, 400));
    }

    private static final class UpdateHTMLEditorKit extends HTMLEditorKit {
        private final StyleSheet styleSheet;

        public UpdateHTMLEditorKit() {
            styleSheet = AppResources.getUpdateStyleSheet();
        }

        @Override
        public StyleSheet getStyleSheet() {
            return styleSheet;
        }

        @Override
        public void setStyleSheet(StyleSheet s) { }
    }

    private final class ContentPane extends JPanel implements ActionListener, HyperlinkListener {
        private static final String AC_OPEN_DL = "open_dl";
        private static final String AC_CLOSE = "close";

        private final URI dlUri;

        public ContentPane(String changelogHtml, @Nullable URL dlUrl) {
            super(new BorderLayout());
            setOpaque(false);

            final boolean canBrowse = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);

            if (!canBrowse || dlUrl == null) {
                dlUri = null;
            } else {
                URI uri = null;
                try {
                    uri = dlUrl.toURI();
                } catch (URISyntaxException e) {
                    Logger.error("Failed to convert URL to URI for browsing", e);
                }
                dlUri = uri;
            }

            JLabel lblTitle = new JLabel("A new update is available!");
            lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 20));
            lblTitle.setHorizontalAlignment(JLabel.CENTER);

            JTextPane changelogPane = new JTextPane();
            changelogPane.setEditable(false);
            changelogPane.setEditorKit(new UpdateHTMLEditorKit());
            changelogPane.setText(changelogHtml);
            if (canBrowse) {
                changelogPane.addHyperlinkListener(this);
            }

            JPanel btnPanel = new JPanel(new GridLayout(1, 0, 0, 2));
            btnPanel.setOpaque(false);
            if (dlUri != null) {
                btnPanel.add(createBtn("Browse to Download", AC_OPEN_DL));
            }
            btnPanel.add(createBtn("OK", AC_CLOSE));

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            add(lblTitle, BorderLayout.PAGE_START);
            add(new JScrollPane(changelogPane), BorderLayout.CENTER);
            add(btnPanel, BorderLayout.PAGE_END);
        }

        private JButton createBtn(String text, String actionCommand) {
            var btn = new JButton(text);
            btn.addActionListener(this);
            btn.setActionCommand(actionCommand);
            return btn;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case AC_OPEN_DL -> {
                    try {
                        Desktop.getDesktop().browse(dlUri);
                    } catch (IOException ex) {
                        Logger.error("Failed to open link", ex);
                        DialogUtils.showErrorDialog(this, "Failed to open link in your default browser!", "Browse to Download");
                    }
                    UpdateAvailableDialog.this.setVisible(false);
                    UpdateAvailableDialog.this.dispose();
                }
                case AC_CLOSE -> {
                    UpdateAvailableDialog.this.setVisible(false);
                    UpdateAvailableDialog.this.dispose();
                }
            }
        }

        @Override
        public void hyperlinkUpdate(HyperlinkEvent evt) {
            if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                var url = evt.getURL();
                try {
                    var uri = url.toURI();
                    Desktop.getDesktop().browse(uri);
                } catch (URISyntaxException | IOException e) {
                    Logger.error("Failed to open link", e);
                    DialogUtils.showErrorDialog(this, "Failed to open link in your default browser!", "Browse to link");
                }
            }
        }
    }
}
