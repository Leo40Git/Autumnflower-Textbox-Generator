package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.data.DataTracker;
import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.DOMRenderer;
import adudecalledleo.aftbg.app.text.node.ContainerNode;
import adudecalledleo.aftbg.app.text.node.Node;
import adudecalledleo.aftbg.app.text.node.Span;
import adudecalledleo.aftbg.app.text.node.color.ColorNode;
import adudecalledleo.aftbg.app.text.node.color.ColorParser;
import adudecalledleo.aftbg.app.text.node.style.FontStyleModifyingNode;
import adudecalledleo.aftbg.app.text.node.style.StyleNode;
import adudecalledleo.aftbg.app.ui.dialog.modifier.ColorModifierDialog;
import adudecalledleo.aftbg.app.ui.text.UnderlineHighlighter;
import adudecalledleo.aftbg.app.ui.text.ZigZagHighlighter;
import adudecalledleo.aftbg.app.ui.util.ErrorMessageBuilder;
import adudecalledleo.aftbg.app.ui.util.IconWithArrow;
import adudecalledleo.aftbg.app.ui.util.UnmodifiableAttributeSetView;
import adudecalledleo.aftbg.app.util.ColorUtils;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxEditorPane extends JEditorPane
        implements GameDefinitionUpdateListener, ActionListener, DOMParser.SpanTracker {
    public static final String A_TOOLBAR_BOLD = "toolbar.bold";
    public static final String A_TOOLBAR_ITALIC = "toolbar.italic";
    public static final String A_TOOLBAR_UNDERLINE = "toolbar.underline";
    public static final String A_TOOLBAR_STRIKETHROUGH = "toolbar.strikethrough";
    public static final String A_TOOLBAR_SUPERSCRIPT = "toolbar.superscript";
    public static final String A_TOOLBAR_SUBSCRIPT = "toolbar.subscript";
    public static final String A_TOOLBAR_COLOR = "toolbar.color";

    private static final BufferedImage SCRATCH_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private static final Highlighter.HighlightPainter HLP_ERROR = new ZigZagHighlighter(Color.RED);
    private final Map<Color, UnderlineHighlighter> hlpEscapedColors = new HashMap<>();

    private Highlighter.HighlightPainter getEscapedColorHighlightPainter(Color color) {
        return hlpEscapedColors.computeIfAbsent(color, UnderlineHighlighter::new);
    }

    private final DataTracker parserCtx;
    private final Consumer<String> textUpdateConsumer;
    private final Timer updateTimer;
    private final Map<Object, Action> actions;
    private final Map<Rectangle2D, String> errors;
    private final SimpleAttributeSet styleNormal, styleMod;
    private final JPopupMenu popupMenu;
    private final JToolBar toolBar;

    private GameDefinition gameDef;
    private WindowContext winCtx;
    private Color defaultTextColor;
    private boolean forceCaretRendering;
    private Face textboxFace;
    private final List<Span> escapedSpans;
    private final Set<Span> nodeDeclSpans;

    public TextboxEditorPane(Consumer<String> textUpdateConsumer) {
        super();
        this.textUpdateConsumer = textUpdateConsumer;

        parserCtx = new DataTracker();
        errors = new HashMap<>();

        styleNormal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleNormal, DOMRenderer.DEFAULT_FONT.getFamily());
        StyleConstants.setFontSize(styleNormal, DOMRenderer.DEFAULT_FONT.getSize());
        StyleConstants.setForeground(styleNormal, Color.WHITE);
        styleMod = new SimpleAttributeSet(styleNormal);
        StyleConstants.setForeground(styleMod, Color.GRAY);

        setDocument(new StyledDocumentImpl());
        setEditorKit(new EditorKitImpl(styleNormal));

        updateTimer = new Timer(250, e -> SwingUtilities.invokeLater(() -> {
            textUpdateConsumer.accept(getText());
            highlight();
        }));
        updateTimer.setRepeats(false);
        updateTimer.setCoalesce(true);

        actions = createActionTable();

        Graphics2D g = SCRATCH_IMAGE.createGraphics();
        g.setFont(DOMRenderer.DEFAULT_FONT);
        var fm = g.getFontMetrics();
        var size = new Dimension(816, fm.getHeight() * 4 + fm.getDescent());
        g.dispose();

        setMinimumSize(size);
        setPreferredSize(size);

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // this one is ignored, because we're the only ones updating the attributes
            }
        });

        setOpaque(true);
        setBackground(ColorUtils.TRANSPARENT);

        ToolTipManager.sharedInstance().registerComponent(this);
        popupMenu = createPopupMenu();
        addMouseListener(new MouseAdapter() {
            private final Position.Bias[] biasRet = new Position.Bias[1];

            private void mousePopupTriggered(MouseEvent e) {
                var point = e.getPoint();
                if (getCaret().getMark() < 0) /* if there are no selected characters */ {
                    int pos = getUI().viewToModel2D(TextboxEditorPane.this, point, biasRet);
                    if (pos >= 0) {
                        setCaretPosition(pos);
                    }
                }
                popupMenu.show(TextboxEditorPane.this, point.x, point.y);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    e.consume();
                    mousePopupTriggered(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    e.consume();
                    mousePopupTriggered(e);
                }
            }
        });

        toolBar = createToolBar();

        this.defaultTextColor = Color.WHITE;
        this.forceCaretRendering = false;
        this.escapedSpans = new LinkedList<>();
        this.nodeDeclSpans = new HashSet<>();

        highlight();
    }

    private Map<Object, Action> createActionTable() {
        HashMap<Object, Action> actions = new HashMap<>();
        Action[] actionsArray = getActions();
        for (Action a : actionsArray) {
            actions.put(a.getValue(Action.NAME), a);
        }
        return actions;
    }

    private JPopupMenu createPopupMenu() {
        final var menu = new JPopupMenu();
        JMenuItem item;

        item = new JMenuItem(actions.get(DefaultEditorKit.cutAction));
        item.setText("Cut");
        item.setIcon(AppResources.Icons.CUT.get());
        menu.add(item);
        item = new JMenuItem(actions.get(DefaultEditorKit.copyAction));
        item.setText("Copy");
        item.setIcon(AppResources.Icons.COPY.get());
        menu.add(item);
        item = new JMenuItem(actions.get(DefaultEditorKit.pasteAction));
        item.setText("Paste");
        item.setIcon(AppResources.Icons.PASTE.get());
        menu.add(item);

        return menu;
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    private JToolBar createToolBar() {
        var bar = new JToolBar("Style");
        bar.setRollover(true);
        bar.add(createToolBarButton(A_TOOLBAR_BOLD, "Bold", AppResources.Icons.TOOLBAR_BOLD));
        bar.add(createToolBarButton(A_TOOLBAR_ITALIC, "Italic", AppResources.Icons.TOOLBAR_ITALIC));
        bar.add(createToolBarButton(A_TOOLBAR_UNDERLINE, "Underline", AppResources.Icons.TOOLBAR_UNDERLINE));
        bar.add(createToolBarButton(A_TOOLBAR_STRIKETHROUGH, "Strikethrough", AppResources.Icons.TOOLBAR_STRIKETHROUGH));
        bar.add(createToolBarButton(A_TOOLBAR_SUPERSCRIPT, "Superscript", AppResources.Icons.TOOLBAR_SUPERSCRIPT));
        bar.add(createToolBarButton(A_TOOLBAR_SUBSCRIPT, "Subscript", AppResources.Icons.TOOLBAR_SUBSCRIPT));

        bar.addSeparator();

        bar.add(createToolBarButton(A_TOOLBAR_COLOR, "Color", new IconWithArrow(AppResources.Icons.TOOLBAR_COLOR.get())));

        return bar;
    }

    private JButton createToolBarButton(String actionCmd, String text, Icon icon) {
        var btn = new JButton();
        btn.setIcon(icon);
        btn.setToolTipText(text);
        btn.setActionCommand(actionCmd);
        btn.addActionListener(this);
        return btn;
    }

    private JButton createToolBarButton(String actionCmd, String text, AppResources.Icons icon) {
        return createToolBarButton(actionCmd, text, icon.get());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case A_TOOLBAR_BOLD -> wrapSelectionInTag("b");
            case A_TOOLBAR_ITALIC -> wrapSelectionInTag("i");
            case A_TOOLBAR_UNDERLINE -> wrapSelectionInTag("u");
            case A_TOOLBAR_STRIKETHROUGH -> wrapSelectionInTag("s");
            case A_TOOLBAR_SUPERSCRIPT -> wrapSelectionInTag("sup");
            case A_TOOLBAR_SUBSCRIPT -> wrapSelectionInTag("sub");
            case A_TOOLBAR_COLOR -> {
                if (winCtx == null) {
                    UIManager.getLookAndFeel().provideErrorFeedback(this);
                    Logger.info("Tried to open color modifier dialog without window context!");
                    return;
                }

                try {
                    forceCaretRendering = true;
                    var result = new ColorModifierDialog(this, winCtx).showDialog();
                    String value;
                    if (result instanceof ColorModifierDialog.ConstantResult rConst) {
                        var c = rConst.getColor();
                        value = "#%02X%02X%02X".formatted(c.getRed(), c.getGreen(), c.getBlue());
                    } else if (result instanceof ColorModifierDialog.PaletteResult rPal) {
                        value = "pal(%d)".formatted(rPal.getIndex());
                    } else {
                        UIManager.getLookAndFeel().provideErrorFeedback(this);
                        Logger.info("Got unknown result " + result + "!");
                        return;
                    }
                    wrapSelectionInTag("c=" + value, "c");
                } finally {
                    forceCaretRendering = false;
                }
            }
        }
    }

    public void wrapSelectionInTag(String tagStart, String tagEnd) {
        final var doc = getDocument();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        int length = end - start;
        if (length <= 0) {
            // no selection! insert an empty tag, and move the caret into it
            try {
                doc.insertString(start, "[%s][/%s]".formatted(tagStart, tagEnd), styleNormal);
            } catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(this);
                Logger.info("Failed to insert empty tag!", e);
                return;
            }
            select(start + 2 + tagStart.length(), 0);
        } else {
            // we have a selection! wrap it in a tag
            String selectedText;
            try {
                selectedText = doc.getText(start, length);
            } catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(this);
                Logger.info("Failed to get selected text!", e);
                return;
            }
            String newText = "[%s]%s[/%s]".formatted(tagStart, selectedText, tagEnd);
            try {
                doc.remove(start, length);
                doc.insertString(start, newText, styleNormal);
            } catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(this);
                Logger.info("Failed to replace selected text!", e);
                return;
            }
            select(start + 2 + tagStart.length(), end + 2 + tagStart.length());
        }
        requestFocus();
    }

    public void wrapSelectionInTag(String tagName) {
        wrapSelectionInTag(tagName, tagName);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setBackground(ColorUtils.TRANSPARENT);
        g2d.clearRect(0, 0, getWidth(), getHeight());
        winCtx.drawBackground(g2d, 0, 0, getWidth(), getHeight(), null);

        if (popupMenu.isVisible() || forceCaretRendering) {
            // force caret to be drawn, so user knows where pasted text/modifiers will be inserted
            getCaret().setVisible(true);
            getCaret().setSelectionVisible(true);
        }

        super.paintComponent(g);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        ErrorMessageBuilder emb = null;
        var point = event.getPoint();
        for (var entry : errors.entrySet()) {
            if (entry.getKey().contains(point.x, point.y)) {
                if (emb == null) {
                    emb = new ErrorMessageBuilder(entry.getValue());
                } else {
                    emb.add(entry.getValue());
                }
            }
        }
        if (emb == null) {
            return null;
        } else {
            return emb.toString();
        }
    }

    public void flushChanges(boolean highlight) {
        updateTimer.stop();
        SwingUtilities.invokeLater(() -> {
            textUpdateConsumer.accept(getText());
            if (highlight) {
                highlight();
            }
        });
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        updateTimer.stop();
        SwingUtilities.invokeLater(this::highlight);
    }

    public void setTextboxFace(Face textboxFace) {
        this.textboxFace = textboxFace;
    }

    @Override
    public void updateGameDefinition(GameDefinition gameDef) {
        hlpEscapedColors.clear();

        this.gameDef = gameDef;
        this.winCtx = gameDef.winCtx().copy();
        setCaretColor(winCtx.getColor(0));
        StyleConstants.setForeground(styleNormal, winCtx.getColor(0));
        var pal = winCtx.getColors();
        defaultTextColor = pal.get(0);
        parserCtx.set(ColorParser.PALETTE, pal);
        flushChanges(true);
    }

    private void highlight() {
        errors.clear();
        escapedSpans.clear();
        if (getDocument() instanceof StyledDocument doc) {
            MutableAttributeSet style = styleNormal, styleEscaped = styleMod;
            doc.setParagraphAttributes(0, doc.getLength(), style, true);
            doc.setCharacterAttributes(0, doc.getLength(), style, true);
            getHighlighter().removeAllHighlights();

            var result = DOMParser.parse(getText(), parserCtx, this);

            highlight0(doc, result.document().getChildren(), style, styleEscaped);
            highlightTrackedSpans(doc);

            if (result.hasErrors()) {
                for (var error : result.errors()) {
                    try {
                        Rectangle2D startRect, endRect;

                        startRect = modelToView2D(error.start());
                        endRect = modelToView2D(error.end());

                        var errorRect = new Rectangle2D.Double(startRect.getX(), startRect.getY(),
                                endRect.getX() - startRect.getX(), Math.max(startRect.getHeight(), endRect.getHeight()));
                        errors.put(errorRect, error.message() + " (" + (error.start() + 1) + "-" + (error.end() + 1) + ")");
                    } catch (BadLocationException e) {
                        Logger.info("Failed to generate tooltip bounds for error", e);
                    }

                    try {
                        getHighlighter().addHighlight(error.start(), error.end(), HLP_ERROR);
                    } catch (BadLocationException e) {
                        Logger.info("Failed to properly highlight error", e);
                    }
                }
            }
        }
    }

    @Override
    public void markEscaped(int start, int end) {
        Logger.trace("adding escaped span - %d, %d".formatted(start, end));
        this.escapedSpans.add(new Span(start, end - start));
    }

    @Override
    public void markNodeDecl(String node, int start, int end) {
        this.nodeDeclSpans.add(new Span(start, end - start));
    }

    private void highlight0(StyledDocument doc, List<Node> nodes, MutableAttributeSet style, MutableAttributeSet styleEscaped) {
        var oldStyle = style;
        var oldStyleEscaped = styleEscaped;
        for (var node : nodes) {
            style = oldStyle;
            styleEscaped = oldStyleEscaped;

            var opening = node.getOpeningSpan();
            var closing = node.getClosingSpan();
            if (!opening.isValid() || !closing.isValid()) {
                continue;
            }
            nodeDeclSpans.remove(opening);
            nodeDeclSpans.remove(closing);
            doc.setCharacterAttributes(opening.start(), opening.length(), styleMod, true);
            doc.setCharacterAttributes(closing.start(), closing.length(), styleMod, true);
            if (node instanceof ColorNode nColor) {
                var color = nColor.getColor();
                if (color == null) {
                    color = defaultTextColor;
                }

                var attr = nColor.getAttributes().get("value");
                if (attr != null) {
                    var attrStyle = new SimpleAttributeSet(styleMod);
                    StyleConstants.setForeground(attrStyle, color);
                    doc.setCharacterAttributes(attr.valueSpan().start(), attr.valueSpan().length(),
                            attrStyle, true);
                }

                style = new SimpleAttributeSet(style);
                StyleConstants.setForeground(style, color);
            } else if (node instanceof StyleNode nStyle) {
                Integer size = nStyle.getSize();
                Color color = null;
                if (nStyle.isColorSet()) {
                    color = nStyle.getColor();
                    if (color == null) {
                        color = defaultTextColor;
                    }

                    var colorAttr = nStyle.getAttributes().get("color");
                    if (colorAttr != null) {
                        var attrStyle = new SimpleAttributeSet(styleMod);
                        StyleConstants.setForeground(attrStyle, color);
                        doc.setCharacterAttributes(colorAttr.valueSpan().start(), colorAttr.valueSpan().length(),
                                attrStyle, true);
                    }
                }

                boolean escapedModified = size != null;
                boolean normalModified = escapedModified || color != null;
                if (normalModified) style = new SimpleAttributeSet(style);
                if (escapedModified) styleEscaped = new SimpleAttributeSet(styleEscaped);

                if (size != null) {
                    final int newSize = StyleConstants.getFontSize(styleNormal) + size;
                    StyleConstants.setFontSize(style, newSize);
                    StyleConstants.setFontSize(styleEscaped, newSize);
                }

                if (color != null) {
                    StyleConstants.setForeground(style, color);
                }
            } else if (node instanceof FontStyleModifyingNode nFSM) {
                style = new SimpleAttributeSet(style);
                nFSM.updateSwingStyle(style);
            }

            final Span contentSpan = node.getContentSpan();
            doc.setCharacterAttributes(contentSpan.start(), contentSpan.length(), style, true);
            final Color contentColor = StyleConstants.getForeground(style);
            var it = escapedSpans.iterator();
            while (it.hasNext()) {
                var escapedSpan = it.next();
                if (escapedSpan.isIn(contentSpan)) {
                    it.remove();
                    doc.setCharacterAttributes(escapedSpan.start(), escapedSpan.length(), styleEscaped, true);
                    try {
                        getHighlighter().addHighlight(escapedSpan.start(), escapedSpan.end(), getEscapedColorHighlightPainter(contentColor));
                    } catch (BadLocationException e) {
                        Logger.info("Failed to add highlighter for escaped color", e);
                    }
                }
            }

            if (node instanceof ContainerNode container) {
                highlight0(doc, container.getChildren(), style, styleEscaped);
            }
        }
    }

    private void highlightTrackedSpans(StyledDocument doc) {
        var hl = getEscapedColorHighlightPainter(defaultTextColor);
        for (var span : escapedSpans) {
            doc.setCharacterAttributes(span.start(), span.length(), styleMod, true);
            try {
                getHighlighter().addHighlight(span.start(), span.end(), hl);
            } catch (BadLocationException e) {
                Logger.info("Failed to add highlighter for escaped color", e);
            }
        }
        escapedSpans.clear();

        for (var span : nodeDeclSpans) {
            doc.setCharacterAttributes(span.start(), span.length(), styleMod, true);
        }
        nodeDeclSpans.clear();
    }

    private static final class StyledDocumentImpl extends DefaultStyledDocument {
        @Override
        public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
            str = str.replaceAll("\t", "    ");
            super.insertString(offs, str, a);
        }
    }

    private static final class EditorKitImpl extends StyledEditorKit {
        private final UnmodifiableAttributeSetView inputAttributes;

        public EditorKitImpl(AttributeSet inputAttributes) {
            this.inputAttributes = new UnmodifiableAttributeSetView(inputAttributes);
        }

        @Override
        public MutableAttributeSet getInputAttributes() {
            return inputAttributes;
        }

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
