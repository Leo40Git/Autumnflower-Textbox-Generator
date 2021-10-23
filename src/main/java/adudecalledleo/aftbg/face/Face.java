package adudecalledleo.aftbg.face;

import adudecalledleo.aftbg.util.ColorUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Face {
    private static final BufferedImage BLANK = new BufferedImage(144, 144, BufferedImage.TYPE_INT_ARGB);

    static {
        Graphics2D g = BLANK.createGraphics();
        g.setBackground(ColorUtils.TRANSPARENT);
        g.clearRect(0, 0, 144, 144);
        g.dispose();
    }

    private final String name;
    private final String category;
    private final Path imagePath;
    BufferedImage image;
    private ImageIcon icon;

    Face(String name, String category, Path imagePath, BufferedImage image) {
        this.name = name;
        this.category = category;
        this.imagePath = imagePath;
        this.image = image;
    }

    Face(String name, String category, Path imagePath) {
        this(name, category, imagePath, null);
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getPath() {
        return category + "/" + name;
    }

    public Path getImagePath() {
        return imagePath;
    }

    public void loadImage(Path basePath) throws FaceLoadException {
        if (imagePath == null) {
            image = BLANK;
            return;
        }
        Path path = basePath.resolve(imagePath).toAbsolutePath();
        try (var in = Files.newInputStream(path)) {
            image = ImageIO.read(in);
        } catch (IOException e) {
            throw new FaceLoadException("Exception occurred while loading image \"" + path + "\"", e);
        }
        if (image.getWidth() != 144 || image.getHeight() != 144) {
            throw new FaceLoadException("Image \"" + path + "\" must be 144 by 144," +
                    "was " + image.getWidth() + " by " + image.getHeight());
        }
        icon = null;
    }

    public boolean isBlank() {
        return image == BLANK;
    }

    public BufferedImage getImage() {
        if (image == null) {
            throw new IllegalStateException("Image hasn't been loaded yet!");
        }
        return image;
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            if (isBlank()) {
                return null;
            }
            icon = new ImageIcon(scaleImage(getImage(), 72, 72), "Icon of face " + getPath());
        }
        return icon;
    }

    private BufferedImage scaleImage(BufferedImage src, int width, int height) {
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return dst;
    }
}
