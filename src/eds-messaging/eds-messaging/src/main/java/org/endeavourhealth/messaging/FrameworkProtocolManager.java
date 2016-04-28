package org.endeavourhealth.messaging;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.ReceivePort;
import org.endeavourhealth.messaging.model.ReceivePortProperties;
import org.endeavourhealth.messaging.model.ServicePlugin;
import org.endeavourhealth.messaging.utilities.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkProtocolManager
{
	private Map<Integer, Server> httpServers;

	public FrameworkProtocolManager()
	{
		httpServers = new HashMap<>();
	}

	public void createReceivePorts(List<ServicePlugin> servicePlugins) throws Exception
	{
		Log.info("Creating receive ports");

		for (ServicePlugin servicePlugin : servicePlugins)
		{
			Log.info(" For service [" + servicePlugin.getServiceId() + "]:");

			for (ReceivePort receivePort : servicePlugin.getReceivePorts())
				registerReceivePort(servicePlugin.getServiceId(), receivePort);
		}

		Log.info("Receive ports created");
	}

	public void start() throws Exception
	{
		Log.info("Starting receive ports");

		for (Server server : httpServers.values())
			server.start();

		Log.info("Receive ports started");
	}

	public void shutdown() throws Exception	{
		Log.info("Stopping receive ports");

		for (Server s : httpServers.values()) {
			s.stop();
			s.join();
		}

		Log.info("Receive ports stopped");
	}

	private void registerReceivePort(String serviceId, ReceivePort receivePort) throws Exception {
		switch (receivePort.getProtocol())
		{
			case HTTP: registerHttpReceivePort(serviceId, receivePort); break;
			default: throw new NotImplementedException(receivePort.getProtocol().value());
		}
	}

	private void registerHttpReceivePort(String serviceId, ReceivePort receivePort) throws Exception
	{
		ReceivePortProperties receivePortProperties = ReceivePortProperties.fromConfiguration(receivePort.getProperties());
		Integer port = receivePort.getPort().intValue();
		String path = receivePortProperties.getPath();

		Log.info("  Creating receive port [" + receivePort.getId() + "] created with protocol [http], port [" + port.toString() + "],  path [" + path + "]");

		Server server = getOrCreateHttpServer(port);

		ServletHolder holder = new ServletHolder(HttpHandler.class);
		holder.setInitParameter(HttpHandler.SERVICEID_KEY, serviceId);
		holder.setInitParameter(HttpHandler.RECEIVEPORTID_KEY, receivePort.getId());

		ServletHandler handler = (ServletHandler)server.getHandler();
		handler.addServletWithMapping(holder, path);
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
}
