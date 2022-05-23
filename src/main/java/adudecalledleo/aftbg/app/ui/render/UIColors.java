package adudecalledleo.aftbg.app.ui.render;

import javax.swing.*;

public final class UIColors {
    private UIColors() { }

    public static void update() {
        Label.update();
        List.update();
    }

    private static UIColor getColor(Object key) {
        return new UIColor(UIManager.getColor(key));
    }

    private static UIColor getColor(String key, String... fallbackKeys) {
        var c = UIManager.getColor(key);
        if (c == null) {
            for (String fallbackKey : fallbackKeys) {
                c = UIManager.getColor(fallbackKey);
                if (c != null) {
                    break;
                }
            }
        }
        return new UIColor(c);
    }

    public static final class Label {
        private static UIColor disabledForeground;

        private static void update() {
            disabledForeground = getColor("Label.disabledForeground", "Label.disabledText");
        }

        public static UIColor getDisabledForeground() {
            return disabledForeground;
        }
    }

    public static final class List {
        private static UIColor background;
        private static UIColor darkerBackground;
        private static UIColor selectionBackground;
        private static UIColor hoveredBackground;
        private static UIColor disabledBackground;
        private static UIColor darkerDisabledBackground;

        private static void update() {
            background = getColor("List.background");
            darkerBackground = background.darker(0.9);
            selectionBackground = getColor("List.selectionBackground", "List[Selected].textBackground");
            hoveredBackground = selectionBackground.withAlpha(127);
            disabledBackground = background.darker(0.6);
            darkerDisabledBackground = disabledBackground.darker(0.9);
        }

        public static UIColor getBackground() {
            return background;
        }

        public static UIColor getDarkerBackground() {
            return darkerBackground;
        }

        public static UIColor getSelectionBackground() {
            return selectionBackground;
        }

        public static UIColor getHoveredBackground() {
            return hoveredBackground;
        }

        public static UIColor getDisabledBackground() {
            return disabledBackground;
        }

        public static UIColor getDarkerDisabledBackground() {
            return darkerDisabledBackground;
        }

        private List() { }
    }
}
