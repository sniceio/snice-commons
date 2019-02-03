package io.snice.buffer.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.ByteNotFoundException;
import io.snice.buffer.ReadableBuffer;
import io.snice.buffer.WritableBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import static io.snice.preconditions.PreConditions.assertArgument;

public class DefaultReadableBuffer implements ReadableBuffer  {

    public static ReadableBuffer of(final byte[] buffer) {
        final Buffer buf = DefaultImmutableBuffer.of(buffer);
        return new DefaultReadableBuffer(buf, 0);
    }

    public static ReadableBuffer of(final byte[] buffer, final int offset, final int length) {
        final Buffer buf = DefaultImmutableBuffer.of(buffer, offset, length);
        return new DefaultReadableBuffer(buf, 0);
    }

    @Override
    public int countWhiteSpace(final int startIndex) {
        return buffer.countWhiteSpace(startIndex);
    }

    public static ReadableBuffer of(final Buffer buffer) {
        return new DefaultReadableBuffer(buffer.toBuffer(), 0);
    }

    /**
     * From where we will continue reading. Note, offset from the lower boundary.
     */
    private int readerIndex;

    /**
     * The position of the reader index that has been marked. I.e., this is the
     * position we will move the reader index back to if someone is asking us to
     * {@link #resetReaderIndex()}
     */
    private int markedReaderIndex;

    /**
     * The {@link ReadableBuffer} is just using an immutable buffer under the hood
     * and exposes some additional functionality for reading around it, which essentially
     * just means to keep track of a reader index.
     */
    private final Buffer buffer;

    private DefaultReadableBuffer(final Buffer buffer, final int readerIndex) {
        this.buffer = buffer;
        this.readerIndex = readerIndex;
    }

    @Override
    public int getReaderIndex() {
        return readerIndex;
    }

    @Override
    public ReadableBuffer setReaderIndex(final int index) {
        assertArgument(index >= 0, "The reader index cannot be less than zero");
        assertArgument(index <= buffer.capacity(), "The reader index cannot be greater than the capacity of the buffer");
        readerIndex = index;
        return this;
    }

    @Override
    public ReadableBuffer resetReaderIndex() {
        readerIndex = markedReaderIndex;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadableBuffer markReaderIndex() {
        markedReaderIndex = readerIndex;
        return this;
    }

    @Override
    public byte readByte() throws IndexOutOfBoundsException {
        return getByte(readerIndex++);
    }

    @Override
    public byte peekByte() throws IndexOutOfBoundsException {
        return getByte(readerIndex);
    }

    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        return readInt() & 0xFFFFFFFFL;
    }

    @Override
    public int readInt() throws IndexOutOfBoundsException {
        final int value = getInt(readerIndex);
        readerIndex += 4;
        return value;
    }

    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException {
        if (length == 0) {
            return EmptyBuffer.EMPTY;
        }

        checkReadableBytes(length);
        final Buffer slice = buffer.slice(readerIndex, readerIndex + length);
        readerIndex += length;
        return slice;
    }
    /**
     * Convenience method for checking if we have enough readable bytes
     *
     * @param length
     *            the length the user wishes to read
     * @throws IndexOutOfBoundsException
     *             in case we don't have the bytes available
     */
    protected void checkReadableBytes(final int length) throws IndexOutOfBoundsException {
        if (!checkReadableBytesSafe(length)) {
            throw new IndexOutOfBoundsException("Not enough readable bytes");
        }
    }

    /**
     * Convenience method for checking if we have enough readable bytes
     *
     * @param length
     *            the length the user wishes to read
     * @return true if we have enough bytes available for read
     */
    protected boolean checkReadableBytesSafe(final int length) {
        return getReadableBytes() >= length;
    }

    @Override
    public Buffer readLine() {
        final int start = getReaderIndex();
        boolean foundCR = false;
        while (hasReadableBytes()) {
            final byte b = readByte();
            switch (b) {
                case LF:
                    return slice(start, getReaderIndex() - (foundCR ? 2 : 1));
                case CR:
                    foundCR = true;
                    break;
                default:
                    if (foundCR) {
                        setReaderIndex(getReaderIndex() - 1);
                        return slice(start, getReaderIndex() - 1);
                    }
            }
        }

        // i guess there were nothing for us to read
        if (start >= getReaderIndex()) {
            return null;
        }

        return slice(start, getReaderIndex());
    }

    @Override
    public Buffer readUntilSingleCRLF() {
        final int start = getReaderIndex();
        int found = 0;
        while (found < 2 && hasReadableBytes()) {
            final byte b = readByte();
            if (found == 0 && b == CR) {
                ++found;
            } else if (found == 1 && b == LF) {
                ++found;
            } else {
                found = 0;
            }
        }
        if (found == 2) {
            return slice(start, getReaderIndex() - 2);
        } else {
            setReaderIndex(start);
            return null;
        }
    }

    @Override
    public Buffer readUntilDoubleCRLF() {
        final int start = getReaderIndex();
        int found = 0;
        while (found < 4 && hasReadableBytes()) {
            final byte b = readByte();
            if ((found == 0 || found == 2) && b == CR) {
                ++found;
            } else if ((found == 1 || found == 3) && b == LF) {
                ++found;
            } else {
                found = 0;
            }
        }
        if (found == 4) {
            return slice(start, getReaderIndex() - 4);
        } else {
            setReaderIndex(start);
            return null;
        }
    }

    @Override
    public int getReadableBytes() {
        return buffer.capacity() - readerIndex;
    }

    @Override
    public Buffer readUntilWhiteSpace() {
        final int index = buffer.indexOfWhiteSpace(readerIndex);
        if (index == -1) {
            return this;
        }

        final int count = buffer.countWhiteSpace(index);
        final Buffer slice = slice(index);
        readerIndex = index + count;
        return slice;
    }


    @Override
    public Buffer readUntil(final byte b) throws ByteNotFoundException {
        return readUntil(4096, b);
    }

    @Override
    public Buffer readUntil(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        final Buffer result = readUntilSafe(maxBytes, bytes);
        if (result == null) {
            throw new ByteNotFoundException(bytes);
        }

        return result;
    }

    @Override
    public Buffer readUntilSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        final int index = indexOf(maxBytes, bytes);
        if (index == -1) {
            return null;
        }

        final int size = index - getReaderIndex();
        final Buffer result;
        if (size == 0) {
            result = EmptyBuffer.EMPTY;
        } else {
            result = readBytes(size);
        }
        readByte(); // consume the one at the index as well
        return result;
    }

    @Override
    public int readUnsignedShort() throws IndexOutOfBoundsException {
        return readShort() & 0xFFFF;
    }

    @Override
    public short readShort() throws IndexOutOfBoundsException {
        final short value = getShort(readerIndex);
        readerIndex += 2;
        return value;
    }

    @Override
    public short readUnsignedByte() throws IndexOutOfBoundsException {
        return (short) (readByte() & 0xFF);
    }

    @Override
    public ReadableBuffer clone() {
        return new DefaultReadableBuffer(buffer, readerIndex);
    }

    @Override
    public boolean equalsIgnoreCase(final Object b) {
        try {
            final DefaultReadableBuffer other = (DefaultReadableBuffer)b;
            return buffer.equalsIgnoreCase(other.buffer);
        } catch (final ClassCastException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return buffer.slice(readerIndex, buffer.capacity()).toString();
    }

    @Override
    public String toUTF8String() {
        return buffer.slice(readerIndex, buffer.capacity()).toUTF8String();
    }

    @Override
    public boolean hasReadableBytes() {
        return getReadableBytes() > 0;
    }

    @Override
    public Buffer toBuffer() {
        return sliceToSize();
    }

    @Override
    public ReadableBuffer toReadableBuffer() {
        return this;
    }

    @Override
    public WritableBuffer toWritableBuffer() {
        return null;
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    @Override
    public int indexOfSingleCRLF() {
        return buffer.indexOfSingleCRLF();
    }

    @Override
    public Buffer indexOfDoubleCRLF() {
        return buffer.indexOfDoubleCRLF();
    }

    @Override
    public boolean isEmpty() {
        return getReadableBytes() == 0;
    }

    @Override
    public int capacity() {
        return buffer.capacity();
    }

    @Override
    public int indexdOfSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented just yet and will probably go away before 1.0");
    }

    @Override
    public int indexOf(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        return buffer.indexOf(readerIndex, maxBytes, bytes);
    }

    @Override
    public int indexOf(final int startIndex, final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException, IndexOutOfBoundsException {
        return buffer.indexOf(startIndex, maxBytes, bytes);
    }

    @Override
    public int indexOf(final byte b) throws ByteNotFoundException, IllegalArgumentException {
        return buffer.indexOf(readerIndex, 4096, b);
    }


    @Override
    public void writeTo(final OutputStream out) throws IOException {
        sliceToSize().writeTo(out);
    }

    @Override
    public Buffer slice(final int start, final int stop) throws IndexOutOfBoundsException, IllegalArgumentException {
        return buffer.slice(start, stop);
    }

    @Override
    public Buffer slice(final int stop) {
        return buffer.slice(readerIndex, stop);
    }

    @Override
    public Buffer slice() {
        return sliceToSize();
    }

    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException {
        return buffer.getByte(index);
    }

    @Override
    public int getInt(final int index) throws IndexOutOfBoundsException {
        return buffer.getInt(index);
    }

    @Override
    public long getUnsignedInt(final int index) throws IndexOutOfBoundsException {
        return buffer.getUnsignedInt(index);
    }

    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException {
        return buffer.getShort(index);
    }

    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        return buffer.getUnsignedShort(index);
    }

    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        return buffer.getUnsignedByte(index);
    }

    @Override
    public int parseToInt() throws NumberFormatException {
        return sliceToSize().parseToInt();
    }

    @Override
    public int parseToInt(final int radix) throws NumberFormatException {
        return buffer.parseToInt(radix);
    }

    @Override
    public boolean endsWith(final byte[] content) throws IllegalArgumentException {
        return buffer.endsWith(content);
    }

    @Override
    public boolean endsWith(final byte b) {
        return buffer.endsWith(b);
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2) {
        return buffer.endsWith(b1, b2);
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3) {
        return buffer.endsWith(b1, b2, b3);
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3, final byte b4) {
        return buffer.endsWith(b1, b2, b3, b4);
    }

    @Override
    public String dumpAsHex() {
        return buffer.dumpAsHex();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        try {
            final Buffer me = sliceToSize();
            if (o instanceof DefaultReadableBuffer) {
                final Buffer that = ((DefaultReadableBuffer)o).sliceToSize();
                return Objects.equals(me, that);
            }

            return Objects.equals(me, o);
        } catch (final ClassCastException e) {
            return false;
        }
    }

    private Buffer sliceToSize() {
        return buffer.slice(readerIndex, capacity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sliceToSize());
    }
}
