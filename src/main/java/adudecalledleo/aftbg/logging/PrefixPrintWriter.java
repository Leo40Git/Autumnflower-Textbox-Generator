package adudecalledleo.aftbg.logging;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.Writer;

final class PrefixPrintWriter extends PrintWriter {
    private final String prefix;
    private boolean newLine;

    public PrefixPrintWriter(@NotNull Writer out, String prefix) {
        super(out);
        this.prefix = prefix;
        newLine = true;
    }

    @Override
    public void write(int c) {
        if (newLine) {
            newLine = false;
            print(prefix);
        }
        super.write(c);
    }

    @Override
    public void write(@NotNull String s, int off, int len) {
        if (newLine) {
            newLine = false;
            print(prefix);
        }
        super.write(s, off, len);
    }

    @Override
    public void println() {
        super.println();
        newLine = true;
    }
}
