package adudecalledleo.aftbg.face;

import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FaceCategory {
    public static final FaceCategory NONE = new FaceCategory(Face.NONE.getCategory());

    static {
        NONE.faces.put(Face.NONE.getName(), Face.NONE);
    }

    private final String name;
    private String iconName;
    final Map<String, Face> faces, facesU;

    FaceCategory(String name) {
        this.name = name;
        iconName = null;
        faces = new LinkedHashMap<>();
        facesU = Collections.unmodifiableMap(faces);
    }

    @SuppressWarnings("CopyConstructorMissesField") // intentional
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
            faces.put(entry.getName(), new Face(entry.getName(), this.name, entry.getImagePath(), entry.image));
        }
    }

    public boolean remove(Face face) {
        return faces.remove(face.getName(), face);
    }

    public Face remove(String name) {
        return faces.remove(name);
    }

    public Face get(String name) {
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
        return facesU;
    }

    @ApiStatus.Internal
    public Map<String, Face> getFacesMutable() {
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
