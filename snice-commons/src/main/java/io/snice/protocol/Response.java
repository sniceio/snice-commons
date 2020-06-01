package io.snice.protocol;

public interface Response<O, T> extends Transaction<O, T> {

    /**
     * Indicates whether or not this {@link Response} is considered a "final" response and as such,
     * one that would complete the {@link Transaction}.
     */
    boolean isFinal();

    @Override
    default boolean isResponse() {
        return true;
    }

    @Override
    default Response<O, T> toResponse() {
        return this;
    }

    interface Builder<O, T> {

        Builder<O, T> isFinal(boolean value);

        Response<O, T> build();
    }
}
