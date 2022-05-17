package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.util.ColorUtils;
import org.jetbrains.annotations.Nullable;

// TODO key navigation?
public final class FaceGrid extends JComponent implements Scrollable, MouseListener, MouseMotionListener {
    public static final Color COLOR_GRID = UIManager.getColor("List.background");
    public static final Color COLOR_GRID_2 = ColorUtils.darker(COLOR_GRID, 0.9);
    public static final Color COLOR_GRID_SELECTED = UIManager.getColor("List.selectionBackground");
    public static final Color COLOR_GRID_HOVERED = new Color(COLOR_GRID_SELECTED.getRed(), COLOR_GRID_SELECTED.getGreen(), COLOR_GRID_SELECTED.getBlue(), 127);

    private static final Dimension DEFAULT_SIZE = new Dimension(72 * 5, 72 * 8);

    private final List<Face> faceList;
    private FaceCategory category;
    private Face selectedFace;
    private int selectedIndex, hoveredIndex;

    public FaceGrid() {
        faceList = new ArrayList<>();
        category = null;
        selectedFace = null;
        selectedIndex = hoveredIndex = -1;

        setOpaque(true);
        setAutoscrolls(true);
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        ToolTipManager.sharedInstance().registerComponent(this);

        setSize(DEFAULT_SIZE);
        setMinimumSize(DEFAULT_SIZE);
        setPreferredSize(DEFAULT_SIZE);
        setMaximumSize(DEFAULT_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        final int faceCount = faceList.size();
        final int lastTile = 8 * ((getHeight() / 8) + 1);
        for (int i = 0; i < lastTile; i++) {
            Color c;
            if (i < faceCount && i == selectedIndex) {
                c = COLOR_GRID_SELECTED;
            } else {
                c = (i % 2 == 0) ? COLOR_GRID : COLOR_GRID_2;
            }
            g.setColor(c);
            g.fillRect((i % 5) * 72, (i / 5) * 72, 72, 72);
            if (i < faceCount) {
                if (i == hoveredIndex) {
                    g.setColor(COLOR_GRID_HOVERED);
                    g.fillRect((i % 5) * 72, (i / 5) * 72, 72, 72);
                }

                var face = faceList.get(i);
                if (face.getIcon() != null) {
                    g.drawImage(face.getIcon().getImage(), (i % 5) * 72, (i / 5) * 72, null);
                }
            }
        }
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(DEFAULT_SIZE);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 72;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 72 * 5;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private int calculateFaceIndex(int mx, int my) {
        return (mx / 72) % 5 + (my / 72) * 5;
    }

    private int calculateFaceIndex(MouseEvent event) {
        return calculateFaceIndex(event.getX(), event.getY());
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int index = calculateFaceIndex(event);
        if (index < faceList.size()) {
            var face = faceList.get(index);
            var src = face.getSource();
            if (src == null) {
                return "<html>%s<br>(src == null?!)</html>".formatted(face.getName());
            } else {
                return "<html>%s<br><b>From:</b> %s</html>".formatted(face.getName(), src.qualifiedName());
            }
        }
        return null;
    }

    public FaceCategory getCategory() {
        return category;
    }

    public void setCategory(FaceCategory category) {
        Objects.requireNonNull(category, "category");
        var oldCategory = this.category;
        this.category = category;
        if (!Objects.equals(oldCategory, category)) {
            faceList.clear();
            faceList.addAll(category.getFaces().values());
            if (selectedIndex >= 0 && selectedIndex < faceList.size()) {
                setSelectedFace(faceList.get(selectedIndex));
            } else {
                setSelectedFace(null);
            }

            int newHeight = faceList.size();
            if (newHeight % 8 == 0) {
                newHeight /= 8;
            } else {
                newHeight = (newHeight / 8) + 1;
            }
            newHeight *= 72;
            var size = new Dimension(72 * 5, Math.max(72 * 8, newHeight));
            setSize(size);
            setMinimumSize(size);
            setPreferredSize(size);
            setMaximumSize(size);
            invalidate();
            repaint();

            firePropertyChange("category", oldCategory, category);
        }
    }

    public Face getSelectedFace() {
        return selectedFace;
    }

    public void setSelectedFace(Face selectedFace) {
        Objects.requireNonNull(category, "category");

        if (selectedFace != null && !category.getFaces().containsValue(selectedFace)) {
            throw new IllegalArgumentException("selectedFace is not in category");
        }

        var oldSelectedFace = this.selectedFace;
        this.selectedFace = selectedFace;
        if (!Objects.equals(oldSelectedFace, selectedFace)) {
            if (selectedFace != null) {
                selectedIndex = faceList.indexOf(selectedFace);
                ensureIndexIsVisible(selectedIndex);
            }
            repaint();

            firePropertyChange("selectedFace", oldSelectedFace, selectedFace);
        }
    }

    private void ensureIndexIsVisible(int index) {
        var rect = new Rectangle((index % 5) * 72, (index / 5) * 72, 72, 72);
        scrollRectToVisible(rect);
    }

    private void tryUpdateSelectedIndex(int newIndex, @Nullable InputEvent e) {
        if (newIndex < faceList.size()) {
            selectedIndex = newIndex;
            setSelectedFace(faceList.get(selectedIndex));
            if (e != null)
                e.consume();
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this) {
            int newIndex = calculateFaceIndex(e);
            tryUpdateSelectedIndex(newIndex, e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) {
        hoveredIndex = calculateFaceIndex(e);
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hoveredIndex = -1;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        hoveredIndex = calculateFaceIndex(e);
        tryUpdateSelectedIndex(hoveredIndex, e);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        hoveredIndex = calculateFaceIndex(e);
        repaint();
    }
}
