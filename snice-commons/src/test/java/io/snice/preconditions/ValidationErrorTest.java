package io.snice.preconditions;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ValidationErrorTest {

    @Test
    public void testSingleError() {
        final var validation = ValidationError.of("error1");
        assertThat(validation.getErrors().size(), is(1));
        assertThat(validation.getErrors().get(0), is("error1"));
    }

    @Test
    public void testAppendButEmpty() {
        final var v1 = ValidationError.of("error1");
        final var v2 = v1.append(null);
        final var v3 = v1.append(new String[0]);

        assertThat(v1.getErrors().size(), is(1));
        assertThat(v1.getErrors().get(0), is("error1"));

        assertThat(v2.getErrors().size(), is(1));
        assertThat(v2.getErrors().get(0), is("error1"));

        assertThat(v3.getErrors().size(), is(1));
        assertThat(v3.getErrors().get(0), is("error1"));
    }

    @Test
    public void testAddErrors() {
        final var validation = ValidationError.of("error1", "error2");
        final var v2 = validation.append("error3");

        // the initial validation should still only contain 2 errors
        assertThat(validation.getErrors().size(), is(2));
        assertThat(validation.getErrors().get(0), is("error1"));
        assertThat(validation.getErrors().get(1), is("error2"));

        // and the v2 that built on the previous should have all that
        // plus the new error
        assertThat(v2.getErrors().size(), is(3));
        assertThat(v2.getErrors().get(0), is("error1"));
        assertThat(v2.getErrors().get(1), is("error2"));
        assertThat(v2.getErrors().get(2), is("error3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadUsage() {
        ValidationError.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadUsage2() {
        ValidationError.of(new String[0]);
    }

}