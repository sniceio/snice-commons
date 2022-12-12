package io.snice.logging;

import org.slf4j.Logger;
import org.slf4j.MDC;

public interface Logging {

    Logger getLogger();

    default void logDebug(final String msg, final Object... args) {
        getLogger().debug(msg, args);
    }

    default void logInfo(final String msg, final Object... args) {
        getLogger().info(msg, args);
    }

    default void logInfo(final Context ctx, final String msg, final Object... args) {
        ctx.copyContext(MDC::put);
        getLogger().info(msg, args);
        clearContext();
    }

    default void logInfo(final Alert alert, final Object... args) {
        log(getLogger()::info, alert, null, args);
    }

    default void logInfo(final Alert alert, final Context ctx, final Object... args) {
        log(getLogger()::info, alert, ctx, args);
    }

    default void logWarn(final Alert alert, final Object... args) {
        log(getLogger()::warn, alert, null, args);
    }

    default void logWarn(final Alert alert, final Context ctx, final Object... args) {
        log(getLogger()::warn, alert, ctx, args);
    }

    default void logError(final Alert alert, final Object... args) {
        log(getLogger()::error, alert, null, args);
    }

    default void logError(final Alert alert, final Context ctx, final Object... args) {
        log(getLogger()::error, alert, ctx, args);
    }

    private void clearContext() {
        MDC.clear();
    }

    /**
     * Log an {@link Alert} using the supplied reporter function.
     *
     * This method is simply so that we can set the correct MDC context
     * in one single place.
     * @param reporter the function to use for the actual logging.
     * @param alert
     * @param args
     */
    default void log(final LogReportFunction reporter, final Alert alert, final Context ctx, final Object... args) {
        if (ctx != null) {
            ctx.copyContext(MDC::put);
        }
        MDC.put(alert.getAttributeName(), String.valueOf(alert.getCode()));
        reporter.apply(alert.getMessage(), args);
        clearContext();
    }


}
