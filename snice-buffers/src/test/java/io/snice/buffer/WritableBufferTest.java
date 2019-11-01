package io.snice.buffer;

import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class WritableBufferTest {

    @Test
    public void testWriteIntAsString() {
        final WritableBuffer buffer = WritableBuffer.of(100);
        buffer.writeAsString(0);
        buffer.write((byte) ' ');
        buffer.writeAsString(10);
        buffer.write((byte) ' ');
        buffer.writeAsString(100);
        buffer.write((byte) ' ');
        buffer.writeAsString(9712);
        assertThat(buffer.build().toString(), is("0 10 100 9712"));
    }

    @Test
    public void testWriteLongAsString() {
        final WritableBuffer buffer = WritableBuffer.of(100);
        buffer.writeAsString(0L);
        buffer.write((byte) ' ');
        buffer.writeAsString(10L);
        buffer.write((byte) ' ');
        buffer.writeAsString(100L);
        buffer.write((byte) ' ');
        buffer.writeAsString(9712L);

        assertThat(buffer.build().toString(), is("0 10 100 9712"));
    }

    @Test
    public void testWriteThenRead() {
        final WritableBuffer writable = WritableBuffer.of(100);
        writable.write(512);
        writable.write(123);

        final ReadableBuffer buffer = writable.build().toReadableBuffer();
        assertThat(buffer.hasReadableBytes(), is(true));
        assertThat(buffer.getReadableBytes(), is(2 * 4));

        assertThat(buffer.readInt(), is(512));
        assertThat(buffer.readInt(), is(123));

        assertThat(buffer.hasReadableBytes(), is(false));
    }

    @Test
    public void testSetBits() {
        final WritableBuffer writable = WritableBuffer.of(new byte[100]);
        writable.setWriterIndex(writable.capacity());

        // just making sure that the short-hand versions
        // is actually working.
        final int index = 12;
        writable.setBit0(index, true);
        writable.setBit1(index, true);
        writable.setBit2(index, true);
        writable.setBit3(index, true);
        writable.setBit4(index, true);
        writable.setBit5(index, true);
        writable.setBit6(index, true);
        writable.setBit7(index, true);

        final Buffer buffer = writable.build();
        assertThat(buffer.getBit0(index), is(true));
        assertThat(buffer.getBit1(index), is(true));
        assertThat(buffer.getBit2(index), is(true));
        assertThat(buffer.getBit3(index), is(true));
        assertThat(buffer.getBit4(index), is(true));
        assertThat(buffer.getBit5(index), is(true));
        assertThat(buffer.getBit6(index), is(true));
        assertThat(buffer.getBit7(index), is(true));

        // which should really be the same as above but who knows,
        // copy-paste errors are easy to make
        ensureAllBits(buffer, index, true);
    }

    @Test
    public void testSetBits2() {
        // 97 binary is 1100001, let's set those bits
        // and sure we can read out 97
        final WritableBuffer writable = WritableBuffer.of(new byte[4]);
        writable.setWriterIndex(4);
        writable.setBit0(3, true);
        writable.setBit5(3, true);
        writable.setBit6(3, true);

        final int value = writable.build().getInt(0);
        assertThat(value, is(97));

    }

    private static void ensureBits(final WritableBuffer writable, final int index) {
    }


    private static void ensureAllBits(final Buffer buffer, final int index, final boolean on) {
        for (int bit = 0; bit < 8; ++bit) {
            assertThat(buffer.getBit(index, bit), is(on));
        }
    }

    @Test
    public void testZeroOut() {
        ensureZeroOut('a', 150, 50, 50);
        ensureZeroOut('b', 50, 10, 10);
        ensureZeroOut('c', 10, 0, 1); // boundary testing
        ensureZeroOut('c', 10, 9, 1); // boundary testing
        ensureZeroOut('c', 10, 9, 0); // doesn't make sense but should work
    }

    private static void ensureZeroOut(final char b, final int byteArrayLength, final int offset, final int length) {
        final WritableBuffer writable = WritableBuffer.of(new byte[byteArrayLength], offset, length);
        writable.zeroOut((byte)b);
        writable.setWriterIndex(length);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            sb.append(b);
        }

        final Buffer buffer = writable.build();
        final String expected = sb.toString();
        final String actual = buffer.toString();
        assertThat(actual, is(expected));

        for (int i = 0; i < length; ++i) {
            assertThat(buffer.getByte(i), is((byte)b));
        }
    }

    @Test
    public void testBuild() throws Exception {
        final int capacity = 100;
        final WritableBuffer buffer = WritableBuffer.of(capacity);
        buffer.write("hello world");

        // build it and make sure that the new buffer is indeed 100% immutble
        // and that our old writable buffer cannot affect it in anyway...
        final Buffer immutable = buffer.build();
        assertThat(immutable.toString(), is("hello world"));
        assertThat(immutable.toReadableBuffer().readUntilWhiteSpace().toString(), is(("hello")));
        assertThat(immutable.slice(immutable.indexOfWhiteSpace()).toString(), is("hello"));
        assertThat(immutable.slice(immutable.indexOfWhiteSpace() + 1, immutable.capacity()).toString(), is("world"));

        ensureDoesntWork(buffer, b -> b.setByte(1, (byte)'a'));
        ensureDoesntWork(buffer, b -> b.setByte(0, (byte)'a'));
        ensureDoesntWork(buffer, b -> b.write("asdf"));
        ensureDoesntWork(buffer, b -> b.write(5));
        ensureDoesntWork(buffer, b -> b.write(5L));

        // you can't mess with the writer index either
        // going beyond the capacity (hence the +1 below) just to test
        // that boundary as well
        for (int i = 0; i < capacity + 1; ++i) {
            final int index = i;
            ensureDoesntWork(buffer, b -> b.setWriterIndex(index));
        }
    }

    @Test
    public void testSetUnsignedInt() {
        final WritableBuffer writable = WritableBuffer.of(new byte[100]);
        writable.setWriterIndex(4);
        writable.setUnsignedInt(0, 100);

        final ReadableBuffer buffer = writable.build().toReadableBuffer();
        assertThat(buffer.getUnsignedInt(0), is(100L));
        assertThat(buffer.readUnsignedInt(), is(100L));
    }

    @Test
    public void testSetInt() {
        final WritableBuffer writable = WritableBuffer.of(new byte[100]);
        writable.setInt(0, 100);
        writable.setUnsignedInt(12, 1234567);
        writable.setWriterIndex(12 + 4);

        final ReadableBuffer buffer = writable.build().toReadableBuffer();
        assertThat(buffer.getInt(0), is(100));
        assertThat(buffer.readInt(), is(100));
        assertThat(buffer.getInt(12), is(1234567));
    }

    @Test
    public void writeNumbers() {
        final WritableBuffer writable = WritableBuffer.of(100);
        writable.write(567);
        writable.write(789);
        writable.write(Integer.MAX_VALUE);
        writable.write(-1);
        writable.write(10L);
        writable.write(Long.MAX_VALUE);
        writable.write(Long.MAX_VALUE - 7); // -7 because you know, why not...
        writable.write(9999999999L);
        writable.write(99999999999L);
        writable.write(-1L);

        // read them all back...
        final ReadableBuffer buffer = writable.build().toReadableBuffer();
        assertThat(buffer.readInt(), is(567));
        assertThat(buffer.readInt(), is(789));
        assertThat(buffer.readInt(), is(Integer.MAX_VALUE));
        assertThat(buffer.readInt(), is(-1));
        assertThat(buffer.readLong(), is(10L));
        assertThat(buffer.readLong(), is(Long.MAX_VALUE));
        assertThat(buffer.readLong(), is(Long.MAX_VALUE - 7));
        assertThat(buffer.readLong(), is(9999999999L));
        assertThat(buffer.readLong(), is(99999999999L));
        assertThat(buffer.readLong(), is(-1L));
    }

    /**
     * Helper method to ensure that if the operation is performed, we blow up on an {@link IllegalStateException}
     *
     */
    private static void ensureDoesntWork(final WritableBuffer b, final Consumer<WritableBuffer> operation) {
        try {
            operation.accept(b);
            fail("Expected to blow up on a " + IllegalStateException.class.getName());
        } catch (final IllegalStateException e) {
            // expected
        }
    }

}
