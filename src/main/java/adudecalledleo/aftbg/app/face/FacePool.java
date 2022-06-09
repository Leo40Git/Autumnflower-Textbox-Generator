package adudecalledleo.aftbg.app.face;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import adudecalledleo.aftbg.app.util.PathUtils;
import adudecalledleo.aftbg.json.JsonReadUtils;
import adudecalledleo.aftbg.json.JsonWriteUtils;
import adudecalledleo.aftbg.json.MalformedJsonException;
import adudecalledleo.aftbg.json.MissingFieldsException;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
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
            if (cat == null) {
                categories.put(entry.getKey(), new FaceCategory(entry.getValue()));
            } else {
                cat.addFrom(entry.getValue());
            }
        }
    }

    public void loadAll(Path basePath) throws FaceLoadException {
        FaceLoadException bigE = null;
        for (FaceCategory cat : categories.values()) {
            try {
                cat.loadAll(basePath);
            } catch (FaceLoadException e) {
                if (bigE == null) {
                    bigE = new FaceLoadException();
                }
                bigE.addSuppressed(new FaceLoadException("Failed to load category \"" + cat.getName() + "\"", e));
            }
        }
        if (bigE != null) {
            throw bigE;
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
    }

    public static final class Adapter {
        private Adapter() { }

        public static FacePool read(JsonReader in) throws IOException {
            List<String> missingFields = new ArrayList<>();
            FacePool pool = new FacePool();
            in.beginObject();

            while (in.hasNext()) {
                String name = in.nextName();
                if (FaceCategory.NONE.getName().equals(name)) {
                    throw new MalformedJsonException(in, "Tried to use reserved category name \"%s\""
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
                        case "entries" -> readCategoryEntries(cat, in, missingFields);
                    }
                }
                in.endObject();
            }

            in.endObject();
            return pool;
        }

        private static void readCategoryEntries(FaceCategory cat, JsonReader in, List<String> missingFields)
                throws IOException {
            if (in.peek() == JsonToken.BEGIN_OBJECT) {
                JsonReadUtils.readSimpleMap(in, JsonReader::nextString,
                        (name, imagePath) -> cat.add(name, Face.DEFAULT_GROUP, Face.DEFAULT_DESCRIPTION, imagePath));
            } else {
                in.beginArray();
                while (in.hasNext()) {
                    in.beginObject();
                    String name = null;
                    String group = Face.DEFAULT_GROUP;
                    String[] desc = Face.DEFAULT_DESCRIPTION;
                    String imagePath = null;
                    while (in.hasNext()) {
                        String field = in.nextName();
                        switch (field) {
                            case "name" -> name = in.nextString();
                            case "group" -> group = in.nextString();
                            case "desc", "description" -> desc = JsonReadUtils.readStringArray(in);
                            case "path" -> imagePath = in.nextString();
                            default -> in.skipValue();
                        }
                    }
                    in.endObject();

                    if (name == null) {
                        missingFields.add("name");
                    }
                    if (imagePath == null) {
                        missingFields.add("path");
                    }
                    if (!missingFields.isEmpty()) {
                        throw new MissingFieldsException(in, "Face", missingFields);
                    }

                    cat.add(name, group, desc, imagePath);
                }
                in.endArray();
            }
        }

        public static void write(JsonWriter out, FacePool value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginObject();
            for (var cat : value.categories.values()) {
                out.name(cat.getName());
                out.beginObject();
                if (cat.getIconName() != null) {
                    out.name("icon");
                    out.value(cat.getIconName());
                }
                out.name("entries");
                out.beginArray();
                for (var entry : cat.getFaces().entrySet()) {
                    final Face face = entry.getValue();
                    out.beginObject();
                    out.name("name");
                    out.value(face.getName());
                    out.name("path");
                    out.value(PathUtils.sanitize(face.getImagePath()));
                    if (!face.getGroup().isEmpty()) {
                        out.name("group");
                        out.value(face.getGroup());
                    }
                    final String[] desc = face.getDescription();
                    if (desc.length > 0) {
                        out.name("description");
                        JsonWriteUtils.writeStringArray(out, desc);
                    }
                    out.endObject();
                }
                out.endArray();
                out.endObject();
            }
            out.endObject();
        }
    }
}
