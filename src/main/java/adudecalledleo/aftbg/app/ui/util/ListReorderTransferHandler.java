package adudecalledleo.aftbg.app.ui.util;

import java.awt.datatransfer.*;
import java.awt.dnd.*;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

public final class ListReorderTransferHandler extends TransferHandler {
    public interface ReorderCallback {
        void move(JList<?> source, int oldIndex, int newIndex);
    }

    public static void install(JList<?> list, ReorderCallback callback) {
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        list.setTransferHandler(new ListReorderTransferHandler(list, callback));
        new DragListenerImpl(list);
    }

    private final JList<?> list;
    private final ReorderCallback reorderCallback;

    public ListReorderTransferHandler(JList<?> list, ReorderCallback reorderCallback) {
        this.list = list;
        this.reorderCallback = reorderCallback;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDataFlavorSupported(ListSelection.FLAVOR)) {
            return false;
        }
        if (support.getDropLocation() instanceof JList.DropLocation dl) {
            return dl.getIndex() != -1;
        }
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        Transferable trans = support.getTransferable();
        ListSelection sel;
        try {
            sel = (ListSelection) trans.getTransferData(ListSelection.FLAVOR);
        } catch (Exception e) {
            return false;
        }

        if (sel.list() != list) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(list,
                        "Can't move item to other list!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            });
            return false;
        }

        int index = sel.index;
        if (support.getDropLocation() instanceof JList.DropLocation dl) {
            int dropTargetIndex = dl.getIndex();
            var model = list.getModel();
            final int maxIndex = model.getSize() - 1;
            index = Math.max(0, Math.min(maxIndex, index));
            dropTargetIndex = Math.max(0, Math.min(maxIndex, dropTargetIndex));
            reorderCallback.move(list, index, dropTargetIndex);
            return true;
        }
        return false;
    }

    private static final class DragListenerImpl implements DragSourceListener, DragGestureListener {
        private final JList<?> list;

        private final DragSource ds;

        public DragListenerImpl(JList<?> list) {
            this.list = list;
            ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_MOVE, this);
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            ListSelection sel = ListSelection.from(list);
            ds.startDrag(dge, DragSource.DefaultMoveDrop, sel, this);
        }

        @Override
        public void dragEnter(DragSourceDragEvent dsde) { }

        @Override
        public void dragOver(DragSourceDragEvent dsde) { }

        @Override
        public void dropActionChanged(DragSourceDragEvent dsde) { }

        @Override
        public void dragExit(DragSourceEvent dse) { }

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) { }
    }

    private record ListSelection(JList<?> list, int index) implements Transferable {
        public static final DataFlavor FLAVOR = new DataFlavor(ListSelection.class, "List selection");

        public static ListSelection from(JList<?> list) {
            return new ListSelection(list, list.getSelectedIndex());
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { FLAVOR };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return FLAVOR.equals(flavor);
        }

        @NotNull
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (FLAVOR.equals(flavor)) {
                return this;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }
}
