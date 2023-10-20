package com.google.polo.pairing;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author borjesson.jonas@gmail.com
 */
public class HexDumpTest {

    @Test
    public void testDumpHexString() {
        final var bytes = new byte[] { 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, (byte) 0xFF };
        assertEquals("0x0A0B0C0D0EFF", HexDump.toHexString(bytes));
        assertEquals("0x0a0b0c0d0eff", HexDump.toHexString(bytes, false));
    }

}