package io.snice.buffer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
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

    public Buffer createBuffer(final String s) {
        return createBuffer(s.getBytes());
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

}
