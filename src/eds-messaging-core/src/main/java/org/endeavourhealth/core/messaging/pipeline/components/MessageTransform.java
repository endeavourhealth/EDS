package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.configuration.MessageTransformConfig;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

			//work out the systemId from the endPoints registered against the serice
			UUID serviceId = UUID.fromString(exchange.getHeader(HeaderKeys.SenderUuid));
			ServiceRepository serviceRepository = new ServiceRepository();
			Service service = serviceRepository.getById(serviceId);
			List<JsonServiceInterfaceEndpoint> endpoints = new ObjectMapper().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
			UUID systemId = null;
			List<UUID> batchIds = null;

			//TODO - need proper mapping of Exchange headers to sender SystemId and transform Class
			//the below code is just a placeholder and should be replaced with a proper lookup from the exchange headers
			systemId = endpoints.stream().map(JsonServiceInterfaceEndpoint::getSystemUuid).findFirst().get();
			batchIds = processEmisCsvTransform(exchange, serviceId, systemId);

			//update the Exchange with the batch IDs, for the next step in the pipeline
			List<String> batchIdStrings = batchIds
											.stream()
											.map(t -> t.toString())
											.collect(Collectors.toList());
			String batchIdString = String.join(";", batchIdStrings);
			exchange.setHeader(HeaderKeys.BatchIds, batchIdString);

		} catch (Exception e) {
			exchange.setException(e);
			LOG.error("Error", e);
		}

		LOG.debug("Message transformed");
	}

	private List<UUID> processEmisCsvTransform(Exchange exchange, UUID serviceId, UUID systemId) throws Exception {

		//for EMIS CSV, the exchange body will be the path of the directory containing the CSV files
		String folderPath = exchange.getBody();

		CsvProcessor processor = new CsvProcessor(exchange.getExchangeId(), serviceId, systemId);
		EmisCsvTransformer.transform(folderPath, processor);
		return processor.getBatchIdsCreated();

	}

	private List<UUID> processTppXmlTransform(Exchange exchange, UUID serviceId, UUID systemId) throws Exception {
		return null;
	}

	private List<UUID> processEmisOpenTransform(Exchange exchange, UUID serviceId, UUID systemId) throws Exception {
		return null;
	}

	private List<UUID> processEmisOpenHrTransform(Exchange exchange, UUID serviceId, UUID systemId) throws Exception {
		return null;
	}
}
