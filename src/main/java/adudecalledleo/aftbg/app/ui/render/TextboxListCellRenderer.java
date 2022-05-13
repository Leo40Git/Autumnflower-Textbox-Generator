package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;

import javax.swing.*;

import adudecalledleo.aftbg.app.data.DataTracker;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.game.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.text.node.color.ColorParser;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxListCellRenderer extends BaseListCellRenderer<Textbox>
        implements GameDefinitionUpdateListener {
    private final DataTracker parserCtx;
    private WindowContext winCtx;

    public TextboxListCellRenderer() {
        super();
        parserCtx = new DataTracker();
        setPreferredSize(new Dimension(72 * 4 + 4, 72));
        setMinimumSize(new Dimension(72 * 4 + 4, 72));
    }

    @Override
    public void updateGameDefinition(GameDefinition gameDef) {
        this.winCtx = gameDef.winCtx();
        parserCtx.set(ColorParser.PALETTE, winCtx.getColors());
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
        //String contents = NodeUtils.getTruncatedDisplay(textParser.parse(parserCtx, value.getText()), 50);
        String contents = "(oops I broke this)"; // FIXME reimplement getTruncatedDisplay... hopefully without parsing?
        setText("<html>"
                + "<b>Textbox " + (index + 1) + "</b><br>"
                + contents
                + "</html>");
        return this;
    }
}
