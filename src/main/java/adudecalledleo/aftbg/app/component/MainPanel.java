package adudecalledleo.aftbg.app.component;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.data.TextboxListSerializer;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.WindowContextUpdateListener;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.FacePoolEditorDialog;
import adudecalledleo.aftbg.app.dialog.PreviewDialog;
import adudecalledleo.aftbg.app.render.TextboxListCellRenderer;
import adudecalledleo.aftbg.app.util.ListReorderTransferHandler;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MainPanel extends JPanel implements ActionListener, ListSelectionListener, ListReorderTransferHandler.ReorderCallback {
    private static final String AC_TEXTBOX_ADD = "textbox.add";
    private static final String AC_TEXTBOX_CLONE = "textbox.clone";
    private static final String AC_TEXTBOX_INSERT_BEFORE = "textbox.insert.before";
    private static final String AC_TEXTBOX_INSERT_AFTER = "textbox.insert.after";
    private static final String AC_TEXTBOX_REMOVE = "textbox.remove";

    private static final String AC_GENERATE = "generate";

    private final TextParser textParser;

    private final List<WindowContextUpdateListener> winCtxUpdateListeners;

    private final List<Textbox> textboxes;
    private int currentTextbox;
    private final TextboxListSerializer projectSerializer;
    private File currentProject;

    private final JList<Textbox> textboxSelector;
    private final FaceSelectionPanel faceSelection;
    private final TextboxEditorPane editorPane;

    private WindowContext winCtx;
    private GameDefinition gameDef;
    private FacePool faces;
    private Path basePath;

    public MainPanel(TextParser textParser) {
        this.textParser = textParser;

        winCtxUpdateListeners = new ArrayList<>();

        textboxes = new ArrayList<>();
        textboxes.add(new Textbox(Face.NONE, ""));
        currentTextbox = 0;
        projectSerializer = new TextboxListSerializer(this);

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
        ListReorderTransferHandler.install(textboxSelector, this);

        setLayout(new BorderLayout());
        add(createTextboxSelectionPanel(), BorderLayout.LINE_START);
        add(createTextboxEditorPanel(), BorderLayout.CENTER);
    }

    private JPanel createTextboxSelectionPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 5));
        JButton btn;
        btn = new JButton("Add", AppResources.Icons.TEXTBOX_ADD.get());
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_ADD);
        btn.setToolTipText("Add a new textbox");
        buttonPanel.add(btn);
        btn = new JButton("Clone", AppResources.Icons.TEXTBOX_CLONE.get());
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_CLONE);
        btn.setToolTipText("Clone the currently selected textbox");
        buttonPanel.add(btn);
        btn = new JButton("Insert B", AppResources.Icons.TEXTBOX_INSERT_BEFORE.get());
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_INSERT_BEFORE);
        btn.setToolTipText("Insert a textbox before the currently selected one");
        buttonPanel.add(btn);
        btn = new JButton("Insert A", AppResources.Icons.TEXTBOX_INSERT_AFTER.get());
        btn.addActionListener(this);
        btn.setActionCommand(AC_TEXTBOX_INSERT_AFTER);
        btn.setToolTipText("Insert a textbox after the currently selected one");
        buttonPanel.add(btn);
        btn = new JButton("Remove", AppResources.Icons.TEXTBOX_REMOVE.get());
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

    public void updateGameDefinition(Path basePath, GameDefinition gameDef, FacePool faces) {
        this.basePath = basePath;
        this.gameDef = gameDef;
        this.faces = faces;
        faceSelection.updateFacePool(faces);
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
                    // TODO more detailed error message
                    JOptionPane.showMessageDialog(this,
                            "Seems like one or more of your textboxes have errors!\n"
                                    + "Correct this, then try generating again.",
                            "Generate textbox(es)", JOptionPane.ERROR_MESSAGE);
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
            currentTextbox = selection;
            updateTextboxEditors();
        }
    }

    @Override
    public void move(JList<?> source, int oldIndex, int newIndex) {
        if (textboxSelector.equals(source)) {
            flushChanges();
            textboxes.add(newIndex, textboxes.remove(oldIndex));
            updateTextboxEditors();
            updateTextboxSelectorModel();
            textboxSelector.setSelectedIndex(newIndex);
        }
    }

    public boolean isProjectEmpty() {
        for (Textbox box : textboxes) {
            if (!box.getText().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void saveProject(boolean forceChooserDialog) throws IOException {
        flushChanges();
        boolean freshlySetProject = false;
        if (currentProject == null || forceChooserDialog) {
            File sel = DialogUtils.fileSaveDialog(this, "Save Project", DialogUtils.FILTER_JSON_FILES);
            if (sel == null) {
                return;
            }
            currentProject = sel;
            freshlySetProject = true;
        }
        if (freshlySetProject && currentProject.exists()) {
            final int result = JOptionPane.showConfirmDialog(this,
                    "File \"" + currentProject + "\" already exists.\nOverwrite it?", "Save Project",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
            if (!currentProject.delete()) {
                throw new IOException("Could not delete file \"" + currentProject + "\"!");
            }
        }
        try (FileWriter fw = new FileWriter(currentProject);
             JsonWriter out = new JsonWriter(fw)) {
            projectSerializer.write(textboxes, out);
        }
    }

    private MenuBarImpl menuBar;

    public JMenuBar getMenuBar() {
        if (menuBar == null) {
            menuBar = new MenuBarImpl();
        }
        return menuBar;
    }

    private final class MenuBarImpl extends JMenuBar implements ActionListener {
        private static final String AC_NEW = "file.new";
        private static final String AC_LOAD = "file.load";
        private static final String AC_SAVE = "file.save";
        private static final String AC_SAVE_AS = "file.save_as";
        private static final String AC_FACE_POOL_EDITOR = "tools.face_pool_editor";

        public MenuBarImpl() {
            super();

            JMenuItem item;

            JMenu fileMenu = new JMenu("File");
            item = new JMenuItem("New Project");
            item.setActionCommand(AC_NEW);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Load Project");
            item.setActionCommand(AC_LOAD);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Save Project");
            item.setActionCommand(AC_SAVE);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Save Project As...");
            item.setActionCommand(AC_SAVE_AS);
            item.addActionListener(this);
            fileMenu.add(item);

            JMenu toolsMenu = new JMenu("Tools");
            item = new JMenuItem("Face Pool Editor", AppResources.Icons.EDIT_FACE_POOL.get());
            item.setActionCommand(AC_FACE_POOL_EDITOR);
            item.addActionListener(this);
            toolsMenu.add(item);

            add(fileMenu);
            add(toolsMenu);
        }

        private boolean canOverwriteCurrentProject(String title, String description) {
            if (!isProjectEmpty()) {
                int result = JOptionPane.showConfirmDialog(MainPanel.this,
                        "Do you want to save the current project before " + description + "?\n" +
                                "Creating a new project will permanently delete all current textboxes!",
                        "New Project", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        saveProject(false);
                    } catch (IOException | IllegalStateException e) {
                        JOptionPane.showMessageDialog(MainPanel.this,
                                "Failed to write project!\n" + e + "\n" +
                                        "To prevent your work from being lost, the current operation has been cancelled.",
                                title, JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                } else return result == JOptionPane.NO_OPTION;
            }
            return true;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            switch (evt.getActionCommand()) {
                case AC_NEW -> {
                    if (!canOverwriteCurrentProject("New Project", "creating a new project")) {
                        break;
                    }
                    currentProject = null;
                    currentTextbox = 0;
                    textboxes.clear();
                    textboxes.add(new Textbox(Face.NONE, ""));
                    updateTextboxEditors();
                    updateTextboxSelectorModel();
                }
                case AC_LOAD -> {
                    if (!canOverwriteCurrentProject("Load Project", "loading another project")) {
                        break;
                    }
                    File src = DialogUtils.fileOpenDialog(MainPanel.this, "Load Project", DialogUtils.FILTER_JSON_FILES);
                    if (src == null) {
                        break;
                    }

                    List<Textbox> newTextboxes;
                    try (FileReader fr = new FileReader(src);
                         JsonReader in = new JsonReader(fr)) {
                        newTextboxes = projectSerializer.read(in, faces);
                    } catch (TextboxListSerializer.ReadCancelledException ignored) {
                        break;
                    } catch (IOException | IllegalStateException e) {
                        JOptionPane.showMessageDialog(MainPanel.this,
                                "Failed to read project!\n" + e,
                                "Load Project", JOptionPane.ERROR_MESSAGE);
                        break;
                    }

                    currentProject = src;
                    currentTextbox = 0;
                    textboxes.clear();
                    textboxes.addAll(newTextboxes);
                    if (textboxes.isEmpty()) {
                        textboxes.add(new Textbox(Face.NONE, ""));
                    }
                    updateTextboxEditors();
                    updateTextboxSelectorModel();
                }
                case AC_SAVE -> {
                    try {
                        saveProject(false);
                    } catch (IOException | IllegalStateException e) {
                        JOptionPane.showMessageDialog(MainPanel.this,
                                "Failed to write project!\n" + e,
                                "Save Project", JOptionPane.ERROR_MESSAGE);
                    }
                }
                case AC_SAVE_AS -> {
                    try {
                        saveProject(true);
                    } catch (IOException | IllegalStateException e) {
                        JOptionPane.showMessageDialog(MainPanel.this,
                                "Failed to write project!\n" + e,
                                "Save Project", JOptionPane.ERROR_MESSAGE);
                    }
                }
                case AC_FACE_POOL_EDITOR -> {
                    var fpd = new FacePoolEditorDialog((Frame) SwingUtilities.getWindowAncestor(this), basePath,
                            new FacePool(faces));
                    fpd.setLocationRelativeTo(null);
                    fpd.setVisible(true);
                }
            }
        }
    }
}
