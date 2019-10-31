package io.snice.buffer;

import io.snice.buffer.impl.DefaultWritableBuffer;

import java.io.UnsupportedEncodingException;

import static io.snice.preconditions.PreConditions.assertArray;

public interface WritableBuffer extends ReadableBuffer {

    static WritableBuffer of(final byte... buffer) {
        assertArray(buffer);
        return DefaultWritableBuffer.of(buffer);
    }

    static WritableBuffer of(final int capacity) {
        return DefaultWritableBuffer.of(capacity);
    }

    static WritableBuffer of(final byte[] buffer, final int offset, final int length) {
        return DefaultWritableBuffer.of(buffer, offset, length);
    }

    void setUnsignedByte(int index, short value) throws IndexOutOfBoundsException;
    void setUnsignedShort(int index, int value) throws IndexOutOfBoundsException;
    void setUnsignedInt(int index, long value) throws IndexOutOfBoundsException;
    void setInt(int index, int value) throws IndexOutOfBoundsException;

    /**
     * Turn the bit within the given byte on or off.
     *
     * @param index the index of the byte
     * @param bitNo the bit within the given byte whose bit we wish to turn on/off.
     * @param on flag indicating whether we are turning on (true) or turning off (false) the bit.
     * @throws IndexOutOfBoundsException in case the index of the bit is not within 0 - 7 (inclusive)
     * or if the index of the byte is out of bounds.
     */
    void setBit(int index, int bitNo, boolean on) throws IndexOutOfBoundsException;

    /**
     * Convenience method for turning on/off the first bit (zero indexed)
     * in the given byte as indicated by the index.
     *
     * @param index the index of the byte whose first bit we wish to turn off/on.
     * @param on flag indicating whether we are turning on (true) or turning off (false) the bit.
     * @throws IndexOutOfBoundsException
     */
    void setBit0(int index, boolean on) throws IndexOutOfBoundsException;
    void setBit1(int index, boolean on) throws IndexOutOfBoundsException;
    void setBit2(int index, boolean on) throws IndexOutOfBoundsException;
    void setBit3(int index, boolean on) throws IndexOutOfBoundsException;
    void setBit4(int index, boolean on) throws IndexOutOfBoundsException;
    void setBit5(int index, boolean on) throws IndexOutOfBoundsException;
    void setBit6(int index, boolean on) throws IndexOutOfBoundsException;
    void setBit7(int index, boolean on) throws IndexOutOfBoundsException;

    /**
     * Set the byte at given index to a new value
     *
     * @param index
     *            the index
     * @param value
     *            the value
     * @throws IndexOutOfBoundsException
     */
    void setByte(int index, byte value) throws IndexOutOfBoundsException;

    /**
     * The writer index. This is where we will be writing our next byte if asked
     * to do so.
     *
     * @return
     */
    int getWriterIndex();

    /**
     * Set the writer index of this buffer. Note that this will also affect the reader
     * index in the following way.
     *
     * If the current reader index is greater than the given index, it will too be set to the same
     * index. Hence, you will end up with no more readable bytes (because we just said by setting the
     * writer index back a bit that we "lost" those bytes, as in we haven't written to them yet, hence
     * they are no longer readable).
     *
     * If the current reader index is less than the given index, it is left untouched.
     *
     * @param index
     */
    void setWriterIndex(int index);

    /**
     * Get the number of writable bytes.
     *
     * @return
     */
    int getWritableBytes();

    /**
     * Checks whether this {@link WritableBuffer} has any space left for writing. Same
     * as {@link #getWritableBytes()} > 0
     *
     * @return
     */
    boolean hasWritableBytes();

    /**
     * Write a byte to where the current writer index is pointing.
     *
     * @param b
     * @throws IndexOutOfBoundsException
     *             in case there is no more space to write to.
     */
    void write(byte b) throws IndexOutOfBoundsException;

    void write(byte[] bytes) throws IndexOutOfBoundsException;

    void write(int value) throws IndexOutOfBoundsException;

    void write(long value) throws IndexOutOfBoundsException;

    /**
     * Same as {@link WritableBuffer#write(String, String)} where the charset is set to
     * "UTF-8"
     *
     * @param s
     * @throws IndexOutOfBoundsException
     *             in case we cannot write entire String to this {@link Buffer}.
     * @throws UnsupportedEncodingException
     *             in case the charset "UTF-8" is not supported by the platform.
     */
    void write(String s) throws IndexOutOfBoundsException;

    /**
     * Write the integer value to this {@link WritableBuffer} as a String.
     *
     * @param value
     *            the value that will be converted to a String before being
     *            written to this {@link WritableBuffer}.
     * @throws IndexOutOfBoundsException
     */
    void writeAsString(int value) throws IndexOutOfBoundsException;

    /**
     * Write the long value to this {@link WritableBuffer} as a String.
     *
     * @param value the value that will be converted to a String before being written to this
     *        {@link WritableBuffer}.
     * @throws IndexOutOfBoundsException
     */
    void writeAsString(long value) throws IndexOutOfBoundsException;

    /**
     * Write a string to this buffer using the specified charset to convert the String into bytes.
     * The <code>writerIndex</code> of this buffer will be increased with the corresponding number
     * of bytes.
     *
     * Note, either the entire string is written to this buffer or if it doesn't fit then nothing is
     * written to this buffer.
     *
     * @param s
     * @param charset
     * @throws IndexOutOfBoundsException in case we cannot write entire String to this
     *         {@link WritableBuffer}.
     * @throws UnsupportedEncodingException in case the specified charset is not supported
     */
    void write(final String s, String charset) throws IndexOutOfBoundsException ,
            UnsupportedEncodingException;

    /**
     * Operation to zero out the underlying byte-array. The entire byte-array, irrespective of where the
     * current reader and writer index are, will be cleared out. However, lower and upper boundary will be
     * respected.
     *
     * Example:
     *
     * If you initialize a {@link WritableBuffer} with an array of 150 elements and all of those elements currently
     * have the byte 'a' in it (so 97). However, you also restrict the view when creating this {@link WritableBuffer} to
     * only see the 50 in the middle then the lower boundary will be set to 50
     * and the upper boundary will be set to 100. When you call {@link #zeroOut()} you will zero out
     * your view, which in this example would be bytes 50 (inclusive) to 100 (exclusive).
     * The rest will be left untouched.
     *
     * See unit tests for this to see "live" examples.
     *
     */
    void zeroOut();

    /**
     * "Zero" out the buffer but instead of zero use the supplied byte.
     */
    void zeroOut(byte b);

    /**
     * <p>
     *     Cloning a {@link WritableBuffer} means to do a deep-clone of everything, i.e., the underlying
     *     byte-array storage, the reader and writer-index etc.
     * </p>
     *
     * @return
     */
    @Override
    WritableBuffer clone();

    /**
     * Depending on the use case, you may find yourself creating a {@link WritableBuffer}, write some stuff
     * and then "freeze" it in place so no other modifications can be made to it. One way to accomplish that is to
     * use the method {@link #toBuffer()}, which in the context of the {@link WritableBuffer} will create
     * an immutable {@link Buffer} by copying the entire underlying byte-array (only the portion that has been
     * written to of course). This would achieve a thread safe 100% immutable buffer that you can safely pass around
     * and be certain no harm can come to it.
     *
     * However, your application may not tolerate that extra byte-array copy and as such, you just want to "freeze"
     * the current {@link WritableBuffer} because you are "done" and as such, you wish to convert it into an
     * immutable {@link Buffer} instead. This {@link #build()} method does that and works as follows:
     *
     * A new immutable {@link Buffer} using current underlying byte-array of the {@link WritableBuffer} will
     * be created. The reference within the {@link WritableBuffer} will be set to null and the writer and reader
     * index of the {@link WritableBuffer} will be set to values indicating that there is no more room to write
     * and no more data available for reading.
     *
     * Calls to {@link #setWriterIndex(int)} and {@link #setReaderIndex(int)} will be disallowed and an
     * {@link IllegalStateException} will be thrown.
     *
     * Any method calls to modify the underlying data (so setXXX and write's) will "blow up"
     * on {@link IndexOutOfBoundsException}s since the writer index indicates that there is no more room
     * to write.
     *
     * Any method calls to read data (both readXXX and getXXX) will also "blow up" on {@link IndexOutOfBoundsException}
     * since the reader index now also indicates that there are no more data to read.
     *
     * @return
     */
    Buffer build();
}
