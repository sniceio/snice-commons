package io.snice.protocol;

public interface TransactionId {

    static TransactionId generateDefault() {
        return UuidTransactionId.generate();
    }

}
