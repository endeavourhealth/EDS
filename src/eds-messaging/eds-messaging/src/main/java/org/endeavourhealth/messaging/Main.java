package org.endeavourhealth.messaging;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.configuration.Plugin;
import org.endeavourhealth.messaging.configuration.schema.routeConfiguration.ReceivePort;
import org.slf4j.Logger;

import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Logger logger = Configuration.getLogger();

        logger.info("--------------------------------------------------");
        logger.info("Endeavour Resolution - a healthcare message engine");
        logger.info("--------------------------------------------------");

        Configuration configuration = Configuration.getInstance();


        InstantiateReceivePorts(configuration);


        System.in.read();


        logger.info("--------------------------------------------------");
        logger.info("Exiting");
        logger.info("--------------------------------------------------");
    }

    private static void InstantiateReceivePorts(Configuration configuration) throws Exception
    {
        Logger logger = Configuration.getLogger();

        for (Plugin plugin : configuration.getPlugins())
        {
            for (ReceivePort receivePort : plugin.getReceivePorts())
            {
                ReceivePort.Http http = receivePort.getHttp();
                ReceivePort.Sftp sftp = receivePort.getSftp();
                ReceivePort.RabbitMQ rabbitMQ = receivePort.getRabbitMQ();

                if (http != null)
                {
                    Server server = new Server(http.getPort());

                    ServletHandler handler = new ServletHandler();
                    server.setHandler(handler);

                    handler.addServletWithMapping(HttpHandler.class, "/*");

                    server.start();
                    //server.join();
                }
                else if (sftp != null)
                {

                }
                else if (rabbitMQ != null)
                {

                }
            }
        }


    }
}
