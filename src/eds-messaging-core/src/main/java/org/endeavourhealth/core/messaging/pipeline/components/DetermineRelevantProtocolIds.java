package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.configuration.DetermineRelevantProtocolIdsConfig;
import org.endeavourhealth.core.data.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.LibraryItem;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DetermineRelevantProtocolIds extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(DetermineRelevantProtocolIds.class);

	private DetermineRelevantProtocolIdsConfig config;

	public DetermineRelevantProtocolIds(DetermineRelevantProtocolIdsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		// Get service id
		String senderId = exchange.getHeader(HeaderKeys.Sender);

		// Get service id from sender id
		ServiceRepository serviceRepository = new ServiceRepository();
		Iterable<Service> services = serviceRepository.getByLocalIdentifier(senderId);

		if (!services.iterator().hasNext())
			throw new PipelineException("No service found for local identifier");

		Service service = services.iterator().next();

		// Determine relevant publisher protocols
		List<LibraryItem> protocols = getProtocolsForPublisherService(service.getId());
		if (protocols.size() == 0)
			throw new PipelineException("No publisher protocols found for service");

		ObjectMapper mapper = new ObjectMapper();
		try {
			String protocolsJson = mapper.writeValueAsString(protocols);
			exchange.setHeader(HeaderKeys.ProtocolData, protocolsJson);
		} catch (JsonProcessingException e) {
			LOG.error("Unable to serialize protocols to JSON");
			throw new PipelineException(e.getMessage());
		}

		List<String> protocolIds = protocols.stream().map(LibraryItem::getUuid).collect(Collectors.toList());
		exchange.setHeader(HeaderKeys.ProtocolIds, String.join(",",protocolIds));

		LOG.debug("Data distribution protocols identified");
	}

	private List<LibraryItem> getProtocolsForPublisherService(UUID serviceId) throws PipelineException {
		List<LibraryItem> libraryItemList;

		// Get all protocols the service is involved in...
		try {
			libraryItemList = LibraryRepositoryHelper.getProtocolsByServiceId(serviceId.toString());
		} catch (Exception e) {
			throw new PipelineException(e.getMessage());
		}

		// Get protocols where service is publisher
		return libraryItemList.stream()
				.filter(
						libraryItem -> libraryItem.getProtocol().getServiceContract().stream()
								.anyMatch(sc ->
										sc.getType().equals("PUBLISHER")
										&& sc.getService().getUuid().equals(serviceId.toString())))
				.collect(Collectors.toList());
	}
}
