package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.text.DOMRenderer;
import adudecalledleo.aftbg.app.ui.WindowBackgroundPanel;
import adudecalledleo.aftbg.app.ui.util.ColorIcon;
import adudecalledleo.aftbg.window.WindowContext;
import adudecalledleo.aftbg.window.WindowPalette;

public final class SelectColorDialog extends DialogWithResult<SelectColorDialog.Result> {
    private static Result lastResult = null;

    public sealed static class Result { }

    public static final class PaletteResult extends Result {
        private final int index;

        public PaletteResult(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static final class ConstantResult extends Result {
        private final Color color;

        public ConstantResult(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    private final ContentPane pane;

    public SelectColorDialog(Component owner, WindowContext winCtx) {
        super(owner);
        setIconImage(AppResources.Icons.TOOLBAR_COLOR.getAsImage());
        setTitle("Add color modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(pane = new ContentPane(winCtx));
        pack();
        getRootPane().setDefaultButton(pane.btnAdd);
    }

    private static final class WindowColorButtonModel extends JToggleButton.ToggleButtonModel {
        private final int colorIndex;

        public WindowColorButtonModel(int colorIndex) {
            this.colorIndex = colorIndex;
        }

        public int getColorIndex() {
            return colorIndex;
        }
    }

    private final class ContentPane extends JPanel implements ActionListener {
        private final WindowContext winCtx;
        final ButtonGroup bgColors;
        final JRadioButton rbCustom;
        final JLabel lblCustomPreview, lblPreview;
        final JButton btnCustom, btnCancel, btnAdd;
        Color customColor;

        private ContentPane(WindowContext winCtx) {
            this.winCtx = winCtx;

            customColor = winCtx.getColor(0);
            result = new PaletteResult(0);

            bgColors = new ButtonGroup();

            JPanel winPanel = new JPanel();
            winPanel.setLayout(new GridLayout(4, 8));

            int selectedI = 0;
            if (lastResult instanceof PaletteResult wr) {
                result = lastResult;
                selectedI = wr.getIndex();
            } else if (lastResult instanceof ConstantResult) {
                selectedI = -1;
            }

            for (int i = 0; i < WindowPalette.COUNT; i++) {
                JRadioButton rb = new JRadioButton();
                rb.setModel(new WindowColorButtonModel(i));
                rb.addActionListener(this);
                bgColors.add(rb);
                if (i == selectedI) {
                    bgColors.setSelected(rb.getModel(), true);
                }
                JLabel lbl = new JLabel(new ColorIcon(winCtx.getColor(i), 24, 24));
                int finalI = i;
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        bgColors.setSelected(rb.getModel(), true);
                        result = new PaletteResult(finalI);
                        updatePreview();
                    }
                });

                JPanel panel = new JPanel();
                panel.add(rb);
                panel.add(lbl);
                winPanel.add(panel);
            }

            rbCustom = new JRadioButton("");
            rbCustom.addActionListener(this);
            bgColors.add(rbCustom);

            if (lastResult instanceof ConstantResult cr) {
                result = lastResult;
                customColor = cr.getColor();
                bgColors.setSelected(rbCustom.getModel(), true);
            }

            lblCustomPreview = new JLabel();
            lblCustomPreview.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    bgColors.setSelected(rbCustom.getModel(), true);
                    result = new ConstantResult(customColor);
                    updatePreview();
                }
            });
            updateCustomColorPreview();
            btnCustom = createBtn("Custom...");

            JPanel customPanel = new JPanel();
            customPanel.add(rbCustom);
            customPanel.add(lblCustomPreview);
            customPanel.add(btnCustom);
            customPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            lblPreview = new JLabel("Sample text");
            lblPreview.setFont(DOMRenderer.DEFAULT_FONT);
            updatePreview();
            JPanel previewPanel = new WindowBackgroundPanel(winCtx);
            previewPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            previewPanel.add(lblPreview);
            previewPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            Box mainBox = new Box(BoxLayout.PAGE_AXIS);
            mainBox.add(winPanel);
            mainBox.add(customPanel);
            mainBox.add(Box.createRigidArea(new Dimension(0, 2)));
            mainBox.add(previewPanel);
            mainBox.add(Box.createRigidArea(new Dimension(0, 2)));

            btnCancel = createBtn("Cancel");
            btnAdd = createBtn("Add");

            JPanel btnsPanel = new JPanel();
            btnsPanel.setLayout(new GridLayout(1, 2));
            btnsPanel.add(btnCancel);
            btnsPanel.add(btnAdd);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setLayout(new BorderLayout());
            add(mainBox, BorderLayout.CENTER);
            add(btnsPanel, BorderLayout.PAGE_END);
        }

        private void updateCustomColorPreview() {
            lblCustomPreview.setIcon(new ColorIcon(customColor, 24, 24));
        }

        private void updatePreview() {
            if (result instanceof PaletteResult winRes) {
                lblPreview.setForeground(winCtx.getColor(winRes.getIndex()));
            } else if (result instanceof ConstantResult constRes) {
                lblPreview.setForeground(constRes.getColor());
            }
        }

        private JButton createBtn(String label) {
            var btn = new JButton(label);
            btn.addActionListener(this);
            return btn;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            var src = e.getSource();
            if (btnCustom.equals(src)) {
                var newColor = JColorChooser.showDialog(this, "Select new color",
                        customColor, false);
                if (newColor == null) {
                    return;
                }
                customColor = newColor;
                updateCustomColorPreview();
                bgColors.setSelected(rbCustom.getModel(), true);
                result = new ConstantResult(customColor);
                updatePreview();
            } if (btnAdd.equals(src)) {
                lastResult = result;
                SelectColorDialog.this.setVisible(false);
                SelectColorDialog.this.dispose();
            } else if (btnCancel.equals(src)) {
                result = null;
                SelectColorDialog.this.setVisible(false);
                SelectColorDialog.this.dispose();
            } else {
                var mdl = bgColors.getSelection();
                if (mdl == rbCustom.getModel()) {
                    result = new ConstantResult(customColor);
                } else if (mdl instanceof WindowColorButtonModel wcbMdl) {
                    result = new PaletteResult(wcbMdl.getColorIndex());
                }
                updatePreview();
            }
        }
    }
}
