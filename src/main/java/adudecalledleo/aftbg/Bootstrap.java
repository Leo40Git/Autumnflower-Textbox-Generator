package adudecalledleo.aftbg;

import adudecalledleo.aftbg.window.WindowArrow;
import adudecalledleo.aftbg.window.WindowBackground;
import adudecalledleo.aftbg.window.WindowBorder;
import adudecalledleo.aftbg.window.WindowColor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Bootstrap {
    public static final boolean OUTPUT_TRANSPARENT = false;

    public static void main(String[] args) {
        Path windowPath = Paths.get("scratch", "Window.png");
        BufferedImage window;
        try (InputStream in = Files.newInputStream(windowPath)) {
            window = ImageIO.read(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read window image from \"" + windowPath.toAbsolutePath() + "\"!", e);
        }

        WindowBackground bg = new WindowBackground(window, new WindowColor(-17, -255, -255));
        WindowBorder border = new WindowBorder(window);
        WindowArrow arrow = new WindowArrow(window);

        BufferedImage dest = new BufferedImage(816, 180, OUTPUT_TRANSPARENT ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        g.setBackground(OUTPUT_TRANSPARENT ? new Color(0, 0, 0, 0) : Color.BLACK);
        g.clearRect(0, 0, 816, 180);

        bg.draw(g, 4, 4, 808, 172, null);
        border.draw(g, 0, 0, 816, 180, null);
        arrow.draw(g, 0, 0, 816, 180, 3, null);

        g.dispose();

        Path destPath = Paths.get("scratch", "dest.png");
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
