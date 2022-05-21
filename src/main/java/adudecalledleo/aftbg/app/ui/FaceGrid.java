package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.*;

import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.ui.render.UIColor;
import adudecalledleo.aftbg.app.ui.render.UIColors;

public final class FaceGrid extends JComponent implements Scrollable, MouseListener, MouseMotionListener {
    private static final Dimension DEFAULT_SIZE = new Dimension(72 * 5, 72 * 8);

    public static Dimension getDefaultSize(Dimension rv) {
        rv.width = DEFAULT_SIZE.width;
        rv.height = DEFAULT_SIZE.height;
        return rv;
    }

    public static Dimension getDefaultSize() {
        return getDefaultSize(new Dimension());
    }

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
        addMouseListener(this);
        addMouseMotionListener(this);
        setupKeyboardActions();
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
            UIColor c;
            if (i < faceCount) {
                if (i == selectedIndex) {
                    c = UIColors.List.getSelectionBackground();
                } else {
                    c = (i % 2 == 0) ? UIColors.List.getBackground() : UIColors.List.getDarkerBackground();
                }
            } else {
                c = (i % 2 == 0) ? UIColors.List.getDisabledBackground() : UIColors.List.getDarkerDisabledBackground();
            }
            g.setColor(c.get());
            g.fillRect((i % 5) * 72, (i / 5) * 72, 72, 72);
            if (i < faceCount) {
                if (i == hoveredIndex) {
                    g.setColor(UIColors.List.getHoveredBackground().get());
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
            return "<html>" + face.toToolTipText(true) + "</html>";
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
                setSelectedFace(faceList.get(selectedIndex), true);
            } else {
                setSelectedFace(null, false);
            }

            int newHeight = faceList.size();
            if (newHeight % 5 == 0) {
                newHeight /= 5;
            } else {
                newHeight = (newHeight / 5) + 1;
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

    public void setSelectedFace(Face selectedFace, boolean shouldScroll) {
        Objects.requireNonNull(category, "category");

        if (selectedFace != null && !category.getFaces().containsValue(selectedFace)) {
            throw new IllegalArgumentException("selectedFace is not in category");
        }

        var oldSelectedFace = this.selectedFace;
        this.selectedFace = selectedFace;
        if (!Objects.equals(oldSelectedFace, selectedFace)) {
            if (selectedFace != null) {
                selectedIndex = faceList.indexOf(selectedFace);
                if (shouldScroll) {
                    ensureIndexIsVisible(selectedIndex);
                }
            }
            repaint();

            firePropertyChange("selectedFace", oldSelectedFace, selectedFace);
        }
    }

    public Rectangle getCellBounds(int index) {
        return new Rectangle((index % 5) * 72, (index / 5) * 72, 72, 72);
    }

    public void ensureIndexIsVisible(int index) {
        scrollRectToVisible(getCellBounds(index));
    }

    private void tryUpdateSelectedIndex(int newIndex, boolean ensureVisible) {
        if (newIndex >= 0 && newIndex < faceList.size()) {
            selectedIndex = newIndex;
            if (ensureVisible) {
                ensureIndexIsVisible(selectedIndex);
            }
            setSelectedFace(faceList.get(selectedIndex), true);
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this) {
            tryUpdateSelectedIndex(calculateFaceIndex(e), false);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getSource() == this) {
            requestFocusInWindow();
        }
    }

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
        tryUpdateSelectedIndex(hoveredIndex, false);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        hoveredIndex = calculateFaceIndex(e);
        repaint();
    }

    private void setupKeyboardActions() {
        var actions = getActionMap();
        actions.put(ACTION_SELECT_FACE_NEXT, new SelectNextFaceAction());
        actions.put(ACTION_SELECT_FACE_PREVIOUS, new SelectPreviousFaceAction());
        actions.put(ACTION_SELECT_FACE_ABOVE, new SelectAboveFaceAction());
        actions.put(ACTION_SELECT_FACE_BELOW, new SelectBelowFaceAction());

        var inputs = getInputMap(WHEN_FOCUSED);
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), ACTION_SELECT_FACE_ABOVE);
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), ACTION_SELECT_FACE_BELOW);
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), ACTION_SELECT_FACE_PREVIOUS);
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), ACTION_SELECT_FACE_NEXT);
    }

    private static final String ACTION_SELECT_FACE_NEXT = "select_face.next";
    private static final String ACTION_SELECT_FACE_PREVIOUS = "select_face.previous";
    private static final String ACTION_SELECT_FACE_ABOVE = "select_face.above";
    private static final String ACTION_SELECT_FACE_BELOW = "select_face.below";

    private final class SelectNextFaceAction extends AbstractAction {
        public SelectNextFaceAction() {
            super("Select next face");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tryUpdateSelectedIndex(selectedIndex + 1, true);
        }
    }

    private final class SelectPreviousFaceAction extends AbstractAction {
        public SelectPreviousFaceAction() {
            super("Select previous face");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tryUpdateSelectedIndex(selectedIndex - 1, true);
        }
    }

    private final class SelectAboveFaceAction extends AbstractAction {
        public SelectAboveFaceAction() {
            super("Select above face");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tryUpdateSelectedIndex(selectedIndex - 5, true);
        }
    }

    private final class SelectBelowFaceAction extends AbstractAction {
        public SelectBelowFaceAction() {
            super("Select below face");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tryUpdateSelectedIndex(selectedIndex + 5, true);
        }
    }
}
