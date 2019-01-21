package io.snice.buffer;

import java.io.UnsupportedEncodingException;

public interface WritableBuffer extends ReadableBuffer {

    void setUnsignedByte(int index, short value) throws IndexOutOfBoundsException;
    void setUnsignedShort(int index, int value) throws IndexOutOfBoundsException;
    void setUnsignedInt(int index, long value) throws IndexOutOfBoundsException;
    void setInt(int index, int value) throws IndexOutOfBoundsException;

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
     * Write a byte to where the current writer index is pointing. Note, many
     * implementations may not support writing and if they don't, they will
     * throw a {@link WriteNotSupportedException}
     *
     * @param b
     * @throws IndexOutOfBoundsException
     *             in case there is no more space to write to (which includes
     *             those cases where the underlying implementation does not
     *             support writing)
     * @throws WriteNotSupportedException
     *             in case the underlying implementation does not support
     *             writes.
     */
    void write(byte b) throws IndexOutOfBoundsException, WriteNotSupportedException;

    void write(byte[] bytes) throws IndexOutOfBoundsException, WriteNotSupportedException;

    void write(int value) throws IndexOutOfBoundsException, WriteNotSupportedException;

    void write(long value) throws IndexOutOfBoundsException, WriteNotSupportedException;

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
    void write(String s) throws IndexOutOfBoundsException, WriteNotSupportedException, UnsupportedEncodingException;

    /**
     * Write the integer value to this {@link WritableBuffer} as a String.
     *
     * @param value
     *            the value that will be converted to a String before being
     *            written to this {@link WritableBuffer}.
     * @throws IndexOutOfBoundsException
     * @throws WriteNotSupportedException
     */
    void writeAsString(int value) throws IndexOutOfBoundsException, WriteNotSupportedException;

    /**
     * Write the long value to this {@link WritableBuffer} as a String.
     *
     * @param value the value that will be converted to a String before being written to this
     *        {@link WritableBuffer}.
     * @throws IndexOutOfBoundsException
     * @throws WriteNotSupportedException
     */
    void writeAsString(long value) throws IndexOutOfBoundsException, WriteNotSupportedException;

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
     * @throws WriteNotSupportedException
     * @throws UnsupportedEncodingException in case the specified charset is not supported
     */
    void write(final String s, String charset) throws IndexOutOfBoundsException, WriteNotSupportedException,
            UnsupportedEncodingException;

    /**
     * <p>
     *     Cloning a {@link WritableBuffer} means to do a deep-clone of everything, i.e., the underlying
     *     byte-array storage, the reader and writer-index etc.
     * </p>
     *
     * @return
     */
    WritableBuffer clone();
}
