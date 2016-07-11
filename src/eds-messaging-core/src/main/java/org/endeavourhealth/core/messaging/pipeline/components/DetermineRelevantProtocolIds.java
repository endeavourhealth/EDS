package org.endeavourhealth.core.messaging.pipeline.components;

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

public class DetermineRelevantProtocolIds implements PipelineComponent {
	private static final String apiAddress = "http://localhost:8888/api";
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
		List<String> protocolIds = getProtocolIdsForPublisherService(service.getId());
		if (protocolIds.size() == 0)
			throw new PipelineException("No publisher protocols found for service");

		exchange.setHeader(HeaderKeys.ProtocolIds, String.join(",", protocolIds));
		LOG.debug("Data distribution protocols identified");
	}

	private List<String> getProtocolIdsForPublisherService(UUID serviceId) throws PipelineException {
		List<LibraryItem> libraryItemList = null;

		// Get all protocols the service is involved in...
		try {
			libraryItemList = LibraryRepositoryHelper.getProtocolsByServiceId(serviceId.toString());
		} catch (Exception e) {
			throw new PipelineException(e.getMessage());
		}

		// Get protocols where service is publisher
		List<LibraryItem> publisherProtocols = libraryItemList.stream()
				.filter(
						libraryItem -> libraryItem.getProtocol().getServiceContract().stream()
								.anyMatch(sc ->
										sc.getType().equals("PUBLISHER")
										&& sc.getService().getUuid().equals(serviceId.toString())))
				.collect(Collectors.toList());

		// Map to library item ids
		return 	publisherProtocols.stream().map(LibraryItem::getUuid).collect(Collectors.toList());
	}
}
