package io.snice.preconditions;

import io.snice.functional.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * There are many cases where validation needs to occur, which may yield in errors. E.g., when validating a SIP or
 * a Diameter message, you may discover that they are missing mandatory headers (SIP) or AVPs (Diameter). However,
 * instead of throwing exceptions, generating a {@link ValidationError} wrapped in an {@link Either} allows for
 * a more functional approach in handling these validation errors.
 */
public interface ValidationError<T> {

    static <T> ValidationError<T> of(T error) {
        assertNotNull(error);
        return new DefaultValidationError<>(List.of(error));
    }

    static <T> ValidationError<T> of(T... errors) {
        assertNotNull(errors);
        assertArgument(errors.length > 0, "The list of errors cannot be empty");
        return new DefaultValidationError<>(List.of(errors));
    }

    /**
     * A list of T describing the issues found while validating.
     *
     * @return
     */
    List<T> getErrors();

    /**
     * If more errors are found, you can append them.
     *
     * Note that {@link ValidationError} is a mutable class and as such,
     * the {@link ValidationError} returned will be a different instance
     * than this one and the current one will be left untouched.
     *
     * @param errors
     * @return a new {@link ValidationError} that contains the list of errors from this
     * instance plus the new set of errors.
     */
    ValidationError append(T... errors);

    class DefaultValidationError<T> implements ValidationError<T> {

        private final List<T> errors;

        private DefaultValidationError(List<T> errors) {
            this.errors = errors;
        }

        @Override
        public List<T> getErrors() {
            return errors;
        }

        @Override
        public ValidationError<T> append(T... errors) {
            if (errors == null || errors.length == 0) {
                return this;
            }
            final var l = new ArrayList<>(this.errors);
            for (int i = 0; i < errors.length; ++i) {
                l.add(errors[i]);
            }
            return new DefaultValidationError<>(Collections.unmodifiableList(l));
        }
    }
}
