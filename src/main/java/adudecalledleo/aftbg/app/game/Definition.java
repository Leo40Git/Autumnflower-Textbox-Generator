package adudecalledleo.aftbg.app.game;

import java.nio.file.Path;

import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.script.TextboxScriptSet;
import adudecalledleo.aftbg.app.util.WindowTintAdapter;
import adudecalledleo.aftbg.window.WindowTint;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

public sealed abstract class Definition permits GameDefinition, ExtensionDefinition {
    public static final String[] DEFAULT_DESCRIPTION = new String[] { "(no description)" };
    public static final String[] DEFAULT_CREDITS = new String[0];

    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(WindowTint.class, new WindowTintAdapter())
            .registerTypeAdapter(FacePool.class, new FacePool.Adapter())
            .registerTypeAdapter(TextboxScriptSet.class, new TextboxScriptSet.Adapter())
            .create();

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

    protected void setAsSource(@Nullable TextboxScriptSet scripts) {
        if (scripts == null) {
            return;
        }

        for (var script : scripts.getScripts()) {
            script.setSource(this);
        }
    }

    @Override
    public String toString() {
        return qualifiedName();
    }
}
