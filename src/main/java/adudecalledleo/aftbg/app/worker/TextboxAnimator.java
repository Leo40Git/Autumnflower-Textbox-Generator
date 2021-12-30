package adudecalledleo.aftbg.app.worker;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import adudecalledleo.aftbg.app.TextboxRenderer;
import adudecalledleo.aftbg.app.component.MainPanel;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.AnimatedPreviewDialog;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.util.DialogUtils;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.logging.Logger;

public final class TextboxAnimator extends AbstractTextboxWorker {
    public TextboxAnimator(MainPanel mainPanel, LoadFrame loadFrame, GameDefinition gameDef, List<Textbox> textboxes) {
        super(mainPanel, loadFrame, gameDef, textboxes);
        parserCtx.put(FacePool.class, gameDef.faces());
    }

    @Override
    protected Void doInBackground() {
        byte[] imageData;
        try {
            imageData = TextboxRenderer.renderAnimation(winCtx, parser, parserCtx, textboxes);
        } catch (IOException e) {
            Logger.error("Failed to generate GIF data", e);
            loadFrame.setAlwaysOnTop(false);
            DialogUtils.showErrorDialog(null, "Failed to generate GIF data!", "Animation Test");
            cleanup();
            return null;
        }

        if (imageData == null) {
            handleParseErrors("Generate animated textbox(es)");
        } else {
            var dialog = new AnimatedPreviewDialog(mainPanel, imageData);
            dialog.setLocationRelativeTo(null);
            cleanup();
            dialog.setVisible(true);
        }

        return null;
    }
}
