package io.snice.buffer;

import io.snice.buffer.impl.DefaultReadableBuffer;

public class ReadableBufferTest extends AbstractReadableBufferTest {

    @Override
    public Buffer createBuffer(final byte[] array) {
        return DefaultReadableBuffer.of(array);
    }

    @Override
    public Buffer createBuffer(final byte[] array, final int offset, final int length) {
        return DefaultReadableBuffer.of(array, offset, length);
    }

}
