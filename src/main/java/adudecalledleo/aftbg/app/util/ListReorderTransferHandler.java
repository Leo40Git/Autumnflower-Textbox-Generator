package adudecalledleo.aftbg.app.util;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;

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
    private final String listHash;

    public ListReorderTransferHandler(JList<?> list, ReorderCallback reorderCallback) {
        this.list = list;
        listHash = Integer.toHexString(System.identityHashCode(list));
        this.reorderCallback = reorderCallback;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        if (support.getDropLocation() instanceof JList.DropLocation dl) {
            return dl.getIndex() != -1;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        Transferable sel = support.getTransferable();
        String indexString;
        try {
            indexString = (String) sel.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return false;
        }

        String[] split = indexString.split(":");
        if (split.length != 2) {
            return false;
        }
        if (!listHash.equals(split[1])) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Can't move item to other list!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            });
            return false;
        }
        indexString = split[0];

        int index = Integer.parseInt(indexString);
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
        private final String listHash;

        private final DragSource ds;

        public DragListenerImpl(JList<?> list) {
            this.list = list;
            listHash = Integer.toHexString(System.identityHashCode(list));
            ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_MOVE, this);
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            // listHash is added to prevent accidentally shuffling another list
            StringSelection sel = new StringSelection(list.getSelectedIndex() + ":" + listHash);
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
}
