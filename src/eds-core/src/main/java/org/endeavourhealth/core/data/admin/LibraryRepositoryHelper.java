package org.endeavourhealth.core.data.admin;

import org.endeavourhealth.core.data.admin.models.*;
import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LibraryRepositoryHelper {
	private static final String XSD = "QueryDocument.xsd";

	private static final LibraryRepository repository = new LibraryRepository();

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
			LibraryItem libraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, xml, XSD);
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
					LibraryItem systemLibraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, systemLibraryItemXml, XSD);
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
		LibraryItem libraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, xml, XSD);
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

		if (technicalInterface == null) {
			throw new Exception("Failed to find technical interface for system " + systemUuidStr + " and technical interface " + technicalInterfaceUuidStr);
		}

		return technicalInterface;
	}
}
