package adudecalledleo.aftbg;

import adudecalledleo.aftbg.draw.TextboxBG;
import adudecalledleo.aftbg.draw.TextboxBorder;

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
    public static void main(String[] args) {
        Path windowPath = Paths.get("scratch", "Window.png");
        BufferedImage window;
        try (InputStream in = Files.newInputStream(windowPath)) {
            window = ImageIO.read(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read window image from \"" + windowPath.toAbsolutePath() + "\"!", e);
        }

        TextboxBG bg = new TextboxBG(window);
        TextboxBorder border = new TextboxBorder(window);

        BufferedImage dest = new BufferedImage(816, 180, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();
        g.clearRect(0, 0, 816, 180);

        bg.draw(g, 4, 4, 808, 172, null);
        border.draw(g, 0, 0, 816, 180, null);

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
