package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.app.component.FaceSelectionPanel;
import adudecalledleo.aftbg.app.component.TextboxEditorPane;
import adudecalledleo.aftbg.app.component.WindowBackgroundScrollPane;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.PreviewDialog;
import adudecalledleo.aftbg.app.render.TextboxListCellRenderer;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MainPanel extends JPanel implements ActionListener, ListSelectionListener {
    private static final String AC_TEXTBOX_ADD = "textbox.add";
    private static final String AC_TEXTBOX_CLONE = "textbox.clone";
    private static final String AC_TEXTBOX_INSERT_BEFORE = "textbox.insert.before";
    private static final String AC_TEXTBOX_INSERT_AFTER = "textbox.insert.after";
    private static final String AC_TEXTBOX_MOVE_UP = "textbox.move.up";
    private static final String AC_TEXTBOX_MOVE_DOWN = "textbox.move.down";
    private static final String AC_TEXTBOX_REMOVE = "textbox.remove";

    private static final String AC_GENERATE = "generate";

    private final TextParser textParser;

    private final List<WindowContextUpdateListener> winCtxUpdateListeners;

    private final List<Textbox> textboxes;
    private int currentTextbox;

    private final JList<Textbox> textboxSelector;
    private final FaceSelectionPanel faceSelection;
    private final TextboxEditorPane editorPane;

    private WindowContext winCtx;
    private GameDefinition gameDef;
    private Path basePath;

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
        textboxSelector.addListSelectionListener(this);
        var renderer = new TextboxListCellRenderer(textParser);
        winCtxUpdateListeners.add(renderer);
        textboxSelector.setCellRenderer(renderer);
        textboxSelector.setOpaque(false);

        setLayout(new BorderLayout());
        add(createTextboxSelectionPanel(), BorderLayout.LINE_START);
        add(createTextboxEditorPanel(), BorderLayout.CENTER);
    }

    private JPanel createTextboxSelectionPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 7));
        JButton btn;
        btn = new JButton("A");
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_ADD);
        btn.setToolTipText("Add a new textbox");
        buttonPanel.add(btn);
        btn = new JButton("C");
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_CLONE);
        btn.setToolTipText("Clone the currently selected textbox");
        buttonPanel.add(btn);
        btn = new JButton("IB");
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_INSERT_BEFORE);
        btn.setToolTipText("Insert a textbox before the currently selected one");
        buttonPanel.add(btn);
        btn = new JButton("IA");
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_INSERT_AFTER);
        btn.setToolTipText("Insert a textbox after the currently selected one");
        buttonPanel.add(btn);
        btn = new JButton("MU");
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_MOVE_UP);
        btn.setToolTipText("Move the currently selected textbox up");
        buttonPanel.add(btn);
        btn = new JButton("MD");
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_MOVE_DOWN);
        btn.setToolTipText("Move the currently selected textbox down");
        buttonPanel.add(btn);
        btn = new JButton("R");
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_REMOVE);
        btn.setToolTipText("Remove the currently selected textbox");
        buttonPanel.add(btn);

        var scroll = new WindowBackgroundScrollPane(textboxSelector);
        winCtxUpdateListeners.add(scroll);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.add(buttonPanel, BorderLayout.PAGE_START);
        listPanel.add(scroll, BorderLayout.CENTER);
        return listPanel;
    }

    private JPanel createTextboxEditorPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 1));
        JButton btnGenerate = new JButton("Generate");
        btnGenerate.setActionCommand(AC_GENERATE);
        btnGenerate.addActionListener(this);
        btnGenerate.setEnabled(false);
        winCtxUpdateListeners.add(winCtx1 -> btnGenerate.setEnabled(true));
        buttonPanel.add(btnGenerate);

        JPanel textboxPanel = new JPanel();
        textboxPanel.setLayout(new BorderLayout());
        textboxPanel.add(faceSelection, BorderLayout.PAGE_START);
        textboxPanel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
        textboxPanel.add(buttonPanel, BorderLayout.PAGE_END);
        return textboxPanel;
    }

    public void updateWindowContext(WindowContext winCtx) {
        this.winCtx = winCtx;
        for (var listener : winCtxUpdateListeners) {
            listener.updateWindowContext(winCtx);
        }
    }

    public void updateGameDefinition(GameDefinition gameDef, Path basePath) {
        this.gameDef = gameDef;
        this.basePath = basePath;
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
        textboxSelector.repaint();
    }

    public void onTextUpdated(String newText) {
        textboxes.get(currentTextbox).setText(newText);
        textboxSelector.repaint();
    }

    public void flushChanges() {
        faceSelection.flushChanges();
        editorPane.flushChanges(false);
        textboxSelector.repaint();
    }

    public void updateTextboxEditors() {
        var box = textboxes.get(currentTextbox);
        faceSelection.setFace(box.getFace());
        editorPane.setText(box.getText());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case AC_GENERATE:
                editorPane.flushChanges(false);
                faceSelection.flushChanges();

                final int textboxCount = textboxes.size();
                var image = new BufferedImage(816, 180 * textboxCount + 2 * (textboxCount - 1), BufferedImage.TYPE_INT_ARGB);

                var g = image.createGraphics();
                g.setBackground(ColorUtils.TRANSPARENT);
                g.clearRect(0, 0, image.getWidth(), image.getHeight());

                var oldClip = g.getClip();
                boolean success = true;
                for (int i = 0; i < textboxCount; i++) {
                    var textbox = textboxes.get(i);
                    var nodes = textParser.parse(textbox.getText());
                    if (nodes.hasErrors()) {
                        success = false;
                        break;
                    }

                    g.setClip(0, 182 * i, 816, 180);
                    winCtx.drawBackground(g, 4, 4 + 182 * i, 808, 172, null);
                    g.drawImage(textbox.getFace().getImage(), 18, 18 + 182 * i, null);
                    TextRenderer.draw(g, nodes, winCtx.getColors(),
                            textbox.getFace().isBlank() ? 18 : 186,
                            21 + 182 * i);
                    winCtx.drawBorder(g, 0, 182 * i, 816, 180, null);
                    if (i < textboxCount - 1) {
                        winCtx.drawArrow(g, 0, 182 * i, 816, 180, 0, null);
                    }
                }
                g.setClip(oldClip);

                g.dispose();

                if (success) {
                    var dialog = new PreviewDialog((Frame) SwingUtilities.getWindowAncestor(this), image);
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                } else {
                    // TODO
                }
                break;
            case AC_TEXTBOX_ADD:
                flushChanges();
                var copyBox = textboxes.get(textboxes.size() - 1);
                var box = new Textbox(copyBox.getFace(), "");
                textboxes.add(box);
                currentTextbox = textboxes.size() - 1;
                updateTextboxEditors();
                updateTextboxSelectorModel();
                break;
            case AC_TEXTBOX_CLONE:
                flushChanges();
                box = new Textbox(textboxes.get(currentTextbox));
                textboxes.add(currentTextbox, box);
                currentTextbox = textboxes.size() - 1;
                updateTextboxEditors();
                updateTextboxSelectorModel();
                break;
            case AC_TEXTBOX_INSERT_BEFORE:
                flushChanges();
                copyBox = textboxes.get(currentTextbox);
                box = new Textbox(copyBox.getFace(), "");
                textboxes.add(currentTextbox, box);
                updateTextboxEditors();
                updateTextboxSelectorModel();
                break;
            case AC_TEXTBOX_INSERT_AFTER:
                flushChanges();
                copyBox = textboxes.get(currentTextbox);
                box = new Textbox(copyBox.getFace(), "");
                textboxes.add(++currentTextbox, box);
                updateTextboxEditors();
                updateTextboxSelectorModel();
                break;
            case AC_TEXTBOX_MOVE_UP:
                flushChanges();
                box = textboxes.get(currentTextbox);
                textboxes.remove(box);
                currentTextbox--;
                if (currentTextbox < 0)
                    currentTextbox = 0;
                textboxes.add(currentTextbox, box);
                updateTextboxEditors();
                updateTextboxSelectorModel();
                break;
            case AC_TEXTBOX_MOVE_DOWN:
                flushChanges();
                box = textboxes.get(currentTextbox);
                textboxes.remove(box);
                currentTextbox++;
                if (currentTextbox > textboxes.size())
                    currentTextbox = textboxes.size();
                textboxes.add(currentTextbox, box);
                updateTextboxEditors();
                updateTextboxSelectorModel();
                break;
            case AC_TEXTBOX_REMOVE:
                flushChanges();
                box = textboxes.get(currentTextbox);
                if (!box.getText().isEmpty()) {
                    final int result = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete textbox " + (currentTextbox + 1) + "?", "Confirm deleting textbox",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result != JOptionPane.YES_OPTION)
                        break;
                }
                textboxes.remove(box);
                if (textboxes.size() == 0)
                    textboxes.add(new Textbox(Face.NONE, ""));
                if (currentTextbox == textboxes.size())
                    currentTextbox--;
                if (currentTextbox < 0)
                    currentTextbox = 0;
                updateTextboxEditors();
                updateTextboxSelectorModel();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (textboxSelector.equals(e.getSource())) {
            flushChanges();
            int selection = textboxSelector.getSelectedIndex();
            if (selection < 0) {
                return;
            }
            System.out.println("selected textbox " + selection);
            currentTextbox = selection;
            updateTextboxEditors();
        }
    }
}
