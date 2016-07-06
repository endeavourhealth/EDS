package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.configuration.DetermineRelevantProtocolIdsConfig;
import org.endeavourhealth.core.data.admin.models.LibraryItem;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DetermineRelevantProtocolIds implements PipelineComponent {
	private static final String apiAddress = "http://localhost:8888/api";
	private static final Logger LOG = LoggerFactory.getLogger(DetermineRelevantProtocolIds.class);

	private DetermineRelevantProtocolIdsConfig config;

	public DetermineRelevantProtocolIds(DetermineRelevantProtocolIdsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws IOException {
		// Get service id
		String senderId = exchange.getHeader(HeaderKeys.Sender);

		// Determine relevant protocols
		getProtocolsForService(senderId, exchange);

		LOG.debug("Data distribution protocols Loaded");
	}

	private void getProtocolsForService(String senderId, Exchange exchange) throws IOException {
		Client client = ClientBuilder.newClient( new ClientConfig().register( LoggingFilter.class ) );
		WebTarget webTarget = client.target(apiAddress + "/library/getProtocols?serviceId=" + senderId);
		Invocation.Builder invocationBuilder =  webTarget.request();

		Response response = invocationBuilder.get();
		String protocolJson = response.readEntity(String.class);

		ObjectMapper mapper = new ObjectMapper();
		List<LibraryItem> libraryItemList = mapper.readValue(protocolJson, new TypeReference<List<LibraryItem>>(){});

		exchange.setHeader(HeaderKeys.ProtocolData, protocolJson);
		List<String> protocolIds = libraryItemList.stream().map(LibraryItem::getUuid).collect(Collectors.toList());
		exchange.setHeader(HeaderKeys.ProtocolIds, String.join(",", protocolIds));
	}
}
