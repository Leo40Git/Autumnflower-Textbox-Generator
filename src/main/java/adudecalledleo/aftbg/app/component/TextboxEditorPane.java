package adudecalledleo.aftbg.app.component;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.dialog.ColorModifierDialog;
import adudecalledleo.aftbg.app.dialog.StyleModifierDialog;
import adudecalledleo.aftbg.app.util.ImmutableAttributeSet;
import adudecalledleo.aftbg.app.util.WindowContextUpdateListener;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.text.modifier.ColorModifierNode;
import adudecalledleo.aftbg.text.modifier.StyleModifierNode;
import adudecalledleo.aftbg.text.modifier.StyleSpec;
import adudecalledleo.aftbg.text.node.*;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class TextboxEditorPane extends JEditorPane implements WindowContextUpdateListener, ActionListener {
    private static final BufferedImage SCRATCH_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private static final String AC_ADD_MOD_COLOR = "add_mod.color";
    private static final String AC_ADD_MOD_STYLE = "add_mod.style";

    private static final boolean REPLACE_LINE_SEPARATOR = !"\n".equals(System.lineSeparator());

    private final TextParser textParser;
    private final Consumer<String> textUpdateConsumer;
    private final Timer updateTimer;
    private final Map<Object, Action> actions;
    private final Map<Rectangle2D, String> errors;
    private final Line2D scratchLine;
    private final SimpleAttributeSet styleNormal, styleMod;
    private final JPopupMenu popupMenu;

    private WindowContext winCtx;
    private boolean forceCaretRendering;
    private NodeList nodes;

    public TextboxEditorPane(TextParser textParser, Consumer<String> textUpdateConsumer) {
        super();
        this.textParser = textParser;
        this.textUpdateConsumer = textUpdateConsumer;
        errors = new HashMap<>();
        scratchLine = new Line2D.Double(0, 0, 0, 0);

        styleNormal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleNormal, TextRenderer.DEFAULT_FONT.getFamily());
        StyleConstants.setFontSize(styleNormal, TextRenderer.DEFAULT_FONT.getSize());
        StyleConstants.setForeground(styleNormal, Color.WHITE);
        styleMod = new SimpleAttributeSet(styleNormal);
        StyleConstants.setForeground(styleMod, Color.GRAY);

        setDocument(new StyledDocumentImpl());
        setEditorKit(new EditorKitImpl(new ImmutableAttributeSet(styleNormal)));

        updateTimer = new Timer(250, e -> SwingUtilities.invokeLater(() -> {
            textUpdateConsumer.accept(getText());
            highlight();
        }));
        updateTimer.setRepeats(false);
        updateTimer.setCoalesce(true);

        actions = createActionTable();

        Graphics2D g = SCRATCH_IMAGE.createGraphics();
        g.setFont(TextRenderer.DEFAULT_FONT);
        var fm = g.getFontMetrics();
        var size = new Dimension(816, fm.getHeight() * 4 + fm.getDescent());
        setMinimumSize(size);
        setPreferredSize(size);
        g.dispose();

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
        forceCaretRendering = false;

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

        JMenu modsMenu = new JMenu("Add Modifier...");
        modsMenu.setMnemonic(KeyEvent.VK_M);
        item = new JMenuItem("Color", AppResources.Icons.MOD_COLOR.get());
        item.setActionCommand(AC_ADD_MOD_COLOR);
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_C);
        modsMenu.add(item);
        item = new JMenuItem("Style", AppResources.Icons.MOD_STYLE.get());
        item.setActionCommand(AC_ADD_MOD_STYLE);
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_S);
        modsMenu.add(item);

        menu.addSeparator();
        menu.add(modsMenu);

        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case AC_ADD_MOD_COLOR -> {
                ColorModifierDialog.Result result;
                try {
                    forceCaretRendering = true;
                    var dialog = new ColorModifierDialog((Frame) SwingUtilities.getWindowAncestor(this), winCtx);
                    dialog.setLocationRelativeTo(null);
                    result = dialog.showDialog();
                } finally {
                    forceCaretRendering = false;
                }
                if (result == null) {
                    requestFocus();
                    break;
                }
                String toInsert;
                if (result instanceof ColorModifierDialog.WindowResult winResult) {
                    toInsert = "\\c[" + winResult.getIndex() + "]";
                } else if (result instanceof ColorModifierDialog.ConstantResult constResult) {
                    var col = constResult.getColor();
                    toInsert = String.format("\\c[#%02X%02X%02X]",
                            col.getRed(), col.getGreen(), col.getBlue());
                } else {
                    throw new InternalError("Unhandled result type " + result + "?!");
                }
                try {
                    replaceSelection(toInsert);
                    updateTimer.restart();
                } finally {
                    requestFocus();
                }
            }
            case AC_ADD_MOD_STYLE -> {
                StyleSpec spec = StyleSpec.DEFAULT;
                if (nodes != null) {
                    for (var node : nodes) {
                        if (node instanceof StyleModifierNode styleModNote) {
                            spec = spec.add(styleModNote.getSpec());
                        }
                    }
                }
                StyleSpec newSpec;
                try {
                    forceCaretRendering = true;
                    var dialog = new StyleModifierDialog((Frame) SwingUtilities.getWindowAncestor(this), spec);
                    dialog.setLocationRelativeTo(null);
                    newSpec = dialog.showDialog();
                } finally {
                    forceCaretRendering = false;
                }
                if (newSpec == null) {
                    requestFocus();
                    break;
                }
                newSpec = spec.difference(newSpec);
                try {
                    replaceSelection(newSpec.toModifier());
                    updateTimer.restart();
                } finally {
                    requestFocus();
                }
            }
        }
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
        }

        super.paintComponent(g);

        g2d.setColor(Color.RED);
        for (var entry : errors.entrySet()) {
            var rect = entry.getKey();
            g2d.clip(rect);
            final double y = rect.getY() + rect.getHeight() - 3;
            boolean raised = true;
            for (double x = rect.getX(); x <= rect.getX() + rect.getWidth() + 2; x += 2) {
                scratchLine.setLine(x, y + (raised ? 2 : 0), x + 2, y + (raised ? 0 : 2));
                g2d.draw(scratchLine);
                raised = !raised;
            }
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

    @Override
    public void updateWindowContext(WindowContext winCtx) {
        this.winCtx = winCtx;
        setCaretColor(winCtx.getColor(0));
        StyleConstants.setForeground(styleNormal, winCtx.getColor(0));
        flushChanges(true);
    }

    private void highlight() {
        if (getDocument() instanceof StyledDocument doc) {
            MutableAttributeSet style = styleNormal;
            doc.setParagraphAttributes(0, doc.getLength(), style, true);
            doc.setCharacterAttributes(0, doc.getLength(), style, true);

            if (winCtx == null) {
                return;
            }

            nodes = null;
            try {
                String text = doc.getText(0, doc.getLength());
                if (REPLACE_LINE_SEPARATOR) {
                    text = text.replaceAll(System.lineSeparator(), "\n");
                }
                nodes = textParser.parse(text);
            } catch (BadLocationException e) {
                Logger.error("Failed to get text to parse!", e);
                return;
            }
            StyleSpec styleSpec = StyleSpec.DEFAULT;

            for (Node node : nodes) {
                if (node instanceof ColorModifierNode modCol) {
                    doc.setCharacterAttributes(modCol.getStart(), modCol.getLength(), styleMod, true);
                    Color c = modCol.getColor(winCtx.getColors());
                    style = new SimpleAttributeSet(style);
                    StyleConstants.setForeground(style, c);

                    var style2 = new SimpleAttributeSet(styleNormal);
                    StyleConstants.setForeground(style2, c);
                    Span argSpan = modCol.getArgSpans()[0];
                    doc.setCharacterAttributes(argSpan.start(), argSpan.length(), style2, true);
                } else if (node instanceof StyleModifierNode modStyle) {
                    doc.setCharacterAttributes(modStyle.getStart(), modStyle.getLength(), styleMod, true);
                    styleSpec = styleSpec.add(modStyle.getSpec());
                    style = new SimpleAttributeSet(style);
                    StyleConstants.setBold(style, styleSpec.isBold());
                    StyleConstants.setItalic(style, styleSpec.isItalic());
                    StyleConstants.setUnderline(style, styleSpec.isUnderline());
                    StyleConstants.setStrikeThrough(style, styleSpec.isStrikethrough());
                    StyleConstants.setSuperscript(style, styleSpec.superscript() == StyleSpec.Superscript.SUPER);
                    StyleConstants.setSubscript(style, styleSpec.superscript() == StyleSpec.Superscript.SUB);
                    StyleConstants.setFontSize(style, StyleConstants.getFontSize(styleNormal) + styleSpec.getTrueSizeAdjust());
                } else if (node instanceof ModifierNode) {
                    doc.setCharacterAttributes(node.getStart(), node.getLength(), styleMod, true);
                } else {
                    doc.setCharacterAttributes(node.getStart(), node.getLength(), style, true);
                }
            }

            errors.clear();

            for (ErrorNode err : nodes.getErrors()) {
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
                    Logger.error("Failed to generate error node text bounds!", e);
                }
            }
        }

        SwingUtilities.invokeLater(this::repaint);
    }

    private static final class StyledDocumentImpl extends DefaultStyledDocument {
        @Override
        public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
            str = str.replaceAll("\t", "    ");
            super.insertString(offs, str, a);
        }
    }

    private static final class EditorKitImpl extends StyledEditorKit {
        private final ImmutableAttributeSet inputAttributes;

        public EditorKitImpl(ImmutableAttributeSet inputAttributes) {
            this.inputAttributes = inputAttributes;
        }

        @Override
        public ImmutableAttributeSet getInputAttributes() {
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
