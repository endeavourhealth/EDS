package org.endeavourhealth.core.queueing;

import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.ExpiringObject;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class RoutingManager {
	private static final Logger LOG = LoggerFactory.getLogger(RoutingManager.class);

	private static RoutingManager instance;

	private ExpiringObject<Map<String, List<RouteGroup>>> cachedRoutingsByExchangeName = new ExpiringObject(1000L * 60L * 5L);
	private ExpiringObject<Map<UUID, List<RoutingOverride>>> cachedRoutingOverrides = new ExpiringObject(1000L * 60L * 5L);

	public static RoutingManager instance() {
		if (instance == null) {
			instance = new RoutingManager();
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

		Map<String, List<RouteGroup>> map = this.cachedRoutingsByExchangeName.get();

		//if our first time, or the cache has expired, then retreive from the ConfigManager and generate the map
		if (map == null) {
			LOG.trace("Routing cache is null, so rebuilding");
			map = new HashMap<>();

			String routings = ConfigManager.getConfiguration("routings");
			try {
				RouteGroup[] arr = ObjectMapperPool.getInstance().readValue(routings, RouteGroup[].class);

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

			this.cachedRoutingsByExchangeName.set(map);
		}

		return map.get(exchangeName);
	}


	public String findRoutingOverride(String exchangeName, UUID serviceId) throws PipelineException {

		Map<UUID, List<RoutingOverride>> map = cachedRoutingOverrides.get();
		if (map == null) {
			LOG.trace("Routing override cache is null, so rebuilding");
			map = new HashMap<>();

			try {
				String json = ConfigManager.getConfiguration("routing_overrides");
				if (!Strings.isNullOrEmpty(json)) {

					RoutingOverride[] arr = ObjectMapperPool.getInstance().readValue(json, RoutingOverride[].class);
					for (RoutingOverride o: arr) {
						UUID oServiceId = o.getServiceId();

						List<RoutingOverride> list = map.get(oServiceId);
						if (list == null) {
							list = new ArrayList<>();
							map.put(oServiceId, list);
						}
						list.add(o);
					}
				}

			} catch (Exception ex) {
				throw new PipelineException("Failed to populate routing overrides map", ex);
			}

			this.cachedRoutingOverrides.set(map);
		}

		List<RoutingOverride> list = map.get(serviceId);
		if (list != null) {
			for (RoutingOverride o: list) {
				if (o.getExchangeName().equals(exchangeName)) {
					return o.getRoutingKey();
				}
			}
		}
		return null;
	}
}
