package org.endeavourhealth.queuereader;

import com.datastax.driver.core.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.data.CassandraConnector;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.Exchange;
import org.endeavourhealth.core.data.audit.models.ExchangeByService;
import org.endeavourhealth.core.data.audit.models.ExchangeEvent;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.subscriber.EnterpriseFiler;
import org.endeavourhealth.transform.enterprise.EnterpriseFhirTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);


	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			LOG.error("Usage: queuereader config_id");
			return;
		}

		LOG.info("Initialising config manager");
		ConfigManager.Initialize("queuereader");

		if (args[0].equalsIgnoreCase("FixExchanges")) {
			fixMissingExchanges();
			return;
		}

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

	private static void fixMissingExchanges() {

		LOG.info("Fixing missing exchanges");

		Session session = CassandraConnector.getInstance().getSession();
		Statement stmt = new SimpleStatement("SELECT exchange_id, batch_id, inserted_at FROM ehr.exchange_batch LIMIT 600000;");
		stmt.setFetchSize(100);

		Set<UUID> exchangeIdsDone = new HashSet<>();

		AuditRepository auditRepository = new AuditRepository();

		ResultSet rs = session.execute(stmt);
		while (!rs.isExhausted()) {
			Row row = rs.one();

			UUID exchangeId = row.get(0, UUID.class);
			UUID batchId = row.get(1, UUID.class);
			Date date = row.getTimestamp(2);
			//LOG.info("Exchange " + exchangeId + " batch " + batchId + " date " + date);

			if (exchangeIdsDone.contains(exchangeId)) {
				continue;
			}

			if (auditRepository.getExchange(exchangeId) != null) {
				continue;
			}

			UUID serviceId = findServiceId(batchId, session);
			if (serviceId == null) {
				continue;
			}

			Exchange exchange = new Exchange();
			ExchangeByService exchangeByService = new ExchangeByService();
			ExchangeEvent exchangeEvent = new ExchangeEvent();

			Map<String, String> headers = new HashMap<>();
			headers.put(HeaderKeys.SenderServiceUuid, serviceId.toString());

			String headersJson = null;
			try {
				headersJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
			} catch (JsonProcessingException e) {
				//not throwing this exception further up, since it should never happen
				//and means we don't need to litter try/catches everywhere this is called from
				LOG.error("Failed to write exchange headers to Json", e);
				continue;
			}

			exchange.setBody("Body not available, as exchange re-created");
			exchange.setExchangeId(exchangeId);
			exchange.setHeaders(headersJson);
			exchange.setTimestamp(date);

			exchangeByService.setExchangeId(exchangeId);
			exchangeByService.setServiceId(serviceId);
			exchangeByService.setTimestamp(date);

			exchangeEvent.setEventDesc("Created_By_Conversion");
			exchangeEvent.setExchangeId(exchangeId);
			exchangeEvent.setTimestamp(new Date());

			auditRepository.save(exchange);
			auditRepository.save(exchangeEvent);
			auditRepository.save(exchangeByService);

			exchangeIdsDone.add(exchangeId);

			LOG.info("Creating exchange " + exchangeId);
		}

		LOG.info("Finished exchange fix");
	}

	private static UUID findServiceId(UUID batchId, Session session) {

		Statement stmt = new SimpleStatement("select resource_type, resource_id from ehr.resource_by_exchange_batch where batch_id = " + batchId + " LIMIT 1;");
		ResultSet rs = session.execute(stmt);
		if (rs.isExhausted()) {
			LOG.error("Failed to find resource_by_exchange_batch for batch_id " + batchId);
			return null;
		}

		Row row = rs.one();
		String resourceType = row.getString(0);
		UUID resourceId = row.get(1, UUID.class);

		stmt = new SimpleStatement("select service_id from ehr.resource_history where resource_type = '" + resourceType + "' and resource_id = " + resourceId + " LIMIT 1;");
		rs = session.execute(stmt);
		if (rs.isExhausted()) {
			LOG.error("Failed to find resource_history for resource_type " + resourceType + " and resource_id " + resourceId);
			return null;
		}

		row = rs.one();
		UUID serviceId = row.get(0, UUID.class);
		return serviceId;
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
