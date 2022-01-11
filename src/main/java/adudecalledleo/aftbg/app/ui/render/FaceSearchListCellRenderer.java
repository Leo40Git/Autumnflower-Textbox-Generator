package adudecalledleo.aftbg.app.ui.render;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;

public final class FaceSearchListCellRenderer extends BaseListCellRenderer<Face> {
    private final Map<String, String> highlightedCache;
    private Pattern highlightedPattern;
    private String highlightedString;
    private Face selectedFace;

    public FaceSearchListCellRenderer() {
        super();
        highlightedCache = new HashMap<>();
        setPreferredSize(FaceListCellRenderer.SIZE);
        setMinimumSize(FaceListCellRenderer.SIZE);
    }

    public String getHighlightedString() {
        return highlightedString;
    }

    public void setHighlightedString(String highlightedString) {
        if (!Objects.equals(this.highlightedString, highlightedString)) {
            highlightedCache.clear();
            if (highlightedString == null) {
                highlightedPattern = null;
            } else {
                highlightedPattern = Pattern.compile(highlightedString, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
            }
        }
        this.highlightedString = highlightedString;
    }

    public Face getSelectedFace() {
        return selectedFace;
    }

    public void setSelectedFace(Face selectedFace) {
        this.selectedFace = selectedFace;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Face> list, Face value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return this;
        }

        updateColors(list, index, isSelected, cellHasFocus);
        if (!isSelected && selectedFace == value) {
            setBackground(FaceGridCellRenderer.SELECTED_FACE_BACKGROUND);
        }
        setIcon(value.getIcon());
        setText(highlight(value.getName()));
        var src = value.getSource();
        if (src == null) {
            setToolTipText(null);
        } else {
            setToolTipText("<html><b>From:</b> %s</html>".formatted(src.qualifiedName()));
        }
        return this;
    }

    private String highlight(String original) {
        if (highlightedString == null) {
            return original;
        }
        return highlightedCache.computeIfAbsent(original, str -> "<html>%s</html>".formatted(
                highlightedPattern.matcher(str)
                        .replaceAll(result -> "<u>%s</u>".formatted(result.group()))));
    }
}
