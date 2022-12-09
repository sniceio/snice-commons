package io.snice.logging;

public interface Alert {

    String ATTRIBUTE_NAME = "alert_code";

    /**
     * When the alert is e.g. logged, this is the key under which the alert code is
     * logged.
     *
     * @see {@link LoggingSupport#log(LogReportFunction, Alert, Context, Object...)}
     */
    default String getAttributeName() {
        return ATTRIBUTE_NAME;
    }

    String getMessage();
    int getCode();
}
