package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.ui.dialog.SelectFaceDialog;

public final class SelectFacePanel extends JPanel implements GameDefinitionUpdateListener, MouseListener, ActionListener {
    private static final String TOOLTIP = "Click to select new face...";

    private final JLabel lblIconAndCat, lblSeparator, lblName;
    private final JButton btnChange;

    private FacePool faces;
    private Face selectedFace;

    public SelectFacePanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setOpaque(true);

        lblIconAndCat = new JLabel();
        lblSeparator = new JLabel(" / ");
        lblSeparator.setEnabled(false);
        lblName = new JLabel();
        btnChange = new JButton("...");
        btnChange.setToolTipText(TOOLTIP);

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.LINE_AXIS));
        displayPanel.add(lblIconAndCat);
        displayPanel.add(lblSeparator);
        displayPanel.add(lblName);

        add(displayPanel, BorderLayout.CENTER);
        add(btnChange, BorderLayout.LINE_END);

        addMouseListener(this);
        btnChange.addActionListener(this);
        setSelectedFace(Face.BLANK);

        setPreferredSize(new Dimension(0, 72));
    }

    public Face getSelectedFace() {
        return selectedFace;
    }

    public void setSelectedFace(Face selectedFace) {
        Objects.requireNonNull(selectedFace, "selectedFace");
        var oldSelectedFace = this.selectedFace;
        this.selectedFace = selectedFace;
        if (!this.selectedFace.equals(oldSelectedFace)) {
            lblIconAndCat.setIcon(selectedFace.getIcon());
            lblIconAndCat.setText(selectedFace.getCategory());
            lblName.setText(selectedFace.getName());
            if (selectedFace.isBlank()) {
                lblSeparator.setVisible(false);
                lblName.setVisible(false);
                setToolTipText(TOOLTIP);
            } else {
                lblSeparator.setVisible(true);
                lblName.setVisible(true);
                setToolTipText("<html>%s<br>%s</html>".formatted(selectedFace.toToolTipText(true), TOOLTIP));
            }
            repaint();

            firePropertyChange("selectedFace", oldSelectedFace, selectedFace);
        }
    }

    @Override
    public void updateGameDefinition(GameDefinition gameDef) {
        this.faces = gameDef.faces();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this && e.getButton() == MouseEvent.BUTTON1) {
            e.consume();
            openDialog();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnChange) {
            openDialog();
        }
    }

    private void openDialog() {
        if (faces == null) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
            return;
        }

        var dlg = new SelectFaceDialog(this, faces, selectedFace);
        dlg.setLocationRelativeTo(null);
        Face newFace = dlg.showDialog();
        if (newFace != null) {
            setSelectedFace(newFace);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
}
