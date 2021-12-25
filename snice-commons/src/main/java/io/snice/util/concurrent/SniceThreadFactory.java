package io.snice.util.concurrent;

import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotEmpty;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class SniceThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final int priority;
    private final Optional<Boolean> isDaemon;

    public static Builder withNamePrefix(final String namePrefix) {
        assertNotEmpty(namePrefix, "The prefix of the thread name cannot be null or the empty string");
        return new Builder(namePrefix);
    }

    private SniceThreadFactory(final String prefix, final ThreadGroup group, final int priority, final Optional<Boolean> isDaemon) {
        this.group = group;
        this.namePrefix = prefix;
        this.priority = priority;
        this.isDaemon = isDaemon;
    }

    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        isDaemon.ifPresent(t::setDaemon);
        t.setPriority(priority);
        return t;
    }

    public static class Builder {

        private final String prefix;
        private ThreadGroup group;
        private int priority = -1;
        private Boolean isDaemon;

        private Builder(final String prefix) {
            this.prefix = prefix;
        }

        public Builder withDaemon(final boolean isDaemon) {
            this.isDaemon = isDaemon;
            return this;
        }

        public Builder withThreadGroup(final ThreadGroup group) {
            assertNotNull(group);
            this.group = group;
            return this;
        }

        public Builder withPriority(final int priority) {
            assertArgument(priority > 0 && priority <= 10, "The thread priority must be within 0 to 10");
            this.priority = priority;
            return this;
        }

        public ThreadFactory build() {
            return new SniceThreadFactory(prefix, ensureThreadGroup(), ensurePriority(), Optional.ofNullable(isDaemon));
        }


        private ThreadGroup ensureThreadGroup() {
            return group != null ? group : Thread.currentThread().getThreadGroup();
        }

        private int ensurePriority() {
            return priority == -1 ? Thread.NORM_PRIORITY : priority;
        }

    }

}
