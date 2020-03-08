package io.snice.net;

import org.junit.Test;

import static io.snice.net.IPv4.convertToStringIP;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IPv4Test {

    @Test
    public void testConstructFromString() {
        ensureBackAndForth("127.0.0.1");
        ensureBackAndForth("10.36.10.10");
        ensureBackAndForth("100.101.102.103");
    }

    @Test
    public void testConstructAndInsertIntoByteArray() {
        final byte[] buffer = new byte[10];
        buffer[0] = (byte)0x00;
        buffer[1] = (byte)0x01;
        buffer[2] = (byte)0x02;
        IPv4.fromString(buffer, 3, "10.36.10.10");

        ensureConversion("10.36.10.10", buffer[3], buffer[4], buffer[5], buffer[6]);
    }

    @Test
    public void convertIPv4ToString() {
        ensureConversion("172.22.18.120", (byte)0xac, (byte)0x16, (byte)0x12, (byte)0x78);
    }

    private static void ensureBackAndForth(final String s) {
        final byte[] b = IPv4.fromString(s);
        ensureConversion(s, b[0], b[1], b[2], b[3]);
    }

    private static void ensureConversion(final String expected, final byte a, final byte b, final byte c, final byte d) {
        final byte[] array = new byte[]{a, b, c, d};
        assertThat(convertToStringIP(a, b, c, d), is(expected));
        assertThat(convertToStringIP(array), is(expected));
    }

}