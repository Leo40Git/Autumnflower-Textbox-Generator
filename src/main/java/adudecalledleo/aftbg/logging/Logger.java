package adudecalledleo.aftbg.logging;

import adudecalledleo.aftbg.BuildInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Logger {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss,SSS");
    private static final ExceptionWriter EXCEPTION_WRITER = new ExceptionWriter("\t");

    private static String logFile;
    private static BufferedWriter writer;

    private Logger() { }

    public static void init() throws IOException {
        logFile = BuildInfo.abbreviatedName().toLowerCase(Locale.ROOT) + ".log";
        Path path = Paths.get(".", logFile).toAbsolutePath();
        Files.deleteIfExists(path);
        writer = Files.newBufferedWriter(path);
    }

    public static void shutdown() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) { }
            writer = null;
        }
    }

    public static String logFile() {
        return logFile;
    }

    public static void log(Level level, String message, Throwable cause) {
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder().append(TIME_FORMAT.format(now)).append(' ')
                .append("[%-20s] ".formatted(Thread.currentThread().getName()))
                .append("[%-5s] ".formatted(level))
                .append(message);
        if (cause != null) {
            EXCEPTION_WRITER.reset();
            cause.printStackTrace(EXCEPTION_WRITER);
            sb.append(System.lineSeparator()).append(EXCEPTION_WRITER.getBuffer());
            // remove last newline
            sb.setLength(sb.length() - System.lineSeparator().length());
        }
        String msg = sb.toString();

        PrintStream stream;
        if (level.isMoreSignificantThan(Level.ERROR)) {
            stream = System.err;
        } else {
            stream = System.out;
        }
        stream.println(msg);
        if (level.isMoreSignificantThan(Level.DEBUG)) {
            try {
                writer.write(msg);
                writer.newLine();
            } catch (IOException ignored) { }
        }
    }

    public static void trace(String message, Throwable cause) {
        log(Level.TRACE, message, cause);
    }

    public static void debug(String message, Throwable cause) {
        log(Level.DEBUG, message, cause);
    }

    public static void info(String message, Throwable cause) {
        log(Level.INFO, message, cause);
    }

    public static void warn(String message, Throwable cause) {
        log(Level.WARN, message, cause);
    }

    public static void error(String message, Throwable cause) {
        log(Level.ERROR, message, cause);
    }

    public static void fatal(String message, Throwable cause) {
        log(Level.FATAL, message, cause);
    }

    public static void log(Level level, String message) {
        log(level, message, null);
    }

    public static void trace(String message) {
        log(Level.TRACE, message);
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void fatal(String message) {
        log(Level.FATAL, message);
    }
}
