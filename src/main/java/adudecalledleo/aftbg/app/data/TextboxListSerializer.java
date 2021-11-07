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
import java.util.Collections;
import java.util.List;

public final class TextboxListSerializer {
    private final Component parent;

    public TextboxListSerializer(Component parent) {
        this.parent = parent;
    }

    public static final class ReadCancelledException extends Exception {
        public ReadCancelledException() {
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
                                    + "Press \"Abort\" (or close this dialog box) to stop loading this project file,\n" +
                                    "\"Ignore\" to ignore this error,\n"
                                    + "or \"Ignore All\" to ignore this error and all future errors of this type.\n"
                                    + "Please note that ignoring textboxes with this error will remove their face.",
                            "Missing face", new String[]{"Abort", "Ignore", "Ignore All"},
                            JOptionPane.ERROR_MESSAGE);
                    switch (result) {
                        case JOptionPane.CLOSED_OPTION:
                        case 0: // Abort
                        default:
                            throw new ReadCancelledException();
                        case 2: // Ignore All
                            ignoreFaceErrors = true;
                        case 1: // Ignore
                            face = Face.NONE;
                            break;
                    }
                }
            }
            in.endObject();

            textboxes.add(new Textbox(face, text));
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
            out.value(textbox.getText());
            out.endObject();
        }
        out.endArray();
    }
}
