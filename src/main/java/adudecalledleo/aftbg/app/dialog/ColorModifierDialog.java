package adudecalledleo.aftbg.app.dialog;

import adudecalledleo.aftbg.app.component.WindowBackgroundPanel;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.window.WindowColors;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public final class ColorModifierDialog extends JDialog {
    public sealed static class Result { }

    public static final class WindowResult extends Result {
        private final int index;

        public WindowResult(int index) {
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

    private static final class ColorIconCache {
        private static final Map<Integer, ImageIcon> ICONS = new HashMap<>();

        public static ImageIcon get(Color color) {
            return ICONS.computeIfAbsent(color.getRGB() & 0xFFFFFF, i -> {
                BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
                Graphics g = img.createGraphics();
                g.setColor(color);
                g.fillRect(0, 0, 32, 32);
                g.dispose();
                return new ImageIcon(img);
            });
        }
    }

    private final ContentPanel panel;

    public ColorModifierDialog(Frame owner, WindowContext winCtx) {
        super(owner);
        setTitle("Add color modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(panel = new ContentPanel(this, winCtx));
        pack();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                panel.result = null;
            }
        });
        getRootPane().setDefaultButton(panel.btnAdd);
    }

    public Result showDialog() {
        setVisible(true);
        return panel.result;
    }

    private static final class WindowColorButtonModel extends DefaultButtonModel {
        private final int colorIndex;

        public WindowColorButtonModel(int colorIndex) {
            this.colorIndex = colorIndex;
        }

        public int getColorIndex() {
            return colorIndex;
        }
    }

    private static final class ContentPanel extends JPanel implements ActionListener {
        private final ColorModifierDialog dialog;
        private final WindowContext winCtx;
        final ButtonGroup bgColors;
        final JRadioButton rbCustom;
        final JLabel lblCustomPreview, lblPreview;
        final JButton btnCustom, btnCancel, btnAdd;
        Color customColor;
        Result result;

        private ContentPanel(ColorModifierDialog dialog, WindowContext winCtx) {
            this.dialog = dialog;
            this.winCtx = winCtx;

            customColor = winCtx.getColor(0);
            result = new WindowResult(0);

            bgColors = new ButtonGroup();

            JPanel winPanel = new JPanel();
            winPanel.setLayout(new GridLayout(4, 8));

            for (int i = 0; i < WindowColors.COUNT; i++) {
                JRadioButton rb = new JRadioButton();
                rb.setModel(new WindowColorButtonModel(i));
                rb.addActionListener(this);
                bgColors.add(rb);
                if (i == 0) {
                    bgColors.setSelected(rb.getModel(), true);
                }
                JLabel lbl = new JLabel(ColorIconCache.get(winCtx.getColor(i)));
                int finalI = i;
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        bgColors.setSelected(rb.getModel(), true);
                        result = new WindowResult(finalI);
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
            lblPreview.setFont(TextRenderer.DEFAULT_FONT);
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
            lblCustomPreview.setIcon(ColorIconCache.get(customColor));
        }

        private void updatePreview() {
            if (result instanceof WindowResult winRes) {
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
                dialog.setVisible(false);
                dialog.dispose();
            } else if (btnCancel.equals(src)) {
                result = null;
                dialog.setVisible(false);
                dialog.dispose();
            } else {
                var mdl = bgColors.getSelection();
                if (mdl == rbCustom.getModel()) {
                    result = new ConstantResult(customColor);
                } else if (mdl instanceof WindowColorButtonModel wcbMdl) {
                    result = new WindowResult(wcbMdl.getColorIndex());
                }
                updatePreview();
            }
        }
    }
}