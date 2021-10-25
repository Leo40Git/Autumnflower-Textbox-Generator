package adudecalledleo.aftbg.app.component;

import adudecalledleo.aftbg.app.model.FaceCBModel;
import adudecalledleo.aftbg.app.model.FaceCategoryCBModel;
import adudecalledleo.aftbg.app.render.FaceListCellRenderer;
import adudecalledleo.aftbg.app.render.FaceCategoryListCellRenderer;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FaceCategory;
import adudecalledleo.aftbg.face.FacePool;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public final class FaceSelectionPanel extends JPanel {
    private final Consumer<Face> faceUpdateListener;
    private final JComboBox<FaceCategory> catSel;
    private final JComboBox<Face> faceSel;
    private final FaceCategoryCBModel catModel;
    private final FaceCBModel faceModel;

    private FacePool facePool;

    public FaceSelectionPanel(Consumer<Face> faceUpdateListener) {
        this.faceUpdateListener = faceUpdateListener;

        catSel = new JComboBox<>();
        faceSel = new JComboBox<>();

        catModel = new FaceCategoryCBModel();
        faceModel = new FaceCBModel();

        catSel.setModel(catModel);
        catSel.setRenderer(new FaceCategoryListCellRenderer());
        faceSel.setModel(faceModel);
        faceSel.setRenderer(new FaceListCellRenderer());

        catSel.addItemListener(e -> {
            if (catSel.equals(e.getSource())) {
                var cat = catModel.getSelectedItem();
                if (cat == null) {
                    catModel.setSelectedItem(FaceCategory.NONE);
                    return;
                }
                faceModel.update(cat);
                faceSel.setEnabled(cat != FaceCategory.NONE);
            }
        });
        faceSel.addItemListener(e -> {
            if (faceSel.equals(e.getSource())) {
                var face = faceModel.getSelectedItem();
                if (face == null) {
                    faceSel.setSelectedIndex(0);
                    return;
                }
                faceUpdateListener.accept(face);
            }
        });

        setLayout(new GridBagLayout());
        var c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.6;
        c.insets.right = 4;
        add(catSel, c);
        c.gridx++;
        c.weightx = 0.4;
        c.insets.right = 0;
        add(faceSel, c);

        catSel.setSelectedItem(FaceCategory.NONE);
        faceSel.setSelectedItem(Face.NONE);
        faceSel.setEnabled(false);
    }

    public void updateFacePool(FacePool pool) {
        this.facePool = pool;
        catModel.update(pool);
    }

    public void setFace(Face face) {
        if (facePool != null) {
            var cat = facePool.getCategory(face.getCategory());
            catModel.setSelectedItem(cat);
            faceModel.setSelectedItem(face);
        }
    }

    public void flushChanges() {
        faceUpdateListener.accept(faceModel.getSelectedItem());
    }
}
