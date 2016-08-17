package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.configuration.PostToSubscriberWebServiceConfig;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
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

public class PostToSubscriberWebService extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToSubscriberWebService.class);

	private PostToSubscriberWebServiceConfig config;

	public PostToSubscriberWebService(PostToSubscriberWebServiceConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		String technicalInterfaceId = exchange.getHeader(HeaderKeys.TransformTo);
		String protocolData = exchange.getHeader(HeaderKeys.ProtocolData);

		LibraryItem protocol;
		try {
			protocol = new ObjectMapper().readValue(protocolData, LibraryItem.class);
		} catch (IOException e) {
			LOG.error("Unable to deserialize protocol");
			throw new PipelineException(e.getMessage(), e);
		}

		// Find subscriber service contracts that require this technical interface
		List<ServiceContract> subscriberContracts = protocol.getProtocol().getServiceContract().stream()
				.filter(sc -> ServiceContractType.SUBSCRIBER.equals(sc.getType())
						&& technicalInterfaceId.equals(sc.getTechnicalInterface().getUuid()))
				.collect(Collectors.toList());

		// Find the relevant endpoints for those subscribers/technical interface
		List<String> endpoints = new ArrayList<>();
		try {
			ServiceRepository serviceRepository = new ServiceRepository();
			for (ServiceContract contract : subscriberContracts) {
				Service service = serviceRepository.getById(UUID.fromString(contract.getService().getUuid()));
				List<JsonServiceInterfaceEndpoint> serviceEndpoints = new ObjectMapper().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
				String endpoint = serviceEndpoints.stream()
						.filter(ep -> ep.getTechnicalInterfaceUuid().toString().equals(contract.getTechnicalInterface().getUuid()))
						.map(JsonServiceInterfaceEndpoint::getEndpoint)
						.findFirst()
						.get();
				endpoints.add(endpoint);
			}
		} catch (IOException e) {
			throw new PipelineException(e.getMessage(), e);
		}

		// Determine which protocol subscribers want this transform format
		exchange.setHeader(HeaderKeys.Subscribers, String.join(",", endpoints));

	}
}
