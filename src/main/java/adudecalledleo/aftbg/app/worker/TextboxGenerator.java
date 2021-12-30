package adudecalledleo.aftbg.app.worker;

import java.awt.*;
import java.util.List;

import adudecalledleo.aftbg.app.TextboxRenderer;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.PreviewDialog;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxGenerator extends AbstractTextboxWorker {
    public TextboxGenerator(Component parent, LoadFrame loadFrame, GameDefinition gameDef, List<Textbox> textboxes) {
        super(parent, loadFrame, gameDef, textboxes);
    }

    @Override
    protected Void doInBackground() {
        var image = TextboxRenderer.render(winCtx, parser, parserCtx, textboxes);

        if (image == null) {
            handleParseErrors("Generate textbox(es)");
        } else {
            var dialog = new PreviewDialog(parent, image);
            dialog.setLocationRelativeTo(null);
            loadFrame.dispose();
            dialog.setVisible(true);
        }

        return null;
    }
}
