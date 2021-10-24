package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.modifier.ColorModifierNode;
import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.Node;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;
import adudecalledleo.aftbg.window.WindowText;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class TextboxEditorPane extends JEditorPane {
    private final TextParser textParser;
    private final Consumer<String> textUpdateConsumer;
    private final Timer updateTimer;
    private final Map<Rectangle2D, String> errors;
    private final Line2D scratchLine;
    private final SimpleAttributeSet styleNormal, styleMod;
    private final Map<Color, AttributeSet> coloredStyles;

    private WindowContext winCtx;
    private boolean hasFace;

    public TextboxEditorPane(TextParser textParser, WindowContext winCtx, Consumer<String> textUpdateConsumer) {
        super();
        this.textParser = textParser;
        this.winCtx = winCtx;
        this.textUpdateConsumer = textUpdateConsumer;
        errors = new HashMap<>();
        scratchLine = new Line2D.Double(0, 0, 0, 0);
        hasFace = false;

        setEditorKit(new EditorKitImpl());
        setDocument(new StyledDocumentImpl());

        updateTimer = new Timer(500, e -> {
            SwingUtilities.invokeLater(() -> {
                textUpdateConsumer.accept(getText());
                highlight();
            });
        });
        updateTimer.setRepeats(false);
        updateTimer.setCoalesce(true);

        setCaretColor(winCtx.getColor(0));

        setFont(WindowText.FONT);
        setForeground(winCtx.getColor(0));

        styleNormal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleNormal, WindowText.FONT.getFamily());
        StyleConstants.setFontSize(styleNormal, WindowText.FONT.getSize());
        StyleConstants.setForeground(styleNormal, winCtx.getColor(0));
        styleMod = new SimpleAttributeSet(styleNormal);
        StyleConstants.setForeground(styleMod, Color.GRAY);
        coloredStyles = new HashMap<>();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateTimer.restart();
            }
        });

        setOpaque(true);
        setBackground(ColorUtils.TRANSPARENT);

        ToolTipManager.sharedInstance().registerComponent(this);

        highlight();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setBackground(ColorUtils.TRANSPARENT);
        g2d.clearRect(0, 0, getWidth(), getHeight());
        winCtx.drawBackground((Graphics2D) g, 0, 0, getWidth(), getHeight(), null);
        super.paintComponent(g);

        g2d.setColor(Color.RED);
        for (var entry : errors.entrySet()) {
            var rect = entry.getKey();
            var oldClip = g2d.getClip();
            g2d.clip(rect);
            final double y = rect.getY() + rect.getHeight() - 3;
            boolean raised = true;
            for (double x = rect.getX(); x <= rect.getX() + rect.getWidth() + 2; x += 2) {
                scratchLine.setLine(x, y + (raised ? 2 : 0), x + 2, y + (raised ? 0 : 2));
                g2d.draw(scratchLine);
                raised = !raised;
            }
            g2d.clip(oldClip);
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        var point = event.getPoint();
        for (var entry : errors.entrySet()) {
            if (entry.getKey().contains(point.x, point.y)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void setHasFace(boolean hasFace) {
        this.hasFace = hasFace;
    }

    public void setWindowContext(WindowContext winCtx) {
        this.winCtx = winCtx;
        updateTimer.stop();
        SwingUtilities.invokeLater(() -> {
            repaint();
            textUpdateConsumer.accept(getText());
            highlight();
        });
    }

    private void highlight() {
        if (getDocument() instanceof StyledDocument doc) {
            AttributeSet style = styleNormal;
            doc.setParagraphAttributes(0, doc.getLength(), style, true);
            doc.setCharacterAttributes(0, doc.getLength(), style, true);

            NodeList nodes;
            try {
                nodes = textParser.parse(doc.getText(0, doc.getLength()));
            } catch (BadLocationException e) {
                e.printStackTrace();
                return;
            }

            errors.clear();

            for (Node node : nodes) {
                System.out.println(node);
                if (node instanceof ColorModifierNode modCol) {
                    doc.setCharacterAttributes(modCol.getStart(), modCol.getLength(), styleMod, true);
                    Color c = modCol.getColor(winCtx.getColors());
                    style = coloredStyles.computeIfAbsent(c, color -> {
                        var colStyle = new SimpleAttributeSet(styleNormal);
                        StyleConstants.setForeground(colStyle, c);
                        return colStyle;
                    });
                    Span argSpan = modCol.getArgSpans()[0];
                    doc.setCharacterAttributes(argSpan.start(), argSpan.length(), style, true);
                } else if (node instanceof ErrorNode err) {
                    doc.setCharacterAttributes(err.getStart(), err.getLength(), styleNormal, true);

                    Rectangle2D startRect, endRect;
                    final int end = err.getStart() + err.getLength();
                    try {
                        startRect = modelToView2D(err.getStart());
                        endRect = modelToView2D(end);

                        errors.put(new Rectangle2D.Double(startRect.getX(), startRect.getY(),
                                endRect.getX() - startRect.getX(), Math.max(startRect.getHeight(), endRect.getHeight())),
                                err.getMessage());
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                } else {
                    doc.setCharacterAttributes(node.getStart(), node.getLength(), style, true);
                }
            }
        }
    }

    private static final class StyledDocumentImpl extends DefaultStyledDocument {
        @Override
        public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
            str = str.replaceAll("\t", "    ");
            super.insertString(offs, str, a);
        }
    }

    private static final class EditorKitImpl extends DefaultEditorKit {
        @Override
        public ViewFactory getViewFactory() {
            return new ViewFactoryImpl();
        }

        private static class ViewFactoryImpl implements ViewFactory {
            @Override
            public View create(final Element elem) {
                final String kind = elem.getName();
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new LabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new ParagraphViewImpl(elem);
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                    default:
                        break;
                }
                return new LabelView(elem);
            }

            private static class ParagraphViewImpl extends ParagraphView {
                public ParagraphViewImpl(final Element elem) {
                    super(elem);
                }

                @Override
                protected void layout(final int width, final int height) {
                    super.layout(Short.MAX_VALUE, height);
                }

                @Override
                public float getMinimumSpan(final int axis) {
                    return super.getPreferredSpan(axis);
                }
            }
        }
    }
}
