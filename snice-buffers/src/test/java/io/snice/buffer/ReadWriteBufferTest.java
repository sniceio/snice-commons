package io.snice.buffer;

import io.snice.buffer.impl.DefaultReadWriteBuffer;

public class ReadWriteBufferTest extends AbstractReadWritableBufferTest {

    @Override
    public Buffer createBuffer(final byte[] array) {
        return DefaultReadWriteBuffer.of(array);
    }

    @Override
    public Buffer createBuffer(final byte[] array, final int offset, final int length) {
        return DefaultReadWriteBuffer.of(array, offset, length);
    }

    @Override
    public ReadWriteBuffer createWritableBuffer(final int capacity) {
        return DefaultReadWriteBuffer.of(capacity);
    }


}
