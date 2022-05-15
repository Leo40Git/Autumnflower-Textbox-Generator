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
    private final StringBuilder sb;
    private final DataTracker parserCtx;
    private WindowContext winCtx;

    public TextboxListCellRenderer() {
        super();
        sb = new StringBuilder();
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
        String contents = makeTruncatedDisplay(value.getText(), 50);
        setText("<html>"
                + "<b>Textbox " + (index + 1) + "</b><br>"
                + contents
                + "</html>");
        return this;
    }

    private static final String CONTENTS_EMPTY = "(empty)";
    private String makeTruncatedDisplay(String contents, int maxLength) {
        if (contents.isBlank()) {
            return CONTENTS_EMPTY;
        } else {
            sb.setLength(0);
            boolean skipSpaces = true, inTagBlock = false;
            for (char c : contents.toCharArray()) {
                if (inTagBlock) {
                    if (c == ']') {
                        inTagBlock = false;
                    }
                } else {
                    if (c == '[') {
                        inTagBlock = true;
                    } else {
                        // trim leading spaces
                        if (c == ' ' && skipSpaces) {
                            continue;
                        } else {
                            skipSpaces = false;
                        }
                        sb.append(c);
                        if (sb.length() == maxLength - 1) {
                            sb.append('\u2026');
                            break;
                        }
                    }
                }
            }
            if (sb.length() == 0) {
                return CONTENTS_EMPTY;
            } else {
                return "\"" + sb + "\"";
            }
        }
    }
}
