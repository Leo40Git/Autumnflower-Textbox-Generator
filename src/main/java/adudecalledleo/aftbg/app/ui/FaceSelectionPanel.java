package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.*;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.ui.render.FaceCategoryListCellRenderer;
import adudecalledleo.aftbg.app.ui.render.FaceListCellRenderer;

public final class FaceSelectionPanel extends JPanel implements ItemListener, GameDefinitionUpdateListener {
    private static final FacePool INITIAL_FACE_POOL = new FacePool();

    private final Consumer<Face> faceUpdateListener;
    private final JComboBox<FaceCategory> catSel;
    private final JList<Face> faceDisplay;
    private final DefaultComboBoxModel<FaceCategory> catModel;
    private final DefaultListModel<Face> faceDisplayModel;

    private FacePool facePool;
    private Face selectedFace;

    public FaceSelectionPanel(Consumer<Face> faceUpdateListener) {
        this.faceUpdateListener = faceUpdateListener;

        selectedFace = Face.NONE;

        catSel = new JComboBox<>();
        faceDisplay = new JList<>();

        catModel = new DefaultComboBoxModel<>();
        faceDisplayModel = new DefaultListModel<>();

        catSel.setModel(catModel);
        catSel.setRenderer(new FaceCategoryListCellRenderer());
        faceDisplay.setModel(faceDisplayModel);
        faceDisplay.setCellRenderer(new FaceListCellRenderer(FaceListCellRenderer.Mode.LIST_SIMPLE));
        faceDisplay.setSelectionModel(new NoSelectionModel());
        faceDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (faceDisplay.isEnabled()) {
                    openPopup();
                }
            }
        });

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

    private void openPopup() {
        new SelectionPopup((FaceCategory) catModel.getSelectedItem())
                .show(faceDisplay, 0, faceDisplay.getHeight() + faceDisplay.getInsets().bottom);
    }

    private void updateCategoriesModel(FacePool pool) {
        int selected = Math.max(catSel.getSelectedIndex(), 0);

        catModel.removeAllElements();
        catModel.addAll(pool.getCategories().values());

        catSel.setSelectedIndex(Math.min(catModel.getSize() - 1, selected));
    }

    private void updateSelectedFace(FaceCategory cat) {
        /*
        int selected = Math.max(faceDisplay.getSelectedIndex(), 0);

        faceDisplayModel.removeAllElements();
        faceDisplayModel.addAll(cat.getFaces().values());

        faceDisplay.setSelectedIndex(Math.min(faceDisplayModel.getSize() - 1, selected));*/
        // TODO
        setFace0(cat.getFaces().values().iterator().next());
    }

    private void setFace0(Face face) {
        selectedFace = face;
        faceDisplayModel.set(0, selectedFace);
        flushChanges();
    }

    public void setFace(Face face) {
        if (facePool != null) {
            var cat = facePool.getCategory(face.getCategory());
            catModel.setSelectedItem(cat);
            faceDisplayModel.set(0, face);
            selectedFace = face;
        }
    }

    public void flushChanges() {
        faceUpdateListener.accept(selectedFace);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        var src = e.getSource();
        if (catSel.equals(src)) {
            if (catModel.getSize() == 0) {
                return;
            }
            var cat = (FaceCategory) catModel.getSelectedItem();
            if (cat == null) {
                catModel.setSelectedItem(FaceCategory.NONE);
                return;
            }
            updateSelectedFace(cat);
            faceDisplay.setEnabled(cat != FaceCategory.NONE);
        }
    }

    private final class SelectionPopup extends JPopupMenu implements ListSelectionListener, MouseMotionListener {
        private final FaceCategory category;

        private final PlaceholderTextField txtSearch;
        private final JList<Face> lstFaces;
        private final DefaultListModel<Face> mdlFaces;
        private final JLabel lblName, lblSource;

        public SelectionPopup(FaceCategory category) {
            this.category = category;

            txtSearch = new PlaceholderTextField();
            txtSearch.setPlaceholder("Search...");

            lstFaces = new JList<>();
            mdlFaces = new DefaultListModel<>();
            mdlFaces.addAll(category.getFaces().values());
            lstFaces.setModel(mdlFaces);
            lstFaces.setCellRenderer(new FaceListCellRenderer(FaceListCellRenderer.Mode.GRID));
            lstFaces.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            lstFaces.setVisibleRowCount(0);
            lstFaces.setSelectedValue(selectedFace, true);
            lstFaces.addListSelectionListener(this);
            lstFaces.addMouseMotionListener(this);

            lblName = new JLabel("Hover over a face...");
            lblSource = new JLabel(" ");

            Box infoBox = new Box(BoxLayout.PAGE_AXIS);
            infoBox.add(lblName);
            infoBox.add(lblSource);

            JScrollPane scrollPane = new JScrollPane(lstFaces);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            var cellSize = lstFaces.getCellRenderer()
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
            // fit on-screen
            size.height += 2;

            scrollPane.setPreferredSize(size);
            scrollPane.setMinimumSize(size);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.add(txtSearch, BorderLayout.PAGE_START);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(infoBox, BorderLayout.PAGE_END);

            add(panel);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            FaceSelectionPanel.this.setFace0(lstFaces.getSelectedValue());
            setVisible(false);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            int i = lstFaces.locationToIndex(p);
            if (i >= 0 && lstFaces.getCellBounds(i, i).contains(p)) {
                Face face = mdlFaces.get(i);
                lblName.setText(face.getName());
                var source = face.getSource();
                if (source == null) {
                    lblSource.setText(" ");
                } else {
                    lblSource.setText("<html><b>From:</b> %s</html>".formatted(face.getSource().qualifiedName()));
                }
            } else {
                lblName.setText("Hover over a face...");
                lblSource.setText(" ");
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) { }
    }
}
