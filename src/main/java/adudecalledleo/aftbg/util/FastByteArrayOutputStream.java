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

    /**
     * Returns the buffer. Note that this buffer's length may not be equal to the {@linkplain #size() actual size}
     * of the buffer!
     *
     * @return the current contents of this output stream, as a byte array.
     * @see #size()
     */
    @Override
    public byte[] toByteArray() {
        return buf;
    }
}
