package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;

public final class MainPanel extends JPanel {
    private final FaceSelectionPanel faceSel;
    private final TextboxEditorPane editorPane;

    public MainPanel(TextParser textParser, WindowContext winCtx, FacePool pool) {
        faceSel = new FaceSelectionPanel(this::onFaceChanged);
        faceSel.updateFacePool(pool);
        editorPane = new TextboxEditorPane(textParser, winCtx, this::onTextUpdated);

        setLayout(new BorderLayout());
        add(faceSel, BorderLayout.PAGE_START);
        add(editorPane, BorderLayout.CENTER);
    }

    public void onFaceChanged(Face newFace) {
        System.out.println("Changed to " + newFace.getPath());
    }

    public void onTextUpdated(String newText) {
        // NOOP
    }
}
