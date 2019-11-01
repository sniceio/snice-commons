package io.snice.buffer.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.buffer.ByteNotFoundException;
import io.snice.buffer.ReadWriteBuffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.buffer.WritableBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertArray;

public final class DefaultReadWriteBuffer implements ReadWriteBuffer {

    /**
     * The actual backing buffer.
     */
    private final byte[] buffer;

    /**
     * This is a little devious but instead of inheritence, I started off with delegation just
     * because it felt that it would def guarantee the integretity of the immutable buffer
     * concept and if there was some kind of base class, bugs could easier break that immutability.
     * But, the immutable buffer implementation does contain all the getXXX etc that we do want
     * and we can then sneakily rely on the above backing buffer, to which we write, actually
     * be visible to this wrapping buffer since the byte-array is actually a reference...
     *
     * Sneaky & ugly but it's still within this class so should be good.
     */
    private final ReadableBuffer wrap;

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
    private int lowerBoundary;

    /**
     * Note that the writer index is, like the reader index, zero based against
     * the lowerBoundary.
     */
    private int writerIndex;

    /**
     * Create a new {@link WritableBuffer} with the content of the specified byte-array.
     * It is assumed that all of the supplied bytes have already been "written" to, i.e., this
     * {@link WritableBuffer} will have no more bytes to write, it is "full".
     *
     * If you just wish to create a {@link WritableBuffer} that is empty and of a certain
     * capacity, then just use the
     *
     * @param buffer
     * @return
     */
    public static ReadWriteBuffer of(final byte[] buffer) {
        assertArray(buffer);
        return new DefaultReadWriteBuffer(buffer);
    }

    public static ReadWriteBuffer of(final int capacity) {
        assertArgument(capacity > 0, "The capacity must be greater than zero");
        return new DefaultReadWriteBuffer(capacity);
    }

    public static ReadWriteBuffer of(final byte[] buffer, final int offset, final int length) {
        assertArray(buffer, offset, length);
        return new DefaultReadWriteBuffer(buffer, offset, length);
    }

    private DefaultReadWriteBuffer(final int capacity) {
        this.buffer = new byte[capacity];
        this.wrap = Buffer.of(buffer).toReadableBuffer();
        writerIndex = 0;
        lowerBoundary = 0;
        upperBoundary = capacity;
    }

    private DefaultReadWriteBuffer(final byte[] buffer, final int offset, final int length) {
        this.buffer = buffer;
        this.wrap = Buffer.of(buffer, offset, length).toReadableBuffer();
        lowerBoundary = offset;
        upperBoundary = offset + length;

        // remember that the writer index is a relative index to the view of the buffer
        // which means it is zero based against the lower boundary. Hence, we cannot
        // set it to offset + length since that would be wrong.
        writerIndex = length;
    }

    private DefaultReadWriteBuffer(final byte[] buffer) {
        this.buffer = buffer;
        this.wrap = Buffer.of(buffer).toReadableBuffer();
        writerIndex = buffer.length;
        lowerBoundary = 0;
        upperBoundary = buffer.length;
    }

    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        final int i = lowerBoundary + index;
        checkIndex(i);
        buffer[lowerBoundary + index] = (byte) value;
    }

    @Override
    public void setUnsignedShort(final int index, final int value) throws IndexOutOfBoundsException {
        final int i = lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 1);
        buffer[i] = (byte) (value >> 8);
        buffer[i + 1] = (byte) value;
    }

    @Override
    public void setUnsignedInt(final int index, final long value) throws IndexOutOfBoundsException {
        checkIndex(index);
        checkIndex(index + 3);
        buffer[lowerBoundary + index + 3] = (byte) value;
        buffer[lowerBoundary + index + 2] = (byte) (value >>> 8);
        buffer[lowerBoundary + index + 1] = (byte) (value >>> 16);
        buffer[lowerBoundary + index + 0] = (byte) (value >>> 24);
    }

    @Override
    public void setInt(final int index, final int value) throws IndexOutOfBoundsException {
        checkIndex(index);
        checkIndex(index + 3);
        buffer[lowerBoundary + index + 0] = (byte) (value >>> 24);
        buffer[lowerBoundary + index + 1] = (byte) (value >>> 16);
        buffer[lowerBoundary + index + 2] = (byte) (value >>> 8);
        buffer[lowerBoundary + index + 3] = (byte) value;
    }

    @Override
    public void setBit(final int index, final int bitNo, final boolean on) throws IndexOutOfBoundsException {
        final int i = lowerBoundary + index;
        checkIndex(i);
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
        final int i = lowerBoundary + index;
        checkIndex(i);
        buffer[i] = value;
    }

    @Override
    public int getWriterIndex() {
        return writerIndex;
    }

    @Override
    public void setWriterIndex(final int index) {
        if (capacity() == 0) {
            throw new IndexOutOfBoundsException("The capacity of this buffer is zero, hence, it is effectively empty and as such, you cannot set the writer index to anything");
        }
        // also pay attention to the reader index! See the javadoc
        if (wrap.getReaderIndex() > index) {
            wrap.setReaderIndex(index);
        }
        this.writerIndex = index;
    }

    @Override
    public int getWritableBytes() {
        return this.upperBoundary - this.writerIndex;
    }

    @Override
    public boolean hasWritableBytes() {
        return getWritableBytes() > 0;
    }

    @Override
    public void write(final byte b) throws IndexOutOfBoundsException{
        checkWriterIndex(writerIndex);
        buffer[lowerBoundary + writerIndex] = b;
        ++writerIndex;
    }

    @Override
    public void write(final byte[] bytes) throws IndexOutOfBoundsException{
        if (!checkWritableBytesSafe(bytes.length)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }

        System.arraycopy(bytes, 0, buffer, writerIndex, bytes.length);
        writerIndex += bytes.length;
    }

    @Override
    public void write(final int value) throws IndexOutOfBoundsException{
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
    public void write(final long value) throws IndexOutOfBoundsException{
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
        write(s, "UTF-8");
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException{
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
        final int size = value < 0 ? Buffers.stringSize(-value) + 1 : Buffers.stringSize(value);
        if (!checkWritableBytesSafe(size)) {
            throw new IndexOutOfBoundsException();
        }
        Buffers.getBytes(value, lowerBoundary + writerIndex + size, buffer);
        writerIndex += size;
    }

    @Override
    public void writeAsString(final long value) throws IndexOutOfBoundsException {
        final int size = value < 0 ? Buffers.stringSize(-value) + 1 : Buffers.stringSize(value);
        if (!checkWritableBytesSafe(size)) {
            throw new IndexOutOfBoundsException();
        }
        Buffers.getBytes(value, lowerBoundary + writerIndex + size, buffer);
        writerIndex += size;
    }


    @Override
    public int countWhiteSpace(final int startIndex) {
        return wrap.countWhiteSpace(startIndex);
    }

    @Override
    public Buffer toBuffer() {
        return Buffers.wrap(copyArray());
    }

    @Override
    public ReadableBuffer toReadableBuffer() {
        return Buffers.wrap(copyArray()).toReadableBuffer();
    }

    @Override
    public WritableBuffer toWritableBuffer() {
        throw new RuntimeException("havent done this yet");
    }

    @Override
    public int indexOfSingleCRLF() {
        return wrap.indexOfSingleCRLF();
    }

    @Override
    public Buffer indexOfDoubleCRLF() {
        return wrap.indexOfDoubleCRLF();
    }

    @Override
    public boolean isEmpty() {
        return getReadableBytes() == 0;
    }

    @Override
    public int capacity() {
        return upperBoundary - lowerBoundary;
    }

    @Override
    public int indexdOfSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        return wrap.indexdOfSafe(maxBytes, bytes);
    }

    @Override
    public int indexOf(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        return wrap.indexOf(maxBytes, bytes);
    }

    @Override
    public int indexOf(final int startIndex, final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        return wrap.indexOf(startIndex, maxBytes, bytes);
    }

    @Override
    public int indexOf(final byte b) throws ByteNotFoundException, IllegalArgumentException {
        return wrap.indexOf(b);
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        wrap.writeTo(out);
    }

    @Override
    public Buffer slice(final int start, final int stop) throws IndexOutOfBoundsException, IllegalArgumentException {
        return wrap.slice(start, stop);
    }

    @Override
    public Buffer slice(final int stop) {
        return wrap.slice(stop);
    }

    @Override
    public Buffer slice() {
        return sliceToSize();
    }

    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException {
        return wrap.getByte(index);
    }

    @Override
    public int getInt(final int index) throws IndexOutOfBoundsException {
        return wrap.getInt(index);
    }

    @Override
    public long getLong(final int index) throws IndexOutOfBoundsException {
        return wrap.getLong(index);
    }

    @Override
    public int getIntFromThreeOctets(final int index) throws IndexOutOfBoundsException {
        return wrap.getIntFromThreeOctets(index);
    }

    @Override
    public long getUnsignedInt(final int index) throws IndexOutOfBoundsException {
        return wrap.getUnsignedInt(index);
    }

    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException {
        return wrap.getShort(index);
    }

    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        return wrap.getUnsignedShort(index);
    }

    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        return wrap.getUnsignedByte(index);
    }

    @Override
    public int parseToInt() throws NumberFormatException {
        return wrap.parseToInt();
    }

    @Override
    public int parseToInt(final int radix) throws NumberFormatException {
        return wrap.parseToInt(radix);
    }

    @Override
    public boolean endsWith(final byte[] content) throws IllegalArgumentException {
        return wrap.endsWith(content);
    }

    @Override
    public boolean endsWith(final byte b) {
        return wrap.endsWith(b);
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2) {
        return wrap.endsWith(b1, b2);
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3) {
        return wrap.endsWith(b1, b2, b3);
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3, final byte b4) {
        return wrap.endsWith(b1, b2, b3, b4);
    }

    @Override
    public String dumpAsHex() {
        return wrap.dumpAsHex();
    }

    @Override
    public ReadWriteBuffer clone() {
        throw new RuntimeException("Haven't implemented this just yet");
    }


    @Override
    public String toString() {
        return toUTF8String();
    }

    @Override
    public String toUTF8String() {
        return wrap.slice(writerIndex).toString();
    }

    @Override
    public int getReaderIndex() {
        return wrap.getReaderIndex();
    }

    @Override
    public ReadableBuffer setReaderIndex(final int index) {

        // for the case when we have build() this buffer and as such,
        // we can no longer actually use it.
        if (capacity() == 0) {
            throw new IndexOutOfBoundsException("The capacity of this buffer is zero, hence, it is effectively empty and as such, you cannot set the reader index to anything");
        }

        assertArgument(index < writerIndex, "The reader index cannot be greater than that of the writer index");
        wrap.setReaderIndex(index);
        return this;
    }

    @Override
    public ReadableBuffer markReaderIndex() {
        wrap.markReaderIndex();
        return this;
    }

    @Override
    public ReadableBuffer resetReaderIndex() {
        wrap.resetReaderIndex();
        return this;
    }

    @Override
    public byte readByte() throws IndexOutOfBoundsException {
        checkReadableBytes(1);
        return wrap.readByte();
    }

    @Override
    public byte peekByte() throws IndexOutOfBoundsException {
        return wrap.peekByte();
    }

    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        checkReadableBytes(4);
        return wrap.readUnsignedInt();
    }

    @Override
    public int readInt() throws IndexOutOfBoundsException {
        checkReadableBytes(4);
        return wrap.readInt();
    }

    @Override
    public long readLong() throws IndexOutOfBoundsException {
        checkReadableBytes(8);
        return wrap.readLong();
    }

    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException {
        checkReadableBytes(length);
        return wrap.readBytes(length);
    }

    @Override
    public Buffer readLine() {
        // TODO: will need to re-implement this one since it doesn't take the
        // writer index into account and it is hard to do when you do not know
        // how far in we are going to read.
        return wrap.readLine();
    }

    @Override
    public Buffer readUntilSingleCRLF() {
        return wrap.readUntilSingleCRLF();
    }

    @Override
    public Buffer readUntilDoubleCRLF() {
        return wrap.readUntilDoubleCRLF();
    }

    @Override
    public int getReadableBytes() {
        // note that we cannot use the wrapped {@link ReadableBuffer} here
        // because it doesn't take into consideration the writer index, only
        // the upper boundary.
        // return writerIndex - wrap.getReaderIndex() - lowerBoundary;
        return writerIndex - wrap.getReaderIndex();
    }

    @Override
    public boolean hasReadableBytes() {
        return getReadableBytes() > 0;
    }

    @Override
    public Buffer readUntilWhiteSpace() {
        return wrap.readUntilWhiteSpace();
    }

    @Override
    public Buffer readUntil(final byte b) throws ByteNotFoundException {
        // TODO: may be a bug here in general, not just this one...
        final int maxBytes = getReadableBytes();
        return wrap.readUntil(4096, b);
    }

    @Override
    public Buffer readUntil(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        return wrap.readUntil(Math.min(maxBytes, getReadableBytes()), bytes);
    }

    @Override
    public Buffer readUntilSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        return wrap.readUntilSafe(Math.min(maxBytes, getReadableBytes()), bytes);
    }

    @Override
    public int readUnsignedShort() throws IndexOutOfBoundsException {
        checkReadableBytes(2);
        return wrap.readUnsignedShort();
    }

    @Override
    public short readShort() throws IndexOutOfBoundsException {
        checkReadableBytes(2);
        return wrap.readShort();
    }

    @Override
    public short readUnsignedByte() throws IndexOutOfBoundsException {
        checkReadableBytes(1);
        return wrap.readUnsignedByte();
    }

    @Override
    public void zeroOut() {
        zeroOut((byte)0);
    }

    @Override
    public void zeroOut(final byte b) {
        for(int i = lowerBoundary; i < upperBoundary; ++i) {
            buffer[i] = b;
        }
    }

    @Override
    public Buffer build() {
        final int length = getReadableBytes();
        // final byte[] array = new byte[length];
        // System.arraycopy(buffer, lowerBoundary + wrap.getReaderIndex(), array, 0, length);

        final Buffer b = Buffer.of(buffer, lowerBoundary + wrap.getReaderIndex(), length);
        wrap.setReaderIndex(upperBoundary);
        writerIndex = upperBoundary;
        lowerBoundary = upperBoundary;

        return b;
    }


    private void checkIndex(final int index) throws IndexOutOfBoundsException {
        if (index >= lowerBoundary + capacity()) {
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

    /**
     * Whenever we e.g. create a new slice we must copy the current visible buffer, as
     * depicted by the reader/writer positions since we cannot share the underlying
     * byte-array outside of this {@link WritableBuffer}.
     */
    private byte[] copyArray() {
        final int length = getReadableBytes();
        final byte[] array = new byte[length];
        System.arraycopy(buffer, lowerBoundary + wrap.getReaderIndex(), array, 0, length);
        return array;
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

    /**
     * Doing equal operations on a {@link WritableBuffer} is not perhaps a normal thing, or at least
     * it shouldn't, since it is a highly mutable structure. As such, the below default implementation will
     * not be fast since we only care about accuracy for now.
     *
     * @param b
     * @return
     */
    @Override
    public boolean equalsIgnoreCase(final Object b) {
        if (this == b) {
            return true;
        }

        if (b instanceof DefaultReadWriteBuffer) {
            final DefaultReadWriteBuffer other = (DefaultReadWriteBuffer) b;
            return wrap.equalsIgnoreCase(other.wrap);
        }

        if (b instanceof DefaultReadableBuffer) {
            final DefaultReadableBuffer other = (DefaultReadableBuffer) b;
            return wrap.equalsIgnoreCase(other);
        }

        if (b instanceof DefaultImmutableBuffer) {
            final DefaultImmutableBuffer other = (DefaultImmutableBuffer) b;
            return sliceToSize().equalsIgnoreCase(other);
        }

        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        try {
            final Buffer me = sliceToSize();
            if (o instanceof WritableBuffer) {
                final Buffer that = ((DefaultReadWriteBuffer)o).sliceToSize();
                return Objects.equals(me, that);
            }

            return Objects.equals(me, o);
        } catch (final ClassCastException e) {
            return false;
        }
    }

    private Buffer sliceToSize() {
        return wrap.slice(writerIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sliceToSize());
    }
}
