package adudecalledleo.aftbg.app.dialog;

import adudecalledleo.aftbg.app.render.FaceCategoryListCellRenderer;
import adudecalledleo.aftbg.app.render.FaceListCellRenderer;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FaceCategory;
import adudecalledleo.aftbg.face.FaceLoadException;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.game.GameDefinition;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Path;
import java.util.Locale;

public final class FacePoolEditorDialog extends JDialog {
    @FunctionalInterface
    public interface SaveCallback {
        void saveFacePool(Component parent, FacePool pool);
    }

    private final Path basePath;
    private final FacePool pool;
    private final SaveCallback saveCallback;

    public static void saveToFile(Component parent, FacePool pool) {
        File file = DialogUtils.fileSaveDialog(parent, "Save face pool to file", DialogUtils.FILTER_JSON_FILES);
        if (file == null) {
            return;
        }
        if (file.exists()) {
            if (JOptionPane.showConfirmDialog(parent,
                    "File \"" + file + "\" already exists.\nDo you want to override it?",
                    "Save",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            if (!file.delete()) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to save to \"" + file + "\"!\nCould not delete file to overwrite it.",
                        "Save", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try (var fw = new FileWriter(file);
             var writer = new BufferedWriter(fw)) {
            GameDefinition.GSON.toJson(pool, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Failed to save to \"" + file + "\"!\n" + e,
                    "Save", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(parent,
                "Successfully saved to \"" + file + "\"!",
                "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    public FacePoolEditorDialog(Frame owner, Path basePath, FacePool pool, SaveCallback saveCallback) {
        super(owner);
        this.basePath = basePath;
        this.pool = pool;
        this.saveCallback = saveCallback;
        setTitle("Edit face pool");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setContentPane(new ContentPanel());
        setPreferredSize(new Dimension(768, 72 * 8 + 120));
        pack();
    }

    public FacePoolEditorDialog(Frame owner, Path basePath, FacePool pool) {
        this(owner, basePath, pool, FacePoolEditorDialog::saveToFile);
    }

    private final class ContentPanel extends JPanel implements ListSelectionListener, ActionListener {
        private static final String AC_CATEGORY_ADD = "category.add";
        private static final String AC_FACE_ADD = "face.add";
        private static final String AC_CATEGORY_REMOVE = "category.remove";
        private static final String AC_FACE_REMOVE = "face.remove";
        private static final String AC_FACE_SET_AS_ICON = "face.set_as_icon";
        private static final String AC_SAVE = "save";
        
        private final JList<FaceCategory> catList;
        private final DefaultListModel<FaceCategory> catModel;
        private final JList<Face> faceList;
        private final DefaultListModel<Face> faceModel;

        private final JButton btnAddFace;
        private final JButton btnRemCat;
        private final JButton btnRemFace;
        private final JButton btnSetIcon;

        public ContentPanel() {
            super();

            catModel = new DefaultListModel<>();
            faceModel = new DefaultListModel<>();

            catList = new JList<>();
            catList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            catList.setModel(catModel);
            catList.setCellRenderer(new FaceCategoryListCellRenderer());
            catList.setEnabled(false);
            catList.addListSelectionListener(this);

            faceList = new JList<>();
            faceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            faceList.setModel(faceModel);
            faceList.setCellRenderer(new FaceListCellRenderer(true));
            faceList.setEnabled(false);
            faceList.addListSelectionListener(this);

            JButton btnAddCat = createButton("Add Category", AC_CATEGORY_ADD, true);
            btnAddFace = createButton("Add Face", AC_FACE_ADD, false);
            btnRemCat = createButton("Remove Category", AC_CATEGORY_REMOVE, false);
            btnRemFace = createButton("Remove Face", AC_FACE_REMOVE, false);
            btnSetIcon = createButton("Set Face as Category Icon", AC_FACE_SET_AS_ICON, false);
            JButton btnSave = createButton("Save", AC_SAVE, true);

            JPanel listsPanel = new JPanel();
            listsPanel.setLayout(new GridBagLayout());
            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0.4;
            c.insets.right = 4;
            listsPanel.add(new JScrollPane(catList), c);
            c.gridx++;
            c.weightx = 0.6;
            c.insets.right = 0;
            listsPanel.add(new JScrollPane(faceList), c);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new GridLayout(3, 2));
            buttonsPanel.add(btnAddCat);
            buttonsPanel.add(btnAddFace);
            buttonsPanel.add(btnRemCat);
            buttonsPanel.add(btnRemFace);
            buttonsPanel.add(btnSetIcon);
            buttonsPanel.add(btnSave);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            setLayout(new BorderLayout());
            add(listsPanel, BorderLayout.CENTER);
            add(buttonsPanel, BorderLayout.PAGE_END);

            updateCategoriesModel();
        }
        
        private JButton createButton(String text, String command, boolean enabled) {
            JButton btn = new JButton(text);
            btn.setActionCommand(command);
            btn.addActionListener(this);
            btn.setEnabled(enabled);
            return btn;
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
                    Path imagePath = basePath.relativize(imageFile.toPath());
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
                    if (selectedCat.get(newName) != null) {
                        JOptionPane.showMessageDialog(this,
                                "Face name \"" + newName + "\" is already taken!",
                                "Add Face", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    var newFace = selectedCat.add(newName, imagePath);
                    try {
                        newFace.loadImage(basePath);
                    } catch (FaceLoadException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Failed to load new face \"" + newName + "\":\n" + ex,
                                "Add Face", JOptionPane.ERROR_MESSAGE);
                        selectedCat.remove(newFace);
                        break;
                    }
                    updateFacesModel();
                    if (selectedCat.getIconName() == null) {
                        selectedCat.setIconName(newName);
                        updateCategoriesModel();
                    }
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
                    selectedCat.remove(selectedFace);
                    updateFacesModel();
                    if (selectedCat.getIconName().equals(selectedFace.getName())) {
                        // default icon is first face
                        selectedCat.setIconName(selectedCat.getFaces().values().iterator().next().getName());
                    }
                    updateCategoriesModel();
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
                    faceList.setSelectedValue(selectedFace, true);
                }
                case AC_SAVE -> {
                    for (var cat : pool.getCategories().values()) {
                        if (cat.getFaces().isEmpty()) {
                            JOptionPane.showMessageDialog(this,
                                    "Category \"" + cat.getName() + "\" is empty!\n" +
                                            "Either add a face to it or remove the category.",
                                    "Save", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    saveCallback.saveFacePool(this, pool);
                }
            }
        }
    }

    private static String processImageName(String name) {
        if (name.length() > 0) {
            int lastDotIndex = name.lastIndexOf('.');
            if (lastDotIndex > 0) {
                name = name.substring(0, lastDotIndex);
            }
            name = (name.charAt(0) + "").toUpperCase(Locale.ROOT) + name.substring(1);
            while (name.contains("_")) {
                name = name.replaceFirst("_[a-z]", String.valueOf(name.charAt(name.indexOf("_") + 1)).toUpperCase(Locale.ROOT));
            }
        }
        return name;
    }
}
