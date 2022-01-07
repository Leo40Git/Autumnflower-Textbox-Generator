package adudecalledleo.aftbg.app.game;

import java.nio.file.Path;

import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.script.TextboxScriptSet;

public sealed abstract class Definition permits GameDefinition, ExtensionDefinition {
    protected final String name;
    protected final String[] description;
    protected final String[] credits;
    protected final Path filePath, basePath;

    public Definition(String name, String[] description, String[] credits, Path filePath, Path basePath) {
        this.name = name;
        this.description = description;
        this.credits = credits;
        this.filePath = filePath;
        this.basePath = basePath;
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

    protected void setAsSource(FacePool faces) {
        for (FaceCategory category : faces.getCategories().values()) {
            if (category == FaceCategory.NONE) {
                continue;
            }

            for (var face : category.getFaces().values()) {
                face.setSource(this);
            }
        }
    }

    protected void setAsSource(TextboxScriptSet scripts) {
        for (var script : scripts.getScripts()) {
            script.setSource(this);
        }
    }
}
