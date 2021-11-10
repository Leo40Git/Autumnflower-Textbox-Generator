package adudecalledleo.aftbg.app.dialog;

import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.text.modifier.StyleSpec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// TODO redesign style dialog?
public final class StyleModifierDialog extends JDialog {
    private final ContentPanel panel;
    
    public StyleModifierDialog(Frame owner) {
        super(owner);
        setTitle("Add style modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(panel = new ContentPanel(this));
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

    private static final class ContentPanel extends JPanel implements ActionListener {
        private final StyleModifierDialog dialog;
        final JCheckBox cbBold, cbItalic, cbUnderline, cbStrikethrough, cbSuperscript, cbSubscript;
        final JLabel lblPreview;
        final JButton btnCancel, btnAdd;
        StyleSpec spec;

        public ContentPanel(StyleModifierDialog dialog) {
            this.dialog = dialog;

            cbBold = createCB("Bold");
            cbItalic = createCB("Italic");
            cbUnderline = createCB("Underline");
            cbStrikethrough = createCB("Strikethrough");
            cbSuperscript = createCB("Superscript");
            cbSubscript = createCB("Subscript");
            lblPreview = new JLabel("Sample text");
            btnCancel = createBtn("Cancel");
            btnAdd = createBtn("Add");

            JPanel cbsPanel = new JPanel();
            cbsPanel.setLayout(new GridLayout(6, 1));
            cbsPanel.add(cbBold);
            cbsPanel.add(cbItalic);
            cbsPanel.add(cbUnderline);
            cbsPanel.add(cbStrikethrough);
            cbsPanel.add(cbSuperscript);
            cbsPanel.add(cbSubscript);

            JPanel previewPanel = new JPanel();
            previewPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            previewPanel.add(lblPreview);
            previewPanel.setAlignmentX(CENTER_ALIGNMENT);

            Box mainBox = new Box(BoxLayout.PAGE_AXIS);
            mainBox.add(cbsPanel);
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
            
            spec = StyleSpec.DEFAULT;
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

        private JButton createBtn(String label) {
            var btn = new JButton(label);
            btn.addActionListener(this);
            return btn;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            var src = e.getSource();
            if (btnAdd.equals(src)) {
                dialog.setVisible(false);
                dialog.dispose();
            } else if (btnCancel.equals(src)) {
                spec = null;
                dialog.setVisible(false);
                dialog.dispose();
            } else {
                if (cbSuperscript.equals(src) && cbSuperscript.isSelected()) {
                    cbSubscript.setSelected(false);
                } else if (cbSubscript.equals(src) && cbSubscript.isSelected()) {
                    cbSuperscript.setSelected(false);
                }
                StyleSpec.Superscript superSpec = StyleSpec.Superscript.DEFAULT;
                if (cbSuperscript.isSelected()) {
                    superSpec = StyleSpec.Superscript.SUPER;
                } else if (cbSubscript.isSelected()) {
                    superSpec = StyleSpec.Superscript.SUB;
                }
                /*
                spec = new StyleSpec(
                        cbBold.isSelected(),
                        cbItalic.isSelected(),
                        cbUnderline.isSelected(),
                        cbStrikethrough.isSelected(),
                        superSpec
                );*/
                SwingUtilities.invokeLater(this::updatePreviewFont);
            }
        }
    }
}