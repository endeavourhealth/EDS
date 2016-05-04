package org.endeavourhealth.messaging.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.db.DBAppender;
import ch.qos.logback.classic.db.DBHelper;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.datastax.driver.core.*;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import org.endeavourhealth.messaging.configuration.schema.engineConfiguration.EngineConfiguration;
import org.endeavourhealth.messaging.configuration.schema.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.messaging.configuration.schema.engineConfiguration.Logging;
import org.endeavourhealth.messaging.database.DbClient;
import org.endeavourhealth.messaging.database.PreparedStatementCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;

import static ch.qos.logback.core.db.DBHelper.closeStatement;

public class CassandraDbAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final String SQL_EVENT = "INSERT INTO logging_event ("
            + "timestmp, formatted_message, logger_name, level_string, thread_name, reference_flag, "
            + "arg0, arg1, arg2, arg3, caller_filename, caller_class, caller_method, caller_line, "
            + "event_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String SQL_EVENT_PROPERTY = "INSERT INTO logging_event_property ("
            + "event_id, mapped_key, mapped_value) VALUES (?, ?, ?)";

    private final String SQL_EVENT_EXCEPTION = "INSERT INTO logging_event_exception ("
            + "event_id, i, trace_line) VALUES (?, ?, ?)";

    private Session session = null;

    public CassandraDbAppender() {


    }

    @Override
    public void start() {

        EngineConfiguration config = EngineConfigurationSerializer.getConfig();
        Logging logging = config.getLogging();
        String keyspace = logging.getKeyspace();

        this.session = DbClient.getInstance().getSession(keyspace);

        //only call into the super-class start() if all the above was successfu
        super.start();
    }


    @Override
    protected void append(ILoggingEvent event) {

        BatchStatement batch = new BatchStatement();

        UUID eventUuid = UUID.randomUUID();

        insertEvent(batch, event, eventUuid);
        insertEventProperties(batch, event, eventUuid);
        insertEventException(batch, event, eventUuid);

        ResultSet rs = session.execute(batch);
        if (!rs.wasApplied()) {
            addWarn("Failed to insert loggingEvent/property/exception");
        }


        /*//cassandra db layer
// Connect to the cluster and keyspace "demo"
        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").addContactPoint("localhost").build();
        Session session = cluster.connect(" mykeyspace ");
        //Session session = cluster.connect();

        String s = "INSERT INTO users (user_id,  fname, lname) VALUES (1800, 'johnxx', 'smith')";
        session.execute(s);

        ResultSet results = session.execute("SELECT * FROM users");
        for (Row row : results) {
            System.out.format("%s %d\n", row.getString("fname"), row.getInt("user_id"));
        }
*/
    }

    private void insertEvent(BatchStatement batch, ILoggingEvent event, UUID eventUuid) {
        StackTraceElement caller = extractFirstCaller(event.getCallerData());
        Object[] args = event.getArgumentArray();

        PreparedStatementCache cache = DbClient.getInstance().getStatementCache(session);
        PreparedStatement preparedStatement = cache.getOrAdd(SQL_EVENT);

        BoundStatement boundStatement = preparedStatement.
                bind().
                setTimestamp(0, new Date(event.getTimeStamp())).
                setString(1, event.getFormattedMessage()).
                setString(2, event.getLoggerName()).
                setString(3, event.getLevel().toString()).
                setString(4, event.getThreadName()).
                setInt(5, DBHelper.computeReferenceMask(event));

        if (args != null && args.length > 0) {
            boundStatement.setString(6, args[0].toString());
        } else {
            boundStatement.setToNull(6);
        }
        if (args != null && args.length > 1) {
            boundStatement.setString(7, args[1].toString());
        } else {
            boundStatement.setToNull(7);
        }
        if (args != null && args.length > 2) {
            boundStatement.setString(8, args[2].toString());
        } else {
            boundStatement.setToNull(8);
        }
        if (args != null && args.length > 3) {
            boundStatement.setString(9, args[3].toString());
        } else {
            boundStatement.setToNull(9);
        }

        boundStatement.setString(10, caller.getFileName()).
                setString(11, caller.getClassName()).
                setString(12, caller.getMethodName()).
                setInt(13, caller.getLineNumber()).
                setUUID(14, eventUuid);

        batch.add(boundStatement);
    }

    private void insertEventProperties(BatchStatement batch, ILoggingEvent event, UUID eventUuid) {

        Map<String, String> mergedMap = mergePropertyMaps(event);

        Set<String> propertiesKeys = mergedMap.keySet();
        if (propertiesKeys.isEmpty()) {
            return;
        }

        PreparedStatementCache cache = DbClient.getInstance().getStatementCache(session);
        PreparedStatement preparedStatement = cache.getOrAdd(SQL_EVENT_PROPERTY);

        for (String key : propertiesKeys) {
            String value = mergedMap.get(key);

            BoundStatement boundStatement = preparedStatement.
                    bind().
                    setUUID(0, eventUuid).
                    setString(1, key).
                    setString(2, value);

            batch.add(boundStatement);
        }
    }

    private void insertEventException(BatchStatement batch, ILoggingEvent event, UUID eventUuid) {
        IThrowableProxy tp = event.getThrowableProxy();
        if (tp == null) {
            return;
        }

        PreparedStatementCache cache = DbClient.getInstance().getStatementCache(session);
        PreparedStatement preparedStatement = cache.getOrAdd(SQL_EVENT_EXCEPTION);

        int lineNumber = 0;
        while (tp != null) {
            lineNumber = buildExceptionStatement(batch, preparedStatement, eventUuid, tp, lineNumber);
            tp = tp.getCause();
        }

    }

    private int buildExceptionStatement(BatchStatement batch, PreparedStatement preparedStatement, UUID eventUuid, IThrowableProxy tp, int lineNumber) {

        //add the first line of the exception
        StringBuilder sb = new StringBuilder();
        ThrowableProxyUtil.subjoinFirstLine(sb, tp);
        createExceptionLineStatement(batch, preparedStatement, eventUuid, lineNumber++, sb.toString());

        //add the regular stack trace lines
        int commonFrames = tp.getCommonFrames();
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        for (int i = 0; i < stepArray.length - commonFrames; i++) {
            sb = new StringBuilder();
            sb.append(CoreConstants.TAB);
            ThrowableProxyUtil.subjoinSTEP(sb, stepArray[i]);
            createExceptionLineStatement(batch, preparedStatement, eventUuid, lineNumber++, sb.toString());
        }

        //if we've got common lines with another exception say we're not logging them
        if (commonFrames > 0) {
            sb = new StringBuilder();
            sb.append(CoreConstants.TAB).append("... ").append(commonFrames).append(
                    " common frames omitted");
            createExceptionLineStatement(batch, preparedStatement, eventUuid, lineNumber++, sb.toString());
        }

        return lineNumber;
    }

    private void createExceptionLineStatement(BatchStatement batch, PreparedStatement preparedStatement, UUID eventUid, int line, String msg) {

        BoundStatement boundStatement = preparedStatement.
                bind().
                setUUID(0, eventUid).
                setInt(1, line).
                setString(2, msg);
        batch.add(boundStatement);

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

    public static void registerDbAppender() {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        CassandraDbAppender dbAppender = new CassandraDbAppender();
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
}
