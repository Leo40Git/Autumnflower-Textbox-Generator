package adudecalledleo.aftbg.app.ui.dialog.modifier;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

import javax.swing.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.ui.FaceSelectionPanel;

public final class FaceModifierDialog extends ModifierDialog {
    private final ContentPane pane;

    public FaceModifierDialog(Component owner, FacePool facePool, Face face) {
        super(owner);
        setIconImage(AppResources.Icons.MOD_FACE.getAsImage());
        setTitle("Add face modifier");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(pane = new ContentPane(facePool, face));
        pack();
        // HACK to get shit to display properly agh
        setSize(new Dimension(72 * 10 + 48, 72 + 80));
        getRootPane().setDefaultButton(pane.btnAdd);
    }

    public Face showDialog() {
        setVisible(true);
        return pane.face;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        pane.face = null;
    }

    private final class ContentPane extends JPanel implements Consumer<Face>, ActionListener {
        final FacePool facePool;
        final JButton btnCancel, btnAdd;
        Face face;

        public ContentPane(FacePool facePool, Face face) {
            this.facePool = facePool;
            this.face = face;

            btnCancel = createBtn("Cancel");
            btnAdd = createBtn("Add");

            FaceSelectionPanel selPanel = new FaceSelectionPanel(this);
            selPanel.updateFacePool(facePool);
            selPanel.setFace(face);

            JPanel btnsPanel = new JPanel();
            btnsPanel.setLayout(new GridLayout(1, 2));
            btnsPanel.add(btnCancel);
            btnsPanel.add(btnAdd);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setLayout(new BorderLayout());
            add(selPanel, BorderLayout.CENTER);
            add(btnsPanel, BorderLayout.PAGE_END);
        }

        private JButton createBtn(String label) {
            var btn = new JButton(label);
            btn.addActionListener(this);
            return btn;
        }

        @Override
        public void accept(Face face) {
            this.face = face;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (btnAdd.equals(src)) {
                FaceModifierDialog.this.setVisible(false);
                FaceModifierDialog.this.dispose();
            } else if (btnCancel.equals(src)) {
                face = null;
                FaceModifierDialog.this.setVisible(false);
                FaceModifierDialog.this.dispose();
            }
        }
    }
}
