package adudecalledleo.aftbg.app.ui.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.plaf.metal.*;

import adudecalledleo.aftbg.Main;

public sealed class UITheme {
    private static final Map<String, UITheme> THEMES;
    private static volatile UITheme currentTheme;
    private static UITheme crossPlatformTheme, systemTheme;

    private static final String METAL_LAF_CLASS = "javax.swing.plaf.metal.MetalLookAndFeel";
    private static final MetalTheme[] METAL_THEMES = { new OceanTheme(), new DefaultMetalTheme() };

    static {
        final String currentLAF = UIManager.getLookAndFeel().getClass().getName();
        Map<String, UITheme> themes = new LinkedHashMap<>();

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
                    themes.put(theme.getName(), theme);
                }
            } else {
                var theme = new UITheme(info.getName(), info.getClassName());
                if (currentLAF.equals(info.getClassName())) {
                    currentTheme = theme;
                }
                themes.put(theme.getName(), theme);
            }
        }

        THEMES = Map.copyOf(themes);
    }

    public static void init() { /* <clinit> */ }

    public static Collection<UITheme> getAllThemes() {
        return THEMES.values();
    }

    public static UITheme getTheme(String name) {
        var theme = THEMES.get(name);
        if (theme == null) {
            throw new IllegalArgumentException("Couldn't find theme \"" + name + "\"!");
        }
        return theme;
    }

    public static UITheme getThemeByClassName(String className) {
        for (var theme : THEMES.values()) {
            if (className.equals(theme.getClassName())) {
                return theme;
            }
        }
        throw new IllegalArgumentException("Couldn't find theme with class name \"" + className + "\"!");
    }

    public static UITheme getCrossPlatformTheme() {
        if (crossPlatformTheme == null) {
            crossPlatformTheme = getThemeByClassName(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        return crossPlatformTheme;
    }

    public static UITheme getSystemTheme() {
        if (systemTheme == null) {
            systemTheme = getThemeByClassName(UIManager.getSystemLookAndFeelClassName());
        }
        return systemTheme;
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
