package adudecalledleo.aftbg.app.model;

import adudecalledleo.aftbg.face.FaceCategory;
import adudecalledleo.aftbg.face.FacePool;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class FaceCategoryCBModel extends AbstractListModel<FaceCategory> implements ComboBoxModel<FaceCategory> {
    private final List<FaceCategory> categories;
    private int selected;

    public FaceCategoryCBModel() {
        categories = new ArrayList<>();
        categories.add(FaceCategory.NONE);
        selected = 0;
    }

    public void update(FacePool pool) {
        categories.clear();
        categories.addAll(pool.getCategories().values());
        if (categories.isEmpty()) {
            selected = -1;
        } else {
            if (selected < 0) {
                selected = 0;
            } else {
                final int size = categories.size();
                if (selected >= size) {
                    selected = size - 1;
                }
            }
        }
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public FaceCategory getSelectedItem() {
        if (selected < 0) {
            return null;
        } else {
            return categories.get(selected);
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem == null) {
            selected = -1;
            fireContentsChanged(this, -1, -1);
            return;
        }
        @SuppressWarnings("SuspiciousMethodCalls") int index = categories.indexOf(anItem);
        if (index >= 0) {
            selected = index;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public int getSize() {
        return categories.size();
    }

    @Override
    public FaceCategory getElementAt(int index) {
        return categories.get(index);
    }
}
