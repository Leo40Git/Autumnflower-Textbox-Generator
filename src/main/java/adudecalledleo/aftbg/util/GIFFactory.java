package adudecalledleo.aftbg.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class GIFFactory {
    private GIFFactory() { }

    public static byte[] create(List<BufferedImage> frames, int delayTime, String comment) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
            IIOMetadata meta = writer.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(frames.get(0).getType()),
                    writer.getDefaultWriteParam());
            String metaFormatName = meta.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(metaFormatName);
            fillMetadata(delayTime, comment, root);
            meta.setFromTree(metaFormatName, root);
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (BufferedImage frame : frames) {
                writer.writeToSequence(new IIOImage(frame, null, meta), writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
            ios.flush();
            return baos.toByteArray();
        }
    }

    public static byte[] create(List<BufferedImage> frames, int delayTime) throws IOException {
        return create(frames, delayTime, null);
    }

    // region Metadata stuff
    private static void fillMetadata(int delayTime, String comment, IIOMetadataNode root) {
        IIOMetadataNode graphicsControl = getOrCreateNode(root, "GraphicControlExtension");
        fillGraphicsControlExtension(delayTime, graphicsControl);
        if (comment != null) {
            IIOMetadataNode comments = getOrCreateNode(root, "CommentExtensions");
            comments.setAttribute("CommentExtension", comment);
        }
        IIOMetadataNode applications = getOrCreateNode(root, "ApplicationExtensions");
        IIOMetadataNode application = new IIOMetadataNode("ApplicationExtension");
        fillApplicationExtension(application);
        applications.appendChild(application);
    }

    private static void fillGraphicsControlExtension(int delayTime, IIOMetadataNode graphicsControl) {
        graphicsControl.setAttribute("disposalMethod", "none");
        graphicsControl.setAttribute("userInputFlag", "FALSE");
        graphicsControl.setAttribute("transparentColorFlag", "FALSE");
        graphicsControl.setAttribute("transparentColorIndex", "0");
        graphicsControl.setAttribute("delayTime", Integer.toString(delayTime));
    }

    private static final byte[] APPLICATION_EXTENSION_USER_OBJECT = new byte[] { 1, 0, 0 };

    private static void fillApplicationExtension(IIOMetadataNode child) {
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");
        child.setUserObject(APPLICATION_EXTENSION_USER_OBJECT);
    }

    private static IIOMetadataNode getOrCreateNode(IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0, length = rootNode.getLength(); i < length; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
    // endregion
}
