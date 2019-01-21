/**
 * 
 */
package io.snice.buffer.old;

import io.snice.buffer.WriteNotSupportedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public final class InputStreamBuffer extends AbstractBuffer {

    private static final String CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER = "Cannot write to an InputStreamBuffer";

    private static final String NOT_IMPLEMENTED_JUST_YET = "Not implemented just yet";

    private final InputStream is;

    /**
     * The default capacity for each individual byte array
     */
    private static final int DEFAULT_CAPACITY = 4096;

    private final List<java.nio.ByteBuffer> storage;

    /**
     * The "local" capacity of each "sub-array".
     */
    private final int localCapacity;

    /**
     * 
     */
    public InputStreamBuffer(final InputStream is) {
        this(InputStreamBuffer.DEFAULT_CAPACITY, is);
    }

    /**
     * 
     * @param initialCapacity
     *            the initial size of the internal byte array
     * @param is
     */
    public InputStreamBuffer(final int initialCapacity, final InputStream is) {
        super(0, 0, 0, 0);
        assert is != null;
        this.is = is;
        localCapacity = initialCapacity;
        storage = new ArrayList<java.nio.ByteBuffer>();
        storage.add(java.nio.ByteBuffer.allocate(localCapacity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OldBuffer slice(final int start, final int stop) {
        checkIndex(lowerBoundary + start);
        checkIndex(lowerBoundary + stop - 1);

        // this has to change now that we can have multiple
        // rows of byte buffers
        final java.nio.ByteBuffer buf = getWritingRow();
        final int upperBoundary = lowerBoundary + stop;
        final int writerIndex = upperBoundary;
        return new ByteBuffer(0, lowerBoundary + start, upperBoundary, writerIndex, buf.array());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    @Override
    public byte readByte() throws IndexOutOfBoundsException, IOException {
        final int read = internalReadBytes(1);
        if (read == -1) {
            // not sure this is really the right thing to do
            throw new IndexOutOfBoundsException();
        }
        return getByte(readerIndex++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte peekByte() throws IndexOutOfBoundsException, IOException {
        final int read = internalReadBytes(1);
        if (read == -1) {
            // not sure this is really the right thing to do
            throw new IndexOutOfBoundsException();
        }
        return getByte(readerIndex);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    @Override
    public OldBuffer readBytes(final int length) throws IndexOutOfBoundsException, IOException {
        if (!checkReadableBytesSafe(length)) {
            final int availableBytes = getReadableBytes();
            final int read = internalReadBytes(length - availableBytes);
            if (read == -1) {
                // end-of-file
                return null;
            } else if (read + availableBytes < length) {
                // do something else?
                throw new IndexOutOfBoundsException("Not enough bytes left in the stream. Wanted " + length
                        + " but only read " + read);
            }
        }

        // perhaps we should create a composite buffer instead of this
        // copying???
        int index = 0;
        final byte[] buf = new byte[length];
        while (index < length) {
            final int spaceLeft = getAvailableLocalReadingSpace();
            final int readAtMost = Math.min(length - index, spaceLeft);
            final int localIndex = getLocalReaderIndex();

            final java.nio.ByteBuffer bb = getReadingRow();
            System.arraycopy(bb.array(), localIndex, buf, index, readAtMost);
            readerIndex += readAtMost;
            index += readAtMost;
        }
        return Buffers.wrap(buf);

    }

    /**
     * Read at most <code>length</code> no of bytes and store it into the
     * internal buffer. This method is blocking in case we don't have enough
     * bytes to read
     * 
     * @param length
     *            the amount of bytes we wishes to read
     * @return the actual number of bytes read.
     * @throws IOException
     */
    private int internalReadBytes(final int length) throws IOException {

        // check if we already have enough bytes available for reading
        // and if so, just return the length the user is asking for
        if (checkReadableBytesSafe(length)) {
            return length;
        }

        return readFromStream(length);

    }

    /**
     * Since the writer index (upper boundary) is where we are to write for the
     * entire buffer we need to translate this into the local index into the
     * array we currently are working with.
     * 
     * @return
     */
    private int getLocalWriterIndex() {
        return writerIndex % localCapacity;
    }

    /**
     * Translates the global reader index into the local index within a row
     * 
     * @return
     */
    private int getLocalReaderIndex() {
        return readerIndex % localCapacity;
    }

    /**
     * Since the underlying storage for this buffer is essentially a 2-D byte
     * array we sometimes need to find out how much capacity is left in a
     * particular row.
     * 
     * @return
     */
    private int getAvailableLocalWritingSpace() {
        return localCapacity - getLocalWriterIndex();
    }

    /**
     * Find out how many bytes are left to read in the current row
     * 
     * @return
     */
    private int getAvailableLocalReadingSpace() {
        return localCapacity - getLocalReaderIndex();
    }

    /**
     * Get which "row" we currently are working with for writing
     * 
     * @return
     */
    private java.nio.ByteBuffer getWritingRow() {
        final int row = writerIndex / localCapacity;
        if (row >= storage.size()) {
            final java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(localCapacity);
            storage.add(buf);
            return buf;
        }

        return storage.get(row);
    }

    /**
     * Get which "row" we currently are working with for reading
     * 
     * @return
     */
    private java.nio.ByteBuffer getReadingRow() {
        final int row = readerIndex / localCapacity;
        return storage.get(row);
    }

    /**
     * Method for reading bytes off the stream and store it in the local
     * "storage"
     * 
     * @param length
     *            the length we wish to read
     * @return the actual amount of bytes we read
     * @throws IOException
     *             in case anything goes wrong while reading
     */
    private int readFromStream(final int length) throws IOException {
        int total = 0;
        int actual = 0;
        while (total < length && actual != -1) {

            final int localIndex = getLocalWriterIndex();
            final int spaceLeft = getAvailableLocalWritingSpace();
            final int readAtMost = Math.min(length - total, spaceLeft);

            final java.nio.ByteBuffer bb = getWritingRow();

            actual = is.read(bb.array(), localIndex, readAtMost);

            if (actual > 0) {
                upperBoundary += actual;
                writerIndex = upperBoundary;
                total += actual;
            }
        }
        return total;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReadableBytes() {
        return super.getReadableBytes();
        // return this.upperBoundary - this.readerIndex;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    @Override
    public boolean hasReadableBytes() {
        if (!checkReadableBytesSafe(1)) {
            try {
                // if we don't have any bytes available for reading
                // then try and read a bunch at the same time. However,
                // we are satisfied if we can only read one byte
                return internalReadBytes(100) >= 1;
            } catch (final IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArray() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException, IOException {
        checkIndex(lowerBoundary + index);
        final java.nio.ByteBuffer bb = getWritingRow();
        // this has to be localized as well
        return bb.get(lowerBoundary + index);
    }

    /**
     * Convenience method for checking if we can get the byte at the specified
     * index. If we can't, then we will try and read the missing bytes off of
     * the underlying {@link InputStream}. If that fails, e.g. we don't ready
     * enough bytes off of the stream, then we will eventually throw an
     * {@link IndexOutOfBoundsException}
     * 
     * @param index
     *            the actual index to check. I.e., this is the actual index in
     *            our byte array, irrespective of what the lowerBoundary is set
     *            to.
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    @Override
    protected void checkIndex(final int index) throws IndexOutOfBoundsException {
        final int missingBytes = index + 1 - (lowerBoundary + capacity());
        if (missingBytes <= 0) {
            // we got all the bytes needed
            return;
        }

        try {
            final int read = readFromStream(missingBytes);
            if (read == -1 || read < missingBytes) {
                throw new IndexOutOfBoundsException();
            }
        } catch (final IOException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(final int index) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort() throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dumpAsHex() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setByte(final int index, final byte value) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setUnsignedShort(final int index, final int value) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
    }

    @Override
    public OldBuffer clone() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        try {
            final OldBuffer b = (OldBuffer) other;

            // should we care about how far we may have read into
            // the two buffers? For now we will...
            // Also, we may want to implement our own array compare
            // since now the two arrays will be copied, which is kind
            // of stupid but for now that is ok. Will worry about potential
            // bottlenecks later. Issue has been added to the tracker to keep
            // track of this...
            return Arrays.equals(getArray(), b.getArray());
        } catch (final ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean equalsIgnoreCase(final Object other) {
        throw new RuntimeException("Sorry, InputStreamBuffer.equalsIgnoreCase isn't implemented yet");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        // TODO: actually go over the array instead of copying like this
        return Arrays.hashCode(getArray());
    }

    @Override
    public String toString() {
        // perhaps not the most efficient way? but it works
        // so for now we'll leave it as this until proven
        // slow
        final OldBuffer b = slice();
        return b.toString();
    }

    @Override
    public String toUTF8String() {
        return slice().toUTF8String();
    }

    @Override
    public int getWritableBytes() {
        return 0;
    }

    @Override
    public boolean hasWritableBytes() {
        return false;
    }

    @Override
    public void write(final byte[] bytes) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException("Cannot write to an InputStreamBuffer");
    }

    public void getBytes() {

    }

    @Override
    public void getBytes(final OldBuffer dst) {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void getBytes(final byte[] dst) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void getBytes(final int index, final OldBuffer dst) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void setInt(final int index, final int value) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public long getUnsignedInt(final int index) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void write(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException(CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER);
    }

    @Override
    public void write(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException(CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER);
    }

    @Override
    public void writeAsString(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException(CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER);
    }

    @Override
    public void writeAsString(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException(CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER);
    }

    @Override
    public void setUnsignedInt(final int index, final long value) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub

    }

}
