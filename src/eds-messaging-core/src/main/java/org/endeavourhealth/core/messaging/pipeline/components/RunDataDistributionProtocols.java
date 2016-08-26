package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.configuration.RunDataDistributionProtocolsConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class RunDataDistributionProtocols extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(RunDataDistributionProtocols.class);

	private RunDataDistributionProtocolsConfig config;

	public RunDataDistributionProtocols(RunDataDistributionProtocolsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		LibraryItem[] protocolsToRun = getProtocols(exchange);
		String batchIds = exchange.getHeader(HeaderKeys.BatchIds);

		// Run DDP
		// new ProtocolRunner().execute(fhirMessage, protocolToRun);

		LOG.debug("Data distribution protocols executed");
	}

	private LibraryItem[] getProtocols(Exchange exchange) throws PipelineException {
		// Get the protocols
		String protocolJson = exchange.getHeader(HeaderKeys.Protocols);
		ObjectMapper mapper = new ObjectMapper();
		LibraryItem[] libraryItemList;
		try {
			libraryItemList = mapper.readValue(protocolJson, LibraryItem[].class);
		} catch (IOException e) {
			throw new PipelineException(e.getMessage(), e);
		}
		return libraryItemList;
	}
}
