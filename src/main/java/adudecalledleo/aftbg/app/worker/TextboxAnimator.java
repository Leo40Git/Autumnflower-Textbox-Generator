package adudecalledleo.aftbg.app.worker;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.app.TextboxRenderer;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.AnimatedPreviewDialog;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.logging.Logger;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxAnimator extends AbstractTextboxWorker {
    public TextboxAnimator(Component parent, LoadFrame loadFrame, WindowContext winCtx, FacePool facePool, List<Textbox> textboxes) {
        super(parent, loadFrame, winCtx, textboxes);
        parserCtx.put(FacePool.class, facePool);
    }

    @Override
    protected Void doInBackground() {
        byte[] imageData;
        try {
            imageData = TextboxRenderer.renderAnimation(winCtx, parser, parserCtx, textboxes);
        } catch (IOException e) {
            Logger.error("Failed to generate GIF data", e);
            loadFrame.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null,
                    "Failed to generate GIF data!\nSee \"" + Logger.logFile() + "\" for more details.",
                    "Animation Test", JOptionPane.ERROR_MESSAGE);
            loadFrame.dispose();
            return null;
        }

        if (imageData == null) {
            handleParseErrors("Generate animated textbox(es)");
        } else {
            var dialog = new AnimatedPreviewDialog(parent, imageData);
            dialog.setLocationRelativeTo(null);
            loadFrame.dispose();
            dialog.setVisible(true);
        }

        return null;
    }
}
