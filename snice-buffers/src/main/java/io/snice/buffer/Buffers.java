package io.snice.buffer;

import io.snice.buffer.impl.DefaultImmutableBuffer;
import io.snice.buffer.impl.EmptyBuffer;
import io.snice.net.IPv4;
import io.snice.preconditions.PreConditions;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static io.snice.preconditions.PreConditions.*;

public final class Buffers {

    private final static byte[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4',
            '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6',
            '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8',
            '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', };

    private final static byte[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
            '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', };

    /**
     * All possible chars for representing a number as a String
     */
    private final static byte[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private final static int[] sizeTable = {
            9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

    private Buffers() {
        // left empty intentionally because this class should not be able
        // to be instantiated.
    }

    /**
     * Wrap the supplied byte array specifying the allowed range of visible
     * bytes.
     *
     * @param buffer
     * @param lowerBoundary
     *            the index of the lowest byte that is accessible to this OldBuffer
     *            (zero based index)
     * @param upperBoundary
     *            the upper oundary (exclusive) of the range of visible bytes.
     * @return
     */
    public static Buffer wrap(final byte[] buffer, final int lowerBoundary, final int upperBoundary) {
        return Buffer.of(buffer, lowerBoundary, upperBoundary - lowerBoundary);
    }

    public static Buffer wrap(final byte... buffer) {
        return Buffer.of(buffer);
    }

    /**
     * Create a new {@link Buffer} of the given size and fill it with
     * a random set of bytes.
     *
     * @param size the size of the buffer, must be greater than zero.
     */
    public static Buffer random(final int size) {
        assertArgument(size > 0, "The size of the randomized buffer must be greater than zero");
        final byte[] bytes = new byte[size];
        ThreadLocalRandom.current().nextBytes(bytes);
        return Buffers.wrap(bytes);
    }

    public static Buffer wrap(final byte buffer) {
        return Buffer.of(buffer);
    }

    public static Buffer wrapAsTbcd(final String tbcd) {
        PreConditions.assertNotEmpty(tbcd, "The TBCD string cannot be null or the empty string");
        final var buffer = WritableBuffer.of(tbcd.length() / 2 + tbcd.length() % 2).fastForwardWriterIndex();
        for (int i = 0; i < buffer.capacity(); ++i) {
            final var digit1 = tbcd.charAt(i * 2);
            assertArgument(Character.isDigit(digit1), "Expected all characters in string to be digits");
            final var b0 = ((digit1 - '0') & 0x0F);

            final int a0;
            final int k = i * 2 + 1;
            if (k < tbcd.length()) {
                final var digit2 = tbcd.charAt(k);
                assertArgument(Character.isDigit(digit2), "Expected all characters in string to be digits");
                a0 = (digit2 - '0') << 4;
            } else {
                // un-even number of digits. Mark this fact by 1111
                a0 = 0xF0;
            }
            buffer.setByte(i, (byte)(a0 + b0));
        }
        return buffer.build();
    }

    /**
     * This is essentially extending the given buffer with the one extra byte.
     *
     * @param buffer
     * @param b
     * @return
     */
    public static Buffer wrap(final Buffer buffer, byte b) {
        if (buffer == null || buffer.isEmpty()) {
            return Buffers.wrap(b);
        }

        final var writable = WritableBuffer.of(buffer.capacity() + 1);
        writable.write(buffer);
        writable.write(b);
        return writable.build();
    }

    public static Buffer wrap(final Buffer buffer, byte... bytes) {
        if (buffer == null || buffer.isEmpty()) {
            return Buffers.wrap(bytes);
        }

        if (bytes == null || bytes.length == 0) {
            return buffer;
        }

        final int totalSize = buffer.capacity() + bytes.length;
        final var writable = WritableBuffer.of(totalSize);
        writable.write(buffer);
        writable.write(bytes);
        return writable.build();

    }

    public static Buffer wrap(final List<Buffer> buffers) {
        if (buffers == null || buffers.isEmpty()) {
            return EmptyBuffer.EMPTY;
        }

        // TODO: of a proper composite buffer
        final int size = (int)buffers.stream().mapToInt(Buffer::capacity).sum();
        final var writable = WritableBuffer.of(size);
        for (int i = 0; i < buffers.size(); ++i) {
            buffers.get(i).writeTo(writable);
        }
        return writable.build();
    }

    public static Buffer wrap(final Buffer buffer) {
        return buffer;
    }

    /**
     * Wrap the specified buffers in a single buffer.
     *
     * NOTE: currently, it will copy all bytes into a new byte-array. Will
     * get to create a composite buffer.
     *
     * @param buffers
     * @return the combined buffers or an empty buffer if you pass in null or zero length array of buffers
     */
    public static Buffer wrap(final Buffer... buffers) {
        if (buffers == null || buffers.length == 0) {
            return EmptyBuffer.EMPTY;
        }

        // TODO: really need that composite buffer!
        int size = 0;
        for (int i = 0; i < buffers.length; ++i) {
            size += buffers[i].capacity();
        }

        final var writable = WritableBuffer.of(size);
        for (int i = 0; i < buffers.length; ++i) {
            buffers[i].writeTo(writable);
        }
        return writable.build();
    }


    /**
     * Assume that the given string is an IPv4 address (i.e. a.b.c.d such as 10.36.10.10)
     * and return a buffer containing that IPv4 address encoded as a 32 bit value.
     *
     * @param ipv4
     * @return
     */
    public static Buffer wrapAsIPv4(final String ipv4) {
        final byte[] b = IPv4.fromString(ipv4);
        return DefaultImmutableBuffer.of(b);
    }

    /**
     * The default {@link #wrap(int)} will wrap the int as a String but if you want
     * wrap the int as, well, integer, you have to call this method.
     *
     * @param value
     * @return
     */
    public static Buffer wrapAsInt(final int value) {
        final byte[] buffer = new byte[4];
        buffer[0] = (byte) (value >>> 24);
        buffer[1] = (byte) (value >>> 16);
        buffer[2] = (byte) (value >>> 8);
        buffer[3] = (byte) value;
        return Buffers.wrap(buffer);
    }

    public static Buffer wrapAsLong(final long value) {
        final byte[] buffer = new byte[8];
        buffer[0] = (byte)(value >>> 56);
        buffer[1] = (byte)(value >>> 48);
        buffer[2] = (byte)(value >>> 40);
        buffer[3] = (byte)(value >>> 32);
        buffer[4] = (byte)(value >>> 24);
        buffer[5] = (byte)(value >>> 16);
        buffer[6] = (byte)(value >>>  8);
        buffer[7] = (byte)(value >>>  0);
        return Buffers.wrap(buffer);
    }

    public static boolean isNullOrEmpty(final Buffer buffer) {
        return buffer == null || buffer.isEmpty();
    }

    public static boolean isNotNullOrEmpty(final Buffer buffer) {
        return buffer != null && !buffer.isEmpty();
    }

    /**
     * Converts the integer value into a string and that is what is being
     * wrapped in a {@link Buffer}
     *
     * @param value
     * @return
     */
    public static Buffer wrap(final int value) {
        final int size = value < 0 ? stringSize(-value) + 1 : stringSize(value);
        final byte[] bytes = new byte[size];
        getBytes(value, size, bytes);
        return DefaultImmutableBuffer.of(bytes);
    }

    public static Buffer wrap(final long value) {
        final int size = value < 0 ? stringSize(-value) + 1 : stringSize(value);
        final byte[] bytes = new byte[size];
        getBytes(value, size, bytes);
        return DefaultImmutableBuffer.of(bytes);
    }

    public static Buffer wrap(final String s) {
        assertNotNull(s, "String cannot be null");

        if (s.isEmpty()) {
            return EmptyBuffer.EMPTY;
        }

        return DefaultImmutableBuffer.of(s.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Copied straight from the Integer class but modified to return bytes instead.
     *
     * Places characters representing the integer i into the character array buf. The characters are
     * placed into the buffer backwards starting with the least significant digit at the specified
     * index (exclusive), and working backwards from there.
     *
     * Will fail if i == Integer.MIN_VALUE
     */
    public static void getBytes(int i, final int index, final byte[] buf) {
        int q, r;
        int charPos = index;
        byte sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = i * 52429 >>> 16 + 3;
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    /**
     * Find out how many characters it would take to represent the value as a string.
     *
     * @param value
     * @return
     */
    public static int stringSizeOf(final int value) {
        return value < 0 ? stringSize(-value) + 1 : stringSize(value);
    }

    // Requires positive x
    public static int stringSize(final int x) {
        for (int i = 0;; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    /**
     * Copied straight from the Long class but modified to return bytes instead.
     *
     * Places characters representing the integer i into the character array buf. The characters are
     * placed into the buffer backwards starting with the least significant digit at the specified
     * index (exclusive), and working backwards from there.
     *
     * Will fail if i == Long.MIN_VALUE
     */
    public static void getBytes(long i, final int index, final byte[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = i2 * 52429 >>> 16 + 3;
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buf[--charPos] = digits[r];
            i2 = q2;
            if (i2 == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = (byte) sign;
        }
    }

    /**
     * Find out how many characters it would take to represent the value as a string.
     *
     * @param value
     * @return
     */
    public static int stringSizeOf(final long value) {
        return value < 0 ? stringSize(-value) + 1 : stringSize(value);
    }

    // Requires positive x
    public static int stringSize(final long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p = 10 * p;
        }
        return 19;
    }

    public static void assertBufferCapacityAtLeast(final Buffer buffer, final int capacity) {
        assertArgument(buffer != null && buffer.capacity() >= capacity);
    }

    public static void assertBufferCapacityAtLeast(final Buffer buffer, final int capacity, final String message) {
        assertArgument(buffer != null && buffer.capacity() >= capacity, message);
    }

    public static void assertNotEmpty(final Buffer buffer) {
        assertArgument(buffer != null && !buffer.isEmpty());
    }

    public static void assertNotEmpty(final Buffer buffer, final String msg) {
        assertArgument(buffer != null && !buffer.isEmpty(), msg);
    }

    public static void assertBufferCapacity(final Buffer buffer, final int capacity) {
        assertArgument(buffer != null && buffer.capacity() == capacity);
    }

    public static void assertBufferCapacity(final Buffer buffer, final int capacity, final String message) {
        assertArgument(buffer != null && buffer.capacity() == capacity, message);
    }
}
