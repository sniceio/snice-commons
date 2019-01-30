package io.snice.buffer.impl;

import com.google.polo.pairing.HexDump;
import io.snice.buffer.Buffer;
import io.snice.buffer.ByteNotFoundException;
import io.snice.buffer.ReadableBuffer;
import io.snice.buffer.WritableBuffer;
import io.snice.preconditions.PreConditions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertArray;

/**
 * The default implementation of our immutable buffer. This implementation
 * is also being used as a base class, which is why it has methods
 * for reader-index etc.
 */
public class DefaultImmutableBuffer implements Buffer {

    public static Buffer of(final byte[] buffer) {
        assertArray(buffer);
        if (buffer.length == 0) {
            return EmptyBuffer.EMPTY;
        }

        return new DefaultImmutableBuffer(buffer, 0, buffer.length);
    }

    public static Buffer of(final byte[] buffer, final int offset, final int length) {
        assertArray(buffer, offset, length);
        if (buffer.length == 0) {
            return EmptyBuffer.EMPTY;
        }

        return new DefaultImmutableBuffer(buffer, offset, offset + length);
    }

    /**
     * The actual buffer
     */
    private final byte[] buffer;

    /**
     * Used when slicing out portions of the buffer.
     * The lower boundary is inclusive.
     */
    private final int lowerBoundary;

    /**
     * The upper boundary of the "window" of data this buffer is allowed to "see".
     */
    private final int upperBoundary;

    private DefaultImmutableBuffer(final byte[] buffer, final int lowerBoundary, final int upperBoundary) {
        this.buffer = buffer;
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
    }

    @Override
    public Buffer toBuffer() {
        return this;
    }

    @Override
    public ReadableBuffer toReadableBuffer() {
        return DefaultReadableBuffer.of(this);
    }

    @Override
    public WritableBuffer toWritableBuffer() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public int indexOfSingleCRLF() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public Buffer indexOfDoubleCRLF() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean isEmpty() {
        return capacity() == 0;
    }

    @Override
    public int capacity() {
        return this.upperBoundary - this.lowerBoundary;
    }

    @Override
    public int indexdOfSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Just because the underlying buffer is of a certain size doesn't mean that all of those
     * bytes are available to this particular slice.
     *
     * @return the total readable bytes, which is the number of bytes in our "window"
     */
    private int getReadableBytes() {
        return upperBoundary - lowerBoundary;
    }

    public static boolean hasReadableBytes() {
        // when creating this buffer we will check if the passed in
        // array is empty and if so, we'll actually return an empty buffer
        // instead so this is safe.
        return true;
    }

    @Override
    public int indexOf(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        assertArgument(maxBytes > 0, "The max bytes must be at least 1");
        assertArgument(bytes.length > 0, "No bytes specified. Not sure what you want me to look for");

        final int capacity = capacity();
        int index = 0;


        while (hasReadableBytes() && (index < capacity) && (maxBytes > index)) {
            if (isByteInArray(getByte(index), bytes)) {
                return index;
            }
            ++index;
        }

        throw new ByteNotFoundException(capacity, bytes);
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final int length = getReadableBytes();
        out.write(buffer, lowerBoundary, length);
    }

    @Override
    public int indexOf(final byte b) throws ByteNotFoundException, IllegalArgumentException {
        return this.indexOf(4096, b);
    }

    @Override
    public Buffer slice(final int start, final int stop) throws IndexOutOfBoundsException, IllegalArgumentException {
        PreConditions.assertArgument(start >= 0, "The start index must be greater than zero");
        PreConditions.assertArgument(stop >= start, "The stop index (" + stop + ") must be greater or equal " +
                "to the start (" + start + ") index");
        if (start == stop) {
            return EmptyBuffer.EMPTY;
        }
        checkIndex(lowerBoundary + start);
        checkIndex(lowerBoundary + stop - 1);
        final int upperBoundary = lowerBoundary + stop;
        return new DefaultImmutableBuffer(buffer, lowerBoundary + start, upperBoundary);
    }

    @Override
    public Buffer slice(final int stop) {
        return slice(0, stop);
    }

    @Override
    public Buffer slice() {
        return this;
    }

    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException {
        checkIndex(lowerBoundary + index);
        return buffer[lowerBoundary + index];
    }

    @Override
    public int getInt(final int index) throws IndexOutOfBoundsException {
        final int i = lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 3);
        return Buffer.signedInt(buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3]);
    }

    @Override
    public long getUnsignedInt(final int index) throws IndexOutOfBoundsException {
        final int i = lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 3);
        return Buffer.unsignedInt(buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3]);
    }

    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException {
        final int i = lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 1);

        // big endian
        return (short) (buffer[i] << 8 | buffer[i + 1] & 0xFF);

        // little endian
        // return (short) (this.buffer[i] & 0xFF | this.buffer[i + 1] << 8);
    }

    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        return getShort(index) & 0xFFFF;
    }

    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        return (short) (getByte(index) & 0xFF);
    }

    @Override
    public int parseToInt() throws NumberFormatException {
        return parseToInt(10);
    }

    @Override
    public boolean endsWith(final byte[] content) throws IllegalArgumentException {
        assertArray(content);
        assertArgument(content.length > 0, "The byte-array cannot be empty");
        if (content.length > capacity()) {
            return false;
        }

        final int length = content.length;
        for (int i = 0; i < length; ++i) {
            if (content[i] != buffer[upperBoundary - length + i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean endsWith(final byte b) throws IllegalArgumentException {
        return buffer[upperBoundary - 1] == b;
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2) throws IllegalArgumentException {
        if (capacity() < 2) {
            return false;
        }

        return buffer[upperBoundary - 2] == b1
                && buffer[upperBoundary - 1] == b2;
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3) throws IllegalArgumentException {
        if (capacity() < 3) {
            return false;
        }

        return buffer[upperBoundary - 3] == b1
                && buffer[upperBoundary - 2] == b2
                && buffer[upperBoundary - 1] == b3;
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3, final byte b4) throws IllegalArgumentException {
        if (capacity() < 4) {
            return false;
        }

        return buffer[upperBoundary - 4] == b1
                && buffer[upperBoundary - 3] == b2
                && buffer[upperBoundary - 2] == b3
                && buffer[upperBoundary - 1] == b4;
    }

    @Override
    public String dumpAsHex() {
        return HexDump.dumpHexString(buffer, lowerBoundary, upperBoundary - lowerBoundary);
    }

    @Override
    public Buffer clone() {
        return this;
    }

    @Override
    public boolean equalsIgnoreCase(final Object other) {
        return internalEquals(true, other);
    }

    @Override
    public String toString() {
        return toUTF8String();
    }

    @Override
    public String toUTF8String() {
        try {
            final int length = getReadableBytes();
            return new String(buffer, lowerBoundary, length, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = lowerBoundary; i < upperBoundary; ++i) {
            result = 31 * result + buffer[i];
        }
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        return internalEquals(false, other);
    }

    private boolean internalEquals(final boolean ignoreCase, final Object other) {
        try {
            if (this == other) {
                return true;
            }
            final DefaultImmutableBuffer b = (DefaultImmutableBuffer) other;
            if (getReadableBytes() != b.getReadableBytes()) {
                return false;
            }

            final int length = getReadableBytes();
            for (int i = 0; i < length; ++i) {
                final byte a1 = buffer[lowerBoundary + i];
                final byte b1 = b.buffer[b.lowerBoundary + i];
                // Do a UTF-8-aware, possibly case-insensitive character match. Only considers
                // case of 7-bit ASCII characters 'a'-'z'. In UTF-8, all bytes of multi-byte
                // characters have thier most signifcant bit set, so they won't be erroneously
                // considered by this algorithm since they won't fall in the range 0x41-0x5a/
                // 0x61-0x7a.

                // This algorithm won't work with UTF-16, and could misfire on malformed UTF-8,
                // e.g. the first byte of a UTF-8 sequence marks the beginning of a multi-byte
                // sequence but the second byte does not have the two high-order bits set to 10.

                // For 7-bit ascii leters, upper and lower-case only differ by one bit,
                // i.e. 'A' is 0x41, and 'a' is 0x61. We need only compare the 5 least
                // signifcant bits.

                if (a1 != b1) {
                    if (ignoreCase &&
                            ((a1 >= 'A' && a1 <= 'Z') || (a1 >= 'a' && a1 <= 'z')) &&
                            ((b1 >= 'A' && b1 <= 'Z') || (b1 >= 'a' && b1 <= 'z')) &&
                            (a1 & 0x1f) == (b1 & 0x1f)) {
                        continue;
                    }
                    return false;
                }
            }

            return true;
        } catch (final NullPointerException | ClassCastException e) {
            return false;
        }
    }

    /**
     * (Copied from the Integer class and slightly altered to read from this
     * buffer instead of a String)
     *
     * Parses the string argument as a signed integer in the radix specified by
     * the second argument. The characters in the string must all be digits of
     * the specified radix (as determined by whether
     * {@link java.lang.Character#digit(char, int)} returns a nonnegative
     * value), except that the first character may be an ASCII minus sign
     * <code>'-'</code> (<code>'&#92;u002D'</code>) to indicate a negative
     * value. The resulting integer value is returned.
     * <p>
     * An exception of type <code>NumberFormatException</code> is thrown if any
     * of the following situations occurs:
     * <ul>
     * <li>The first argument is <code>null</code> or is a string of length
     * zero.
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or larger than
     * {@link java.lang.Character#MAX_RADIX}.
     * <li>Any character of the string is not a digit of the specified radix,
     * except that the first character may be a minus sign <code>'-'</code> (
     * <code>'&#92;u002D'</code>) provided that the string is longer than length
     * 1.
     * <li>The value represented by the string is not a value of type
     * <code>int</code>.
     * </ul>
     * <p>
     * Examples: <blockquote>
     *
     * <pre>
     * parseInt("0", 10) returns 0
     * parseInt("473", 10) returns 473
     * parseInt("-0", 10) returns 0
     * parseInt("-FF", 16) returns -255
     * parseInt("1100110", 2) returns 102
     * parseInt("2147483647", 10) returns 2147483647
     * parseInt("-2147483648", 10) returns -2147483648
     * parseInt("2147483648", 10) throws a NumberFormatException
     * parseInt("99", 8) throws a NumberFormatException
     * parseInt("Kona", 10) throws a NumberFormatException
     * parseInt("Kona", 27) returns 411787
     * </pre>
     *
     * </blockquote>
     *
     * @param radix
     *            the radix to be used while parsing <code>s</code>.
     * @return the integer represented by the string argument in the specified
     *         radix.
     * @exception NumberFormatException
     *                if the <code>String</code> does not contain a parsable
     *                <code>int</code>.
     */
    @Override
    public final int parseToInt(final int radix) throws NumberFormatException {
        if (isEmpty()) {
            throw new NumberFormatException("Buffer is empty, cannot convert it to an integer");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = 0;

        final int max = getReadableBytes();
        final int limit;
        final int multmin;
        int digit;

        if (max > 0) {
            if (getByte(i) == (byte) '-') {
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                limit = -Integer.MAX_VALUE;
            }
            multmin = limit / radix;
            if (i < max) {
                digit = Character.digit((char) getByte(i++), radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) getByte(i++), radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                if (result < multmin) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + this + "\"");
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else { /* Only got "-" */
                throw new NumberFormatException("For input string: \"" + this + "\"");
            }
        } else {
            return -result;
        }
    }

    protected static boolean isByteInArray(final byte b, final byte[] bytes) {
        for (final byte x : bytes) {
            if (x == b) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method for checking if we can read at the index
     *
     * @param index
     * @throws IndexOutOfBoundsException
     */
    protected void checkIndex(final int index) throws IndexOutOfBoundsException {
        if (index >= this.lowerBoundary + capacity()) {
            throw new IndexOutOfBoundsException();
        }
    }
}
