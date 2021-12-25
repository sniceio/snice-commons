package io.snice.functional;

import io.snice.preconditions.PreConditions;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertArrayNotEmpty;

public final class Optionals {

    private Optionals() {
        // only static methods.
    }

    /**
     * Check if all given {@link Optional}s are empty.
     *
     * @param optionals an array of {@link Optional}s
     * @return true if all {@link Optional}s are empty, false otherwise. If an empty array is given
     * @throws IllegalArgumentException in case the array of {@link Optional}s are null or empty
     */
    public static boolean isAllEmpty(final Optional<?>... optionals) throws IllegalArgumentException {
        assertArrayNotEmpty(optionals, "The given array of Optionals cannot be null or the empty array");

        for (int i = 0; i < optionals.length; ++i) {
            final var optional = optionals[i];
            if (optional == null) {
                throw new IllegalArgumentException("The given Optional at index " + i + " was null");
            }

            if (optional.isPresent()) {
                return false;
            }
        }

        return true;
    }
}
