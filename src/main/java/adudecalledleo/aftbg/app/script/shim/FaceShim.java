package adudecalledleo.aftbg.app.script.shim;

import adudecalledleo.aftbg.app.face.Face;
import org.graalvm.polyglot.HostAccess;

public final class FaceShim {
    final Face delegate;

    FaceShim(Face delegate) {
        this.delegate = delegate;
    }

    @HostAccess.Export
    public String getName() {
        return delegate.getName();
    }

    @HostAccess.Export
    public String getCategory() {
        return delegate.getCategory();
    }

    @Override
    public String toString() {
        return delegate.getPath();
    }
}
