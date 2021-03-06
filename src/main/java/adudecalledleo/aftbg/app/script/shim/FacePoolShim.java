package adudecalledleo.aftbg.app.script.shim;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.face.FacePool;

@SuppressWarnings("unused")
public final class FacePoolShim {
    private final FacePool delegate;

    FacePoolShim(FacePool delegate) {
        this.delegate = delegate;
    }

    public final FaceShim blank = ShimHelpers.FACE_NONE;

    public FaceShim get(String category, String name) {
        FaceCategory cat = delegate.getCategory(category);
        if (cat == null) {
            return null;
        }
        Face face = cat.getFace(name);
        if (face == null) {
            return null;
        }
        return ShimHelpers.wrap(face);
    }
}
