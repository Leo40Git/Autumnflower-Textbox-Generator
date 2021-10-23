package adudecalledleo.aftbg.face;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class FaceCategory {
    private final String name;
    String icon;
    final Map<String, Face> faces;

    FaceCategory(String name) {
        this.name = name;
        icon = null;
        faces = new HashMap<>();
    }

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

    public void loadAll(Path basePath) throws FaceLoadException {
        for (Face face : faces.values()) {
            face.loadImage(basePath);
        }
    }
}
