package adudecalledleo.aftbg.app.worker;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.SwingWorker;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.window.WindowContext;

public abstract class AbstractTextboxWorker extends SwingWorker<Void, Void> {
    protected final Component parent;
    protected final LoadFrame loadFrame;
    protected final TextParser parser;
    protected final WindowContext winCtx;
    protected final List<Textbox> textboxes;

    public AbstractTextboxWorker(Component parent, LoadFrame loadFrame, WindowContext winCtx, List<Textbox> textboxes) {
        this.parent = parent;
        this.loadFrame = loadFrame;
        this.parser = new TextParser();
        this.winCtx = winCtx.copy();
        this.textboxes = textboxes;
    }

    protected void drawTextbox(Graphics2D g, int x, int y, Face face, NodeList nodes, int arrowFrame) {
        winCtx.drawBackground(g, x + 4, y + 4, 808, 172, null);
        g.drawImage(face.getImage(), x + 18, y + 18, null);
        TextRenderer.draw(g, nodes, winCtx.getColors(),
                x + (face.isBlank() ? 18 : 186),
                y + 21);
        winCtx.drawBorder(g, x, y, 816, 180, null);
        if (arrowFrame >= 0) {
            winCtx.drawArrow(g, x, y, 816, 180, arrowFrame, null);
        }
    }
}
