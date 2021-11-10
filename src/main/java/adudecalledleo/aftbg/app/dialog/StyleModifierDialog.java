package adudecalledleo.aftbg.app.dialog;

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
    
    public StyleModifierDialog(Frame owner, StyleSpec spec) {
        super(owner);
        setTitle("Add style modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(panel = new ContentPanel(this, spec));
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
        final JButton btnCancel, btnAdd;
        StyleSpec spec;

        public ContentPanel(StyleModifierDialog dialog, StyleSpec spec) {
            this.dialog = dialog;
            this.spec = spec;

            // TODO

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
                spec = null;
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
}
