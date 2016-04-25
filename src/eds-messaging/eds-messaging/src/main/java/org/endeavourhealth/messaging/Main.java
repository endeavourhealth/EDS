package org.endeavourhealth.messaging;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.configuration.Plugin;
import org.endeavourhealth.messaging.configuration.schema.routeConfiguration.ReceivePort;
import org.endeavourhealth.messaging.utilities.Log;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Log.info("--------------------------------------------------");
        Log.info("Endeavour Resolution - a healthcare message engine");
        Log.info("--------------------------------------------------");

        Configuration configuration = Configuration.getInstance();

        Log.info("Creating listeners");

        List<Server> httpListeners = createHttpListeners(configuration);

        for (Server httpListener : httpListeners)
            httpListener.start();

        Log.info("");
        Log.info("---------> Press any key to exit");
        Log.info("");

        System.in.read();

        for (Server httpListener : httpListeners)
            httpListener.stop();

        for (Server httpListener : httpListeners)
            httpListener.join();

        Log.info("--------------------------------------------------");
        Log.info("Exiting");
        Log.info("--------------------------------------------------");
    }

    private static List<Server> createHttpListeners(Configuration configuration) throws Exception
    {
        List<Server> httpListeners = new ArrayList<>();

        List<Integer> ports = new ArrayList<>();

        for (Plugin plugin : configuration.getPlugins())
        {
            for (ReceivePort receivePort : plugin.getReceivePorts())
            {
                if (receivePort.getHttp() != null)
                {
                    Log.info("Created http listener on port " + receivePort.getHttp().getPort());

                    Server httpListener = createHttpListener(receivePort.getHttp().getPort());

                    httpListeners.add(httpListener);
                }
            }
        }

        return httpListeners;
    }

    private static Server createHttpListener(int port)
    {
        Server server = new Server(port);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(HttpHandler.class, "/*");

        return server;
    }
}
