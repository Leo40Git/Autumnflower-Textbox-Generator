package adudecalledleo.aftbg.app.face;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.*;

import adudecalledleo.aftbg.app.game.DefinitionObject;
import adudecalledleo.aftbg.app.util.ColorUtils;
import adudecalledleo.aftbg.app.util.PathUtils;

public final class Face extends DefinitionObject {
    public static final String[] DEFAULT_COMMENTS = new String[0];

    public static final Face BLANK = new Face("Blank", "None", DEFAULT_COMMENTS,
            null, new BufferedImage(144, 144, BufferedImage.TYPE_INT_ARGB));

    private final String name;
    private final String category;
    private final String[] comments;
    private final String imagePath;
    private BufferedImage image;
    private ImageIcon icon;

    private Face(String name, String category, String[] comments, String imagePath, BufferedImage image) {
        this.name = name;
        this.category = category;
        this.comments = comments;
        this.imagePath = imagePath;
        this.image = image;
    }

    public Face(String name, String category, String[] comments, String imagePath) {
        this(name, category, comments, imagePath, null);
    }

    public boolean isBlank() {
        return this == BLANK;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String[] getComments() {
        return comments.clone();
    }

    public String getPath() {
        if (isBlank()) {
            return category;
        } else {
            return category + "/" + name;
        }
    }

    public String getImagePath() {
        return imagePath;
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
            if (isBlank()) {
                return;
            } else {
                throw new IllegalStateException("imagePath == null?!");
            }
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

    public String createCommentToolTip() {
        return String.join("<br>", comments);
    }

    public String toToolTipText(boolean includeName) {
        if (isBlank()) {
            return category;
        } else {
            String commentBlock = createCommentToolTip();
            String sourceBlock;
            if (source == null) {
                sourceBlock = "(source == null?!)";
            } else {
                sourceBlock = "<b>From:</b> " + source.qualifiedName();
            }
            if (includeName) {
                if (commentBlock.isEmpty()) {
                    return "%s<br>%s".formatted(name, sourceBlock);
                } else {
                    return "%s<br>%s<br>%s".formatted(name, commentBlock, sourceBlock);
                }
            } else {
                if (commentBlock.isEmpty()) {
                    return sourceBlock;
                } else {
                    return commentBlock + "<br>" + sourceBlock;
                }
            }
        }
    }
}
