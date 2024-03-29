/*
 * Copyright (C) 2006 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.polo.pairing;

import java.nio.charset.Charset;

public class HexDump {
   private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private final static char[] HEX_DIGITS_LOWER_CASE = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Dump a byte array as hex, with the option to select whether the ABCDEF hex digits should be
     * upper or lower case.
     */
    public static String dumpHexString(final byte[] array, boolean upperCase) {
        return dumpHexString(array, 0, array.length, upperCase);
    }

    public static String dumpHexString(final byte[] array) {
        return dumpHexString(array, 0, array.length, true);
    }

    public static String dumpHexString(final byte[] array, final int offset, final int length) {
        return dumpHexString(array, offset, length, true);
    }

    /**
     * Dump the given byte array as a hex string, with the option to select whether the ABCDEF hex digits should be
     * upper or lower case.
     *
     * @param array the byte array
     * @param offset the offset into the byte array
     * @param length the length of the byte array to dump
     * @param upperCase flag indicating whether the ABCDEF hex digits should be upper or lower case.
     * @return
     */
    public static String dumpHexString(final byte[] array, final int offset, final int length, boolean upperCase) {
        final StringBuilder result = new StringBuilder();

        final byte[] line = new byte[16];
        int lineIndex = 0;

        result.append("\n0x")
              .append(toHexString(offset));

        for (int i = offset; i < (offset + length); i++) {
            if (lineIndex == 16) {
                result.append(" ");

                for (int j = 0; j < 16; j++) {
                    if ((line[j] > ' ') && (line[j] < '~')) {
                        result.append(new String(line, j, 1, Charset.forName("UTF-8")));
                    } else {
                        result.append(".");
                    }
                }

                result.append("\n0x");
                result.append(toHexString(i));
                lineIndex = 0;
            }

            final byte b = array[i];
            result.append(" ");
            result.append(HEX_DIGITS[(b >>> 4) & 0x0F]);
            result.append(HEX_DIGITS[b & 0x0F]);

            line[lineIndex++] = b;
        }

        if (lineIndex != 16) {
            int count = (16 - lineIndex) * 3;
            count++;
            for (int i = 0; i < count; i++) {
                result.append(" ");
            }

            for (int i = 0; i < lineIndex; i++) {
                if ((line[i] > ' ') && (line[i] < '~')) {
                    result.append(new String(line, i, 1));
                } else {
                    result.append(".");
                }
            }
        }

        return result.toString();
    }

    public static String toHexString(final byte b) {
        return toHexString(toByteArray(b));
    }

    public static String toHexString(final boolean prefix, final byte[] array) {
        return toHexString(prefix, array, 0, array.length, true);
    }

    public static String toHexString(final boolean prefix, final byte[] array, final boolean upperCase) {
        return toHexString(prefix, array, 0, array.length, upperCase);
    }

    public static String toHexString(final byte[] array, final boolean upperCase) {
        return toHexString(true, array, 0, array.length, upperCase);
    }

    public static String toHexString(final byte[] array) {
        return toHexString(true, array, 0, array.length, true);
    }

    public static String toHexString(final byte[] array, final int offset, final int length) {
        return toHexString(true, array, offset, length);
    }

    public static String toHexString(final boolean prefix, final byte[] array, final int offset, final int length) {
        return toHexString(prefix, array, offset, length, true);
    }

    public static String toHexString(final boolean prefix, final byte[] array, final int offset, final int length, boolean upperCase) {
        final char[] alphabet = upperCase ? HEX_DIGITS : HEX_DIGITS_LOWER_CASE;

        final char[] buf;
        int bufIndex = 0;

        if (prefix) {
            buf = new char[2 + length * 2];

            buf[0] = '0';
            buf[1] = 'x';

            bufIndex = 2;
        } else {
            buf = new char[length * 2];
        }

        for (int i = offset; i < (offset + length); i++) {
            final byte b = array[i];
            buf[bufIndex++] = alphabet[(b >>> 4) & 0x0F];
            buf[bufIndex++] = alphabet[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(final int i) {
        return toHexString(toByteArray(i));
    }

    public static byte[] toByteArray(final byte b) {
        final byte[] array = new byte[1];
        array[0] = b;
        return array;
    }

    public static byte[] toByteArray(final int i) {
        final byte[] array = new byte[4];

        array[3] = (byte) (i & 0xFF);
        array[2] = (byte) ((i >> 8) & 0xFF);
        array[1] = (byte) ((i >> 16) & 0xFF);
        array[0] = (byte) ((i >> 24) & 0xFF);

        return array;
    }

    private static int toByte(final char c) {
        if ((c >= '0') && (c <= '9')) {
            return c - '0';
        }
        if ((c >= 'A') && (c <= 'F')) {
            return (c - 'A') + 10;
        }
        if ((c >= 'a') && (c <= 'f')) {
            return (c - 'a') + 10;
        }

        throw new IllegalArgumentException("Invalid hex char '" + c + "'");
    }

    public static byte[] hexStringToByteArray(final String hexString) {
        final int length = hexString.length();
        final byte[] buffer = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte) ((toByte(hexString.charAt(i)) << 4) | toByte(hexString.charAt(i + 1)));
        }

        return buffer;
    }
}