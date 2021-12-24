package adudecalledleo.aftbg.app.game.shim;

@SuppressWarnings("unused")
public final class TextboxShim {
    private FaceShim face;
    private String text;

    TextboxShim(FaceShim face, String text) {
        this.face = face;
        this.text = text;
    }

    public FaceShim getFace() {
        return face;
    }

    public void setFace(FaceShim face) {
        this.face = face;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
