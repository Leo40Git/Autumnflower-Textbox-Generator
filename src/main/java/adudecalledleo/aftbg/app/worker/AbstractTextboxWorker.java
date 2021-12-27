package adudecalledleo.aftbg.app.worker;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.text.TextParser;
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
