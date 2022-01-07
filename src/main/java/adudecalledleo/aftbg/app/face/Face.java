package adudecalledleo.aftbg.app.face;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.*;

import adudecalledleo.aftbg.app.game.DefinedObject;
import adudecalledleo.aftbg.app.util.ColorUtils;
import adudecalledleo.aftbg.app.util.PathUtils;

public final class Face extends DefinedObject {
    private static final BufferedImage BLANK = new BufferedImage(144, 144, BufferedImage.TYPE_INT_ARGB);

    public static final Face NONE = new Face("(none)", "None", null, BLANK);

    static {
        Graphics2D g = BLANK.createGraphics();
        g.setBackground(ColorUtils.TRANSPARENT);
        g.clearRect(0, 0, 144, 144);
        g.dispose();
    }

    private final String name;
    private final String category;
    private final String imagePath;
    BufferedImage image;
    private ImageIcon icon;

    Face(String name, String category, String imagePath, BufferedImage image) {
        this.name = name;
        this.category = category;
        this.imagePath = imagePath;
        this.image = image;
    }

    Face(String name, String category, String imagePath) {
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

    public String getImagePath() {
        return imagePath;
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

    public void loadImage(Path basePath) throws FaceLoadException {
        if (imagePath == null) {
            image = BLANK;
            return;
        }

        Path path = PathUtils.tryResolve(basePath, imagePath, "image", FaceLoadException::new).toAbsolutePath();
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

    private static BufferedImage scaleImage(BufferedImage src, int width, int height) {
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setBackground(ColorUtils.TRANSPARENT);
        g.clearRect(0, 0, width, height);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return dst;
    }

    @Override
    public String toString() {
        return getPath();
    }
}
