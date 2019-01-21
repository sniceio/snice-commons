package io.snice.buffer;

import io.snice.buffer.impl.DefaultImmutableBuffer;
import io.snice.buffer.impl.EmptyBuffer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ImmutableBufferTest extends AbstractBufferTest {

    @Override
    public Buffer createBuffer(final byte[] array) {
        return DefaultImmutableBuffer.of(array);
    }

    /**
     * The internal implementation of the {@link DefaultImmutableBuffer} relies on the fact that
     * the of-method is checking the length of the passed in array and creates an empty buffer
     * instead if the array is zero length. E.g. the method {@link DefaultImmutableBuffer#hasReadableBytes()}
     * relies on this behavior and you'll mess is up if you change this. So, if this unit test breaks you really
     * need to change some of the internals as well...
     */
    @Test
    public void testCreatWithEmpty() {
        final Buffer b = DefaultImmutableBuffer.of(new byte[0]);
        assertThat(b instanceof EmptyBuffer, is(true));
        assertThat(b.isEmpty(), is(true));
    }

    @Test
    public void testWrapLong() throws Exception {
        assertThat(Buffers.wrap(123L).toString(), is("123"));
        assertThat(Buffers.wrap(-123L).toString(), is("-123"));
    }

    @Test
    public void testWrapInt() throws Exception {
        assertThat(Buffers.wrap(123).toString(), is("123"));
        assertThat(Buffers.wrap(-123).toString(), is("-123"));
    }

}
