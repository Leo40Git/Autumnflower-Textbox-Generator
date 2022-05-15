package adudecalledleo.aftbg.app.ui.worker;

import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.app.TextboxRenderer;
import adudecalledleo.aftbg.app.data.DataTracker;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.text.node.color.ColorParser;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.MainPanel;
import adudecalledleo.aftbg.app.ui.dialog.PreviewDialog;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxGenerator extends AbstractWorker {
    private final DataTracker parserCtx;
    private final WindowContext winCtx;
    private final List<Textbox> textboxes;

    public TextboxGenerator(MainPanel mainPanel, LoadFrame loadFrame, GameDefinition gameDef, List<Textbox> textboxes) {
        super(mainPanel, loadFrame);
        this.winCtx = gameDef.winCtx().copy();
        this.textboxes = textboxes;

        this.parserCtx = new DataTracker()
                .set(ColorParser.PALETTE, TextboxGenerator.this.winCtx.getPalette());
    }

    @Override
    protected Void doInBackground() {
        // FIXME TextboxRenderer.render just hides errors!
        //  Inline it, then report errors properly here.

        var image = TextboxRenderer.render(winCtx, parserCtx, textboxes);

        if (image == null) {
            loadFrame.setAlwaysOnTop(false);
            // TODO more detailed error message
            JOptionPane.showMessageDialog(mainPanel,
                    "Seems like one or more of your textboxes have errors!\n"
                            + "Correct this, then try generating again.",
                    "Generate textbox(es)", JOptionPane.ERROR_MESSAGE);
            cleanup();
        } else {
            var dialog = new PreviewDialog(mainPanel, image);
            dialog.setLocationRelativeTo(null);
            cleanup();
            dialog.setVisible(true);
        }

        return null;
    }

}
