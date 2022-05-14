package adudecalledleo.aftbg.app.text;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Map;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.text.node.ContainerNode;
import adudecalledleo.aftbg.app.text.node.Node;
import adudecalledleo.aftbg.app.text.node.TextNode;
import adudecalledleo.aftbg.app.text.node.color.ColorNode;
import adudecalledleo.aftbg.app.text.node.gimmick.GimmickNode;
import adudecalledleo.aftbg.app.text.node.gimmick.TextFill;
import adudecalledleo.aftbg.app.text.node.gimmick.TextFlip;
import adudecalledleo.aftbg.app.text.node.style.FontStyleModifyingNode;
import adudecalledleo.aftbg.app.text.node.style.StyleNode;
import adudecalledleo.aftbg.app.text.util.FontStyle;
import adudecalledleo.aftbg.app.util.GraphicsState;
import adudecalledleo.aftbg.app.util.RainbowPaint;

public final class DOMRenderer {
    public static final Color OUTLINE_COLOR = new Color(0, 0, 0, 127);
    public static final int OUTLINE_WIDTH = 4;
    private static final Stroke OUTLINE_STROKE = new BasicStroke(OUTLINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    // this secondary outline is drawn with the actual text color, to try and replicate RPG Maker MV (AKA browser) AA
    public static final int OUTLINE2_WIDTH = 1;
    public static final float OUTLINE2_OPAQUENESS = 0.275f;
    private static final Stroke OUTLINE2_STROKE = new BasicStroke(OUTLINE2_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
    public static final Composite OUTLINE2_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OUTLINE2_OPAQUENESS);

    public static final Font DEFAULT_FONT = AppResources.getFont().deriveFont(Font.PLAIN, 28);

    private DOMRenderer() { }

    private static final Map<FontStyle, Font> STYLED_FONTS = new HashMap<>();
    private static final ThreadLocal<Map<TextAttribute, Object>> TL_ATTR_MAP =
            ThreadLocal.withInitial(() -> new HashMap<>(6));

    public static Font getStyledFont(FontStyle style) {
        return STYLED_FONTS.computeIfAbsent(style, key -> {
            Map<TextAttribute, Object> map = TL_ATTR_MAP.get();
            map.put(TextAttribute.WEIGHT, key.bold() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
            map.put(TextAttribute.POSTURE, key.italic() ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
            map.put(TextAttribute.UNDERLINE, key.underline() ? TextAttribute.UNDERLINE_ON : -1);
            map.put(TextAttribute.STRIKETHROUGH, key.strikethrough() ? TextAttribute.STRIKETHROUGH_ON : false);
            map.put(TextAttribute.SUPERSCRIPT, switch (key.superscript()) {
                case MID -> 0;
                case SUPER -> TextAttribute.SUPERSCRIPT_SUPER;
                case SUB -> TextAttribute.SUPERSCRIPT_SUB;
            });
            map.put(TextAttribute.SIZE, DEFAULT_FONT.getSize() + key.sizeAdjust());
            return DEFAULT_FONT.deriveFont(map);
        });
    }

    static {
        STYLED_FONTS.put(FontStyle.DEFAULT, DEFAULT_FONT);
    }

    public static void render(Graphics2D g, Node root, int x, int y) {
        var oldState = GraphicsState.save(g);

        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(DEFAULT_FONT);
        final int defaultMaxAscent = g.getFontMetrics().getMaxAscent();
        var data = new RendererData(g, oldState, defaultMaxAscent, x, y);

        DOMRenderer.render0(root, data);

        oldState.restore(g);
    }

    private static final class RendererData {
        public final Graphics2D graphics;
        public final GraphicsState oldState;
        public final int defaultMaxAscent;
        public final int startX;
        public final StringBuilder sb;
        public final AffineTransform tx, tx2;

        public int x, y;
        public FontStyle fontStyle;
        public TextFill textFill;
        public TextFlip textFlip;

        public RendererData(Graphics2D graphics, GraphicsState oldState,
                            int defaultMaxAscent, int startX, int startY) {
            this.graphics = graphics;
            this.oldState = oldState;
            this.defaultMaxAscent = defaultMaxAscent;
            this.startX = x = startX;
            this.y = startY;
            this.sb = new StringBuilder();
            this.tx = new AffineTransform();
            this.tx2 = new AffineTransform();
            this.fontStyle = FontStyle.DEFAULT;
            this.textFill = TextFill.COLOR;
            this.textFlip = TextFlip.NONE;
        }

        public void resetX() {
            this.x = startX;
        }

        public void updateFont(Graphics2D g) {
            g.setFont(getStyledFont(fontStyle));
        }
    }

    public static void render0(Node node, RendererData data) {
        final Graphics2D g = data.graphics;
        final StringBuilder sb = data.sb;

        if (node instanceof TextNode nText) {
            for (var c : nText.getContents().toCharArray()) {
                if (c == '\n') {
                    if (!sb.isEmpty()) {
                        renderText(data, sb.toString(), data.x, data.y);
                        sb.setLength(0);
                    }
                    data.resetX();
                    data.y += 36;
                } else {
                    sb.append(c);
                }
            }

            if (!sb.isEmpty()) {
                data.x += renderText(data, sb.toString(), data.x, data.y);
                sb.setLength(0);
            }
        } else if (node instanceof ContainerNode nContainer) {
            boolean colorChanged = false;
            Color oldColor = g.getColor();
            boolean styleChanged = false;
            FontStyle oldStyle = data.fontStyle;
            boolean fillChanged = false;
            TextFill oldFill = data.textFill;
            boolean flipChanged = false;
            TextFlip oldFlip = data.textFlip;

            if (node instanceof ColorNode nColor) {
                colorChanged = true;
                g.setColor(nColor.getColor());
            } else if (node instanceof StyleNode nStyle) {
                if (nStyle.isColorSet()) {
                    colorChanged = true;
                    g.setColor(nStyle.getColor());
                }
                if (nStyle.getSize() != null) {
                    styleChanged = true;
                    data.fontStyle = data.fontStyle.withSizeAdjust(nStyle.getSize());
                    data.updateFont(g);
                }
            } else if (node instanceof FontStyleModifyingNode nFSM) {
                styleChanged = true;
                data.fontStyle = nFSM.updateStyle(data.fontStyle);
                data.updateFont(g);
            } else if (node instanceof GimmickNode nGimmick) {
                if (nGimmick.getFill() != null) {
                    fillChanged = true;
                    data.textFill = nGimmick.getFill();
                }
                if (nGimmick.getFlip() != null) {
                    flipChanged = true;
                    data.textFlip = nGimmick.getFlip();
                }
            }

            for (var child : nContainer.getChildren()) {
                render0(child, data);
            }

            if (colorChanged) {
                g.setColor(oldColor);
            }
            if (styleChanged) {
                data.fontStyle = oldStyle;
                data.updateFont(g);
            }
            if (fillChanged) {
                data.textFill = oldFill;
            }
            if (flipChanged) {
                data.textFlip = oldFlip;
            }
        }
    }

    private static int renderText(RendererData data, String text, int x, int y) {
        final Graphics2D g = data.graphics;
        final GraphicsState oldState = data.oldState;
        final int defaultMaxAscent = data.defaultMaxAscent;
        final AffineTransform tx = data.tx, tx2 = data.tx2;

        // make the text vertically centered
        final int yo = defaultMaxAscent / 2 - g.getFontMetrics().getMaxAscent() / 2;

        // generate an outline of the text
        var layout = new TextLayout(text, g.getFont(), g.getFontRenderContext());

        boolean flipH = data.textFlip.isHorizontal(), flipV = data.textFlip.isVertical();

        Shape outline;

        if (flipH || flipV) {
            var bounds = layout.getBounds();
            double moveX = x;
            if (flipH) {
                moveX += bounds.getWidth();
            }
            double moveY = y + defaultMaxAscent - yo;
            if (flipV) {
                moveY -= bounds.getHeight();
            }
            tx.setToScale(flipH ? -1 : 1, flipV ? -1 : 1);
            tx2.setToTranslation(moveX, moveY);
            outline = layout.getOutline(tx);
            outline = tx2.createTransformedShape(outline);
        } else {
            tx.setToTranslation(x, y + defaultMaxAscent - yo);
            outline = layout.getOutline(tx);
        }

        var c = g.getColor(); // (save the actual text color for later)
        // draw a transparent outline of the text...
        g.setStroke(OUTLINE_STROKE);
        g.setColor(OUTLINE_COLOR);
        g.draw(outline);
        switch (data.textFill) {
            case COLOR -> {
                g.setColor(c);
                // ...then draw a secondary outline...
                g.setComposite(OUTLINE2_COMPOSITE);
                g.setStroke(OUTLINE2_STROKE);
                g.draw(outline);
                // ...and then, fill in the text!
                g.setComposite(oldState.composite());
            }
            case RAINBOW -> g.setPaint(RainbowPaint.get());
            default -> throw new IllegalStateException("Unsupported fill type " + data.textFill + "!");
        }
        g.fill(outline);
        g.setColor(c);

        return (int) layout.getAdvance();
    }
}
