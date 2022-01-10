package adudecalledleo.aftbg.app.ui;

import javax.swing.*;

public final class NoSelectionModel extends DefaultListSelectionModel {
    @Override
    public void addSelectionInterval(int index0, int index1) {
        super.setSelectionInterval(-1, -1);
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        super.setSelectionInterval(-1, -1);
    }
}
