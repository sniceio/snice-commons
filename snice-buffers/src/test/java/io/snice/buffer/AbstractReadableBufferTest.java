package io.snice.buffer;

import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Base class for all those tests concerning {@link ReadableBuffer}s.
 *
 */
public abstract class AbstractReadableBufferTest extends AbstractBufferTest {

    @Test
    public void testReadProgression() {
        final ReadableBuffer buffer = createBuffer("hello world").toReadableBuffer();

        // read the first 5 bytes...
        assertThat(buffer.readBytes(5).toString(), is("hello"));

        // which means that what we have left is " world".
        assertThat(buffer.toString(), is(" world"));

        // read 5 more bytes, which will consume almost all (not the 'd')
        assertThat(buffer.readBytes(5).toString(), is(" worl"));

        // so now we should only have a single 'd' left.
        assertThat(buffer.toString(), is("d"));

        // if we reset the reader index we can start over...
        buffer.setReaderIndex(0);
    }

    @Test
    public void testResetReaderIndex() {
        final ReadableBuffer buffer = createBuffer("more stuff to write about").toReadableBuffer();
        assertThat(buffer.readBytes(10).toString(), is("more stuff"));
        assertThat(buffer.toString(), is(" to write about"));

        // rest the reader index...
        buffer.setReaderIndex(0);
        assertThat(buffer.readBytes(10).toString(), is("more stuff"));
        assertThat(buffer.toString(), is(" to write about"));

        // set to something else...
        buffer.setReaderIndex(5);
        assertThat(buffer.readBytes(10).toString(), is("stuff to w"));
        assertThat(buffer.toString(), is("rite about"));
    }

    @Test
    public void testReadFromSliced() {
        final ReadableBuffer buffer = createBuffer("lets test some sliced reading").toReadableBuffer();
        final ReadableBuffer slice01 = buffer.readBytes(15).toReadableBuffer();
        assertThat(slice01.toString(), is("lets test some "));
        assertThat(slice01.readBytes(4).toString(), is("lets"));
        assertThat(slice01.readBytes(5).toString(), is(" test"));

        final ReadableBuffer slice02 = buffer.readBytes(8).toReadableBuffer();
        assertThat(slice02.toString(), is("sliced r"));
        assertThat(slice02.setReaderIndex(1).readBytes(3).toString(), is("lic"));
        assertThat(slice02.readBytes(4).toString(), is("ed r"));
        ensureUnableToRead(slice02, b -> b.readByte()); // should be at the end so if we try again, we should blow up
    }

    /**
     * Ensure we can't set the reader index to outside the capacity of the buffer
     */
    @Test
    public void setBadReaderIndex() {
        final String content = "checking the reader index";
        final ReadableBuffer buffer = createBuffer(content).toReadableBuffer();
        assertThat(buffer.capacity(), is(content.length()));

        // set to the very last index is ok though
        buffer.setReaderIndex(content.length());
        assertThat(buffer.hasReadableBytes(), is(false));
        ensureUnableToRead(buffer, b -> b.readByte());
        ensureUnableToRead(buffer, b -> b.readBytes(2));
        ensureUnableToRead(buffer, b -> b.readUnsignedInt());

        ensureUnableToSetReaderIndex(buffer, -1);
        ensureUnableToSetReaderIndex(buffer, content.length() + 1);
    }

    private static void ensureUnableToRead(final ReadableBuffer buffer, final Consumer<ReadableBuffer> fn) {
        try {
            fn.accept(buffer);
            fail("expected to fail here...");
        } catch (final IndexOutOfBoundsException e) {
            // expected...
        }
    }

    private static void ensureUnableToSetReaderIndex(final ReadableBuffer buffer, final int index) {
        try {
            buffer.setReaderIndex(index);
            fail("Expected to blow up");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }
}
