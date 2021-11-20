package adudecalledleo.aftbg.logging;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

final class ExceptionWriter extends PrintWriter {
    private final String newLinePrefix;
    private boolean newLine;

    public ExceptionWriter(String newLinePrefix) {
        super(new StringWriter());
        this.newLinePrefix = newLinePrefix;
        newLine = true;
    }

    public StringBuffer getBuffer() {
        return ((StringWriter) out).getBuffer();
    }

    public void reset() {
        ((StringWriter) out).getBuffer().setLength(0);
        newLine = true;
    }

    @Override
    public void write(int c) {
        if (newLine) {
            newLine = false;
            print(newLinePrefix);
        }
        super.write(c);
    }

    @Override
    public void write(@NotNull String s, int off, int len) {
        if (newLine) {
            newLine = false;
            print(newLinePrefix);
        }
        super.write(s, off, len);
    }

    @Override
    public void println() {
        super.println();
        newLine = true;
    }
}
