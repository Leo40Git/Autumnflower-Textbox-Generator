package adudecalledleo.aftbg.app.face;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FaceCategory {
    public static final FaceCategory NONE = new FaceCategory(Face.BLANK.getCategory());

    static {
        NONE.faces.put(Face.BLANK.getName(), Face.BLANK);
    }

    private final String name;
    private String iconName;
    private final Map<String, Face> faces;

    FaceCategory(String name) {
        this.name = name;
        iconName = null;
        faces = new LinkedHashMap<>();
    }

    FaceCategory(FaceCategory other) {
        this(other.name);
        iconName = other.iconName;
        addFrom(other);
    }

    public String getName() {
        return name;
    }

    public Face add(String name, String imagePath) {
        Face face = new Face(name, this.name, imagePath);
        faces.put(name, face);
        return face;
    }

    public void addFrom(FaceCategory other) {
        for (var entry : other.faces.values()) {
            faces.put(entry.getName(), entry);
        }
    }

    public boolean removeFace(Face face) {
        return faces.remove(face.getName(), face);
    }

    public Face removeFace(String name) {
        return faces.remove(name);
    }

    public Face getFace(String name) {
        return faces.get(name);
    }

    public Face getIcon() {
        if (iconName == null) {
            return null;
        }
        return faces.get(iconName);
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Map<String, Face> getFaces() {
        return faces;
    }

    public void loadAll(Path basePath) throws FaceLoadException {
        for (Face face : faces.values()) {
            face.loadImage(basePath);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
