package org.endeavourhealth.messaging;

import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.HttpListener;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.RabbitListener;
import org.endeavourhealth.messaging.model.ServicePlugin;
import org.endeavourhealth.messaging.protocols.HttpReceivePortManager;
import org.endeavourhealth.messaging.protocols.RabbitReceivePortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FrameworkProtocolManager
{
	private static final Logger LOG = LoggerFactory.getLogger(FrameworkProtocolManager.class);
	private HttpReceivePortManager httpReceivePortManager;
	private RabbitReceivePortManager rabbitReceivePortManager;

	public FrameworkProtocolManager()
	{
		httpReceivePortManager = new HttpReceivePortManager();
		rabbitReceivePortManager = new RabbitReceivePortManager();
	}

	public void createServices(List<ServicePlugin> servicePlugins) throws Exception
	{
		LOG.info("Creating listeners");

		for (ServicePlugin servicePlugin : servicePlugins)
		{
			LOG.info(" For service [" + servicePlugin.getServiceId() + "]:");

			for (HttpListener httpListener : servicePlugin.getListeners().getHttpListener())
				httpReceivePortManager.registerListener(servicePlugin.getServiceId(), httpListener);

			for (RabbitListener rabbitListener : servicePlugin.getListeners().getRabbitListener())
				rabbitReceivePortManager.registerListener(servicePlugin.getServiceId(), rabbitListener);
		}

		LOG.info("Receive ports created");
	}

	public void start() throws Exception
	{
		LOG.info("Starting receive ports");
		httpReceivePortManager.start();
		rabbitReceivePortManager.start();
		LOG.info("Receive ports started");
	}

	public void shutdown() throws Exception	{
		LOG.info("Stopping receive ports");
		httpReceivePortManager.shutdown();
		rabbitReceivePortManager.shutdown();
		LOG.info("Receive ports stopped");
	}
}
