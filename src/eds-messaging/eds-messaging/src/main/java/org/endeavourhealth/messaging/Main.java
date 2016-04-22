package org.endeavourhealth.messaging;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.endeavourhealth.messaging.configuration.Configuration;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Endeavour Resolution - a healthcare message engine");
        System.out.println("");

        Configuration configuration = Configuration.getInstance();

        Server server = new Server(8080);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(MessagePipeline.class, "/*");

        server.start();
        server.join();
    }
}
