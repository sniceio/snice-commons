package io.snice.buffer;

import io.snice.buffer.impl.DefaultReadWriteBuffer;

import static io.snice.preconditions.PreConditions.assertArray;

public interface ReadWriteBuffer extends ReadableBuffer, WritableBuffer {

    static ReadWriteBuffer of(final byte... buffer) {
        assertArray(buffer);
        return DefaultReadWriteBuffer.of(buffer);
    }

    static ReadWriteBuffer of(final int capacity) {
        return DefaultReadWriteBuffer.of(capacity);
    }

    static ReadWriteBuffer of(final byte[] buffer, final int offset, final int length) {
        return DefaultReadWriteBuffer.of(buffer, offset, length);
    }

}
