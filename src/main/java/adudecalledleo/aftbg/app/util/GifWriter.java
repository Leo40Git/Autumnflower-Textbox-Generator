package adudecalledleo.aftbg.app.util;

import java.awt.image.*;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.jetbrains.annotations.Nullable;

public final class GifWriter implements AutoCloseable {
    public static int toFrames(double seconds, int delayTime) {
        return (int) (seconds / (delayTime * 0.01));
    }

    private final ImageOutputStream output;
    private final int delayTime;
    private final @Nullable String comment;
    private final ImageWriter writer;

    private boolean initialized;
    private IIOMetadata imageMeta;
    private String imageMetaFormatName;
    private IIOMetadataNode imageRoot;

    public GifWriter(ImageOutputStream output, int delayTime, @Nullable String comment) {
        this.output = output;
        this.delayTime = delayTime;
        this.comment = comment;

        writer = ImageIO.getImageWritersByFormatName("gif").next();
        writer.setOutput(output);

        initialized = false;
    }

    public GifWriter(ImageOutputStream output, int delayTime) {
        this(output, delayTime, null);
    }

    public void writeFrame(BufferedImage frame, int repeatCount) throws IOException {
        if (!initialized) {
            imageMeta = writer.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(frame.getType()),
                    writer.getDefaultWriteParam());
            imageMetaFormatName = imageMeta.getNativeMetadataFormatName();
            imageRoot = (IIOMetadataNode) imageMeta.getAsTree(imageMetaFormatName);
            fillImageMetadata(imageRoot, delayTime, comment);
            imageMeta.setFromTree(imageMetaFormatName, imageRoot);

            writer.prepareWriteSequence(null);

            initialized = true;
        }

        int thisDelayTime = delayTime * repeatCount;
        fillDelayTimeOnly(imageRoot, thisDelayTime);
        imageMeta.mergeTree(imageMetaFormatName, imageRoot);
        writer.writeToSequence(new IIOImage(frame, null, imageMeta), writer.getDefaultWriteParam());
    }

    @Override
    public void close() throws IOException {
        writer.endWriteSequence();
        output.close();
    }

    // region Metadata stuff
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
