package adudecalledleo.aftbg.app.component.text;

import java.awt.*;

import javax.swing.text.*;

public final class ZigZagHighlighter extends DefaultHighlighter.DefaultHighlightPainter {
    private static final BasicStroke STROKE_1 = new BasicStroke(0.01F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10, new float[] { 1, 3 }, 0);
    private static final BasicStroke STROKE_2 = new BasicStroke(0.01F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10, new float[] { 1, 1 }, 1);
    private static final BasicStroke STROKE_3 = new BasicStroke(0.01F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10, new float[] { 1, 3 }, 2);

    public ZigZagHighlighter(Color c) {
        super(c);
    }

    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
        Color color = getColor();

        if (color == null) {
            g.setColor(c.getSelectionColor());
        }
        else {
            g.setColor(color);
        }

        if (c.isEditable()) {
            try {
                Shape sh = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                Rectangle rect = (sh instanceof Rectangle) ? (Rectangle) sh : sh.getBounds();
                drawZigZagLine(g, rect);
                return rect;
            } catch (BadLocationException badlocationexception){
                return null;
            }
        }
        return null;
    }

    private static void drawZigZagLine(Graphics g, Rectangle rect){
        int x1 = rect.x;
        int x2 = x1 + rect.width - 1;
        int y = rect.y + rect.height - 1;
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(STROKE_1);
        g2.drawLine(x1, y, x2, y);
        y--;
        g2.setStroke(STROKE_2);
        g2.drawLine(x1, y, x2, y);
        y--;
        g2.setStroke(STROKE_3);
        g2.drawLine(x1, y, x2, y);
    }
}
