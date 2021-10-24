package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.app.components.FaceSelectionPanel;
import adudecalledleo.aftbg.app.components.TextboxEditorPane;
import adudecalledleo.aftbg.app.components.TextboxSelectorScrollPane;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.render.TextboxListCellRenderer;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class MainPanel extends JPanel {
    private final List<WindowContextUpdateListener> winCtxUpdateListeners;

    private final List<Textbox> textboxes;
    private int currentTextbox;

    private final JList<Textbox> textboxSelector;
    private final FaceSelectionPanel faceSelection;
    private final TextboxEditorPane editorPane;

    public MainPanel(TextParser textParser) {
        winCtxUpdateListeners = new ArrayList<>();

        textboxes = new ArrayList<>();
        textboxes.add(new Textbox(Face.NONE, ""));
        currentTextbox = 0;

        faceSelection = new FaceSelectionPanel(this::onFaceChanged);
        editorPane = new TextboxEditorPane(textParser, this::onTextUpdated);
        winCtxUpdateListeners.add(editorPane);

        textboxSelector = new JList<>();
        textboxSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateTextboxSelectorModel();
        var renderer = new TextboxListCellRenderer(textParser);
        winCtxUpdateListeners.add(renderer);
        textboxSelector.setCellRenderer(renderer);
        textboxSelector.setOpaque(false);

        JPanel listPanel = new JPanel();
        var scroll = new TextboxSelectorScrollPane(textboxSelector);
        winCtxUpdateListeners.add(scroll);
        listPanel.add(scroll);

        JPanel textboxPanel = new JPanel();
        textboxPanel.setLayout(new BorderLayout());
        textboxPanel.add(faceSelection, BorderLayout.PAGE_START);
        textboxPanel.add(editorPane, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(listPanel, BorderLayout.LINE_START);
        add(textboxPanel, BorderLayout.CENTER);
    }

    public void updateWindowContext(WindowContext winCtx) {
        for (var listener : winCtxUpdateListeners) {
            listener.updateWindowContext(winCtx);
        }
    }

    public void updateGameDefinition(GameDefinition gameDef) {
        faceSelection.updateFacePool(gameDef.getFaces());
    }

    private void updateTextboxSelectorModel() {
        DefaultListModel<Textbox> model = new DefaultListModel<>();
        model.addAll(textboxes);
        textboxSelector.setModel(model);
        textboxSelector.setSelectedIndex(currentTextbox);
        textboxSelector.ensureIndexIsVisible(currentTextbox);
    }

    public void onFaceChanged(Face newFace) {
        textboxes.get(currentTextbox).setFace(newFace);
        updateTextboxSelectorModel();
    }

    public void onTextUpdated(String newText) {
        textboxes.get(currentTextbox).setText(newText);
        updateTextboxSelectorModel();
    }
}
