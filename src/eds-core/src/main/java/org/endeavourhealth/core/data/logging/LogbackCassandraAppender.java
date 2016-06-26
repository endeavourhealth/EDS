package org.endeavourhealth.core.data.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.db.DBHelper;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.endeavourhealth.core.data.logging.models.LoggingEvent;
import org.endeavourhealth.core.data.logging.models.LoggingEventException;
import org.endeavourhealth.core.data.logging.models.LoggingEventProperty;
import org.endeavourhealth.core.engineConfiguration.EngineConfiguration;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class LogbackCassandraAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackCassandraAppender.class);

    private final LoggingRepository loggingRepository = new LoggingRepository();

    public LogbackCassandraAppender() {}

    @Override
    public void start() {

        EngineConfiguration config = EngineConfigurationSerializer.getConfig();

        //only call into the super-class start() if all the above was successfu
        super.start();
    }


    @Override
    protected void append(ILoggingEvent event) {

        try {
            UUID eventUuid = UUID.randomUUID();

            LoggingEvent loggingEvent = createLoggingEvent(event, eventUuid);
            List<LoggingEventProperty> loggingEventProperties = createLoggingEventProperties(event, eventUuid);
            List<LoggingEventException> loggingEventExceptions = createLoggingEventExceptions(event, eventUuid);

            loggingRepository.save(loggingEvent, loggingEventProperties, loggingEventExceptions);

        } catch (Exception e) {
            //any exception raised from the above must be manually output
            super.addError("Error appending event", e);
            e.printStackTrace();
        }
    }

    private static List<LoggingEventException> createLoggingEventExceptions(ILoggingEvent event, UUID eventUuid) {

        IThrowableProxy tp = event.getThrowableProxy();
        if (tp == null) {
            return null;
        }

        List<LoggingEventException> l = new ArrayList<>();

        int lineNumber = 0;
        while (tp != null) {
            lineNumber = createLoggingEventExceptionsForThrowable(l, eventUuid, tp, lineNumber);
            tp = tp.getCause();
        }

        return l;
    }

    private static int createLoggingEventExceptionsForThrowable(List<LoggingEventException> l, UUID eventUuid, IThrowableProxy tp, int lineNumber) {

        //add the first line of the exception
        StringBuilder sb = new StringBuilder();
        ThrowableProxyUtil.subjoinFirstLine(sb, tp);
        l.add(createLoggingEventExceptionsForThrowableStatment(eventUuid, lineNumber++, sb.toString()));

        //add the regular stack trace lines
        int commonFrames = tp.getCommonFrames();
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        for (int i = 0; i < stepArray.length - commonFrames; i++) {
            sb = new StringBuilder();
            sb.append(CoreConstants.TAB);
            ThrowableProxyUtil.subjoinSTEP(sb, stepArray[i]);
            l.add(createLoggingEventExceptionsForThrowableStatment(eventUuid, lineNumber++, sb.toString()));
        }

        //if we've got common lines with another exception say we're not logging them
        if (commonFrames > 0) {
            sb = new StringBuilder();
            sb.append(CoreConstants.TAB).append("... ").append(commonFrames).append(
                    " common frames omitted");
            l.add(createLoggingEventExceptionsForThrowableStatment(eventUuid, lineNumber++, sb.toString()));
        }

        return lineNumber;
    }

    private static LoggingEventException createLoggingEventExceptionsForThrowableStatment(UUID eventUid, int line, String msg) {

        LoggingEventException e = new LoggingEventException();
        e.setEventId(eventUid);
        e.setLineNumber(line);
        e.setTraceLine(msg);
        return e;
    }

    private static List<LoggingEventProperty> createLoggingEventProperties(ILoggingEvent event, UUID eventUuid) {

        Map<String, String> mergedMap = mergePropertyMaps(event);

        Set<String> propertiesKeys = mergedMap.keySet();
        if (propertiesKeys.isEmpty()) {
            return null;
        }

        List<LoggingEventProperty> l = new ArrayList<>();

        for (String key : propertiesKeys) {
            String value = mergedMap.get(key);

            LoggingEventProperty p = new LoggingEventProperty();
            p.setEvent_id(eventUuid);
            p.setMappedKey(key);
            p.setMappedValue(value);

            l.add(p);
        }

        return l;
    }

    private static LoggingEvent createLoggingEvent(ILoggingEvent event, UUID eventUuid) {

        StackTraceElement caller = extractFirstCaller(event.getCallerData());
        Object[] args = event.getArgumentArray();

        LoggingEvent e = new LoggingEvent();

        e.setTimestmp(new Date(event.getTimeStamp()));
        e.setFormattedMessage(event.getFormattedMessage());
        e.setLoggerName(event.getLoggerName());
        e.setLevel(event.getLevel().toString());
        e.setThreadName(event.getThreadName());
        e.setReferenceFlag(new Integer(DBHelper.computeReferenceMask(event)));

        if (args != null && args.length > 0) {
            e.setArg0(args[0].toString());
        }
        if (args != null && args.length > 1) {
            e.setArg1(args[1].toString());
        }
        if (args != null && args.length > 2) {
            e.setArg2(args[2].toString());
        }
        if (args != null && args.length > 3) {
            e.setArg3(args[3].toString());
        }

        e.setCallerFilename(caller.getFileName());
        e.setCallerClass(caller.getClassName());
        e.setCallerMethod(caller.getMethodName());
        e.setCallerLine(new Integer(caller.getLineNumber()));
        e.setEventId(eventUuid);

        return e;
    }

    private static StackTraceElement extractFirstCaller(StackTraceElement[] callerStack) {
        if (callerStack != null
                && callerStack.length > 0
                && callerStack[0] != null) {
            return callerStack[0];
        } else {
            return CallerData.naInstance();
        }
    }

    private static Map<String, String> mergePropertyMaps(ILoggingEvent event) {

        Map<String, String> mergedMap = new HashMap<String, String>();
        // we add the context properties first, then the event properties, since
        // we consider that event-specific properties should have priority over
        // context-wide properties.
        Map<String, String> loggerContextMap = event.getLoggerContextVO().getPropertyMap();
        Map<String, String> mdcMap = event.getMDCPropertyMap();

        if (loggerContextMap != null) {
            mergedMap.putAll(loggerContextMap);
        }
        if (mdcMap != null) {
            mergedMap.putAll(mdcMap);
        }

        return mergedMap;
    }

    private static void registerDbAppender() {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        LogbackCassandraAppender dbAppender = new LogbackCassandraAppender();
        dbAppender.setContext(rootLogger.getLoggerContext());
        dbAppender.setName("DB Appender");
        dbAppender.start();

        //use an async appender so logging to DB doesn't block
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(rootLogger.getLoggerContext());
        asyncAppender.setName("Async DB Appender");
        //    // excluding caller data (used for stack traces) improves appender's performance
        //    asyncAppender.setIncludeCallerData(false);
        //    // set threshold to 0 to disable discarding and keep all events
        //    asyncAppender.setDiscardingThreshold(0);
        //    asyncAppender.setQueueSize(256);
        asyncAppender.addAppender(dbAppender);
        asyncAppender.start();

        rootLogger.addAppender(asyncAppender);
    }

    public static void tryRegisterDbAppender() {

        //register the DB logger explicitly, as too diificult to handle errors
        //if initialised via logback.xml
        try {
            registerDbAppender();
        } catch (Exception e) {
            LOG.error("Failed to initialise DB logging appender", e);
        }
    }
}
