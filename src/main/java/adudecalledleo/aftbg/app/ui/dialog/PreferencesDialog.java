package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppPreferences;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.ui.render.UIColors;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;
import adudecalledleo.aftbg.app.ui.util.UITheme;

public final class PreferencesDialog extends ModalDialog {
    private final ContentPane pane;

    public PreferencesDialog(Component owner) {
        super(owner);
        setIconImage(AppResources.Icons.PREFS.getAsImage());
        setTitle("Preferences");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(pane = new ContentPane());
        pack();
    }

    private final class ContentPane extends JPanel implements ActionListener {
        private final JCheckBox cbCopyCurrentFace, cbAutoUpdateCheck;
        private final JComboBox<UITheme> cbTheme;
        private final JButton btnOK, btnApply, btnCancel;

        public ContentPane() {
            cbCopyCurrentFace = createCb("Copy face from current textbox when creating new textbox",
                    AppPreferences.shouldCopyCurrentFace());
            cbAutoUpdateCheck = createCb("Automatically check for updates on app launch",
                    AppPreferences.isAutoUpdateCheckEnabled());

            DefaultComboBoxModel<UITheme> mdlTheme = new DefaultComboBoxModel<>();
            mdlTheme.addAll(UITheme.getAllThemes());
            cbTheme = new JComboBox<>(mdlTheme);
            cbTheme.setSelectedItem(UITheme.getCurrentTheme());
            cbTheme.setAlignmentX(LEFT_ALIGNMENT);
            cbTheme.setAlignmentY(TOP_ALIGNMENT);

            btnOK = createBtn("OK");
            btnApply = createBtn("Apply");
            btnCancel = createBtn("Cancel");

            Box catGeneral = createCat("General");
            catGeneral.add(cbCopyCurrentFace);
            catGeneral.add(cbAutoUpdateCheck);

            Box catAppearance = createCat("Appearance");
            var lblTheme = new JLabel("Theme:");
            lblTheme.setAlignmentX(LEFT_ALIGNMENT);
            lblTheme.setAlignmentY(TOP_ALIGNMENT);
            catAppearance.add(lblTheme);
            catAppearance.add(cbTheme);

            Box mainBox = new Box(BoxLayout.PAGE_AXIS);
            mainBox.add(catGeneral);
            mainBox.add(catAppearance);

            JPanel btnsPanel = new JPanel(new GridLayout(1, 3));
            btnsPanel.add(btnOK);
            btnsPanel.add(btnApply);
            btnsPanel.add(btnCancel);

            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            add(mainBox, BorderLayout.CENTER);
            add(btnsPanel, BorderLayout.PAGE_END);
        }

        private Box createCat(String title) {
            var cat = new Box(BoxLayout.PAGE_AXIS);
            cat.setBorder(BorderFactory.createTitledBorder(title));
            cat.setAlignmentX(LEFT_ALIGNMENT);
            cat.setAlignmentY(TOP_ALIGNMENT);
            return cat;
        }

        private JCheckBox createCb(String text, boolean selected) {
            var cb = new JCheckBox(text);
            cb.setSelected(selected);
            cb.setAlignmentX(LEFT_ALIGNMENT);
            cb.setAlignmentY(TOP_ALIGNMENT);
            return cb;
        }

        private JButton createBtn(String label) {
            var btn = new JButton(label);
            btn.addActionListener(this);
            return btn;
        }

        private void doApply() {
            AppPreferences.setShouldCopyCurrentFace(cbCopyCurrentFace.isSelected());
            AppPreferences.setAutoUpdateCheckEnabled(cbAutoUpdateCheck.isSelected());

            var newTheme = (UITheme) cbTheme.getSelectedItem();
            if (newTheme != null && UITheme.getCurrentTheme() != newTheme) {
                if (newTheme.apply()) {
                    UIColors.update();
                    SwingUtilities.updateComponentTreeUI(PreferencesDialog.this);
                    SwingUtilities.updateComponentTreeUI(getOwner());
                    PreferencesDialog.this.pack();
                    getOwner().pack();
                } else {
                    DialogUtils.showErrorDialog(this, "Failed to change theme!", "Preferences");
                }
            }
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
