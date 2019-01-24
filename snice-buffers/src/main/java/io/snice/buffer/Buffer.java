package io.snice.buffer;

import io.snice.buffer.impl.DefaultImmutableBuffer;

import static io.snice.preconditions.PreConditions.assertArray;

/**
 * <p>
 * An immutable buffer interface.
 * </p>
 *
 * <p>
 * Yes, yet another {@link Buffer} class, as we didn't have enough!
 * <br/>
 * This follows somewhat the pattern of the Netty buffers but they are not as standalone
 * as I'd like. Also, if you do work with a lot of byte-arrays, you kind of want to wrap
 * them into something a little more usable.
 * </p>
 */
public interface Buffer {

    byte LF = '\n';
    byte CR = '\r';

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

    static Buffer of(final byte[] buffer) {
        assertArray(buffer);
        return DefaultImmutableBuffer.of(buffer);
    }

    static Buffer of(final byte[] buffer, final int offset, final int length) {
        return DefaultImmutableBuffer.of(buffer, offset, length);
    }

    /**
     * Convert the current buffer into the immutable version of the buffer classes. If this buffer
     * already is a {@link Buffer} this will simply return this. If this is of type {@link ReadableBuffer} or
     * a {@link WritableBuffer} then these will be converted into a immutable version (so the base {@link Buffer});
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
     * <p>
     * The capacity of this buffer.
     * </p>
     *
     * <p>
     * For the {@link ReadableBuffer} and {@link WritableBuffer}, the capacity is not affected by where the
     * reader index is etc.
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
     * Same as {@link #readUntil(int, byte...)} but instead of returning the
     * buffer with everything up until the specified byte it returns the index
     * instead.
     *
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
    int indexOf(int maxBytes, byte... bytes) throws ByteNotFoundException, IllegalArgumentException;

    /**
     *
     * @param b
     * @return
     * @throws ByteNotFoundException
     * @throws IllegalArgumentException
     */
    int indexOf(byte b) throws ByteNotFoundException, IllegalArgumentException;

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
     * Same as {@link #slice(0, int)}
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

    default boolean endsWithCRLF() {
        return endsWith(CR, LF);
    }

    default boolean endsWithDoubleCRLF() {
        return endsWith(CR, LF, CR, LF);
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
     * reading from the underlying {@link ReadableBuffer}, those bytes will be discarded.
     * </p>
     * @return
     */
    @Override
    String toString();

    String toUTF8String();
}
