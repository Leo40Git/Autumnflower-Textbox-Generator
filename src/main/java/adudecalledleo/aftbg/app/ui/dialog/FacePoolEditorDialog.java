package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.face.FaceLoadException;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.ui.render.FaceCategoryListCellRenderer;
import adudecalledleo.aftbg.app.ui.render.FaceListCellRenderer;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;
import adudecalledleo.aftbg.app.ui.util.ListReorderTransferHandler;
import adudecalledleo.aftbg.app.util.PathUtils;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

import static adudecalledleo.aftbg.Main.logger;

public final class FacePoolEditorDialog extends ModalDialog {
    private Path filePath;
    private FacePool pool;

    private final ContentPane contentPane;

    public FacePoolEditorDialog(Component owner) {
        super(owner);
        filePath = null;
        pool = null;
        setIconImage(AppResources.Icons.EDIT_FACE_POOL.getAsImage());
        setTitle("Edit face pool");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                if (pool == null) {
                    FacePoolEditorDialog.this.setVisible(false);
                    FacePoolEditorDialog.this.dispose();
                    return;
                }

                switch (JOptionPane.showConfirmDialog(FacePoolEditorDialog.this,
                        "Do you want to save your face pool before exiting?", "Close dialog",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    if (!saveFacePool()) {
                        break;
                    }
                case JOptionPane.NO_OPTION:
                    FacePoolEditorDialog.this.setVisible(false);
                    FacePoolEditorDialog.this.dispose();
                default:
                case JOptionPane.CANCEL_OPTION:
                    break;
                }
            }
        });
        setResizable(false);
        setContentPane(contentPane = new ContentPane());
        setJMenuBar(new MenuBar());
        setPreferredSize(new Dimension(768, 72 * 8 + 160));
        pack();
    }

    private boolean saveFacePool() {
        for (var cat : pool.getCategories().values()) {
            if (cat.getFaces().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Category \"" + cat.getName() + "\" is empty!\n" +
                                "Either add a face to it or remove the category.",
                        "Save Pool", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        try (JsonWriter writer = JsonWriter.json5(filePath)) {
            writer.setIndent("  ");
            FacePool.Adapter.write(writer, pool);
        } catch (Exception e) {
            logger().error("Failed to write face pool", e);
            DialogUtils.showErrorDialog(this,
                    "Failed to write face pool!\n" + e, "Save Pool");
            return false;
        }

        return true;
    }
    
    private final class MenuBar extends JMenuBar implements ActionListener {
        private static final String AC_NEW = "file.new";
        private static final String AC_LOAD = "file.load";
        private static final String AC_SAVE = "file.save";
        
        public MenuBar() {
            super();

            JMenuItem item;
            
            JMenu fileMenu = new JMenu("File");
            item = new JMenuItem("New Pool");
            item.setActionCommand(AC_NEW);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Load Pool");
            item.setActionCommand(AC_LOAD);
            item.addActionListener(this);
            fileMenu.add(item);
            item = new JMenuItem("Save Pool");
            item.setActionCommand(AC_SAVE);
            item.addActionListener(this);
            fileMenu.add(item);

            add(fileMenu);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            switch (evt.getActionCommand()) {
                case AC_NEW -> {
                    File file = DialogUtils.fileSaveDialog(this, "Select pool file location",
                            DialogUtils.FILTER_JSON_FILES);
                    if (file == null) {
                        return;
                    }
                    filePath = file.toPath();
                    if (pool == null) {
                        pool = new FacePool();
                    } else {
                        pool.clear();
                    }
                    contentPane.enableButtonsOnLoad();
                    contentPane.updateCategoriesModel();
                }
                case AC_LOAD -> {
                    File file = DialogUtils.fileOpenDialog(this, "Select pool file location",
                            DialogUtils.FILTER_JSON_FILES);
                    if (file == null) {
                        return;
                    }
                    Path newFilePath = file.toPath();
                    FacePool newPool;
                    try (JsonReader reader = JsonReader.json5(newFilePath)) {
                        newPool = FacePool.Adapter.read(reader);
                        newPool.loadAll(newFilePath.getParent());
                    } catch (Exception e) {
                        logger().error("Failed to read face pool for editing", e);
                        DialogUtils.showErrorDialog(this,
                                "Failed to read face pool!\n" + e, "Load Pool");
                        return;
                    }
                    filePath = newFilePath;
                    pool = newPool;
                    contentPane.enableButtonsOnLoad();
                    contentPane.updateCategoriesModel();
                }
                case AC_SAVE -> saveFacePool();
            }
        }
    }

    private final class ContentPane extends JPanel implements ListSelectionListener, ActionListener, ListReorderTransferHandler.ReorderCallback {
        private static final String AC_CATEGORY_ADD = "category.add";
        private static final String AC_FACE_ADD = "face.add";
        private static final String AC_CATEGORY_REMOVE = "category.remove";
        private static final String AC_FACE_REMOVE = "face.remove";
        private static final String AC_FACE_ADD_FOLDER = "face.add_folder";
        private static final String AC_CATEGORY_REMOVE_ICON = "category.remove_icon";
        private static final String AC_FACE_SET_AS_ICON = "face.set_as_icon";

        private final JList<FaceCategory> catList;
        private final DefaultListModel<FaceCategory> catModel;
        private final JList<Face> faceList;
        private final DefaultListModel<Face> faceModel;

        private final JButton btnAddCat;
        private final JButton btnAddFace;
        private final JButton btnRemCat;
        private final JButton btnRemFace;
        private final JButton btnAddFolder;
        private final JButton btnRemIcon;
        private final JButton btnSetIcon;

        public ContentPane() {
            super();

            catModel = new DefaultListModel<>();
            faceModel = new DefaultListModel<>();

            catList = new JList<>();
            catList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            catList.setModel(catModel);
            catList.setCellRenderer(new FaceCategoryListCellRenderer());
            ListReorderTransferHandler.install(catList, this);
            catList.setEnabled(false);
            catList.addListSelectionListener(this);

            faceList = new JList<>();
            faceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            faceList.setModel(faceModel);
            faceList.setCellRenderer(new FaceListCellRenderer(true));
            ListReorderTransferHandler.install(faceList, this);
            faceList.setEnabled(false);
            faceList.addListSelectionListener(this);

            btnAddCat = createButton("Add Category", AC_CATEGORY_ADD);
            btnAddFace = createButton("Add Face", AC_FACE_ADD);
            btnRemCat = createButton("Remove Category", AC_CATEGORY_REMOVE);
            btnRemFace = createButton("Remove Face", AC_FACE_REMOVE);
            btnAddFolder = createButton("Add Entire Folder", AC_FACE_ADD_FOLDER);
            btnRemIcon = createButton("Remove Category Icon", AC_CATEGORY_REMOVE_ICON);
            btnSetIcon = createButton("Set Face as Category Icon", AC_FACE_SET_AS_ICON);

            JPanel listsPanel = new JPanel();
            listsPanel.setLayout(new GridLayout(1, 2, 4, 0));
            listsPanel.add(new JScrollPane(catList));
            listsPanel.add(new JScrollPane(faceList));

            JPanel dumbPanel = new JPanel();
            dumbPanel.setLayout(new GridLayout(1, 2));
            dumbPanel.add(btnAddFolder);
            dumbPanel.add(btnRemIcon);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            buttonsPanel.setLayout(new GridLayout(3, 2));
            buttonsPanel.add(btnAddCat);
            buttonsPanel.add(btnAddFace);
            buttonsPanel.add(btnRemCat);
            buttonsPanel.add(btnRemFace);
            buttonsPanel.add(dumbPanel);
            buttonsPanel.add(btnSetIcon);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setLayout(new BorderLayout());
            add(listsPanel, BorderLayout.CENTER);
            add(buttonsPanel, BorderLayout.PAGE_END);
        }
        
        private JButton createButton(String text, String command) {
            JButton btn = new JButton(text);
            btn.setActionCommand(command);
            btn.addActionListener(this);
            btn.setEnabled(false);
            return btn;
        }
        
        private void enableButtonsOnLoad() {
            btnAddCat.setEnabled(true);
            btnAddFolder.setEnabled(true);
        }

        private void updateCategoriesModel() {
            int currentIndex = catList.getSelectedIndex();
            catModel.clear();

            for (var cat : pool.getCategories().values()) {
                if (cat != FaceCategory.NONE) {
                    catModel.addElement(cat);
                }
            }

            if (currentIndex > 0) {
                currentIndex = Math.min(currentIndex, catModel.size() - 1);
                catList.setSelectedIndex(currentIndex);
                catList.ensureIndexIsVisible(currentIndex);
            }

            catList.setEnabled(true);
        }

        private void updateFacesModel() {
            int currentIndex = faceList.getSelectedIndex();
            faceModel.clear();

            var selectedCat = catList.getSelectedValue();
            if (selectedCat == null) {
                faceList.setEnabled(false);
                return;
            }

            faceModel.addAll(selectedCat.getFaces().values());
            if (currentIndex > 0) {
                currentIndex = Math.min(currentIndex, faceModel.size() - 1);
                faceList.setSelectedIndex(currentIndex);
                faceList.ensureIndexIsVisible(currentIndex);
            }

            faceList.setEnabled(true);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            var src = e.getSource();
            if (catList.equals(src)) {
                updateFacesModel();
                boolean newState = catList.getSelectedValue() != null;
                btnRemCat.setEnabled(newState);
                btnAddFace.setEnabled(newState);
                btnRemIcon.setEnabled(newState);
            } else if (faceList.equals(src)) {
                boolean newState = catList.getSelectedValue() != null;
                btnRemFace.setEnabled(newState);
                btnSetIcon.setEnabled(newState);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case AC_CATEGORY_ADD -> {
                    String newName = JOptionPane.showInputDialog(this,
                            "Enter name for new category:",
                            "Add Category", JOptionPane.INFORMATION_MESSAGE);
                    if (newName == null) {
                        break;
                    }
                    if (newName.isBlank()) {
                        JOptionPane.showMessageDialog(this,
                                "Category name must not be blank!",
                                "Add Category", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (FaceCategory.NONE.getName().equals(newName)) {
                        JOptionPane.showMessageDialog(this,
                                "Category name \"" + FaceCategory.NONE.getName() + "\" is reserved!",
                                "Add Category", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (newName.contains("/")) {
                        JOptionPane.showMessageDialog(this,
                                "Category name cannot contain slashes (/)!",
                                "Add Category", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    var newCat = pool.getOrCreateCategory(newName);
                    updateCategoriesModel();
                    catList.setSelectedValue(newCat, true);
                }
                case AC_FACE_ADD -> {
                    var selectedCat = catList.getSelectedValue();
                    if (selectedCat == null) {
                        break;
                    }
                    File imageFile = DialogUtils.fileOpenDialog(this, "Select image for new face",
                            DialogUtils.FILTER_IMAGE_FILES);
                    if (imageFile == null) {
                        break;
                    }
                    Path imagePath = filePath.getParent().relativize(imageFile.toPath());
                    String newName = imagePath.getFileName().toString();
                    newName = processImageName(newName);
                    newName = (String) JOptionPane.showInputDialog(this,
                            "Enter name for new face:",
                            "Add Face", JOptionPane.INFORMATION_MESSAGE, null, null, newName);
                    if (newName == null) {
                        break;
                    }
                    if (newName.isBlank()) {
                        JOptionPane.showMessageDialog(this,
                                "Face name must not be blank!",
                                "Add Face", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (selectedCat.getFace(newName) != null) {
                        JOptionPane.showMessageDialog(this,
                                "Face name \"" + newName + "\" is already taken!",
                                "Add Face", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    String descRaw = DialogUtils.showMultilineInputDialog(this,
                            "Enter description for new face:",
                            "Add Face", JOptionPane.INFORMATION_MESSAGE, null);
                    String[] desc = Face.DEFAULT_DESCRIPTION;
                    if (descRaw != null && !descRaw.isBlank()) {
                        desc = descRaw.split("\n");
                    }
                    var newFace = selectedCat.add(newName, desc,
                            PathUtils.sanitize(imagePath.toString()));
                    try {
                        newFace.loadImage(filePath.getParent());
                    } catch (FaceLoadException ex) {
                        logger().error("Failed to load new face", ex);
                        DialogUtils.showErrorDialog(this,
                                "Failed to load new face \"" + newName + "\":\n" + ex,
                                "Add Face");
                        selectedCat.removeFace(newFace);
                        break;
                    }
                    updateFacesModel();
                }
                case AC_CATEGORY_REMOVE -> {
                    var selectedCat = catList.getSelectedValue();
                    if (selectedCat == null) {
                        break;
                    }
                    if (!selectedCat.getFaces().isEmpty()) {
                        if (JOptionPane.showConfirmDialog(this,
                                "Are you sure you want to remove the \"" + selectedCat.getName() + "\" category?\n"
                                        + "This will remove all faces in this category, and cannot be undone!",
                                "Remove Category",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                            break;
                        }
                    }
                    pool.removeCategory(selectedCat);
                    updateCategoriesModel();
                }
                case AC_FACE_REMOVE -> {
                    var selectedCat = catList.getSelectedValue();
                    if (selectedCat == null) {
                        break;
                    }
                    var selectedFace = faceList.getSelectedValue();
                    if (selectedFace == null) {
                        break;
                    }
                    if (JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to remove the \"" + selectedFace.getName() + "\" face?\n"
                                    + "This cannot be undone!",
                            "Remove Face",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                        break;
                    }
                    selectedCat.removeFace(selectedFace);
                    updateFacesModel();
                    String iconName = selectedCat.getIconName();
                    if (iconName != null && iconName.equals(selectedFace.getName())) {
                        selectedCat.setIconName(null);
                    }
                    updateCategoriesModel();
                    catList.setSelectedValue(selectedCat, true);
                }
                case AC_CATEGORY_REMOVE_ICON -> {
                    var selectedCat = catList.getSelectedValue();
                    if (selectedCat == null) {
                        break;
                    }
                    var selectedFace = faceList.getSelectedValue();
                    selectedCat.setIconName(null);
                    updateCategoriesModel();
                    catList.setSelectedValue(selectedCat, true);
                    if (selectedFace != null) {
                        faceList.setSelectedValue(selectedFace, true);
                    }
                }
                case AC_FACE_SET_AS_ICON -> {
                    var selectedCat = catList.getSelectedValue();
                    if (selectedCat == null) {
                        break;
                    }
                    var selectedFace = faceList.getSelectedValue();
                    if (selectedFace == null) {
                        break;
                    }
                    selectedCat.setIconName(selectedFace.getName());
                    updateCategoriesModel();
                    catList.setSelectedValue(selectedCat, true);
                    faceList.setSelectedValue(selectedFace, true);
                }
                case AC_FACE_ADD_FOLDER -> {
                    File dir = DialogUtils.folderOpenDialog(this, "Open folder to add as category");
                    if (dir == null) {
                        break;
                    }
                    File[] files = dir.listFiles();
                    if (files == null) {
                        JOptionPane.showMessageDialog(this,
                                "File \"" + dir + "\" is not a folder!",
                                "Add Entire Folder",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    String catName = processImageName(dir.getName());
                    catName = (String) JOptionPane.showInputDialog(this,
                            "Enter name for new category:",
                            "Add Entire Folder", JOptionPane.INFORMATION_MESSAGE, null, null, catName);
                    if (catName.isBlank()) {
                        JOptionPane.showMessageDialog(this,
                                "Category name must not be blank!",
                                "Add Entire Folder", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (FaceCategory.NONE.getName().equals(catName)) {
                        JOptionPane.showMessageDialog(this,
                                "Category name \"" + FaceCategory.NONE.getName() + "\" is reserved!",
                                "Add Entire Folder", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    var newCat = pool.getOrCreateCategory(catName);
                    for (var file : files) {
                        Path imagePath = filePath.getParent().relativize(file.toPath());
                        String faceName = imagePath.getFileName().toString();
                        faceName = processImageName(faceName);
                        if (newCat.getFace(faceName) != null) {
                            JOptionPane.showMessageDialog(this,
                                    "Face name \"" + faceName + "\" is already taken!",
                                    "Add Entire Folder", JOptionPane.ERROR_MESSAGE);
                            continue;
                        }
                        var newFace = newCat.add(faceName, Face.DEFAULT_DESCRIPTION,
                                PathUtils.sanitize(imagePath.toString()));
                        try {
                            newFace.loadImage(filePath.getParent());
                        } catch (FaceLoadException ex) {
                            logger().error("Failed to load new face", ex);
                            DialogUtils.showErrorDialog(this,
                                    "Failed to load new face \"" + faceName + "\":\n" + ex,
                                    "Add Entire Folder");
                            newCat.removeFace(newFace);
                        }
                    }
                    updateCategoriesModel();
                    updateFacesModel();
                }
            }
        }

        @Override
        public void move(JList<?> source, int oldIndex, int newIndex) {
            if (catList.equals(source)) {
                var values = new ArrayList<>(pool.getCategories().values());
                values.add(newIndex, values.remove(oldIndex));
                pool.clear();
                for (var value : values) {
                    pool.getCategories().put(value.getName(), value);
                }
                updateCategoriesModel();
                catList.setSelectedIndex(newIndex);
                catList.ensureIndexIsVisible(newIndex);
            } else if (faceList.equals(source)) {
                var selectedCat = catList.getSelectedValue();
                if (selectedCat == null) {
                    return;
                }
                var values = new ArrayList<>(selectedCat.getFaces().values());
                values.add(newIndex, values.remove(oldIndex));
                selectedCat.getFaces().clear();
                for (var value : values) {
                    selectedCat.getFaces().put(value.getName(), value);
                }
                updateFacesModel();
                faceList.setSelectedIndex(newIndex);
                faceList.ensureIndexIsVisible(newIndex);
            }
        }
    }

    private static final Pattern TRIMSTART_PATTERN = Pattern.compile("^_*(\\w)");
    private static final Pattern DESNAKE_PATTERN = Pattern.compile("_(\\w)");
    public static String processImageName(String name) {
        if (name.length() > 0) {
            int lastDotIndex = name.lastIndexOf('.');
            if (lastDotIndex > 0) {
                name = name.substring(0, lastDotIndex);
            }
            name = TRIMSTART_PATTERN.matcher(name)
                    .replaceAll(result -> result.group(1).toUpperCase(Locale.ROOT));
            name = DESNAKE_PATTERN.matcher(name)
                    .replaceAll(result -> " " + result.group(1).toUpperCase(Locale.ROOT));
        }
        return name;
    }
}
