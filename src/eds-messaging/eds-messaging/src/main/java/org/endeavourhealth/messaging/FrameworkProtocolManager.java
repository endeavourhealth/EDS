package org.endeavourhealth.messaging;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.configuration.Plugin;
import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.ReceivePort;
import org.endeavourhealth.messaging.utilities.Log;

import java.util.ArrayList;
import java.util.List;

public class FrameworkProtocolManager {

	public static final String RECEIVE_PORT_ID_KEY = "ReceivePortId";

	private static FrameworkProtocolManager frameworkProtocolManager = null;
	public static FrameworkProtocolManager getInstance() {
		if (frameworkProtocolManager == null) {
			frameworkProtocolManager = new FrameworkProtocolManager();
		}
		return frameworkProtocolManager;
	}

	private List<Server> httpServers;
	// private List<String> sftpServers;

	FrameworkProtocolManager() {
		httpServers = new ArrayList<>();
		// sftpServers = new ArrayList<>();
	}

	public void createListeners(Configuration configuration) throws Exception {
		for (Plugin plugin : configuration.getPlugins()) {
			for (ReceivePort receivePort : plugin.getReceivePorts()) {
				registerReceivePort(receivePort);
			}
		}
	}

	public void shutdown() throws Exception {
		for(Server s : httpServers) {
			s.stop();
			s.join();
		}
	}

	private void registerReceivePort(ReceivePort receivePort) throws Exception {
		switch (receivePort.getProtocol()) {
			case HTTP:
				registerHttpReceivePort(receivePort);
				break;
		}
	}

	private void registerHttpReceivePort(ReceivePort receivePort) throws Exception {
		Server server = null;
		Integer port = receivePort.getPort().intValue();

		// try to find existing server running on port
		for(Server s : httpServers){
			if (((ServerConnector)s.getConnectors()[0]).getLocalPort() == port) {
				server = s;
				break;
			}
		}

		// If none found, create a new one
		if (server == null) {
			server = new Server(port);
			ServletHandler handler = new ServletHandler();
			server.setHandler(handler);
			httpServers.add(server);
			server.start();
		}

		ServletHandler handler = (ServletHandler)server.getHandler();
		ServletHolder holder = new ServletHolder(HttpHandler.class);
		holder.setInitParameter(RECEIVE_PORT_ID_KEY, receivePort.getId());

		String path = getPropertiesEntryValueByKey(receivePort.getProperties(), "Path");
		handler.addServletWithMapping(holder, path);
		Log.info("Http receiver [" + receivePort.getId() + "] registered on port [" + port + "] - path [" + path + "]");
	}

	// TODO : SFtp receiver
//	private void registerSftpReceivePort(ReceivePort receivePort) {
//	}

	// TODO : Extract to somewhere useful!
	private String getPropertiesEntryValueByKey(ReceivePort.Properties properties, String key) {
		for(ReceivePort.Properties.Entry entry : properties.getEntry()) {
			if (entry.getKey().equals(key))
				return entry.getValue();
		}
		return null;
	}
}
