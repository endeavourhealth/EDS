package org.endeavourhealth.core.data.admin;

import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LibraryRepositoryHelper {

	//disable validation when READING XML (it's still done when writing)
	//private static final String XSD = "QueryDocument.xsd";

	private static final long CACHE_DURATION = 1000 * 60; //cache objects for 60s

	private static final LibraryRepository repository = new LibraryRepository();

	private static final Map<String, ExpiringCache<TechnicalInterface>> technicalInterfaceCache = new ConcurrentHashMap<>();
	private static final Map<String, ExpiringCache<LibraryItem>> libraryItemCache = new ConcurrentHashMap<>();

	public static List<LibraryItem> getProtocolsByServiceId(String serviceId) throws ParserConfigurationException, IOException, SAXException, JAXBException {
		DefinitionItemType itemType = DefinitionItemType.Protocol;

		Iterable<ActiveItem> activeItems = null;
		List<Item> items = new ArrayList();

		activeItems = repository.getActiveItemByTypeId(itemType.getValue(), false);

		for (ActiveItem activeItem: activeItems) {
			Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
			if (item.getIsDeleted()==false)
				items.add(item);
		}

		List<LibraryItem> ret = new ArrayList<>();

		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			String xml = item.getXmlContent();
			LibraryItem libraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, xml, null);
			//LibraryItem libraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, xml, XSD);
			Protocol protocol = libraryItem.getProtocol();
			List<ServiceContract> serviceContracts = protocol.getServiceContract();
			for (int s = 0; s < serviceContracts.size(); s++) {
				ServiceContract service = serviceContracts.get(s);
				if (service.getService().getUuid().equals(serviceId)) {
					// Load full system details
					String systemUuid = service.getSystem().getUuid();
					ActiveItem activeSystemItem = repository.getActiveItemByItemId(UUID.fromString(systemUuid));
					Item systemItem = repository.getItemByKey(activeSystemItem.getItemId(), activeSystemItem.getAuditId());
					String systemLibraryItemXml = systemItem.getXmlContent();

					LibraryItem systemLibraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, systemLibraryItemXml, null);
					//LibraryItem systemLibraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, systemLibraryItemXml, XSD);
					System system = systemLibraryItem.getSystem();
					service.setSystem(system);

					TechnicalInterface technicalInterface = system.getTechnicalInterface().stream()
							.filter(ti -> ti.getUuid().equals(service.getTechnicalInterface().getUuid()))
							.findFirst()
							.get();

					service.setTechnicalInterface(technicalInterface);

					ret.add(libraryItem);
				}
			}
		}

		return ret;
	}

	public static LibraryItem getLibraryItem(UUID itemUuid) throws Exception {

		ActiveItem activeItem = repository.getActiveItemByItemId(itemUuid);
		Item item = repository.getItemByKey(itemUuid, activeItem.getAuditId());

		String xml = item.getXmlContent();
		LibraryItem libraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, xml, null);
		//LibraryItem libraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, xml, XSD);
		return libraryItem;

	}

	public static TechnicalInterface getTechnicalInterfaceDetails(String systemUuidStr, String technicalInterfaceUuidStr) throws Exception {

		UUID systemUuid = UUID.fromString(systemUuidStr);

		LibraryItem libraryItem = null;
		try {
			libraryItem = LibraryRepositoryHelper.getLibraryItem(systemUuid);
		} catch (Exception e) {
			throw new Exception("Failed to read library item for " + systemUuidStr, e);
		}

		System system = libraryItem.getSystem();
		TechnicalInterface technicalInterface = system
				.getTechnicalInterface()
				.stream()
				.filter(ti -> ti.getUuid().equals(technicalInterfaceUuidStr))
				.findFirst()
				.get();

		return technicalInterface;
	}

	/**
	 * uses a cache to cut load on deserialising Technical Interfaces. Cached TIs only stay in the cache for a minute before
	 * being reloaded from the DB. Would ideally like to use JCS for this caching, but that requires the cached object
	 * implement Serializable, which I don't want to do. So this is a quick alternative.
     */
	public static TechnicalInterface getTechnicalInterfaceDetailsUsingCache(String systemUuidStr, String technicalInterfaceUuidStr) throws Exception {

		String cacheKey = systemUuidStr + ":" + technicalInterfaceUuidStr;
		ExpiringCache<TechnicalInterface> cacheWrapper = technicalInterfaceCache.get(cacheKey);
		if (cacheWrapper == null
				|| cacheWrapper.isExpired()) {

			synchronized (technicalInterfaceCache) {

				//once in the sync block, make another check, in case another thread has refreshed our cache for us
				cacheWrapper = technicalInterfaceCache.get(cacheKey);
				if (cacheWrapper == null
						|| cacheWrapper.isExpired()) {

					TechnicalInterface ti = getTechnicalInterfaceDetails(systemUuidStr, technicalInterfaceUuidStr);
					cacheWrapper = new ExpiringCache<>(ti, CACHE_DURATION);
					technicalInterfaceCache.put(cacheKey, cacheWrapper);
				}
			}
		}
		return cacheWrapper.getObject();
	}

	/**
	 * uses a cache to cut load on deserialising LibraryItems (particularly protocols). Library items
	 * stay in the cache for one minute before expiring.
     */
	public static LibraryItem getLibraryItemUsingCache(UUID itemUuid) throws Exception {
		String cacheKey = itemUuid.toString();
		ExpiringCache<LibraryItem> cacheWrapper = libraryItemCache.get(cacheKey);
		if (cacheWrapper == null
				|| cacheWrapper.isExpired()) {

			synchronized (libraryItemCache) {

				//once in the sync block, make another check, in case another thread has refreshed our cache for us
				cacheWrapper = libraryItemCache.get(cacheKey);
				if (cacheWrapper == null
						|| cacheWrapper.isExpired()) {

					LibraryItem libraryItem = getLibraryItem(itemUuid);
					cacheWrapper = new ExpiringCache<>(libraryItem, CACHE_DURATION);
					libraryItemCache.put(cacheKey, cacheWrapper);
				}
			}
		}
		return cacheWrapper.getObject();
	}
}

/**
 * simple cache wrapper to allow us to expire items in the map
 */
class ExpiringCache<T> {
	private T object;
	private long expiry;

	public ExpiringCache(T object, long msLife) {
		this.object = object;
		this.expiry = java.lang.System.currentTimeMillis() + msLife;
	}

	public boolean isExpired() {
		return java.lang.System.currentTimeMillis() > expiry;
	}

	public T getObject() {
		return object;
	}

	public long getExpiry() {
		return expiry;
	}
}
