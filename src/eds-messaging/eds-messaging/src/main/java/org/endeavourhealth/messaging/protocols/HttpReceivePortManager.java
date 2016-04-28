package org.endeavourhealth.messaging.protocols;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.HttpListener;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.HttpReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpReceivePortManager {
	private static final Logger LOG = LoggerFactory.getLogger(HttpReceivePortManager.class);

	private Map<Integer, Server> httpServers;

	public HttpReceivePortManager() {
		httpServers = new HashMap<>();
	}

	public void registerListener(String serviceId, HttpListener httpListener) throws Exception {
		LOG.info("   Created http listener protocol on port " + Integer.toString(httpListener.getPort()));

		Server server = getOrCreateHttpServer(httpListener.getPort());

		for (HttpReceiver httpReceiver : httpListener.getReceiver())
			createHttpReceivers(serviceId, server, httpReceiver);
	}

	private void createHttpReceivers(String serviceId, Server server, HttpReceiver httpReceiver) throws Exception
	{
		LOG.info("   Creating receiver at path [" + httpReceiver.getPath() + "]");

		ServletHolder holder = new ServletHolder(HttpHandler.class);
		holder.setInitParameter(HttpHandler.SERVICEID_KEY, serviceId);

		ServletHandler handler = (ServletHandler)server.getHandler();
		handler.addServletWithMapping(holder, httpReceiver.getPath());
	}

	private Server getOrCreateHttpServer(int port)
	{
		Server server = httpServers.getOrDefault(port, null);

		if (server == null)
		{
			server = new Server(port);
			ServletHandler handler = new ServletHandler();
			server.setHandler(handler);
			httpServers.put(port, server);
		}

		return server;
	}

	public void shutdown() throws Exception {
		for (Server s : httpServers.values()) {
			s.stop();
			s.join();
		}
	}

	public void start() throws Exception {
		for (Server server : httpServers.values())
			server.start();
	}
}
