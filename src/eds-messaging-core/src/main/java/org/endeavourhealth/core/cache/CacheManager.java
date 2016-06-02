package org.endeavourhealth.core.cache;

import java.util.ArrayList;
import java.util.List;

public class CacheManager {
	private static List<ICacheable> caches = new ArrayList<>();

	public static void registerCache(ICacheable cache) {
		caches.add(cache);
	}

	public static void clearCaches() {
		caches.forEach(ICacheable::clearCache);
	}
}
