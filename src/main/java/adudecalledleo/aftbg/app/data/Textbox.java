package adudecalledleo.aftbg.app.data;

import adudecalledleo.aftbg.app.face.Face;

public final class Textbox {
    private Face face;
    private String text;

    public Textbox(Face face, String text) {
        this.face = face;
        this.text = text;
    }

    public Textbox(Textbox other) {
        this(other.face, other.text);
    }

    public Face getFace() {
        return face;
    }

    public void setFace(Face face) {
        this.face = face;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
