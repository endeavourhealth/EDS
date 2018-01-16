package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ValidateMessageTypeConfig;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class ValidateMessageType extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(ValidateMessageType.class);

	private ValidateMessageTypeConfig config;

	public ValidateMessageType(ValidateMessageTypeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);
		String sourceSystem = exchange.getHeader(HeaderKeys.SourceSystem);
		// String messageType = exchange.getHeader(HeaderKeys.MessageEvent);
		//String messageFormat = exchange.getHeader(HeaderKeys.MessageFormat);
		String formatVersion = exchange.getHeader(HeaderKeys.SystemVersion);

		// Get the (publisher) protocols
		String[] protocolIds = null;
		try {
			protocolIds = exchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);
		} catch (Exception ex) {
			throw new PipelineException("Failed to read protocol IDs from exchange " + exchange.getId());
		}

		// Ensure at least one of the publisher protocols is for this system/format
		boolean senderIsValid = false;

		for (String protocolId: protocolIds) {

			UUID protocolUuid = UUID.fromString(protocolId);
			LibraryItem libraryItem = null;
			try {
				libraryItem = LibraryRepositoryHelper.getLibraryItem(protocolUuid);
			} catch (Exception e) {
				throw new PipelineException("Failed to read protocol item for " + protocolId, e);
			}

			Protocol protocol = libraryItem.getProtocol();
			List<ServiceContract> serviceContracts = protocol.getServiceContract();

			for (ServiceContract serviceContract: serviceContracts) {
				if (!serviceContract.getType().equals(ServiceContractType.PUBLISHER)) {
					continue;
				}
				if (!serviceContract.getService().getUuid().equals(serviceUuid)) {
					continue;
				}

				//because we've re-loaded the protocol from the DB, the technical interface is only populated
				//with the UUID, so we need to manually load it to get the extra details
				String technicalInterfaceUuidStr = serviceContract.getTechnicalInterface().getUuid();
				String systemUuidStr = serviceContract.getSystem().getUuid();
				TechnicalInterface technicalInterface = null;
				try {
					//use a function that caches them for a minute at a time
					technicalInterface = LibraryRepositoryHelper.getTechnicalInterfaceDetailsUsingCache(systemUuidStr, technicalInterfaceUuidStr);
					//technicalInterface = LibraryRepositoryHelper.getTechnicalInterfaceDetails(systemUuidStr, technicalInterfaceUuidStr);
				} catch (Exception ex) {
					throw new PipelineException("Failed to retrieve technical interface", ex);
				}
				//TechnicalInterface technicalInterface = serviceContract.getTechnicalInterface();

				if (!technicalInterface.getMessageFormat().equals(sourceSystem)) {
					continue;
				}
				if (!technicalInterface.getMessageFormatVersion().equals(formatVersion)) {
					continue;
				}

				senderIsValid = true;
			}
		}

		if (!senderIsValid) {
			LOG.error("Failed to find publisher service contract for service {} software {} version {}", serviceUuid, sourceSystem, formatVersion);
			LOG.error("Checked {} protocols", protocolIds.length);
			for (String protocolId: protocolIds) {
				LOG.error("Protocol {}", protocolId);
			}

			throw new PipelineException("No valid publisher service contracts found");
		}

		LOG.debug("Message validated");
	}



	/*@Override
	public void process(Exchange exchange) throws PipelineException {
		String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);
		String sourceSystem = exchange.getHeader(HeaderKeys.SourceSystem);
		// String messageType = exchange.getHeader(HeaderKeys.MessageEvent);
		//String messageFormat = exchange.getHeader(HeaderKeys.MessageFormat);
		String formatVersion = exchange.getHeader(HeaderKeys.SystemVersion);

		// Get the (publisher) protocols
		String protocolJson = exchange.getHeader(HeaderKeys.Protocols);
		LibraryItem[] libraryItemList;
		try {
			libraryItemList = ObjectMapperPool.getInstance().readValue(protocolJson, LibraryItem[].class);

			// Ensure at least one of the publisher protocols is for this system/format
			boolean senderIsValid = false;
			for (LibraryItem libraryItem: libraryItemList) {
				Protocol protocol = libraryItem.getProtocol();
				List<ServiceContract> serviceContracts = protocol.getServiceContract();

				for (ServiceContract serviceContract: serviceContracts) {
					if (!serviceContract.getType().equals(ServiceContractType.PUBLISHER)) {
						continue;
					}
					if (!serviceContract.getService().getUuid().equals(serviceUuid)) {
						continue;
					}

					TechnicalInterface technicalInterface = serviceContract.getTechnicalInterface();
					if (!technicalInterface.getMessageFormat().equals(sourceSystem)) {
						continue;
					}
					if (!technicalInterface.getMessageFormatVersion().equals(formatVersion)) {
						continue;
					}

					senderIsValid = true;
				}

			}
			if (!senderIsValid) {
				LOG.error("Failed to find publisher protocol for service {} software {} version {}", serviceUuid, sourceSystem, formatVersion);
				LOG.error("Checked {} protocols", libraryItemList.length);
				LOG.error(protocolJson);

				throw new PipelineException("No valid publisher service contracts found");
			}

		} catch (IOException e) {
			LOG.error("Error parsing protocol JSON", e);
			throw new PipelineException("Error parsing protocol JSON : " + e.getMessage(), e);
		}

		LOG.debug("Message validated");
	}*/
}
