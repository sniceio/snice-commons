package io.snice.protocol;

import java.util.Optional;

public interface Transaction<O, T> {

    /**
     * The so-called owner is the entity that started this {@link Transaction} and is protocol
     * dependent. In e.g. HTTP, perhaps the 'owner' would be a HTTP URL. If you use this in e.g.
     * an Actor environment (such as with hektor.io), the 'owner' is probably a reference to
     * an Actor.
     */
    O getOwner();

    TransactionId getTransactionId();

    default Optional<T> getPayload() {
        return Optional.empty();
    }

    default boolean isRequest() {
        return false;
    }

    default Request<O, T> toRequest() {
        throw new ClassCastException("Unable to cast " + getClass().getName() + " into a " + Request.class.getName());
    }

    default boolean isResponse() {
        return false;
    }

    default Response<O, T> toResponse() {
        throw new ClassCastException("Unable to cast " + getClass().getName() + " into a " + Response.class.getName());
    }
}
