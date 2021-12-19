package adudecalledleo.aftbg.app.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppPreferences;
import adudecalledleo.aftbg.app.AppResources;

public final class PreferencesDialog extends JDialog {
    private ContentPane pane;

    public PreferencesDialog(Frame owner) {
        super(owner);
        setIconImage(AppResources.Icons.PREFS.getAsImage());
        setTitle("Edit preferences");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(pane = new ContentPane());
        pack();
    }

    private final class ContentPane extends JPanel implements ActionListener {
        private final JCheckBox cbAutoUpdateCheck;
        private final JButton btnOK, btnApply, btnCancel;

        public ContentPane() {
            Box mainBox = new Box(BoxLayout.PAGE_AXIS);

            Box catGeneral = new Box(BoxLayout.PAGE_AXIS);
            catGeneral.setBorder(BorderFactory.createTitledBorder("General"));
            cbAutoUpdateCheck = new JCheckBox("Automatically check for updates on app launch");
            cbAutoUpdateCheck.setSelected(AppPreferences.isAutoUpdateCheckEnabled());
            catGeneral.add(cbAutoUpdateCheck);
            mainBox.add(catGeneral);

            JPanel btnsPanel = new JPanel(new GridLayout(1, 3));
            btnOK = createBtn("OK");
            btnsPanel.add(btnOK);
            btnApply = createBtn("Apply");
            btnsPanel.add(btnApply);
            btnCancel = createBtn("Cancel");
            btnsPanel.add(btnCancel);

            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            add(mainBox, BorderLayout.CENTER);
            add(btnsPanel, BorderLayout.PAGE_END);
        }

        private JButton createBtn(String label) {
            var btn = new JButton(label);
            btn.addActionListener(this);
            return btn;
        }

        private void doApply() {
            AppPreferences.setAutoUpdateCheckEnabled(cbAutoUpdateCheck.isSelected());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            var src = e.getSource();
            if (btnOK.equals(src)) {
                doApply();
                PreferencesDialog.this.setVisible(false);
                PreferencesDialog.this.dispose();
            } else if (btnApply.equals(src)) {
                doApply();
            } else if (btnCancel.equals(src)) {
                PreferencesDialog.this.setVisible(false);
                PreferencesDialog.this.dispose();
            }
        }
    }
}
