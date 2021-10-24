package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.app.component.FaceSelectionPanel;
import adudecalledleo.aftbg.app.component.TextboxEditorPane;
import adudecalledleo.aftbg.app.component.TextboxSelectorScrollPane;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.PreviewDialog;
import adudecalledleo.aftbg.app.render.TextboxListCellRenderer;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.window.WindowContext;
import adudecalledleo.aftbg.window.WindowText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class MainPanel extends JPanel implements ActionListener {
    private static final String AC_GENERATE = "generate";

    private final TextParser textParser;

    private final List<WindowContextUpdateListener> winCtxUpdateListeners;

    private final List<Textbox> textboxes;
    private int currentTextbox;

    private final JList<Textbox> textboxSelector;
    private final FaceSelectionPanel faceSelection;
    private final TextboxEditorPane editorPane;

    private WindowContext winCtx;

    public MainPanel(TextParser textParser) {
        this.textParser = textParser;

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

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 1));
        var btn = new JButton("Generate");
        btn.setActionCommand(AC_GENERATE);
        btn.addActionListener(this);
        btn.setEnabled(false);
        winCtxUpdateListeners.add(winCtx1 -> btn.setEnabled(true));
        buttonPanel.add(btn);

        JPanel textboxPanel = new JPanel();
        textboxPanel.setLayout(new BorderLayout());
        textboxPanel.add(faceSelection, BorderLayout.PAGE_START);
        textboxPanel.add(editorPane, BorderLayout.CENTER);
        textboxPanel.add(buttonPanel, BorderLayout.PAGE_END);

        setLayout(new BorderLayout());
        add(listPanel, BorderLayout.LINE_START);
        add(textboxPanel, BorderLayout.CENTER);
    }

    public void updateWindowContext(WindowContext winCtx) {
        this.winCtx = winCtx;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case AC_GENERATE -> {
                editorPane.flushChanges(false);
                faceSelection.flushChanges();

                final int textboxCount = textboxes.size();
                var image = new BufferedImage(816, 180 * textboxCount + 2 * (textboxCount - 1), BufferedImage.TYPE_INT_ARGB);
                var g = image.createGraphics();

                boolean success = true;
                for (int i = 0; i < textboxCount; i++) {
                    var textbox = textboxes.get(i);
                    var nodes = textParser.parse(textbox.getText());
                    if (nodes.hasErrors()) {
                        success = false;
                        break;
                    }

                    winCtx.drawBackground(g, 4, 4 + 182 * i, 812, 176, null);
                    winCtx.drawBorder(g, 0, 180 * i, 816, 180, null);
                    g.drawImage(textbox.getFace().getImage(), 18, 18 + 182 * i, null);
                    WindowText.draw(g, nodes, winCtx.getColors(),
                            textbox.getFace().isBlank() ? 18 : 186,
                            21 + 182 * i);
                    winCtx.drawArrow(g, 0, 182 * i, 816, 180, 0, null);
                }

                g.dispose();

                if (success) {
                    var dialog = new PreviewDialog((Frame) SwingUtilities.getWindowAncestor(this), image);
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                } else {
                    // TODO
                }
            }
        }
    }
}
