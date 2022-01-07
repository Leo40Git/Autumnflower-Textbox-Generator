package adudecalledleo.aftbg.app.component.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.face.FacePool;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.text.TextParser;
import adudecalledleo.aftbg.app.text.node.NodeUtils;
import adudecalledleo.aftbg.window.WindowColors;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxListCellRenderer extends BaseListCellRenderer<Textbox>
        implements GameDefinitionUpdateListener {
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
    public void updateGameDefinition(GameDefinition gameDef) {
        this.winCtx = gameDef.winCtx();
        textParserCtx
                .put(WindowColors.class, winCtx.getColors())
                .put(FacePool.class, gameDef.faces());
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
