package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.*;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.ui.render.FaceCategoryListCellRenderer;
import adudecalledleo.aftbg.app.ui.render.FaceGridCellRenderer;
import adudecalledleo.aftbg.app.ui.render.FaceListCellRenderer;
import adudecalledleo.aftbg.app.ui.render.FaceSearchListCellRenderer;
import adudecalledleo.aftbg.app.ui.util.ComboBoxUtils;
import adudecalledleo.aftbg.app.ui.worker.FaceSearchWorker;

public final class FaceSelectionPanel extends JPanel implements ItemListener, GameDefinitionUpdateListener {
    private static final FacePool INITIAL_FACE_POOL = new FacePool();

    private final Consumer<Face> faceUpdateListener;
    private final JComboBox<FaceCategory> catSel;
    private final JComboBox<Face> faceDisplay;
    private final DefaultComboBoxModel<FaceCategory> catModel;
    private final DefaultComboBoxModel<Face> faceDisplayModel;
    private final SelectionPopup selectionPopup;

    private FacePool facePool;
    private Face selectedFace;

    public FaceSelectionPanel(Consumer<Face> faceUpdateListener) {
        this.faceUpdateListener = faceUpdateListener;

        selectedFace = Face.NONE;

        catSel = new JComboBox<>();
        faceDisplay = new JComboBox<>();

        catModel = new DefaultComboBoxModel<>();
        faceDisplayModel = new DefaultComboBoxModel<>();

        selectionPopup = new SelectionPopup();

        catSel.setModel(catModel);
        catSel.setRenderer(new FaceCategoryListCellRenderer());
        faceDisplay.setModel(faceDisplayModel);
        faceDisplay.setRenderer(new FaceListCellRenderer(false));
        ComboBoxUtils.replacePopup(faceDisplay, selectionPopup);

        setLayout(new GridBagLayout());
        var c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.4;
        c.insets.right = 2;
        add(catSel, c);
        c.gridx++;
        c.weightx = 0.6;
        c.insets.right = 0;
        add(faceDisplay, c);

        updateFacePool(INITIAL_FACE_POOL);
        catSel.setSelectedItem(FaceCategory.NONE);
        faceDisplayModel.addElement(Face.NONE);
        faceDisplay.setEnabled(false);

        catSel.addItemListener(this);
    }

    public void updateFacePool(FacePool pool) {
        this.facePool = pool;
        updateCategoriesModel(pool);
    }

    @Override
    public void updateGameDefinition(GameDefinition gameDef) {
        updateFacePool(gameDef.faces());
    }

    private void updateCategoriesModel(FacePool pool) {
        int selected = Math.max(catSel.getSelectedIndex(), 0);

        catModel.removeAllElements();
        catModel.addAll(pool.getCategories().values());

        int newIndex = Math.min(catModel.getSize() - 1, selected);
        catSel.setSelectedIndex(newIndex);
        selectionPopup.updateCategory(catModel.getElementAt(newIndex));
    }

    private void updateCategory(FaceCategory cat) {
        int selected = Math.max(0, selectionPopup.mdlFaces.indexOf(selectedFace));
        selectionPopup.updateCategory(cat);
        selected = Math.min(selectionPopup.mdlFaces.getSize() - 1, selected);
        setFace0(selectionPopup.mdlFaces.get(selected));
    }

    private void setFace0(Face face) {
        selectedFace = face;
        faceDisplayModel.removeAllElements();
        faceDisplayModel.addElement(selectedFace);
        flushChanges();
    }

    public void setFace(Face face) {
        if (facePool != null) {
            var cat = facePool.getCategory(face.getCategory());
            catModel.setSelectedItem(cat);
            faceDisplayModel.removeAllElements();
            faceDisplayModel.addElement(face);
            selectedFace = face;
        }
    }

    public void flushChanges() {
        faceUpdateListener.accept(selectedFace);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (catModel.getSize() == 0) {
            return;
        }

        var cat = (FaceCategory) catModel.getSelectedItem();
        if (cat == null) {
            catModel.setSelectedItem(FaceCategory.NONE);
            return;
        }

        updateCategory(cat);
        faceDisplay.setEnabled(cat != FaceCategory.NONE);
    }

    private final class SelectionPopup extends JPopupMenu implements ActionListener, DocumentListener, MouseListener, MouseMotionListener {
        private final PlaceholderTextField txtSearch;
        private final JList<Face> lstFaces;
        private final DefaultListModel<Face> mdlFaces;
        private final JLabel lblName, lblSource;
        private final JScrollPane scrollPane;

        private final FaceGridCellRenderer lcrGrid;
        private final FaceSearchListCellRenderer lcrSearchResults;

        private FaceCategory currentCategory;
        private boolean isSearching;
        private FaceSearchWorker currentSearchWorker;

        public SelectionPopup() {
            lcrGrid = new FaceGridCellRenderer();
            lcrSearchResults = new FaceSearchListCellRenderer();

            txtSearch = new PlaceholderTextField();
            txtSearch.setPlaceholder("Search...");
            txtSearch.getDocument().addDocumentListener(this);
            txtSearch.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(0, 0, 2, 0),
                    txtSearch.getBorder()));

            JButton btnClear = new JButton("x");
            btnClear.addActionListener(this);

            lstFaces = new JList<>();
            mdlFaces = new DefaultListModel<>();
            lstFaces.setModel(mdlFaces);
            lstFaces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setupGrid(false);
            lstFaces.addMouseListener(this);
            lstFaces.addMouseMotionListener(this);

            lblName = new JLabel();
            lblSource = new JLabel();

            JPanel searchPanel = new JPanel(new BorderLayout());
            searchPanel.setOpaque(false);
            searchPanel.add(txtSearch, BorderLayout.CENTER);
            searchPanel.add(btnClear, BorderLayout.LINE_END);

            Box infoBox = new Box(BoxLayout.PAGE_AXIS);
            infoBox.add(lblName);
            infoBox.add(lblSource);

            scrollPane = new JScrollPane(lstFaces);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.add(searchPanel, BorderLayout.PAGE_START);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(infoBox, BorderLayout.PAGE_END);

            add(panel);
            resetHover();
        }

        public void updateCategory(FaceCategory cat) {
            this.currentCategory = cat;
            if (isSearching) {
                txtSearch.setText(null);
            } else {
                refreshModel();
                setupGrid(true);
            }
        }

        private void refreshModel() {
            mdlFaces.clear();
            mdlFaces.addAll(currentCategory.getFaces().values());
        }

        private void setupGrid(boolean doResize) {
            lstFaces.setCellRenderer(lcrGrid);
            lstFaces.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            lstFaces.setVisibleRowCount(0);
            if (doResize) {
                // recalculate scroll pane size
                var cellSize = lcrGrid
                        .getListCellRendererComponent(lstFaces, Face.NONE,
                                0, false, false)
                        .getPreferredSize();

                final int columns = 5;
                final int maxRows = 8;

                var size = new Dimension(cellSize.width * columns + scrollPane.getVerticalScrollBar().getWidth(),
                        0);

                // don't ask me why these extra 19 pixels are needed, but this seems to solve rows sometimes breaking
                //  one column before they should
                size.width += 19;

                // calculate height
                int modelSize = Math.min(columns * maxRows, lstFaces.getModel().getSize());
                size.height = cellSize.height * (modelSize / columns);
                if (modelSize % columns > 0) {
                    size.height += cellSize.height;
                }

                // again, for some reason these extra 2 pixels prevent the list from being scrollable if all rows would
                //  fit on-screen
                size.height += 2;

                scrollPane.setPreferredSize(size);
                scrollPane.setMinimumSize(size);

                packRememberingSearchFocus();

                lstFaces.ensureIndexIsVisible(mdlFaces.indexOf(selectedFace));
            }
        }

        private void setupSearchResults() {
            final int maxRows = 8;

            lstFaces.setCellRenderer(lcrSearchResults);
            lstFaces.setLayoutOrientation(JList.VERTICAL);
            lstFaces.setVisibleRowCount(maxRows);

            // recalculate scroll pane size
            var cellSize = lcrSearchResults
                    .getListCellRendererComponent(lstFaces, Face.NONE,
                            0, false, false)
                    .getPreferredSize();
            int modelSize = Math.min(maxRows, lstFaces.getModel().getSize());
            var size = new Dimension(cellSize.width + scrollPane.getVerticalScrollBar().getWidth(),
                    cellSize.height * modelSize);

            // don't ask me why these extra 2 pixels are needed, but this seems to solve the horizontal scrollbar
            //  appearing when it really shouldn't
            size.width += 2;

            scrollPane.setPreferredSize(size);
            scrollPane.setMinimumSize(size);

            packRememberingSearchFocus();
        }

        private void packRememberingSearchFocus() {
            boolean searchFocused = txtSearch.isFocusOwner();
            pack();
            if (searchFocused) {
                txtSearch.requestFocus();
            }
        }

        private void updateSearchQuery() {
            if (currentSearchWorker != null) {
                currentSearchWorker.cancel(true);
                currentSearchWorker = null;
            }

            String query = txtSearch.getText();
            if (query.isEmpty()) {
                refreshModel();
                if (isSearching) {
                    setupGrid(true);
                }
                isSearching = false;
            } else {
                if (!isSearching) {
                    setupSearchResults();
                }
                isSearching = true;
                String queryLC = query.toLowerCase(Locale.ROOT);
                lcrSearchResults.setHighlightedString(queryLC);
                mdlFaces.clear();

                // TODO wait a bit before actually searching (see TextboxEditorPane highlight behavior)
                // TODO add loading animation (swap viewport?)
                currentSearchWorker = new FaceSearchWorker(currentCategory, queryLC, mdlFaces::addAll);
                currentSearchWorker.execute();
            }
        }

        private void resetHover() {
            lstFaces.getSelectionModel().clearSelection();
            lblName.setText("Hover over a face...");
            lblSource.setText(" ");
        }

        @Override
        public void show(Component invoker, int x, int y) {
            resetHover();
            txtSearch.setText(null);
            lcrGrid.setSelectedFace(selectedFace);
            lcrSearchResults.setSelectedFace(selectedFace);
            super.show(invoker, x, y);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            txtSearch.setText(null);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateSearchQuery();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateSearchQuery();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            int i = lstFaces.locationToIndex(p);
            if (i < 0 || !lstFaces.getCellBounds(i, i).contains(p)) {
                resetHover();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point p = e.getPoint();
            int i = lstFaces.locationToIndex(p);
            if (i >= 0 && lstFaces.getCellBounds(i, i).contains(p)) {
                Face face = mdlFaces.get(i);
                FaceSelectionPanel.this.setFace0(face);
                setVisible(false);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            int i = lstFaces.locationToIndex(p);
            if (i >= 0 && lstFaces.getCellBounds(i, i).contains(p)) {
                lstFaces.setSelectedIndex(i);
                Face face = mdlFaces.get(i);
                lblName.setText(face.getName());
                var source = face.getSource();
                if (source == null) {
                    lblSource.setText("<html><b>From:</b> (source == null!)</html>");
                } else {
                    lblSource.setText("<html><b>From:</b> %s</html>".formatted(face.getSource().qualifiedName()));
                }
            } else {
                resetHover();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseMoved(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            resetHover();
        }

        @Override
        public void mouseClicked(MouseEvent e) { }

        @Override
        public void mouseEntered(MouseEvent e) { }

        @Override
        public void changedUpdate(DocumentEvent e) { }
    }
}
