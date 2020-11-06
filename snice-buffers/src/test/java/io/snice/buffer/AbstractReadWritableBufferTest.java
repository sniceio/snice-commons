package io.snice.buffer;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public abstract class AbstractReadWritableBufferTest extends AbstractReadableBufferTest {

    abstract ReadWriteBuffer createWritableBuffer(final int capacity);

    protected ReadWriteBuffer createWritableBuffer(final int capacity, final int offset, final int length) {
        return (ReadWriteBuffer)createBuffer(new byte[capacity], offset, length);
    }

    @Test
    public void testWriteFiveOctetLong() throws Exception {
        final WritableBuffer buffer = createWritableBuffer(100);
        buffer.writeFiveOctets(123);
        buffer.write(777);
        buffer.write("hello");
        final var b = buffer.build();
        assertThat(b.capacity(), is(5 + 4 + "hello".getBytes("UTF-8").length));
        assertThat(b.getLongFromFiveOctets(0), is(123L));
        assertThat(b.getInt(0 + 5), is(777));
    }

    @Test
    public void testCapacity() {
        assertThat(createWritableBuffer(10).capacity(), is(10));
        assertThat(createWritableBuffer(10, 2, 4).capacity(), is(4));
    }

    @Test
    public void testIsWritable() {
        final ReadWriteBuffer buffer = createWritableBuffer(10);
        assertThat(buffer.hasWritableBytes(), is(true));
        assertThat(buffer.hasReadableBytes(), is(false));
        assertThat(buffer.getWritableBytes(), is(10));
    }

    @Test
    public void testWriteIntAsString() {
        final ReadWriteBuffer buffer = createWritableBuffer(100);
        buffer.writeAsString(0);
        buffer.write((byte) ' ');
        buffer.writeAsString(10);
        buffer.write((byte) ' ');
        buffer.writeAsString(100);
        buffer.write((byte) ' ');
        buffer.writeAsString(9712);
        assertThat(buffer.toString(), is("0 10 100 9712"));
    }

    @Test
    public void testWriteLongAsString() {
        final ReadWriteBuffer buffer = createWritableBuffer(100);
        buffer.writeAsString(0L);
        buffer.write((byte) ' ');
        buffer.writeAsString(10L);
        buffer.write((byte) ' ');
        buffer.writeAsString(100L);
        buffer.write((byte) ' ');
        buffer.writeAsString(9712L);
        assertThat(buffer.toString(), is("0 10 100 9712"));
    }

    /**
     * If you of a new {@link WritableBuffer} with a certain capacity it is at that point
     * considered "empty" and as such, you should not be able to actually read until you have
     * written to the certain areas.
     */
    @Test
    public void testReadShouldNotWork() {
        final ReadWriteBuffer buffer = createWritableBuffer(100);
        testLotsOfWritesAndReads(buffer);

        // now, if we reset the writer index we should be able to do it
        // all over again...
        buffer.setWriterIndex(0);
        testLotsOfWritesAndReads(buffer);
    }

    private static void testLotsOfWritesAndReads(final ReadWriteBuffer buffer) {
        assertThat(buffer.hasReadableBytes(), is(false));
        assertThat(buffer.isEmpty(), is(true));

        ensureNoReadWorks(buffer);

        // let's write one byte and read it back...
        buffer.write((byte)'a');
        assertThat(buffer.readByte(), is((byte)'a'));

        // but now, after reading that byte, we should no longer be able to
        // read again...
        ensureNoReadWorks(buffer);

        // write some more...
        buffer.write((byte)'a');
        buffer.write((byte)'b');
        buffer.write((byte)'c');
        buffer.write((byte)'d');
        assertThat(buffer.readBytes(4).toString(), is("abcd"));

        ensureNoReadWorks(buffer);

        buffer.writeAsString(888);
        buffer.write(999);
        assertThat(buffer.readBytes(3).toString(), is("888"));
        assertThat(buffer.readInt(), is(999));
        ensureNoReadWorks(buffer);

    }

    private static void ensureNoReadWorks(final ReadWriteBuffer buffer) {
        ensureDoesntWork(buffer, b -> b.readByte());
        ensureDoesntWork(buffer, b -> b.readUnsignedByte());

        ensureDoesntWork(buffer, b -> b.readInt());
        ensureDoesntWork(buffer, b -> b.readUnsignedInt());

        ensureDoesntWork(buffer, b -> b.readBytes(1));
        ensureDoesntWork(buffer, b -> b.readBytes(10));
    }

    @Test
    public void testWriteThenRead() {
        final ReadWriteBuffer buffer = createWritableBuffer(100);
        buffer.write(512);
        buffer.write(123);

        assertThat(buffer.hasReadableBytes(), is(true));
        assertThat(buffer.getReadableBytes(), is(2 * 4));

        assertThat(buffer.readInt(), is(512));
        assertThat(buffer.readInt(), is(123));

        assertThat(buffer.hasReadableBytes(), is(false));
    }

    @Test
    public void testSetBits() {
        final ReadWriteBuffer buffer = (ReadWriteBuffer)createBuffer(new byte[100]);
        for (int i = 0; i < 10; ++i) {
            ensureBits(buffer, i);
        }

        // now that we have actually turned on all bits in the array
        // we should be able to read out
        assertThat(buffer.readInt(), is(-1)); // because all bits are set so signed int should be -2
        assertThat(buffer.readInt(), is(-1));
    }

    @Test
    public void testSetBits2() {
        // 97 binary is 1100001, let's set those bits
        // and sure we can read out 97
        final ReadWriteBuffer buffer = (ReadWriteBuffer)createBuffer(new byte[4]);
        buffer.setBit0(3, true);
        buffer.setBit5(3, true);
        buffer.setBit6(3, true);

        int value = buffer.getInt(0);
        assertThat(value, is(97));

        // then set the same pattern in the first byte
        buffer.setBit0(0, true);
        buffer.setBit5(0, true);
        buffer.setBit6(0, true);

        // when then if we just read that byte, it should also
        // be integer 97 when converted
        value = buffer.getByte(0);
        assertThat(value, is(97));

        // then let's make an integer our of only three bytes
        value = buffer.getIntFromThreeOctets(1);
        assertThat(value, is(97));

        // let's flip those bits in the first byte back
        // and then read byte after byte and only the
        // last one should be 97
        buffer.setBit0(0, false);
        buffer.setBit5(0, false);
        buffer.setBit6(0, false);
        assertThat(buffer.readByte(), is((byte)0));
        assertThat(buffer.readByte(), is((byte)0));
        assertThat(buffer.readByte(), is((byte)0));
        assertThat(buffer.readByte(), is((byte)97));

        // finally check so the bit pattern are "checkable"
        assertThat(buffer.getBit0(3), is(true));
        assertThat(buffer.getBit1(3), is(false));
        assertThat(buffer.getBit2(3), is(false));
        assertThat(buffer.getBit3(3), is(false));
        assertThat(buffer.getBit4(3), is(false));
        assertThat(buffer.getBit5(3), is(true));
        assertThat(buffer.getBit6(3), is(true));
        assertThat(buffer.getBit7(3), is(false));
    }

    private static void ensureBits(final ReadWriteBuffer buffer, final int index) {
        ensureAllBits(buffer, index, false);

        // just making sure that the short-hand versions
        // is actually working.
        buffer.setBit0(index, true);
        buffer.setBit1(index, true);
        buffer.setBit2(index, true);
        buffer.setBit3(index, true);
        buffer.setBit4(index, true);
        buffer.setBit5(index, true);
        buffer.setBit6(index, true);
        buffer.setBit7(index, true);

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

    private void ensureZeroOut(final char b, final int byteArrayLength, final int offset, final int length) {
        final ReadWriteBuffer buffer = (ReadWriteBuffer)createBuffer(new byte[byteArrayLength], offset, length);
        buffer.zeroOut((byte)b);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            sb.append(b);
        }
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
        final ReadWriteBuffer buffer = createWritableBuffer(capacity);
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
        ensureDoesntWork(buffer, b -> b.readByte());

        // you can't mess with the reader & writer index either
        // going beyond the capacity (hence the +1 below) just to test
        // that boundary as well
        for (int i = 0; i < capacity + 1; ++i) {
            final int index = i;
            ensureDoesntWork(buffer, b -> b.setReaderIndex(index));
            ensureDoesntWork(buffer, b -> b.setWriterIndex(index));
        }
    }

    @Test
    public void testThreeOctetInt() {
        ensureWriteThreeOctetInt(0);
        ensureWriteThreeOctetInt(7);
        ensureWriteThreeOctetInt(10);

        // if we have an int where any of the bits in the top byte is set, those will
        // effectively cutoff, leaving the lower 24 bits only. So, if we write the
        // max value, then when we should only have 24 "on" bits left.
        ensureWriteThreeOctetInt(Integer.MAX_VALUE, 0b111111111111111111111111);

        ensureNegativeNumbersThreeOctetIntsNotPossible(-1); //boundary
        ensureNegativeNumbersThreeOctetIntsNotPossible(-2);
        ensureNegativeNumbersThreeOctetIntsNotPossible(Integer.MIN_VALUE); //boundary
    }

    @Test
    public void testSetUnsignedInt() {
        final ReadWriteBuffer buffer = (ReadWriteBuffer)createBuffer(new byte[100]);
        buffer.setUnsignedInt(0, 100);
        assertThat(buffer.getUnsignedInt(0), is(100L));
        assertThat(buffer.readUnsignedInt(), is(100L));
    }

    private void ensureNegativeNumbersThreeOctetIntsNotPossible(final int value) {
        try {
            final ReadWriteBuffer buffer = createWritableBuffer(100);
            buffer.writeThreeOctets(value);
            fail("Expected to fail with an IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    private void ensureWriteThreeOctetInt(final int value, final int expected) {
        final ReadWriteBuffer buffer = createWritableBuffer(3);
        buffer.writeThreeOctets(value);
        assertThat(buffer.getIntFromThreeOctets(0), is(expected));
        assertThat(buffer.toReadableBuffer().readIntFromThreeOctets(), is(expected));
    }

    private void ensureWriteThreeOctetInt(final int value) {
        ensureWriteThreeOctetInt(value, value);
    }

    @Test
    public void testSetInt() {
        final ReadWriteBuffer buffer = (ReadWriteBuffer)createBuffer(new byte[100]);
        buffer.setInt(0, 100);
        assertThat(buffer.getInt(0), is(100));
        assertThat(buffer.readInt(), is(100));

        buffer.setUnsignedInt(12, 1234567);
        assertThat(buffer.getInt(12), is(1234567));
    }

    @Test
    public void writeNumbers() {
        final ReadWriteBuffer buffer = createWritableBuffer(100);
        buffer.write(567);
        buffer.write(789);
        buffer.write(Integer.MAX_VALUE);
        buffer.write(-1);

        assertThat(buffer.readInt(), is(567));
        assertThat(buffer.readInt(), is(789));
        assertThat(buffer.readInt(), is(Integer.MAX_VALUE));
        assertThat(buffer.readInt(), is(-1));

        buffer.write(10L);
        assertThat(buffer.readLong(), is(10L));

        buffer.write(Long.MAX_VALUE);
        assertThat(buffer.readLong(), is(Long.MAX_VALUE));

        // -7 because you know, why not...
        buffer.write(Long.MAX_VALUE - 7);
        assertThat(buffer.readLong(), is(Long.MAX_VALUE - 7));

        buffer.write(9999999999L);
        assertThat(buffer.readLong(), is(9999999999L));

        buffer.write(99999999999L);
        assertThat(buffer.readLong(), is(99999999999L));

        buffer.write(-1L);
        assertThat(buffer.readLong(), is(-1L));
    }

    /**
     * Helper method to ensure that if the operation is performed, we blow up on an {@link IllegalArgumentException}
     *
     */
    private static void ensureDoesntWork(final ReadWriteBuffer b, final Consumer<ReadWriteBuffer> operation) {
        try {
            operation.accept(b);
            fail("Expected to blow up on a " + IndexOutOfBoundsException.class.getName());
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
    }

}
