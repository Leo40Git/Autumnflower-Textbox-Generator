package adudecalledleo.aftbg.app.worker;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.app.render.TextRenderer;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.window.WindowColors;
import adudecalledleo.aftbg.window.WindowContext;

public abstract class AbstractTextboxWorker extends SwingWorker<Void, Void> {
    protected final Component parent;
    protected final LoadFrame loadFrame;
    protected final TextParser parser;
    protected final TextParser.Context parserCtx;
    protected final WindowContext winCtx;
    protected final List<Textbox> textboxes;

    public AbstractTextboxWorker(Component parent, LoadFrame loadFrame, WindowContext winCtx, List<Textbox> textboxes) {
        this.parent = parent;
        this.loadFrame = loadFrame;
        this.parser = new TextParser();
        this.winCtx = winCtx.copy();
        this.parserCtx = new TextParser.Context()
                .put(WindowColors.class, this.winCtx.getColors());
        this.textboxes = textboxes;
    }

    protected void drawTextbox(Graphics2D g, int x, int y, Face face, NodeList nodes, int arrowFrame) {
        winCtx.drawBackground(g, x + 4, y + 4, 808, 172, null);
        g.drawImage(face.getImage(), x + 18, y + 18, null);
        TextRenderer.draw(g, nodes,
                x + (face.isBlank() ? 18 : 186),
                y + 21);
        winCtx.drawBorder(g, x, y, 816, 180, null);
        if (arrowFrame >= 0) {
            winCtx.drawArrow(g, x, y, 816, 180, arrowFrame, null);
        }
    }

    protected void handleParseErrors(String title) {
        loadFrame.setAlwaysOnTop(false);
        // TODO more detailed error message
        JOptionPane.showMessageDialog(parent,
                "Seems like one or more of your textboxes have errors!\n"
                        + "Correct this, then try generating again.",
                title, JOptionPane.ERROR_MESSAGE);
        loadFrame.dispose();
    }
}
