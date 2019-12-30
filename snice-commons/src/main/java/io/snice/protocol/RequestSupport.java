package io.snice.protocol;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class RequestSupport<O, T> extends TransactionSupport<O, T> implements Request<O, T> {

    protected RequestSupport(final TransactionId transactionId, final O owner, final Optional<T> payload) {
        super(transactionId, owner, payload);
    }

    protected RequestSupport(final TransactionId transactionId, final O owner) {
        super(transactionId, owner);
    }

    protected RequestSupport(final O owner) {
        super(TransactionId.generateDefault(), owner);
    }

    public static <O> RequestSupport<O, Object> create(final O owner) {
        assertNotNull(owner);
        return new RequestSupport<>(owner);
    }

    public static <O, T> BuilderSupport<O, T> of(final O owner, final T payload) {
        assertNotNull(owner);
        assertNotNull(payload);
        return new BuilderSupport<>(owner, Optional.of(payload));
    }

    public static <O> BuilderSupport<O, Object> of(final O owner) {
        assertNotNull(owner);
        return new BuilderSupport<>(owner, Optional.empty());
    }

    @Override
    public Response.Builder<O, Object> buildResponse() {
        return new ResponseSupport.BuilderSupport<>(getTransactionId(), getOwner(), Optional.empty());
    }

    @Override
    public <T1> Response.Builder<O, T1> buildResponse(final T1 payload) {
        assertNotNull(payload);
        return new ResponseSupport.BuilderSupport<>(getTransactionId(), getOwner(), Optional.of(payload));
    }

    @Override
    public Response<O, Object> createResponse() {
        return new ResponseSupport.BuilderSupport<>(getTransactionId(), getOwner(), Optional.empty()).build();
    }

    public static class BuilderSupport<O, T> implements Request.Builder<O, T> {

        private final O owner;
        private final Optional<T> payload;
        private TransactionId transactionId;

        protected BuilderSupport(final O owner, final Optional<T> payload) {
            this.owner = owner;
            this.payload = payload;
        }


        @Override
        public Builder<O, T> withTransactionId(final TransactionId transactionId) {
            assertNotNull(transactionId);
            this.transactionId = transactionId;
            return this;
        }

        @Override
        public Request<O, T> build() {
            final TransactionId id = transactionId == null ? TransactionId.generateDefault() : transactionId;
            return internalBuild(id, owner, payload);
        }

        protected Request<O, T> internalBuild(final TransactionId id, final O owner, final Optional<T> payload) {
            return new RequestSupport<>(id, owner, payload);
        }
    }


}
