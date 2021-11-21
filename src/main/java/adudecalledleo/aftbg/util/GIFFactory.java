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
            IIOMetadataNode graphicsControl = getNode(root, "GraphicControlExtension");
            graphicsControl.setAttribute("disposalMethod", "none");
            graphicsControl.setAttribute("userInputFlag", "FALSE");
            graphicsControl.setAttribute("transparentColorFlag", "FALSE");
            graphicsControl.setAttribute("transparentColorIndex", "0");
            graphicsControl.setAttribute("delayTime", Integer.toString(delayTime));
            if (comment != null) {
                IIOMetadataNode comments = getNode(root, "CommentExtensions");
                comments.setAttribute("CommentExtension", comment);
            }
            IIOMetadataNode application = getNode(root, "ApplicationExtensions");
            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");
            child.setUserObject(new byte[] { 1, 0, 0 });
            application.appendChild(child);
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

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++)
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0)
                return (IIOMetadataNode) rootNode.item(i);
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}
