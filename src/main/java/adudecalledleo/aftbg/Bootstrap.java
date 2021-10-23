package adudecalledleo.aftbg;

import adudecalledleo.aftbg.app.MainPanel;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FaceLoadException;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Bootstrap {
    public static final boolean OUTPUT_TRANSPARENT = false;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        Path basePath = Paths.get("scratch");

        Path defPath = basePath.resolve("def.json");
        GameDefinition def;
        try (BufferedReader reader = Files.newBufferedReader(defPath)) {
            def = GameDefinition.read(reader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read game definition from \"" + defPath.toAbsolutePath() + "\"!", e);
        }
        System.out.println("Using " + def.getName() + " game definition");

        try {
            def.getFaces().loadAll(basePath);
        } catch (FaceLoadException e) {
            throw new RuntimeException("Failed to load faces", e);
        }

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new MainPanel(def.getFaces()));
        frame.pack();
        frame.setVisible(true);
    }

    private void generateTestBox(Path basePath, GameDefinition def) {
        TextParser parser = new TextParser();
        NodeList nodes = parser.parse("\\c[0]Mercia:\n\\c[25]Hold on.\n\\c[#555]t\\c[4]e\\c[#FEDCBA]s\\c[10]t");
        if (nodes.hasErrors()) {
            System.out.format("has %d error(s):%n", nodes.getErrors().size());
            for (var error : nodes.getErrors()) {
                System.out.println(error);
            }
            return;
        } else {
            for (var node : nodes) {
                System.out.println(node);
            }
        }

        Face merciaNeutral = def.getFaces().getByPath("Mercia/Neutral");
        if (merciaNeutral == null) {
            throw new NullPointerException("Face \"Mercia/Neutral\" is missing!");
        }

        Path windowPath = basePath.resolve(def.getWindowPath());
        BufferedImage window;
        try (InputStream in = Files.newInputStream(windowPath)) {
            window = ImageIO.read(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read window image from \"" + windowPath.toAbsolutePath() + "\"!", e);
        }

        WindowBackground bg = new WindowBackground(window, def.getWindowTint());
        WindowBorder border = new WindowBorder(window);

        WindowArrow arrow = new WindowArrow(window);
        WindowColors colors = new WindowColors(window);

        BufferedImage dest = new BufferedImage(816, 180, OUTPUT_TRANSPARENT ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        g.setBackground(OUTPUT_TRANSPARENT ? ColorUtils.TRANSPARENT : Color.BLACK);
        g.clearRect(0, 0, 816, 180);

        bg.draw(g, 4, 4, 808, 172, null);
        border.draw(g, 0, 0, 816, 180, null);
        g.drawImage(merciaNeutral.getImage(), 18, 18, null);
        WindowText.draw(g, nodes, colors, 186, 21);
        arrow.draw(g, 0, 0, 816, 180, 3, null);

        g.dispose();

        Path destPath = basePath.resolve("dest.png");
        try {
            Files.deleteIfExists(destPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete destination file \"" + destPath.toAbsolutePath() + "\"!", e);
        }
        try (OutputStream out = Files.newOutputStream(destPath)) {
            ImageIO.write(dest, "PNG", out);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write destination image to \"" + destPath.toAbsolutePath() + "\"!", e);
        }
    }
}
