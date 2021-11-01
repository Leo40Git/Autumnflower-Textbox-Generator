package adudecalledleo.aftbg.app.component;

import adudecalledleo.aftbg.app.render.FaceCategoryListCellRenderer;
import adudecalledleo.aftbg.app.render.FaceListCellRenderer;
import adudecalledleo.aftbg.app.util.ComboBoxFiltering;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FaceCategory;
import adudecalledleo.aftbg.face.FacePool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

public final class FaceSelectionPanel extends JPanel implements ItemListener {
    private final Consumer<Face> faceUpdateListener;
    private final JComboBox<FaceCategory> catSel;
    private final JComboBox<Face> faceSel;
    private final DefaultComboBoxModel<FaceCategory> catModel;
    private final DefaultComboBoxModel<Face> faceModel;

    private FacePool facePool;

    public FaceSelectionPanel(Consumer<Face> faceUpdateListener) {
        this.faceUpdateListener = faceUpdateListener;

        catSel = new JComboBox<>();
        faceSel = new JComboBox<>();

        catModel = new DefaultComboBoxModel<>();
        faceModel = new DefaultComboBoxModel<>();

        catSel.setModel(catModel);
        catSel.setRenderer(new FaceCategoryListCellRenderer());
        faceSel.setModel(faceModel);
        faceSel.setRenderer(new FaceListCellRenderer(false));
        ComboBoxFiltering.install(faceSel);

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
        add(faceSel, c);

        updateFacePool(FacePool.EMPTY);
        catSel.setSelectedItem(FaceCategory.NONE);
        faceSel.setSelectedItem(Face.NONE);
        faceSel.setEnabled(false);

        catSel.addItemListener(this);
        faceSel.addItemListener(this);
    }

    public void updateFacePool(FacePool pool) {
        this.facePool = pool;
        updateCategoriesModel(pool);
    }

    private static int getSelectedIndex(DefaultComboBoxModel<?> model) {
        var selectedObj = model.getSelectedItem();
        int selected;
        if (selectedObj == null) {
            selected = 0;
        } else {
            selected = model.getIndexOf(selectedObj);
        }
        return selected;
    }

    private static void setSelectedIndex(DefaultComboBoxModel<?> model, int selected) {
        model.setSelectedItem(model.getElementAt(Math.min(model.getSize() - 1, selected)));
    }

    private void updateCategoriesModel(FacePool pool) {
        int selected = Math.max(catSel.getSelectedIndex(), 0);

        catModel.removeAllElements();
        catModel.addAll(pool.getCategories().values());

        catSel.setSelectedIndex(Math.min(catModel.getSize() - 1, selected));
    }

    private void updateFacesModel(FaceCategory cat) {
        int selected = Math.max(faceSel.getSelectedIndex(), 0);

        faceModel.removeAllElements();
        faceModel.addAll(cat.getFaces().values());

        faceSel.setSelectedIndex(Math.min(faceModel.getSize() - 1, selected));
    }

    public void setFace(Face face) {
        if (facePool != null) {
            var cat = facePool.getCategory(face.getCategory());
            catModel.setSelectedItem(cat);
            faceModel.setSelectedItem(face);
        }
    }

    private Face getSelectedFace() {
        var selectedItem = faceSel.getSelectedItem();
        if (selectedItem instanceof Face face) {
            return face;
        } else if (selectedItem instanceof String str) {
            var cat = (FaceCategory) catModel.getSelectedItem();
            if (cat == null) {
                return Face.NONE;
            }
            var face = cat.get(str);
            if (face == null) {
                return Face.NONE;
            }
            faceSel.setSelectedItem(face);
            return face;
        }
        return Face.NONE;
    }

    public void flushChanges() {
        faceUpdateListener.accept(getSelectedFace());
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
            updateFacesModel(cat);
            faceSel.setEnabled(cat != FaceCategory.NONE);
        } else if (faceSel.equals(src)) {
            if (faceModel.getSize() == 0) {
                return;
            }
            faceUpdateListener.accept(getSelectedFace());
        }
    }
}
