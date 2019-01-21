package io.snice.preconditions;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class TestPreConditions {

    @Test
    public void testAssertArray() throws Exception {

        PreConditions.assertArray(new byte[20], 10, 5, "Should be ok");
        PreConditions.assertArray(new byte[2], 0, 2, "Should be ok");
        PreConditions.assertArray(new byte[3], 2, 0, "Should be ok"); // border, we would get nothing but still ok

        ensureBadArray(null, 0, 10);
        ensureBadArray(new byte[3], 0, 10); // length too long
        ensureBadArray(new byte[3], -1, 1); // offset wrong
        ensureBadArray(new byte[3], 0, 20); // length too long again
        ensureBadArray(new byte[3], 2, 2); // offset + length beyond the array

        ensureBadArray(new byte[3], 3, 0); // offset is zero based so should fail...
        ensureBadArray(new byte[3], 3, 1); // both offset + length and just the offset on its own are off so should def fail
    }

    private void ensureBadArray(final byte[] array, final int offset, final int length) {
        try {
            PreConditions.assertArray(array, offset, length, "Expected to fail");
            fail("Expected it to fail");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }
}
