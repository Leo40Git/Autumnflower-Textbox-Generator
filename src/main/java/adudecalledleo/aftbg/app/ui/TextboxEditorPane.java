package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.text.TextParser;
import adudecalledleo.aftbg.app.text.TextRenderer;
import adudecalledleo.aftbg.app.text.modifier.*;
import adudecalledleo.aftbg.app.text.node.*;
import adudecalledleo.aftbg.app.ui.dialog.modifier.*;
import adudecalledleo.aftbg.app.ui.text.UnderlineHighlighter;
import adudecalledleo.aftbg.app.ui.text.ZigZagHighlighter;
import adudecalledleo.aftbg.app.util.ColorUtils;
import adudecalledleo.aftbg.app.util.UnmodifiableAttributeSetView;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.window.WindowColors;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxEditorPane extends JEditorPane
        implements GameDefinitionUpdateListener, ActionListener {
    private static final BufferedImage SCRATCH_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private static final Highlighter.HighlightPainter HLP_ERROR = new ZigZagHighlighter(Color.RED);
    private static final Map<Color, Highlighter.HighlightPainter> HLP_ESCAPED_COLORS = new HashMap<>();

    private static Highlighter.HighlightPainter getEscapedColorHighlightPainter(Color color) {
        return HLP_ESCAPED_COLORS.computeIfAbsent(color, UnderlineHighlighter::new);
    }

    private static final String AC_ADD_MOD_COLOR = "add_mod.color";
    private static final String AC_ADD_MOD_STYLE = "add_mod.style";
    private static final String AC_ADD_MOD_GIMMICK = "add_mod.gimmick";
    private static final String AC_ADD_MOD_FACE = "add_mod.face";
    private static final String AC_ADD_MOD_DELAY = "add_mod.delay";
    private static final String AC_ADD_MOD_TEXT_SPEED = "add_mod.text_speed";
    private static final String AC_ADD_MOD_INTERRUPT = "add_mod.interrupt";

    private final TextParser textParser;
    private final TextParser.Context textParserCtx;
    private final Consumer<String> textUpdateConsumer;
    private final Timer updateTimer;
    private final Map<Object, Action> actions;
    private final Map<Rectangle2D, String> errors;
    private final SimpleAttributeSet styleNormal, styleMod;
    private final JPopupMenu popupMenu;

    private GameDefinition gameDef;
    private WindowContext winCtx;
    private boolean forceCaretRendering;
    private Face textboxFace;
    private NodeList nodes;

    public TextboxEditorPane(Consumer<String> textUpdateConsumer) {
        super();
        this.textUpdateConsumer = textUpdateConsumer;

        textParser = new TextParser();
        textParserCtx = new TextParser.Context();
        errors = new HashMap<>();

        styleNormal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleNormal, TextRenderer.DEFAULT_FONT.getFamily());
        StyleConstants.setFontSize(styleNormal, TextRenderer.DEFAULT_FONT.getSize());
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
        g.setFont(TextRenderer.DEFAULT_FONT);
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
        item = new JMenuItem("Gimmick", AppResources.Icons.MOD_GIMMICK.get());
        item.setActionCommand(AC_ADD_MOD_GIMMICK);
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_G);
        modsMenu.add(item);

        modsMenu.addSeparator();

        item = new JMenuItem("Animation Only!");
        item.setEnabled(false);
        modsMenu.add(item);
        item = new JMenuItem("Face", AppResources.Icons.MOD_FACE.get());
        item.setActionCommand(AC_ADD_MOD_FACE);
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_F);
        modsMenu.add(item);
        item = new JMenuItem("Delay", AppResources.Icons.MOD_DELAY.get());
        item.setActionCommand(AC_ADD_MOD_DELAY);
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_D);
        modsMenu.add(item);
        item = new JMenuItem("Text Speed", AppResources.Icons.MOD_TEXT_SPEED.get());
        item.setActionCommand(AC_ADD_MOD_TEXT_SPEED);
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_T);
        modsMenu.add(item);
        item = new JMenuItem("Interrupt");
        item.setActionCommand(AC_ADD_MOD_INTERRUPT);
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_I);
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
                    var dialog = new ColorModifierDialog(this, winCtx);
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
                    toInsert = "\\" + ColorModifierNode.KEY + "[" + winResult.getIndex() + "]";
                } else if (result instanceof ColorModifierDialog.ConstantResult constResult) {
                    var col = constResult.getColor();
                    toInsert = "\\%c[#%02X%02X%02X]".formatted(
                            ColorModifierNode.KEY, col.getRed(), col.getGreen(), col.getBlue());
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
                    var dialog = new StyleModifierDialog(this, spec);
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
            case AC_ADD_MOD_GIMMICK -> {
                GimmickSpec spec = GimmickSpec.DEFAULT;
                if (nodes != null) {
                    for (var node : nodes) {
                        if (node instanceof GimmickModifierNode gimmickModNote) {
                            spec = spec.add(gimmickModNote.getSpec());
                        }
                    }
                }
                GimmickSpec newSpec;
                try {
                    forceCaretRendering = true;
                    var dialog = new GimmickModifierDialog(this, winCtx, spec);
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
            case AC_ADD_MOD_FACE -> {
                Face newFace;
                try {
                    forceCaretRendering = true;
                    var dialog = new FaceModifierDialog(this, gameDef.faces(), textboxFace);
                    dialog.setLocationRelativeTo(null);
                    newFace = dialog.showDialog();
                } finally {
                    forceCaretRendering = false;
                }
                if (newFace == null) {
                    requestFocus();
                    break;
                }
                String mod = "\\" + FaceModifierNode.KEY;
                if (newFace != Face.NONE) {
                    mod = "\\%c[%s]".formatted(FaceModifierNode.KEY, newFace.getPath());
                }
                try {
                    replaceSelection(mod);
                    updateTimer.restart();
                } finally {
                    requestFocus();
                }
            }
            case AC_ADD_MOD_DELAY -> {
                Integer delayLength;
                try {
                    forceCaretRendering = true;
                    var dialog = new DelayModifierDialog(this);
                    dialog.setLocationRelativeTo(null);
                    delayLength = dialog.showDialog();
                } finally {
                    forceCaretRendering = false;
                }
                if (delayLength == null) {
                    requestFocus();
                    break;
                }
                try {
                    replaceSelection("\\%c[%d]".formatted(DelayModifierNode.KEY, delayLength));
                    updateTimer.restart();
                } finally {
                    requestFocus();
                }
            }
            case AC_ADD_MOD_TEXT_SPEED -> {
                Integer newTextSpeed;
                try {
                    forceCaretRendering = true;
                    var dialog = new TextSpeedModifierDialog(this);
                    dialog.setLocationRelativeTo(null);
                    newTextSpeed = dialog.showDialog();
                } finally {
                    forceCaretRendering = false;
                }
                if (newTextSpeed == null) {
                    requestFocus();
                    break;
                }
                try {
                    replaceSelection("\\%c[%d]".formatted(TextSpeedModifierNode.KEY, newTextSpeed));
                    updateTimer.restart();
                } finally {
                    requestFocus();
                }
            }
            case AC_ADD_MOD_INTERRUPT -> {
                try {
                    replaceSelection("\\" + InterruptModifierNode.KEY);
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

        //getHighlighter().paint(g);
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

    public void setTextboxFace(Face textboxFace) {
        this.textboxFace = textboxFace;
    }

    @Override
    public void updateGameDefinition(GameDefinition gameDef) {
        this.gameDef = gameDef;
        this.winCtx = gameDef.winCtx().copy();
        setCaretColor(winCtx.getColor(0));
        StyleConstants.setForeground(styleNormal, winCtx.getColor(0));
        textParserCtx
                .put(WindowColors.class, winCtx.getColors())
                .put(FacePool.class, gameDef.faces());
        flushChanges(true);
    }

    private static final boolean DUMP_NODES = false;

    private void highlight() {
        Highlighter highlighter = getHighlighter();
        if (getDocument() instanceof StyledDocument doc) {
            MutableAttributeSet style = styleNormal, styleEscaped = styleMod;
            doc.setParagraphAttributes(0, doc.getLength(), style, true);
            doc.setCharacterAttributes(0, doc.getLength(), style, true);
            highlighter.removeAllHighlights();

            if (winCtx == null) {
                return;
            }

            nodes = null;
            try {
                nodes = textParser.parse(textParserCtx, doc.getText(0, doc.getLength()));
            } catch (BadLocationException e) {
                Logger.error("Failed to get text to parse!", e);
                return;
            }

            // region NODE DUMP
            if (DUMP_NODES) {
                Logger.trace("=== NODE DUMP START ===");
                for (Node node : nodes) {
                    Logger.trace(node.toString());
                }
                Logger.trace("=== NODE DUMP  END  ===");
            }
            // endregion

            StyleSpec styleSpec = StyleSpec.DEFAULT;

            for (Node node : nodes) {
                if (node instanceof ColorModifierNode modCol) {
                    doc.setCharacterAttributes(modCol.getStart(), modCol.getLength(), styleMod, true);
                    Color c = modCol.getColor();
                    style = new SimpleAttributeSet(style);
                    StyleConstants.setForeground(style, c);

                    var style2 = new SimpleAttributeSet(styleNormal);
                    StyleConstants.setForeground(style2, c);
                    Span argSpan = modCol.getArgSpans()[0];
                    doc.setCharacterAttributes(argSpan.start(), argSpan.length(), style2, true);
                } else if (node instanceof StyleModifierNode modStyle) {
                    doc.setCharacterAttributes(modStyle.getStart(), modStyle.getLength(), styleMod, true);
                    styleSpec = styleSpec.add(modStyle.getSpec());
                    style = modifyStyleToSpec(style, styleSpec);
                    styleEscaped = modifyStyleToSpec(styleEscaped, styleSpec);
                } else if (node instanceof ModifierNode) {
                    doc.setCharacterAttributes(node.getStart(), node.getLength(), styleMod, true);
                } else if (node instanceof TextNode.Escaped) {
                    try {
                        highlighter.addHighlight(node.getStart(), node.getStart() + node.getLength(),
                                getEscapedColorHighlightPainter(StyleConstants.getForeground(style)));
                        doc.setCharacterAttributes(node.getStart(), node.getLength(), styleEscaped, true);
                    } catch (BadLocationException e) {
                        Logger.error("Failed to properly highlight escaped text!", e);
                    }
                } else {
                    doc.setCharacterAttributes(node.getStart(), node.getLength(), style, true);
                }
            }

            errors.clear();

            for (ErrorNode err : nodes.getErrors()) {
                final int end = err.getStart() + err.getLength();

                try {
                    Rectangle2D startRect, endRect;

                    startRect = modelToView2D(err.getStart());
                    endRect = modelToView2D(end);

                    errors.put(new Rectangle2D.Double(startRect.getX(), startRect.getY(),
                                    endRect.getX() - startRect.getX(), Math.max(startRect.getHeight(), endRect.getHeight())),
                            err.getMessage());
                } catch (BadLocationException e) {
                    Logger.error("Failed to generate tooltip bounds for error!", e);
                }

                try {
                    highlighter.addHighlight(err.getStart(), end, HLP_ERROR);
                    doc.setCharacterAttributes(err.getStart(), err.getLength(), styleNormal, true);
                } catch (BadLocationException e) {
                    Logger.error("Failed to properly highlight error!", e);
                }
            }
        }

        SwingUtilities.invokeLater(this::repaint);
    }

    private MutableAttributeSet modifyStyleToSpec(MutableAttributeSet style, StyleSpec styleSpec) {
        style = new SimpleAttributeSet(style);
        StyleConstants.setBold(style, styleSpec.isBold());
        StyleConstants.setItalic(style, styleSpec.isItalic());
        StyleConstants.setUnderline(style, styleSpec.isUnderline());
        StyleConstants.setStrikeThrough(style, styleSpec.isStrikethrough());
        StyleConstants.setSuperscript(style, styleSpec.superscript() == StyleSpec.Superscript.SUPER);
        StyleConstants.setSubscript(style, styleSpec.superscript() == StyleSpec.Superscript.SUB);
        StyleConstants.setFontSize(style, StyleConstants.getFontSize(styleNormal) + styleSpec.getRealSizeAdjust());
        return style;
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
