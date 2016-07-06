package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.configuration.RunDataDistributionProtocolsConfig;
import org.endeavourhealth.core.data.admin.models.LibraryItem;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class RunDataDistributionProtocols implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(RunDataDistributionProtocols.class);

	private RunDataDistributionProtocolsConfig config;

	public RunDataDistributionProtocols(RunDataDistributionProtocolsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws IOException {
		// Get the id
		String protocolId = exchange.getHeader(HeaderKeys.ProtocolIds);

		// Get the protocols
		String protocolJson = exchange.getHeader(HeaderKeys.ProtocolData);
		ObjectMapper mapper = new ObjectMapper();
		List<LibraryItem> libraryItemList = mapper.readValue(protocolJson, new TypeReference<List<LibraryItem>>(){});

		// Fetch by ID


		// Run DDPs

		// Get distinct output formats
		exchange.setHeader(HeaderKeys.TransformTo, "A,B,C");

		LOG.debug("Data distribution protocols executed");
	}
}
