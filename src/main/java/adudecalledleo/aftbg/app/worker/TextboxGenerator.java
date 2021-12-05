package adudecalledleo.aftbg.app.worker;

import java.awt.Component;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import adudecalledleo.aftbg.app.data.Textbox;
import adudecalledleo.aftbg.app.dialog.PreviewDialog;
import adudecalledleo.aftbg.app.util.LoadFrame;
import adudecalledleo.aftbg.text.TextParser;
import adudecalledleo.aftbg.text.TextRenderer;
import adudecalledleo.aftbg.util.ColorUtils;
import adudecalledleo.aftbg.window.WindowContext;

public final class TextboxGenerator extends SwingWorker<Void, Void> {
    private final Component parent;
    private final LoadFrame loadFrame;
    private final TextParser parser;
    private final WindowContext winCtx;
    private final List<Textbox> textboxes;

    public TextboxGenerator(Component parent, LoadFrame loadFrame, TextParser parser, WindowContext winCtx, List<Textbox> textboxes) {
        this.parent = parent;
        this.loadFrame = loadFrame;
        this.parser = parser;
        this.winCtx = winCtx;
        this.textboxes = textboxes;
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
            var nodes = parser.parse(textbox.getText());
            if (nodes.hasErrors()) {
                success = false;
                break;
            }

            g.setClip(0, 182 * i, 816, 180);
            winCtx.drawBackground(g, 4, 4 + 182 * i, 808, 172, null);
            g.drawImage(textbox.getFace().getImage(), 18, 18 + 182 * i, null);
            TextRenderer.draw(g, nodes, winCtx.getColors(),
                    textbox.getFace().isBlank() ? 18 : 186,
                    21 + 182 * i);
            winCtx.drawBorder(g, 0, 182 * i, 816, 180, null);
            if (i < textboxCount - 1) {
                winCtx.drawArrow(g, 0, 182 * i, 816, 180, 0, null);
            }
        }

        g.dispose();

        if (success) {
            var dialog = new PreviewDialog((Frame) SwingUtilities.getWindowAncestor(parent), image);
            dialog.setLocationRelativeTo(null);
            loadFrame.dispose();
            dialog.setVisible(true);
        } else {
            loadFrame.setAlwaysOnTop(false);
            // TODO more detailed error message
            JOptionPane.showMessageDialog(parent,
                    "Seems like one or more of your textboxes have errors!\n"
                            + "Correct this, then try generating again.",
                    "Generate textbox(es)", JOptionPane.ERROR_MESSAGE);
            loadFrame.dispose();
        }

        return null;
    }
}
