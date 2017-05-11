package org.endeavourhealth.core.queueing;

import org.endeavourhealth.common.cache.CacheManager;
import org.endeavourhealth.common.cache.ICacheable;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	//private RouteGroup[] routingMap;
	private Map<String, List<RouteGroup>> cachedRoutings;

	public String getRoutingKeyForIdentifier(String exchangeName, String identifier) throws PipelineException {
		List<RouteGroup> routings = getRoutingMap(exchangeName);
		if (routings == null) {
			throw new PipelineException("No routings found for exchange " + exchangeName);
		}

		for (RouteGroup routeGroup : routings) {

			if (Pattern.matches(routeGroup.getRegex(), identifier)) {
				//LOG.debug("Routing key [" + routeGroup.getRouteKey()+ "] found for identifier [" + identifier + "]");
				return routeGroup.getRouteKey();
			}
		}

		throw new PipelineException("No routing key found for value [" + identifier + "] and exchange name " + exchangeName);
	}

	private List<RouteGroup> getRoutingMap(String exchangeName) throws PipelineException {
		if (cachedRoutings == null) {

			Map<String, List<RouteGroup>> map = new HashMap<>();

			String routings = ConfigManager.getConfiguration("routings");

			try {
				RouteGroup[] arr = ObjectMapperPool.getInstance().readValue(routings, RouteGroup[].class);
				LOG.debug("Routing table loaded : " + routings);
				for (RouteGroup r: arr) {

					List<RouteGroup> list = map.get(r.getExchangeName());
					if (list == null) {
						list = new ArrayList<>();
						map.put(r.getExchangeName(), list);
					}
					list.add(r);
				}
			}
			catch (Exception ex) {
				throw new PipelineException("Failed to populate routing map from JSON " + routings, ex);
			}

			this.cachedRoutings = map;
		}

		return cachedRoutings.get(exchangeName);
	}

	public void clearCache() {
		cachedRoutings = null;
	}

	/*public String getRoutingKeyForIdentifier(String identifier) {
		RouteGroup[] routingMap = getRoutingMap();

		for (RouteGroup routeGroup : routingMap) {
			if (Pattern.matches(routeGroup.getRegex(), identifier)) {
				//LOG.debug("Routing key [" + routeGroup.getRouteKey()+ "] found for identifier [" + identifier + "]");
				return routeGroup.getRouteKey();
			}
		}

		LOG.error("No routing key found for identifier [" + identifier + "] - set to [Unknown]");
		return "Unknown";
	}

	private RouteGroup[] getRoutingMap() {
		if (routingMap == null) {
			String routings = ConfigManager.getConfiguration("routings");

			try {
				routingMap = ObjectMapperPool.getInstance().readValue(routings, RouteGroup[].class);
				LOG.debug("Routing table loaded : " + routings);
			}
			catch (Exception e) {
				LOG.error("Error reading routing config, falling back to defaults", e);
				routingMap = new RouteGroup[]{
						new RouteGroup("Default A-M", "Default fallback group, initial character A-M", "A-M", "[A-M].*"),
						new RouteGroup("Default A-Z", "Default fallback group, initial character N-Z", "N-Z", "[N-Z].*"),
						new RouteGroup("Fallback", "Default fallback group, all remaining", "Fallback", ".*"),
						// Any others will fall back to the "Unknown" routing key
				};
			}
		}

		return routingMap;
	}

	public void clearCache() {
		routingMap = null;
	}*/
}
