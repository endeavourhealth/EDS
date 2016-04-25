package org.endeavourhealth.messaging;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.configuration.Plugin;
import org.endeavourhealth.messaging.configuration.schema.routeConfiguration.ReceivePort;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Logger logger = Configuration.getLogger();

        logger.info("--------------------------------------------------");
        logger.info("Endeavour Resolution - a healthcare message engine");
        logger.info("--------------------------------------------------");

        Configuration configuration = Configuration.getInstance();

        logger.info("Creating listeners");

        List<Server> httpListeners = createHttpListeners(configuration);

        for (Server httpListener : httpListeners)
            httpListener.start();

        logger.info("");
        logger.info("---------> Press any key to exit");
        logger.info("");

        System.in.read();

        for (Server httpListener : httpListeners)
            httpListener.stop();

        for (Server httpListener : httpListeners)
            httpListener.join();

        logger.info("--------------------------------------------------");
        logger.info("Exiting");
        logger.info("--------------------------------------------------");
    }

    private static List<Server> createHttpListeners(Configuration configuration) throws Exception
    {
        Logger logger = Configuration.getLogger();

        List<Server> httpListeners = new ArrayList<>();

        List<Integer> ports = new ArrayList<>();

        for (Plugin plugin : configuration.getPlugins())
        {
            for (ReceivePort receivePort : plugin.getReceivePorts())
            {
                if (receivePort.getHttp() != null)
                {
                    logger.info("Created http listener on port " + receivePort.getHttp().getPort());

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
