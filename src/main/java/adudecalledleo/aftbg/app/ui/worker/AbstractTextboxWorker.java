package adudecalledleo.aftbg.app.ui.worker;

import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.text.TextParser;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.MainPanel;
import adudecalledleo.aftbg.window.WindowColors;
import adudecalledleo.aftbg.window.WindowContext;

public abstract class AbstractTextboxWorker extends AbstractWorker {
    protected final TextParser parser;
    protected final TextParser.Context parserCtx;
    protected final WindowContext winCtx;
    protected final List<Textbox> textboxes;

    public AbstractTextboxWorker(MainPanel mainPanel, LoadFrame loadFrame, GameDefinition gameDef, List<Textbox> textboxes) {
        super(mainPanel, loadFrame);
        this.winCtx = gameDef.winCtx().copy();
        this.textboxes = textboxes;

        this.parser = new TextParser();
        this.parserCtx = new TextParser.Context()
                .put(WindowColors.class, winCtx.getColors());
    }

    protected void handleParseErrors(String title) {
        loadFrame.setAlwaysOnTop(false);
        // TODO more detailed error message
        JOptionPane.showMessageDialog(mainPanel,
                "Seems like one or more of your textboxes have errors!\n"
                        + "Correct this, then try generating again.",
                title, JOptionPane.ERROR_MESSAGE);
        cleanup();
    }
}
