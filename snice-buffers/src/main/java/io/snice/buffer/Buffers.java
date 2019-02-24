package io.snice.buffer;

import io.snice.buffer.impl.DefaultImmutableBuffer;
import io.snice.buffer.impl.EmptyBuffer;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static io.snice.preconditions.PreConditions.assertNotNull;

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
     *            the upper boundary (exclusive) of the range of visible bytes.
     * @return
     */
    public static Buffer wrap(final byte[] buffer, final int lowerBoundary, final int upperBoundary) {
        return Buffer.of(buffer, lowerBoundary, upperBoundary - lowerBoundary);
    }

    public static Buffer wrap(final byte... buffer) {
        return Buffer.of(buffer);
    }

    public static Buffer wrap(final Buffer... buffers) {
        assertNotNull(buffers == null || buffers.length == 0, "You must specify at least one buffer");
        return Buffers.wrap(Arrays.asList(buffers));

    }

    public static Buffer wrap(final List<Buffer> buffers) {
        if (buffers == null || buffers.isEmpty()) {
            return EmptyBuffer.EMPTY;
        }

        // TODO: create a proper composite buffer
        final int size = (int)buffers.stream().mapToInt(Buffer::capacity).sum();
        final byte[] buffer = new byte[size];
        int index = 0;
        for (final Buffer b : buffers) {
            for (int i = 0; i < b.capacity(); ++i) {
                buffer[index++] = b.getByte(i);
            }
        };

        return Buffers.wrap(buffer);
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
}
