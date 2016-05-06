package org.endeavourhealth.core.messaging;

import org.endeavourhealth.core.messaging.configuration.schema.serviceConfiguration.HttpListener;
import org.endeavourhealth.core.messaging.configuration.schema.serviceConfiguration.RabbitListener;
import org.endeavourhealth.core.messaging.configuration.schema.serviceConfiguration.SftpListener;
import org.endeavourhealth.core.messaging.model.ServicePlugin;
import org.endeavourhealth.core.messaging.protocols.HttpReceivePortManager;
import org.endeavourhealth.core.messaging.protocols.RabbitReceivePortManager;
import org.endeavourhealth.core.messaging.protocols.SftpReceivePortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FrameworkProtocolManager
{
	private static final Logger LOG = LoggerFactory.getLogger(FrameworkProtocolManager.class);
	private HttpReceivePortManager httpReceivePortManager;
	private RabbitReceivePortManager rabbitReceivePortManager;
	private SftpReceivePortManager sftpReceivePortManager;

	public FrameworkProtocolManager()
	{
		httpReceivePortManager = new HttpReceivePortManager();
		rabbitReceivePortManager = new RabbitReceivePortManager();
		sftpReceivePortManager = new SftpReceivePortManager();
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

			for (SftpListener sftpListener : servicePlugin.getListeners().getSftpListener())
				sftpReceivePortManager.registerListener(servicePlugin.getServiceId(), sftpListener);
		}

		LOG.info("Receive ports created");
	}

	public void start() throws Exception
	{
		LOG.info("Starting receive ports");
		httpReceivePortManager.start();
		rabbitReceivePortManager.start();
		sftpReceivePortManager.start();
		LOG.info("Receive ports started");
	}

	public void shutdown() throws Exception	{
		LOG.info("Stopping receive ports");
		httpReceivePortManager.shutdown();
		rabbitReceivePortManager.shutdown();
		sftpReceivePortManager.shutdown();
		LOG.info("Receive ports stopped");
	}
}
