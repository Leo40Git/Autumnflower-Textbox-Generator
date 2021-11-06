package adudecalledleo.aftbg;

import adudecalledleo.aftbg.app.AppFrame;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.face.FaceLoadException;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.window.WindowContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Bootstrap {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        try {
            AppResources.load();
        } catch (IOException e) {
            // TODO log error to file
            JOptionPane.showMessageDialog(null,
                    "Failed to load app resources!",
                    "Failed to launch", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        Path basePath = Paths.get("scratch").toAbsolutePath();

        Path defPath = basePath.resolve("def.json");
        GameDefinition gameDef;
        try (BufferedReader reader = Files.newBufferedReader(defPath)) {
            gameDef = GameDefinition.read(reader);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to read game definition from \"" + defPath.toAbsolutePath() + "\"!\n" + e,
                    "Failed to start application", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        System.out.println("Using " + gameDef.getName() + " game definition");

        Path windowPath = basePath.resolve(gameDef.getWindowPath());
        BufferedImage window;
        try (InputStream in = Files.newInputStream(windowPath)) {
            window = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to read window image from \"" + windowPath.toAbsolutePath() + "\"!",
                    "Failed to start application", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        WindowContext winCtx = new WindowContext(window, gameDef.getWindowTint());

        FacePool faces;
        Path facesPath = basePath.resolve(gameDef.getFacesPath());
        try (BufferedReader reader = Files.newBufferedReader(facesPath)) {
            faces = GameDefinition.GSON.fromJson(reader, FacePool.class);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to read face pool from \"" + facesPath.toAbsolutePath() + "\"!\n" + e,
                    "Failed to start application", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        try {
            faces.loadAll(basePath);
        } catch (FaceLoadException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to load face pictures!\n" + e,
                    "Failed to start application", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        AppFrame frame = new AppFrame(basePath, gameDef, winCtx, faces, new TextParser());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
