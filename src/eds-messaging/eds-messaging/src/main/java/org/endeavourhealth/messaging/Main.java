package org.endeavourhealth.messaging;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.configuration.Plugin;
import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.ProtocolType;
import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.ReceivePort;
import org.endeavourhealth.messaging.utilities.Log;

import java.util.ArrayList;
import java.util.List;

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

            List<Server> httpListeners = createHttpListeners(configuration);

            for (Server httpListener : httpListeners)
                httpListener.start();

            Log.info("--------------------------------------------");
            Log.info("Msgx started");
            Log.info("Press any key to exit...");

            System.in.read();

            for (Server httpListener : httpListeners)
                httpListener.stop();

            for (Server httpListener : httpListeners)
                httpListener.join();

        }
        catch (Exception e)
        {
            Log.error("Fatal exception occurred", e);
        }

        Log.info("--------------------------------------------");
        Log.info("Exiting");
        Log.info("--------------------------------------------");
    }

    private static List<Server> createHttpListeners(Configuration configuration) throws Exception
    {
        List<Server> httpListeners = new ArrayList<>();

        for (Plugin plugin : configuration.getPlugins())
            for (ReceivePort receivePort : plugin.getReceivePorts())
                if (receivePort.getProtocol().equals(ProtocolType.HTTP))
                    httpListeners.add(createHttpListener(receivePort.getPort().intValue()));

        return httpListeners;
    }

    private static Server createHttpListener(int port)
    {
        Log.info("Created http listener on port " + Integer.toString(port));

        Server server = new Server(port);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(HttpHandler.class, "/*");

        return server;
    }
}
