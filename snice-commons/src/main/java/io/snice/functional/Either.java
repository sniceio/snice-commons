package io.snice.functional;

import java.util.function.Consumer;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Note:This class is essentially a condensed version of the one find in vavr.io.
 *
 * {@link Either} represents a value of two possible types, either it is a left or it is
 * a right. By convention, the right value is considered the success branch, and the left
 * an error. This {@link Either} is right-biased, meaning operations by default operate
 * on the right value if present.
 *
 * Vavr.io is an excellent library and you should check it out. The reason for not
 * pulling in that library into the various snice.io projects is that, in general, the
 * core of those libraries are to be kept small and as much as possible, only dependent
 * on the JVM. With that said, pretty sure I"ll regret it and soon enough having to swap
 * this {@link Either} out for the full functionality of vavr.io. Time will tell.
 *
 *
 */
public interface Either<L, R> {


    <U> U fold(Function<? super L, ? extends U> leftMapper, Function<? super R, ? extends U> rightMapper);

    void accept(Consumer<? super L> leftConsumer, Consumer<? super R> rightConsumer);

    static <L, R> Either<L, R> right(final R value) {
        return new Right<>(value);
    }

    static <L, R> Left<L, R> left(final L value) {
        return new Left<>(value);
    }

    default boolean isRight() {
        return false;
    }

    default boolean isLeft() {
        return false;
    }

    default R get() {
        throw new IllegalArgumentException("Unable to get a Right on a Left");
    }

    default L getLeft() {
        throw new IllegalArgumentException("Unable to get a Left on a Right");
    }

    class Right<L, R> implements Either<L,R> {


        private final R value;

        private Right(final R value) {
            this.value = value;
        }

        @Override
        public <U> U fold(final Function<? super L, ? extends U> leftMapper, final Function<? super R, ? extends U> rightMapper) {
            assertNotNull(rightMapper, "The right mapper cannot be null");
            return rightMapper.apply(value);
        }

        @Override
        public void accept(Consumer<? super L> leftConsumer, Consumer<? super R> rightConsumer) {
            assertNotNull(rightConsumer, "The right consumer cannot be null");
            rightConsumer.accept(value);
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public R get() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Right<?, ?> right = (Right<?, ?>) o;

            return value.equals(right.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    class Left<L, R> implements Either<L, R> {

        private final L value;

        private Left(final L value) {
            this.value = value;
        }

        @Override
        public <U> U fold(final Function<? super L, ? extends U> leftMapper, final Function<? super R, ? extends U> rightMapper) {
            assertNotNull(leftMapper, "The left mapper cannot be null");
            return leftMapper.apply(value);
        }

        @Override
        public void accept(Consumer<? super L> leftConsumer, Consumer<? super R> rightConsumer) {
            assertNotNull(leftConsumer, "The left consumer cannot be null");
            leftConsumer.accept(value);
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public L getLeft() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Left<?, ?> left = (Left<?, ?>) o;

            return value.equals(left.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}

