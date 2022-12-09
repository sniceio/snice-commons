package io.snice.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.snice.preconditions.PreConditions.assertArgument;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AppenderTestSupport implements Appender<ILoggingEvent> {

    private final List<ILoggingEvent> events = new ArrayList<>();
    private Context context;

    @Override
    public String getName() {
        return getClass().getName();
    }

    public ILoggingEvent lastLogEvent() {
        // actually want the test to blow up on a NPE since typically the unit tests expects
        // that there will be a log event present (after all, we are testing the logging support,
        // not the underlying logging framework itself) and if there isn't one, instead of always
        // checking Optional.isPresent all the time, just let the unit test blow up on NPE and fail.
        return events.size() > 0 ? events.get(events.size() - 1) : null;
    }

    public void ensureLastLogEvent(final String expectedMsg) {
        ensureLastLogEvent(expectedMsg, Map.of());
    }

    public void ensureLastLogEvent(final String expectedMsg, final Alert expectedAlert) {
        ensureLastLogEvent(expectedMsg, Map.of(expectedAlert.getAttributeName(), String.valueOf(expectedAlert.getCode())));
    }

    public void ensureLastLogEvent(final String expectedMsg, final Alert expectedAlert, final String... expectedMdcValues) {
        assertArgument(expectedMdcValues.length % 2 == 0, "Expected an even number of arguments");
        final var mdc = new HashMap<String, String>();
        for (int i = 0; i < expectedMdcValues.length; i += 2) {
            mdc.put(expectedMdcValues[i], expectedMdcValues[i + 1]);
        }

        if (expectedAlert != null) {
            mdc.put(expectedAlert.getAttributeName(), String.valueOf(expectedAlert.getCode()));
        }

        ensureLastLogEvent(expectedMsg, mdc);

    }

    public void ensureLastLogEvent(final String expectedMsg, final String... expectedMdcValues) {
        ensureLastLogEvent(expectedMsg, null, expectedMdcValues);
    }

    public void ensureLastLogEvent(final String expectedMsg, final Map<String, String> expectedMdc) {
        final var logEvent = lastLogEvent();
        assertThat(logEvent.getFormattedMessage(), is(expectedMsg));

        final var actualMdc = logEvent.getMDCPropertyMap();
        assertThat(actualMdc.size(), is(expectedMdc.size()));
        expectedMdc.entrySet().forEach(entry -> {
            final var key = entry.getKey();
            assertThat("The key \"" + key + "\" is missing in the actual MDC", actualMdc.containsKey(key), is(true));
            assertThat(actualMdc.get(key), is(entry.getValue()));
        });
    }

    @Override
    public void doAppend(final ILoggingEvent event) throws LogbackException {
        events.add(event);
    }

    @Override
    public void setName(final String name) {

    }

    @Override
    public void setContext(final Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void addStatus(final Status status) {

    }

    @Override
    public void addInfo(final String msg) {

    }

    @Override
    public void addInfo(final String msg, final Throwable ex) {

    }

    @Override
    public void addWarn(final String msg) {

    }

    @Override
    public void addWarn(final String msg, final Throwable ex) {

    }

    @Override
    public void addError(final String msg) {

    }

    @Override
    public void addError(final String msg, final Throwable ex) {

    }

    @Override
    public void addFilter(final Filter<ILoggingEvent> newFilter) {

    }

    @Override
    public void clearAllFilters() {

    }

    @Override
    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return null;
    }

    @Override
    public FilterReply getFilterChainDecision(final ILoggingEvent event) {
        return null;
    }

    @Override
    public void start() {
        System.err.println("Starting");
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }
}
