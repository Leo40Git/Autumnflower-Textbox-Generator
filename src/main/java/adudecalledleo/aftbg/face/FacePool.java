package adudecalledleo.aftbg.face;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class FacePool {
    private final Map<String, FaceCategory> categories, categoriesU;

    public FacePool() {
        categories = new HashMap<>();
        categoriesU = Collections.unmodifiableMap(categories);
    }

    public void addFrom(FacePool other) {
        for (var entry : other.categories.entrySet()) {
            var cat = categories.get(entry.getKey());
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

    public FaceCategory getCategory(String category) {
        return categories.get(category);
    }

    public Map<String, FaceCategory> getCategories() {
        return categoriesU;
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
                FaceCategory cat = pool.categories.computeIfAbsent(in.nextName(), FaceCategory::new);
                in.beginObject();
                while (in.hasNext()) {
                    switch (in.nextName()) {
                        case "icon" -> cat.icon = in.nextString();
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
                if (cat.icon == null) {
                    // default icon is first entry
                    cat.icon = name;
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
                out.name(cat.getName());
                out.beginObject();
                if (cat.icon != null) {
                    out.name("icon");
                    out.value(cat.icon);
                }
                out.name("entries");
                out.beginObject();
                for (var entry : cat.faces.entrySet()) {
                    final Face face = entry.getValue();
                    out.name(face.getName());
                    out.value(face.getImagePath().toString());
                }
                out.endObject();
                out.endObject();
            }
            out.endObject();
        }
    }
}
