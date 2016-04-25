package org.endeavourhealth.messaging.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log
{
    private static final String LOGGER_NAME = "resolution";
    private static Logger logger = null;

    private static void initialise()
    {
        if (logger == null)
            logger = LoggerFactory.getLogger(LOGGER_NAME);
    }

    public static void info()
    {
        info("");
    }

    public static void info(String text)
    {
        initialise();

        logger.info(text);
    }

    public static void warn(String text)
    {
        initialise();

        logger.warn(text);
    }

    public static void error(String text)
    {
        initialise();

        logger.error(text);
    }

    public static void debug(String text)
    {
        initialise();

        logger.debug(text);
    }
}
