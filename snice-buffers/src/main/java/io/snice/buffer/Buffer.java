package io.snice.buffer;

import io.snice.buffer.impl.DefaultImmutableBuffer;

import java.io.IOException;
import java.io.OutputStream;

import static io.snice.preconditions.PreConditions.assertArray;
import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * <p>
 * An immutable buffer interface.
 * </p>
 *
 * <p>
 * The main purpose of this {@link Buffer} class is to provide a thread safe buffer that
 * supports a variety of operations commonly used when working with framers/parsers.
 * Common operations that one may run into in those situations is scanning until
 * you find a certain byte, or sequence of bytes ({@link #indexOf(int, int, byte...)}, ability
 * to {@link #slice()} off areas of the buffer and treat them as independent buffers (yet thread safe and cheap
 * because no underlying byte-array is copied) and just in general helping out with working
 * with byte-arrays.
 * </p>
 *
 * <p>
 *     Of course, since this {@link Buffer} is immutable all operations herein are of the type
 *     "find the index of x" but if you want to keep track of your read progress, which is typical
 *     when building parsers and framers, then you don't want to keep track of the read index yourself
 *     so in that case, use the {@link ReadableBuffer} instead.
 * </p>
 *
 * <p>
 * This follows somewhat the pattern of the Netty buffers but they are not as standalone
 * as I'd like. Also, if you do work with a lot of byte-arrays, you kind of want to wrap
 * them into something a little more usable.
 * </p>
 */
public interface Buffer {

    byte AT = '@';
    byte COLON = ':';
    byte SEMI = ';';
    byte DOUBLE_QOUTE = '"';
    byte CR = '\r';
    byte LF = '\n';
    byte SP = ' ';
    byte HTAB = '\t';
    byte DASH = '-';
    byte PERIOD = '.';
    byte COMMA = ',';
    byte EXCLAMATIONPOINT = '!';
    byte PERCENT = '%';
    byte STAR = '*';
    byte UNDERSCORE = '_';
    byte QUESTIONMARK = '?';
    byte PLUS = '+';
    byte BACKTICK = '`';
    byte TICK = '\'';
    byte TILDE = '~';
    byte EQ = '=';
    byte SLASH = '/';
    byte BACK_SLASH = '\\';

    /**
     * Left parenthesis
     */
    byte LPAREN = '(';

    /**
     * Right parenthesis
     */
    byte RPAREN = ')';

    /**
     * Right angle quote
     */
    byte RAQUOT = '>';

    /**
     * Left angle quote
     */
    byte LAQUOT = '<';

    /**
     * Double quotation mark
     */
    byte DQUOT = '"';

    /**
     * Helper method to "parse" out a unsigned int from the given 4 bytes.
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @return
     */
    static long unsignedInt(final byte a, final byte b, final byte c, final byte d) {
        return (a & 0xff) << 24 | (b & 0xff) << 16 | (c & 0xff) << 8 | d & 0xff;
    }

    /**
     * Helper method to "parse" out a signed int from the given 4 bytes.
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @return
     */
    static int signedInt(final byte a, final byte b, final byte c, final byte d) {
        return (a & 0xff) << 24 | (b & 0xff) << 16 | (c & 0xff) << 8 | d & 0xff;
    }

    /**
     * Helper method to "parse" out a signed int from the given 3 bytes.
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    static int signedInt(final byte a, final byte b, final byte c) {
        return (a & 0xff) << 16 | (b & 0xff) << 8 | c & 0xff;
    }

    /**
     * Helper method to convert a byte into a binary string.
     *
     * @param b
     * @return
     */
    static String toBinaryString(final byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    static Buffer of(final byte... buffer) {
        assertArray(buffer);
        return DefaultImmutableBuffer.of(buffer);
    }

    static Buffer of(final byte[] buffer, final int offset, final int length) {
        return DefaultImmutableBuffer.of(buffer, offset, length);
    }

    /**
     * Helper method for checking whether the supplied byte is a alphanumeric
     * character or not.
     *
     * @param ch
     * @return true if the byte is indeed a alphanumeric character, false
     *         otherwise
     */
    static boolean isAlphaNum(final char ch) {
        return ch >= 97 && ch <= 122 || ch >= 48 && ch <= 57 || ch >= 65 && ch <= 90;
    }

    static boolean isAlphaNum(final byte b) {
        return isAlphaNum((char) b);
    }

    /**
     * Find the (next) index of white space, which is defined as {@link #SP} or a {@link #HTAB}.
     *
     * This is a convenience method for just doing a {@link #indexOf(int, int, byte...)}.
     *
     * @param startIndex start (inclusive) searching from this point in buffer.
     * @return the index of where the next {@link #SP} or {@link #HTAB} is found or -1 (negative one)
     * if none is found.
     */
    default int indexOfWhiteSpace(final int startIndex) throws ByteNotFoundException, IllegalArgumentException {
        return indexOf(startIndex, 4096, SP, HTAB);
    }

    /**
     * Same as {@link #indexOfWhiteSpace(int)} with a start index of zero
     *
     * @return
     */
     default int indexOfWhiteSpace() throws ByteNotFoundException, IllegalArgumentException {
         return indexOfWhiteSpace(0);
     }

    /**
     * Count all the consecutive white space starting with the supplied index. If the start index
     * itself isn't a white space ({@link #SP} or {@link #HTAB} then zero will be returned.
     *
     * @param startIndex start counting from here (inclusive)
     * @return the number of consecutive white spaces found starting at the supplied index.
     * Zero if none is found.
     */
     int countWhiteSpace(int startIndex);

    /**
     * This is the same as {@link #countWhiteSpace(int)} with a start index of zero
     */
    default int countWhiteSpace() {
        return countWhiteSpace(0);
    }

    /**
     * Convert the current buffer into the immutable version of the buffer classes. If this buffer
     * already is a {@link Buffer} this will simply return this. If this is of type {@link ReadableBuffer} or
     * a {@link WritableBuffer} then these will be converted into a immutable version (so the base {@link Buffer});
     *
     * Also note that in the case of the {@link ReadableBuffer}, any bytes that have been consumed by reading
     * passed them will not be part of the returned {@link Buffer}. After all, any bytes that have been read
     * may in fact have been discarded.
     *
     * @return
     */
    Buffer toBuffer();

    /**
     * <p>
     * Convert this immutable buffer into a {@link ReadableBuffer}.
     * </p>
     *
     * <p>
     * Convert this immutable buffer into a {@link ReadableBuffer}. Since a {@link ReadableBuffer}
     * is immutable from the point of the underlying byte-storage, this is fairly cheap. The only
     * part of a {@link ReadableBuffer} that is mutable is its reader-index.
     * </p>
     *
     * @return
     */
    ReadableBuffer toReadableBuffer();

    /**
     * <p>
     * Convert this immutable buffer into a {@link WritableBuffer}.
     * </p>
     *
     * <p>
     *     Since a {@link WritableBuffer} has the ability to change the contents of the buffer, this
     *     operation will always clone the entire underlying byte-storage so if any changes are made
     *     to it, this current buffer will be un-affected.
     * </p>
     *
     * <p>
     *     Note that if you perform this operation on a {@link WritableBuffer} then this will be the
     *     same as cloning that buffer.
     * </p>
     *
     * @return
     */
    WritableBuffer toWritableBuffer();

    /**
     * Find the index of a single CRLF or -1 (negative one) if we
     * can't find it.
     *
     * @return the resulting buffer containing everything up until (but not
     *         inclusive) the single-crlf or null if no single-crlf was not
     *         found.
     */
    int indexOfSingleCRLF();

    /**
     * Read until we find a double CRLF and slice that buffer out.
     * The double CRLF will NOT be part of the returned buffer.
     *
     * If we cannot find a double CRLF then null will be returned.
     *
     * @return the resulting buffer containing everything up until (but not
     *         inclusive) the double-crlf or null if no double-crlf was not
     *         found.
     */
    Buffer indexOfDoubleCRLF();

    /**
     * Check whether this buffer is empty or not.
     *
     * @return
     */
    boolean isEmpty();

    /**
     * Test to see if this buffer starts with the <code>other</code> buffer.
     *
     * If the <code>other</code> is an empty buffer, true will be returned, which is consistent with how
     * {@link String#startsWith(String)} behaves as well.
     *
     * @param other
     * @return
     * @throws IllegalArgumentException in case <code>other</code> is null
     */
    default boolean startsWith(final Buffer other) throws IllegalArgumentException{
        assertNotNull(other, "The other buffer cannot be null");
        if (other.isEmpty()) {
            return true;
        }

        if (other.capacity() > this.capacity()) {
            return false;
        }

        final Buffer slice = slice(other.capacity());
        return slice.equals(other);
    }

    default boolean startsWithIgnoreCase(final Buffer other) {
        assertNotNull(other, "The other buffer cannot be null");
        if (other.isEmpty()) {
            return true;
        }

        if (other.capacity() > this.capacity()) {
            return false;
        }

        final Buffer slice = slice(other.capacity());
        return slice.equalsIgnoreCase(other);
    }

    /**
     * <p>
     * The capacity of this buffer.
     * </p>
     *
     * <p>
     * For the {@link ReadableBuffer} and {@link WritableBuffer}, the capacity is not affected by where the
     * reader index is etc.
     * </p>
     *
     * <p>
     *     Also note that in the case of the {@link WritableBuffer}, the capacity is also not affected by
     *     where the writer index is, however, it may be that there are bytes that yet has to have
     *     anything written to them and as such, a portion of your {@link WritableBuffer} may be empty.
     *     So, capacity essentially checks the underlying byte-array and how large it is. The
     *     {@link ReadableBuffer#getReaderIndex()} is the mark for how far you have read into that
     *     underlying byte-buffer and the {@link WritableBuffer#getWriterIndex()} keeps track of how
     *     far into that byte-array you have written things into.
     * </p>
     * <p>
     *     TODO: document the above with an image.
     * </p>
     *
     * @return the capacity
     */
    int capacity();

    /**
     * Same as {@link #indexOf(int, byte...)} but will return null instead of
     * throwing a {@link ByteNotFoundException}
     *
     * @param maxBytes
     * @param bytes
     * @return
     * @throws IllegalArgumentException
     */
    int indexdOfSafe(int maxBytes, byte... bytes) throws IllegalArgumentException;

    /**
     *
     */
    int indexOf(int maxBytes, byte... bytes) throws ByteNotFoundException, IllegalArgumentException;

    /**
     *
     *
     * @param startIndex where to start searching. Note that if the start index is out of
     *                   bounds, i.e. less than zero or greater than the capacity of the
     *                   buffer it will be silently ignored and -1 (negative one) will
     *                   be returned to indicate we didn't find it.
     * @param maxBytes
     *            the maximum number of bytes we would like to read before
     *            giving up.
     * @param bytes
     *            the bytes we are looking for (either one of them)
     * @return the index of the found byte or -1 (negative one) if we couldn't
     *         find it.
     * @throws IllegalArgumentException in case maxBytes is zero or less or
     * in case the bytes we are looking for hasn't been specified (cmon - how would
     * I know what to look for if you don't tell me!)
     * @throws ByteNotFoundException
     *             will ONLY be thrown if we haven't found the byte within the
     *             maxBytes limit. If the buffer we are searching in is less
     *             than maxBytes and we can't find what we are looking for then
     *             negative one will be returned instead.
     */
    int indexOf(int startIndex, int maxBytes, byte... bytes) throws ByteNotFoundException, IllegalArgumentException;

    /**
     * Search for the first occurrence of the specified byte and return it's index, or negative 1 if not found.
     *
     * Note that if this is a {@link ReadableBuffer} then the <code>readerIndex</code> dictates from where
     * we start searching since everything that has been read so far is "consumed" and no longer in view.
     * However, the index returned is based on the underlying buffer, hence, don't try and interpret the actual index
     * too much and it really should only be used to a subsequent call to {@link #getByte(int)} etc.
     *
     * Perhaps this is slightly confusing but then again, we couldnt return an index that is based off of
     * the reader index either since every read would then invalidate the previously returned <code>indexOf</code>
     * results so that would make no sense and be even more confusing.
     *
     * @param b
     * @return
     * @throws ByteNotFoundException
     *             will ONLY be thrown if we haven't found the byte within the
     *             maxBytes limit. If the buffer we are searching in is less
     *             than maxBytes and we can't find what we are looking for then
     *             negative one will be returned instead.
     * @throws IllegalArgumentException
     */
    int indexOf(byte b) throws ByteNotFoundException, IllegalArgumentException;

    /**
     * <p>
     *     Write the content of this {@link Buffer} to the {@link OutputStream}.
     * </p>
     *
     * <p>
     *     For the default immutable {@link Buffer}, the result of this operation will always be
     *     the same, i.e., the content will never change and as such, you will get the same result
     *     every time. However, for the {@link ReadableBuffer} and, in particular, for the {@link WritableBuffer}, this
     *     is not necessarily true since they both are mutable.
     * </p>
     *
     * <p>
     *     <b>{@link ReadableBuffer}:</b> for this buffer, every time you issue a <code>readXXX</code> you are
     *     essentially consuming that data and in everything before it is discarded. Therefore, whenever you issue
     *     a {@link #writeTo(OutputStream)} and in between those calls you have also done a few <code>readXXX</code>
     *     operations, the amount of data written to the {@link OutputStream} will be less than last time.
     *     This behaviour is also consistent with how {@link #toString()} works for the {@link ReadableBuffer}.
     * </p>
     *
     * <p>
     *     <b>{@link WritableBuffer}:</b> Since the writable buffer can add more data to the buffer, as well as
     *     changing existing data "in the middle", it should not come as a surprise that multiple calls to
     *     link {@link #writeTo(OutputStream)}} will likely yield different results. Also, since this buffer is
     *     an extension to the {@link ReadableBuffer}, the beviour regarding reading also applies to this writable
     *     buffer, which is of course consistent with how {@link #toString()} works for the {@link WritableBuffer}
     *     as well.
     * </p>
     *
     *
     * @param out
     */
    void writeTo(OutputStream out) throws IOException;

    /**
     * Get a slice of the buffer starting at <code>start</code> (inclusive)
     * ending at <code>stop</code> (exclusive). Hence, the new capacity of the
     * new buffer is <code>stop - start</code>
     *
     * @param start the start index (zero based)
     * @param stop the stop index (zero based)
     * @throws IndexOutOfBoundsException in case either the start of stop indexes is beyond
     * that of the capacity of this buffer.
     * @throws IllegalArgumentException in case the start index is greater than stop, or less than zero etc.
     * @return
     */
    Buffer slice(int start, int stop) throws IndexOutOfBoundsException, IllegalArgumentException;

    /**
     *
     * @param stop
     * @return
     */
    Buffer slice(int stop);

    /**
     * Slice off the rest of the buffer, which for the default immutable buffer
     * is the same as just returning this.
     *
     * @return
     */
    Buffer slice();

    /**
     * Get the byte at the index.
     *
     * @param index
     * @return the byte at the specified index
     * @throws IndexOutOfBoundsException
     *             in case the index is greater than the capacity of this buffer
     */
    byte getByte(int index) throws IndexOutOfBoundsException;

    /**
     * Get a 32-bit integer at the specified absolute index. This method will
     * not modify the readerIndex of this buffer.
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     *             in case there is not 4 bytes left to read
     */
    int getInt(int index) throws IndexOutOfBoundsException;

    /**
     * Somewhat of an odd method but there are times when you need to parse out
     * three octets as an int, as opposed to the normal 4.
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     */
    int getIntFromThreeOctets(int index) throws IndexOutOfBoundsException;

    long getUnsignedInt(int index) throws IndexOutOfBoundsException;

    short getShort(int index) throws IndexOutOfBoundsException;

    int getUnsignedShort(int index) throws IndexOutOfBoundsException;

    short getUnsignedByte(int index) throws IndexOutOfBoundsException;

    /**
     * Parse all the readable bytes in this buffer as a unsigned integer value.
     * The reader index will not be modified.
     *
     * @return
     * @throws NumberFormatException
     *             in case the bytes in the buffer cannot be converted into an
     *             integer value.
     */
    int parseToInt() throws NumberFormatException;

    /**
     * Convert the entire buffer to a signed integer value
     *
     * @param radix
     * @return
     */
    int parseToInt(int radix) throws NumberFormatException;

    /**
     * <p>
     * Check if this buffer ends with the passed in bytes.
     * </p>
     *
     *
     * @param content
     * @return
     * @throws IllegalArgumentException in case the passed in byte-array is null or zero length
     */
    boolean endsWith(final byte[] content) throws IllegalArgumentException;

    /**
     * Convenience method for checking if this buffer ends with the specified byte.
     *
     * @param b
     * @return true if the this buffer indeed ends with the specified byte, false otherwise.
     */
    boolean endsWith(final byte b);

    /**
     * Convenience method for checking if this buffer ends with the two specified bytes.
     *
     * Note that if the buffer is less than two bytes long then false will
     * be returned, as opposed to throwing some kind of exception. The reasoning
     * is that if the buffer is less than two bytes then obviously this buffer doesnt
     * end with the specified bytes.
     *
     * @param b1
     * @param b2
     * @return true if the this buffer indeed ends with the specified bytes, false otherwise.
     */
    boolean endsWith(final byte b1, final byte b2);

    /**
     * Convenience method for checking if this buffer ends with the three specified bytes.
     *
     * Note that if the buffer is less than three bytes long then false will
     * be returned, as opposed to throwing some kind of exception. The reasoning
     * is that if the buffer is less than three bytes then obviously this buffer doesnt
     * end with the specified bytes.
     *
     * @param b1
     * @param b2
     * @param b3
     * @return true if the this buffer indeed ends with the specified bytes, false otherwise.
     */
    boolean endsWith(final byte b1, final byte b2, final byte b3);

    /**
     * Convenience method for checking if this buffer ends with the four specified bytes.
     *
     * Note that if the buffer is less than four bytes long then false will
     * be returned, as opposed to throwing some kind of exception. The reasoning
     * is that if the buffer is less than four bytes then obviously this buffer doesnt
     * end with the specified bytes.
     *
     * @param b1
     * @param b2
     * @param b3
     * @param b4
     * @return true if the this buffer indeed ends with the specified bytes, false otherwise.
     */
    boolean endsWith(final byte b1, final byte b2, final byte b3, final byte b4);

    /**
     * Check if this buffer ends with a single CR, a single LF or a CR directly followed by a LF.
     *
     * @return
     */
    default boolean endsWithEOL() {
        return endsWith(CR) || endsWith(LF) || endsWithCRLF();
    }

    default boolean endsWithCRLF() {
        return endsWith(CR, LF);
    }

    default boolean endsWithDoubleCRLF() {
        return endsWith(CR, LF, CR, LF);
    }

    /**
     * <p>
     *     If this {@link Buffer} ends with CR, LF or a single LF or a single CR it will
     *     be stripped and a new (sliced) {@link Buffer} will be returned. If this {@link Buffer} does
     *     not contain EOL, then <code>this</code> will be returned.
     * </p>
     *
     * <p>
     *     Note: this is not {@link ReadableBuffer#readLine()}, meaning that if this buffer has CR, LF or CRLF
     *     somewhere in the middle of the buffer, they will not be stripped! Hence, the following would
     *     be returned as is: <code>Buffers.wrap("I have CRLF in \r\n the middle").stripEOL()</code> would still
     *     be the exact same content. Just compare with e.g. how usually <code>trim</code> functions works, only
     *     removing whitespaces in the beginning and at the end, not the middle. Same same...
     * </p>
     *
     * <p>
     *     Also note that if this is a {@link ReadableBuffer} and you have read into this buffer, then you'll
     *     only get what was still "visible". E.g., if you originally had the following {@link ReadableBuffer}:
     *     <code>"This one ends with CRLF\r\n"</code> and you have read 5 bytes in like so
     *     <code>buffer.readBytes(5)</code> then invoking {@link #stripEOL()} will return
     *     <code>"one ends with CRLF"</code>
     * </p>
     *
     * @return
     */
    default Buffer stripEOL() {
        if (endsWithCRLF()) {
            return slice(capacity() - 2);
        }

        if (endsWith(LF) || endsWith(CR)) {
            return slice(capacity() - 1);
        }

        return this;
    }

    /**
     * Dump the content of this buffer as a hex dump ala Wireshark. Mainly for
     * debugging purposes
     *
     * @return
     */
    String dumpAsHex();

    /**
     * If necessary, this will perform a deep clone of this object. However, for the
     * default immutable buffer, it will just return this since everything is immutable.
     * For the {@link ReadableBuffer} only the reader index is actually mutable and as such,
     * it is a cheap operation since the backing storage (usually just a byte-array) is not
     * copied. However, for all {@link WritableBuffer} this will be a deep-clone.
     *
     * @return
     */
    Buffer clone();

    /**
     * Check whether to buffers are considered to be equal.
     *
     * Note, for the {@link ReadableBuffer} and {@link WritableBuffer} they will only consider
     * the visiable space. I.e., if two {@link ReadableBuffer}s actually has the same underlying byte-array
     * storage but one of the buffers have read "further" then the visible area has changed and as such, they
     * are not considered equal.
     *
     * @param b
     * @return
     */
    @Override
    boolean equals(Object b);

    boolean equalsIgnoreCase(Object b);

    @Override
    int hashCode();

    /**
     * <p>
     *     Return the buffer as a string.
     * </p>
     *
     * <p>
     * Note, if the sub-class is a {@link ReadableBuffer} then depending
     * how much you have read, it will return only the available and readable bytes since as you keep
     * reading from the underlying {@link ReadableBuffer}, those bytes will be discarded (or at least
     * "hidden" - whether or not they are truly discarded and as such, purged from the heap, depends on the
     * underlying implementation)
     * </p>
     * @return
     */
    @Override
    String toString();

    String toUTF8String();
}
