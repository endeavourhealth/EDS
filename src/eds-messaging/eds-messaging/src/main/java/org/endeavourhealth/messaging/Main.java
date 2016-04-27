package org.endeavourhealth.messaging;

import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.utilities.Log;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Log.info("--------------------------------------------");
        Log.info("Endeavour Msgx - a healthcare message engine");
        Log.info("--------------------------------------------");

        try
        {
            Configuration configuration = Configuration.getInstance();

            Log.info("Is running as JAR = " + configuration.isRunningAsJar().toString());
            Log.info("Executing path = " + configuration.getCodeSourceLocation());
            Log.info("Plugin path = " + configuration.getPluginPath());
            Log.info("--------------------------------------------");

            configuration.loadPlugins();

            Log.info("Creating listeners:");

						FrameworkProtocolManager.getInstance().createListeners(configuration);

            Log.info("--------------------------------------------");
            Log.info("Msgx started");
            Log.info("Press any key to exit...");

            System.in.read();

						FrameworkProtocolManager.getInstance().shutdown();
        }
        catch (Exception e)
        {
            Log.error("Fatal exception occurred", e);
        }

        Log.info("--------------------------------------------");
        Log.info("Exiting");
        Log.info("--------------------------------------------");
    }
}
