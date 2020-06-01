package io.snice.protocol;

import java.util.Optional;

public class ResponseSupport<O, T> extends TransactionSupport<O, T> implements Response<O, T> {

    private final boolean isFinal;

    protected ResponseSupport(final TransactionId transactionId, final O owner, final boolean isFinal, final Optional<T> payload) {
        super(transactionId, owner, payload);
        this.isFinal = isFinal;
    }

    protected ResponseSupport(final TransactionId transactionId, final O owner, final boolean isFinal) {
        super(transactionId, owner);
        this.isFinal = isFinal;
    }

    protected ResponseSupport(final TransactionId transactionId, final O owner) {
        super(transactionId, owner);
        this.isFinal = true;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    public static class BuilderSupport<O, T> implements Response.Builder<O, T> {

        private final TransactionId transactionId;
        private final O owner;
        private final Optional<T> payload;

        private boolean isFinal = true;

        protected BuilderSupport(final TransactionId transactionId, final O owner, final Optional<T> payload) {
            this.transactionId = transactionId;
            this.owner = owner;
            this.payload = payload;
        }

        @Override
        public Builder<O, T> isFinal(final boolean value) {
            this.isFinal = value;
            return this;
        }

        @Override
        public final Response<O, T> build() {
            return internalBuild(transactionId, owner, payload, isFinal);
        }

        /**
         * Meant for sub-classes to override in order to return a more specific {@link Response} class.
         *
         * @param id
         * @param owner
         * @param payload
         * @param isFinal
         * @return
         */
        protected Response<O, T> internalBuild(final TransactionId id, final O owner, final Optional<T> payload, final boolean isFinal) {
            return new ResponseSupport<O, T>(id, owner, isFinal, payload);
        }

    }
}
