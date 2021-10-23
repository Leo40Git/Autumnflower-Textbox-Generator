package adudecalledleo.aftbg.app.model;

import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FaceCategory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class FaceCBModel extends AbstractListModel<Face> implements ComboBoxModel<Face> {
    private final List<Face> faces;
    private int selected;

    public FaceCBModel() {
        faces = new ArrayList<>();
        selected = -1;
    }

    public void update(FaceCategory cat) {
        faces.clear();
        faces.addAll(cat.getFaces().values());
        if (faces.isEmpty()) {
            selected = -1;
        } else {
            final int size = faces.size();
            if (selected < 0 || selected >= size) {
                selected = size - 1;
            }
        }
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public Face getSelectedItem() {
        if (selected < 0) {
            return null;
        } else {
            return faces.get(selected);
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem == null) {
            selected = -1;
            fireContentsChanged(this, -1, -1);
            return;
        }
        @SuppressWarnings("SuspiciousMethodCalls") int index = faces.indexOf(anItem);
        if (index >= 0) {
            selected = index;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public int getSize() {
        return faces.size();
    }

    @Override
    public Face getElementAt(int index) {
        return faces.get(index);
    }
}
