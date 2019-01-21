package io.snice.buffer;

public interface ReadableBuffer extends Buffer {

    /**
     * The reader index
     *
     * @return
     */
    int getReaderIndex();

    ReadableBuffer setReaderIndex(int index);

    /**
     * Mark the current position of the reader index.
     *
     */
    ReadableBuffer markReaderIndex();

    /**
     * Reset the reader index to the marked position or to the beginning of the
     * buffer if mark hasn't explicitly been called.
     */
    ReadableBuffer resetReaderIndex();

    /**
     * Read the next byte, which will also increase the readerIndex by one.
     *
     * @return the next byte
     * @throws IndexOutOfBoundsException
     *             in case there is nothing left to read
     */
    byte readByte() throws IndexOutOfBoundsException;

    /**
     * Peak a head to see what the next byte is. This method will not change the
     * readerIndex
     *
     * @return the next byte
     * @throws IndexOutOfBoundsException
     *             in case there is nothing left to read
     */
    byte peekByte() throws IndexOutOfBoundsException;

    /**
     * Read an unsigned int and will increase the reader index of this buffer by
     * 4
     *
     * @return a long representing the unsigned int
     * @throws IndexOutOfBoundsException
     *             in case there is not 4 bytes left to read
     */
    long readUnsignedInt() throws IndexOutOfBoundsException;

    /**
     * Read an int and will increase the reader index of this buffer by 4
     *
     * @return the int value
     * @throws IndexOutOfBoundsException
     *             in case there is not 4 bytes left to read
     */
    int readInt() throws IndexOutOfBoundsException;

    /**
     * Read the requested number of bytes and increase the readerIndex with the
     * corresponding number of bytes. The new buffer and this buffer both share
     * the same backing array so changing either one of them will affect the
     * other.
     *
     * @param length
     * @return
     * @throws IndexOutOfBoundsException
     */
    Buffer readBytes(int length) throws IndexOutOfBoundsException;

    /**
     * Reads a line, i.e., it reads until we hit a line feed ('\n') or a
     * carriage return ('\r'), or a carriage return followed immediately by a
     * line feed.
     *
     * @return a buffer containing the line but without the line terminating
     *         characters
     */
    Buffer readLine();

    /**
     * Read until we find a single CRLF. The single CRLF will NOT be part of the
     * returned buffer but will be consumed.
     *
     * If we cannot find a single CRLF then null will be returned and the passed
     * in buffer will be reset to the same reader index as when it was passed
     * in.
     *
     * Note that this one is very similar to {@link ReadableBuffer#readLine()} but the
     * readLine doesn't enforce the CRLF being present, which is typical for
     * e.g. SIP and is important when reading bytes being streamed over e.g.
     * a network connection
     *
     * @return the resulting buffer containing everything up until (but not
     *         inclusive) the single-crlf or null if no single-crlf was not
     *         found.
     */
    Buffer readUntilSingleCRLF();

    /**
     * Read until we find a double CRLF. The double CRLF will NOT be part of the
     * returned buffer but they will be consumed.
     *
     * If we cannot find a double CRLF then null will be returned and the passed
     * in buffer will be reset to the same reader index as when it was passed
     * in.
     *
     * @return the resulting buffer containing everything up until (but not
     *         inclusive) the double-crlf or null if no double-crlf was not
     *         found.
     */
    Buffer readUntilDoubleCRLF();

    /**
     * Returns the number of available bytes for reading without blocking. If
     * this returns less than what you want, there may still be more bytes
     * available depending on the underlying implementation. E.g., a {@link ReadableBuffer}
     * backed by an {@link java.io.InputStream} may be able to read more off the stream,
     * however, it may not be able to do so without blocking.
     *
     * @return
     */
    int getReadableBytes();

    /**
     * Checks whether this buffer has any bytes available for reading without
     * blocking.
     *
     * This is the same as <code>{@link #getReadableBytes()} > 0</code>
     *
     * @return
     */
    boolean hasReadableBytes();

    /**
     * Same as {@link #readUntil(4096, 'b')}
     *
     * Read until the specified byte is encountered and return a buffer
     * representing that section of the buffer.
     *
     * If the byte isn't found, then a {@link ByteNotFoundException} is thrown
     * and the {@link #getReaderIndex()} is left where we bailed out.
     *
     * Note, the byte we are looking for will have been consumed so whatever
     * that is left in the {@link Buffer} will not contain that byte.
     *
     * Example: <code>
     *    Buffer buffer = Buffers.wrap("hello world");
     *    Buffer hello = buffer.readUntil((byte)' ');
     *    System.out.println(hello);  // will contain "hello"
     *    System.out.println(buffer); // will contain "world"
     * </code>
     *
     * As the example above illustrates, we are looking for a space, which is
     * found between "hello" and "world". Since the space will be consumed, the
     * original buffer will now only contain "world" and not " world".
     *
     * @param b
     *            the byte to look for
     * @return a buffer containing the content from the initial reader index to
     *         the the position where the byte was found (exclusive the byte we
     *         are looking for)
     *
     * @throws ByteNotFoundException
     *             in case the byte we were looking for is not found.
     */
    Buffer readUntil(byte b) throws ByteNotFoundException;

    /**
     * Read until any of the specified bytes have been encountered or until we
     * have read a maximum amount of bytes. This one works exactly the same as
     * {@link #readUntil(byte)} except it allows you to look for multiple bytes
     * and to specify for how many bytes we should be looking before we give up.
     *
     * Example, we want to read until we either find
     *
     * @param maxBytes
     *            the maximum number of bytes we would like to read before
     *            giving up.
     * @param bytes
     *            the bytes we are looking for (either one of them)
     * @return a buffer containing the content from the initial reader index to
     *         the the position where the byte was found (exclusive the byte we
     *         are looking for)
     * @throws ByteNotFoundException
     *             in case none of the bytes we were looking for are found
     *             within the specified maximum number of bytes.
     * @throws IllegalArgumentException
     *             in no bytes to look for is specified.
     */
    Buffer readUntil(int maxBytes, byte... bytes) throws ByteNotFoundException, IllegalArgumentException;

    /**
     * Same as {@link #readUntil(int, byte...)} but will return null instead of
     * throwing a {@link ByteNotFoundException}
     *
     * @param maxBytes
     * @param bytes
     * @return
     * @throws IllegalArgumentException
     */
    Buffer readUntilSafe(int maxBytes, byte... bytes) throws IllegalArgumentException;

    int readUnsignedShort() throws IndexOutOfBoundsException;
    short readShort() throws IndexOutOfBoundsException;
    short readUnsignedByte() throws IndexOutOfBoundsException;


    /**
     * <p>
     *     Cloning a {@link ReadableBuffer} just means that you need a separate object
     *     with its own reader-index since the actual underlying byte-array is not
     *     modifiable. Hence, it is a fairly cheap operation.
     * </p>
     * @return
     */
    @Override
    ReadableBuffer clone();
}
