package adudecalledleo.aftbg.app.ui.util;

import java.awt.*;
import java.awt.datatransfer.*;

@SuppressWarnings("ClassCanBeRecord")
public final class TransferableImage implements Transferable {
    private final Image i;

    public TransferableImage(Image i) {
        this.i = i;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(DataFlavor.imageFlavor) && i != null) {
            return i;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.imageFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }
}
