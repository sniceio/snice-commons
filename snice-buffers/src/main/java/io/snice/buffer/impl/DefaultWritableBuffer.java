package io.snice.buffer.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.buffer.WritableBuffer;

import java.io.UnsupportedEncodingException;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertArray;

public final class DefaultWritableBuffer implements WritableBuffer {

    /**
     * Once this {@link WritableBuffer} has been {@link #build()}, you cannot
     * ever touch it again.
     */
    private boolean done;

    /**
     * The actual backing buffer.
     */
    private final byte[] buffer;

    /**
     * The upper boundary is always just that, our upper boundary and for the {@link WritableBuffer}
     * that means that we cannot write beyond that boundary. Since {@link WritableBuffer}s are
     * mutable we do not typically share the underlying byte array with anything and as such, under normal
     * circumstances, the upper boundary will be that of the max buffer size. However, it could be that
     * you allocated a large byte-array for network operations and then instead of allocating and copying over
     * the data, you want to keep that memory around but then want to restrict the view.
     *
     * Same logic applies to the lower boundary.
     */
    private final int upperBoundary;
    private final int lowerBoundary;

    /**
     * Note that the writer index is zero based against the lowerBoundary.
     */
    private int writerIndex;

    /**
     * Create a new {@link WritableBuffer} using the supplied buffer as the backing array.
     * It is assumed that all the bytes in this buffer are "empty" and as such, our write index
     * is set at zero.
     *
     * @param buffer
     */
    public static WritableBuffer of(final byte[] buffer) {
        assertArray(buffer);
        return new DefaultWritableBuffer(buffer, 0, buffer.length);
    }

    /**
     * Create a new {@link WritableBuffer} with the given size. This is just a convenience  method
     * for <code>Writablebuffer.of(new byte[capacity])</code>
     *
     * @param capacity the capacity of the {@link WritableBuffer}.
     */
    public static WritableBuffer of(final int capacity) {
        assertArgument(capacity > 0, "The capacity must be greater than zero");
        return WritableBuffer.of(new byte[capacity]);
    }

    /**
     * Create a new {@link WritableBuffer} using the supplied byte-array as the backing buffer along
     * with constraints of which part of this byte-array is accessible.
     *
     * @param buffer the byte-array to use as the backing array
     * @param offset offset into the backing array from which this {@link WritableBuffer} has access. Anything
     *               below that offset is not accessible to this {@link WritableBuffer}.
     * @param length the length, from the offset, of how many bytes in the backing array is accessible to this
     *               {@link WritableBuffer}.
     */
    public static WritableBuffer of(final byte[] buffer, final int offset, final int length) {
        assertArray(buffer, offset, length);
        return new DefaultWritableBuffer(buffer, offset, length);
    }

    private DefaultWritableBuffer(final byte[] buffer, final int offset, final int length) {
        this.buffer = buffer;
        lowerBoundary = offset;
        upperBoundary = offset + length;
        writerIndex = offset;
    }

    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        assertNotDone();
        checkIndex(index);
        buffer[lowerBoundary + index] = (byte) value;
    }

    @Override
    public void setUnsignedShort(final int index, final int value) throws IndexOutOfBoundsException {
        assertNotDone();
        checkIndex(index);
        checkIndex(index + 1);
        final int i = lowerBoundary + index;
        buffer[i] = (byte) (value >> 8);
        buffer[i + 1] = (byte) value;
    }

    @Override
    public void setUnsignedInt(final int index, final long value) throws IndexOutOfBoundsException {
        assertNotDone();
        checkIndex(index);
        checkIndex(index + 3);
        final int i = lowerBoundary + index;
        buffer[i + 3] = (byte) value;
        buffer[i + 2] = (byte) (value >>> 8);
        buffer[i + 1] = (byte) (value >>> 16);
        buffer[i + 0] = (byte) (value >>> 24);
    }

    @Override
    public void setInt(final int index, final int value) throws IndexOutOfBoundsException {
        assertNotDone();
        checkIndex(index);
        checkIndex(index + 3);
        final int i = lowerBoundary + index;
        buffer[i + 0] = (byte) (value >>> 24);
        buffer[i + 1] = (byte) (value >>> 16);
        buffer[i + 2] = (byte) (value >>> 8);
        buffer[i + 3] = (byte) value;
    }

    @Override
    public void setThreeOctetInt(final int index, final int value) throws IndexOutOfBoundsException {
        assertNotDone();
        checkIndex(index);
        checkIndex(index + 2);
        final int i = lowerBoundary + index;
        buffer[i + 0] = (byte) (value >>> 16);
        buffer[i + 1] = (byte) (value >>> 8);
        buffer[i + 2] = (byte) value;
    }

    @Override
    public void setBit(final int index, final int bitNo, final boolean on) throws IndexOutOfBoundsException {
        assertNotDone();
        checkIndex(index);
        final int i = lowerBoundary + index;
        if (on) {
            buffer[i] |= 1 << bitNo;
        } else {
            buffer[i] &= ~(1 << bitNo);
        }
    }

    @Override
    public void setBit0(final int index, final boolean on) throws IndexOutOfBoundsException {
        setBit(index, 0, on);
    }

    @Override
    public void setBit1(final int index, final boolean on) throws IndexOutOfBoundsException {
        setBit(index, 1, on);
    }

    @Override
    public void setBit2(final int index, final boolean on) throws IndexOutOfBoundsException {
        setBit(index, 2, on);
    }

    @Override
    public void setBit3(final int index, final boolean on) throws IndexOutOfBoundsException {
        setBit(index, 3, on);
    }

    @Override
    public void setBit4(final int index, final boolean on) throws IndexOutOfBoundsException {
        setBit(index, 4, on);
    }

    @Override
    public void setBit5(final int index, final boolean on) throws IndexOutOfBoundsException {
        setBit(index, 5, on);
    }

    @Override
    public void setBit6(final int index, final boolean on) throws IndexOutOfBoundsException {
        setBit(index, 6, on);
    }

    @Override
    public void setBit7(final int index, final boolean on) throws IndexOutOfBoundsException {
        setBit(index, 7, on);
    }

    @Override
    public void setByte(final int index, final byte value) throws IndexOutOfBoundsException {
        assertNotDone();
        checkIndex(index);
        buffer[lowerBoundary + index] = value;
    }

    @Override
    public int getWriterIndex() {
        assertNotDone();
        return writerIndex;
    }


    @Override
    public void setWriterIndex(final int index) {
        assertNotDone();
        if (index < 0 || index > upperBoundary - lowerBoundary) {
            throw new IndexOutOfBoundsException();
        }

        this.writerIndex = index;
    }

    @Override
    public int getWritableBytes() {
        assertNotDone();
        return this.upperBoundary - this.writerIndex;
    }

    @Override
    public boolean hasWritableBytes() {
        assertNotDone();
        return getWritableBytes() > 0;
    }

    @Override
    public void write(final byte b) throws IndexOutOfBoundsException{
        assertNotDone();
        checkWriterIndex(writerIndex);
        buffer[lowerBoundary + writerIndex] = b;
        ++writerIndex;
    }

    @Override
    public void write(final byte[] bytes) throws IndexOutOfBoundsException{
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(final byte[] bytes, final int offset, final int length) throws IndexOutOfBoundsException {
        assertNotDone();
        if (!checkWritableBytesSafe(length)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }

        System.arraycopy(bytes, offset, buffer, writerIndex, length);
        writerIndex += length;
    }


    @Override
    public void write(final int value) throws IndexOutOfBoundsException{
        assertNotDone();
        if (!checkWritableBytesSafe(4)) {
            throw new IndexOutOfBoundsException("Unable to write the entire int to this buffer. Nothing was written");
        }
        final int index = lowerBoundary + writerIndex;
        buffer[index + 0] = (byte) (value >>> 24);
        buffer[index + 1] = (byte) (value >>> 16);
        buffer[index + 2] = (byte) (value >>> 8);
        buffer[index + 3] = (byte) value;
        writerIndex += 4;
    }

    @Override
    public void writeThreeOctets(final int value) throws IndexOutOfBoundsException {
        assertNotDone();
        if (!checkWritableBytesSafe(3)) {
            throw new IndexOutOfBoundsException("Unable to write the entire three octet int to this buffer. Nothing was written");
        }
        assertArgument(value >= 0);
        final int i = lowerBoundary + writerIndex;
        buffer[i + 0] = (byte) (value >>> 16);
        buffer[i + 1] = (byte) (value >>> 8);
        buffer[i + 2] = (byte) value;
        writerIndex += 3;
    }

    @Override
    public void write(final long value) throws IndexOutOfBoundsException{
        assertNotDone();
        if (!checkWritableBytesSafe(8)) {
            throw new IndexOutOfBoundsException("Unable to write the entire long to this buffer. Nothing was written");
        }
        final int index = lowerBoundary + writerIndex;
        buffer[index + 0] = (byte)(value >>> 56);
        buffer[index + 1] = (byte)(value >>> 48);
        buffer[index + 2] = (byte)(value >>> 40);
        buffer[index + 3] = (byte)(value >>> 32);
        buffer[index + 4] = (byte)(value >>> 24);
        buffer[index + 5] = (byte)(value >>> 16);
        buffer[index + 6] = (byte)(value >>>  8);
        buffer[index + 7] = (byte)(value >>>  0);
        writerIndex += 8;
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException{
        assertNotDone();
        write(s, "UTF-8");
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException{
        assertNotDone();
        try {
            final byte[] bytes = s.getBytes(charset);
            if (!checkWritableBytesSafe(bytes.length)) {
                throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
            }

            System.arraycopy(bytes, 0, buffer, writerIndex, bytes.length);
            writerIndex += bytes.length;
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Charset " + charset + " is not supported by the platform", e);
        }
    }

    @Override
    public void writeAsString(final int value) throws IndexOutOfBoundsException{
        assertNotDone();
        final int size = value < 0 ? Buffers.stringSize(-value) + 1 : Buffers.stringSize(value);
        if (!checkWritableBytesSafe(size)) {
            throw new IndexOutOfBoundsException();
        }
        Buffers.getBytes(value, lowerBoundary + writerIndex + size, buffer);
        writerIndex += size;
    }

    @Override
    public void writeAsString(final long value) throws IndexOutOfBoundsException {
        assertNotDone();
        final int size = value < 0 ? Buffers.stringSize(-value) + 1 : Buffers.stringSize(value);
        if (!checkWritableBytesSafe(size)) {
            throw new IndexOutOfBoundsException();
        }
        Buffers.getBytes(value, lowerBoundary + writerIndex + size, buffer);
        writerIndex += size;
    }


    @Override
    public WritableBuffer clone() {
        assertNotDone();
        throw new RuntimeException("Haven't implemented this just yet");
    }


    @Override
    public String toString() {
        assertNotDone();
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public int capacity() {
        return upperBoundary - lowerBoundary;
    }


    @Override
    public void zeroOut() {
        assertNotDone();
        zeroOut((byte)0);
    }

    @Override
    public void zeroOut(final byte b) {
        assertNotDone();
        for(int i = lowerBoundary; i < upperBoundary; ++i) {
            buffer[i] = b;
        }
    }

    @Override
    public Buffer build() {
        assertNotDone();
        done = true;
        return Buffer.of(buffer, lowerBoundary, writerIndex);
    }

    private void assertNotDone() {
        if (done) {
            throw new IllegalStateException("This " + WritableBuffer.class.getName() + " has already been built " +
                    "and as such can never be modified again");
        }
    }

    private void checkIndex(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= upperBoundary - lowerBoundary) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkWriterIndex(final int index) throws IndexOutOfBoundsException {
        if (index < this.writerIndex || index >= this.upperBoundary) {
            throw new IndexOutOfBoundsException();
        }
    }

    private boolean checkWritableBytesSafe(final int length) {
        return getWritableBytes() >= length;
    }

}
