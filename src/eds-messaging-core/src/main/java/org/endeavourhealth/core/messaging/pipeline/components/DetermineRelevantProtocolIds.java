package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.DetermineRelevantProtocolIdsConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.audit.ExchangeProtocolErrorDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.transform.common.AuditWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DetermineRelevantProtocolIds extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(DetermineRelevantProtocolIds.class);

	private DetermineRelevantProtocolIdsConfig config;
	private static ExchangeProtocolErrorDalI errorDal = DalProvider.factoryExchangeProtocolErrorDal();

	public DetermineRelevantProtocolIds(DetermineRelevantProtocolIdsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);
		String systemUuid = exchange.getHeader(HeaderKeys.SenderSystemUuid);

		String json = getProtocolIdsForPublisherService(serviceUuid, systemUuid, exchange.getId());
		exchange.setHeader(HeaderKeys.ProtocolIds, json);

		//commit what we've just received to the DB
		try {
			AuditWriter.writeExchange(exchange);
		} catch (Exception ex) {
			throw new PipelineException("Failed write exchange " + exchange.getId() + " to database", ex);
		}

		LOG.debug("Data distribution protocols identified");
	}

	public static String getProtocolIdsForPublisherService(String serviceUuid, String systemUuid, UUID exchangeId) throws PipelineException {

		//find all protocols where our service is an active publisher
		LOG.debug("Getting protocols for service " + serviceUuid);
		List<LibraryItem> protocolsForService = getProtocolsForPublisherService(serviceUuid);
		LOG.debug("Found " + protocolsForService.size() + " protocols for service " + serviceUuid);

		if (protocolsForService.isEmpty()) {
			saveProtocolError(exchangeId);
			throw new PipelineException("No publisher protocols found for service " + serviceUuid);
		}

		//the above list of protocols is filtered by service only and NOT system, as we don't want to filter
		//by system on outbound transforms (e.g. FHIR->subscriber DB). But we do want to validate on system
		//before inbound transforms
		List<LibraryItem> protocolsForServiceAndSystem = new ArrayList<>();

		for (LibraryItem libraryItem: protocolsForService) {
			Protocol protocol = libraryItem.getProtocol();
			for (ServiceContract serviceContract : protocol.getServiceContract()) {
				if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
						&& serviceContract.getService().getUuid().equals(serviceUuid)
						&& serviceContract.getSystem().getUuid().equals(systemUuid)
						&& serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

					protocolsForServiceAndSystem.add(libraryItem);
					break;
				}
			}
		}

		//if there's no protocol covering our service AND system as a publisher, then that's an error
		LOG.debug("Found " + protocolsForServiceAndSystem.size() + " protocols for service " + serviceUuid + " and system " + systemUuid);
		if (protocolsForServiceAndSystem.isEmpty()) {
			saveProtocolError(exchangeId);
			throw new PipelineException("No publisher protocols found for service " + serviceUuid + " and system " + systemUuid);
		}

		//return the list of protocols for our service ONLY. When sending to subscribers, the feed can't be filtered by
		//system. Even if a subscriber protocol is set up just to take a specific system from this service,
		//we actually want to include all systems, otherwise the feed is incomplete.
		try {
			List<String> protocolIds = new ArrayList<>();
			for (LibraryItem libraryItem: protocolsForService) {
				protocolIds.add(libraryItem.getUuid());
			}
			String json = ObjectMapperPool.getInstance().writeValueAsString(protocolIds.toArray());
			LOG.debug("Returning " + json + " for service " + serviceUuid);
			return json;

		} catch (JsonProcessingException e) {
			LOG.error("Unable to serialize protocols to JSON");
			throw new PipelineException(e.getMessage(), e);
		}

	}

	private static void saveProtocolError(UUID exchangeId) throws PipelineException {

		try {
			errorDal.save(exchangeId);
		} catch (Exception e) {
			throw new PipelineException("Error saving exchange protocol error for exchange " + exchangeId);
		}
	}

	private static List<LibraryItem> getProtocolsForPublisherService(String serviceUuid) throws PipelineException {

		try {
			List<LibraryItem> ret = new ArrayList<>();

			//the above fn will return is all protocols where the service is present, but we want to filter
			//that down to only ones where our service is an active publisher
			List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid, null); //passing null means don't filter on system ID

			for (LibraryItem libraryItem: libraryItems) {
				Protocol protocol = libraryItem.getProtocol();
				if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

					for (ServiceContract serviceContract : protocol.getServiceContract()) {
						if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
								&& serviceContract.getService().getUuid().equals(serviceUuid)
								&& serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

							ret.add(libraryItem);
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



	/*@Override
	public void process(Exchange exchange) throws PipelineException {
		String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);
		String systemUuid = exchange.getHeader(HeaderKeys.SenderSystemUuid);

		// Determine relevant publisher protocols
		String protocolIdsJson = getProtocolIdsForPublisherService(serviceUuid, systemUuid, exchange.getId());
		exchange.setHeader(HeaderKeys.ProtocolIds, protocolIdsJson);

		//commit what we've just received to the DB
		try {
			AuditWriter.writeExchange(exchange);
		} catch (Exception ex) {
			throw new PipelineException("Failed write exchange " + exchange.getId() + " to database", ex);
		}

		LOG.debug("Data distribution protocols identified");
	}

	public static String getProtocolIdsForPublisherService(String serviceUuid, String systemUuid, UUID exchangeId) throws PipelineException {

		ExchangeProtocolErrorDalI errorDal = DalProvider.factoryExchangeProtocolErrorDal();
		List<String> protocolIds = getProtocolsForPublisherService(serviceUuid, systemUuid);
		if (protocolIds.size() == 0) {
			try {
				errorDal.save(exchangeId);
			} catch (Exception e) {
				throw new PipelineException("Error saving exchange protocol error for exchange " + exchangeId);
			}
			throw new PipelineException("No publisher protocols found for service " + serviceUuid + " and system " + systemUuid);
		}

		try {
			return ObjectMapperPool.getInstance().writeValueAsString(protocolIds.toArray());

		} catch (JsonProcessingException e) {
			LOG.error("Unable to serialize protocols to JSON");
			throw new PipelineException(e.getMessage(), e);
		}
	}

	private static List<String> getProtocolsForPublisherService(String serviceUuid, String systemUuid) throws PipelineException {

		try {
			List<String> ret = new ArrayList<>();

			List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid, systemUuid);

			//the above fn will return is all protocols where the service and system are present, but we want to filter
			//that down to only ones where our service and system are an active publisher
			for (LibraryItem libraryItem: libraryItems) {
				Protocol protocol = libraryItem.getProtocol();
				if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

					for (ServiceContract serviceContract : protocol.getServiceContract()) {
						if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
								&& serviceContract.getService().getUuid().equals(serviceUuid)
								&& serviceContract.getSystem().getUuid().equals(systemUuid)
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
	}*/



}
