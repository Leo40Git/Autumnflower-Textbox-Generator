package adudecalledleo.aftbg.app.ui.worker;

import java.util.List;

import adudecalledleo.aftbg.app.TextboxRenderer;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.ui.MainPanel;
import adudecalledleo.aftbg.app.ui.dialog.PreviewDialog;
import adudecalledleo.aftbg.app.util.LoadFrame;

public final class TextboxGenerator extends AbstractTextboxWorker {
    public TextboxGenerator(MainPanel mainPanel, LoadFrame loadFrame, GameDefinition gameDef, List<Textbox> textboxes) {
        super(mainPanel, loadFrame, gameDef, textboxes);
    }

    @Override
    protected Void doInBackground() {
        var image = TextboxRenderer.render(winCtx, parser, parserCtx, textboxes);

        if (image == null) {
            handleParseErrors("Generate textbox(es)");
        } else {
            var dialog = new PreviewDialog(mainPanel, image);
            dialog.setLocationRelativeTo(null);
            cleanup();
            dialog.setVisible(true);
        }

        return null;
    }
}
