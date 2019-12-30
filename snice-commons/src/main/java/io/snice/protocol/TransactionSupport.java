package io.snice.protocol;

import java.util.Optional;

public abstract class TransactionSupport<O, T> implements Transaction<O, T> {

    private final TransactionId transactionId;
    private final O owner;
    private final Optional<T> payload;

    protected TransactionSupport(final TransactionId transactionId, final O owner, final Optional<T> payload) {
        this.transactionId = transactionId;
        this.owner = owner;
        this.payload = payload;
    }

    protected TransactionSupport(final TransactionId transactionId, final O owner) {
        this(transactionId, owner, Optional.empty());
    }

    @Override
    public Optional<T> getPayload() {
        return payload;
    }

    @Override
    public final O getOwner() {
        return owner;
    }

    @Override
    public final TransactionId getTransactionId() {
        return transactionId;
    }
}
