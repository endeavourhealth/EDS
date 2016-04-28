package org.endeavourhealth.messaging;

import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.utilities.Log;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Log.info("--------------------------------------------------");
        Log.info("Endeavour Resolution - a healthcare message engine");
        Log.info("--------------------------------------------------");

        try
        {
            Configuration configuration = Configuration.getInstance();

            Log.info("Is running as JAR = " + configuration.isRunningAsJar().toString());
            Log.info("Executing path = " + configuration.getCodeSourceLocation());
            Log.info("Service plugin path = " + configuration.getServicePluginPath());
            Log.info("--------------------------------------------------");

            configuration.loadServicePlugins();

            FrameworkProtocolManager frameworkProtocolManager = new FrameworkProtocolManager();
            frameworkProtocolManager.createReceivePorts(configuration.getServicePlugins());

            frameworkProtocolManager.start();

            Log.info("--------------------------------------------------");
            Log.info("Resolution started");
            Log.info("Press any key to exit...");

            System.in.read();

            frameworkProtocolManager.shutdown();
        }
        catch (Exception e)
        {
            Log.error("Fatal exception occurred", e);
        }

        Log.info("--------------------------------------------------");
        Log.info("Exiting");
        Log.info("--------------------------------------------------");
    }
}
