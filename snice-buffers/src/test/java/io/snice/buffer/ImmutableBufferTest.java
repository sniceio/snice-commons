package io.snice.buffer;

import io.snice.buffer.impl.DefaultImmutableBuffer;
import io.snice.buffer.impl.EmptyBuffer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ImmutableBufferTest extends AbstractBufferTest {

    @Override
    public Buffer createBuffer(final byte[] array) {
        return DefaultImmutableBuffer.of(array);
    }

    @Override
    public Buffer createBuffer(final byte[] array, int offset, int length) {
        return DefaultImmutableBuffer.of(array, offset, length);
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

    @Test
    public void testGoodBuffer() {
        assertGoodBuffer(10, 3, 7);

        // will be empty but is still allowed. Remember that
        // the offset is zero based so 9 will be the last
        // "good" index
        assertGoodBuffer(10, 9, 0);

        assertGoodBuffer(10, 0, 10);
    }

    @Test
    public void testBadCreateBuffer() {
        assertBadBuffer(10, 0, -1);
        assertBadBuffer(10, -1, 4);
        assertBadBuffer(10, 3, 11);
    }

    private static void assertGoodBuffer(final int size, final int offset, final int length) {
        try {
            final byte[] buf = new byte[size];
            Buffer.of(buf, offset, length);
        } catch (final IllegalArgumentException e) {
            fail("Dit NOT expect the creation of the buffer to fail for a buffer of: "
                    + size + ", an offset of " + offset + " and an length of: " + length);
        }

    }

    private static void assertBadBuffer(final int size, final int offset, final int length) {
        try {
            final byte[] buf = new byte[size];
            Buffer.of(buf, offset, length);
            fail("Expected to fail with an IllegalArgumentException for a buffer of size "
                    + size + " an offset of " + offset + " and a length of: " + length);
        } catch (final IllegalArgumentException e) {
            // expected
        }

    }

}
