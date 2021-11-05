package adudecalledleo.aftbg.app.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// TODO IMPLEMENT THIS!!!
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

    private final ContentPanel panel;

    public ColorModifierDialog(Frame owner) {
        super(owner);
        setTitle("Add color modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(panel = new ContentPanel(this));
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

    private static final class ContentPanel extends JPanel implements ActionListener {
        private final ColorModifierDialog dialog;
        final JButton btnCancel, btnAdd;
        Result result;

        private ContentPanel(ColorModifierDialog dialog) {
            this.dialog = dialog;

            btnCancel = createBtn("Cancel");
            btnAdd = createBtn("Add");

            JPanel btnsPanel = new JPanel();
            btnsPanel.setLayout(new GridLayout(1, 2));
            btnsPanel.add(btnCancel);
            btnsPanel.add(btnAdd);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setLayout(new BorderLayout());
            add(btnsPanel, BorderLayout.PAGE_END);
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
                result = null;
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
}
