package adudecalledleo.aftbg.face;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FaceCategory {
    public static final FaceCategory NONE = new FaceCategory("None");

    static {
        NONE.add("(none)", null);
    }

    private final String name;
    String icon;
    final Map<String, Face> faces, facesU;

    FaceCategory(String name) {
        this.name = name;
        icon = null;
        faces = new LinkedHashMap<>();
        facesU = Collections.unmodifiableMap(faces);
    }

    @SuppressWarnings("CopyConstructorMissesField") // intentional
    public FaceCategory(FaceCategory other) {
        this(other.name);
        icon = other.icon;
        addFrom(other);
    }

    public String getName() {
        return name;
    }

    void add(String name, Path imagePath) {
        faces.put(name, new Face(name, this.name, imagePath));
    }

    void addFrom(FaceCategory other) {
        for (var entry : other.faces.values()) {
            faces.put(entry.getName(), new Face(entry.getName(), this.name, entry.getImagePath(), entry.image));
        }
    }

    public Face get(String name) {
        return faces.get(name);
    }

    public Face getIcon() {
        if (icon == null) {
            return null;
        }
        return faces.get(icon);
    }

    public Map<String, Face> getFaces() {
        return facesU;
    }

    public void loadAll(Path basePath) throws FaceLoadException {
        for (Face face : faces.values()) {
            face.loadImage(basePath);
        }
    }
}
