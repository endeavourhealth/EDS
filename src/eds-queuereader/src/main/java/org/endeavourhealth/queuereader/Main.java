package org.endeavourhealth.queuereader;

import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.ExchangeByService;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.subscriber.EnterpriseFiler;
import org.endeavourhealth.transform.enterprise.EnterpriseFhirTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);


	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			LOG.error("Usage: queuereader config_id");
			return;
		}

		LOG.info("Initialising config manager");
		ConfigManager.Initialize("queuereader");

		//hack to get the Enterprise data streaming
		try {
			UUID serviceUuid = UUID.fromString(args[0]);
			startEnterpriseStream(serviceUuid);
		} catch (IllegalArgumentException iae) {
			//fine, just let it continue to below
		} catch (Exception ex) {
			LOG.error("", ex);
			return;
		}
		//LOG.info("Fixing events");
		//fixExchangeEvents();
		/*LOG.info("Fixing exchanges");
		fixExchanges();*/

		LOG.info("--------------------------------------------------");
		LOG.info("EDS Queue Reader " + args[0]);
		LOG.info("--------------------------------------------------");

		LOG.info("Fetching queuereader configuration");
		String configXml = ConfigManager.getConfiguration(args[0]);
		QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);

		// Instantiate rabbit handler
		LOG.info("Creating EDS queue reader");
		RabbitHandler rabbitHandler = new RabbitHandler(configuration);

		// Begin consume
		LOG.info("Starting message consumption");
		rabbitHandler.start();
		LOG.info("EDS Queue reader running");
	}

	private static void startEnterpriseStream(UUID serviceId) throws Exception {

		LOG.info("Starting Enterprise Streaming for " + serviceId);

		Service service = new ServiceRepository().getById(serviceId);
		List<UUID> orgIds = new ArrayList<>(service.getOrganisations().keySet());
		UUID orgId = orgIds.get(0);

		List<ExchangeByService> exchangeByServiceList = new AuditRepository().getExchangesByService(serviceId, Integer.MAX_VALUE);
		for (ExchangeByService exchangeByService: exchangeByServiceList) {
			UUID exchangeId = exchangeByService.getExchangeId();

			List<ExchangeBatch> exchangeBatches = new ExchangeBatchRepository().retrieveForExchangeId(exchangeId);
			LOG.info("Processing exchange " + exchangeId + " with " + exchangeBatches.size() + " batches");

			for (ExchangeBatch exchangeBatch : exchangeBatches) {

				UUID batchId = exchangeBatch.getBatchId();
				LOG.info("Processing exchange " + exchangeId + " and batch " + batchId);

				try {
					String outbound = EnterpriseFhirTransformer.transformFromFhir(serviceId, orgId, batchId, null);
					EnterpriseFiler.file(outbound);

				} catch (Exception ex) {
					throw new PipelineException("Failed to process exchange " + exchangeId + " and batch " + batchId, ex);
				}
			}
		}
	}

	/*private static void fixExchangeEvents() {

		List<ExchangeEvent> events = new AuditRepository().getAllExchangeEvents();
		for (ExchangeEvent event: events) {
			if (event.getEventDesc() != null) {
				continue;
			}

			String eventDesc = "";
			int eventType = event.getEvent().intValue();
			switch (eventType) {
				case 1:
					eventDesc = "Receive";
					break;
				case 2:
					eventDesc = "Validate";
					break;
				case 3:
					eventDesc = "Transform_Start";
					break;
				case 4:
					eventDesc = "Transform_End";
					break;
				case 5:
					eventDesc = "Send";
					break;
				default:
					eventDesc = "??? " + eventType;
			}

			event.setEventDesc(eventDesc);
			new AuditRepository().save(null, event);
		}

	}*/

	/*private static void fixExchanges() {

		AuditRepository auditRepository = new AuditRepository();

		Map<UUID, Set<UUID>> existingOnes = new HashMap();

		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();

		List<Exchange> exchanges = auditRepository.getAllExchanges();
		for (Exchange exchange: exchanges) {

			UUID exchangeUuid = exchange.getExchangeId();
			String headerJson = exchange.getHeaders();
			HashMap<String, String> headers = null;
			try {
				headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);
			} catch (Exception e) {
				LOG.error("Failed to read headers for exchange " + exchangeUuid + " and Json " + headerJson);
				continue;
			}

			*//*String serviceId = headers.get(HeaderKeys.SenderServiceUuid);
			if (serviceId == null) {
				LOG.warn("No service ID found for exchange " + exchange.getExchangeId());
				continue;
			}
			UUID serviceUuid = UUID.fromString(serviceId);

			Set<UUID> exchangeIdsDone = existingOnes.get(serviceUuid);
			if (exchangeIdsDone == null) {
				exchangeIdsDone = new HashSet<>();

				List<ExchangeByService> exchangeByServices = auditRepository.getExchangesByService(serviceUuid, Integer.MAX_VALUE);
				for (ExchangeByService exchangeByService: exchangeByServices) {
					exchangeIdsDone.add(exchangeByService.getExchangeId());
				}

				existingOnes.put(serviceUuid, exchangeIdsDone);
			}

			//create the exchange by service entity
			if (!exchangeIdsDone.contains(exchangeUuid)) {

				Date timestamp = exchange.getTimestamp();

				ExchangeByService newOne = new ExchangeByService();
				newOne.setExchangeId(exchangeUuid);
				newOne.setServiceId(serviceUuid);
				newOne.setTimestamp(timestamp);

				auditRepository.save(newOne);
			}*//*

			try {
				headers.remove(HeaderKeys.BatchIds);
				String newHeaderJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
				exchange.setHeaders(newHeaderJson);

				auditRepository.save(exchange);

			} catch (JsonProcessingException e) {
				LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
			}

			if (!headers.containsKey(HeaderKeys.BatchIds)) {

				//fix the batch IDs not being in the exchange
				List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeUuid);
				if (!batches.isEmpty()) {

					List<UUID> batchUuids = batches
							.stream()
							.map(t -> t.getBatchId())
							.collect(Collectors.toList());
					try {
						String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchUuids.toArray());
						headers.put(HeaderKeys.BatchIds, batchUuidsStr);
						String newHeaderJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
						exchange.setHeaders(newHeaderJson);

						auditRepository.save(exchange, null);

					} catch (JsonProcessingException e) {
						LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
					}
				}
			//}
		}
	}*/
}
