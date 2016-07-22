package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.configuration.RunDataDistributionProtocolsConfig;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
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
		LibraryItem protocolToRun = getProtocol(exchange);
		setProtocolData(exchange, protocolToRun);

		String fhirMessage = exchange.getBody();

		// Run DDP
		// new ProtocolRunner().execute(fhirMessage, protocolToRun);

		// Get output format list for multicast
		List<String> technicalInterfaceIds = getTechnicalInterfaceIds(protocolToRun);
		exchange.setHeader(HeaderKeys.TransformTo, String.join(",", technicalInterfaceIds));

		LOG.debug("Data distribution protocols executed");
	}

	private List<String> getTechnicalInterfaceIds(LibraryItem protocolToRun) {
		// Get distinct output formats
		return protocolToRun.getProtocol().getServiceContract().stream()
				.filter(serviceContract -> serviceContract.getType().equals(ServiceContractType.SUBSCRIBER))
				.map(ServiceContract::getTechnicalInterface)
				.map(TechnicalInterface::getUuid)
				.distinct()
				.collect(Collectors.toList());
	}

	private LibraryItem getProtocol(Exchange exchange) throws PipelineException {
		// Get the id
		String protocolId = exchange.getHeader(HeaderKeys.ProtocolIds);

		// Get the protocols
		String protocolJson = exchange.getHeader(HeaderKeys.ProtocolData);
		ObjectMapper mapper = new ObjectMapper();
		List<LibraryItem> libraryItemList;
		try {
			libraryItemList = mapper.readValue(protocolJson, new TypeReference<List<LibraryItem>>(){});
		} catch (IOException e) {
			throw new PipelineException(e.getMessage());
		}

		// Fetch by ID
		return libraryItemList.stream()
				.filter(libraryItem -> libraryItem.getUuid().equals(protocolId))
				.findFirst()
				.orElseThrow(() -> new PipelineException("Unknown protocol id"));
	}

	private void setProtocolData(Exchange exchange, LibraryItem protocol) throws PipelineException {
		try {
			String protocolData = new ObjectMapper().writeValueAsString(protocol);
			exchange.setHeader(HeaderKeys.ProtocolData, protocolData);
		} catch (JsonProcessingException e) {
			LOG.error("Unable to serialize protocol");
			throw new PipelineException(e.getMessage());
		}

	}
}
