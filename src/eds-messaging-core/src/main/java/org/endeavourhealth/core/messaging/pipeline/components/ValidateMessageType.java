package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.ValidateMessageTypeConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

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

/*
			Boolean senderIsValid = Arrays.stream(libraryItemList)
					.map(li -> li.getProtocol().getServiceContract())
					.flatMap(Collection::stream)
					.filter(serviceContract ->
							serviceContract.getType().equals(ServiceContractType.PUBLISHER)
							&& serviceContract.getService().getUuid().equals(serviceUuid))
					.map(ServiceContract::getTechnicalInterface)
					.anyMatch(ti ->
							ti.getName().equals(sourceSystem) &&
							ti.getMessageFormat().equals(messageFormat) &&
							ti.getMessageFormatVersion().equals(formatVersion)
					);

			if (senderIsValid == false)
				throw new PipelineException("No valid publisher service contracts found");
*/

		} catch (IOException e) {
			LOG.error("Error parsing protocol JSON");
			throw new PipelineException("Error parsing protocol JSON : " + e.getMessage(), e);
		}

		LOG.debug("Message validated");
	}
}
