package adudecalledleo.aftbg.app.component;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import adudecalledleo.aftbg.app.AppPreferences;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.component.render.TextboxListCellRenderer;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.data.TextboxListSerializer;
import adudecalledleo.aftbg.app.dialog.AboutDialog;
import adudecalledleo.aftbg.app.dialog.FacePoolEditorDialog;
import adudecalledleo.aftbg.app.dialog.PreferencesDialog;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.script.TextboxScriptSet;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.util.ListReorderTransferHandler;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.app.worker.ExtensionDefinitionLoader;
import adudecalledleo.aftbg.app.worker.GameDefinitionLoader;
import adudecalledleo.aftbg.app.worker.TextboxAnimator;
import adudecalledleo.aftbg.app.worker.TextboxGenerator;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.logging.Logger;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class MainPanel extends JPanel implements ActionListener, ListSelectionListener, ListReorderTransferHandler.ReorderCallback {
    private static final String AC_TEXTBOX_ADD = "textbox.add";
    private static final String AC_TEXTBOX_CLONE = "textbox.clone";
    private static final String AC_TEXTBOX_INSERT_BEFORE = "textbox.insert.before";
    private static final String AC_TEXTBOX_INSERT_AFTER = "textbox.insert.after";
    private static final String AC_TEXTBOX_REMOVE = "textbox.remove";

    private static final String AC_GENERATE = "generate";
    private static final String AC_GENERATE_ANIMATION = "generate_animation";

    public final JFrame frame;

    private final List<GameDefinitionUpdateListener> updateListeners;

    private final List<Textbox> textboxes;
    private int currentTextbox;
    private final TextboxListSerializer projectSerializer;
    private File currentProject;

    private final JList<Textbox> textboxSelector;
    private final FaceSelectionPanel faceSelection;
    private final TextboxEditorPane editorPane;

    private GameDefinition gameDef;

    private MenuBar menuBar;

    public MainPanel(JFrame frame) {
        this.frame = frame;

        updateListeners = new ArrayList<>();

        textboxes = new ArrayList<>();
        textboxes.add(new Textbox(Face.NONE, ""));
        currentTextbox = 0;
        projectSerializer = new TextboxListSerializer(this);

        faceSelection = new FaceSelectionPanel(this::onFaceChanged);
        updateListeners.add(faceSelection);
        editorPane = new TextboxEditorPane(this::onTextUpdated);
        updateListeners.add(editorPane);

        textboxSelector = new JList<>();
        textboxSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateTextboxSelectorModel();
        textboxSelector.addListSelectionListener(this);
        var renderer = new TextboxListCellRenderer();
        updateListeners.add(renderer);
        textboxSelector.setCellRenderer(renderer);
        textboxSelector.setOpaque(false);
        ListReorderTransferHandler.install(textboxSelector, this);

        setLayout(new BorderLayout());
        add(createTextboxSelectionPanel(), BorderLayout.LINE_START);
        add(createTextboxEditorPanel(), BorderLayout.CENTER);
    }

    public JMenuBar getMenuBar() {
        if (menuBar == null) {
            menuBar = new MenuBar();
        }
        return menuBar;
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
        updateListeners.add(scroll);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.add(buttonPanel, BorderLayout.PAGE_START);
        listPanel.add(scroll, BorderLayout.CENTER);
        return listPanel;
    }

    private JPanel createTextboxEditorPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        JButton btnGenerate = new JButton("Generate");
        btnGenerate.setActionCommand(AC_GENERATE);
        btnGenerate.addActionListener(this);
        btnGenerate.setEnabled(false);
        updateListeners.add(gameDef -> btnGenerate.setEnabled(true));
        buttonPanel.add(btnGenerate);
        JButton btnGenerateAnim = new JButton("Generate Animation");
        btnGenerateAnim.setActionCommand(AC_GENERATE_ANIMATION);
        btnGenerateAnim.addActionListener(this);
        btnGenerateAnim.setEnabled(false);
        updateListeners.add(gameDef -> btnGenerateAnim.setEnabled(true));
        buttonPanel.add(btnGenerateAnim);

        JPanel textboxPanel = new JPanel();
        textboxPanel.setLayout(new BorderLayout());
        textboxPanel.add(faceSelection, BorderLayout.PAGE_START);
        textboxPanel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
        textboxPanel.add(buttonPanel, BorderLayout.PAGE_END);
        return textboxPanel;
    }

    public void updateGameDefinition(GameDefinition gameDef) {
        this.gameDef = gameDef;
        AppPreferences.setLastGameDefinition(gameDef.filePath());
        for (var listener : updateListeners) {
            listener.updateGameDefinition(gameDef);
        }
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
        editorPane.setTextboxFace(newFace);
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
        editorPane.setTextboxFace(box.getFace());
        editorPane.setText(box.getText());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case AC_GENERATE -> {
                if (gameDef == null) {
                    JOptionPane.showMessageDialog(this,
                            "A game definition hasn't been loaded yet!",
                            "Generate", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                editorPane.flushChanges(false);
                faceSelection.flushChanges();

                List<Textbox> textboxesCopy = new ArrayList<>(textboxes);

                frame.setEnabled(false);
                LoadFrame loadFrame = new LoadFrame("Generating...", false);
                var worker = new TextboxGenerator(this, loadFrame, gameDef, textboxesCopy);
                worker.execute();
            }
            case AC_GENERATE_ANIMATION -> {
                if (gameDef == null) {
                    JOptionPane.showMessageDialog(this,
                            "A game definition hasn't been loaded yet!",
                            "Generate Animated", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                editorPane.flushChanges(false);
                faceSelection.flushChanges();

                List<Textbox> textboxesCopy = new ArrayList<>(textboxes);

                frame.setEnabled(false);
                LoadFrame loadFrame = new LoadFrame("Generating...", false);
                var worker = new TextboxAnimator(this, loadFrame, gameDef, textboxesCopy);
                worker.execute();
            }
            case AC_TEXTBOX_ADD -> {
                flushChanges();
                var copyBox = textboxes.get(textboxes.size() - 1);
                var box = new Textbox(copyBox.getFace(), "");
                textboxes.add(box);
                currentTextbox = textboxes.size() - 1;
                updateTextboxEditors();
                updateTextboxSelectorModel();
            }
            case AC_TEXTBOX_CLONE -> {
                flushChanges();
                Textbox box = new Textbox(textboxes.get(currentTextbox));
                textboxes.add(currentTextbox, box);
                currentTextbox = textboxes.size() - 1;
                updateTextboxEditors();
                updateTextboxSelectorModel();
            }
            case AC_TEXTBOX_INSERT_BEFORE -> {
                flushChanges();
                Textbox copyBox = textboxes.get(currentTextbox);
                Textbox box = new Textbox(copyBox.getFace(), "");
                textboxes.add(currentTextbox, box);
                updateTextboxEditors();
                updateTextboxSelectorModel();
            }
            case AC_TEXTBOX_INSERT_AFTER -> {
                flushChanges();
                Textbox copyBox = textboxes.get(currentTextbox);
                Textbox box = new Textbox(copyBox.getFace(), "");
                textboxes.add(++currentTextbox, box);
                updateTextboxEditors();
                updateTextboxSelectorModel();
            }
            case AC_TEXTBOX_REMOVE -> {
                flushChanges();
                Textbox box = textboxes.get(currentTextbox);
                if (!box.getText().isEmpty()) {
                    final int result = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete textbox " + (currentTextbox + 1) + "?", "Confirm deleting textbox",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
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
            }
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

    public boolean saveProject(boolean forceChooserDialog) throws IOException {
        flushChanges();
        if (currentProject == null || forceChooserDialog) {
            File sel = DialogUtils.fileSaveDialog(this, "Save Project", DialogUtils.FILTER_JSON_FILES);
            if (sel == null) {
                return false;
            }
            if (sel.exists()) {
                final int result = JOptionPane.showConfirmDialog(this,
                        "File \"" + sel + "\" already exists.\nOverwrite it?", "Save Project",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    return false;
                }
                if (!sel.delete()) {
                    throw new IOException("Could not delete file \"" + sel + "\"!");
                }
            }
            currentProject = sel;
        }
        try (FileWriter fw = new FileWriter(currentProject);
             JsonWriter out = GameDefinition.GSON.newJsonWriter(fw)) {
            projectSerializer.write(textboxes, out);
        }
        return true;
    }

    private final class MenuBar extends JMenuBar implements ActionListener, GameDefinitionUpdateListener {
        private static final String AC_NEW = "file.new";
        private static final String AC_LOAD = "file.load";
        private static final String AC_SAVE = "file.save";
        private static final String AC_SAVE_AS = "file.save_as";
        private static final String AC_DEF_LOAD = "file.def.load";
        private static final String AC_DEF_LOAD_EXT = "file.def.load_ext";
        private static final String AC_PREFS = "file.preferences";
        private static final String AC_FACE_POOL_EDITOR = "tools.face_pool_editor";
        private static final String AC_ABOUT = "help.about";

        private final JMenu scriptsMenu;

        public MenuBar() {
            super();

            JMenuItem item;

            JMenu fileMenu = new JMenu("File");
            item = new JMenuItem("New Project", AppResources.Icons.PROJECT_NEW.get());
            item.setActionCommand(AC_NEW);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Load Project", AppResources.Icons.PROJECT_LOAD.get());
            item.setActionCommand(AC_LOAD);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Save Project", AppResources.Icons.PROJECT_SAVE.get());
            item.setActionCommand(AC_SAVE);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Save Project As...", AppResources.Icons.PROJECT_SAVE_AS.get());
            item.setActionCommand(AC_SAVE_AS);
            item.addActionListener(this);
            fileMenu.add(item);
            fileMenu.addSeparator();
            item = new JMenuItem("Load Game Definition");
            item.setActionCommand(AC_DEF_LOAD);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Load Extension");
            item.setActionCommand(AC_DEF_LOAD_EXT);
            item.addActionListener(this);
            fileMenu.add(item);
            fileMenu.addSeparator();
            item = new JMenuItem("Preferences...", AppResources.Icons.PREFS.get());
            item.setActionCommand(AC_PREFS);
            item.addActionListener(this);
            fileMenu.add(item);

            scriptsMenu = new JMenu("Scripts");
            scriptsMenu.setEnabled(false);

            JMenu toolsMenu = new JMenu("Tools");
            item = new JMenuItem("Face Pool Editor", AppResources.Icons.EDIT_FACE_POOL.get());
            item.setActionCommand(AC_FACE_POOL_EDITOR);
            item.addActionListener(this);
            toolsMenu.add(item);

            JMenu helpMenu = new JMenu("Help");
            item = new JMenuItem("About", AppResources.Icons.ABOUT.get());
            item.setActionCommand(AC_ABOUT);
            item.addActionListener(this);
            helpMenu.add(item);

            add(fileMenu);
            add(scriptsMenu);
            add(toolsMenu);
            add(helpMenu);

            updateListeners.add(this);
            if (gameDef != null) {
                updateGameDefinition(gameDef);
            }
        }

        @Override
        public void updateGameDefinition(GameDefinition gameDef) {
            scriptsMenu.removeAll();

            TextboxScriptSet scripts = gameDef.scripts();
            if (scripts == null) {
                scriptsMenu.setEnabled(false);
            } else {
                scriptsMenu.setEnabled(true);
                for (var script : scripts.getScripts()) {
                    Action a = new AbstractAction(script.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            Textbox box = textboxes.get(currentTextbox);
                            try {
                                script.run(gameDef.faces(), box);
                            } catch (Exception e) {
                                Logger.error("Failed to run script!", e);
                                DialogUtils.showErrorDialog(MainPanel.this, "Failed to run script!", "Script Error");
                                return;
                            }
                            updateTextboxEditors();
                        }
                    };

                    String desc = script.getDescription();
                    if (desc != null) {
                        a.putValue(Action.SHORT_DESCRIPTION, desc);
                    }

                    scriptsMenu.add(a);
                }
            }
        }

        private boolean canOverwriteCurrentProject(String title, String description) {
            if (!isProjectEmpty()) {
                int result = JOptionPane.showConfirmDialog(MainPanel.this,
                        "<html>Do you want to save the current project before " + description + "?<br/>" +
                                "<b>Doing this will irreversibly delete all current textboxes!</b></html>",
                        "New Project", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        return saveProject(false);
                    } catch (IOException | IllegalStateException e) {
                        Logger.error("Failed to write project!", e);
                        JOptionPane.showMessageDialog(MainPanel.this,
                                "Failed to write project!\n" + e + "\n"
                                        + DialogUtils.logFileInstruction() + "\n"
                                        + "To prevent your work from being lost, the current operation has been cancelled.",
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
                         JsonReader in = GameDefinition.GSON.newJsonReader(fr)) {
                        newTextboxes = projectSerializer.read(in, gameDef.faces());
                    } catch (TextboxListSerializer.ReadCancelledException ignored) {
                        break;
                    } catch (IOException | IllegalStateException e) {
                        Logger.error("Failed to read project!", e);
                        DialogUtils.showErrorDialog(MainPanel.this,
                                "Failed to read project!\n" + e, "Load Project");
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
                        Logger.error("Failed to write project!", e);
                        DialogUtils.showErrorDialog(MainPanel.this,
                                "Failed to write project!\n" + e, "Save Project");
                    }
                }
                case AC_SAVE_AS -> {
                    try {
                        saveProject(true);
                    } catch (IOException | IllegalStateException e) {
                        Logger.error("Failed to write project!", e);
                        DialogUtils.showErrorDialog(MainPanel.this,
                                "Failed to write project!\n" + e, "Save Project");
                    }
                }
                case AC_DEF_LOAD -> {
                    if (!gameDef.extensions().isEmpty()) {
                        int result = JOptionPane.showConfirmDialog(MainPanel.this,
                                "<html>Are you sure?<br/>"
                                        + "Loading a new game definition will <b>unload all extensions!</b></html>",
                                "Load Game Definition", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (result != JOptionPane.YES_OPTION) {
                            break;
                        }
                    }

                    File defFile = DialogUtils.fileOpenDialog(MainPanel.this, "Load game definition", DialogUtils.FILTER_JSON_FILES);
                    if (defFile == null) {
                        break;
                    }
                    Path defPath = defFile.toPath();

                    frame.setEnabled(false);
                    LoadFrame loadFrame = new LoadFrame("Loading...", false);
                    var worker = new GameDefinitionLoader(MainPanel.this, loadFrame, defPath);
                    worker.execute();
                }
                case AC_DEF_LOAD_EXT -> {
                    File extFile = DialogUtils.fileOpenDialog(MainPanel.this, "Load extension definition", DialogUtils.FILTER_JSON_FILES);
                    if (extFile == null) {
                        break;
                    }
                    Path extPath = extFile.toPath();

                    frame.setEnabled(false);
                    LoadFrame loadFrame = new LoadFrame("Loading...", false);
                    var worker = new ExtensionDefinitionLoader(MainPanel.this, loadFrame, gameDef, extPath);
                    worker.execute();
                }
                case AC_PREFS -> {
                    var dialog = new PreferencesDialog(MainPanel.this);
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
                case AC_FACE_POOL_EDITOR -> {
                    var fpd = new FacePoolEditorDialog(MainPanel.this);
                    fpd.setLocationRelativeTo(null);
                    fpd.setVisible(true);
                }
                case AC_ABOUT -> {
                    var dialog = new AboutDialog(MainPanel.this, gameDef, MainPanel.this::updateGameDefinition);
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
            }
        }
    }
}
