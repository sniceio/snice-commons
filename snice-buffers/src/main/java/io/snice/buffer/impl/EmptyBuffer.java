package io.snice.buffer.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.ByteNotFoundException;
import io.snice.buffer.ReadableBuffer;
import io.snice.buffer.WritableBuffer;

import java.io.IOException;
import java.io.OutputStream;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class EmptyBuffer implements ReadableBuffer {

    private static final String NOT_ENOUGH_READABLE_BYTES = "Not enough readable bytes";
    private static final String THIS_BUFFER_IS_EMPTY = "This buffer is empty";
    private static final String NUMBER_FORMAT_ERROR = "This buffer is empty and therefore cannot be parsed as an integer";

    public static final Buffer EMPTY = new EmptyBuffer();

    private EmptyBuffer() {
        // only one is really needed to of (well, one per class loader I guess
        // will be the actual end result)
    }

    @Override
    public int indexOfWhiteSpace(final int startIndex) {
        return -1;
    }

    @Override
    public int indexOfWhiteSpace() {
        return -1;
    }

    @Override
    public int countWhiteSpace(final int startIndex) {
        return 0;
    }

    @Override
    public Buffer toBuffer() {
        return this;
    }

    @Override
    public boolean endsWith(final byte[] content) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean endsWith(final byte b) {
        return false;
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2) {
        return false;
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3) {
        return false;
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3, final byte b4) {
        return false;
    }

    @Override
    public boolean startsWith(final Buffer other) throws IllegalArgumentException{
        assertNotNull(other, "The other buffer cannot be null");
        return other.isEmpty();
    }

    @Override
    public boolean startsWithIgnoreCase(final Buffer other) {
        assertNotNull(other, "The other buffer cannot be null");
        return other.isEmpty();
    }

    @Override
    public ReadableBuffer toReadableBuffer() {
        return this;
    }

    @Override
    public WritableBuffer toWritableBuffer() {
        throw new RuntimeException("Not implementd yet");
    }

    @Override
    public int indexOfSingleCRLF() {
        return -1;
    }

    @Override
    public Buffer indexOfDoubleCRLF() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public int indexdOfSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        return -1;
    }

    @Override
    public int indexOf(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        return -1;
    }

    @Override
    public int indexOf(final int startIndex, final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException, IndexOutOfBoundsException {
        return -1;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        // we're an empty buffer so nothing to write...
    }

    @Override
    public void writeTo(final WritableBuffer out) {
        // we're an empty buffer so nothing to write...
    }

    @Override
    public int indexOf(final byte b) throws ByteNotFoundException, IllegalArgumentException {
        return -1;
    }

    @Override
    public int countOccurences(final int startIndex, final int maxbytes, final byte b) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public Buffer slice(final int start, final int stop) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (start != 0 && stop != 0) {
            throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
        }
        return this;
    }

    @Override
    public Buffer slice(final int stop) {
        if (stop != 0) {
            throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
        }
        return this;
    }

    @Override
    public Buffer slice() {
        return this;
    }

    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public int getInt(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public long getLong(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public int getIntFromThreeOctets(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public long getLongFromFiveOctets(int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public long getUnsignedInt(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(THIS_BUFFER_IS_EMPTY);
    }

    @Override
    public int parseToInt() throws NumberFormatException {
        throw new NumberFormatException(NUMBER_FORMAT_ERROR);
    }

    @Override
    public int parseToInt(final int radix) {
        throw new NumberFormatException(NUMBER_FORMAT_ERROR);
    }

    @Override
    public String dumpAsHex() {
        return "";
    }

    @Override
    public int getReaderIndex() {
        return 0;
    }

    @Override
    public ReadableBuffer setReaderIndex(final int index) {
        return this;
    }

    @Override
    public ReadableBuffer markReaderIndex() {
        return this;
    }

    @Override
    public ReadableBuffer resetReaderIndex() {
        return null;
    }

    @Override
    public byte readByte() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public byte peekByte() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public int readInt() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public int readIntFromThreeOctets() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public long readLong() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException {
        if (length == 0) {
            return this;
        }
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public Buffer readLine() {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public Buffer readUntilSingleCRLF() {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public Buffer readUntilDoubleCRLF() {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public int getReadableBytes() {
        return 0;
    }

    @Override
    public boolean hasReadableBytes() {
        return false;
    }

    @Override
    public Buffer readUntilWhiteSpace() {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public Buffer readUntil(final byte b) throws ByteNotFoundException {
        throw new ByteNotFoundException(b);
    }

    @Override
    public Buffer readUntil(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public Buffer readUntilSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public int readUnsignedShort() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public short readShort() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public short readUnsignedByte() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(NOT_ENOUGH_READABLE_BYTES);
    }

    @Override
    public ReadableBuffer clone() {
        return this;
    }

    @Override
    public boolean equalsIgnoreCase(final Object b) {
        try {
            return b instanceof EmptyBuffer || ((Buffer) b).isEmpty();
        } catch (final ClassCastException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public String toUTF8String() {
        return "";
    }
}
