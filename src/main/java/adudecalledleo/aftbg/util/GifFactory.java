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
import java.io.OutputStream;
import java.util.List;

public final class GifFactory {
    private GifFactory() { }

    public static void write(List<BufferedImage> frames, int delayTime, String comment, OutputStream out) throws IOException {
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();

            IIOMetadata imageMeta = writer.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(frames.get(0).getType()),
                    writer.getDefaultWriteParam());
            String imageMetaFormatName = imageMeta.getNativeMetadataFormatName();
            IIOMetadataNode imageRoot = (IIOMetadataNode) imageMeta.getAsTree(imageMetaFormatName);
            fillImageMetadata(imageRoot, delayTime, comment);
            imageMeta.setFromTree(imageMetaFormatName, imageRoot);

            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (BufferedImage frame : frames) {
                writer.writeToSequence(new IIOImage(frame, null, imageMeta), writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
            ios.flush();
        }
    }

    public static byte[] create(List<BufferedImage> frames, int delayTime, String comment) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            write(frames, delayTime, comment, baos);
            return baos.toByteArray();
        }
    }

    private static void fillImageMetadata(IIOMetadataNode root, int delayTime, String comment) {
        IIOMetadataNode graphicsControl = getOrCreateNode(root, "GraphicControlExtension");
        fillGraphicsControlExtension(delayTime, graphicsControl);
        IIOMetadataNode applications = getOrCreateNode(root, "ApplicationExtensions");
        IIOMetadataNode application = new IIOMetadataNode("ApplicationExtension");
        fillApplicationExtension(application);
        applications.appendChild(application);
        if (comment != null) {
            IIOMetadataNode comments = getOrCreateNode(root, "CommentExtensions");
            comments.setAttribute("CommentExtension", comment);
        }
    }

    private static void fillGraphicsControlExtension(int delayTime, IIOMetadataNode graphicsControl) {
        graphicsControl.setAttribute("disposalMethod", "none");
        graphicsControl.setAttribute("userInputFlag", "FALSE");
        graphicsControl.setAttribute("transparentColorFlag", "FALSE");
        graphicsControl.setAttribute("transparentColorIndex", "0");
        graphicsControl.setAttribute("delayTime", Integer.toString(delayTime));
    }

    private static void fillApplicationExtension(IIOMetadataNode child) {
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");
        child.setUserObject(new byte[] { 0x01, 0x00, 0x00 });
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
