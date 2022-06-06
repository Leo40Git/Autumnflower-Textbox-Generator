package adudecalledleo.aftbg.app.face;

public class FaceLoadException extends Exception {
    public FaceLoadException() { }

    public FaceLoadException(String message) {
        super(message);
    }

    public FaceLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
