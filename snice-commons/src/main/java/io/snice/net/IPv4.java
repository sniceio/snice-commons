package io.snice.net;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertArray;
import static io.snice.preconditions.PreConditions.assertNotEmpty;

/**
 * Basic utility class for dealing with various IPv4 operations.
 *
 */
public class IPv4 {

    private static final String ERROR_MSG_ILLEGAL_FORMAT
            = "Illegal format. Expected the address to be in format \"a.b.c.d\"";

    /**
     * Helper method to convert an IPv4 address represented as a byte-array
     * into a human readable String.
     */
    public static String convertToStringIP(final byte[] ip) {
        assertArray(ip, 0, 4);
        final short a = (short) (ip[0] & 0xFF);
        final short b = (short) (ip[1] & 0xFF);
        final short c = (short) (ip[2] & 0xFF);
        final short d = (short) (ip[3] & 0xFF);
        return a + "." + b + "." + c + "." + d;
    }

    public static String convertToStringIP(final byte a, final byte b, final byte c, final byte d) {
        return ((short) (a & 0xFF)) + "." +
                ((short) (b & 0xFF)) + "." +
                ((short) (c & 0xFF)) + "." +
                ((short) (d & 0xFF));
    }

    /**
     * Convert the human readable string into a byte-array representing the
     * IPv4 address.
     *
     * @param str
     * @return
     */
    public static byte[] fromString(final String str) {
        final byte[] buffer = new byte[4];
        return fromString(buffer, 0, str);
    }

    /**
     * Same as {@link #fromString(String)} but insert the result in the given destination byte
     * array. The given destination byte array is also the same as returned, for a fluent API.
     *
     * @param dst the destination byte-array to which we'll insert the converted IPv4 String
     * @param offset offset into the destination byte-array
     * @param str the IPv4 address as a string that we are encoding to a 32bit value.
     * @return the destination byte array, just for a fluent API
     */
    public static byte[] fromString(final byte[] dst, final int offset, final String str) {

        assertNotEmpty(str, "The IP address cannot be null or the empty string");
        assertArray(dst, offset, 4, "There are not enough bytes available in the destination byte-array");

        final String[] parts = str.split("\\.");
        assertArgument(parts.length == 4, ERROR_MSG_ILLEGAL_FORMAT);

        dst[offset + 0] = (byte) Short.parseShort(parts[0]);
        dst[offset + 1] = (byte) Short.parseShort(parts[1]);
        dst[offset + 2] = (byte) Short.parseShort(parts[2]);
        dst[offset + 3] = (byte) Short.parseShort(parts[3]);

        return dst;

    }
}
