package adudecalledleo.aftbg.app.util;

import java.util.Locale;

import adudecalledleo.aftbg.Main;

public enum OperatingSystem {
    WINDOWS, MAC, LINUX, UNKNOWN;

    private static OperatingSystem current;

    public static OperatingSystem get() {
        if (current == null) {
            current = detect();
        }
        return current;
    }

    public static boolean isWindows() {
        return get() == WINDOWS;
    }

    public static boolean isMac() {
        return get() == MAC;
    }

    public static boolean isLinux() {
        return get() == LINUX;
    }

    public static boolean isUnknown() {
        return get() == UNKNOWN;
    }

    private static OperatingSystem detect() {
        String osName = System.getProperty("os.name");
        if (osName == null) {
            Main.logger().error("System property \"os.name\" has no value! Something is seriously wrong with this JVM!");
            return UNKNOWN;
        }

        osName = osName.toLowerCase(Locale.ROOT);
        if (osName.contains("mac") || osName.contains("darwin")) {
            return MAC;
        } else if (osName.contains("win")) {
            return WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return LINUX;
        }

        Main.logger().warn("Unknown (and possibly unsupported) operating system, os.name is \"%s\""
                .formatted(System.getProperty("os.name")));
        return UNKNOWN;
    }
}
