package org.endeavourhealth.hl7receiver.logging;

public class Logger {

    private static IDBLogger dbLogger = null;
    private org.slf4j.Logger logger;

    public static Logger getLogger(Class clazz) {
        return new Logger(clazz);
    }

    public synchronized static void setDBLogger(IDBLogger dbLogger) {
        Logger.dbLogger = dbLogger;
    }

    public synchronized static IDBLogger getDBLogger() {
        return dbLogger;
    }

    private Logger(Class clazz) {
        logger = org.slf4j.LoggerFactory.getLogger(clazz);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void error(String message, Throwable exception) {
        try {
            IDBLogger dbLogger = getDBLogger();

            dbLogger.logError(
                    exception.getClass().getCanonicalName(),
                    "class",
                    message);
        } catch (Exception e) {
            logger.error("error logging exception");
        }

        logger.error(message, exception);
    }
}
