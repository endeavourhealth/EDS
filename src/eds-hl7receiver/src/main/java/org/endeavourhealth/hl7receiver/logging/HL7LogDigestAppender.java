package org.endeavourhealth.hl7receiver.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HL7LogDigestAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(HL7LogDigestAppender.class);

    private IDBDigestLogger dbLogger;

    public HL7LogDigestAppender(IDBDigestLogger dbLogger) {
        this.dbLogger = dbLogger;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {

        if (eventObject == null)
            return;

        if (eventObject.getLevel() != Level.ERROR)
            return;

        try {
            String logClass = getLogClass(eventObject);
            String logMethod = getLogMethod(eventObject);
            String logMessage = eventObject.getFormattedMessage();
            String exception = getException(eventObject);

            dbLogger.logErrorDigest(logClass, logMethod, logMessage, exception);
        } catch (PgStoredProcException e) {
            LOG.error("Error during logging error digest", e);
        }
    }

    private static String getException(ILoggingEvent eventObject) {
        return constructExceptionMessage(eventObject.getThrowableProxy());
    }

    private static String constructExceptionMessage(IThrowableProxy exception) {
        if (exception == null)
            return "";

        String message = "[" + exception.getClassName() + "]  " + exception.getMessage();

        if (exception.getCause() != null)
            if (exception.getCause() != exception)
                message += "\r\n" + constructExceptionMessage(exception.getCause());

        return message;
    }

    private static String getLogClass(ILoggingEvent eventObject) {
        StackTraceElement stackTraceElement = getFirstStackTraceElement(eventObject);

        if (stackTraceElement != null)
            return stackTraceElement.getClassName();

        return "";
    }

    private static String getLogMethod(ILoggingEvent eventObject) {
        StackTraceElement stackTraceElement = getFirstStackTraceElement(eventObject);

        if (stackTraceElement != null)
            return stackTraceElement.getMethodName();

        return "";
    }

    private static StackTraceElement getFirstStackTraceElement(ILoggingEvent eventObject) {
        if (eventObject.getCallerData() != null)
            if (eventObject.getCallerData().length > 0)
                if (eventObject.getCallerData()[0] != null)
                    return eventObject.getCallerData()[0];

        return null;
    }
}
