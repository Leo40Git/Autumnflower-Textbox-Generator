package adudecalledleo.aftbg.app.script.shim;

import adudecalledleo.aftbg.app.face.Face;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public final class FaceShim {
    final Face delegate;

    FaceShim(Face delegate) {
        this.delegate = delegate;
    }

    public String getName() {
        return delegate.getName();
    }

    public String getCategory() {
        return delegate.getCategory();
    }

    @Override
    public String toString() {
        return delegate.getPath();
    }
}
