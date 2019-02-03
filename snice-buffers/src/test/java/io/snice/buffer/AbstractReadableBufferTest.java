package io.snice.buffer;

import io.snice.buffer.impl.EmptyBuffer;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Base class for all those tests concerning {@link ReadableBuffer}s.
 *
 */
public abstract class AbstractReadableBufferTest extends AbstractBufferTest {

    protected ReadableBuffer createReadableBuffer(final String s) {
        return (ReadableBuffer)createBuffer(s);
    }

    protected ReadableBuffer createReadableBuffer(final byte[] bytes) {
        return (ReadableBuffer)createBuffer(bytes);
    }

    @Test
    public void testReadUntilWhiteSpace() throws Exception {
        ensureReadUntilWhiteSpace("hello world", "hello", "world");
        ensureReadUntilWhiteSpace("hello\tworld", "hello", "world");

        // doesn't matter how many spaces there is, should all be consumed
        ensureReadUntilWhiteSpace("hello      world", "hello", "world");
        ensureReadUntilWhiteSpace("hello   \t\t   world", "hello", "world");

        // should only consume up until the first one...
        // but next read we'll get the final split
        final ReadableBuffer buf = ensureReadUntilWhiteSpace("hello      world again", "hello", "world again");
        assertThat(buf.readUntilWhiteSpace().toString(), is("world"));
        assertThat(buf.toString(), is("again"));
    }

    private ReadableBuffer ensureReadUntilWhiteSpace(final String line, final String expected1, final String expected2) {
        final ReadableBuffer buffer = createReadableBuffer(line);
        assertThat(buffer.readUntilWhiteSpace().toString(), is(expected1));
        assertThat(buffer.toString(), is(expected2));
        return buffer;
    }
    /**
     * Make sure that we can read the single-crlf sequence correctly.
     *
     * @throws Exception
     */
    @Test
    public void testReadUntilSingleCRLF() throws Exception {

        // no CRLF should lead to null...
        ReadableBuffer buffer = createReadableBuffer("hello");
        assertThat(buffer.readUntilSingleCRLF(), is((Buffer)null));

        // Only CR should also lead to null
        buffer = createReadableBuffer("hello\r");
        assertThat(buffer.readUntilSingleCRLF(), is((Buffer)null));

        // now we have two so that should yield a "hello" buffer back.
        buffer = createReadableBuffer("hello\r\n");
        assertThat(buffer.readUntilSingleCRLF().toString(), is("hello"));

        // One hello and then a null
        buffer = createReadableBuffer("hello\r\nworld");
        assertThat(buffer.readUntilSingleCRLF().toString(), is("hello"));
        assertThat(buffer.readUntilSingleCRLF(), is((Buffer)null));

        // One hello and then world
        buffer = createReadableBuffer("hello\r\nworld\r\n");
        assertThat(buffer.readUntilSingleCRLF().toString(), is("hello"));
        assertThat(buffer.readUntilSingleCRLF().toString(), is("world"));

        // One hello and then an empty buffer
        buffer = createReadableBuffer("hello\r\n\r\n");
        assertThat(buffer.readUntilSingleCRLF().toString(), is("hello"));
        assertThat(buffer.readUntilSingleCRLF().isEmpty(), is(true));
    }

    /**
     * Make sure that we can read the double-crlf sequence correctly.
     *
     * @throws Exception
     */
    @Test
    public void testReadUntilDoubleCRLF() throws Exception {
        ReadableBuffer buffer = createReadableBuffer("hello\r\n\r\nworld");
        Buffer hello = buffer.readUntilDoubleCRLF();
        assertThat(hello.toString(), is("hello"));
        assertThat(buffer.toString(), is("world"));

        // note that the first sequence is missing the last '\n'
        buffer = createReadableBuffer("hello\r\n\rworld\r\n\r\n");
        hello = buffer.readUntilDoubleCRLF();
        assertThat(hello.toString(), is("hello\r\n\rworld"));
        assertThat(buffer.toString(), is(""));

        // if we only have double crlf we will end up with two empty buffers...
        buffer = createReadableBuffer("\r\n\r\n");
        final Buffer empty = buffer.readUntilDoubleCRLF();
        assertThat(empty.isEmpty(), is(true));
        // isEmpty for stream backed buffers will never ever return anything but false for isEmpty()
        // however, has readable bytes do accomplish what we want for this test.
        assertThat(buffer.hasReadableBytes(), is(false));

        // of course, if there are two double-crlf sequences we should still
        // only read the first one... and reading the next double-crlf should
        // yield two buffers both with the word "world" in it...
        buffer = createReadableBuffer("hello\r\n\r\nworld\r\n\r\nworld");
        hello = buffer.readUntilDoubleCRLF();
        assertThat(hello.toString(), is("hello"));
        assertThat(buffer.toString(), is("world\r\n\r\nworld"));
        final Buffer world = buffer.readUntilDoubleCRLF();
        assertThat(world.toString(), is("world"));
        assertThat(buffer.toString(), is("world"));

    }

    @Override
    @Test
    public void testIndexOf() throws Exception {
        final ReadableBuffer buffer = createReadableBuffer("hello world ena goa grejor".getBytes());
        assertThat(buffer.indexOf(100, (byte) 'h'), is(0));
        assertThat(buffer.indexOf(100, (byte) 'e'), is(1));
        assertThat(buffer.indexOf(100, (byte) 'l'), is(2));
        assertThat(buffer.indexOf(100, (byte) 'o'), is(4));
        assertThat(buffer.indexOf(100, (byte) ' '), is(5));
        assertThat(buffer.indexOf(100, (byte) 'w'), is(6));
        assertThat(buffer.getByte(6), is((byte) 'w'));

        // indexOf should not affect the reader index so
        // everything should still be there.
        assertThat(buffer.toString(), is("hello world ena goa grejor"));

        // read some byte so now that we try and find 'w' it is actually
        // not there anymore...
        assertThat(buffer.readBytes(11).toString(), is("hello world"));
        assertThat(buffer.indexOf((byte) 'w'), is(-1));
        // however, we still have an 'o' around in the visible area
        // Remember that the index is in relation to the entire buffer
        // and not where the reader index is.
        assertThat(buffer.indexOf((byte) 'o'), is(17));
    }

    @Test
    public void testSliceEmptyBuffer() throws Exception {
        final Buffer buffer = EmptyBuffer.EMPTY;
        assertEmptyBuffer(buffer.slice());
        assertThat(buffer.slice().toReadableBuffer().hasReadableBytes(), is(false));

        final ReadableBuffer buf = Buffers.wrap("a little harder").toReadableBuffer();
        buf.readBytes(buf.capacity());
        assertEmptyBuffer(buf.slice());
        assertThat(buf.slice().toReadableBuffer().hasReadableBytes(), is(false));
    }

    @Test
    public void testReadUntil2() throws Exception {
        ReadableBuffer buffer = createReadableBuffer("this is a somewhat long string".getBytes());
        Buffer buf = buffer.readUntil(100, (byte) 'a', (byte) 'o');
        assertThat(buf.toString(), is("this is "));

        buffer = createReadableBuffer("this is a somewhat long string".getBytes());
        buf = buffer.readUntil(100, (byte) 'o', (byte) 'a');
        assertThat(buf.toString(), is("this is "));

        buffer = createReadableBuffer("this is a somewhat long string".getBytes());
        buf = buffer.readUntil(100, (byte) 'k', (byte) 'c', (byte) 'o');
        assertThat(buf.toString(), is("this is a s"));

        buffer = createReadableBuffer("this is a somewhat long string".getBytes());
        buf = buffer.readUntil(100, (byte) 'k', (byte) 'c', (byte) 'g');
        assertThat(buf.toString(), is("this is a somewhat lon"));
        assertThat(buffer.toString(), is(" string"));

        // but now we really only want to read a maximum 10 bytes
        buffer = createReadableBuffer("this is a somewhat long string".getBytes());
        try {
            buf = buffer.readUntil(10, (byte) 'k', (byte) 'c', (byte) 'g');
            fail("Expected a ByteNotFoundException");
        } catch (final ByteNotFoundException e) {
            // the buffer should have been left untouched.
            assertThat(buffer.toString(), is("this is a somewhat long string"));
        }

        // also make sure that after slicing the read until stuff
        // works as expected.
        buffer = createReadableBuffer("this is a somewhat long string".getBytes());
        buffer.readBytes(5);
        buf = buffer.readUntil((byte) 'i');
        assertThat(buf.toString(), is(""));

        buf = buffer.readUntil((byte) 'e');
        assertThat(buf.toString(), is("s a som"));

        // slice things up and make sure that our readUntil works on slices as well
        final ReadableBuffer slice = buffer.slice(buffer.getReaderIndex() + 5).toReadableBuffer();
        assertThat(slice.toString(), is("what "));
        buf = slice.readUntil((byte) 'a');
        assertThat(buf.toString(), is("wh"));
        assertThat(slice.toString(), is("t "));

        buf = slice.readUntil((byte) ' ');
        assertThat(buf.toString(), is("t"));
        assertThat(slice.toString(), is(""));
        assertThat(slice.hasReadableBytes(), is(false));

        // head back to the buffer which should have been unaffected by our
        // work on the slice. we should have "what long string" left
        buf = buffer.readUntil((byte) 'o');
        assertThat(buf.toString(), is("what l"));
        assertThat(buffer.readUntil((byte) 'n').toString(), is(""));
        assertThat(buffer.readUntil((byte) 'i').toString(), is("g str"));
    }

    @Test
    public void testReadUntil() throws Exception {
        ReadableBuffer buffer = createReadableBuffer("hello world".getBytes());
        final Buffer hello = buffer.readUntil((byte) ' ');
        assertThat(hello.toString(), is("hello"));

        // read the next 5 bytes and that should give us the world part
        final Buffer world = buffer.readBytes(5);
        assertThat(world.toString(), is("world"));

        // test to read until 'h' is found, which should yeild nothing
        // since the character that is found is essentially thrown away
        buffer = createReadableBuffer("hello world again".getBytes());
        final Buffer empty = buffer.readUntil((byte) 'h');
        assertThat(empty.toString(), is(""));

        // then read until 'a' is found
        final Buffer more = buffer.readUntil((byte) 'a');
        assertThat(more.toString(), is("ello world "));

        final Buffer gai = buffer.readUntil((byte) 'n');
        assertThat(gai.toString(), is("gai"));

        // and now there should be nothing left
        try {
            buffer.readByte();
            fail("Expected a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

        // no 'u' in the string, exception expected
        buffer = createReadableBuffer("nothing will match".getBytes());
        try {
            buffer.readUntil((byte) 'u');
            fail("Expected a ByteNotFoundException");
        } catch (final ByteNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testRead() throws Exception {
        final ReadableBuffer buffer = createReadableBuffer(RawData.rawEthernetFrame);

        // read 100 bytes at a time
        for (int i = 0; i < 5; ++i) {
            final Buffer hundred = buffer.readBytes(100);
            assertThat(hundred.capacity(), is(100));
            for (int k = 0; k < 100; ++k) {
                assertThat(hundred.getByte(k), CoreMatchers.is(RawData.rawEthernetFrame[k + i * 100]));
            }
        }

        // there are 547 bytes in the rawEthernetFrame so there should be 47
        // left
        final Buffer theRest = buffer.readBytes(47);
        assertThat(theRest.capacity(), is(47));
        for (int k = 0; k < 47; ++k) {
            assertThat(theRest.getByte(k), CoreMatchers.is(RawData.rawEthernetFrame[k + 500]));
        }
    }

    /**
     * Make sure we can read a single line that doesn't contain any new line
     * characters
     *
     * @throws Exception
     */
    @Test
    public void testReadLineSingleLineNoCRLF() throws Exception {
        // final String s = "just a regular line, nothing special";
        final String s = "hello";
        final ReadableBuffer buffer = createReadableBuffer(s.getBytes());
        assertThat(buffer.readLine().toString(), is(s));

        // no more lines to read
        assertThat(buffer.readLine(), is((Buffer) null));
    }

    /**
     * Make sure that we can read line by line
     *
     * @throws Exception
     */
    @Test
    public void testReadLine() throws Exception {
        int count = 0;
        final ReadableBuffer buffer = RawData.sipBuffer.toReadableBuffer();
        while (buffer.readLine() != null) {
            ++count;
        }

        // this sip buffer contains 19 lines
        assertThat(count, is(19));
    }

    /**
     * Contains two lines separated by a single '\n'
     *
     * @throws Exception
     */
    @Test
    public void testReadLines() throws Exception {
        final String line1 = "this is line 1";
        final String line2 = "and this is line 2";
        ReadableBuffer buffer = createReadableBuffer((line1 + "\n" + line2).getBytes());

        // the first readLine should be equal to line 1 and
        // the '\n' should have been stripped off
        assertThat(buffer.readLine().toString(), is(line1));

        // and then of course check the second line
        assertThat(buffer.readLine().toString(), is(line2));

        // and then there should be no more
        assertThat(buffer.readLine(), is((Buffer) null));

        // now add only a CR
        buffer = createReadableBuffer((line1 + "\r" + line2).getBytes());
        assertThat(buffer.readLine().toString(), is(line1));
        assertThat(buffer.readLine().toString(), is(line2));
        assertThat(buffer.readLine(), is((Buffer) null));

        // now add CR + LF
        buffer = createReadableBuffer((line1 + "\r\n" + line2).getBytes());
        assertThat(buffer.readLine().toString(), is(line1));
        assertThat(buffer.readLine().toString(), is(line2));
        assertThat(buffer.readLine(), is((Buffer) null));

        // this one is a little trickier. Add LF + CR + LF, which should
        // result in line1, followed by empty line, followed by line 2
        buffer = createReadableBuffer((line1 + "\n\r\n" + line2).getBytes());
        assertThat(buffer.readLine().toString(), is(line1));
        assertThat(buffer.readLine().toString(), is(new String(new byte[0])));
        assertThat(buffer.readLine().toString(), is(line2));
        assertThat(buffer.readLine(), is((Buffer) null));
    }

    @Test
    public void testReadBytes() throws IOException {
        final ReadableBuffer buffer = createReadableBuffer(allocateByteArray(100));
        final ReadableBuffer b1 = buffer.readBytes(10).toReadableBuffer();

        // both should have 90 bytes left to read
        // assertThat(buffer.readableBytes(), is(90));
        assertThat(buffer.readByte(), is((byte) 0x0a));
        assertThat(buffer.readByte(), is((byte) 0x0b));
        assertThat(buffer.readByte(), is((byte) 0x0c));
        assertThat(buffer.readByte(), is((byte) 0x0d));

        // the next buffer that will be read is the one at index 10

        // even though we read some bytes off of the main
        // buffer, we should still be able to directly access
        // the bytes
        assertThat(buffer.getByte(0), is((byte) 0x00));
        assertThat(buffer.getByte(5), is((byte) 0x05));
        assertThat(buffer.getByte(16), is((byte) 0x10));
        assertThat(buffer.getByte(32), is((byte) 0x20));

        // and this one should be 10 of course
        assertThat(b1.getReadableBytes(), is(10));
        assertThat(b1.capacity(), is(10));

        assertThat(b1.getByte(0), is((byte) 0x00));
        assertThat(b1.getByte(1), is((byte) 0x01));
        assertThat(b1.getByte(2), is((byte) 0x02));
        assertThat(b1.getByte(3), is((byte) 0x03));
        assertThat(b1.getByte(4), is((byte) 0x04));
        assertThat(b1.getByte(5), is((byte) 0x05));
        assertThat(b1.getByte(6), is((byte) 0x06));
        assertThat(b1.getByte(7), is((byte) 0x07));
        assertThat(b1.getByte(8), is((byte) 0x08));
        assertThat(b1.getByte(9), is((byte) 0x09));

        // the getByte doesn't move the reader index so we should be able
        // to read through all the above again

        assertThat(b1.readByte(), is((byte) 0x00));
        assertThat(b1.readByte(), is((byte) 0x01));
        assertThat(b1.readByte(), is((byte) 0x02));
        assertThat(b1.readByte(), is((byte) 0x03));
        assertThat(b1.readByte(), is((byte) 0x04));
        assertThat(b1.readByte(), is((byte) 0x05));
        assertThat(b1.readByte(), is((byte) 0x06));
        assertThat(b1.readByte(), is((byte) 0x07));
        assertThat(b1.readByte(), is((byte) 0x08));
        assertThat(b1.readByte(), is((byte) 0x09));
    }

    @Test
    public void testSlice() throws Exception {
        final ReadableBuffer buffer = createReadableBuffer(allocateByteArray(100));
        final ReadableBuffer b1 = buffer.slice(50, 70).toReadableBuffer();
        assertThat(b1.capacity(), is(20));
        assertThat(b1.readByte(), is((byte) 50));
        assertThat(b1.getByte(0), is((byte) 50));

        assertThat(b1.readByte(), is((byte) 51));
        assertThat(b1.getByte(1), is((byte) 51));

        assertThat(b1.getByte(19), is((byte) 69));
        try {
            b1.getByte(20);
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
        try {
            b1.getByte(21);
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

        // sliceing it again is based on the seconds
        // buffer's point-of-view
        final ReadableBuffer b2 = b1.slice(b1.capacity()).toReadableBuffer();

        // remember, we already read two bytes above so we should
        // be at 52
        assertThat(b2.readByte(), is((byte) 52));
        assertThat(b2.readByte(), is((byte) 53));

        // since the b1 buffer already have 2 of its bytes
        // read, there are 18 bytes left
        assertThat(b2.capacity(), is(18));
        assertThat(b2.getByte(17), is((byte) 69));

        try {
            // the capacity is 18, which means that the last
            // index is 17, which means that trying to access
            // 18 should yield in an exception
            b2.getByte(18);
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

        // grab the entire b2 buffer
        final ReadableBuffer b3 = b2.slice(0, 18).toReadableBuffer();
        assertThat(b3.capacity(), is(18));
        assertThat(b3.readByte(), is((byte) 52));
        assertThat(b3.getByte(b3.capacity() - 1), is((byte) 69));
    }

    /**
     * Test to make sure that it is possible to mark the reader index, continue
     * reading and then reset the buffer and as such, continue from where we
     * last called marked...
     *
     * @throws Exception
     */
    @Test
    public void testResetAndMarkReaderIndex() throws Exception {
        final ReadableBuffer buffer = createReadableBuffer(allocateByteArray(100));

        // read and "throw away" 10 bytes
        buffer.readBytes(10);

        // make sure that the next byte that is being read is 10
        assertThat(buffer.readByte(), is((byte) 0x0A));

        // reset the buffer and make sure that now, the next
        // byte being read should be zero again
        buffer.resetReaderIndex();
        assertThat(buffer.readByte(), is((byte) 0x00));
        assertThat(buffer.readByte(), is((byte) 0x01));

        // mark it and read a head, then check that we are in
        // the correct spot, reset and check that we are back again
        buffer.markReaderIndex();
        assertThat(buffer.readByte(), is((byte) 0x02));
        buffer.readBytes(10);
        assertThat(buffer.readByte(), is((byte) 0x0D));
        buffer.resetReaderIndex();
        assertThat(buffer.readByte(), is((byte) 0x02));

        // make sure that it works for slices as well
        final ReadableBuffer slice = buffer.slice(30, 50).toReadableBuffer();
        assertThat(slice.readByte(), is((byte) 30));
        assertThat(slice.readByte(), is((byte) 31));
        slice.resetReaderIndex();
        assertThat(slice.readByte(), is((byte) 30));
        slice.readBytes(5);
        slice.markReaderIndex();
        assertThat(slice.readByte(), is((byte) 36));
        assertThat(slice.readByte(), is((byte) 37));
        assertThat(slice.readByte(), is((byte) 38));
        slice.resetReaderIndex();
        assertThat(slice.readByte(), is((byte) 36));
    }

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

    /**
     * Make sure that slicing multiple times works since that is a very common
     * operation.
     *
     * @throws Exception
     */
    @Test
    public void testDoubleSlice() throws Exception {
        final String str = "This is a fairly long sentance and all together this should be 71 bytes";
        final ReadableBuffer buffer = createReadableBuffer(str);
        buffer.readByte();
        final ReadableBuffer b1 = buffer.slice().toReadableBuffer();
        assertThat(b1.toString(), is(str.substring(1)));

        final Buffer b2 = b1.readBytes(20);
        assertThat(b2.toString(), is(str.subSequence(1, 21)));

        // now, slice the already sliced b1_1. Since we haven't ready anything
        // from b1_1Slice just yet we should end up with the exact same thing.
        final Buffer b2Slice = b2.slice();
        assertThat(b2Slice.toString(), is(str.subSequence(1, 21)));

        final Buffer again = b2Slice.slice(4, 10);
        assertThat(again.toString(), is("is a f"));
    }


    /**
     * Test the read until on a sliced buffer.
     *
     * @throws Exception
     */
    @Test
    public void testReadUntilFromSlicedBuffer() throws Exception {
        final ReadableBuffer original = createReadableBuffer("hello world this is going to be a longer one".getBytes());
        final ReadableBuffer buffer = original.slice(6, original.capacity()).toReadableBuffer();
        final Buffer world = buffer.readUntil((byte) ' ');
        assertThat(world.toString(), is("world"));

        final Buffer longer = buffer.readUntil((byte) 'a');
        assertThat(longer.toString(), is("this is going to be "));

        final Buffer theRest = buffer.readLine();
        assertThat(theRest.toString(), is(" longer one"));
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

    @Test
    public void testWriteToOutputStreamAfterRead() throws Exception {
        ensureWriteToOutputStream("one two three", 1);
        ensureWriteToOutputStream("one two three", 3);
        ensureWriteToOutputStream("a", 1); // off by 1 bugs...
    }

    @Test
    public void testStripEOLSpecialCase() throws Exception {
        ensureStripEOLSpecialCase("\r", 1);
        ensureStripEOLSpecialCase("\n", 1);
        ensureStripEOLSpecialCase("\r\n", 2);
    }

    @Test
    public void testStripEOLReadable() throws Exception {
        ensureStripEOL("no CRLF in this one", false, false, 5);
        ensureStripEOL("Just a CR here...\r", true, false, 3);
        ensureStripEOL("Just a LF in this one...\n", false, true, 5);
        ensureStripEOL("Alright, got both!\r\n", true, true, 5);

        ensureStripEOL("Note, this is not readUntilCRLF, this \r\n is strip so we should have the entire thing left", false, false);

        // and ensure we don't have a +1 off bug resulting, usually, in blowing up
        // in a spectacular way...
        ensureStripEOL("", false, false);
        ensureStripEOL("a", false, false);
        ensureStripEOL("a\r", true, false);
        ensureStripEOL("a\n", false, true);
        ensureStripEOL("a\r\n", true, true);

        ensureStripEOL("ab", false, false);
        ensureStripEOL("ab\r", true, false);
        ensureStripEOL("ab\n", false, true);
        ensureStripEOL("ab\r\n", true, true);
    }

    @Test
    public void testEqualsHashCodeWithReadable() throws Exception {
        final ReadableBuffer b1 = createReadableBuffer("hello world");
        final ReadableBuffer b2 = createReadableBuffer("hello world");
        assertThat(b1, is(b2));
        assertThat(b1.hashCode(), is(b2.hashCode()));

        final ReadableBuffer b3 = createBuffer("hello not world").toReadableBuffer();
        assertThat(b1, is(not(b3)));
        assertThat(b1.hashCode(), is(not(b3.hashCode())));
        assertThat(b2, is(not(b3)));

        // because the way we do equals right now when one of the
        // buffers has read a head they are no longer equal.
        // One motivation is because the bytes that have been
        // consumed actually can be discarded...
        b2.readByte();
        assertThat(b1, is(not(b2)));
        assertThat(b1.hashCode(), is(not(b2.hashCode())));

        // because of this, if we now read a head in both
        // b1 and b3 and only leave the "world" portion left
        // then all of a sudden b1 and b3 actually are equal
        b1.readBytes(6);
        b3.readBytes(10);
        assertThat(b1, is(b3));
        assertThat(b1.hashCode(), is(b3.hashCode()));

        final ReadableBuffer a1 = createReadableBuffer("123 world");
        final ReadableBuffer a2 = createReadableBuffer("456 world");
        assertThat(a1, not(a2));
        assertThat(a1.hashCode(), not(a2.hashCode()));

        final Buffer a1_1 = a1.readBytes(3);
        final Buffer a2_1 = a2.readBytes(3);
        assertThat(a1_1, not(a2_1));
        assertThat(a1_1.hashCode(), not(a2_1.hashCode()));

        // now they should be equal
        final Buffer a1_2 = a1.slice();
        final Buffer a2_2 = a2.slice();
        assertThat(a1_2, is(a2_2));
        assertThat(a1_2.hashCode(), is(a2_2.hashCode()));

        final Buffer a1_3 = a1.readBytes(5);
        final Buffer a2_3 = a2.readBytes(5);
        assertThat(a1_3, is(a2_3));
        assertThat(a1_3.hashCode(), is(a2_3.hashCode()));

        final ReadableBuffer from = createReadableBuffer("From");
        final ReadableBuffer fromHeader = createReadableBuffer("some arbitrary crap first then From and then some more shit");
        fromHeader.readBytes(31);
        final Buffer fromAgain = fromHeader.readBytes(4);
        assertThat(fromAgain.toString(), is("From"));
        assertThat(fromAgain, is(from));

        // TODO: bug - the decision to implement the ReadableBuffer through delegation instead
        // has come back to bit us...
        // assertThat(from, is(fromAgain));
    }

    /**
     * Bug found when parsing a SIP URI. The port wasn't picked up because we
     * were doing parseToInt without regards to the offset within the buffer
     *
     * @throws Exception
     */
    @Test
    public void testParseAsIntBugWhenParsingSipURI() throws Exception {
        final ReadableBuffer b = Buffers.wrap("sip:alice@example.com:5099").toReadableBuffer();
        assertThat(b.readBytes(4).toString(), is("sip:"));
        assertThat(b.readBytes(5).toString(), is("alice"));
        assertThat(b.readByte(), is((byte) '@'));
        final ReadableBuffer hostPort = b.slice().toReadableBuffer();
        assertThat(hostPort.toString(), is("example.com:5099"));
        assertThat(hostPort.readBytes(11).toString(), is("example.com"));
        final ReadableBuffer host = hostPort.slice(0, 11).toReadableBuffer();
        assertThat(host.toString(), is("example.com"));
        assertThat(hostPort.readByte(), is((byte) ':'));
        assertThat(hostPort.parseToInt(), is(5099));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testWrapArraySpecifyingTheWindows() throws Exception {
        final ReadableBuffer buffer = Buffers.wrap("hello world".getBytes(), 3, 9).toReadableBuffer();
        assertThat(buffer.toString(), is("lo wor"));
        assertThat(buffer.getByte(0), is((byte) 'l'));
        assertThat(buffer.getByte(1), is((byte) 'o'));

        assertThat(buffer.readByte(), is((byte) 'l'));
        assertThat(buffer.readByte(), is((byte) 'o'));

        assertThat(buffer.getByte(0), is((byte) 'l'));
        assertThat(buffer.getByte(1), is((byte) 'o'));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testClone() throws Exception {
        final Buffer buffer = Buffers.wrap(allocateByteArray(100));
        final Buffer clone = buffer.clone();
        assertBuffers(buffer, clone);

        // make sure that cloning slices are also
        // correct
        final Buffer slice = clone.slice(30, 40);
        assertThat(slice.getByte(0), is((byte) 30));
        final Buffer sliceClone = slice.clone();
        assertBuffers(sliceClone, slice);
    }

    /**
     * For a readable buffer, when to we do {@link Buffer#toBuffer()} any bytes that have been
     * consumed by reading past them should not be included in the new of the {@link Buffer} that
     * is returned.
     *
     * @throws Exception
     */
    @Test
    public void testToBuffer() throws Exception {
        final ReadableBuffer buffer = createReadableBuffer("hello world");
        buffer.readUntilWhiteSpace();
        assertThat(buffer.toBuffer().toString(), is("world"));
    }

    private static void assertBuffers(final Buffer b1, final Buffer b2) throws Exception {
        // make sure they are the exact same size and have
        // the same content etc
        assertThat(b1.capacity(), is(b2.capacity()));
        for (int i = 0; i < b1.capacity(); ++i) {
            assertThat(b1.getByte(i), is(b2.getByte(i)));
        }
    }

    /**
     * Special case where we expect buffer that's left to be empty. Was easier to separate out
     * this test case since otherwise the actual unit test became too complicated and too much
     * code in it. don't want to debug the actual unit test itself!
     *
     * @param line
     * @param read
     */
    protected void ensureStripEOLSpecialCase(final String line, final int read) {
        final ReadableBuffer buffer = createReadableBuffer(line);
        buffer.readBytes(read);

        final Buffer stripped = buffer.stripEOL();

        assertThat(stripped.isEmpty(), is(true));
        assertThat(stripped.toString(), is(""));
    }

    protected void ensureStripEOL(final String line, final boolean cr, final boolean lf, final int read) {
        final ReadableBuffer buffer = createReadableBuffer(line);
        buffer.readBytes(read);

        final Buffer stripped = buffer.stripEOL();

        if (cr && lf) {
            assertThat(buffer.getReadableBytes(), is(stripped.capacity() + 2));
            assertThat(stripped.toString(), is(line.substring(read, line.length() - 2)));
        } else if (cr || lf) {
            assertThat(buffer.getReadableBytes(), is(stripped.capacity() + 1));
            assertThat(stripped.toString(), is(line.substring(read, line.length() - 1)));
        } else {
            // neither so should be the exact same.
            assertThat(buffer, is(stripped));
        }
    }

    private void ensureWriteToOutputStream(final String data, final int readNoOfBytes) throws IOException {
        // first make sure that writing the entire data does indeed mean that all that
        // data is written and that is what we get back as well...
        final ReadableBuffer b1 = (ReadableBuffer)createBuffer(data);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        b1.writeTo(out);
        assertThat(b1.toString(), is(out.toString()));
        assertThat(b1.toString(), is(data));

        // now to the real test. If you then read from the buffer, the write to OutputStream
        // should then NOT include the data that was read.
        if (readNoOfBytes > 0) {
            final Buffer slice = b1.readBytes(readNoOfBytes);
            final String expected = data.substring(0, readNoOfBytes);
            assertThat(slice.toString(), is(expected));

            final ByteArrayOutputStream outAgain = new ByteArrayOutputStream();
            b1.writeTo(outAgain);

            final String whatsLeftExpected = data.substring(readNoOfBytes, data.length());
            assertThat(b1.toString(), is(outAgain.toString()));
            assertThat(b1.toString(), is(whatsLeftExpected));

            // also then try a slice of the slice... assuming the slice has anything left to read from it.
            final ReadableBuffer readableSlice = slice.toReadableBuffer();
            if (readableSlice.hasReadableBytes()) {
                readableSlice.readByte();
                final ByteArrayOutputStream outAgain2 = new ByteArrayOutputStream();
                readableSlice.writeTo(outAgain2);

                // remember, we are operating on the first slice, which is the
                // very first part of the original data...
                final String whatsLeft2 = expected.substring(1);
                assertThat(readableSlice.toString(), is(outAgain2.toString()));
                assertThat(readableSlice.toString(), is(whatsLeft2));
            }

        }

    }

    /**
     * Simple helper method to allocate an array of bytes. Each byte in the
     * array will just be +1 from the previous, making it easy to test the
     * various operations, such as slice, getByte etc (since you know exactly
     * what to expect at each index)
     *
     * @param length
     * @return
     */
    protected static byte[] allocateByteArray(final int length) {
        final byte[] array = new byte[length];
        for (int i = 0; i < length; ++i) {
            array[i] = (byte) i;
        }

        return array;
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

    /**
     * Convenience method for making sure that the buffer is indeed empty
     *
     * @param buffer
     */
    private static void assertEmptyBuffer(final Buffer buffer) {
        assertThat(buffer.capacity(), is(0));
        assertThat(buffer.isEmpty(), is(true));
    }
}
