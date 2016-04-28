package org.endeavourhealth.messaging;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.HttpListener;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.HttpReceiver;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.RabbitListener;
import org.endeavourhealth.messaging.model.ServicePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkProtocolManager
{
	private static final Logger LOG = LoggerFactory.getLogger(FrameworkProtocolManager.class);

	private Map<Integer, Server> httpServers;

	public FrameworkProtocolManager()
	{
		httpServers = new HashMap<>();
	}

	public void createReceivePorts(List<ServicePlugin> servicePlugins) throws Exception
	{
		LOG.info("Creating listeners");

		for (ServicePlugin servicePlugin : servicePlugins)
		{
			LOG.info(" For service [" + servicePlugin.getServiceId() + "]:");

			for (HttpListener httpListener : servicePlugin.getListeners().getHttpListener())
			{
				LOG.info("   Created http listener protocol on port " + Integer.toString(httpListener.getPort()));

				Server server = getOrCreateHttpServer(httpListener.getPort());

				for (HttpReceiver httpReceiver : httpListener.getReceiver())
				{
					createHttpReceivers(servicePlugin.getServiceId(), server, httpReceiver);
				}
			}

			for (RabbitListener rabbitListener : servicePlugin.getListeners().getRabbitListener())
			{

			}
		}

		LOG.info("Receive ports created");
	}

	public void start() throws Exception
	{
		LOG.info("Starting receive ports");

		for (Server server : httpServers.values())
			server.start();

		LOG.info("Receive ports started");
	}

	public void shutdown() throws Exception
	{
		LOG.info("Stopping receive ports");

		for (Server s : httpServers.values())
		{
			s.stop();
			s.join();
		}

		LOG.info("Receive ports stopped");
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

	private void createHttpReceivers(String serviceId, Server server, HttpReceiver httpReceiver) throws Exception
	{
		LOG.info("   Creating receiver at path [" + httpReceiver.getPath() + "]");

		ServletHolder holder = new ServletHolder(HttpHandler.class);
		holder.setInitParameter(HttpHandler.SERVICEID_KEY, serviceId);

		ServletHandler handler = (ServletHandler)server.getHandler();
		handler.addServletWithMapping(holder, httpReceiver.getPath());
	}
}
