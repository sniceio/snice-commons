package io.snice.buffer;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BuffersTest {

    @Test
    public void testEquals() {
        final var a = Buffers.wrap((byte) 0x00, (byte) 0x01, (byte) 0x02);
        final var b = Buffers.wrap((byte) 0x00, (byte) 0x01, (byte) 0x02);
        assertThat(a, is(b));
    }

    @Test
    public void testTBCD() {
        final var tbcd01 = Buffers.wrapAsTbcd("1234");
        assertThat(tbcd01.toTBCD(), is("1234"));
        assertThat(tbcd01, is(Buffers.wrap((byte)0x21, (byte)0x43)));

        final var tbcd02 = Buffers.wrapAsTbcd("56789");
        assertThat(tbcd02.toTBCD(), is("56789"));
        assertThat(tbcd02, is(Buffers.wrap((byte)0x65, (byte)0x87, (byte)0xF9)));

        final var tbcd03 = Buffers.wrapAsTbcd("7");
        assertThat(tbcd03.toTBCD(), is("7"));
        assertThat(tbcd03, is(Buffers.wrap((byte)0xF7)));

        final var tbcd04 = Buffers.wrapAsTbcd("78");
        assertThat(tbcd04.toTBCD(), is("78"));
        assertThat(tbcd04, is(Buffers.wrap((byte)0x87)));
    }

    @Test
    public void testWrapAsInt() {
        assertThat(Buffers.wrapAsInt(123).getInt(0), is(123));
        assertThat(Buffers.wrapAsInt(-123).getInt(0), is(-123));
        assertThat(Buffers.wrapAsInt(0).getInt(0), is(0));
        assertThat(Buffers.wrapAsInt(Integer.MAX_VALUE).getInt(0), is(Integer.MAX_VALUE));
        assertThat(Buffers.wrapAsInt(Integer.MIN_VALUE).getInt(0), is(Integer.MIN_VALUE));
    }

    @Test
    public void testWrapAsLong() {
        assertThat(Buffers.wrapAsLong(123L).getLong(0), is(123L));
        assertThat(Buffers.wrapAsLong(-123L).getLong(0), is(-123L));
        assertThat(Buffers.wrapAsLong(0L).getLong(0), is(0L));
        assertThat(Buffers.wrapAsLong(Long.MAX_VALUE).getLong(0), is(Long.MAX_VALUE));
        assertThat(Buffers.wrapAsLong(Long.MIN_VALUE).getLong(0), is(Long.MIN_VALUE));
    }

}