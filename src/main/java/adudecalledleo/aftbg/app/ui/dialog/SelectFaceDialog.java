package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.ui.FaceGrid;
import adudecalledleo.aftbg.app.ui.LoadAnimLabel;
import adudecalledleo.aftbg.app.ui.PlaceholderTextField;
import adudecalledleo.aftbg.app.ui.render.FaceCategoryListCellRenderer;
import adudecalledleo.aftbg.app.ui.render.FaceSearchListCellRenderer;
import adudecalledleo.aftbg.app.ui.worker.FaceSearchWorker;

public final class SelectFaceDialog extends DialogWithResult<Face> {
    public SelectFaceDialog(Component owner, FacePool faces, Face currentFace) {
        super(owner);
        setTitle("Select face");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        ContentPane pane;
        setContentPane(pane = new ContentPane(faces));
        getRootPane().setDefaultButton(pane.btnOK);
        pack();
        pane.setCurrentFace(currentFace);
    }

    private final class ContentPane extends JPanel implements ActionListener, ListSelectionListener, PropertyChangeListener, DocumentListener {
        private final FacePool faces;

        private final JList<FaceCategory> lstCategories;

        private final JPanel pnlMain;

        private final JPanel pnlSearch;
        private final PlaceholderTextField txtSearch;
        private final Timer searchUpdateTimer;
        private final JButton btnSearchClear;

        private final Box boxNoneCategory;

        private final FaceGrid faceGrid;
        private final JScrollPane faceGridScroller;

        private final LoadAnimLabel lblSearchAnim;
        private final DefaultListModel<Face> mdlFilteredFaces;
        private final JList<Face> lstFilteredFaces;
        private final JScrollPane lstFilteredFacesScroller;
        private final FaceSearchListCellRenderer lstFilteredFacesRenderer;
        private final JLabel lblSearchNoResults;

        final JButton btnOK;
        private final JButton btnCancel;

        private volatile boolean txtSearch_suppressUpdate;
        private FaceSearchWorker currentSearchWorker;

        public ContentPane(FacePool faces) {
            super(new BorderLayout());
            this.faces = faces;

            txtSearch_suppressUpdate = false;

            DefaultListModel<FaceCategory> mdlCategories = new DefaultListModel<>();
            mdlCategories.addElement(FaceCategory.NONE);
            mdlCategories.addAll(faces.getCategories().values());
            lstCategories = new JList<>(mdlCategories);
            lstCategories.setCellRenderer(new FaceCategoryListCellRenderer());
            lstCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            lstCategories.addListSelectionListener(this);

            boxNoneCategory = new Box(BoxLayout.PAGE_AXIS);
            boxNoneCategory.add(new JLabel("Textbox will have no face."));
            boxNoneCategory.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            faceGrid = new FaceGrid();
            faceGrid.addPropertyChangeListener("selectedFace", this);
            faceGridScroller = new JScrollPane(faceGrid);
            faceGridScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            lblSearchAnim = new LoadAnimLabel();
            lblSearchAnim.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
            lblSearchAnim.setHorizontalAlignment(JLabel.CENTER);
            lblSearchAnim.setVerticalAlignment(JLabel.CENTER);

            mdlFilteredFaces = new DefaultListModel<>();
            lstFilteredFaces = new JList<>(mdlFilteredFaces);
            lstFilteredFaces.setCellRenderer(lstFilteredFacesRenderer = new FaceSearchListCellRenderer());
            lstFilteredFaces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            lstFilteredFaces.addListSelectionListener(this);
            lstFilteredFacesScroller = new JScrollPane(lstFilteredFaces);

            lblSearchNoResults = new JLabel("(no results!)");
            lblSearchNoResults.setFont(lblSearchNoResults.getFont().deriveFont(Font.BOLD, 24));
            lblSearchNoResults.setHorizontalAlignment(JLabel.CENTER);
            lblSearchNoResults.setVerticalAlignment(JLabel.CENTER);

            txtSearch = new PlaceholderTextField();
            txtSearch.setPlaceholder("Search by name...");
            txtSearch.getDocument().addDocumentListener(this);
            btnSearchClear = createBtn("x");

            searchUpdateTimer = new Timer(250, this);
            searchUpdateTimer.setRepeats(false);

            btnOK = createBtn("OK");
            btnCancel = createBtn("Cancel");

            pnlSearch = new JPanel(new BorderLayout());
            pnlSearch.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            pnlSearch.add(txtSearch, BorderLayout.CENTER);
            pnlSearch.add(btnSearchClear, BorderLayout.LINE_END);

            pnlMain = new JPanel(new BorderLayout());
            pnlMain.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            pnlMain.add(pnlSearch, BorderLayout.PAGE_START);
            pnlMain.add(faceGridScroller, BorderLayout.CENTER);

            JPanel btnsPanel = new JPanel();
            btnsPanel.setLayout(new GridLayout(1, 2));
            btnsPanel.add(btnCancel);
            btnsPanel.add(btnOK);

            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            add(new JScrollPane(lstCategories), BorderLayout.LINE_START);
            add(pnlMain, BorderLayout.CENTER);
            add(btnsPanel, BorderLayout.PAGE_END);
        }

        public void setCurrentFace(Face currentFace) {
            var cat = faces.getCategory(currentFace.getCategory());
            lstCategories.setSelectedValue(cat, true);
            if (cat != FaceCategory.NONE) {
                faceGrid.setSelectedFace(currentFace, true);
                faceGrid.requestFocusInWindow();
            }
        }

        private JButton createBtn(String text) {
            var btn = new JButton(text);
            btn.addActionListener(this);
            return btn;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            var src = e.getSource();
            if (src == btnOK) {
                if (lstCategories.getSelectedValue() == FaceCategory.NONE) {
                    SelectFaceDialog.this.result = Face.BLANK;
                }
                SelectFaceDialog.this.setVisible(false);
                SelectFaceDialog.this.dispose();
            } else if (src == btnCancel) {
                SelectFaceDialog.this.result = null;
                SelectFaceDialog.this.setVisible(false);
                SelectFaceDialog.this.dispose();
            } else if (src == btnSearchClear) {
                txtSearch_suppressUpdate = true;
                try {
                    txtSearch.setText(null);
                    cancelSearch(false);
                } finally {
                    txtSearch_suppressUpdate = false;
                }
            } else if (src == searchUpdateTimer) {
                var text = txtSearch.getText().trim();
                lstFilteredFaces.setEnabled(false);
                mdlFilteredFaces.clear();
                lstFilteredFacesRenderer.setHighlightedString(text);
                currentSearchWorker = new FaceSearchWorker(lstCategories.getSelectedValue(), text) {
                    @Override
                    protected void process(List<Face> chunks) {
                        if (isCancelled()) {
                            return;
                        }

                        SwingUtilities.invokeLater(() -> {
                            pnlMain.remove(lblSearchAnim);
                            lblSearchAnim.stopAnimating();
                            if (chunks.isEmpty()) {
                                pnlMain.remove(lstFilteredFacesScroller);
                                pnlMain.add(lblSearchNoResults, BorderLayout.CENTER);
                            } else {
                                pnlMain.remove(lblSearchNoResults);
                                mdlFilteredFaces.addAll(chunks);
                                lstFilteredFaces.setSelectedValue(result, true);
                                lstFilteredFaces.setEnabled(true);
                                pnlMain.add(lstFilteredFacesScroller, BorderLayout.CENTER);
                            }
                            pnlMain.validate();
                            pnlMain.repaint();
                        });
                    }
                };
                currentSearchWorker.execute();
                pnlMain.remove(faceGridScroller);
                pnlMain.remove(boxNoneCategory);
                pnlMain.remove(lblSearchNoResults);
                pnlMain.add(lblSearchAnim, BorderLayout.CENTER);
                lblSearchAnim.startAnimating();
                pnlMain.validate();
                pnlMain.repaint();
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            var src = e.getSource();
            if (src == lstCategories) {
                txtSearch_suppressUpdate = true;
                try {
                    txtSearch.setText(null);
                    cancelSearch(true);
                } finally {
                    txtSearch_suppressUpdate = false;
                }
            } else if (src == lstFilteredFaces) {
                var face = lstFilteredFaces.getSelectedValue();
                if (face != null) {
                    result = face;
                }
            }
        }

        private void pnlMain_updateActive(boolean categoryChanged) {
            var cat = lstCategories.getSelectedValue();
            if (cat == FaceCategory.NONE) {
                pnlMain.remove(faceGridScroller);
                pnlMain.add(boxNoneCategory, BorderLayout.CENTER);
                pnlSearch.setVisible(false);
            } else {
                if (categoryChanged) {
                    faceGrid.setCategory(cat);
                    faceGridScroller.setViewportView(faceGrid); // to resync scrollbar
                }
                if (result != null) {
                    faceGrid.setSelectedFace(result, true);
                }
                pnlMain.remove(boxNoneCategory);
                pnlMain.add(faceGridScroller, BorderLayout.CENTER);
                pnlSearch.setVisible(true);
            }
            pnlMain.validate();
            pnlMain.repaint();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == faceGrid && "selectedFace".equals(evt.getPropertyName())) {
                result = faceGrid.getSelectedFace();
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            txtSearch_update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            txtSearch_update();
        }

        private void txtSearch_update() {
            if (txtSearch_suppressUpdate) {
                return;
            }

            txtSearch_suppressUpdate = true;
            try {
                var text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    cancelSearch(false);
                } else {
                    searchUpdateTimer.restart();
                }
            } finally {
                txtSearch_suppressUpdate = false;
            }
        }

        private void cancelSearch(boolean categoryChanged) {
            searchUpdateTimer.stop();
            if (currentSearchWorker != null) {
                currentSearchWorker.cancel(true);
                currentSearchWorker = null;
            }
            pnlMain.remove(lblSearchAnim);
            lblSearchAnim.stopAnimating();
            pnlMain.remove(lstFilteredFacesScroller);
            pnlMain.remove(lblSearchNoResults);
            pnlMain_updateActive(categoryChanged);
        }

        @Override
        public void changedUpdate(DocumentEvent e) { }
    }
}
