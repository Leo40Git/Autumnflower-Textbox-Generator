package adudecalledleo.aftbg.app.script.shim;

import org.graalvm.polyglot.HostAccess;

@SuppressWarnings("unused")
public final class TextboxShim {
    @HostAccess.Export
    public FaceShim face;
    @HostAccess.Export
    public String text;

    TextboxShim(FaceShim face, String text) {
        this.face = face;
        this.text = text;
    }
}
