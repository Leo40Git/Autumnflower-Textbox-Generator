package adudecalledleo.aftbg.app.dialog;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.text.modifier.StyleSpec;
import adudecalledleo.aftbg.util.TriState;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class StyleModifierDialog extends JDialog {
    private final ContentPanel panel;
    
    public StyleModifierDialog(Frame owner, StyleSpec spec) {
        super(owner);
        setIconImage(AppResources.Icons.MOD_STYLE.getAsImage());
        setTitle("Add style modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(panel = new ContentPanel(spec));
        pack();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                panel.spec = null;
            }
        });
        getRootPane().setDefaultButton(panel.btnAdd);
    }

    public StyleSpec showDialog() {
        setVisible(true);
        return panel.spec;
    }

    private static final class ScriptButtonModel extends JToggleButton.ToggleButtonModel {
        public final StyleSpec.Superscript script;

        public ScriptButtonModel(StyleSpec.Superscript script) {
            this.script = script;
        }
    }

    private final class ContentPanel extends JPanel implements ActionListener, ChangeListener {
        private final JCheckBox cbReset, cbBold, cbItalic, cbUnderline, cbStrikethrough;
        private final ButtonGroup bgScript;
        private final JSpinner spnSizeAdjust;
        private final JLabel lblPreview;
        final JButton btnCancel, btnAdd;
        StyleSpec spec;

        public ContentPanel(StyleSpec spec) {
            if (spec == null) {
                spec = StyleSpec.DEFAULT;
            }
            this.spec = spec;

            cbReset = createCB("Reset");
            cbBold = createCB("Bold");
            cbItalic = createCB("Italic");
            cbUnderline = createCB("Underline");
            cbStrikethrough = createCB("Strikethrough");

            bgScript = new ButtonGroup();
            JRadioButton rbSuper = createScriptRB("Superscript", StyleSpec.Superscript.SUPER);
            JRadioButton rbMid = createScriptRB("Regular", StyleSpec.Superscript.MID);
            JRadioButton rbSub = createScriptRB("Subscript", StyleSpec.Superscript.SUB);

            spnSizeAdjust = new JSpinner(new SpinnerNumberModel(0, -4, 4, 1));
            spnSizeAdjust.addChangeListener(this);

            lblPreview = new JLabel("Sample text");
            lblPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblPreview.setAlignmentY(Component.CENTER_ALIGNMENT);
            lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
            lblPreview.setVerticalAlignment(SwingConstants.CENTER);
            var dim = new Dimension(280, 100);
            lblPreview.setMinimumSize(dim);
            lblPreview.setPreferredSize(dim);

            btnCancel = createBtn("Cancel");
            btnAdd = createBtn("Add");

            cbBold.setSelected(spec.isBold());
            cbItalic.setSelected(spec.isItalic());
            cbUnderline.setSelected(spec.isUnderline());
            cbStrikethrough.setSelected(spec.isStrikethrough());
            switch (spec.superscript()) {
                case DEFAULT, MID -> bgScript.setSelected(rbMid.getModel(), true);
                case SUPER -> bgScript.setSelected(rbSuper.getModel(), true);
                case SUB -> bgScript.setSelected(rbSub.getModel(), true);
            }
            spnSizeAdjust.setValue(spec.sizeAdjust());

            JPanel scriptPanel = new JPanel();
            scriptPanel.setLayout(new GridLayout(1, 3));
            scriptPanel.add(rbSuper);
            scriptPanel.add(rbMid);
            scriptPanel.add(rbSub);

            JPanel sizePanel = new JPanel();
            sizePanel.setLayout(new BorderLayout());
            sizePanel.add(new JLabel("Size Adjust: "), BorderLayout.LINE_START);
            sizePanel.add(spnSizeAdjust, BorderLayout.CENTER);

            JPanel settingsPanel = new JPanel();
            settingsPanel.setLayout(new GridLayout(7, 1));
            settingsPanel.add(cbReset);
            settingsPanel.add(cbBold);
            settingsPanel.add(cbItalic);
            settingsPanel.add(cbUnderline);
            settingsPanel.add(cbStrikethrough);
            settingsPanel.add(scriptPanel);
            settingsPanel.add(sizePanel);

            JPanel previewPanel = new JPanel();
            previewPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            previewPanel.add(lblPreview);
            previewPanel.setAlignmentX(CENTER_ALIGNMENT);

            Box mainBox = new Box(BoxLayout.PAGE_AXIS);
            mainBox.add(settingsPanel);
            mainBox.add(Box.createRigidArea(new Dimension(0, 2)));
            mainBox.add(previewPanel);
            mainBox.add(Box.createRigidArea(new Dimension(0, 2)));

            JPanel btnsPanel = new JPanel();
            btnsPanel.setLayout(new GridLayout(1, 2));
            btnsPanel.add(btnCancel);
            btnsPanel.add(btnAdd);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setLayout(new BorderLayout());
            add(mainBox, BorderLayout.CENTER);
            add(btnsPanel, BorderLayout.PAGE_END);

            updatePreviewFont();
        }

        private void updatePreviewFont() {
            lblPreview.setFont(TextRenderer.getStyledFont(spec));
        }

        private JCheckBox createCB(String label) {
            var cb = new JCheckBox(label);
            cb.addActionListener(this);
            return cb;
        }

        private JRadioButton createScriptRB(String label, StyleSpec.Superscript script) {
            var rb = new JRadioButton(label);
            rb.setModel(new ScriptButtonModel(script));
            bgScript.add(rb);
            rb.addActionListener(this);
            return rb;
        }

        private JButton createBtn(String label) {
            var btn = new JButton(label);
            btn.addActionListener(this);
            return btn;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            var src = e.getSource();
            if (btnAdd.equals(src)) {
                StyleModifierDialog.this.setVisible(false);
                StyleModifierDialog.this.dispose();
            } else if (btnCancel.equals(src)) {
                spec = null;
                StyleModifierDialog.this.setVisible(false);
                StyleModifierDialog.this.dispose();
            } else {
                updateSpec();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            updateSpec();
        }

        private void updateSpec() {
            spec = new StyleSpec(
                    cbReset.isSelected(),
                    TriState.fromBoolean(cbBold.isSelected()),
                    TriState.fromBoolean(cbItalic.isSelected()),
                    TriState.fromBoolean(cbUnderline.isSelected()),
                    TriState.fromBoolean(cbStrikethrough.isSelected()),
                    ((ScriptButtonModel) bgScript.getSelection()).script,
                    (Integer) spnSizeAdjust.getValue());
            updatePreviewFont();
        }
    }
}
