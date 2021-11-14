package adudecalledleo.aftbg.app.data;

import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TextboxListSerializer {
    private static final String[] OPTIONS = { "Abort", "Ignore", "Ignore All" };
    private static final int ABORT_OPTION = 0;
    private static final int IGNORE_OPTION = 1;
    private static final int IGNORE_ALL_OPTION = 2;

    private final Component parent;

    public TextboxListSerializer(Component parent) {
        this.parent = parent;
    }

    public static final class ReadCancelledException extends Exception {
        private ReadCancelledException() {
            super();
        }
    }

    public List<Textbox> read(JsonReader in, FacePool facePool) throws IOException, ReadCancelledException {
        boolean ignoreFaceErrors = false;

        in.beginArray();
        ArrayList<Textbox> textboxes = new ArrayList<>();
        int textboxIndex = 1;
        while (in.hasNext()) {
            in.beginObject();
            String facePath = null, text = null;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "face" -> facePath = in.nextString();
                    case "text" -> text = in.nextString();
                    default -> in.skipValue();
                }
            }
            if (facePath == null) {
                throw new IllegalStateException("Textbox missing required value 'face");
            }
            if (text == null) {
                throw new IllegalStateException("Textbox missing required value 'text");
            }
            Face face = facePool.getByPath(facePath);
            if (face == null) {
                if (ignoreFaceErrors) {
                    face = Face.NONE;
                } else {
                    int result = DialogUtils.showCustomConfirmDialog(parent, "Textbox " + textboxIndex
                                    + " specifies a face that isn't currently loaded: \"" + facePath + "\"\n"
                                    + "Select \"Abort\" (or close this dialog box) to stop loading this project file,\n" +
                                    "\"Ignore\" to ignore this error,\n"
                                    + "or \"Ignore All\" to ignore this error and all future errors of this type.\n"
                                    + "Please note that ignoring textboxes with this error will remove their face.",
                            "Missing face", OPTIONS, JOptionPane.ERROR_MESSAGE);
                    switch (result) {
                        case JOptionPane.CLOSED_OPTION:
                        case ABORT_OPTION:
                        default:
                            throw new ReadCancelledException();
                        case IGNORE_ALL_OPTION:
                            ignoreFaceErrors = true;
                        case IGNORE_OPTION:
                            face = Face.NONE;
                            break;
                    }
                }
            }
            in.endObject();

            textboxes.add(new Textbox(face, text));
            textboxIndex++;
        }
        in.endArray();
        return textboxes;
    }

    public void write(List<Textbox> textboxes, JsonWriter out) throws IOException {
        out.beginArray();
        for (Textbox textbox : textboxes) {
            out.beginObject();
            out.name("face");
            out.value(textbox.getFace().getPath());
            out.name("text");
            out.value(textbox.getText().replaceAll(System.lineSeparator(), "\n"));
            out.endObject();
        }
        out.endArray();
    }
}
