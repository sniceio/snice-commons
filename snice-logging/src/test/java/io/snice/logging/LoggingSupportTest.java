package io.snice.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class LoggingSupportTest {

    private AppenderTestSupport capturingAppender;

    /**
     * Helper method to setup json logging. Not actually used for testing but just nice if you also want to
     * view the full mdc etc while figuring out a particular test.
     */
    private static void configureJsonAppender(final LoggerContext ctx, final ch.qos.logback.classic.Logger root) {
        final var appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(ctx);

        final var encoder = new LoggingEventCompositeJsonEncoder();
        encoder.setContext(ctx);
        final var provider = new LoggingEventJsonProviders();
        provider.addLogLevel(new LogLevelJsonProvider());
        provider.addMessage(new MessageJsonProvider());
        provider.addMdc(new MdcJsonProvider());
        encoder.setProviders(provider);
        encoder.start();
        appender.setEncoder(encoder);

        appender.start();

        root.addAppender(appender);
    }

    @Before
    public void setup() {
        final var ctx = (LoggerContext)LoggerFactory.getILoggerFactory();
        final var root = ctx.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);

        // Note: if you call this and then do not call configureJsonAppender, there is something
        // I've missed in the configuration and setup of logback whereby the MDC isn't propagated
        // to the log event and as such, the unit tests will fail. Will try and figure out what's missing
        // in the setup but if you do NOT want the json logging (which, again, is just for human consumption)
        // and as such do not call the configureJsonAppender then you also have to comment out the
        // line below...
        root.detachAndStopAllAppenders();

        capturingAppender = new AppenderTestSupport();
        capturingAppender.setContext(ctx);
        root.addAppender(capturingAppender);

        configureJsonAppender(ctx, root);
    }

    @Test
    public void testAlertLogging() {
        final var myClass = new MyClass();
        myClass.logInfo(MyAlertCode.WOW, "Alice");
        capturingAppender.ensureLastLogEvent("Wow says Alice", MyAlertCode.WOW);

        Context ctx = new MyDumbLoggingContext();
        myClass.logWarn(MyAlertCode.OOOPS, ctx);
        capturingAppender.ensureLastLogEvent("Something went wrong.", MyAlertCode.OOOPS, "hello", "world");

        ctx = new MyDumbLoggingContext2();
        myClass.logWarn(MyAlertCode.CRASH, ctx, "Null pointer");
        capturingAppender.ensureLastLogEvent("Crashed on Null pointer",
                MyAlertCode.CRASH, "foo", "woo", "alice", "bob");
    }

    private static class MyDumbLoggingContext implements Context {

        @Override
        public void copyContext(final BiConsumer<String, String> visitor) {
            visitor.accept("hello", "world");
        }
    }

    private static class MyDumbLoggingContext2 implements Context {

        @Override
        public void copyContext(final BiConsumer<String, String> visitor) {
            visitor.accept("foo", "woo");
            visitor.accept("alice", "bob");
        }
    }

    private enum MyAlertCode implements Alert {
        WOW(12, "Wow says {}"),
        OOOPS(14, "Something went wrong."),
        CRASH(15, "Crashed on {}");

        private final int code;
        private final String msg;

        MyAlertCode(final int code, final String msg) {
            this.code = code;
            this.msg = msg;
        }
        @Override
        public String getMessage() {
            return msg;
        }

        @Override
        public int getCode() {
            return code;
        }
    }

    private static class MyClass implements LoggingSupport {

        private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

        @Override
        public Logger getLogger() {
            return logger;
        }
    }
}
