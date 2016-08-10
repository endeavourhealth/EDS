package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.configuration.MessageTransformConfig;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.transform.common.exceptions.SoftwareNotSupportedException;
import org.endeavourhealth.transform.common.exceptions.VersionNotSupportedException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageTransform extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransform.class);

	private MessageTransformConfig config;

	public MessageTransform(MessageTransformConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {

		try {

			UUID serviceId = UUID.fromString(exchange.getHeader(HeaderKeys.SenderUuid));
			String software = exchange.getHeader(HeaderKeys.SourceSystem);

			//find technical interface for software name

			String version = exchange.getHeader(HeaderKeys.SystemVersion);

			//find the system ID by using values from the message header
			ServiceRepository serviceRepository = new ServiceRepository();
			Service service = serviceRepository.getById(serviceId);
			List<JsonServiceInterfaceEndpoint> endpoints = new ObjectMapper().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});

			//TODO - need correct way of finding systemID from serviceID and exchange headers
			UUID systemId = endpoints.stream().map(JsonServiceInterfaceEndpoint::getSystemUuid).findFirst().get();

			//find the organisation UUIDs covered by the service
			Set<UUID> orgIds = service.getOrganisations().keySet();

			List<UUID> batchIds = null;

			if (software.equalsIgnoreCase("EmisExtractService")) {
				if (!version.equalsIgnoreCase("5.1")) {
					throw new VersionNotSupportedException(software, version);
				}
				batchIds = processEmisCsvTransform(exchange, serviceId, systemId, version, orgIds);

			} else if (software.equalsIgnoreCase("EmisOpen")) {
				//TODO - validate version for EmisOpen
				batchIds = processEmisOpenTransform(exchange, serviceId, systemId, version, orgIds);

			} else if (software.equalsIgnoreCase("OpenHR")) {
				//TODO - validate version for EmisOpen
				batchIds = processEmisOpenHrTransform(exchange, serviceId, systemId, version, orgIds);

			} else if (software.equalsIgnoreCase("TPPExtractService")) {
				//TODO - validate version for TPPExtractService
				batchIds = processTppXmlTransform(exchange, serviceId, systemId, version, orgIds);

			} else {
				throw new SoftwareNotSupportedException(software, version);
			}

			//update the Exchange with the batch IDs, for the next step in the pipeline
			String batchIdString = convertUUidsToStrings(batchIds);
			exchange.setHeader(HeaderKeys.BatchIds, batchIdString);

			LOG.trace("Message transformed");

		} catch (Exception e) {
			exchange.setException(e);
			LOG.error("Error", e);
		}
	}

	private static String convertUUidsToStrings(List<UUID> uuids) {
		List<String> batchIdStrings = uuids
				.stream()
				.map(t -> t.toString())
				.collect(Collectors.toList());
		return String.join(";", batchIdStrings);
	}

	private List<UUID> processEmisCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version, Set<UUID> orgIds) throws Exception {

		//for EMIS CSV, the exchange body will be a list of files received
		String decodedFileString = exchange.getBody();
		String[] decodedFiles = decodedFileString.split("\n");

		return EmisCsvTransformer.splitAndTransform(decodedFiles, exchange.getExchangeId(), serviceId, systemId, orgIds);
	}

	private List<UUID> processTppXmlTransform(Exchange exchange, UUID serviceId, UUID systemId, String version, Set<UUID> orgIds) throws Exception {
		//TODO - plug in TPP XML transform
		return null;
	}

	private List<UUID> processEmisOpenTransform(Exchange exchange, UUID serviceId, UUID systemId, String version, Set<UUID> orgIds) throws Exception {
		//TODO - plug in EMIS OPEN transform
		return null;
	}

	private List<UUID> processEmisOpenHrTransform(Exchange exchange, UUID serviceId, UUID systemId, String version, Set<UUID> orgIds) throws Exception {
		//TODO - plug in OpenHR transform
		return null;
	}
}
