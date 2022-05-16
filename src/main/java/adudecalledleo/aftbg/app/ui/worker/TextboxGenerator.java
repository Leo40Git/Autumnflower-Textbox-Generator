package adudecalledleo.aftbg.app.ui.worker;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.List;

import adudecalledleo.aftbg.app.data.DataTracker;
import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.DOMRenderer;
import adudecalledleo.aftbg.app.text.node.Node;
import adudecalledleo.aftbg.app.text.node.color.ColorParser;
import adudecalledleo.aftbg.app.ui.LoadFrame;
import adudecalledleo.aftbg.app.ui.MainPanel;
import adudecalledleo.aftbg.app.ui.dialog.ErrorReportDialog;
import adudecalledleo.aftbg.app.ui.dialog.PreviewDialog;
import adudecalledleo.aftbg.app.util.ColorUtils;
import adudecalledleo.aftbg.app.util.Pair;
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
        final int textboxCount = textboxes.size();
        var image = new BufferedImage(816, 180 * textboxCount + 2 * (textboxCount - 1), BufferedImage.TYPE_INT_ARGB);

        var g = image.createGraphics();
        g.setBackground(ColorUtils.TRANSPARENT);
        g.clearRect(0, 0, image.getWidth(), image.getHeight());

        List<Pair<Integer, DOMParser.Error>> errors = null;
        for (int i = 0; i < textboxCount; i++) {
            var textbox = textboxes.get(i);
            var result = DOMParser.parse(textbox.getText(), parserCtx);
            if (result.hasErrors()) {
                if (errors == null) {
                    errors = new ArrayList<>();
                }
                for (var error : result.errors()) {
                    errors.add(new Pair<>(i, error));
                }
                continue;
            }

            // only render textbox if we don't have any errors (since if we do have errors,
            //  we're just gonna throw away the result anyway)
            if (errors == null) {
                g.setClip(0, 182 * i, 816, 180);
                renderTextbox(winCtx, g, 0, 182 * i, textbox.getFace(), result.document(), i < textboxCount - 1 ? 0 : -1);
            }
        }

        g.dispose();

        if (errors == null) {
            var dialog = new PreviewDialog(mainPanel, image);
            dialog.setLocationRelativeTo(null);
            cleanup();
            dialog.setVisible(true);
        } else {
            var dialog = new ErrorReportDialog(mainPanel, errors);
            dialog.setLocationRelativeTo(null);
            cleanup();
            dialog.setVisible(true);
        }

        return null;
    }

    private static void renderTextbox(WindowContext winCtx, Graphics2D g, int x, int y, Face face, Node root, int arrowFrame) {
        winCtx.drawBackground(g, x + 4, y + 4, 808, 172, null);
        g.drawImage(face.getImage(), x + 18, y + 18, null);
        DOMRenderer.render(g, root,
                x + (face.isBlank() ? 18 : 186),
                y + 21);
        winCtx.drawBorder(g, x, y, 816, 180, null);
        if (arrowFrame >= 0) {
            winCtx.drawArrow(g, x, y, 816, 180, arrowFrame, null);
        }
    }
}
