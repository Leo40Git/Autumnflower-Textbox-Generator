package adudecalledleo.aftbg;

import adudecalledleo.aftbg.face.FaceLoadException;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.window.WindowContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public record TextboxResources(GameDefinition gameDefinition, WindowContext windowContext, FacePool facePool) {
    public static TextboxResources load(Path basePath) throws LoadException {
        Path defPath = basePath.resolve("def.json");
        GameDefinition gameDef;
        try (BufferedReader reader = Files.newBufferedReader(defPath)) {
            gameDef = GameDefinition.GSON.fromJson(reader, GameDefinition.class);
        } catch (IOException | IllegalStateException e) {
            throw new LoadException("Failed to read game definition", e);
        }
        System.out.println("Using " + gameDef.getName() + " game definition");

        Path windowPath = basePath.resolve(gameDef.getWindowPath());
        BufferedImage window;
        try (InputStream in = Files.newInputStream(windowPath)) {
            window = ImageIO.read(in);
        } catch (IOException e) {
            throw new LoadException("Failed to read window image", e);
        }
        WindowContext winCtx = new WindowContext(window, gameDef.getWindowTint());

        FacePool faces;
        Path facesPath = basePath.resolve(gameDef.getFacesPath());
        try (BufferedReader reader = Files.newBufferedReader(facesPath)) {
            faces = GameDefinition.GSON.fromJson(reader, FacePool.class);
        } catch (Exception e) {
            throw new LoadException("Failed to read face pool", e);
        }

        try {
            faces.loadAll(basePath);
        } catch (FaceLoadException e) {
            throw new LoadException("Failed to load images for face pool", e);
        }

        return new TextboxResources(gameDef, winCtx, faces);
    }

    public static final class LoadException extends Exception {
        private LoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
