package io.snice.functional;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static io.snice.functional.Optionals.isAllEmpty;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class OptionalsTest {

    @Test
    public void testOptionalsIsAllEmpty() {
        assertThat(isAllEmpty(empty()), is(true));
        assertThat(isAllEmpty(empty(), empty()), is(true));
        assertThat(isAllEmpty(empty(), empty(), empty()), is(true));
        assertThat(isAllEmpty(empty(), Optional.of("Not Empty"), empty()), is(false));
        assertThat(isAllEmpty(Optional.of("Not Empty"), Optional.of("Not Empty"), empty()), is(false));
        assertThat(isAllEmpty(Optional.of("Not Empty")), is(false));

        assertThat(isAllEmpty(Optional.of(123)), is(false));
        assertThat(isAllEmpty(Optional.of("hello"), Optional.of(123)), is(false));
        assertThat(isAllEmpty(empty(), Optional.of(123), Optional.of(new Object())), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyArrayOfOptionals() {
        isAllEmpty(new Optional[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArrayOfOptionals() {
        isAllEmpty(null);
    }

    @Test
    public void testNullOptionalInsideArrayOfOptionals() {
        ensureExceptions(0, null, empty());
        ensureExceptions(1, empty(), null);
        ensureExceptions(2, empty(), empty(), null);
        ensureExceptions(1, empty(), null, empty());
    }

    private void ensureExceptions(final int failedIndex, final Optional<?>... optionals) {
        try {
            isAllEmpty(optionals);
            fail("Expected to fail since there is a null optional within the array of Optionals");
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("The given Optional at index " + failedIndex + " was null"));
        }
    }

}