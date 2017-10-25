package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.configuration.DetermineRelevantProtocolIdsConfig;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
		String protocolIdsJson = getProtocolIdsForPublisherService(serviceUuid);
		exchange.setHeader(HeaderKeys.ProtocolIds, protocolIdsJson);

		//commit what we've just received to the DB
		try {
			AuditWriter.writeExchange(exchange);
		} catch (Exception ex) {
			throw new PipelineException("Failed write exchange " + exchange.getId() + " to database", ex);
		}

		LOG.debug("Data distribution protocols identified");
	}

	public static String getProtocolIdsForPublisherService(String serviceUuid) throws PipelineException {

		List<String> protocolIds = getProtocolsForPublisherService(serviceUuid);
		if (protocolIds.size() == 0)
			throw new PipelineException("No publisher protocols found for service " + serviceUuid);

		try {
			return ObjectMapperPool.getInstance().writeValueAsString(protocolIds.toArray());

		} catch (JsonProcessingException e) {
			LOG.error("Unable to serialize protocols to JSON");
			throw new PipelineException(e.getMessage(), e);
		}
	}

	private static List<String> getProtocolsForPublisherService(String serviceUuid) throws PipelineException {

		try {
			List<String> ret = new ArrayList<>();

			List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid);
			for (LibraryItem libraryItem: libraryItems) {
				Protocol protocol = libraryItem.getProtocol();
				if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

					for (ServiceContract serviceContract : protocol.getServiceContract()) {
						if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
								&& serviceContract.getService().getUuid().equals(serviceUuid)
								&& serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

							ret.add(libraryItem.getUuid());
							break;
						}
					}
				}
			}

			return ret;

		} catch (Exception ex) {
			throw new PipelineException("Error getting protocols for service " + serviceUuid, ex);
		}
	}
	/*private static List<String> getProtocolsForPublisherService(String serviceUuid) throws PipelineException {

		try {
			List<LibraryItem> libraryItemList = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid);

			// Get protocols where service is publisher
			return libraryItemList.stream()
					.filter(
							libraryItem -> libraryItem.getProtocol().getServiceContract().stream()
									.anyMatch(sc ->
											sc.getType().equals(ServiceContractType.PUBLISHER)
											&& sc.getService().getUuid().equals(serviceUuid)
											&& sc.getActive() == ServiceContractActive.TRUE //added missing check
									))
					.map(t -> t.getUuid())
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new PipelineException(e.getMessage(), e);
		}

	}*/

}
