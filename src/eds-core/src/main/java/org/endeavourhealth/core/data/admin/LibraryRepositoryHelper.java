package org.endeavourhealth.core.data.admin;

import org.endeavourhealth.core.data.admin.models.*;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibraryRepositoryHelper {
	private static final String XSD = "QueryDocument.xsd";

	public static List<LibraryItem> getProtocolsByServiceId(String serviceId) throws ParserConfigurationException, IOException, SAXException, JAXBException {
		DefinitionItemType itemType = DefinitionItemType.Protocol;

		Iterable<ActiveItem> activeItems = null;
		List<Item> items = new ArrayList();

		LibraryRepository repository = new LibraryRepository();

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
					ret.add(XmlSerializer.deserializeFromString(LibraryItem.class, xml, XSD));
				}
			}
		}

		return ret;
	}
}
