package adudecalledleo.aftbg.face;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FacePool {
    public static final FacePool EMPTY = new FacePool();

    private final Map<String, FaceCategory> categories, categoriesU;

    public FacePool() {
        categories = new LinkedHashMap<>();
        categories.put(FaceCategory.NONE.getName(), FaceCategory.NONE);
        categoriesU = Collections.unmodifiableMap(categories);
    }

    @SuppressWarnings("CopyConstructorMissesField")
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
        String[] parts = path.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Path \"" + path + "\" should have 2 parts, but instead has " + parts.length);
        }
        FaceCategory cat = getCategory(parts[0]);
        if (cat == null) {
            return null;
        }
        return cat.get(parts[1]);
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
        return categoriesU;
    }

    @ApiStatus.Internal
    public Map<String, FaceCategory> getCategoriesMutable() {
        return categories;
    }

    public void clear() {
        categories.clear();
        categories.put(FaceCategory.NONE.getName(), FaceCategory.NONE);
    }

    public static final class Adapter extends TypeAdapter<FacePool> {
        @Override
        public FacePool read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.skipValue();
                return null;
            }
            FacePool pool = new FacePool();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (FaceCategory.NONE.getName().equals(name)) {
                    throw new IllegalStateException("Category name \"" + FaceCategory.NONE.getName() + "\" is reserved!");
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

        private void readCategoryEntries(FaceCategory cat, JsonReader in) throws IOException {
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (cat.getIconName() == null) {
                    // default icon is first entry
                    cat.setIconName(name);
                }
                Path imagePath = Paths.get(in.nextString());
                cat.add(name, imagePath);
            }
            in.endObject();
        }

        @Override
        public void write(JsonWriter out, FacePool value) throws IOException {
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
                for (var entry : cat.faces.entrySet()) {
                    final Face face = entry.getValue();
                    out.name(face.getName());
                    out.value(face.getImagePath().toString().replaceAll("\\\\", "/"));
                }
                out.endObject();
                out.endObject();
            }
            out.endObject();
        }
    }
}
