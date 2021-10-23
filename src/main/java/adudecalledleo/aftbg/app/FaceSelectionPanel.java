package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.app.model.FaceCBModel;
import adudecalledleo.aftbg.app.model.FaceCategoryCBModel;
import adudecalledleo.aftbg.app.render.FaceCBRenderer;
import adudecalledleo.aftbg.app.render.FaceCategoryCBRenderer;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FaceCategory;
import adudecalledleo.aftbg.face.FacePool;

import javax.swing.*;
import java.util.function.Consumer;

public class FaceSelectionPanel extends JPanel {
    private final JComboBox<FaceCategory> catSel;
    private final JComboBox<Face> faceSel;
    private final FaceCategoryCBModel catModel;
    private final FaceCBModel faceModel;

    public FaceSelectionPanel(Consumer<Face> faceUpdateListener) {
        catSel = new JComboBox<>();
        faceSel = new JComboBox<>();

        catModel = new FaceCategoryCBModel();
        faceModel = new FaceCBModel();

        catSel.setModel(catModel);
        catSel.setRenderer(new FaceCategoryCBRenderer());
        faceSel.setModel(faceModel);
        faceSel.setRenderer(new FaceCBRenderer());

        catSel.addItemListener(e -> {
            if (catSel.equals(e.getSource())) {
                var cat = catModel.getSelectedItem();
                if (cat == null) {
                    faceModel.update(FaceCategory.NONE);
                    faceSel.setEnabled(false);
                } else {
                    faceModel.update(cat);
                    faceSel.setEnabled(true);
                }
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

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(catSel);
        add(faceSel);
    }

    public void updateFacePool(FacePool pool) {
        catModel.update(pool);
    }
}
