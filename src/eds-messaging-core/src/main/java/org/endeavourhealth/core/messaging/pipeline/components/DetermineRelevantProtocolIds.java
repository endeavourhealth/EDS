package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.DetermineRelevantProtocolIdsConfig;
import org.endeavourhealth.core.data.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class DetermineRelevantProtocolIds extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(DetermineRelevantProtocolIds.class);

	private DetermineRelevantProtocolIdsConfig config;

	public DetermineRelevantProtocolIds(DetermineRelevantProtocolIdsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);

		// Determine relevant publisher protocols
		List<String> protocolIds = getProtocolsForPublisherService(serviceUuid);
		if (protocolIds.size() == 0)
			throw new PipelineException("No publisher protocols found for service " + serviceUuid);

		try {
			String protocolsJson = ObjectMapperPool.getInstance().writeValueAsString(protocolIds.toArray());
			exchange.setHeader(HeaderKeys.Protocols, protocolsJson);
		} catch (JsonProcessingException e) {
			LOG.error("Unable to serialize protocols to JSON");
			throw new PipelineException(e.getMessage(), e);
		}

		//commit what we've just received to the DB
		AuditWriter.writeExchange(exchange);

		LOG.debug("Data distribution protocols identified");
	}

	private List<String> getProtocolsForPublisherService(String serviceUuid) throws PipelineException {

		try {
			List<LibraryItem> libraryItemList = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid);

			// Get protocols where service is publisher
			return libraryItemList.stream()
					.filter(
							libraryItem -> libraryItem.getProtocol().getServiceContract().stream()
									.anyMatch(sc ->
											sc.getType().equals(ServiceContractType.PUBLISHER)
													&& sc.getService().getUuid().equals(serviceUuid)))
					.map(t -> t.getUuid())
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new PipelineException(e.getMessage(), e);
		}

	}

	/*@Override
	public void process(Exchange exchange) throws PipelineException {
		String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);

		// Determine relevant publisher protocols
		List<LibraryItem> protocols = getProtocolsForPublisherService(serviceUuid);
		if (protocols.size() == 0)
			throw new PipelineException("No publisher protocols found for service " + serviceUuid);

		try {
			String protocolsJson = ObjectMapperPool.getInstance().writeValueAsString(protocols.toArray());
			exchange.setHeader(HeaderKeys.Protocols, protocolsJson);
		} catch (JsonProcessingException e) {
			LOG.error("Unable to serialize protocols to JSON");
			throw new PipelineException(e.getMessage(), e);
		}

		//commit what we've just received to the DB
		AuditWriter.writeExchange(exchange);

		LOG.debug("Data distribution protocols identified");
	}

	private List<LibraryItem> getProtocolsForPublisherService(String serviceUuid) throws PipelineException {
		List<LibraryItem> libraryItemList;

		// Get all protocols the service is involved in...
		try {
			libraryItemList = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid);
		} catch (Exception e) {
			throw new PipelineException(e.getMessage(), e);
		}

		// Get protocols where service is publisher
		return libraryItemList.stream()
				.filter(
						libraryItem -> libraryItem.getProtocol().getServiceContract().stream()
								.anyMatch(sc ->
										sc.getType().equals(ServiceContractType.PUBLISHER)
										&& sc.getService().getUuid().equals(serviceUuid)))
				.collect(Collectors.toList());
	}*/
}
