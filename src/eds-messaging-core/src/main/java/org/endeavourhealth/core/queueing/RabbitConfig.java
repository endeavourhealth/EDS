package org.endeavourhealth.core.queueing;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitConfig {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitConfig.class);

	private String password = null;
	private String username = null;
	private String nodes = null;
	private String managementNodes = null;
	private String sslProtocol = null;
	private String managementProtocol = null;

	private static RabbitConfig instance;

	public static RabbitConfig getInstance()  {
		if (instance == null)
			instance = new RabbitConfig();

		return instance;
	}

	private RabbitConfig() {

		try {
			JsonNode rabbitConfig = ConfigManager.getConfigurationAsJson("rabbit");

			this.nodes = rabbitConfig.get("nodes").asText();
			this.username = rabbitConfig.get("username").asText();
			this.password = rabbitConfig.get("password").asText();

			JsonNode sslNode = rabbitConfig.get("ssl");
			if (sslNode != null) {
				this.sslProtocol = sslNode.asText();
			}


			if (rabbitConfig.has("managementPortOffset")) {
				LOG.trace("Loading Rabbit config old way, using managementPortOffset");

				//the management API is on a different port on each node, which we can
				//calculate from the port offset in the configuration
				int portOffset = rabbitConfig.get("managementPortOffset").asInt();
				String[] nodeArray = nodes.split(" *, *");
				for (int i=0; i<nodeArray.length; i++) {
					String node = nodeArray[i];
					int idx = node.indexOf(':');
					String host = node.substring(0, idx);
					String port = node.substring(idx+1);
					int portInt = Integer.parseInt(port) + portOffset;
					node = host + ":" + portInt;
					nodeArray[i] = node;
				}
				this.managementNodes = String.join(",", nodeArray);

				//work out management protocol based on the port
				if (managementNodes.endsWith("15671")) {
					this.managementProtocol = "https";
				} else {
					this.managementProtocol = "http";
				}

			} else {
				//LOG.trace("Loading Rabbit config new way 1");

				int managementPort = rabbitConfig.get("managementPort").asInt();
				String[] nodeArray = nodes.split(" *, *");
				for (int i=0; i<nodeArray.length; i++) {
					String node = nodeArray[i];
					int idx = node.indexOf(':');
					String host = node.substring(0, idx);
					node = host + ":" + managementPort;
					nodeArray[i] = node;
				}

				this.managementNodes = String.join(",", nodeArray);
				this.managementProtocol = rabbitConfig.get("managementProtocol").asText();
			}

		} catch (Exception e) {
			LOG.error("Failed to load rabbit config", e);
		}
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public String getNodes() {
		return nodes;
	}

	public String getManagementNodes() {
		return managementNodes;
	}

	public String getSslProtocol() {
		return sslProtocol;
	}

	public String getManagementProtocol() {
		return managementProtocol;
	}
}
