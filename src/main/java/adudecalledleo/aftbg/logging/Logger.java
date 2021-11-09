package adudecalledleo.aftbg.logging;

import adudecalledleo.aftbg.Bootstrap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss,SSS");

    private static BufferedWriter writer;

    private Logger() { }

    public static void init() throws IOException {
        writer = Files.newBufferedWriter(Paths.get(".", Bootstrap.LOG_NAME), StandardOpenOption.WRITE);
        Runtime.getRuntime().addShutdownHook(new Thread(Logger::shutdown));
    }

    public static void shutdown() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) { }
            writer = null;
        }
    }

    public static void log(Level level, String message, Throwable cause) {
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder().append(TIME_FORMAT.format(now)).append(" [").append(level).append("] ").append(message);
        if (cause != null) {
            sb.append(System.lineSeparator()).append('\t').append(cause);
            for (StackTraceElement elem : cause.getStackTrace()) {
                sb.append(System.lineSeparator()).append("\t\tat ").append(elem);
            }
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

    public static void log(Level level, String message) {
        log(level, message, null);
    }
}
