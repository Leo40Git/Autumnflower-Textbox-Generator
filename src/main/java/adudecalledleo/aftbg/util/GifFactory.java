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

    public static int toFrames(double seconds, int delayTime) {
        return (int) (seconds / (delayTime * 0.01));
    }

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
            BufferedImage lastFrame = null;
            boolean wroteLastFrame = false;
            int thisDelayTime = delayTime;
            for (BufferedImage frame : frames) {
                if (frame == lastFrame) {
                    thisDelayTime += delayTime;
                    wroteLastFrame = false;
                } else {
                    fillDelayTimeOnly(imageRoot, thisDelayTime);
                    imageMeta.mergeTree(imageMetaFormatName, imageRoot);
                    writer.writeToSequence(new IIOImage(lastFrame == null ? frame : lastFrame,
                            null, imageMeta), writer.getDefaultWriteParam());
                    thisDelayTime = delayTime;
                    wroteLastFrame = true;
                    lastFrame = frame;
                }
            }
            if (lastFrame != null && !wroteLastFrame) {
                fillDelayTimeOnly(imageRoot, thisDelayTime);
                imageMeta.mergeTree(imageMetaFormatName, imageRoot);
                writer.writeToSequence(new IIOImage(lastFrame, null, imageMeta), writer.getDefaultWriteParam());
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
        fillGraphicsControlExtension(graphicsControl, delayTime);
        IIOMetadataNode applications = getOrCreateNode(root, "ApplicationExtensions");
        IIOMetadataNode application = new IIOMetadataNode("ApplicationExtension");
        fillApplicationExtension(application);
        applications.appendChild(application);
        if (comment != null) {
            IIOMetadataNode comments = getOrCreateNode(root, "CommentExtensions");
            comments.setAttribute("CommentExtension", comment);
        }
    }

    // region Metadata stuff
    private static void fillGraphicsControlExtension(IIOMetadataNode node, int delayTime) {
        node.setAttribute("disposalMethod", "none");
        node.setAttribute("userInputFlag", "FALSE");
        node.setAttribute("transparentColorFlag", "FALSE");
        node.setAttribute("transparentColorIndex", "0");
        node.setAttribute("delayTime", Integer.toString(delayTime));
    }

    private static void fillApplicationExtension(IIOMetadataNode node) {
        node.setAttribute("applicationID", "NETSCAPE");
        node.setAttribute("authenticationCode", "2.0");
        node.setUserObject(new byte[] { 0x01, 0x00, 0x00 });
    }

    private static void fillDelayTimeOnly(IIOMetadataNode root, int delayTime) {
        IIOMetadataNode graphicsControl = getOrCreateNode(root, "GraphicControlExtension");
        graphicsControl.setAttribute("delayTime", Integer.toString(delayTime));
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
