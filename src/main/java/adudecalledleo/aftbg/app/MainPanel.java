package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.app.components.FaceSelectionPanel;
import adudecalledleo.aftbg.app.components.TextboxEditorPane;
import adudecalledleo.aftbg.app.components.TextboxSelectorScrollPane;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.render.TextboxListCellRenderer;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class MainPanel extends JPanel {
    private final List<Textbox> textboxes;
    private int currentTextbox;

    private final JList<Textbox> textboxSelector;
    private final FaceSelectionPanel faceSelection;
    private final TextboxEditorPane editorPane;

    public MainPanel(TextParser textParser, WindowContext winCtx, FacePool pool) {
        faceSelection = new FaceSelectionPanel(this::onFaceChanged);
        faceSelection.updateFacePool(pool);
        editorPane = new TextboxEditorPane(textParser, winCtx, this::onTextUpdated);

        textboxes = new ArrayList<>();
        textboxes.add(new Textbox(Face.NONE, "Hello!"));
        textboxes.add(new Textbox(Face.NONE, "This is a test."));
        textboxes.add(new Textbox(Face.NONE, "blablablablablablablablablablablablablablabla"));
        currentTextbox = 0;

        textboxSelector = new JList<>();
        textboxSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateTextboxSelectorModel();
        textboxSelector.setCellRenderer(new TextboxListCellRenderer(textParser, winCtx));
        textboxSelector.setOpaque(false);

        JPanel listPanel = new JPanel();
        listPanel.add(new TextboxSelectorScrollPane(textboxSelector, winCtx));

        JPanel textboxPanel = new JPanel();
        textboxPanel.setLayout(new BorderLayout());
        textboxPanel.add(faceSelection, BorderLayout.PAGE_START);
        textboxPanel.add(editorPane, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(listPanel, BorderLayout.LINE_START);
        add(textboxPanel, BorderLayout.CENTER);
    }

    private void updateTextboxSelectorModel() {
        DefaultListModel<Textbox> model = new DefaultListModel<>();
        model.addAll(textboxes);
        textboxSelector.setModel(model);
        textboxSelector.setSelectedIndex(currentTextbox);
        textboxSelector.ensureIndexIsVisible(currentTextbox);
    }

    public void onFaceChanged(Face newFace) {
        System.out.println("Changed to " + newFace.getPath());
    }

    public void onTextUpdated(String newText) {
        // NOOP
    }
}
