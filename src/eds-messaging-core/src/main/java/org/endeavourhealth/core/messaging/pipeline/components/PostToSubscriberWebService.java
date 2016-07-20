package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.configuration.PostToSubscriberWebServiceConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PostToSubscriberWebService extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToSubscriberWebService.class);

	private PostToSubscriberWebServiceConfig config;

	public PostToSubscriberWebService(PostToSubscriberWebServiceConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		String messageFormat = exchange.getHeader(HeaderKeys.TransformTo);
		String messageVersion = exchange.getHeader(HeaderKeys.SystemVersion);
		String protocolData = exchange.getHeader(HeaderKeys.ProtocolData);
		LibraryItem protocol;
		try {
			protocol = new ObjectMapper().readValue(protocolData, LibraryItem.class);
		} catch (IOException e) {
			LOG.error("Unable to deserialize protocol");
			throw new PipelineException(e.getMessage());
		}

		// Find subscribers to this protocol that need this format/version
		List<String> subscribers = protocol.getProtocol().getServiceContract().stream()
				.filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER)
						&& sc.getTechnicalInterface().getMessageFormat().equals(messageFormat)
						&& sc.getTechnicalInterface().getMessageFormatVersion().equals(messageVersion))
				.map(sc -> sc.getService().getUuid())
				.collect(Collectors.toList());

		// Get relevant endpoint addresses for subscribers given technical interface Id

		// Determine which protocol subscribers want this transform format
		exchange.setHeader(HeaderKeys.Subscribers, String.join(",", subscribers));

	}
}
