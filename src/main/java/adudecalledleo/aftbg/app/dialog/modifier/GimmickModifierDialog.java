package adudecalledleo.aftbg.app.dialog.modifier;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.component.WindowBackgroundPanel;
import adudecalledleo.aftbg.app.component.render.BaseListCellRenderer;
import adudecalledleo.aftbg.app.text.TextRenderer;
import adudecalledleo.aftbg.app.text.modifier.GimmickModifierNode;
import adudecalledleo.aftbg.app.text.modifier.GimmickSpec;
import adudecalledleo.aftbg.app.text.node.NodeList;
import adudecalledleo.aftbg.app.text.node.TextNode;
import adudecalledleo.aftbg.window.WindowContext;

public final class GimmickModifierDialog extends ModifierDialog {
    private final ContentPane pane;

    public GimmickModifierDialog(Component owner, WindowContext winCtx, GimmickSpec spec) {
        super(owner);
        setIconImage(AppResources.Icons.MOD_GIMMICK.getAsImage());
        setTitle("Add gimmick modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(pane = new ContentPane(winCtx, spec));
        pack();
        getRootPane().setDefaultButton(pane.btnAdd);
    }

    public GimmickSpec showDialog() {
        setVisible(true);
        return pane.spec;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        pane.spec = null;
    }

    private static final class GimmickEnumListCellRenderer extends BaseListCellRenderer<GimmickSpec.GimmickEnum> {
        @Override
        public Component getListCellRendererComponent(JList<? extends GimmickSpec.GimmickEnum> list,
                                                      GimmickSpec.GimmickEnum value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            updateColors(list, index, isSelected, cellHasFocus);
            setText(value.getLabel());
            return this;
        }
    }

    private final class ContentPane extends JPanel implements ActionListener, ItemListener {
        private final class PreviewPanel extends WindowBackgroundPanel {
            private static final TextNode SAMPLE_TEXT = new TextNode(0, 0, "Sample text");
            private final NodeList nodes;

            public PreviewPanel(WindowContext winCtx) {
                super(winCtx);
                nodes = new NodeList();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                nodes.clear();
                nodes.add(new GimmickModifierNode(0, 0, spec));
                nodes.add(SAMPLE_TEXT);

                var oldFont = g.getFont();
                g.setFont(TextRenderer.DEFAULT_FONT);
                var fm = g.getFontMetrics();
                int width = fm.stringWidth(SAMPLE_TEXT.getContents());
                int height = fm.getMaxAscent();
                g.setFont(oldFont);

                g.setColor(winCtx.getColor(0));
                TextRenderer.draw((Graphics2D) g, nodes,
                        getWidth() / 2 - width / 2, getHeight() / 2 - height / 2);
            }
        }

        private final JCheckBox cbReset;
        private final JComboBox<GimmickSpec.Fill> cbFill;
        private final JComboBox<GimmickSpec.Flip> cbFlip;
        private final PreviewPanel pnlPreview;
        final JButton btnCancel, btnAdd;
        GimmickSpec spec;

        public ContentPane(WindowContext winCtx, GimmickSpec spec) {
            if (spec == null) {
                spec = GimmickSpec.DEFAULT;
            }
            this.spec = spec;

            cbReset = createCB("Reset");
            cbFill = createEnumComboBox(GimmickSpec.Fill.class);
            cbFill.setSelectedItem(spec.fill());
            cbFlip = createEnumComboBox(GimmickSpec.Flip.class);
            cbFlip.setSelectedItem(spec.flip());

            pnlPreview = new PreviewPanel(winCtx);
            pnlPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlPreview.setAlignmentY(Component.CENTER_ALIGNMENT);
            var dim = new Dimension(280, 100);
            pnlPreview.setMinimumSize(dim);
            pnlPreview.setPreferredSize(dim);
            pnlPreview.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));

            btnCancel = createBtn("Cancel");
            btnAdd = createBtn("Add");

            JPanel settingsPanel = new JPanel();
            settingsPanel.setLayout(new GridLayout(5, 1));
            settingsPanel.add(cbReset);
            settingsPanel.add(new JLabel("Fill type:"));
            settingsPanel.add(cbFill);
            settingsPanel.add(new JLabel("Flip type:"));
            settingsPanel.add(cbFlip);

            Box mainBox = new Box(BoxLayout.PAGE_AXIS);
            mainBox.add(settingsPanel);
            mainBox.add(Box.createRigidArea(new Dimension(0, 2)));
            mainBox.add(pnlPreview);
            mainBox.add(Box.createRigidArea(new Dimension(0, 2)));

            JPanel btnsPanel = new JPanel();
            btnsPanel.setLayout(new GridLayout(1, 2));
            btnsPanel.add(btnCancel);
            btnsPanel.add(btnAdd);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setLayout(new BorderLayout());
            add(mainBox, BorderLayout.CENTER);
            add(btnsPanel, BorderLayout.PAGE_END);
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

        private <E extends Enum<E> & GimmickSpec.GimmickEnum> JComboBox<E> createEnumComboBox(Class<E> enumClass) {
            DefaultComboBoxModel<E> model = new DefaultComboBoxModel<>();
            for (var value : enumClass.getEnumConstants()) {
                if (value.isDefault()) {
                    continue;
                }
                model.addElement(value);
            }

            JComboBox<E> box = new JComboBox<>();
            box.setModel(model);
            box.setRenderer(new GimmickEnumListCellRenderer());
            box.addItemListener(this);
            return box;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            var src = e.getSource();
            if (btnAdd.equals(src)) {
                GimmickModifierDialog.this.setVisible(false);
                GimmickModifierDialog.this.dispose();
            } else if (btnCancel.equals(src)) {
                spec = null;
                GimmickModifierDialog.this.setVisible(false);
                GimmickModifierDialog.this.dispose();
            }
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            updateSpec();
        }

        private void updateSpec() {
            spec = new GimmickSpec(cbReset.isSelected(),
                    (GimmickSpec.Fill) cbFill.getSelectedItem(),
                    (GimmickSpec.Flip) cbFlip.getSelectedItem());
            pnlPreview.repaint();
        }
    }
}
