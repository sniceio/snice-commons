package io.snice.util.concurrent;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SniceThreadFactoryTest {

    @Test
    public void testCreateThreadFactory() throws Exception {
        final var th = new ThreadGroup("snice");
        final var factory = SniceThreadFactory.withNamePrefix("hello-")
                .withDaemon(true)
                .withPriority(7)
                .withThreadGroup(th)
                .build();
        final var lock = new CountDownLatch(1);
        final var t = factory.newThread(() -> lock.countDown());
        assertThat(t.getName(), is("hello-1"));
        assertThat(t.getThreadGroup(), is(th));
        assertThat(t.isDaemon(), is(true));
        t.start();
        lock.await(1, TimeUnit.SECONDS);
    }

    @Test
    public void testPriority() {
        ensureWrongPriority(-1);
        ensureWrongPriority(-2);
        ensureWrongPriority(11);
        ensureWrongPriority(12);
    }

    private void ensureWrongPriority(int priority) {
        try {
            SniceThreadFactory.withNamePrefix("unit-test-").withPriority(priority).build();
            Assert.fail("A priority of " + priority + " should not have been accepted");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

}