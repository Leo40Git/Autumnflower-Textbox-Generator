package adudecalledleo.aftbg.app.script.shim;

import java.util.Map;
import java.util.WeakHashMap;

import adudecalledleo.aftbg.app.Textbox;
import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FacePool;

public final class ShimHelpers {
    private ShimHelpers() { }

    public static final FaceShim FACE_NONE;

    private static final Map<Face, FaceShim> FACE_SHIM_MAP;

    static {
        FACE_SHIM_MAP = new WeakHashMap<>();

        FACE_NONE = wrap(Face.BLANK);
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

    public static void apply(TextboxShim copy, Textbox box) {
        box.setFace(unwrap(copy.face));
        box.setText(copy.text);
    }
}
