package adudecalledleo.aftbg.app.text.util;

import java.util.Optional;

public final class StringScanner {
    public static final char EOF = 0;

    private final char[] chars;
    private final StringBuilder tempSB;
    private int pos;

    public StringScanner(String string) {
        chars = string.toCharArray();
        tempSB = new StringBuilder();
        pos = 0;
    }

    public int tell() {
        return pos;
    }

    public int end() {
        return chars.length - 1;
    }

    public int remaining() {
        return chars.length - 1 - pos;
    }

    public void seek(int newPos) {
        this.pos = Math.max(0, Math.min(chars.length, newPos));
    }

    public char peek() {
        if (pos >= chars.length) {
            return EOF;
        }
        return chars[pos];
    }

    public void next() {
        pos++;
    }

    public char read() {
        if (pos >= chars.length) {
            return EOF;
        }
        return chars[pos++];
    }

    public Optional<String> read(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length <= 0");
        }
        if (pos + length > chars.length) {
            return Optional.empty();
        }
        var s = new String(chars, pos, length);
        pos += length;
        return Optional.of(s);
    }

    public Optional<String> until(char terminator) {
        if (pos >= chars.length) {
            return Optional.empty();
        }
        if (chars[pos] == terminator) {
            return Optional.of("");
        }
        final int startPos = pos;
        char c = read();
        while (c != EOF && c != terminator) {
            c = read();
        }
        if (c == terminator) {
            var s = new String(chars, startPos, pos - startPos - 1);
            return Optional.of(s);
        } else {
            pos = startPos;
            return Optional.empty();
        }
    }

    public Optional<String> until(String terminator) {
        final int oldPos = pos;
        tempSB.setLength(0);
        boolean found = false;
        final char[] termChars = terminator.toCharArray();
        final int maxSearchPos = chars.length - termChars.length + 1;
        for (; pos < maxSearchPos; pos++) {
            found = true;
            for (int termPos = 0; termPos < termChars.length; termPos++) {
                if (chars[pos + termPos] != termChars[termPos]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                break;
            } else {
                tempSB.append(peek());
            }
        }
        if (found) {
            pos += termChars.length;
            return Optional.of(tempSB.toString());
        } else {
            pos = oldPos;
            return Optional.empty();
        }
    }
}
