package io.snice.buffer;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BuffersTest {

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