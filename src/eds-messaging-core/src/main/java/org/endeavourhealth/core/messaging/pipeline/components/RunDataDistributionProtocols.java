package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.RunDataDistributionProtocolsConfig;
import org.endeavourhealth.core.data.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RunDataDistributionProtocols extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(RunDataDistributionProtocols.class);

	private RunDataDistributionProtocolsConfig config;

	public RunDataDistributionProtocols(RunDataDistributionProtocolsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		// Get the batch Id and list of protocols to run on it
		//String batchId = exchange.getHeader(HeaderKeys.BatchIdsJson);
		String batchId = null;
		String batchIdJson = exchange.getHeader(HeaderKeys.BatchIdsJson);
		try {
			batchId = ObjectMapperPool.getInstance().readValue(batchIdJson, String.class);
		} catch (Exception ex) {
			throw new PipelineException("Error reading from JSON " + batchIdJson, ex);
		}

		List<LibraryItem> protocolsToRun = getProtocols(exchange);
		//LibraryItem[] protocolsToRun = getProtocols(exchange);
		List<TransformBatch> transformBatches = new ArrayList<>();

		// Run each protocol, creating a transformation batch for each
		// (Contains list of relevant resources and subscriber service contracts)
		for (LibraryItem protocol : protocolsToRun) {

			List<ServiceContract> subscribers = protocol
					.getProtocol()
					.getServiceContract()
					.stream()
					.filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
					.collect(Collectors.toList());

			//if there's no subscribers on this protocol, just skip it
			if (subscribers.isEmpty()) {
				continue;
			}

			TransformBatch transformBatch = new TransformBatch();
			transformBatch.setBatchId(UUID.fromString(batchId));
			transformBatch.setProtocolId(UUID.fromString(protocol.getUuid()));
			transformBatch.setSubscribers(subscribers);

			//TODO - apply protocol cohort filtering
			//TODO - apply protocol dataset filtering

			transformBatches.add(transformBatch);
		}

		// Add transformation batch list to the exchange
		try {
			String transformBatchesJson = ObjectMapperPool.getInstance().writeValueAsString(transformBatches);
			exchange.setHeader(HeaderKeys.TransformBatch, transformBatchesJson);
		} catch (JsonProcessingException e) {
			LOG.error("Error serializing transformation batches", e);
			throw new PipelineException("Error serializing transformation batches", e);
		}
		LOG.debug("Data distribution protocols executed");
	}

	private List<LibraryItem> getProtocols(Exchange exchange) throws PipelineException {

		List<LibraryItem> ret = new ArrayList<>();

		String[] protocolIds = null;
		String protocolIdJson = exchange.getHeader(HeaderKeys.Protocols);
		try {
			protocolIds = ObjectMapperPool.getInstance().readValue(protocolIdJson, String[].class);
		} catch (IOException e) {
			throw new PipelineException("Failed to read protocol IDs from json " + protocolIdJson, e);
		}

		for (String protocolId: protocolIds) {
			UUID protocolUuid = UUID.fromString(protocolId);
			try {
				LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItem(protocolUuid);
				ret.add(libraryItem);
			} catch (Exception e) {
				throw new PipelineException("Failed to read protocol item for " + protocolId, e);
			}
		}

		return ret;
	}
	/*private LibraryItem[] getProtocols(Exchange exchange) throws PipelineException {
		// Get the protocols
		String protocolJson = exchange.getHeader(HeaderKeys.Protocols);
		LibraryItem[] libraryItemList;
		try {
			libraryItemList = ObjectMapperPool.getInstance().readValue(protocolJson, LibraryItem[].class);
		} catch (IOException e) {
			throw new PipelineException(e.getMessage(), e);
		}
		return libraryItemList;
	}*/
}
