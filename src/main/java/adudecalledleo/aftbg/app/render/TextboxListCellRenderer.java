package adudecalledleo.aftbg.app.render;

import adudecalledleo.aftbg.app.util.WindowContextUpdateListener;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.NodeUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;

public final class TextboxListCellRenderer extends BaseListCellRenderer<Textbox> implements WindowContextUpdateListener {
    private final TextParser textParser;
    private WindowContext winCtx;

    public TextboxListCellRenderer(TextParser textParser) {
        super();
        this.textParser = textParser;
        setPreferredSize(new Dimension(72 * 4 + 4, 72));
        setMinimumSize(new Dimension(72 * 4 + 4, 72));
    }

    @Override
    public void updateWindowContext(WindowContext winCtx) {
        this.winCtx = winCtx;
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
        String contents = NodeUtils.getTruncatedDisplay(textParser.parse(value.getText()), 50);
        setText("<html>"
                + "<b>Textbox " + (index + 1) + "</b><br>"
                + contents
                + "</html>");
        return this;
    }
}
