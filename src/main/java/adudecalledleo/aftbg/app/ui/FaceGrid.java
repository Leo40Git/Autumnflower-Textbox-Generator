package adudecalledleo.aftbg.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.List;
import java.util.*;

import javax.swing.*;

import adudecalledleo.aftbg.Main;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.face.Face;
import adudecalledleo.aftbg.app.face.FaceCategory;
import adudecalledleo.aftbg.app.ui.render.NegativeComposite;
import adudecalledleo.aftbg.app.ui.render.UIColor;
import adudecalledleo.aftbg.app.ui.render.UIColors;

public final class FaceGrid extends JComponent implements Scrollable, MouseListener, MouseMotionListener {
    private static final String AFTER_PREFIX = "after:";
    private static final Dimension DEFAULT_SIZE = new Dimension(72 * 5, 72 * 8);

    public static Dimension getDefaultSize(Dimension rv) {
        rv.width = DEFAULT_SIZE.width;
        rv.height = DEFAULT_SIZE.height;
        return rv;
    }

    public static Dimension getDefaultSize() {
        return getDefaultSize(new Dimension());
    }

    private static final class GroupInfo {
        public final int firstIndex;
        public int nextIndex, size;
        public boolean expanded;

        public GroupInfo(int firstIndex) {
            this.firstIndex = firstIndex;
            nextIndex = firstIndex;
            size = 0;
            expanded = false;
        }
    }

    private final List<Face> faceList;
    private final Map<String, GroupInfo> groups;
    private FaceCategory category;
    private Face selectedFace;
    private int selectedIndex, hoveredIndex;
    private BufferedImage backplate;

    public FaceGrid() {
        faceList = new ArrayList<>();
        groups = new HashMap<>();
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
        var g2d = (Graphics2D) g;

        paintBackplate(g2d);

        final int faceCount = faceList.size();
        int x = 0, y = 0;
        for (int i = 0; i < faceCount; i++) {
            Face face = faceList.get(i);

            UIColor bgCol;
            if (i == selectedIndex) {
                bgCol = UIColors.List.getSelectionBackground();
            } else {
                bgCol = (i % 2 == 0) ? UIColors.List.getBackground() : UIColors.List.getDarkerBackground();
            }
            g2d.setColor(bgCol.get());
            g2d.fillRect(x, y, 72, 72);

            if (i == hoveredIndex) {
                g2d.setColor(UIColors.List.getHoveredBackground().get());
                g2d.fillRect(x, y, 72, 72);
            }

            GroupInfo group;
            if ((group = getGroupInfo(face)) != null) {
                if (!group.expanded && i != group.firstIndex) {
                    continue;
                }
            }

            g2d.drawImage(Objects.requireNonNull(face.getIcon()).getImage(), x, y, null);

            if (group != null && i == group.firstIndex) {
                BufferedImage icon = group.expanded ? AppResources.getCollapseIcon() : AppResources.getExpandIcon();
                var oldComp = g2d.getComposite();
                g2d.setComposite(NegativeComposite.INSTANCE);
                g2d.drawImage(icon, x, y + 72 - icon.getHeight(), null);
                g2d.setComposite(oldComp);
            }

            x += 72;
            if (x >= DEFAULT_SIZE.width) {
                x = 0;
                y += 72;
            }
        }
    }

    private void paintBackplate(Graphics g) {
        if (backplate == null) {
            final int width = getWidth(), height = getHeight();
            backplate = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = backplate.createGraphics();
            bg.setColor(UIColors.List.getDisabledBackground().get());
            bg.fillRect(0, 0, getWidth(), getHeight());
            bg.setColor(UIColors.List.getDarkerDisabledBackground().get());
            int x = 72, y = 0;
            while (y < height) {
                bg.fillRect(x, y, 72, 72);
                x += 144;
                if (x >= width) {
                    x -= width;
                    y += 72;
                }
            }
            bg.dispose();
        }
        g.drawImage(backplate, 0, 0, null);
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

    private GroupInfo getGroupInfo(Face face) {
        if (face.getGroup().isEmpty()) {
            return groups.get(AFTER_PREFIX + face.getName());
        } else {
            return groups.get(face.getGroup());
        }
    }

    private int getFaceIndexAt(int mx, int my) {
        final int faceCount = faceList.size();
        int x = 0, y = 0;
        for (int i = 0; i < faceCount; i++) {
            Face face = faceList.get(i);

            GroupInfo group;
            if ((group = getGroupInfo(face)) != null) {
                if (i != group.firstIndex && !group.expanded) {
                    continue;
                }
            }

            if (mx >= x && my >= y && mx <= x + 72 && my <= y + 72) {
                return i;
            }

            x += 72;
            if (x >= DEFAULT_SIZE.width) {
                x = 0;
                y += 72;
            }
        }
        return -1;
    }

    private int getFaceIndexAt(MouseEvent event) {
        return getFaceIndexAt(event.getX(), event.getY());
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int index = getFaceIndexAt(event);
        if (index >= 0 && index < faceList.size()) {
            Face face = faceList.get(index);
            GroupInfo group = getGroupInfo(face);
            if (group == null || index != group.firstIndex) {
                return "<html>" + face.toToolTipText(true) + "</html>";
            } else {
                return "<html>" + face.toToolTipText(true) + "<hr>"
                        + "<em>group of %d - right-click to %s</em></html>".formatted(group.size, group.expanded ? "collapse" : "expand");
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
            updateFaceList();
            resize();

            firePropertyChange("category", oldCategory, category);
        }
    }

    private void updateFaceList() {
        faceList.clear();
        groups.clear();

        Face selectedFace = null;

        int i = 0;
        for (var face : category.getFaces().values()) {
            if (!face.getGroup().isEmpty()) {
                GroupInfo group = null;
                if (groups.containsKey(face.getGroup())) {
                    group = groups.get(face.getGroup());
                } else if (face.getGroup().startsWith(AFTER_PREFIX)) {
                    String faceName = face.getGroup().substring(AFTER_PREFIX.length());
                    var firstFace = category.getFace(faceName);
                    if (firstFace.getGroup().isEmpty()) {
                        int firstIndex = faceList.indexOf(firstFace);
                        if (firstIndex >= 0) {
                            group = new GroupInfo(firstIndex);
                            group.nextIndex++;
                            group.size++;
                            groups.put(face.getGroup(), group);
                        } else {
                            Main.logger().warn("[FaceGrid] Tried to add \"{}\" group for non-existent face {}, "
                                    + "adding new group at the end instead", AFTER_PREFIX, faceName);
                        }
                    } else {
                        group = groups.get(firstFace.getGroup());
                    }
                }

                if (group == null) {
                    group = new GroupInfo(i);
                    groups.put(face.getGroup(), group);
                }

                faceList.add(group.nextIndex++, face);
                group.size++;
            } else {
                faceList.add(face);
            }

            if (i == selectedIndex) {
                selectedFace = face;
            }
            i++;
        }

        setSelectedFace(selectedFace, selectedFace != null);
    }

    private void resize() {
        final int faceCount = faceList.size();
        int newHeight = 0;
        for (int i = 0; i < faceCount; i++) {
            Face face = faceList.get(i);

            GroupInfo group;
            if ((group = getGroupInfo(face)) != null) {
                if (i != group.firstIndex && !group.expanded) {
                    continue;
                }
            }

            newHeight++;
        }

        if (newHeight % 5 == 0) {
            newHeight /= 5;
        } else {
            newHeight = (newHeight / 5) + 1;
        }
        newHeight *= 72;

        if (backplate != null && backplate.getHeight() != newHeight) {
            backplate = null;
        }

        var size = new Dimension(DEFAULT_SIZE.width, Math.max(DEFAULT_SIZE.height, newHeight));
        setSize(size);
        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
        invalidate();
        repaint();
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

                GroupInfo selectedGroup;
                if (!selectedFace.getGroup().isEmpty() && (selectedGroup = groups.get(selectedFace.getGroup())) != null) {
                    selectedGroup.expanded = true;
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
            if (e.getButton() == MouseEvent.BUTTON3) {
                // RMB, not MMB -
                //  getButton() returns the index of the button from left to right
                int i = getFaceIndexAt(e);
                if (i >= 0) {
                    Face face = faceList.get(i);
                    GroupInfo group;
                    if ((group = getGroupInfo(face)) == null) {
                        return;
                    }
                    if (i == group.firstIndex) {
                        group.expanded = !group.expanded;
                        resize();
                    }
                }
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                tryUpdateSelectedIndex(getFaceIndexAt(e), false);
            }
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
        if (e.getSource() == this) {
            hoveredIndex = getFaceIndexAt(e);
            repaint();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() == this) {
            hoveredIndex = -1;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getSource() == this) {
            hoveredIndex = getFaceIndexAt(e);
            tryUpdateSelectedIndex(hoveredIndex, false);
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getSource() == this) {
            hoveredIndex = getFaceIndexAt(e);
            repaint();
        }
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
}
