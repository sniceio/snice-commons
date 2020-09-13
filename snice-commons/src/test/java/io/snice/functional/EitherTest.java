package io.snice.functional;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class EitherTest {

    @Test
    public void testRight() {
        final Either<String, Integer> right = Either.right(123);
        assertThat(right.isRight(), is(true));
        assertThat(right.isLeft(), is(false));
        assertThat(right.fold(l -> "hello", i -> i.toString()), is("123"));
        assertThat(right.get(), is(123));
    }

    @Test
    public void testLeft() {
        final Either<String, Integer> left = Either.left("hello world");
        assertThat(left.isRight(), is(false));
        assertThat(left.isLeft(), is(true));
        assertThat(left.fold(l -> l.length(), i -> -1), is(11));
        assertThat(left.getLeft(), is("hello world"));
    }

    /**
     * Following the convention from vavr.io, a right is considered success and left an error
     * condition so there may be cases where you either want the right only and if it is
     * not there then throw an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNotSuccessful() {
        Either.left("ooops, error").fold(e -> {
            throw new IllegalArgumentException("unable to produce the result, Got error");},
                r -> r);
    }

    @Test
    public void testSuccessful() {
        final Integer result = Either.right(12).fold(e -> {
                    throw new IllegalArgumentException("unable to produce the result, Got error");},
                r -> r * 2);
        assertThat(result, is(24));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLeftOnRight() {
        Either.right("hello").getLeft();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRightOnLeft() {
        Either.left("hello").get();
    }

    @Test
    public void testEquality() {
        ensureEquals(Either.right("hello"), Either.right("hello"));
        ensureEquals(Either.right(123), Either.right(123));

        ensureEquals(Either.left("hello"), Either.left("hello"));
        ensureEquals(Either.left(123), Either.left(123));

        // the value hello is in one case in right and the other on the left side
        ensureNotEquals(Either.right("hello"), Either.left("hello"));
    }

    private static void ensureEquals(final Either a, final Either b) {
        assertThat(a, is(b));
        assertThat(b, is(a));
    }

    private static void ensureNotEquals(final Either a, final Either b) {
        assertThat(a, not(b));
        assertThat(b, not(a));
    }

}