package adudecalledleo.aftbg.app.game.shim;

import java.util.Map;
import java.util.WeakHashMap;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;

public final class ShimHelpers {
    private ShimHelpers() { }

    public static final FaceShim FACE_NONE;

    private static final Map<Face, FaceShim> FACE_SHIM_MAP;

    static {
        FACE_SHIM_MAP = new WeakHashMap<>();

        FACE_NONE = wrap(Face.NONE);
    }

    public static FaceShim wrap(Face face) {
        return FACE_SHIM_MAP.computeIfAbsent(face, FaceShim::new);
    }

    public static Face unwrap(FaceShim shim) {
        return shim.delegate;
    }

    public static FacePoolShim wrap(FacePool pool) {
        return new FacePoolShim(pool);
    }

    public static TextboxShim copy(Textbox box) {
        return new TextboxShim(wrap(box.getFace()), box.getText());
    }
}
