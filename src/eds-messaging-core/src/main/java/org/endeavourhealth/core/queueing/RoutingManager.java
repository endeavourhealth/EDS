package org.endeavourhealth.core.queueing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.cache.CacheManager;
import org.endeavourhealth.core.cache.ICacheable;
import org.endeavourhealth.core.data.config.ConfigKeys;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class RoutingManager implements ICacheable {
	private static final Logger LOG = LoggerFactory.getLogger(RoutingManager.class);
	private static RoutingManager instance;

	public static RoutingManager getInstance() {
		if (instance == null) {
			instance = new RoutingManager();
			CacheManager.registerCache(instance);
		}
		return instance;
	}

	private RouteGroup[] routingMap;

	public String getRoutingKeyForIdentifier(String identifier) {
		RouteGroup[] routingMap = getRoutingMap();

		for (RouteGroup routeGroup : routingMap) {
			if (Pattern.matches(routeGroup.getRegex(), identifier)) {
				LOG.debug("Routing key [" + routeGroup.getRouteKey()+ "] found for identifier [" + identifier + "]");
				return routeGroup.getRouteKey();
			}
		}

		LOG.error("No routing key found for identifier [" + identifier + "] - set to [Unknown]");
		return "Unknown";
	}

	public void clearCache() {
		routingMap = null;
	}

	private RouteGroup[] getRoutingMap() {
		if (routingMap == null) {
			ConfigurationResource routingConfig = new ConfigurationRepository().getByKey(ConfigKeys.RouteGroupings);

			try {
				routingMap = new ObjectMapper().readValue(routingConfig.getConfigurationData(), RouteGroup[].class);
				LOG.debug("Routing table loaded : " + routingConfig.getConfigurationData());
			}
			catch (Exception e) {
				LOG.error("Error reading routing config, falling back to defaults");
				routingMap = new RouteGroup[]{
						new RouteGroup("Default A-M", "Default fallback group, initial character A-M", "A-M", "[A-M].*"),
						new RouteGroup("Default A-Z", "Default fallback group, initial character N-Z", "N-Z", "[N-Z].*"),
						new RouteGroup("Default 0-9", "Default fallback group, initial character 0-9", "0-9", "[0-9].*"),
						// Any others will fall back to the "Unknown" routing key
				};
			}
		}

		return routingMap;
	}
}
