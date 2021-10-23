package adudecalledleo.aftbg.face;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Face {
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

    public BufferedImage getImage() {
        if (image == null) {
            throw new IllegalStateException("Image hasn't been loaded yet!");
        }
        return image;
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            icon = new ImageIcon(getImage(), "Icon of face " + getPath());
        }
        return icon;
    }
}
