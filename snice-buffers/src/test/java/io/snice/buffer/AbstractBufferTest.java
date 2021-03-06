package io.snice.buffer;

import io.snice.buffer.impl.EmptyBuffer;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Obviously, no matter what kind of underlying buffer is used, all the tests
 * should pass. Hence, we are putting all tests in this class and then each
 * sub-class just needs to override the createBuffer-factory method
 *
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractBufferTest {

    /**
     * Factory method for creating the type of buffer that you need to test
     *
     * @param array
     * @return
     */
    public abstract Buffer createBuffer(final byte[] array);

    public abstract Buffer createBuffer(final byte[] array, int offset, int length);

    public Buffer createBuffer(final String s) {
        return createBuffer(s.getBytes());
    }

    @Test
    public void testGetContent() throws Exception {
        final var buffer = createBuffer(generateContent(100));
        final var content = buffer.getContent();
        ensureContent(content, 0);

        // slice it out...
        ensureContent(buffer.slice(10, 100).getContent(), 10);
        ensureContent(buffer.slice(10, 20).getContent(), 10);

        // boundary testing...
        ensureContent(buffer.slice(10, 10).getContent(), 10);
        ensureContent(buffer.slice(99, 100).getContent(), 99);
        ensureContent(buffer.slice(100, 100).getContent(), 100);

        // slice of slice...
        final var slice = buffer.slice(10, 50);

        // remember we already sliced 10 out so the slice of 10 of 10 means we should be
        // at 20...
        ensureContent(slice.slice(10, 30).getContent(), 20);

        // again, the slice started at 10 so... and we sliced out
        // 10 to 50 which means that there are only 40 bytes in the slice so
        // ask for 40...
        ensureContent(slice.slice(0, 40).getContent(), 10);

        ensureContent(slice.slice(1, 40).getContent(), 11);
        ensureContent(slice.slice(1, 30).getContent(), 11);
    }

    /**
     * Ensure that the given byte-array contains the bytes from (inclusive) start
     * to the end and it is assumed that the content is +1 as far as values
     * goes for every new byte up the index.
     */
    private void ensureContent(final byte[] content, int start) {
        for (int i = 0; i < content.length; ++i) {
            assertThat(content[i], is((byte)(start + i)));
        }
    }


    private final byte[] generateContent(int length) {
        final byte[] content = new byte[length];
        for (int i = 0; i < length; ++i) {
            content[i] = (byte)i;
        }
        return content;
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
    public void testConvertToIpv4String() {
        final Buffer b = Buffers.wrap((byte)0x00, (byte)0x01, (byte)0xac, (byte)0x16, (byte)0x12, (byte)0x78, (byte)0xaa);
        assertThat(b.toIPv4String(2), is("172.22.18.120"));
    }

    @Test
    public void testIsNullOrEmpty() {
        assertThat(Buffers.isNullOrEmpty(null), is(true));
        assertThat(Buffers.isNullOrEmpty(createBuffer("")), is(true));
        assertThat(Buffers.isNullOrEmpty(EmptyBuffer.EMPTY), is(true));

        assertThat(Buffers.isNullOrEmpty(createBuffer(" ")), is(false));
    }

    @Test
    public void testIsNotNullOrEmpty() {
        assertThat(Buffers.isNotNullOrEmpty(null), is(false));
        assertThat(Buffers.isNotNullOrEmpty(createBuffer("")), is(false));
        assertThat(Buffers.isNotNullOrEmpty(EmptyBuffer.EMPTY), is(false));

        assertThat(Buffers.isNotNullOrEmpty(createBuffer(" ")), is(true));
    }

    @Test
    public void testCreateWithOffsetAndLength() {
        ensureBuffer(0, 3, "hej", 'h', 'e', 'j');
        ensureBuffer(0, 3, "hej", 'h', 'e', 'j', null, null, null, null);
        ensureBuffer(2, 3, "hej", null, null, 'h', 'e', 'j', null, null, null, null);

        // empty buffer but should be legal
        ensureBuffer(2, 0, "", null, null, 'h', 'e', 'j', null, null, null, null);

        ensureBuffer(2, 1, "h", null, null, 'h', 'e', 'j', null, null, null, null);
    }

    private void ensureBuffer(final int offset, final int length, final String expected, final Character... content) {
        final byte[] buf = new byte[content.length];
        for (int i = 0; i < content.length; ++i) {
            buf[i] = content[i] != null ? (byte)content[i].charValue() : buf[i];
        }

        final Buffer buffer = createBuffer(buf, offset, length);
        if (expected.isEmpty()) {
            assertThat("This should be an empty buffer", buffer.isEmpty(), is(true));
        }
        assertThat(buffer.toString(), is(expected));
    }

    @Test
    public void testEndsWithByte() {
        ensureEndsWith(createBuffer("hello world grejor"), (byte)'r');
        ensureEndsWith(createBuffer("hello world grejor"), (byte)'o', (byte)'r');
        ensureEndsWith(createBuffer("hello world grejor"), (byte)'j', (byte)'o', (byte)'r');
        ensureEndsWith(createBuffer("hello world grejor"), (byte)'e', (byte)'j', (byte)'o', (byte)'r');

        assertThat(createBuffer("hello world grejor\r\n").endsWithCRLF(), is(true));
        assertThat(createBuffer("hello world grejor\r\n nisse").endsWithCRLF(), is(false));

        assertThat(createBuffer("\r\n").endsWithCRLF(), is(true));
        assertThat(createBuffer("\r\n\r\n").endsWithDoubleCRLF(), is(true));
    }

    @Test
    public void testEndsWith() {
        ensureEndsWith(createBuffer("hello world ena goa grejor"), "grejor");
        ensureEndsWith(createBuffer("hello ena goa "), "goa ");
        ensureEndsWith(createBuffer("hello ena goa "), " ");
        ensureEndsWith(createBuffer("hello ena goa "), "a ");
        ensureEndsWith(createBuffer("hello ena goa "), "oa ");
        ensureEndsWith(createBuffer("hello ena goa "), "goa ");
        ensureEndsWith(createBuffer("hello ena goa "), "hello ena goa ");

        ensureEndsWith(createBuffer("hello ena goa\r\n"), "goa\r\n");
        ensureEndsWith(createBuffer("hello \r\nERROR\r\n"), "\r\nERROR\r\n");
        ensureEndsWith(createBuffer("hello \r\nOK\r\n"), "\r\nOK\r\n");

        // no match...
        ensureNotEndsWith(createBuffer("nope doesnt fit"), "nisse");
        ensureNotEndsWith(createBuffer("nope"), "nisse and this is way too long");

        // zero length is considered bad...
        ensureEndsWithFails(createBuffer("nope"), "".getBytes());
        ensureEndsWithFails(createBuffer("nope"), null);
        ensureEndsWithFails(createBuffer("nope"), new byte[0]);

        // make sure that when we slice things out that the end is still maintained...
        final Buffer orig = createBuffer("hello world ena goa grejor");
        final Buffer slice01 = orig.slice(5, 15);
        assertThat(slice01.toString(), is(" world ena"));
        ensureEndsWith(slice01, "ena");

        final Buffer slice01b = slice01.slice(1, 6);
        assertThat(slice01b.toString(), is("world"));
        ensureEndsWith(slice01b, "world");
        ensureEndsWith(slice01b, "d");
    }

    @Test
    public void testEndsWithEOL() {
        assertThat(createBuffer("hello yes\n").endsWithEOL(), is(true));
        assertThat(createBuffer("hello yes\r").endsWithEOL(), is(true));
        assertThat(createBuffer("hello yes\r\n").endsWithEOL(), is(true));
        assertThat(createBuffer("hello yes\r\n\n").endsWithEOL(), is(true));
        assertThat(createBuffer("hello yes\r\n\n\r").endsWithEOL(), is(true));

        assertThat(createBuffer("\n").endsWithEOL(), is(true));
        assertThat(createBuffer("\r").endsWithEOL(), is(true));
        assertThat(createBuffer("\r\n").endsWithEOL(), is(true));
    }

    @Test
    public void testStartsWith() {
        assertThat(createBuffer("hello world").startsWith(createBuffer("hello")), is(true));
        assertThat(createBuffer("hello world").startsWith(createBuffer("hello world this is too long")), is(false));

        // testing for 1-off errors...
        assertThat(createBuffer("hello world").startsWith(createBuffer("hello worl")), is(true));
        assertThat(createBuffer("hello world").startsWith(createBuffer("hello world")), is(true));
        assertThat(createBuffer("hello world").startsWith(createBuffer("hello world!")), is(false));

        // other 1-off error types...
        assertThat(createBuffer("").startsWith(createBuffer("nope")), is(false));
        assertThat(createBuffer("").startsWith(createBuffer("")), is(true));
        assertThat(createBuffer("empty should match I guess").startsWith(createBuffer("")), is(true));

        // do some slicing
        final String s = "hello world";
        assertThat(createBuffer(s).slice(6, s.length()).startsWith(createBuffer("world")), is(true));
        assertThat(createBuffer(s).slice(6, s.length()).startsWith(createBuffer(s).slice(6, 7)), is(true));
    }

    @Test
    public void testStartsWithIgnoreCase() {
        assertThat(createBuffer("hello world").startsWithIgnoreCase(createBuffer("heLlO")), is(true));
        assertThat(createBuffer("hello world").startsWithIgnoreCase(createBuffer("hello world this is too long")), is(false));

        // testing for 1-off errors...
        assertThat(createBuffer("hello World").startsWithIgnoreCase(createBuffer("hello worl")), is(true));
        assertThat(createBuffer("hello World").startsWithIgnoreCase(createBuffer("hello world")), is(true));
        assertThat(createBuffer("hello World").startsWithIgnoreCase(createBuffer("hello world!")), is(false));

        // other 1-off error types...
        assertThat(createBuffer("").startsWithIgnoreCase(createBuffer("nope")), is(false));
        assertThat(createBuffer("").startsWithIgnoreCase(createBuffer("")), is(true));
        assertThat(createBuffer("empty should match I guess").startsWithIgnoreCase(createBuffer("")), is(true));

        // do some slicing
        final String s = "hello world";
        assertThat(createBuffer(s.toUpperCase()).slice(6, s.length()).startsWithIgnoreCase(createBuffer("world")), is(true));
        assertThat(createBuffer(s.toUpperCase()).slice(6, s.length()).startsWithIgnoreCase(createBuffer(s).slice(6, 7)), is(true));
    }

    private static void ensureEndsWith(final Buffer buffer, final byte b) {
        assertThat(buffer.endsWith(b), is(true));
    }

    private static void ensureEndsWith(final Buffer buffer, final byte b1, final byte b2) {
        assertThat(buffer.endsWith(b1, b2), is(true));
    }

    private static void ensureEndsWith(final Buffer buffer, final byte b1, final byte b2, final byte b3) {
        assertThat(buffer.endsWith(b1, b2, b3), is(true));
    }

    private static void ensureEndsWith(final Buffer buffer, final byte b1, final byte b2, final byte b3, final byte b4) {
        assertThat(buffer.endsWith(b1, b2, b3, b4), is(true));
    }

    private static void ensureNotEndsWith(final Buffer buffer, final byte[] bytes) {
        assertThat(buffer.endsWith(bytes), is(false));
    }

    private static void ensureNotEndsWith(final Buffer buffer, final String str) {
        ensureNotEndsWith(buffer, str.getBytes());
    }

    private static void ensureEndsWith(final Buffer buffer, final byte[] bytes) {
        assertThat(buffer.endsWith(bytes), is(true));
    }

    private static void ensureEndsWith(final Buffer buffer, final String str) {
        ensureEndsWith(buffer, str.getBytes());
    }

    private static void ensureEndsWithFails(final Buffer buffer, final byte[] bytes) {
        try {
            buffer.endsWith(bytes);
            fail("Expected the Buffer.endsWith to fail here");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIndexOf() throws Exception {
        final Buffer buffer = createBuffer("hello world ena goa grejor");
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
    }

    @Test
    public void testCountOccurances() {
        final Buffer buffer = createBuffer("127.0.0.1");
        assertThat(buffer.countOccurences('.'), is(3));

        ensureCountOccurances(3, '.', "127.0.0.1", 3);

        // start after the first .
        ensureCountOccurances(4, '.', "127.0.0.1", 2);

        // boundary testing.
        ensureCountOccurances(7, '.', "127.0.0.1", 1);
        ensureCountOccurances(8, '.', "127.0.0.1", 0);

        try {
            ensureCountOccurances(9, '.', "127.0.0.1", 0);
            fail("expected to blow up because we are out of bounds");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

    }

    @Test
    public void testCountOccurancesOnSlices() {
        final Buffer buffer = createBuffer("127.0.0.1");

        // slice operations...
        final Buffer slice1 = buffer.slice(3, buffer.capacity());
        assertThat(slice1.toString(), is(".0.0.1")); // really just to make it easier to read the unit test.
        assertThat(slice1.countOccurences('.'), is(3));
        ensureCountOccurances(1, '.', slice1, 2);
        ensureCountOccurances(5, '.', slice1, 0);

        // slice of the slice
        final Buffer slice1_1 = slice1.slice(1, 5);
        assertThat(slice1_1.toString(), is("0.0.")); // really just to make it easier to read the unit test.
        assertThat(slice1_1.countOccurences('.'), is(2));
        ensureCountOccurances(1, '.', slice1_1, 2);
        ensureCountOccurances(2, '.', slice1_1, 1);
        ensureCountOccurances(3, '.', slice1_1, 1);

        try {
            ensureCountOccurances(4, '.', slice1_1, 0);
            fail("expected to blow up because we are out of bounds");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * Ensure that the max bytes parameter is honored...
     */
    @Test
    public void testCountOccurencesWithMaxBytes() {
        // this one is 48 characters long.
        //              (zero indexed)         3    7    13   18   23  27    33   39   44
        //                                     v    v    v    v    v   v     v     v    v
        final Buffer buffer = createBuffer("one two three four five six seven eight nine ten");

        // really just to validate that I counted the spaces above correctly...
        final List<Integer> indices = Arrays.asList(3, 7, 13, 18, 23, 27, 33, 39, 44);
        for (int i = 0; i < indices.size(); ++i) {
            final int start = i == 0 ? 0 : indices.get(i - 1) + 1;
            assertThat(buffer.indexOf(start, 1024, (byte)' '), is(indices.get(i)));
        }

        ensureCountOccurances(0, ' ', buffer, 9);

        ensureCountOccurances(0, 11, ' ', buffer, 2); // only go to about half way into "three" so two spaces
        ensureCountOccurances(4, 11 - 4, ' ', buffer, 1); // note that the

        // boundary testing...
        ensureCountOccurances(2, 0, ' ', buffer, 0); // dumb but allowed
        ensureCountOccurances(2, 1, ' ', buffer, 0); // only checking the 'e' in "one"
        ensureCountOccurances(3, 1, ' ', buffer, 1); // now we are "standing" on the first space between "one" and "two"
        ensureCountOccurances(2, 2, ' ', buffer, 1); // starting from the 'e' in "one", checking 2 bytes, which will include the first space.
        ensureCountOccurances(33, 2, ' ', buffer, 1); // starting at the white space between seven and eight

        // boundary testing at the end of the array and also
        // making sure if we go beyond the capacity for "maxbytes" then the
        // capacity will be used instead.
        ensureCountOccurances(33, 1000, ' ', buffer, 3);
        ensureCountOccurances(39, 1000, ' ', buffer, 2);
        ensureCountOccurances(44, 1000, ' ', buffer, 1);
        ensureCountOccurances(45, 1000, ' ', buffer, 0);
        ensureCountOccurances(46, 1000, ' ', buffer, 0);
        ensureCountOccurances(47, 1000, ' ', buffer, 0);

        try {
            ensureCountOccurances(48, 1000, ' ', buffer, 0);
            fail("expected to blow up because we are out of bounds");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * Ensure that the max bytes parameter is honored even with slices...
     */
    @Test
    public void testCountOccurencesWithMaxBytesOnSlices() {
        // this one is 48 characters long.
        //              (zero indexed)         3    7    13   18   23  27    33   39   44
        //                                     v    v    v    v    v   v     v     v    v
        final Buffer buffer = createBuffer("one two three four five six seven eight nine ten");
        final Buffer slice1 = buffer.slice(18, 34); // should contain 4 white spaces
        assertThat(slice1.toString(), is(" five six seven "));
        ensureCountOccurances(0, 1000, ' ', slice1, 4);
        ensureCountOccurances(5, 1000, ' ', slice1, 3);
        ensureCountOccurances(15, 1000, ' ', slice1, 1); // standing on the last space
        ensureCountOccurances(15, 1, ' ', slice1, 1);
    }

    private void ensureCountOccurances(final int start, final char ch, final String s, final int expected) {
        final Buffer buffer = createBuffer(s);
        ensureCountOccurances(start, ch, buffer, expected);
    }

    private static void ensureCountOccurances(final int start, final char ch, final Buffer buffer, final int expected) {
        ensureCountOccurances(start, 1024, ch, buffer, expected);
    }

    private static void ensureCountOccurances(final int start, final int max, final char ch, final Buffer buffer, final int expected) {
        assertThat(buffer.countOccurences(start, max, ch), is(expected));
    }

    @Test
    public void testCountWhiteSpace() throws Exception {
        assertThat(createBuffer("").countWhiteSpace(), is(0));
        assertThat(createBuffer(" ").countWhiteSpace(), is(1));
        assertThat(createBuffer("\t").countWhiteSpace(), is(1));

        assertThat(createBuffer(" \t").countWhiteSpace(), is(2));
        assertThat(createBuffer("\t ").countWhiteSpace(), is(2));

        assertThat(createBuffer("not until later \t ").countWhiteSpace(), is(0));
        assertThat(createBuffer("nothing").countWhiteSpace(), is(0));

        // slice and then count.
        assertThat(createBuffer("hello world").slice(5, 10).countWhiteSpace(), is(1));
        assertThat(createBuffer("hello  world").slice(5, 10).countWhiteSpace(), is(2));
        assertThat(createBuffer("hello  \t\tworld").slice(5, 10).countWhiteSpace(), is(4));

        // start counting from a different spot
        assertThat(createBuffer("hello world").countWhiteSpace(5), is(1));
        assertThat(createBuffer("hello world").countWhiteSpace(6), is(0));
        assertThat(createBuffer("hello   world").countWhiteSpace(5), is(3));
        assertThat(createBuffer("hello   world").countWhiteSpace(6), is(2)); // start one space in...
    }

    /**
     * index of white space is just a convenience method but still
     * better work!
     *
     * @throws Exception
     */
    @Test
    public void testIndexOfWhitespace() throws Exception {
        final Buffer buffer = createBuffer("hello ena goa grejor");

        final int index = buffer.indexOfWhiteSpace();
        assertThat(index, is(5));
        assertThat(buffer.getByte(index), is((byte)' '));

        // slice out the first word and then the new
        // index of white space should be 3 (zero index remember!)
        final Buffer slice01 = buffer.slice(index + 1, buffer.capacity());
        assertThat(slice01.indexOfWhiteSpace(), is(3));

        // test to start later in the original buffer
        assertThat(buffer.indexOfWhiteSpace(index + 1), is(9));
        assertThat(buffer.slice(9).toString(), is("hello ena"));

        // Also, if we slice and keep the white space then
        // zero should be returned of course
        assertThat(buffer.slice(index, buffer.capacity()).indexOfWhiteSpace(), is(0));

        // and just make sure we catch HTAB as well
        assertThat(createBuffer("hello\ttab").indexOfWhiteSpace(), is(5));
        assertThat(createBuffer("hello\t tab and then space").indexOfWhiteSpace(), is(5));
        assertThat(createBuffer("hello \tspace first then tab").indexOfWhiteSpace(), is(5));
    }

    /**
     * The index is based off of the window of the slice so even if the underlying
     * buffer is much larger, the index is still zero based from the beginning of the window.
     *
     * Ensure this is true so slice a bunch of times...
     *
     * @throws Exception
     */
    @Test
    public void testIndexOfAfterSlicing() throws Exception {
        final Buffer buffer = createBuffer("hello world ena goa grejor");
        final Buffer hello = buffer.slice(5);
        assertThat(hello.indexOf((byte)'h'), is(0));
        assertThat(hello.indexOf((byte)'e'), is(1));
        assertThat(hello.indexOf((byte)'l'), is(2)); // skip the second 'l' because we would find the first again
        assertThat(hello.indexOf((byte)'o'), is(4));
        assertThat(hello.indexOf((byte)' '), is(-1)); // outside of our window
        assertThat(hello.indexOf((byte)'w'), is(-1)); // outside of our window

        // also, index of white space should also be -1,
        // which really should be exactly the same as searching for
        // space but just in case we fat-finger something at some point
        assertThat(hello.indexOfWhiteSpace(), is(-1));

        // slice further in...
        final Buffer world = buffer.slice(6, 6 + 5);
        assertThat(world.indexOf((byte)'w'), is(0));
        assertThat(world.indexOf((byte)'o'), is(1));
        assertThat(world.indexOf((byte)'r'), is(2));
        assertThat(world.indexOf((byte)'l'), is(3));
        assertThat(world.indexOf((byte)'d'), is(4));

        assertThat(world.indexOf((byte)'h'), is(-1)); // outside of our window
        assertThat(world.indexOf((byte)' '), is(-1)); // outside of our window

        // and as always, check boundaries at the end as well.
        // btw, 'grejor' means stuff in swedish... in case you wondered...
        final Buffer grejor = buffer.slice(20, buffer.capacity());
        assertThat(grejor.indexOf((byte)'g'), is(0));
        assertThat(grejor.indexOf((byte)'r'), is(1));
        assertThat(grejor.indexOf((byte)'e'), is(2));
        assertThat(grejor.indexOf((byte)'j'), is(3));
        assertThat(grejor.indexOf((byte)'o'), is(4));
        assertThat(grejor.indexOf(2, 100, (byte)'r'), is(5)); // skip passed the first 'r'
    }

    @Test
    public void testSlicing() throws Exception {
        final Buffer orig = createBuffer("hello world ena goa grejor");

        assertThat(orig.slice(0).toString(), is(""));
        assertThat(orig.slice(1).toString(), is("h"));
        assertThat(orig.slice(2).toString(), is("he"));
        assertThat(orig.slice(3).toString(), is("hel"));
        assertThat(orig.slice(4).toString(), is("hell"));
        assertThat(orig.slice(5).toString(), is("hello"));
        assertThat(orig.slice(6).toString(), is("hello "));
        assertThat(orig.slice(7).toString(), is("hello w"));
        assertThat(orig.slice(8).toString(), is("hello wo"));
        assertThat(orig.slice(9).toString(), is("hello wor"));
        assertThat(orig.slice(10).toString(), is("hello worl"));
        assertThat(orig.slice(11).toString(), is("hello world"));
        assertThat(orig.slice(12).toString(), is("hello world "));
        assertThat(orig.slice(13).toString(), is("hello world e"));
        assertThat(orig.slice(14).toString(), is("hello world en"));
        assertThat(orig.slice(15).toString(), is("hello world ena"));

        assertThat(orig.slice(0, 0).toString(), is(""));
        assertThat(orig.slice(0, 1).toString(), is("h"));
        assertThat(orig.slice(0, 2).toString(), is("he"));
        assertThat(orig.slice(0, 3).toString(), is("hel"));
        assertThat(orig.slice(0, 4).toString(), is("hell"));
        assertThat(orig.slice(0, 5).toString(), is("hello"));
        assertThat(orig.slice(0, 6).toString(), is("hello "));
        assertThat(orig.slice(0, 7).toString(), is("hello w"));
        assertThat(orig.slice(0, 8).toString(), is("hello wo"));
        assertThat(orig.slice(0, 9).toString(), is("hello wor"));
        assertThat(orig.slice(0, 10).toString(), is("hello worl"));
        assertThat(orig.slice(0, 11).toString(), is("hello world"));
        assertThat(orig.slice(0, 12).toString(), is("hello world "));
        assertThat(orig.slice(0, 13).toString(), is("hello world e"));
        assertThat(orig.slice(0, 14).toString(), is("hello world en"));
        assertThat(orig.slice(0, 15).toString(), is("hello world ena"));

        assertThat(orig.slice(5, 11).toString(), is(" world"));
        assertThat(orig.slice(6, 11).toString(), is("world"));
        assertThat(orig.slice(9, 10).toString(), is("l"));
        assertThat(orig.slice(12, orig.capacity()).toString(), is("ena goa grejor"));

        assertThat(orig.toString(), is("hello world ena goa grejor"));
    }

    /**
     * Whenever we slice out a new buffer, that buffer has it's own buffer window and all
     * operations is in relation to that window.
     *
     * @throws Exception
     */
    @Test
    public void testSliceOfSlices() throws Exception {
        final Buffer orig = createBuffer("hello world ena goa grejor");

        final Buffer slice01 = orig.slice(6, 19);
        assertThat(slice01.toString(), is("world ena goa"));

        final Buffer slice01a = slice01.slice(6, 9);
        assertThat(slice01a.toString(), is("ena"));
        assertThat(slice01a.indexOf((byte)'e'), is(0));
        assertThat(slice01a.indexOf((byte)'n'), is(1));
        assertThat(slice01a.indexOf((byte)'a'), is(2));

        // but of course, the slice01 is still un-affected so
        // looking for the same stuff will yield different
        // indexes because slice01's window is different.
        assertThat(slice01.indexOf((byte)'e'), is(0 + 6));
        assertThat(slice01.indexOf((byte)'n'), is(1 + 6));
        assertThat(slice01.indexOf((byte)'a'), is(2 + 6));

        // and same is true for the original, which is yet
        // another 6 bytes "earlier"... but note that there
        // is an e earlier in the original buffer
        assertThat(orig.indexOf((byte)'e'), is(1)); // the e in 'hello'
        assertThat(orig.indexOf((byte)'n'), is(1 + 6 + 6));
        assertThat(orig.indexOf((byte)'a'), is(2 + 6 + 6));

        final Buffer slice01b = slice01.slice(1, 4);
        assertThat(slice01b.toString(), is("orl"));
        assertThat(slice01b.indexOf((byte)'o'), is(0));
        assertThat(slice01b.indexOf((byte)'r'), is(1));
        assertThat(slice01b.indexOf((byte)'l'), is(2));
    }

    @Test
    public void testHashCode() {
        final Buffer a = createBuffer("hello");
        final Buffer b = createBuffer("hello");
        final Buffer c = createBuffer("world");
        assertThat(a.hashCode(), is(b.hashCode()));
        assertThat(c.hashCode(), not(b.hashCode()));
    }

    @Test
    public void testEqualsBasicStuff() throws Exception {
        assertBufferEquality("hello", "hello", true);
        assertBufferEquality("hello", "world", false);
        assertBufferEquality("hello ", "world", false);
        assertBufferEquality("hello world", "world", false);
        assertBufferEquality("Hello", "hello", false);
        assertBufferEquality("h", "h", true);
    }

    @Test
    public void testEqualsAfterSlice() throws Exception {
        final Buffer b1 = createBuffer("hello world");
        final Buffer b2 = createBuffer("hello");
        assertThat(b1.slice(b2.capacity()), is(b2));
        assertThat(b1.slice(b2.capacity()).equals(b2), is(true));
    }

    @Test
    public void testEqualsIgnoreCase() throws Exception {
        assertBufferEqualityIgnoreCase("Hello", "hello", true);
        assertBufferEqualityIgnoreCase("this is A lOng string...", "tHis iS a long string...", true);
        assertBufferEqualityIgnoreCase("Hello", "HEllo", true);
        assertBufferEqualityIgnoreCase("Hello", "HEllO", true);
        assertBufferEqualityIgnoreCase("Hello", "HEllO ", false); // space at the end
        assertBufferEqualityIgnoreCase("123 abC", "123 abc", true);
        assertBufferEqualityIgnoreCase("123 abC !@#$", "123 ABc !@#$", true);
    }

    @Test
    public void testUtf8EqualsIgnoreCase() throws Exception {
        // case-insensitive comparison looks only at the 5 least significant bits for characters
        // that are in the 7-bit ASCII range.

        // 1-byte UTF-8 characters
        assertThat(createBuffer(new byte[] {0x40}).equalsIgnoreCase(createBuffer(new byte[] {0x40})), is(true));
        assertThat(createBuffer(new byte[] {0x40}).equalsIgnoreCase(createBuffer(new byte[] {0x60})), is(false));
        assertThat(createBuffer(new byte[] {0x41}).equalsIgnoreCase(createBuffer(new byte[] {0x41})), is(true));
        assertThat(createBuffer(new byte[] {0x41}).equalsIgnoreCase(createBuffer(new byte[] {0x61})), is(true)); // 'A' and 'a'
        assertThat(createBuffer(new byte[] {0x5a}).equalsIgnoreCase(createBuffer(new byte[] {0x5a})), is(true));
        assertThat(createBuffer(new byte[] {0x5a}).equalsIgnoreCase(createBuffer(new byte[] {0x7a})), is(true)); // 'Z' and 'z'
        assertThat(createBuffer(new byte[] {0x5b}).equalsIgnoreCase(createBuffer(new byte[] {0x5b})), is(true));
        assertThat(createBuffer(new byte[] {0x5b}).equalsIgnoreCase(createBuffer(new byte[] {0x7b})), is(false));

        // 2-byte UTF-8 characters. The second byte has the 5 least significant bits the same. In Java,
        // bytes are signed, so we need to convert unsigned notation to signed for the compiler to take it.

        assertThat(createBuffer(new byte[] {0xc0 - 256, 0x80 - 256}).equalsIgnoreCase(createBuffer(new byte[] {0xc0 - 256, 0x80 - 256})), is(true));
        assertThat(createBuffer(new byte[] {0xc0 - 256, 0x80 - 256}).equalsIgnoreCase(createBuffer(new byte[] {0xc0 - 256, 0xa0 - 256})), is(false));
        assertThat(createBuffer(new byte[] {0xc0 - 256, 0x8f - 256}).equalsIgnoreCase(createBuffer(new byte[] {0xc0 - 256, 0x8f - 256})), is(true));
        assertThat(createBuffer(new byte[] {0xc0 - 256, 0x8f - 256}).equalsIgnoreCase(createBuffer(new byte[] {0xc0 - 256, 0xaf - 256})), is(false));
    }

    private void assertBufferEqualityIgnoreCase(final String a, final String b, final boolean equals) {
        final Buffer bufA = createBuffer(a);
        final Buffer bufB = createBuffer(b);
        assertThat(bufA.equalsIgnoreCase(bufB), is(equals));
        assertThat(bufB.equalsIgnoreCase(bufA), is(equals));
    }

    private void assertBufferEquality(final String a, final String b, final boolean equals) {
        final Buffer bufA = createBuffer(a);
        final Buffer bufB = createBuffer(b);
        assertThat(bufA.equals(bufB), is(equals));
        assertThat(bufB.equals(bufA), is(equals));
    }

    @Test
    public void testEqualsHashCode() throws Exception {
        final Buffer b1 = createBuffer("hello world");
        final Buffer b2 = createBuffer("hello world");
        assertThat(b1, is(b2));
        assertThat(b1.hashCode(), is(b2.hashCode()));

        final Buffer b3 = createBuffer("hello not world");
        assertThat(b1, is(not(b3)));
        assertThat(b1.hashCode(), is(not(b3.hashCode())));
        assertThat(b2, is(not(b3)));
    }

    /**
     * A buffer can be parsed as an integer assuming there are no bad characters
     * in there.
     *
     * @throws Exception
     */
    @Test
    public void testParseAsInt() throws Exception {
        assertParseAsInt("1234", 1234);
        assertParseAsInt("-1234", -1234);
        assertParseAsInt("0", 0);
        assertParseAsInt("5060", 5060);

        // negative tests
        assertParseAsIntBadInput("apa");
        assertParseAsIntBadInput("");
        assertParseAsIntBadInput("-5 nope, everything needs to be digits");
        assertParseAsIntBadInput("5 nope, everything needs to be digits");

        // assertParseAsIntSliceFirst("hello:5060:asdf", 6, 10, 5060);
        // assertParseAsIntSliceFirst("hello:-5:asdf", 6, 8, -5);
    }


    @Test
    public void testWriteToOutputStream() throws Exception {
        ensureWriteToOutputStream("one two three");
        ensureWriteToOutputStream("a"); // off by 1 bugs...
        ensureWriteToOutputStream("");
    }

    @Test
    public void testMap() throws Exception {
        final Buffer a = createBuffer("hello");
        final Map<Buffer, String> map = new HashMap<Buffer, String>();
        map.put(a, "fup");
        assertThat(map.get(a), is("fup"));

        final Buffer b = createBuffer("hello");
        assertThat(map.get(b), is("fup"));

        final Buffer c = createBuffer("world");
        map.put(c, "apa");
        assertThat(map.containsKey(c), is(true));

        final Buffer d = createBuffer("nope");
        assertThat(map.containsKey(d), is(false));
    }

    private void ensureWriteToOutputStream(final String data) throws IOException  {
        final Buffer b1 = createBuffer(data);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        b1.writeTo(out);
        assertThat(b1.toString(), is(out.toString()));
    }

    @Test
    public void testStripEOL() throws Exception {
        ensureStripEOL("no CRLF in this one", false, false);
        ensureStripEOL("Just a CR here...\r", true, false);
        ensureStripEOL("Just a LF in this one...\n", false, true);
        ensureStripEOL("Alright, got both!\r\n", true, true);

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

        ensureStripEOL("\r", true, false);
        ensureStripEOL("\n", false, true);
        ensureStripEOL("\r\n", true, true);
    }

    @Test
    public void testComposeBuffers() {
        final Buffer hello = createBuffer("hello");
        final Buffer space = createBuffer(" ");
        final Buffer world = createBuffer("world");

        final Buffer helloWorld = Buffers.wrap(hello, space, world);
        assertThat(helloWorld.toString(), is("hello world"));

        // should be fine to slice
        assertThat(helloWorld.slice(3, 8).toString(), is("lo wo"));

        // and we shouldn't "notice" the boundaries of course...
        assertThat(helloWorld.getByte(0), is((byte)'h'));
        assertThat(helloWorld.getByte(1), is((byte)'e'));
        assertThat(helloWorld.getByte(2), is((byte)'l'));
        assertThat(helloWorld.getByte(3), is((byte)'l'));
        assertThat(helloWorld.getByte(4), is((byte)'o'));
        assertThat(helloWorld.getByte(5), is((byte)' '));
        assertThat(helloWorld.getByte(6), is((byte)'w'));
        assertThat(helloWorld.getByte(7), is((byte)'o'));
        assertThat(helloWorld.getByte(8), is((byte)'r'));
        assertThat(helloWorld.getByte(9), is((byte)'l'));
        assertThat(helloWorld.getByte(10), is((byte)'d'));
    }

    protected void ensureStripEOL(final String line, final boolean cr, final boolean lf) {
        final Buffer buffer = createBuffer(line);
        final Buffer stripped = buffer.stripEOL();

        if (cr && lf) {
            assertThat(buffer.capacity(), is(stripped.capacity() + 2));
            assertThat(stripped.toString(), is(line.substring(0, line.length() - 2)));
        } else if (cr || lf) {
            assertThat(buffer.capacity(), is(stripped.capacity() + 1));
            assertThat(stripped.toString(), is(line.substring(0, line.length() - 1)));
        } else {
            // neither so should be the exact same.
            assertThat(buffer, is(stripped));
        }
    }

    protected void assertParseAsInt(final String number, final int expectedNumber) throws NumberFormatException {
        final Buffer buffer = createBuffer(number);
        assertThat(buffer.parseToInt(), is(expectedNumber));
    }

    protected void assertParseAsIntBadInput(final String badNumber) {
        try {
            final Buffer buffer = createBuffer(badNumber);
            buffer.parseToInt();
            fail("Expected a NumberFormatException");
        } catch (final NumberFormatException e) {
            // expected
        }
    }

}
