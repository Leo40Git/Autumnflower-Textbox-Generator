package adudecalledleo.aftbg;

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

        BufferedImage dest = new BufferedImage(816, 180, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();
        g.setBackground(new Color(0, 0, 0, 0));
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
