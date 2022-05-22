package adudecalledleo.aftbg.app.game;

import java.nio.file.Path;

import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.script.TextboxScript;
import org.jetbrains.annotations.Nullable;

public sealed abstract class Definition permits GameDefinition, ExtensionDefinition {
    public static final String[] DEFAULT_DESCRIPTION = new String[] { "(no description)" };
    public static final String[] DEFAULT_CREDITS = new String[0];

    protected final String id;
    protected final String name;
    protected final String[] description;
    protected final String[] credits;
    protected final Path filePath, basePath;

    public Definition(String id, String name, String[] description, String[] credits, Path filePath, Path basePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.credits = credits;
        this.filePath = filePath;
        this.basePath = basePath;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String[] description() {
        return description;
    }

    public String[] credits() {
        return credits;
    }

    public Path filePath() {
        return filePath;
    }

    public Path basePath() {
        return basePath;
    }

    public abstract String qualifiedName();

    protected void setAsSource(@Nullable FacePool faces) {
        if (faces == null) {
            return;
        }

        for (FaceCategory category : faces.getCategories().values()) {
            if (category == FaceCategory.NONE) {
                continue;
            }

            for (var face : category.getFaces().values()) {
                face.setSource(this);
            }
        }
    }

    protected void setAsSource(@Nullable Iterable<TextboxScript> scripts) {
        if (scripts == null) {
            return;
        }

        for (var script : scripts) {
            script.setSource(this);
        }
    }

    @Override
    public String toString() {
        return qualifiedName();
    }
}
