package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;

import javax.swing.*;

public class MainPanel extends JPanel {
    private final FaceSelectionPanel faceSel;

    public MainPanel(FacePool pool) {
        add(faceSel = new FaceSelectionPanel(this::onFaceChanged));
        faceSel.updateFacePool(pool);
    }

    public void onFaceChanged(Face newFace) {
        System.out.println("Changed to " + newFace.getPath());
    }
}
