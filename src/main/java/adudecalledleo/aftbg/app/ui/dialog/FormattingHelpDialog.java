package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.ui.util.StyledHTMLEditorKit;

public final class FormattingHelpDialog extends JDialog {
    public FormattingHelpDialog(Component owner) {
        super(ModalDialog.getWindowAncestor(owner), ModalityType.MODELESS);
        setTitle("Formatting Help");
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        ContentPane contentPane;
        setContentPane(contentPane = new ContentPane());
        getRootPane().setDefaultButton(contentPane.btnOK);
        pack();
    }

    private final class ContentPane extends JPanel implements ActionListener {
        private final JButton btnOK;

        public ContentPane() {
            super(new BorderLayout());

            btnOK = new JButton("OK");
            btnOK.addActionListener(this);

            JTextPane mainPane = new JTextPane();
            mainPane.setEditable(false);
            mainPane.setEditorKit(new StyledHTMLEditorKit());
            mainPane.setText(AppResources.getFormattingHelpContents());

            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            add(new JScrollPane(mainPane), BorderLayout.CENTER);
            add(btnOK, BorderLayout.PAGE_END);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnOK) {
                FormattingHelpDialog.this.setVisible(false);
            }
        }
    }
}
