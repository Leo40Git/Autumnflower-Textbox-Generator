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
    private final Map<String, Face> byPath;
    private final Map<String, Map<String, Face>> byCategory, byCategoryU;

    public FacePool() {
        byPath = new HashMap<>();
        byCategory = new HashMap<>();
        byCategoryU = Collections.unmodifiableMap(byCategory);
    }

    public void add(Face face) {
        if (byPath.put(face.getPath(), face) != null) {
            throw new IllegalStateException("Tried to add face with duplicate path \"" + face.getPath() + "\"!");
        }
        byCategory.computeIfAbsent(face.getCategory(), ignored -> new HashMap<>()).put(face.getName(), face);
    }

    public void addFrom(FacePool pool) {
        byPath.putAll(pool.byPath);
        for (var entry : pool.byCategory.entrySet()) {
            var map = byCategory.computeIfAbsent(entry.getKey(), ignored -> new HashMap<>());
            map.putAll(entry.getValue());
        }
    }

    public void loadAll(Path basePath) throws FaceLoadException {
        for (Face face : byPath.values()) {
            face.loadImage(basePath);
        }
    }

    public Face getByPath(String path) {
        return byPath.get(path);
    }

    public Map<String, Face> getCategory(String category) {
        return byCategory.get(category);
    }

    public Map<String, Map<String, Face>> getCategories() {
        return byCategoryU;
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
                String category = in.nextName();
                readCategory(pool, category, in);
            }
            in.endObject();
            return pool;
        }

        private void readCategory(FacePool pool, String category, JsonReader in) throws IOException {
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                Path imagePath = Paths.get(in.nextString());
                pool.add(new Face(name, category, imagePath));
            }
            in.endObject();
        }

        @Override
        public void write(JsonWriter out, FacePool value) throws IOException {
            out.beginObject();
            for (var catEntry : value.byCategory.entrySet()) {
                out.name(catEntry.getKey());
                out.beginArray();
                for (var entry : catEntry.getValue().entrySet()) {
                    final Face face = entry.getValue();
                    out.beginObject();
                    out.name("name");
                    out.value(face.getName());
                    out.name("path");
                    out.value(face.getImagePath().toString());
                    out.endObject();
                }
                out.endArray();
            }
            out.endObject();
        }
    }
}
