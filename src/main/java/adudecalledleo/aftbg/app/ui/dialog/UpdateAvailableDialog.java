package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;
import adudecalledleo.aftbg.logging.Logger;

public final class UpdateAvailableDialog extends ModalDialog {
    public UpdateAvailableDialog(Component owner, String changelogHtml) {
        super(owner);
        setTitle("Update available!");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(new ContentPane(changelogHtml));
        pack();

        var dim = getSize();
        setSize(Math.max(dim.width, 480), Math.max(dim.height, 300));
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

    private static final class ContentPane extends JPanel implements HyperlinkListener {
        public ContentPane(String changelogHtml) {
            super(new BorderLayout());
            setOpaque(false);

            JTextPane changelogPane = new JTextPane();
            changelogPane.setEditable(false);
            changelogPane.setEditorKit(new UpdateHTMLEditorKit());
            changelogPane.setText(changelogHtml);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                changelogPane.addHyperlinkListener(this);
            }

            JPanel btnPanel = new JPanel(new GridLayout(0, 1, 0, 2));
            btnPanel.add(new JLabel("something something update"));
            btnPanel.add(new JButton("Frickin sweet luis"));

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            add(new JScrollPane(changelogPane), BorderLayout.CENTER);
            add(btnPanel, BorderLayout.PAGE_END);
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
                    DialogUtils.showErrorDialog(this, "Failed to open link in your default parser!", "Browse to link");
                }
            }
        }
    }
}
