package adudecalledleo.aftbg.app;

import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.modifier.ColorModifierNode;
import adudecalledleo.aftbg.text.node.*;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;
import adudecalledleo.aftbg.window.WindowText;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public final class TextboxEditorPane extends JEditorPane {
    private final TextParser textParser;
    private final Consumer<String> textUpdateConsumer;
    private final Timer updateTimer;
    private final SimpleAttributeSet styleNormal, styleMod, styleError;

    private WindowContext winCtx;
    private boolean hasFace;

    public TextboxEditorPane(TextParser textParser, WindowContext winCtx, Consumer<String> textUpdateConsumer) {
        super();
        this.textParser = textParser;
        this.winCtx = winCtx;
        this.textUpdateConsumer = textUpdateConsumer;
        hasFace = false;

        setEditorKit(new EditorKit());
        setDocument(new StyledDocumentImpl());

        updateTimer = new Timer(1000, e -> {
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
        styleError = new SimpleAttributeSet(styleNormal);
        StyleConstants.setBackground(styleError, Color.RED);
        StyleConstants.setForeground(styleError, Color.WHITE);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateTimer.restart();
            }
        });

        setOpaque(true);
        setBackground(ColorUtils.TRANSPARENT);

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
            MutableAttributeSet style = styleNormal;
            doc.setParagraphAttributes(0, doc.getLength(), style, true);
            doc.setCharacterAttributes(0, doc.getLength(), style, true);

            NodeList nodes;
            try {
                nodes = textParser.parse(doc.getText(0, doc.getLength()));
            } catch (BadLocationException e) {
                e.printStackTrace();
                return;
            }

            for (Node node : nodes) {
                System.out.println(node);
                if (node instanceof ColorModifierNode modCol) {
                    doc.setCharacterAttributes(modCol.getStart(), modCol.getLength(), styleMod, true);
                    Color c = modCol.getColor(winCtx.getColors());
                    style = new SimpleAttributeSet(styleNormal);
                    StyleConstants.setForeground(style, c);
                    Span argSpan = modCol.getArgSpans()[0];
                    doc.setCharacterAttributes(argSpan.start(), argSpan.length(), style, true);
                } else if (node instanceof ErrorNode err) {
                    // TODO squiggly line instead?
                    // TODO tooltip malarkey??
                    doc.setCharacterAttributes(err.getStart(), err.getLength(), styleError, true);
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

    private static final class EditorKit extends DefaultEditorKit {
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
