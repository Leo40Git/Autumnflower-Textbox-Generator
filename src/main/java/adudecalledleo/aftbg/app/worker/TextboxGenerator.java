package adudecalledleo.aftbg.app.worker;

import java.awt.*;
import java.awt.image.*;
import java.util.List;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.PreviewDialog;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxGenerator extends AbstractTextboxWorker {
    public TextboxGenerator(Component parent, LoadFrame loadFrame, WindowContext winCtx, List<Textbox> textboxes) {
        super(parent, loadFrame, winCtx, textboxes);
    }

    @Override
    protected Void doInBackground() {
        final int textboxCount = textboxes.size();
        var image = new BufferedImage(816, 180 * textboxCount + 2 * (textboxCount - 1), BufferedImage.TYPE_INT_ARGB);

        var g = image.createGraphics();
        g.setBackground(ColorUtils.TRANSPARENT);
        g.clearRect(0, 0, image.getWidth(), image.getHeight());

        boolean success = true;
        for (int i = 0; i < textboxCount; i++) {
            var textbox = textboxes.get(i);
            var nodes = parser.parse(parserCtx, textbox.getText());
            if (nodes.hasErrors()) {
                success = false;
                break;
            }

            g.setClip(0, 182 * i, 816, 180);
            drawTextbox(g, 0, 182 * i, textbox.getFace(), nodes, i < textboxCount - 1 ? 0 : -1);
        }

        g.dispose();

        if (success) {
            var dialog = new PreviewDialog(parent, image);
            dialog.setLocationRelativeTo(null);
            loadFrame.dispose();
            dialog.setVisible(true);
        } else {
            handleParseErrors("Generate textbox(es)");
        }

        return null;
    }
}
