package io.snice.buffer;

import io.snice.buffer.impl.DefaultImmutableBuffer;
import io.snice.buffer.impl.DefaultReadableBuffer;
import io.snice.buffer.impl.EmptyBuffer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReadableBufferTest extends AbstractReadableBufferTest {

    @Override
    public Buffer createBuffer(byte[] array) {
        return DefaultReadableBuffer.of(array);
    }

}
