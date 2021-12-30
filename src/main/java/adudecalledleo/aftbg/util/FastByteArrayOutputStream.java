package adudecalledleo.aftbg.util;

import java.io.ByteArrayOutputStream;

public final class FastByteArrayOutputStream extends ByteArrayOutputStream {
    public FastByteArrayOutputStream() { }

    public FastByteArrayOutputStream(int size) {
        super(size);
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public byte[] toByteArray() {
        return buf;
    }
}
