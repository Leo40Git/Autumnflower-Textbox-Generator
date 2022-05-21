package adudecalledleo.aftbg.app.face;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.json.MalformedJsonException;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePool {
    private final Map<String, FaceCategory> categories;

    public FacePool() {
        categories = new LinkedHashMap<>();
    }

    public FacePool(FacePool other) {
        this();
        addFrom(other);
    }

    public void addFrom(FacePool other) {
        for (var entry : other.categories.entrySet()) {
            var cat = categories.get(entry.getKey());
            if (cat == FaceCategory.NONE) {
                continue;
            }
            if (cat == null) {
                categories.put(entry.getKey(), new FaceCategory(entry.getValue()));
            } else {
                cat.addFrom(entry.getValue());
            }
        }
    }

    public void loadAll(Path basePath) throws FaceLoadException {
        for (FaceCategory cat : categories.values()) {
            cat.loadAll(basePath);
        }
    }

    public Face getByPath(String path) {
        if (FaceCategory.NONE.getName().equals(path)) {
            return Face.BLANK;
        }

        int index = path.indexOf('/');
        if (index < 0) {
            throw new IllegalArgumentException("Path \"" + path + "\" is invalid: No separator between category and name!");
        }
        FaceCategory cat = getCategory(path.substring(0, index));
        if (cat == null) {
            return null;
        }
        return cat.getFace(path.substring(index + 1));
    }

    public FaceCategory getCategory(String name) {
        return categories.get(name);
    }

    public FaceCategory getOrCreateCategory(String name) {
        return categories.computeIfAbsent(name, FaceCategory::new);
    }

    public boolean removeCategory(FaceCategory category) {
        return categories.remove(category.getName(), category);
    }

    public FaceCategory removeCategory(String name) {
        return categories.remove(name);
    }

    public Map<String, FaceCategory> getCategories() {
        return categories;
    }

    public void clear() {
        categories.clear();
        categories.put(FaceCategory.NONE.getName(), FaceCategory.NONE);
    }

    public static final class Adapter {
        private Adapter() { }

        public static FacePool read(JsonReader in) throws IOException {
            FacePool pool = new FacePool();
            in.beginObject();

            while (in.hasNext()) {
                String name = in.nextName();
                if (FaceCategory.NONE.getName().equals(name)) {
                    throw new MalformedJsonException(in, "Tried to use reserve category name \"%s\""
                            .formatted(FaceCategory.NONE.getName()));
                }
                if (name.contains("/")) {
                    throw new MalformedJsonException(in, "Tried to use invalid category name \"%s\" (contains slashes)"
                            .formatted(name));
                }
                FaceCategory cat = pool.categories.computeIfAbsent(name, FaceCategory::new);
                in.beginObject();
                while (in.hasNext()) {
                    switch (in.nextName()) {
                        case "icon" -> cat.setIconName(in.nextString());
                        case "entries" -> readCategoryEntries(cat, in);
                    }
                }
                in.endObject();
            }

            in.endObject();
            return pool;
        }

        private static void readCategoryEntries(FaceCategory cat, JsonReader in) throws IOException {
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                String imagePath = in.nextString();
                cat.add(name, imagePath);
            }
            in.endObject();
        }

        public static void write(JsonWriter out, FacePool value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginObject();
            for (var cat : value.categories.values()) {
                if (cat == FaceCategory.NONE) {
                    continue;
                }
                out.name(cat.getName());
                out.beginObject();
                if (cat.getIconName() != null) {
                    out.name("icon");
                    out.value(cat.getIconName());
                }
                out.name("entries");
                out.beginObject();
                for (var entry : cat.getFaces().entrySet()) {
                    final Face face = entry.getValue();
                    out.name(face.getName());
                    out.value(PathUtils.sanitize(face.getImagePath()));
                }
                out.endObject();
                out.endObject();
            }
            out.endObject();
        }
    }
}
