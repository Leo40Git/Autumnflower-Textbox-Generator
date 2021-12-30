package adudecalledleo.aftbg.app.game;

import java.awt.image.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import adudecalledleo.aftbg.app.script.ScriptLoadException;
import adudecalledleo.aftbg.app.script.TextboxScriptSet;
import adudecalledleo.aftbg.app.util.PathAdapter;
import adudecalledleo.aftbg.app.util.WindowTintAdapter;
import adudecalledleo.aftbg.face.FaceLoadException;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.window.WindowContext;
import adudecalledleo.aftbg.window.WindowTint;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.ApiStatus;

public record GameDefinition(String name,
                             Path basePath, WindowContext winCtx, FacePool faces, TextboxScriptSet scripts,
                             String[] credits) {
    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(WindowTint.class, new WindowTintAdapter())
            .registerTypeAdapter(FacePool.class, new FacePool.Adapter())
            .registerTypeAdapter(TextboxScriptSet.class, new TextboxScriptSet.Adapter())
            .create();

    public static GameDefinition load(Path filePath) throws LoadException {
        filePath = filePath.toAbsolutePath();

        Path basePath = filePath.getParent();
        if (basePath == null) {
            throw new LoadException("File path \"%s\" has no parent(?!)"
                    .formatted(filePath));
        }

        JsonRep jsonRep;
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            jsonRep = GSON.fromJson(reader, JsonRep.class);
        } catch (Exception e) {
            throw new LoadException("Failed to read definition from \"%s\""
                    .formatted(filePath));
        }

        Path facesPath = tryResolve(basePath, jsonRep.facesPath, "face pool definition");
        FacePool faces;
        try (BufferedReader reader = Files.newBufferedReader(facesPath)) {
            faces = GSON.fromJson(reader, FacePool.class);
        } catch (Exception e) {
            throw new LoadException("Failed to read face pool definition from \"%s\""
                    .formatted(facesPath));
        }

        Path scriptsPath = tryResolve(basePath, jsonRep.scriptsPath, "scripts definition");
        TextboxScriptSet scripts;
        try (BufferedReader reader = Files.newBufferedReader(scriptsPath)) {
            scripts = GSON.fromJson(reader, TextboxScriptSet.class);
        } catch (Exception e) {
            throw new LoadException("Failed to read scripts definition from \"%s\""
                    .formatted(scriptsPath));
        }

        Path windowPath = tryResolve(basePath, jsonRep.windowPath, "Window image");
        BufferedImage windowImage;
        try (InputStream input = Files.newInputStream(windowPath)) {
            windowImage = ImageIO.read(input);
        } catch (IOException e) {
            throw new LoadException("Failed to load Window image from \"%s\""
                    .formatted(windowPath));
        }

        WindowContext winCtx = new WindowContext(windowImage, jsonRep.windowTint);
        try {
            faces.loadAll(basePath);
        } catch (FaceLoadException e) {
            throw new LoadException("Failed to load face pool", e);
        }
        try {
            scripts.loadAll(basePath);
        } catch (ScriptLoadException e) {
            throw new LoadException("Failed to load scripts", e);
        }

        return new GameDefinition(jsonRep.name, basePath, winCtx, faces, scripts, jsonRep.credits);
    }

    private static Path tryResolve(Path base, String other, String description) throws LoadException {
        try {
            return base.resolve(other);
        } catch (InvalidPathException e) {
            throw new LoadException("Got invalid %s path".formatted(description), e);
        }
    }

    public static final class LoadException extends Exception {
        private LoadException(String message) {
            super(message);
        }

        private LoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @ApiStatus.Internal
    public static final class JsonRep {
        public String name;
        public String windowPath;
        public WindowTint windowTint;
        public String facesPath;
        public String scriptsPath;
        public String[] credits;
    }
}
