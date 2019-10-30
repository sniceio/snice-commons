package io.snice.buffer;

import io.snice.buffer.impl.DefaultWritableBuffer;

public class WritableBufferTest extends AbstractWritableBufferTest {

    @Override
    public Buffer createBuffer(final byte[] array) {
        return DefaultWritableBuffer.of(array);
    }

    @Override
    public Buffer createBuffer(final byte[] array, final int offset, final int length) {
        return DefaultWritableBuffer.of(array, offset, length);
    }

    @Override
    public WritableBuffer createWritableBuffer(final int capacity) {
        return DefaultWritableBuffer.of(capacity);
    }


}
