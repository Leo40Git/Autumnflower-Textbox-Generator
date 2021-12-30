package adudecalledleo.aftbg.app.dialog.modifier;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import adudecalledleo.aftbg.app.AppResources;

public final class DelayModifierDialog extends ModifierDialog {
    private final ContentPane pane;

    public DelayModifierDialog(Component owner) {
        super(owner);
        setIconImage(AppResources.Icons.MOD_DELAY.getAsImage());
        setTitle("Add delay modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(pane = new ContentPane());
        pack();
        getRootPane().setDefaultButton(pane.btnAdd);
    }

    public Integer showDialog() {
        setVisible(true);
        return pane.delayLength;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        pane.delayLength = null;
    }

    private final class ContentPane extends JPanel implements ActionListener, ChangeListener {
        final JSpinner spnDelayLength;
        final JButton btnCancel, btnAdd;
        final JLabel previewLabel;
        Integer delayLength;

        public ContentPane() {
            delayLength = 1;

            spnDelayLength = new JSpinner(new SpinnerNumberModel(delayLength, 1, null, 1));
            spnDelayLength.addChangeListener(this);
            btnCancel = createBtn("Cancel");
            btnAdd = createBtn("Add");
            previewLabel = new JLabel();
            updatePreviewLabel();

            Box mainBox = new Box(BoxLayout.PAGE_AXIS);
            mainBox.add(new JLabel("Enter delay length in frames:"));
            mainBox.add(Box.createVerticalStrut(2));
            mainBox.add(spnDelayLength);
            mainBox.add(Box.createVerticalStrut(2));
            mainBox.add(new JLabel("Frames are 1/100ths of a second."));
            mainBox.add(previewLabel);

            JPanel btnsPanel = new JPanel();
            btnsPanel.setLayout(new GridLayout(1, 2));
            btnsPanel.add(btnCancel);
            btnsPanel.add(btnAdd);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setLayout(new BorderLayout());
            add(mainBox, BorderLayout.CENTER);
            add(btnsPanel, BorderLayout.PAGE_END);
        }

        private JButton createBtn(String label) {
            var btn = new JButton(label);
            btn.addActionListener(this);
            return btn;
        }

        private void updatePreviewLabel() {
            previewLabel.setText("This will pause the animation for %s seconds.".formatted(delayLength / 100.0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (btnAdd.equals(src)) {
                DelayModifierDialog.this.setVisible(false);
                DelayModifierDialog.this.dispose();
            } else if (btnCancel.equals(src)) {
                delayLength = null;
                DelayModifierDialog.this.setVisible(false);
                DelayModifierDialog.this.dispose();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            delayLength = (Integer) spnDelayLength.getValue();
            updatePreviewLabel();
        }
    }
}
