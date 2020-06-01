package org.endeavourhealth.core.queueing;

import org.endeavourhealth.common.cache.CacheManager;
import org.endeavourhealth.common.cache.ICache;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class RoutingManager implements ICache {
	private static final Logger LOG = LoggerFactory.getLogger(RoutingManager.class);

	private static RoutingManager instance;

	//private RouteGroup[] routingMap;
	private Map<String, List<RouteGroup>> cachedRoutingsByExchangeName;
	private Date cacheExpiry = null;

	public static RoutingManager getInstance() {
		if (instance == null) {
			instance = new RoutingManager();
			CacheManager.registerCache(instance);
		}
		return instance;
	}

	public String getRoutingKeyForIdentifier(String exchangeName, String identifier) throws PipelineException {
		List<RouteGroup> routings = getRoutingMapForExchange(exchangeName);
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

	private List<RouteGroup> getRoutingMapForExchange(String exchangeName) throws PipelineException {

		if (cachedRoutingsByExchangeName == null
				|| cacheExpiry.before(new Date())) {
			//LOG.debug("Re-creating routing map cache");

			Map<String, List<RouteGroup>> map = new HashMap<>();

			String routings = ConfigManager.getConfiguration("routings");

			try {
				RouteGroup[] arr = ObjectMapperPool.getInstance().readValue(routings, RouteGroup[].class);
				//LOG.debug("Routing table loaded : " + routings);
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

			this.cacheExpiry = new Date(System.currentTimeMillis() + (5L * 1000L * 60L)); //expire this cache in five minutes
			this.cachedRoutingsByExchangeName = map;
		}

		return cachedRoutingsByExchangeName.get(exchangeName);
	}

	@Override
	public String getName() {
		return "RoutingManager";
	}

	@Override
	public
	long getSize() {
		return cachedRoutingsByExchangeName == null ? 0 : cachedRoutingsByExchangeName.size();
	}

	@Override
	public void clearCache() {
		cachedRoutingsByExchangeName = null;
	}

}
