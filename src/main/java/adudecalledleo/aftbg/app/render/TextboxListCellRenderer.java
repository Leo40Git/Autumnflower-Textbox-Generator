package adudecalledleo.aftbg.app.render;

import adudecalledleo.aftbg.app.util.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.util.WindowContextUpdateListener;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.NodeUtils;
import adudecalledleo.aftbg.window.WindowColors;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public final class TextboxListCellRenderer extends BaseListCellRenderer<Textbox>
        implements WindowContextUpdateListener, GameDefinitionUpdateListener {
    private final TextParser textParser;
    private final TextParser.Context textParserCtx;
    private WindowContext winCtx;

    public TextboxListCellRenderer() {
        super();
        textParser = new TextParser();
        textParserCtx = new TextParser.Context();
        setPreferredSize(new Dimension(72 * 4 + 4, 72));
        setMinimumSize(new Dimension(72 * 4 + 4, 72));
    }

    @Override
    public void updateWindowContext(WindowContext winCtx) {
        this.winCtx = winCtx;
        textParserCtx.put(WindowColors.class, winCtx.getColors());
    }

    @Override
    public void updateGameDefinition(Path basePath, GameDefinition gameDef, FacePool facePool) {
        textParserCtx.put(FacePool.class, facePool);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Textbox> list, Textbox value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setOpaque(true);
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setOpaque(false);
            setForeground(winCtx == null ? Color.WHITE : winCtx.getColor(0));
        }
        setIcon(value.getFace().getIcon());
        String contents = NodeUtils.getTruncatedDisplay(textParser.parse(textParserCtx, value.getText()), 50);
        setText("<html>"
                + "<b>Textbox " + (index + 1) + "</b><br>"
                + contents
                + "</html>");
        return this;
    }
}
