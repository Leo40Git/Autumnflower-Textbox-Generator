package adudecalledleo.aftbg.app.render;

import adudecalledleo.aftbg.app.WindowContextUpdateListener;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.node.NodeUtils;
import adudecalledleo.aftbg.window.WindowContext;

import javax.swing.*;
import java.awt.*;

public final class TextboxListCellRenderer extends BaseListCellRenderer<Textbox> implements WindowContextUpdateListener {
    private final TextParser textParser;

    public TextboxListCellRenderer(TextParser textParser) {
        super();
        this.textParser = textParser;
        setPreferredSize(new Dimension(72 * 4 + 4, 72));
        setMinimumSize(new Dimension(72 * 4 + 4, 72));
    }

    @Override
    public void updateWindowContext(WindowContext winCtx) {
        setForeground(winCtx.getColor(0));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Textbox> list, Textbox value, int index, boolean isSelected, boolean cellHasFocus) {
        setBackground(list.getSelectionBackground());
        setOpaque(isSelected);
        setIcon(value.getFace().getIcon());
        String contents = NodeUtils.getTruncatedDisplay(textParser.parse(value.getText()), 30);
        setText("<html>"
                + "<b>Textbox " + (index + 1) + "</b><br>"
                + contents + "<br>"
                + "</html>");
        return this;
    }
}
