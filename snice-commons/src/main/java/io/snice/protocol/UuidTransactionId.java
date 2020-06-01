package io.snice.protocol;

import java.util.Objects;
import java.util.UUID;

public class UuidTransactionId implements TransactionId {

    private final UUID uuid;

    public static TransactionId generate() {
        return new UuidTransactionId(UUID.randomUUID());
    }

    private UuidTransactionId(final UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final UuidTransactionId that = (UuidTransactionId) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
