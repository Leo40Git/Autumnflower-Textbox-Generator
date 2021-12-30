package adudecalledleo.aftbg.app.game;

import java.awt.image.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

public record GameDefinition(String name,
                             Path basePath, WindowContext winCtx, FacePool faces, TextboxScriptSet scripts,
                             String[] credits) {
    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Path.class, new PathAdapter())
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

        GameDefinitionData data;
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            data = GSON.fromJson(reader, GameDefinitionData.class);
        } catch (Exception e) {
            throw new LoadException("Failed to read data from \"%s\""
                    .formatted(filePath));
        }

        Path facesPath = basePath.resolve(data.facesPath);
        FacePool faces;
        try (BufferedReader reader = Files.newBufferedReader(facesPath)) {
            faces = GSON.fromJson(reader, FacePool.class);
        } catch (Exception e) {
            throw new LoadException("Failed to read face pool data from \"%s\""
                    .formatted(facesPath));
        }

        Path scriptsPath = basePath.resolve(data.scriptsPath);
        TextboxScriptSet scripts;
        try (BufferedReader reader = Files.newBufferedReader(scriptsPath)) {
            scripts = GSON.fromJson(reader, TextboxScriptSet.class);
        } catch (Exception e) {
            throw new LoadException("Failed to read scripts data from \"%s\""
                    .formatted(scriptsPath));
        }

        Path windowPath = basePath.resolve(data.windowPath);
        BufferedImage windowImage;
        try (InputStream input = Files.newInputStream(windowPath)) {
            windowImage = ImageIO.read(input);
        } catch (IOException e) {
            throw new LoadException("Failed to load Window image from \"%s\""
                    .formatted(windowPath));
        }

        WindowContext winCtx = new WindowContext(windowImage, data.windowTint);
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

        return new GameDefinition(data.name, basePath, winCtx, faces, scripts, data.credits);
    }

    public static final class LoadException extends Exception {
        private LoadException(String message) {
            super(message);
        }

        private LoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
