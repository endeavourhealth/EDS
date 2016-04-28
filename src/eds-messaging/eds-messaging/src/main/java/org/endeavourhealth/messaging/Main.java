package org.endeavourhealth.messaging;

import org.endeavourhealth.messaging.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception
    {
        LOG.info("--------------------------------------------------");
        LOG.info("Endeavour Resolution - a healthcare message engine");
        LOG.info("--------------------------------------------------");

        try
        {
            Configuration configuration = Configuration.getInstance();

            LOG.info("Is running as JAR = " + configuration.isRunningAsJar().toString());
            LOG.info("Executing path = " + configuration.getCodeSourceLocation());
            LOG.info("Service plugin path = " + configuration.getServicePluginPath());
            LOG.info("--------------------------------------------------");

            configuration.loadServicePlugins();

            FrameworkProtocolManager frameworkProtocolManager = new FrameworkProtocolManager();
            frameworkProtocolManager.createReceivePorts(configuration.getServicePlugins());

            frameworkProtocolManager.start();

            LOG.info("--------------------------------------------------");
            LOG.info("Resolution started");
            LOG.info("Press any key to exit...");

            System.in.read();

            frameworkProtocolManager.shutdown();
        }
        catch (Exception e)
        {
            LOG.error("Fatal exception occurred", e);
        }

        LOG.info("--------------------------------------------------");
        LOG.info("Exiting");
        LOG.info("--------------------------------------------------");
    }
}
