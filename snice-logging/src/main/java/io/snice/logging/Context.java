package io.snice.logging;

import java.util.function.BiConsumer;

public interface Context {

    /**
     * Ask the {@link Context} to copy any internal key-value pairs to the external context
     * as it wishes. Typically, this is just copying it to the Mapped Diagnostic Context (MDC).
     *
     * @param visitor the external context represented as a "visitor".
     */
    void copyContext(BiConsumer<String, String> visitor);

}
