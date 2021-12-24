package adudecalledleo.aftbg.app.game.shim;

import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FaceCategory;
import adudecalledleo.aftbg.face.FacePool;

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
        Face face = cat.get(name);
        if (face == null) {
            return null;
        }
        return ShimHelpers.wrap(face);
    }
}
