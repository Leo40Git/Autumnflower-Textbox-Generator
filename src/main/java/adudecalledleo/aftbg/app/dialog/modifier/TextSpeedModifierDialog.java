package adudecalledleo.aftbg.app.dialog.modifier;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.worker.TextboxAnimator;

public final class TextSpeedModifierDialog extends ModifierDialog {
    private static int lastTextSpeed = TextboxAnimator.DEFAULT_TEXT_SPEED;

    private final ContentPane pane;

    public TextSpeedModifierDialog(Component owner) {
        super(owner);
        setIconImage(AppResources.Icons.MOD_TEXT_SPEED.getAsImage());
        setTitle("Add text speed modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(pane = new ContentPane());
        pack();
        getRootPane().setDefaultButton(pane.btnAdd);
    }

    public Integer showDialog() {
        setVisible(true);
        return pane.textSpeed;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        pane.textSpeed = null;
    }

    private final class ContentPane extends JPanel implements ActionListener, ChangeListener {
        final JSpinner spnTextSpeed;
        final JButton btnCancel, btnAdd;
        final JLabel previewLabel;
        Integer textSpeed;

        public ContentPane() {
            textSpeed = lastTextSpeed;

            spnTextSpeed = new JSpinner(new SpinnerNumberModel(textSpeed, 0, null, 1));
            spnTextSpeed.addChangeListener(this);
            btnCancel = createBtn("Cancel");
            btnAdd = createBtn("Add");
            previewLabel = new JLabel();
            updatePreviewLabel();

            Box mainBox = new Box(BoxLayout.PAGE_AXIS);
            mainBox.add(new JLabel("Enter new text speed:"));
            mainBox.add(Box.createVerticalStrut(2));
            mainBox.add(spnTextSpeed);
            mainBox.add(Box.createVerticalStrut(2));
            mainBox.add(new JLabel("'Text speed' is the number of frames to wait before appending a character."));
            mainBox.add(new JLabel("Frames are 1/100ths of a second. Default text speed is %d (%s seconds)."
                    .formatted(TextboxAnimator.DEFAULT_TEXT_SPEED, TextboxAnimator.DEFAULT_TEXT_SPEED / 100.0)));
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
            previewLabel.setText("This will make the animation for pause for %s seconds after each character."
                    .formatted(textSpeed / 100.0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (btnAdd.equals(src)) {
                lastTextSpeed = textSpeed;
                TextSpeedModifierDialog.this.setVisible(false);
                TextSpeedModifierDialog.this.dispose();
            } else if (btnCancel.equals(src)) {
                textSpeed = null;
                TextSpeedModifierDialog.this.setVisible(false);
                TextSpeedModifierDialog.this.dispose();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            textSpeed = (Integer) spnTextSpeed.getValue();
            updatePreviewLabel();
        }
    }
}
