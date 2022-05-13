package adudecalledleo.aftbg.app.util;

import java.awt.*;
import java.awt.geom.*;

public record GraphicsState(Composite composite,
                            Paint paint, Stroke stroke,
                            RenderingHints renderingHints, AffineTransform transform,
                            Color foreground, Color background, Shape clip, Font font) {
    public static GraphicsState save(Graphics2D g) {
        return new GraphicsState(g.getComposite(), g.getPaint(), g.getStroke(), g.getRenderingHints(),
                g.getTransform(), g.getColor(), g.getBackground(), g.getClip(), g.getFont());
    }

    public void restore(Graphics2D g) {
        g.setComposite(composite);
        g.setColor(foreground);
        g.setPaint(paint);
        g.setStroke(stroke);
        g.setRenderingHints(renderingHints);
        g.setTransform(transform);
        g.setBackground(background);
        g.setClip(clip);
        g.setFont(font);
    }
}
