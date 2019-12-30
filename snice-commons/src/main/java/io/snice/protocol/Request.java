package io.snice.protocol;

public interface Request<O, T> extends Transaction<O, T> {

    /**
     * Create a new final {@link Response} with no payload.
     *
     * @return
     */
    Response.Builder<O, Object> buildResponse();

    Response<O, Object> createResponse();

    <T> Response.Builder<O, T> buildResponse(T payload);

    @Override
    default boolean isRequest() {
        return true;
    }

    @Override
    default Request<O, T> toRequest() {
        return this;
    }

    interface Builder<O, T> {

        /**
         * Specify the {@link TransactionId}.
         *
         * If not specified, the default {@link TransactionId} implementation will be used,
         * which is obtained through {@link TransactionId#generateDefault()}.
         */

        Builder<O, T> withTransactionId(TransactionId id);
        Request<O, T> build();
    }


}
