package org.endeavourhealth.core.queueing;

import org.endeavourhealth.core.cache.CacheManager;
import org.endeavourhealth.core.cache.ICacheable;
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

	private Route[] routingMap;

	public String getRoutingKeyForIdentifier(String identifier) {
		Route[] routingMap = getRoutingMap();

		for (Route route : routingMap) {
			if (Pattern.matches(route.regex, identifier)) {
				LOG.debug("Routing key [" + route.routeKey + "] found for identifier [" + identifier + "]");
				return route.routeKey;
			}
		}

		LOG.error("No routing key found for identifier [" + identifier + "] - set to [Unknown]");
		return "Unknown";
	}

	public void clearCache() {
		routingMap = null;
	}

	private Route[] getRoutingMap() {
		if (routingMap == null) {
			// TODO : Move routing map to config/db
			routingMap = new Route[]{
					new Route("GPs_Eng_Wls", "[A-H,J-N,P,W,Y][0-9][0-9][0-9][0-9][0-9]"),
					new Route("GPs_Sct", "S[0-9][0-9][0-9][0-9][0-9]"),
					new Route("A-M", "[A-M].*"),
					new Route("N-Z", "[N-Z].*"),
					new Route("0-9", "[0-9].*"),
			};
		}

		return routingMap;
	}
}
