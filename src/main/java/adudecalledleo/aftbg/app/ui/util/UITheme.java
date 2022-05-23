package adudecalledleo.aftbg.app.ui.util;

import java.util.LinkedList;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.metal.*;

import adudecalledleo.aftbg.Main;

public sealed class UITheme {
    private static final List<UITheme> THEMES;
    private static volatile UITheme currentTheme;

    private static final String METAL_LAF_CLASS = "javax.swing.plaf.metal.MetalLookAndFeel";
    private static final MetalTheme[] METAL_THEMES = { new OceanTheme(), new DefaultMetalTheme() };

    static {
        final String currentLAF = UIManager.getLookAndFeel().getClass().getName();
        List<UITheme> themes = new LinkedList<>();

        for (var info : UIManager.getInstalledLookAndFeels()) {
            if (METAL_LAF_CLASS.equals(info.getClassName())) {
                // add different metal themes
                boolean isCurrentTheme = METAL_LAF_CLASS.equals(currentLAF);
                final var currentMetalTheme = MetalLookAndFeel.getCurrentTheme();
                for (var metalTheme : METAL_THEMES) {
                    var theme = new UITheme.Metal(info.getName(), METAL_LAF_CLASS, metalTheme);
                    if (isCurrentTheme && currentMetalTheme.getClass() == metalTheme.getClass()) {
                        currentTheme = theme;
                    }
                    themes.add(theme);
                }
            } else {
                var theme = new UITheme(info.getName(), info.getClassName());
                if (currentLAF.equals(info.getClassName())) {
                    currentTheme = theme;
                }
                themes.add(theme);
            }
        }

        THEMES = List.copyOf(themes);
    }

    public static void init() { /* <clinit> */ }

    public static List<UITheme> getAllThemes() {
        return THEMES;
    }

    public static UITheme getCurrentTheme() {
        return currentTheme;
    }

    protected final String name, className;

    protected UITheme(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public boolean apply() {
        try {
            UIManager.setLookAndFeel(className);
            currentTheme = this;
            return true;
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException e) {
            Main.logger().error("Failed to apply theme!", e);
            return false;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    private static final class Metal extends UITheme {
        private final MetalTheme theme;

        public Metal(String name, String className, MetalTheme theme) {
            super(name + " (" + theme.getName() + ")", className);
            this.theme = theme;
        }

        @Override
        public boolean apply() {
            MetalLookAndFeel.setCurrentTheme(theme);
            return super.apply();
        }
    }
}
