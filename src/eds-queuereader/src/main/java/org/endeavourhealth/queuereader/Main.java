package org.endeavourhealth.queuereader;

import com.datastax.driver.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.cassandra.CassandraConnector;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.configuration.ConfigDeserialiser;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.Exchange;
import org.endeavourhealth.core.data.audit.models.ExchangeByService;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static final ParserPool PARSER_POOL = new ParserPool();

	public static void main(String[] args) throws Exception {

		LOG.info("Initialising config manager");
		ConfigManager.Initialize("queuereader");

		if (args.length >= 0
				&& args[0].equalsIgnoreCase("FindCodes")) {
			findCodes();
		}

		if (args.length >= 0
				&& args[0].equalsIgnoreCase("FindDeletedOrgs")) {
			findDeletedOrgs();
		}

		if (args.length != 1) {
			LOG.error("Usage: queuereader config_id");
			return;
		}

		String configId = args[0];

		LOG.info("--------------------------------------------------");
		LOG.info("EDS Queue Reader " + configId);
		LOG.info("--------------------------------------------------");

		LOG.info("Fetching queuereader configuration");
		String configXml = ConfigManager.getConfiguration(configId);
		QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);

		// Instantiate rabbit handler
		LOG.info("Creating EDS queue reader");
		RabbitHandler rabbitHandler = new RabbitHandler(configuration);

		// Begin consume
		LOG.info("Starting message consumption");
		rabbitHandler.start();
		LOG.info("EDS Queue reader running");
	}

	private static void findDeletedOrgs() {
		LOG.info("Starting finding deleted orgs");

		ServiceRepository serviceRepository = new ServiceRepository();
		AuditRepository auditRepository = new AuditRepository();

		List<Service> services = new ArrayList<>();
		for (Service service: serviceRepository.getAll()) {
			services.add(service);
		}

		services.sort((o1, o2) -> {
			String name1 = o1.getName();
			String name2 = o2.getName();
			return name1.compareToIgnoreCase(name2);
		});

		for (Service service: services) {

			UUID serviceUuid = service.getId();
			List<ExchangeByService> exchangeByServices = auditRepository.getExchangesByService(serviceUuid, 1, new Date(0), new Date());

			LOG.info("Service: " + service.getName() + " " + service.getLocalIdentifier());

			if (exchangeByServices.isEmpty()) {
				LOG.info("    no exchange found!");
				continue;
			}

			try {
				ExchangeByService exchangeByService = exchangeByServices.get(0);
				UUID exchangeId = exchangeByService.getExchangeId();
				Exchange exchange = auditRepository.getExchange(exchangeId);

				String headerJson = exchange.getHeaders();
				HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);

				String systemUuidStr = headers.get(HeaderKeys.SenderSystemUuid);
				UUID systemUuid = UUID.fromString(systemUuidStr);

				int batches = countBatches(exchangeId, serviceUuid, systemUuid);
				LOG.info("    Most recent exchange had " + batches + " batches");

				if (batches > 1 && batches < 2000) {
					continue;
				}

				//go back until we find the FIRST exchange where it broke
				exchangeByServices = auditRepository.getExchangesByService(serviceUuid, 250, new Date(0), new Date());
				for (int i=0; i<exchangeByServices.size(); i++) {
					exchangeByService = exchangeByServices.get(i);
					exchangeId = exchangeByService.getExchangeId();
					batches = countBatches(exchangeId, serviceUuid, systemUuid);

					exchange = auditRepository.getExchange(exchangeId);
					Date timestamp = exchange.getTimestamp();

					if (batches < 1 || batches > 2000) {
						LOG.info("    " + timestamp + " had " + batches);
					}

					if (batches > 1 && batches < 2000) {
						LOG.info("    " + timestamp + " had " + batches);
						break;
					}
				}


			} catch (Exception ex) {
				LOG.error("", ex);
			}

		}

		LOG.info("Finished finding deleted orgs");
	}

	private static int countBatches(UUID exchangeId, UUID serviceId, UUID systemId) {
		int batches = 0;
		List<ExchangeTransformAudit> audits = new AuditRepository().getAllExchangeTransformAudits(serviceId, systemId, exchangeId);
		for (ExchangeTransformAudit audit: audits) {
			batches += audit.getNumberBatchesCreated();
		}
		return batches;
	}

	/*private static void fixExchanges(UUID justThisService) {
		LOG.info("Fixing exchanges");

		try {
			Iterable<Service> iterable = new ServiceRepository().getAll();
			for (Service service : iterable) {
				UUID serviceId = service.getId();

				if (justThisService != null
						&& !service.getId().equals(justThisService)) {
					LOG.info("Skipping service " + service.getName());
					continue;
				}

				LOG.info("Doing service " + service.getName());

				List<UUID> exchangeIds = new AuditRepository().getExchangeIdsForService(serviceId);
				for (UUID exchangeId : exchangeIds) {

					Exchange exchange = AuditWriter.readExchange(exchangeId);

					String software = exchange.getHeader(HeaderKeys.SourceSystem);
					if (!software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {
						continue;
					}

					boolean changed = false;

					String body = exchange.getBody();

					String[] files = body.split("\n");
					if (files.length == 0) {
						continue;
					}

					for (int i=0; i<files.length; i++) {
						String original = files[i];

						//remove /r characters
						String trimmed = original.trim();

						//add the new prefix
						if (!trimmed.startsWith("sftpreader/EMIS001/")) {
							trimmed = "sftpreader/EMIS001/" + trimmed;
						}

						if (!original.equals(trimmed)) {
							files[i] = trimmed;
							changed = true;
						}
					}

					if (changed) {

						LOG.info("Fixed exchange " + exchangeId);
						LOG.info(body);

						body = String.join("\n", files);
						exchange.setBody(body);

						AuditWriter.writeExchange(exchange);
					}
				}
			}

			LOG.info("Fixed exchanges");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/


	/*private static void deleteDataForService(UUID serviceId) {

		Service dbService = new ServiceRepository().getById(serviceId);

		//the delete will take some time, so do the delete in a separate thread
		LOG.info("Deleting all data for service " + dbService.getName() + " " + dbService.getId());
		FhirDeletionService deletor = new FhirDeletionService(dbService);

		try {
			deletor.deleteData();
			LOG.info("Completed deleting all data for service " + dbService.getName() + " " + dbService.getId());
		} catch (Exception ex) {
			LOG.error("Error deleting service " + dbService.getName() + " " + dbService.getId(), ex);
		}
	}*/

	/*private static void fixProblems(UUID serviceId, String sharedStoragePath, boolean testMode) {
		LOG.info("Fixing problems for service " + serviceId);

		AuditRepository auditRepository = new AuditRepository();
		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
		ResourceRepository resourceRepository = new ResourceRepository();

		List<ExchangeByService> exchangeByServiceList = auditRepository.getExchangesByService(serviceId, Integer.MAX_VALUE);

		//go backwards as the most recent is first
		for (int i=exchangeByServiceList.size()-1; i>=0; i--) {
			ExchangeByService exchangeByService = exchangeByServiceList.get(i);
			UUID exchangeId = exchangeByService.getExchangeId();
			LOG.info("Doing exchange " + exchangeId);

			EmisCsvHelper helper = null;

			try {
				Exchange exchange = AuditWriter.readExchange(exchangeId);
				String exchangeBody = exchange.getBody();
				String[] files = exchangeBody.split(java.lang.System.lineSeparator());

				File orgDirectory = validateAndFindCommonDirectory(sharedStoragePath, files);
				Map<Class, AbstractCsvParser> allParsers = new HashMap<>();
				String properVersion = null;

				String[] versions = new String[]{EmisCsvToFhirTransformer.VERSION_5_0, EmisCsvToFhirTransformer.VERSION_5_1, EmisCsvToFhirTransformer.VERSION_5_3, EmisCsvToFhirTransformer.VERSION_5_4};
				for (String version: versions) {

					try {

						List<AbstractCsvParser> parsers = new ArrayList<>();

						EmisCsvToFhirTransformer.findFileAndOpenParser(Observation.class, orgDirectory, version, true, parsers);
						EmisCsvToFhirTransformer.findFileAndOpenParser(DrugRecord.class, orgDirectory, version, true, parsers);
						EmisCsvToFhirTransformer.findFileAndOpenParser(IssueRecord.class, orgDirectory, version, true, parsers);

						for (AbstractCsvParser parser: parsers) {
							Class cls = parser.getClass();
							allParsers.put(cls, parser);
						}

						properVersion = version;

					} catch (Exception ex) {
						//ignore
					}
				}

				if (allParsers.isEmpty()) {
					throw new Exception("Failed to open parsers for exchange " + exchangeId + " in folder " + orgDirectory);
				}

				UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);
				//FhirResourceFiler dummyFiler = new FhirResourceFiler(exchangeId, serviceId, systemId, null, null, 10);

				if (helper == null) {
					helper = new EmisCsvHelper(findDataSharingAgreementGuid(new ArrayList<>(allParsers.values())));
				}

				ObservationPreTransformer.transform(properVersion, allParsers, null, helper);
				IssueRecordPreTransformer.transform(properVersion, allParsers, null, helper);
				DrugRecordPreTransformer.transform(properVersion, allParsers, null, helper);

				Map<String, List<String>> problemChildren = helper.getProblemChildMap();

				List<ExchangeBatch> exchangeBatches = exchangeBatchRepository.retrieveForExchangeId(exchangeId);

				for (Map.Entry<String, List<String>> entry : problemChildren.entrySet()) {
					String patientLocallyUniqueId = entry.getKey().split(":")[0];

					UUID edsPatientId = IdHelper.getEdsResourceId(serviceId, systemId, ResourceType.Patient, patientLocallyUniqueId);
					if (edsPatientId == null) {
						throw new Exception("Failed to find edsPatientId for local Patient ID " + patientLocallyUniqueId + " in exchange " + exchangeId);
					}

					//find the batch ID for our patient
					UUID batchId = null;
					for (ExchangeBatch exchangeBatch: exchangeBatches) {
						if (exchangeBatch.getEdsPatientId() != null
								&& exchangeBatch.getEdsPatientId().equals(edsPatientId)) {
							batchId = exchangeBatch.getBatchId();
							break;
						}
					}
					if (batchId == null) {
						throw new Exception("Failed to find batch ID for eds Patient ID " + edsPatientId + " in exchange " + exchangeId);
					}

					//find the EDS ID for our problem
					UUID edsProblemId = IdHelper.getEdsResourceId(serviceId, systemId, ResourceType.Condition, entry.getKey());
					if (edsProblemId == null) {
						LOG.warn("No edsProblemId found for local ID " + entry.getKey() + " - assume bad data referring to non-existing problem?");
						//throw new Exception("Failed to find edsProblemId for local Patient ID " + problemLocallyUniqueId + " in exchange " + exchangeId);
					}

					//convert our child IDs to EDS references
					List<Reference> references = new ArrayList<>();

					HashSet<String> contentsSet = new HashSet<>();
					contentsSet.addAll(entry.getValue());

					for (String referenceValue : contentsSet) {
						Reference reference = ReferenceHelper.createReference(referenceValue);
						ReferenceComponents components = ReferenceHelper.getReferenceComponents(reference);
						String locallyUniqueId = components.getId();
						ResourceType resourceType = components.getResourceType();
						UUID edsResourceId = IdHelper.getEdsResourceId(serviceId, systemId, resourceType, locallyUniqueId);

						Reference globallyUniqueReference = ReferenceHelper.createReference(resourceType, edsResourceId.toString());
						references.add(globallyUniqueReference);
					}

					//find the resource for the problem itself
					ResourceByExchangeBatch problemResourceByExchangeBatch = null;
					List<ResourceByExchangeBatch> resources = resourceRepository.getResourcesForBatch(batchId, ResourceType.Condition.toString());
					for (ResourceByExchangeBatch resourceByExchangeBatch: resources) {
						if (resourceByExchangeBatch.getResourceId().equals(edsProblemId)) {
							problemResourceByExchangeBatch = resourceByExchangeBatch;
							break;
						}
					}
					if (problemResourceByExchangeBatch == null) {
						throw new Exception("Problem not found for edsProblemId " + edsProblemId + " for exchange " + exchangeId);
					}

					if (problemResourceByExchangeBatch.getIsDeleted()) {
						LOG.warn("Problem " + edsProblemId + " is deleted, so not adding to it for exchange " + exchangeId);
						continue;
					}

					String json = problemResourceByExchangeBatch.getResourceData();
					Condition fhirProblem = (Condition)PARSER_POOL.parse(json);

					//update the problems
					if (fhirProblem.hasContained()) {
						if (fhirProblem.getContained().size() > 1) {
							throw new Exception("Problem " + edsProblemId + " is has " + fhirProblem.getContained().size() + " contained resources for exchange " + exchangeId);
						}
						fhirProblem.getContained().clear();
					}

					List_ list = new List_();
					list.setId("Items");
					fhirProblem.getContained().add(list);

					Extension extension = ExtensionConverter.findExtension(fhirProblem, FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE);
					if (extension == null) {
						Reference listReference = ReferenceHelper.createInternalReference("Items");
						fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE, listReference));
					}

					for (Reference reference : references) {
						list.addEntry().setItem(reference);
					}

					String newJson = FhirSerializationHelper.serializeResource(fhirProblem);
					if (newJson.equals(json)) {
						LOG.warn("Skipping edsProblemId " + edsProblemId + " as JSON hasn't changed");
						continue;
					}

					problemResourceByExchangeBatch.setResourceData(newJson);

					String resourceType = problemResourceByExchangeBatch.getResourceType();
					UUID versionUuid = problemResourceByExchangeBatch.getVersion();

					ResourceHistory problemResourceHistory = resourceRepository.getResourceHistoryByKey(edsProblemId, resourceType, versionUuid);
					problemResourceHistory.setResourceData(newJson);
					problemResourceHistory.setResourceChecksum(FhirStorageService.generateChecksum(newJson));

					ResourceByService problemResourceByService = resourceRepository.getResourceByServiceByKey(serviceId, systemId, resourceType, edsProblemId);
					if (problemResourceByService.getResourceData() == null) {
						problemResourceByService = null;
						LOG.warn("Not updating edsProblemId " + edsProblemId + " for exchange " + exchangeId + " as it's been subsequently delrted");
					} else {
						problemResourceByService.setResourceData(newJson);
					}

					//save back to THREE tables
					if (!testMode) {

						resourceRepository.save(problemResourceByExchangeBatch);
						resourceRepository.save(problemResourceHistory);
						if (problemResourceByService != null) {
							resourceRepository.save(problemResourceByService);
						}
						LOG.info("Fixed edsProblemId " + edsProblemId + " for exchange Id " + exchangeId);

					} else {
						LOG.info("Would change edsProblemId " + edsProblemId + " to new JSON");
						LOG.info(newJson);
					}
				}

			} catch (Exception ex) {
				LOG.error("Failed on exchange " + exchangeId, ex);
				break;
			}
		}

		LOG.info("Finished fixing problems for service " + serviceId);
	}

	private static String findDataSharingAgreementGuid(List<AbstractCsvParser> parsers) throws Exception {

		//we need a file name to work out the data sharing agreement ID, so just the first file we can find
		File f = parsers
				.iterator()
				.next()
				.getFile();

		String name = Files.getNameWithoutExtension(f.getName());
		String[] toks = name.split("_");
		if (toks.length != 5) {
			throw new TransformException("Failed to extract data sharing agreement GUID from filename " + f.getName());
		}
		return toks[4];
	}



	private static void closeParsers(Collection<AbstractCsvParser> parsers) {
		for (AbstractCsvParser parser : parsers) {
			try {
				parser.close();
			} catch (IOException ex) {
				//don't worry if this fails, as we're done anyway
			}
		}
	}


	private static File validateAndFindCommonDirectory(String sharedStoragePath, String[] files) throws Exception {
		String organisationDir = null;

		for (String file: files) {
			File f = new File(sharedStoragePath, file);
			if (!f.exists()) {
				LOG.error("Failed to find file {} in shared storage {}", file, sharedStoragePath);
				throw new FileNotFoundException("" + f + " doesn't exist");
			}
			//LOG.info("Successfully found file {} in shared storage {}", file, sharedStoragePath);

			try {
				File orgDir = f.getParentFile();

				if (organisationDir == null) {
					organisationDir = orgDir.getAbsolutePath();
				} else {
					if (!organisationDir.equalsIgnoreCase(orgDir.getAbsolutePath())) {
						throw new Exception();
					}
				}

			} catch (Exception ex) {
				throw new FileNotFoundException("" + f + " isn't in the expected directory structure within " + organisationDir);
			}

		}
		return new File(organisationDir);
	}*/

	/*private static void testLogging() {

		while (true) {
			System.out.println("Checking logging at " + System.currentTimeMillis());
			try {
				Thread.sleep(4000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			LOG.trace("trace logging");
			LOG.debug("debug logging");
			LOG.info("info logging");
			LOG.warn("warn logging");
			LOG.error("error logging");
		}

	}
*/
	/*private static void fixExchangeProtocols() {
		LOG.info("Fixing exchange protocols");

		AuditRepository auditRepository = new AuditRepository();

		Session session = CassandraConnector.getInstance().getSession();
		Statement stmt = new SimpleStatement("SELECT exchange_id FROM audit.Exchange LIMIT 1000;");
		stmt.setFetchSize(100);

		ResultSet rs = session.execute(stmt);
		while (!rs.isExhausted()) {
			Row row = rs.one();
			UUID exchangeId = row.get(0, UUID.class);

			LOG.info("Processing exchange " + exchangeId);
			Exchange exchange = auditRepository.getExchange(exchangeId);

			String headerJson = exchange.getHeaders();
			HashMap<String, String> headers = null;
			try {
				headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);
			} catch (Exception ex) {
				LOG.error("Failed to parse headers for exchange " + exchange.getExchangeId(), ex);
				continue;
			}

			String serviceIdStr = headers.get(HeaderKeys.SenderServiceUuid);
			if (Strings.isNullOrEmpty(serviceIdStr)) {
				LOG.error("Failed to find service ID for exchange " + exchange.getExchangeId());
				continue;
			}

			UUID serviceId = UUID.fromString(serviceIdStr);
			List<String> newIds = new ArrayList<>();
			String protocolJson = headers.get(HeaderKeys.Protocols);

			if (!headers.containsKey(HeaderKeys.Protocols)) {

				try {
					List<LibraryItem> libraryItemList = LibraryRepositoryHelper.getProtocolsByServiceId(serviceIdStr);

					// Get protocols where service is publisher
					newIds = libraryItemList.stream()
							.filter(
									libraryItem -> libraryItem.getProtocol().getServiceContract().stream()
											.anyMatch(sc ->
													sc.getType().equals(ServiceContractType.PUBLISHER)
															&& sc.getService().getUuid().equals(serviceIdStr)))
							.map(t -> t.getUuid().toString())
							.collect(Collectors.toList());
				} catch (Exception e) {
					LOG.error("Failed to find protocols for exchange " + exchange.getExchangeId(), e);
					continue;
				}

			} else {

				try {
					JsonNode node = ObjectMapperPool.getInstance().readTree(protocolJson);

					for (int i = 0; i < node.size(); i++) {
						JsonNode libraryItemNode = node.get(i);
						JsonNode idNode = libraryItemNode.get("uuid");
						String id = idNode.asText();
						newIds.add(id);
					}
				} catch (Exception e) {
					LOG.error("Failed to read Json from " + protocolJson + " for exchange " + exchange.getExchangeId(), e);
					continue;
				}
			}

			try {
				if (newIds.isEmpty()) {
					headers.remove(HeaderKeys.Protocols);

				} else {
					String protocolsJson = ObjectMapperPool.getInstance().writeValueAsString(newIds.toArray());
					headers.put(HeaderKeys.Protocols, protocolsJson);
				}

			} catch (JsonProcessingException e) {
				LOG.error("Unable to serialize protocols to JSON for exchange " + exchange.getExchangeId(), e);
				continue;
			}

			try {
				headerJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
				exchange.setHeaders(headerJson);
			} catch (JsonProcessingException e) {
				LOG.error("Failed to write exchange headers to Json for exchange " + exchange.getExchangeId(), e);
				continue;
			}

			auditRepository.save(exchange);
		}

		LOG.info("Finished fixing exchange protocols");
	}*/

	/*private static void fixExchangeHeaders() {
		LOG.info("Fixing exchange headers");

		AuditRepository auditRepository = new AuditRepository();
		ServiceRepository serviceRepository = new ServiceRepository();
		OrganisationRepository organisationRepository = new OrganisationRepository();

		List<Exchange> exchanges = new AuditRepository().getAllExchanges();
		for (Exchange exchange: exchanges) {

			String headerJson = exchange.getHeaders();
			HashMap<String, String> headers = null;
			try {
				headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);
			} catch (Exception ex) {
				LOG.error("Failed to parse headers for exchange " + exchange.getExchangeId(), ex);
				continue;
			}

			if (headers.containsKey(HeaderKeys.SenderLocalIdentifier)
					&& headers.containsKey(HeaderKeys.SenderOrganisationUuid)) {
				continue;
			}

			String serviceIdStr = headers.get(HeaderKeys.SenderServiceUuid);
			if (Strings.isNullOrEmpty(serviceIdStr)) {
				LOG.error("Failed to find service ID for exchange " + exchange.getExchangeId());
				continue;
			}

			UUID serviceId = UUID.fromString(serviceIdStr);
			Service service = serviceRepository.getById(serviceId);
			Map<UUID, String> orgMap = service.getOrganisations();
			if (orgMap.size() != 1) {
				LOG.error("Wrong number of orgs in service " + serviceId + " for exchange " + exchange.getExchangeId());
				continue;
			}

			UUID orgId = orgMap
					.keySet()
					.stream()
					.collect(StreamExtension.firstOrNullCollector());
			Organisation organisation = organisationRepository.getById(orgId);
			String odsCode = organisation.getNationalId();

			headers.put(HeaderKeys.SenderLocalIdentifier, odsCode);
			headers.put(HeaderKeys.SenderOrganisationUuid, orgId.toString());

			try {
				headerJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
			} catch (JsonProcessingException e) {
				//not throwing this exception further up, since it should never happen
				//and means we don't need to litter try/catches everywhere this is called from
				LOG.error("Failed to write exchange headers to Json", e);
				continue;
			}

			exchange.setHeaders(headerJson);

			auditRepository.save(exchange);

			LOG.info("Creating exchange " + exchange.getExchangeId());
		}

		LOG.info("Finished fixing exchange headers");
	}*/

	/*private static void fixExchangeHeaders() {
		LOG.info("Fixing exchange headers");

		AuditRepository auditRepository = new AuditRepository();
		ServiceRepository serviceRepository = new ServiceRepository();
		OrganisationRepository organisationRepository = new OrganisationRepository();
		LibraryRepository libraryRepository = new LibraryRepository();

		List<Exchange> exchanges = new AuditRepository().getAllExchanges();
		for (Exchange exchange: exchanges) {

			String headerJson = exchange.getHeaders();
			HashMap<String, String> headers = null;
			try {
				headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);
			} catch (Exception ex) {
				LOG.error("Failed to parse headers for exchange " + exchange.getExchangeId(), ex);
				continue;
			}

			String serviceIdStr = headers.get(HeaderKeys.SenderServiceUuid);
			if (Strings.isNullOrEmpty(serviceIdStr)) {
				LOG.error("Failed to find service ID for exchange " + exchange.getExchangeId());
				continue;
			}

			boolean changed = false;

			UUID serviceId = UUID.fromString(serviceIdStr);
			Service service = serviceRepository.getById(serviceId);
			try {
				List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});

				for (JsonServiceInterfaceEndpoint endpoint : endpoints) {

					UUID endpointSystemId = endpoint.getSystemUuid();
					String endpointInterfaceId = endpoint.getTechnicalInterfaceUuid().toString();

					ActiveItem activeItem = libraryRepository.getActiveItemByItemId(endpointSystemId);
					Item item = libraryRepository.getItemByKey(endpointSystemId, activeItem.getAuditId());
					LibraryItem libraryItem = QueryDocumentSerializer.readLibraryItemFromXml(item.getXmlContent());
					System system = libraryItem.getSystem();
					for (TechnicalInterface technicalInterface : system.getTechnicalInterface()) {

						if (endpointInterfaceId.equals(technicalInterface.getUuid())) {

							if (!headers.containsKey(HeaderKeys.SourceSystem)) {
								headers.put(HeaderKeys.SourceSystem, technicalInterface.getMessageFormat());
								changed = true;
							}
							if (!headers.containsKey(HeaderKeys.SystemVersion)) {
								headers.put(HeaderKeys.SystemVersion, technicalInterface.getMessageFormatVersion());
								changed = true;
							}
							if (!headers.containsKey(HeaderKeys.SenderSystemUuid)) {
								headers.put(HeaderKeys.SenderSystemUuid, endpointSystemId.toString());
								changed = true;
							}
						}
					}

				}
			} catch (Exception e) {
				LOG.error("Failed to find endpoint details for " + exchange.getExchangeId());
				continue;
			}

			if (changed) {
				try {
					headerJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
				} catch (JsonProcessingException e) {
					//not throwing this exception further up, since it should never happen
					//and means we don't need to litter try/catches everywhere this is called from
					LOG.error("Failed to write exchange headers to Json", e);
					continue;
				}

				exchange.setHeaders(headerJson);
				auditRepository.save(exchange);

				LOG.info("Fixed exchange " + exchange.getExchangeId());
			}
		}

		LOG.info("Finished fixing exchange headers");
	}*/

	/*private static void testConnection(String configName) {
		try {

			JsonNode config = ConfigManager.getConfigurationAsJson(configName, "enterprise");
			String driverClass = config.get("driverClass").asText();
			String url = config.get("url").asText();
			String username = config.get("username").asText();
			String password = config.get("password").asText();

			//force the driver to be loaded
			Class.forName(driverClass);

			Connection conn = DriverManager.getConnection(url, username, password);
			conn.setAutoCommit(false);
			LOG.info("Connection ok");

			conn.close();
		} catch (Exception e) {
			LOG.error("", e);
		}
	}*/
	/*private static void testConnection() {
		try {

			JsonNode config = ConfigManager.getConfigurationAsJson("postgres", "enterprise");
			String url = config.get("url").asText();
			String username = config.get("username").asText();
			String password = config.get("password").asText();

			//force the driver to be loaded
			Class.forName("org.postgresql.Driver");

			Connection conn = DriverManager.getConnection(url, username, password);
			conn.setAutoCommit(false);
			LOG.info("Connection ok");

			conn.close();
		} catch (Exception e) {
			LOG.error("", e);
		}
	}*/


	/*private static void startEnterpriseStream(UUID serviceId, String configName, UUID exchangeIdStartFrom, UUID batchIdStartFrom) throws Exception {

		LOG.info("Starting Enterprise Streaming for " + serviceId + " using " + configName + " starting from exchange " + exchangeIdStartFrom + " and batch " + batchIdStartFrom);

		LOG.info("Testing database connection");
		testConnection(configName);

		Service service = new ServiceRepository().getById(serviceId);
		List<UUID> orgIds = new ArrayList<>(service.getOrganisations().keySet());
		UUID orgId = orgIds.get(0);

		List<ExchangeByService> exchangeByServiceList = new AuditRepository().getExchangesByService(serviceId, Integer.MAX_VALUE);

		for (int i=exchangeByServiceList.size()-1; i>=0; i--) {
			ExchangeByService exchangeByService = exchangeByServiceList.get(i);
		//for (ExchangeByService exchangeByService: exchangeByServiceList) {
			UUID exchangeId = exchangeByService.getExchangeId();

			if (exchangeIdStartFrom != null) {
				if (!exchangeIdStartFrom.equals(exchangeId)) {
					continue;
				} else {
					//once we have a match, set to null so we don't skip any subsequent ones
					exchangeIdStartFrom = null;
				}
			}

			Exchange exchange = AuditWriter.readExchange(exchangeId);
			String senderOrgUuidStr = exchange.getHeader(HeaderKeys.SenderOrganisationUuid);
			UUID senderOrgUuid = UUID.fromString(senderOrgUuidStr);

			//this one had 90,000 batches and doesn't need doing again
			*//*if (exchangeId.equals(UUID.fromString("b9b93be0-afd8-11e6-8c16-c1d5a00342f3"))) {
				LOG.info("Skipping exchange " + exchangeId);
				continue;
			}*//*

			List<ExchangeBatch> exchangeBatches = new ExchangeBatchRepository().retrieveForExchangeId(exchangeId);
			LOG.info("Processing exchange " + exchangeId + " with " + exchangeBatches.size() + " batches");

			for (int j=0; j<exchangeBatches.size(); j++) {
				ExchangeBatch exchangeBatch = exchangeBatches.get(j);
				UUID batchId = exchangeBatch.getBatchId();

				if (batchIdStartFrom != null) {
					if (!batchIdStartFrom.equals(batchId)) {
						continue;
					} else {
						batchIdStartFrom = null;
					}
				}

				LOG.info("Processing exchange " + exchangeId + " and batch " + batchId + " " + (j+1) + "/" + exchangeBatches.size());

				try {
					String outbound = FhirToEnterpriseCsvTransformer.transformFromFhir(senderOrgUuid, batchId, null);
					if (!Strings.isNullOrEmpty(outbound)) {
						EnterpriseFiler.file(outbound, configName);
					}

				} catch (Exception ex) {
					throw new PipelineException("Failed to process exchange " + exchangeId + " and batch " + batchId, ex);
				}
			}
		}

	}*/

	/*private static void fixMissingExchanges() {

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
	}*/

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
				headers.remove(HeaderKeys.BatchIdsJson);
				String newHeaderJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
				exchange.setHeaders(newHeaderJson);

				auditRepository.save(exchange);

			} catch (JsonProcessingException e) {
				LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
			}

			if (!headers.containsKey(HeaderKeys.BatchIdsJson)) {

				//fix the batch IDs not being in the exchange
				List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeUuid);
				if (!batches.isEmpty()) {

					List<UUID> batchUuids = batches
							.stream()
							.map(t -> t.getBatchId())
							.collect(Collectors.toList());
					try {
						String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchUuids.toArray());
						headers.put(HeaderKeys.BatchIdsJson, batchUuidsStr);
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

	/*private static UUID findSystemId(Service service, String software, String messageVersion) throws PipelineException {

		List<JsonServiceInterfaceEndpoint> endpoints = null;
		try {
			endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});

			for (JsonServiceInterfaceEndpoint endpoint: endpoints) {

				UUID endpointSystemId = endpoint.getSystemUuid();
				String endpointInterfaceId = endpoint.getTechnicalInterfaceUuid().toString();

				LibraryRepository libraryRepository = new LibraryRepository();
				ActiveItem activeItem = libraryRepository.getActiveItemByItemId(endpointSystemId);
				Item item = libraryRepository.getItemByKey(endpointSystemId, activeItem.getAuditId());
				LibraryItem libraryItem = QueryDocumentSerializer.readLibraryItemFromXml(item.getXmlContent());
				System system = libraryItem.getSystem();
				for (TechnicalInterface technicalInterface: system.getTechnicalInterface()) {

					if (endpointInterfaceId.equals(technicalInterface.getUuid())
							&& technicalInterface.getMessageFormat().equalsIgnoreCase(software)
							&& technicalInterface.getMessageFormatVersion().equalsIgnoreCase(messageVersion)) {

						return endpointSystemId;
					}
				}
			}
		} catch (Exception e) {
			throw new PipelineException("Failed to process endpoints from service " + service.getId());
		}

		return null;
	}
*/
	/*private static void addSystemIdToExchangeHeaders() throws Exception {
		LOG.info("populateExchangeBatchPatients");

		AuditRepository auditRepository = new AuditRepository();
		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
		ResourceRepository resourceRepository = new ResourceRepository();
		ServiceRepository serviceRepository = new ServiceRepository();
		//OrganisationRepository organisationRepository = new OrganisationRepository();

		Session session = CassandraConnector.getInstance().getSession();
		Statement stmt = new SimpleStatement("SELECT exchange_id FROM audit.exchange LIMIT 500;");
		stmt.setFetchSize(100);

		ResultSet rs = session.execute(stmt);
		while (!rs.isExhausted()) {
			Row row = rs.one();
			UUID exchangeId = row.get(0, UUID.class);

			org.endeavourhealth.core.data.audit.models.Exchange exchange = auditRepository.getExchange(exchangeId);

			String headerJson = exchange.getHeaders();
			HashMap<String, String> headers = null;
			try {
				headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);
			} catch (Exception e) {
				LOG.error("Failed to read headers for exchange " + exchangeId + " and Json " + headerJson);
				continue;
			}

			if (Strings.isNullOrEmpty(headers.get(HeaderKeys.SenderServiceUuid))) {
				LOG.info("Skipping exchange " + exchangeId + " as no service UUID");
				continue;
			}

			if (!Strings.isNullOrEmpty(headers.get(HeaderKeys.SenderSystemUuid))) {
				LOG.info("Skipping exchange " + exchangeId + " as already got system UUID");
				continue;
			}

			try {

				//work out service ID
				String serviceIdStr = headers.get(HeaderKeys.SenderServiceUuid);
				UUID serviceId = UUID.fromString(serviceIdStr);

				String software = headers.get(HeaderKeys.SourceSystem);
				String version = headers.get(HeaderKeys.SystemVersion);
				Service service = serviceRepository.getById(serviceId);
				UUID systemUuid = findSystemId(service, software, version);

				headers.put(HeaderKeys.SenderSystemUuid, systemUuid.toString());

				//work out protocol IDs
				try {
					String newProtocolIdsJson = DetermineRelevantProtocolIds.getProtocolIdsForPublisherService(serviceIdStr);
					headers.put(HeaderKeys.ProtocolIds, newProtocolIdsJson);
				} catch (Exception ex) {
					LOG.error("Failed to recalculate protocols for " + exchangeId + ": " + ex.getMessage());
				}

				//save to DB
				headerJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
				exchange.setHeaders(headerJson);
				auditRepository.save(exchange);

			} catch (Exception ex) {
				LOG.error("Error with exchange " + exchangeId, ex);
			}
		}

		LOG.info("Finished populateExchangeBatchPatients");
	}*/


	/*private static void populateExchangeBatchPatients() throws Exception {
		LOG.info("populateExchangeBatchPatients");

		AuditRepository auditRepository = new AuditRepository();
		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
		ResourceRepository resourceRepository = new ResourceRepository();
		//ServiceRepository serviceRepository = new ServiceRepository();
		//OrganisationRepository organisationRepository = new OrganisationRepository();

		Session session = CassandraConnector.getInstance().getSession();
		Statement stmt = new SimpleStatement("SELECT exchange_id FROM audit.exchange LIMIT 500;");
		stmt.setFetchSize(100);

		ResultSet rs = session.execute(stmt);
		while (!rs.isExhausted()) {
			Row row = rs.one();
			UUID exchangeId = row.get(0, UUID.class);

			org.endeavourhealth.core.data.audit.models.Exchange exchange = auditRepository.getExchange(exchangeId);

			String headerJson = exchange.getHeaders();
			HashMap<String, String> headers = null;
			try {
				headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);
			} catch (Exception e) {
				LOG.error("Failed to read headers for exchange " + exchangeId + " and Json " + headerJson);
				continue;
			}

			if (Strings.isNullOrEmpty(headers.get(HeaderKeys.SenderServiceUuid))
					|| Strings.isNullOrEmpty(headers.get(HeaderKeys.SenderSystemUuid))) {
				LOG.info("Skipping exchange " + exchangeId + " because no service or system in header");
				continue;
			}

			try {
				UUID serviceId = UUID.fromString(headers.get(HeaderKeys.SenderServiceUuid));
				UUID systemId = UUID.fromString(headers.get(HeaderKeys.SenderSystemUuid));

				List<ExchangeBatch> exchangeBatches = exchangeBatchRepository.retrieveForExchangeId(exchangeId);
				for (ExchangeBatch exchangeBatch : exchangeBatches) {

					if (exchangeBatch.getEdsPatientId() != null) {
						continue;
					}

					UUID batchId = exchangeBatch.getBatchId();
					List<ResourceByExchangeBatch> resourceWrappers = resourceRepository.getResourcesForBatch(batchId, ResourceType.Patient.toString());
					if (resourceWrappers.isEmpty()) {
						continue;
					}

					List<UUID> patientIds = new ArrayList<>();
					for (ResourceByExchangeBatch resourceWrapper : resourceWrappers) {
						UUID patientId = resourceWrapper.getResourceId();

						if (resourceWrapper.getIsDeleted()) {
							deleteEntirePatientRecord(patientId, serviceId, systemId, exchangeId, batchId);
						}

						if (!patientIds.contains(patientId)) {
							patientIds.add(patientId);
						}
					}

					if (patientIds.size() != 1) {
						LOG.info("Skipping exchange " + exchangeId + " and batch " + batchId + " because found " + patientIds.size() + " patient IDs");
						continue;
					}

					UUID patientId = patientIds.get(0);
					exchangeBatch.setEdsPatientId(patientId);

					exchangeBatchRepository.save(exchangeBatch);
				}
			} catch (Exception ex) {
				LOG.error("Error with exchange " + exchangeId, ex);
			}
		}

		LOG.info("Finished populateExchangeBatchPatients");
	}

	private static void deleteEntirePatientRecord(UUID patientId, UUID serviceId, UUID systemId, UUID exchangeId, UUID batchId) throws Exception {

		FhirStorageService storageService = new FhirStorageService(serviceId, systemId);

		ResourceRepository resourceRepository = new ResourceRepository();
		List<ResourceByPatient> resourceWrappers = resourceRepository.getResourcesByPatient(serviceId, systemId, patientId);
		for (ResourceByPatient resourceWrapper: resourceWrappers) {
			String json = resourceWrapper.getResourceData();
			Resource resource = new JsonParser().parse(json);

			storageService.exchangeBatchDelete(exchangeId, batchId, resource);
		}


	}*/

	/*private static void convertPatientSearch() {
		LOG.info("Converting Patient Search");

		ResourceRepository resourceRepository = new ResourceRepository();

		try {
			Iterable<Service> iterable = new ServiceRepository().getAll();
			for (Service service : iterable) {
				UUID serviceId = service.getId();
				LOG.info("Doing service " + service.getName());

				for (UUID systemId : findSystemIds(service)) {

					List<ResourceByService> resourceWrappers = resourceRepository.getResourcesByService(serviceId, systemId, ResourceType.EpisodeOfCare.toString());
					for (ResourceByService resourceWrapper: resourceWrappers) {
						if (Strings.isNullOrEmpty(resourceWrapper.getResourceData())) {
							continue;
						}

						try {
							EpisodeOfCare episodeOfCare = (EpisodeOfCare) new JsonParser().parse(resourceWrapper.getResourceData());
							String patientId = ReferenceHelper.getReferenceId(episodeOfCare.getPatient());

							ResourceHistory patientWrapper = resourceRepository.getCurrentVersion(ResourceType.Patient.toString(), UUID.fromString(patientId));
							if (Strings.isNullOrEmpty(patientWrapper.getResourceData())) {
								continue;
							}

							Patient patient = (Patient) new JsonParser().parse(patientWrapper.getResourceData());

							PatientSearchHelper.update(serviceId, systemId, patient);
							PatientSearchHelper.update(serviceId, systemId, episodeOfCare);

						} catch (Exception ex) {
							LOG.error("Failed on " + resourceWrapper.getResourceType() + " " + resourceWrapper.getResourceId(), ex);
						}
					}
				}
			}

			LOG.info("Converted Patient Search");

		} catch (Exception ex) {
			LOG.error("", ex);
		}

	}*/

	private static List<UUID> findSystemIds(Service service) throws Exception {

		List<UUID> ret = new ArrayList<>();

		List<JsonServiceInterfaceEndpoint> endpoints = null;
		try {
			endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
			for (JsonServiceInterfaceEndpoint endpoint: endpoints) {
				UUID endpointSystemId = endpoint.getSystemUuid();
				ret.add(endpointSystemId);
			}
		} catch (Exception e) {
			throw new Exception("Failed to process endpoints from service " + service.getId());
		}

		return ret;
	}

	/*private static void convertPatientLink() {
		LOG.info("Converting Patient Link");

		ResourceRepository resourceRepository = new ResourceRepository();

		try {
			Iterable<Service> iterable = new ServiceRepository().getAll();
			for (Service service : iterable) {
				UUID serviceId = service.getId();
				LOG.info("Doing service " + service.getName());

				for (UUID systemId : findSystemIds(service)) {

					List<ResourceByService> resourceWrappers = resourceRepository.getResourcesByService(serviceId, systemId, ResourceType.Patient.toString());
					for (ResourceByService resourceWrapper: resourceWrappers) {
						if (Strings.isNullOrEmpty(resourceWrapper.getResourceData())) {
							continue;
						}

						try {
							Patient patient = (Patient)new JsonParser().parse(resourceWrapper.getResourceData());
							PatientLinkHelper.updatePersonId(patient);

						} catch (Exception ex) {
							LOG.error("Failed on " + resourceWrapper.getResourceType() + " " + resourceWrapper.getResourceId(), ex);
						}
					}
				}
			}

			LOG.info("Converted Patient Link");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static void fixConfidentialPatients(String sharedStoragePath, UUID justThisService) {
		LOG.info("Fixing Confidential Patients using path " + sharedStoragePath + " and service " + justThisService);

		ResourceRepository resourceRepository = new ResourceRepository();
		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
		ParserPool parserPool = new ParserPool();
		MappingManager mappingManager = CassandraConnector.getInstance().getMappingManager();
		Mapper<ResourceHistory> mapperResourceHistory = mappingManager.mapper(ResourceHistory.class);
		Mapper<ResourceByExchangeBatch> mapperResourceByExchangeBatch = mappingManager.mapper(ResourceByExchangeBatch.class);

		try {
			Iterable<Service> iterable = new ServiceRepository().getAll();
			for (Service service : iterable) {
				UUID serviceId = service.getId();

				if (justThisService != null
						&& !service.getId().equals(justThisService)) {
					LOG.info("Skipping service " + service.getName());
					continue;
				}

				LOG.info("Doing service " + service.getName());

				List<UUID> systemIds = findSystemIds(service);

				Map<String, ResourceHistory> resourcesFixed = new HashMap<>();
				Map<UUID, Set<UUID>> exchangeBatchesToPutInProtocolQueue = new HashMap<>();

				List<UUID> exchangeIds = new AuditRepository().getExchangeIdsForService(serviceId);
				for (UUID exchangeId: exchangeIds) {

					Exchange exchange = AuditWriter.readExchange(exchangeId);

					String software = exchange.getHeader(HeaderKeys.SourceSystem);
					if (!software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {
						continue;
					}

					if (systemIds.size() > 1) {
						throw new Exception("Multiple system IDs for service " + serviceId);
					}
					UUID systemId = systemIds.get(0);

					String body = exchange.getBody();
					String[] files = body.split(java.lang.System.lineSeparator());

					if (files.length == 0) {
						continue;
					}

					LOG.info("Doing Emis CSV exchange " + exchangeId);

					Set<UUID> batchIdsToPutInProtocolQueue = new HashSet<>();

					Map<UUID, List<UUID>> batchesPerPatient = new HashMap<>();
					List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeId);
					for (ExchangeBatch batch: batches) {
						UUID patientId = batch.getEdsPatientId();
						if (patientId != null) {
							List<UUID> batchIds = batchesPerPatient.get(patientId);
							if (batchIds == null) {
								batchIds = new ArrayList<>();
								batchesPerPatient.put(patientId, batchIds);
							}
							batchIds.add(batch.getBatchId());
						}
					}

					File f = new File(sharedStoragePath, files[0]);
					File dir = f.getParentFile();

					String version = EmisCsvToFhirTransformer.determineVersion(dir);

					String dataSharingAgreementId = EmisCsvToFhirTransformer.findDataSharingAgreementGuid(f);

					EmisCsvHelper helper = new EmisCsvHelper(dataSharingAgreementId);
					ResourceFiler filer = new ResourceFiler(exchangeId, serviceId, systemId, null, null, 1);

					Map<Class, AbstractCsvParser> parsers = new HashMap<>();

					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.admin.Patient.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord.class, dir, version, true, parsers);

					ProblemPreTransformer.transform(version, parsers, filer, helper);
					ObservationPreTransformer.transform(version, parsers, filer, helper);
					DrugRecordPreTransformer.transform(version, parsers, filer, helper);
					IssueRecordPreTransformer.transform(version, parsers, filer, helper);
					DiaryPreTransformer.transform(version, parsers, filer, helper);

					org.endeavourhealth.transform.emis.csv.schema.admin.Patient patientParser = (org.endeavourhealth.transform.emis.csv.schema.admin.Patient)parsers.get(org.endeavourhealth.transform.emis.csv.schema.admin.Patient.class);
					while (patientParser.nextRecord()) {
						if (patientParser.getIsConfidential()
								&& !patientParser.getDeleted()) {
							PatientTransformer.createResource(patientParser, filer, helper, version);
						}
					}
					patientParser.close();

					org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation consultationParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation)parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation.class);
					while (consultationParser.nextRecord()) {
						if (consultationParser.getIsConfidential()
								&& !consultationParser.getDeleted()) {
							ConsultationTransformer.createResource(consultationParser, filer, helper, version);
						}
					}
					consultationParser.close();

					org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation observationParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation)parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation.class);
					while (observationParser.nextRecord()) {
						if (observationParser.getIsConfidential()
								&& !observationParser.getDeleted()) {
							ObservationTransformer.createResource(observationParser, filer, helper, version);
						}
					}
					observationParser.close();

					org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary diaryParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary)parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary.class);
					while (diaryParser.nextRecord()) {
						if (diaryParser.getIsConfidential()
								&& !diaryParser.getDeleted()) {
							DiaryTransformer.createResource(diaryParser, filer, helper, version);
						}
					}
					diaryParser.close();

					org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord drugRecordParser = (org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord)parsers.get(org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord.class);
					while (drugRecordParser.nextRecord()) {
						if (drugRecordParser.getIsConfidential()
								&& !drugRecordParser.getDeleted()) {
							DrugRecordTransformer.createResource(drugRecordParser, filer, helper, version);
						}
					}
					drugRecordParser.close();

					org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord issueRecordParser = (org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord)parsers.get(org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord.class);
					while (issueRecordParser.nextRecord()) {
						if (issueRecordParser.getIsConfidential()
								&& !issueRecordParser.getDeleted()) {
							IssueRecordTransformer.createResource(issueRecordParser, filer, helper, version);
						}
					}
					issueRecordParser.close();

					filer.waitToFinish(); //just to close the thread pool, even though it's not been used
					List<Resource> resources = filer.getNewResources();
					for (Resource resource: resources) {

						String patientId = IdHelper.getPatientId(resource);
						UUID edsPatientId = UUID.fromString(patientId);

						ResourceType resourceType = resource.getResourceType();
						UUID resourceId = UUID.fromString(resource.getId());

						boolean foundResourceInDbBatch = false;

						List<UUID> batchIds = batchesPerPatient.get(edsPatientId);
						if (batchIds != null) {
							for (UUID batchId : batchIds) {

								List<ResourceByExchangeBatch> resourceByExchangeBatches = resourceRepository.getResourcesForBatch(batchId, resourceType.toString(), resourceId);
								if (resourceByExchangeBatches.isEmpty()) {
									//if we've deleted data, this will be null
									continue;
								}

								foundResourceInDbBatch = true;

								for (ResourceByExchangeBatch resourceByExchangeBatch : resourceByExchangeBatches) {

									String json = resourceByExchangeBatch.getResourceData();
									if (!Strings.isNullOrEmpty(json)) {
										LOG.warn("JSON already in resource " + resourceType + " " + resourceId);
									} else {

										json = parserPool.composeString(resource);
										resourceByExchangeBatch.setResourceData(json);
										resourceByExchangeBatch.setIsDeleted(false);
										resourceByExchangeBatch.setSchemaVersion("0.1");

										LOG.info("Saved resource by batch " + resourceType + " " + resourceId + " in batch " + batchId);

										UUID versionUuid = resourceByExchangeBatch.getVersion();
										ResourceHistory resourceHistory = resourceRepository.getResourceHistoryByKey(resourceId, resourceType.toString(), versionUuid);
										if (resourceHistory == null) {
											throw new Exception("Failed to find resource history for " + resourceType + " " + resourceId + " and version " + versionUuid);
										}
										resourceHistory.setIsDeleted(false);
										resourceHistory.setResourceData(json);
										resourceHistory.setResourceChecksum(FhirStorageService.generateChecksum(json));
										resourceHistory.setSchemaVersion("0.1");

										resourceRepository.save(resourceByExchangeBatch);
										resourceRepository.save(resourceHistory);
										batchIdsToPutInProtocolQueue.add(batchId);

										String key = resourceType.toString() + ":" + resourceId;
										resourcesFixed.put(key, resourceHistory);
									}

									//if a patient became confidential, we will have deleted all resources for that
									//patient, so we need to undo that too
									//to undelete WHOLE patient record
									//1. if THIS resource is a patient
									//2. get all other deletes from the same exchange batch
									//3. delete those from resource_by_exchange_batch (the deleted ones only)
									//4. delete same ones from resource_history
									//5. retrieve most recent resource_history
									//6. if not deleted, add to resources fixed
									if (resourceType == ResourceType.Patient) {

										List<ResourceByExchangeBatch> resourcesInSameBatch = resourceRepository.getResourcesForBatch(batchId);
										LOG.info("Undeleting " + resourcesInSameBatch.size() + " resources for batch " + batchId);
										for (ResourceByExchangeBatch resourceInSameBatch: resourcesInSameBatch) {
											if (!resourceInSameBatch.getIsDeleted()) {
												continue;
											}

											//patient and episode resources will be restored by the above stuff, so don't try
											//to do it again
											if (resourceInSameBatch.getResourceType().equals(ResourceType.Patient.toString())
													|| resourceInSameBatch.getResourceType().equals(ResourceType.EpisodeOfCare.toString())) {
												continue;
											}

											ResourceHistory deletedResourceHistory = resourceRepository.getResourceHistoryByKey(resourceInSameBatch.getResourceId(), resourceInSameBatch.getResourceType(), resourceInSameBatch.getVersion());

											mapperResourceByExchangeBatch.delete(resourceInSameBatch);
											mapperResourceHistory.delete(deletedResourceHistory);
											batchIdsToPutInProtocolQueue.add(batchId);

											//check the most recent version of our resource, and if it's not deleted, add to the list to update the resource_by_service table
											ResourceHistory mostRecentDeletedResourceHistory = resourceRepository.getCurrentVersion(resourceInSameBatch.getResourceType(), resourceInSameBatch.getResourceId());
											if (mostRecentDeletedResourceHistory != null
													&& !mostRecentDeletedResourceHistory.getIsDeleted()) {

												String key2 = mostRecentDeletedResourceHistory.getResourceType().toString() + ":" + mostRecentDeletedResourceHistory.getResourceId();
												resourcesFixed.put(key2, mostRecentDeletedResourceHistory);
											}
										}
									}
								}
							}
						}

						//if we didn't find records in the DB to update, then
						if (!foundResourceInDbBatch) {

							//we can't generate a back-dated time UUID, but we need one so the resource_history
							//table is in order. To get a suitable time UUID, we just pull out the first exchange batch for our exchange,
							//and the batch ID is actually a time UUID that was allocated around the right time
							ExchangeBatch firstBatch = exchangeBatchRepository.retrieveFirstForExchangeId(exchangeId);

							//if there was no batch for the exchange, then the exchange wasn't processed at all. So skip this exchange
							//and we'll pick up the same patient data in a following exchange
							if (firstBatch == null) {
								continue;
							}
							UUID versionUuid = firstBatch.getBatchId();

							//find suitable batch ID
							UUID batchId = null;
							if (batchIds != null
									&& batchIds.size() > 0) {
								batchId = batchIds.get(batchIds.size()-1);

							} else {
								//create new batch ID if not found
								ExchangeBatch exchangeBatch = new ExchangeBatch();
								exchangeBatch.setBatchId(UUIDs.timeBased());
								exchangeBatch.setExchangeId(exchangeId);
								exchangeBatch.setInsertedAt(new Date());
								exchangeBatch.setEdsPatientId(edsPatientId);
								exchangeBatchRepository.save(exchangeBatch);

								batchId = exchangeBatch.getBatchId();

								//add to map for next resource
								if (batchIds == null) {
									batchIds = new ArrayList<>();
								}
								batchIds.add(batchId);
								batchesPerPatient.put(edsPatientId, batchIds);
							}

							String json = parserPool.composeString(resource);

							ResourceHistory resourceHistory = new ResourceHistory();
							resourceHistory.setResourceId(resourceId);
							resourceHistory.setResourceType(resourceType.toString());
							resourceHistory.setVersion(versionUuid);
							resourceHistory.setCreatedAt(new Date());
							resourceHistory.setServiceId(serviceId);
							resourceHistory.setSystemId(systemId);
							resourceHistory.setIsDeleted(false);
							resourceHistory.setSchemaVersion("0.1");
							resourceHistory.setResourceData(json);
							resourceHistory.setResourceChecksum(FhirStorageService.generateChecksum(json));

							ResourceByExchangeBatch resourceByExchangeBatch = new ResourceByExchangeBatch();
							resourceByExchangeBatch.setBatchId(batchId);
							resourceByExchangeBatch.setExchangeId(exchangeId);
							resourceByExchangeBatch.setResourceType(resourceType.toString());
							resourceByExchangeBatch.setResourceId(resourceId);
							resourceByExchangeBatch.setVersion(versionUuid);
							resourceByExchangeBatch.setIsDeleted(false);
							resourceByExchangeBatch.setSchemaVersion("0.1");
							resourceByExchangeBatch.setResourceData(json);

							resourceRepository.save(resourceHistory);
							resourceRepository.save(resourceByExchangeBatch);

							batchIdsToPutInProtocolQueue.add(batchId);
						}
					}

					if (!batchIdsToPutInProtocolQueue.isEmpty()) {
						exchangeBatchesToPutInProtocolQueue.put(exchangeId, batchIdsToPutInProtocolQueue);
					}
				}

				//update the resource_by_service table (and the resource_by_patient view)
				for (ResourceHistory resourceHistory: resourcesFixed.values()) {
					UUID latestVersionUpdatedUuid = resourceHistory.getVersion();

					ResourceHistory latestVersion = resourceRepository.getCurrentVersion(resourceHistory.getResourceType(), resourceHistory.getResourceId());
					UUID latestVersionUuid = latestVersion.getVersion();

					//if there have been subsequent updates to the resource, then skip it
					if (!latestVersionUuid.equals(latestVersionUpdatedUuid)) {
						continue;
					}

					Resource resource = parserPool.parse(resourceHistory.getResourceData());
					ResourceMetadata metadata = MetadataFactory.createMetadata(resource);
					UUID patientId = ((PatientCompartment)metadata).getPatientId();

					ResourceByService resourceByService = new ResourceByService();
					resourceByService.setServiceId(resourceHistory.getServiceId());
					resourceByService.setSystemId(resourceHistory.getSystemId());
					resourceByService.setResourceType(resourceHistory.getResourceType());
					resourceByService.setResourceId(resourceHistory.getResourceId());
					resourceByService.setCurrentVersion(resourceHistory.getVersion());
					resourceByService.setUpdatedAt(resourceHistory.getCreatedAt());
					resourceByService.setPatientId(patientId);
					resourceByService.setSchemaVersion(resourceHistory.getSchemaVersion());
					resourceByService.setResourceMetadata(JsonSerializer.serialize(metadata));
					resourceByService.setResourceData(resourceHistory.getResourceData());

					resourceRepository.save(resourceByService);

					//call out to our patient search and person matching services
					if (resource instanceof Patient) {
						PatientLinkHelper.updatePersonId((Patient)resource);
						PatientSearchHelper.update(serviceId, resourceHistory.getSystemId(), (Patient)resource);

					} else if (resource instanceof EpisodeOfCare) {
						PatientSearchHelper.update(serviceId, resourceHistory.getSystemId(), (EpisodeOfCare)resource);
					}
				}

				if (!exchangeBatchesToPutInProtocolQueue.isEmpty()) {
					//find the config for our protocol queue
					String configXml = ConfigManager.getConfiguration("inbound", "queuereader");

					//the config XML may be one of two serialised classes, so we use a try/catch to safely try both if necessary
					QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
					Pipeline pipeline = configuration.getPipeline();

					PostMessageToExchangeConfig config = pipeline
							.getPipelineComponents()
							.stream()
							.filter(t -> t instanceof PostMessageToExchangeConfig)
							.map(t -> (PostMessageToExchangeConfig) t)
							.filter(t -> t.getExchange().equalsIgnoreCase("EdsProtocol"))
							.collect(StreamExtension.singleOrNullCollector());

					//post to the protocol exchange
					for (UUID exchangeId : exchangeBatchesToPutInProtocolQueue.keySet()) {
						Set<UUID> batchIds = exchangeBatchesToPutInProtocolQueue.get(exchangeId);

						org.endeavourhealth.core.messaging.exchange.Exchange exchange = AuditWriter.readExchange(exchangeId);

						String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIds);
						exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

						PostMessageToExchange component = new PostMessageToExchange(config);
						component.process(exchange);
					}
				}
			}

			LOG.info("Finished Fixing Confidential Patients");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static void fixDeletedAppointments(String sharedStoragePath, boolean saveChanges, UUID justThisService) {
		LOG.info("Fixing Deleted Appointments using path " + sharedStoragePath + " and service " + justThisService);

		ResourceRepository resourceRepository = new ResourceRepository();
		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
		ParserPool parserPool = new ParserPool();
		MappingManager mappingManager = CassandraConnector.getInstance().getMappingManager();
		Mapper<ResourceHistory> mapperResourceHistory = mappingManager.mapper(ResourceHistory.class);
		Mapper<ResourceByExchangeBatch> mapperResourceByExchangeBatch = mappingManager.mapper(ResourceByExchangeBatch.class);

		try {
			Iterable<Service> iterable = new ServiceRepository().getAll();
			for (Service service : iterable) {
				UUID serviceId = service.getId();

				if (justThisService != null
						&& !service.getId().equals(justThisService)) {
					LOG.info("Skipping service " + service.getName());
					continue;
				}

				LOG.info("Doing service " + service.getName());

				List<UUID> systemIds = findSystemIds(service);

				List<UUID> exchangeIds = new AuditRepository().getExchangeIdsForService(serviceId);
				for (UUID exchangeId: exchangeIds) {

					Exchange exchange = AuditWriter.readExchange(exchangeId);

					String software = exchange.getHeader(HeaderKeys.SourceSystem);
					if (!software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {
						continue;
					}

					if (systemIds.size() > 1) {
						throw new Exception("Multiple system IDs for service " + serviceId);
					}
					UUID systemId = systemIds.get(0);

					String body = exchange.getBody();
					String[] files = body.split(java.lang.System.lineSeparator());

					if (files.length == 0) {
						continue;
					}

					LOG.info("Doing Emis CSV exchange " + exchangeId);

					Map<UUID, List<UUID>> batchesPerPatient = new HashMap<>();
					List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeId);
					for (ExchangeBatch batch : batches) {
						UUID patientId = batch.getEdsPatientId();
						if (patientId != null) {
							List<UUID> batchIds = batchesPerPatient.get(patientId);
							if (batchIds == null) {
								batchIds = new ArrayList<>();
								batchesPerPatient.put(patientId, batchIds);
							}
							batchIds.add(batch.getBatchId());
						}
					}

					File f = new File(sharedStoragePath, files[0]);
					File dir = f.getParentFile();

					String version = EmisCsvToFhirTransformer.determineVersion(dir);

					Map<Class, AbstractCsvParser> parsers = new HashMap<>();

					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.admin.Patient.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.appointment.Slot.class, dir, version, true, parsers);

					//find any deleted patients
					List<UUID> deletedPatientUuids = new ArrayList<>();

					org.endeavourhealth.transform.emis.csv.schema.admin.Patient patientParser = (org.endeavourhealth.transform.emis.csv.schema.admin.Patient) parsers.get(org.endeavourhealth.transform.emis.csv.schema.admin.Patient.class);
					while (patientParser.nextRecord()) {
						if (patientParser.getDeleted()) {
							//find the EDS patient ID for this local guid
							String patientGuid = patientParser.getPatientGuid();
							UUID edsPatientId = IdHelper.getEdsResourceId(serviceId, systemId, ResourceType.Patient, patientGuid);
							if (edsPatientId == null) {
								throw new Exception("Failed to find patient ID for service " + serviceId + " system " + systemId + " resourceType " + ResourceType.Patient + " local ID " + patientGuid);
							}
							deletedPatientUuids.add(edsPatientId);
						}
					}
					patientParser.close();

					//go through the appts file to find properly deleted appt GUIDS
					List<UUID> deletedApptUuids = new ArrayList<>();

					org.endeavourhealth.transform.emis.csv.schema.appointment.Slot apptParser = (org.endeavourhealth.transform.emis.csv.schema.appointment.Slot) parsers.get(org.endeavourhealth.transform.emis.csv.schema.appointment.Slot.class);
					while (apptParser.nextRecord()) {
						if (apptParser.getDeleted()) {
							String patientGuid = apptParser.getPatientGuid();
							String slotGuid = apptParser.getSlotGuid();
							if (!Strings.isNullOrEmpty(patientGuid)) {
								String uniqueLocalId = EmisCsvHelper.createUniqueId(patientGuid, slotGuid);
								UUID edsApptId = IdHelper.getEdsResourceId(serviceId, systemId, ResourceType.Appointment, uniqueLocalId);
								deletedApptUuids.add(edsApptId);
							}
						}
					}
					apptParser.close();

					for (UUID edsPatientId : deletedPatientUuids) {

						List<UUID> batchIds = batchesPerPatient.get(edsPatientId);
						if (batchIds == null) {
							//if there are no batches for this patient, we'll be handling this data in another exchange
							continue;
						}

						for (UUID batchId : batchIds) {
							List<ResourceByExchangeBatch> apptWrappers = resourceRepository.getResourcesForBatch(batchId, ResourceType.Appointment.toString());
							for (ResourceByExchangeBatch apptWrapper : apptWrappers) {

								//ignore non-deleted appts
								if (!apptWrapper.getIsDeleted()) {
									continue;
								}

								//if the appt was deleted legitamately, then skip it
								UUID apptId = apptWrapper.getResourceId();
								if (deletedApptUuids.contains(apptId)) {
									continue;
								}

								ResourceHistory deletedResourceHistory = resourceRepository.getResourceHistoryByKey(apptWrapper.getResourceId(), apptWrapper.getResourceType(), apptWrapper.getVersion());

								if (saveChanges) {
									mapperResourceByExchangeBatch.delete(apptWrapper);
									mapperResourceHistory.delete(deletedResourceHistory);
								}
								LOG.info("Un-deleted " + apptWrapper.getResourceType() + " " + apptWrapper.getResourceId() + " in batch " + batchId + " patient " + edsPatientId);

								//now get the most recent instance of the appointment, and if it's NOT deleted, insert into the resource_by_service table
								ResourceHistory mostRecentResourceHistory = resourceRepository.getCurrentVersion(apptWrapper.getResourceType(), apptWrapper.getResourceId());
								if (mostRecentResourceHistory != null
										&& !mostRecentResourceHistory.getIsDeleted()) {

									Resource resource = parserPool.parse(mostRecentResourceHistory.getResourceData());
									ResourceMetadata metadata = MetadataFactory.createMetadata(resource);
									UUID patientId = ((PatientCompartment) metadata).getPatientId();

									ResourceByService resourceByService = new ResourceByService();
									resourceByService.setServiceId(mostRecentResourceHistory.getServiceId());
									resourceByService.setSystemId(mostRecentResourceHistory.getSystemId());
									resourceByService.setResourceType(mostRecentResourceHistory.getResourceType());
									resourceByService.setResourceId(mostRecentResourceHistory.getResourceId());
									resourceByService.setCurrentVersion(mostRecentResourceHistory.getVersion());
									resourceByService.setUpdatedAt(mostRecentResourceHistory.getCreatedAt());
									resourceByService.setPatientId(patientId);
									resourceByService.setSchemaVersion(mostRecentResourceHistory.getSchemaVersion());
									resourceByService.setResourceMetadata(JsonSerializer.serialize(metadata));
									resourceByService.setResourceData(mostRecentResourceHistory.getResourceData());

									if (saveChanges) {
										resourceRepository.save(resourceByService);
									}
									LOG.info("Restored " + apptWrapper.getResourceType() + " " + apptWrapper.getResourceId() + " to resource_by_service table");
								}
							}
						}
					}
				}
			}

			LOG.info("Finished Deleted Appointments Patients");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/


	/*private static void fixReviews(String sharedStoragePath, UUID justThisService) {
		LOG.info("Fixing Reviews using path " + sharedStoragePath + " and service " + justThisService);

		ResourceRepository resourceRepository = new ResourceRepository();
		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
		ParserPool parserPool = new ParserPool();

		try {
			Iterable<Service> iterable = new ServiceRepository().getAll();
			for (Service service : iterable) {
				UUID serviceId = service.getId();

				if (justThisService != null
						&& !service.getId().equals(justThisService)) {
					LOG.info("Skipping service " + service.getName());
					continue;
				}

				LOG.info("Doing service " + service.getName());

				List<UUID> systemIds = findSystemIds(service);

				Map<String, Long> problemCodes = new HashMap<>();

				List<UUID> exchangeIds = new AuditRepository().getExchangeIdsForService(serviceId);
				for (UUID exchangeId: exchangeIds) {

					Exchange exchange = AuditWriter.readExchange(exchangeId);

					String software = exchange.getHeader(HeaderKeys.SourceSystem);
					if (!software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {
						continue;
					}

					String body = exchange.getBody();
					String[] files = body.split(java.lang.System.lineSeparator());

					if (files.length == 0) {
						continue;
					}

					Map<UUID, List<UUID>> batchesPerPatient = new HashMap<>();
					List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeId);
					LOG.info("Doing Emis CSV exchange " + exchangeId + " with " + batches.size() + " batches");
					for (ExchangeBatch batch: batches) {
						UUID patientId = batch.getEdsPatientId();
						if (patientId != null) {
							List<UUID> batchIds = batchesPerPatient.get(patientId);
							if (batchIds == null) {
								batchIds = new ArrayList<>();
								batchesPerPatient.put(patientId, batchIds);
							}
							batchIds.add(batch.getBatchId());
						}
					}

					File f = new File(sharedStoragePath, files[0]);
					File dir = f.getParentFile();

					String version = EmisCsvToFhirTransformer.determineVersion(dir);

					Map<Class, AbstractCsvParser> parsers = new HashMap<>();
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation.class, dir, version, true, parsers);

					org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem problemParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem)parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem.class);
					org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation observationParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation)parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation.class);

					while (problemParser.nextRecord()) {
						String patientGuid = problemParser.getPatientGuid();
						String observationGuid = problemParser.getObservationGuid();
						String key = patientGuid + ":" + observationGuid;
						if (!problemCodes.containsKey(key)) {
							problemCodes.put(key, null);
						}
					}
					problemParser.close();

					while (observationParser.nextRecord()) {
						String patientGuid = observationParser.getPatientGuid();
						String observationGuid = observationParser.getObservationGuid();
						String key = patientGuid + ":" + observationGuid;
						if (problemCodes.containsKey(key)) {
							Long codeId = observationParser.getCodeId();
							if (codeId == null) {
								continue;
							}
							problemCodes.put(key, codeId);
						}
					}
					observationParser.close();
					LOG.info("Found " + problemCodes.size() + " problem codes so far");

					String dataSharingAgreementId = EmisCsvToFhirTransformer.findDataSharingAgreementGuid(f);

					EmisCsvHelper helper = new EmisCsvHelper(dataSharingAgreementId);

					while (observationParser.nextRecord()) {
						String problemGuid = observationParser.getProblemGuid();
						if (!Strings.isNullOrEmpty(problemGuid)) {
							String patientGuid = observationParser.getPatientGuid();
							Long codeId = observationParser.getCodeId();
							if (codeId == null) {
								continue;
							}

							String key = patientGuid + ":" + problemGuid;
							Long problemCodeId = problemCodes.get(key);
							if (problemCodeId == null
									|| problemCodeId.longValue() != codeId.longValue()) {
								continue;
							}

							//if here, our code is the same as the problem, so it's a review
							String locallyUniqueId = patientGuid + ":" + observationParser.getObservationGuid();
							ResourceType resourceType = ObservationTransformer.getTargetResourceType(observationParser, helper);

							for (UUID systemId: systemIds) {

								UUID edsPatientId = IdHelper.getEdsResourceId(serviceId, systemId, ResourceType.Patient, patientGuid);
								if (edsPatientId == null) {
									throw new Exception("Failed to find patient ID for service " + serviceId + " system " + systemId + " resourceType " + ResourceType.Patient + " local ID " + patientGuid);
								}

								UUID edsObservationId = IdHelper.getEdsResourceId(serviceId, systemId, resourceType, locallyUniqueId);
								if (edsObservationId == null) {

									//try observations as diagnostic reports, because it could be one of those instead
									if (resourceType == ResourceType.Observation) {
										resourceType = ResourceType.DiagnosticReport;
										edsObservationId = IdHelper.getEdsResourceId(serviceId, systemId, resourceType, locallyUniqueId);
									}

									if (edsObservationId == null) {
										throw new Exception("Failed to find observation ID for service " + serviceId + " system " + systemId + " resourceType " + resourceType + " local ID " + locallyUniqueId);
									}
								}

								List<UUID> batchIds = batchesPerPatient.get(edsPatientId);
								if (batchIds == null) {
									//if there are no batches for this patient, we'll be handling this data in another exchange
									continue;
									//throw new Exception("Failed to find batch ID for patient " + edsPatientId + " in exchange " + exchangeId + " for resource " + resourceType + " " + edsObservationId);
								}
								for (UUID batchId: batchIds) {

									List<ResourceByExchangeBatch> resourceByExchangeBatches = resourceRepository.getResourcesForBatch(batchId, resourceType.toString(), edsObservationId);
									if (resourceByExchangeBatches.isEmpty()) {
										//if we've deleted data, this will be null
										continue;
										//throw new Exception("No resources found for batch " + batchId + " resource type " + resourceType + " and resource id " + edsObservationId);
									}

									for (ResourceByExchangeBatch resourceByExchangeBatch: resourceByExchangeBatches) {

										String json = resourceByExchangeBatch.getResourceData();
										if (Strings.isNullOrEmpty(json)) {
											throw new Exception("No JSON in resource " + resourceType + " " + edsObservationId + " in batch " + batchId);
										}
										Resource resource = parserPool.parse(json);
										if (addReviewExtension((DomainResource)resource)) {
											json = parserPool.composeString(resource);
											resourceByExchangeBatch.setResourceData(json);
											LOG.info("Changed " + resourceType + " " + edsObservationId + " to have extension in batch " + batchId);

											resourceRepository.save(resourceByExchangeBatch);

											UUID versionUuid = resourceByExchangeBatch.getVersion();
											ResourceHistory resourceHistory = resourceRepository.getResourceHistoryByKey(edsObservationId, resourceType.toString(), versionUuid);
											if (resourceHistory == null) {
												throw new Exception("Failed to find resource history for " + resourceType + " " + edsObservationId + " and version " + versionUuid);
											}
											resourceHistory.setResourceData(json);
											resourceHistory.setResourceChecksum(FhirStorageService.generateChecksum(json));
											resourceRepository.save(resourceHistory);

											ResourceByService resourceByService = resourceRepository.getResourceByServiceByKey(serviceId, systemId, resourceType.toString(), edsObservationId);
											if (resourceByService != null) {
												UUID serviceVersionUuid = resourceByService.getCurrentVersion();
												if (serviceVersionUuid.equals(versionUuid)) {

													resourceByService.setResourceData(json);
													resourceRepository.save(resourceByService);
												}
											}
										} else {
											LOG.info("" + resourceType + " " + edsObservationId + " already has extension");
										}
									}

								}
							}

							//1. find out resource type originall saved from
							//2. retrieve from resource_by_exchange_batch
							//3. update resource in resource_by_exchange_batch
							//4. retrieve from resource_history
							//5. update resource_history
							//6. retrieve record from resource_by_service
							//7. if resource_by_service version UUID matches the resource_history updated, then update that too
						}
					}
					observationParser.close();
				}
			}

			LOG.info("Finished Fixing Reviews");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static boolean addReviewExtension(DomainResource resource) {

		if (ExtensionConverter.hasExtension(resource, FhirExtensionUri.IS_REVIEW)) {
			return false;
		}

		Extension extension = ExtensionConverter.createExtension(FhirExtensionUri.IS_REVIEW, new BooleanType(true));
		resource.addExtension(extension);

		return true;
	}*/


	/*private static void runProtocolsForConfidentialPatients(String sharedStoragePath, UUID justThisService) {
		LOG.info("Running Protocols for Confidential Patients using path " + sharedStoragePath + " and service " + justThisService);

		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();

		try {
			Iterable<Service> iterable = new ServiceRepository().getAll();
			for (Service service : iterable) {
				UUID serviceId = service.getId();

				if (justThisService != null
						&& !service.getId().equals(justThisService)) {
					LOG.info("Skipping service " + service.getName());
					continue;
				}

				//once we match the servce, set this to null to do all other services
				justThisService = null;

				LOG.info("Doing service " + service.getName());

				List<UUID> systemIds = findSystemIds(service);


				List<String> interestingPatientGuids = new ArrayList<>();
				Map<UUID, Map<UUID, List<UUID>>> batchesPerPatientPerExchange = new HashMap<>();

				List<UUID> exchangeIds = new AuditRepository().getExchangeIdsForService(serviceId);
				for (UUID exchangeId: exchangeIds) {

					Exchange exchange = AuditWriter.readExchange(exchangeId);

					String software = exchange.getHeader(HeaderKeys.SourceSystem);
					if (!software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {
						continue;
					}

					String body = exchange.getBody();
					String[] files = body.split(java.lang.System.lineSeparator());

					if (files.length == 0) {
						continue;
					}

					LOG.info("Doing Emis CSV exchange " + exchangeId);

					Map<UUID, List<UUID>> batchesPerPatient = new HashMap<>();

					List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeId);
					for (ExchangeBatch batch : batches) {
						UUID patientId = batch.getEdsPatientId();
						if (patientId != null) {
							List<UUID> batchIds = batchesPerPatient.get(patientId);
							if (batchIds == null) {
								batchIds = new ArrayList<>();
								batchesPerPatient.put(patientId, batchIds);
							}
							batchIds.add(batch.getBatchId());
						}
					}

					batchesPerPatientPerExchange.put(exchangeId, batchesPerPatient);

					File f = new File(sharedStoragePath, files[0]);
					File dir = f.getParentFile();

					String version = EmisCsvToFhirTransformer.determineVersion(dir);

					Map<Class, AbstractCsvParser> parsers = new HashMap<>();

					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.admin.Patient.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord.class, dir, version, true, parsers);
					EmisCsvToFhirTransformer.findFileAndOpenParser(org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord.class, dir, version, true, parsers);

					org.endeavourhealth.transform.emis.csv.schema.admin.Patient patientParser = (org.endeavourhealth.transform.emis.csv.schema.admin.Patient) parsers.get(org.endeavourhealth.transform.emis.csv.schema.admin.Patient.class);
					while (patientParser.nextRecord()) {
						if (patientParser.getIsConfidential() || patientParser.getDeleted()) {
							interestingPatientGuids.add(patientParser.getPatientGuid());
						}
					}
					patientParser.close();

					org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation consultationParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation) parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation.class);
					while (consultationParser.nextRecord()) {
						if (consultationParser.getIsConfidential()
								&& !consultationParser.getDeleted()) {
							interestingPatientGuids.add(consultationParser.getPatientGuid());
						}
					}
					consultationParser.close();

					org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation observationParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation) parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation.class);
					while (observationParser.nextRecord()) {
						if (observationParser.getIsConfidential()
								&& !observationParser.getDeleted()) {
							interestingPatientGuids.add(observationParser.getPatientGuid());
						}
					}
					observationParser.close();

					org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary diaryParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary) parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary.class);
					while (diaryParser.nextRecord()) {
						if (diaryParser.getIsConfidential()
								&& !diaryParser.getDeleted()) {
							interestingPatientGuids.add(diaryParser.getPatientGuid());
						}
					}
					diaryParser.close();

					org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord drugRecordParser = (org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord) parsers.get(org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord.class);
					while (drugRecordParser.nextRecord()) {
						if (drugRecordParser.getIsConfidential()
								&& !drugRecordParser.getDeleted()) {
							interestingPatientGuids.add(drugRecordParser.getPatientGuid());
						}
					}
					drugRecordParser.close();

					org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord issueRecordParser = (org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord) parsers.get(org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord.class);
					while (issueRecordParser.nextRecord()) {
						if (issueRecordParser.getIsConfidential()
								&& !issueRecordParser.getDeleted()) {
							interestingPatientGuids.add(issueRecordParser.getPatientGuid());
						}
					}
					issueRecordParser.close();
				}

				Map<UUID, Set<UUID>> exchangeBatchesToPutInProtocolQueue = new HashMap<>();

				for (String interestingPatientGuid: interestingPatientGuids) {

					if (systemIds.size() > 1) {
						throw new Exception("Multiple system IDs for service " + serviceId);
					}
					UUID systemId = systemIds.get(0);

					UUID edsPatientId = IdHelper.getEdsResourceId(serviceId, systemId, ResourceType.Patient, interestingPatientGuid);
					if (edsPatientId == null) {
						throw new Exception("Failed to find patient ID for service " + serviceId + " system " + systemId + " resourceType " + ResourceType.Patient + " local ID " + interestingPatientGuid);
					}

					for (UUID exchangeId: batchesPerPatientPerExchange.keySet()) {
						Map<UUID, List<UUID>> batchesPerPatient = batchesPerPatientPerExchange.get(exchangeId);
						List<UUID> batches = batchesPerPatient.get(edsPatientId);
						if (batches != null) {

							Set<UUID> batchesForExchange = exchangeBatchesToPutInProtocolQueue.get(exchangeId);
							if (batchesForExchange == null) {
								batchesForExchange = new HashSet<>();
								exchangeBatchesToPutInProtocolQueue.put(exchangeId, batchesForExchange);
							}

							batchesForExchange.addAll(batches);
						}
					}
				}


				if (!exchangeBatchesToPutInProtocolQueue.isEmpty()) {
					//find the config for our protocol queue
					String configXml = ConfigManager.getConfiguration("inbound", "queuereader");

					//the config XML may be one of two serialised classes, so we use a try/catch to safely try both if necessary
					QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
					Pipeline pipeline = configuration.getPipeline();

					PostMessageToExchangeConfig config = pipeline
							.getPipelineComponents()
							.stream()
							.filter(t -> t instanceof PostMessageToExchangeConfig)
							.map(t -> (PostMessageToExchangeConfig) t)
							.filter(t -> t.getExchange().equalsIgnoreCase("EdsProtocol"))
							.collect(StreamExtension.singleOrNullCollector());

					//post to the protocol exchange
					for (UUID exchangeId : exchangeBatchesToPutInProtocolQueue.keySet()) {
						Set<UUID> batchIds = exchangeBatchesToPutInProtocolQueue.get(exchangeId);

						org.endeavourhealth.core.messaging.exchange.Exchange exchange = AuditWriter.readExchange(exchangeId);

						String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIds);
						exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);
						LOG.info("Posting exchange " + exchangeId + " batch " + batchIdString);

						PostMessageToExchange component = new PostMessageToExchange(config);
						component.process(exchange);
					}
				}
			}

			LOG.info("Finished Running Protocols for Confidential Patients");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static void fixOrgs() {

		LOG.info("Posting orgs to protocol queue");

		String[] orgIds = new String[]{
		"332f31a2-7b28-47cb-af6f-18f65440d43d",
		"c893d66b-eb89-4657-9f53-94c5867e7ed9"};

		ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
		ResourceRepository resourceRepository = new ResourceRepository();

		Map<UUID, Set<UUID>> exchangeBatches = new HashMap<>();

		for (String orgId: orgIds) {

			LOG.info("Doing org ID " + orgId);
			UUID orgUuid = UUID.fromString(orgId);

			try {

				//select batch_id from ehr.resource_by_exchange_batch where resource_type = 'Organization' and resource_id = 8f465517-729b-4ad9-b405-92b487047f19 LIMIT 1 ALLOW FILTERING;
				ResourceByExchangeBatch resourceByExchangeBatch = resourceRepository.getFirstResourceByExchangeBatch(ResourceType.Organization.toString(), orgUuid);
				UUID batchId = resourceByExchangeBatch.getBatchId();

				//select exchange_id from ehr.exchange_batch where batch_id = 1a940e10-1535-11e7-a29d-a90b99186399 LIMIT 1 ALLOW FILTERING;
				ExchangeBatch exchangeBatch = exchangeBatchRepository.retrieveFirstForBatchId(batchId);
				UUID exchangeId = exchangeBatch.getExchangeId();

				Set<UUID> list = exchangeBatches.get(exchangeId);
				if (list == null) {
					list = new HashSet<>();
					exchangeBatches.put(exchangeId, list);
				}
				list.add(batchId);

			} catch (Exception ex) {
				LOG.error("", ex);
				break;
			}
		}

		try {
			//find the config for our protocol queue (which is in the inbound config)
			String configXml = ConfigManager.getConfiguration("inbound", "queuereader");

			//the config XML may be one of two serialised classes, so we use a try/catch to safely try both if necessary
			QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
			Pipeline pipeline = configuration.getPipeline();

			PostMessageToExchangeConfig config = pipeline
					.getPipelineComponents()
					.stream()
					.filter(t -> t instanceof PostMessageToExchangeConfig)
					.map(t -> (PostMessageToExchangeConfig) t)
					.filter(t -> t.getExchange().equalsIgnoreCase("EdsProtocol"))
					.collect(StreamExtension.singleOrNullCollector());

			//post to the protocol exchange
			for (UUID exchangeId : exchangeBatches.keySet()) {
				Set<UUID> batchIds = exchangeBatches.get(exchangeId);

				org.endeavourhealth.core.messaging.exchange.Exchange exchange = AuditWriter.readExchange(exchangeId);

				String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIds);
				exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);
				LOG.info("Posting exchange " + exchangeId + " batch " + batchIdString);

				PostMessageToExchange component = new PostMessageToExchange(config);
				component.process(exchange);
			}

		} catch (Exception ex) {

			LOG.error("", ex);
			return;
		}


		LOG.info("Finished posting orgs to protocol queue");
	}*/

	private static void findCodes() {

		LOG.info("Finding missing codes");

		AuditRepository auditRepository = new AuditRepository();
		ServiceRepository serviceRepository = new ServiceRepository();

		Session session = CassandraConnector.getInstance().getSession();
		Statement stmt = new SimpleStatement("SELECT service_id, system_id, exchange_id, version FROM audit.exchange_transform_audit ALLOW FILTERING;");
		stmt.setFetchSize(100);

		ResultSet rs = session.execute(stmt);
		while (!rs.isExhausted()) {
			Row row = rs.one();
			UUID serviceId = row.get(0, UUID.class);
			UUID systemId = row.get(1, UUID.class);
			UUID exchangeId = row.get(2, UUID.class);
			UUID version = row.get(3, UUID.class);

			ExchangeTransformAudit audit = auditRepository.getExchangeTransformAudit(serviceId, systemId, exchangeId, version);
			String xml = audit.getErrorXml();
			if (xml == null) {
				continue;
			}

			String codePrefix = "Failed to find clinical code CodeableConcept for codeId ";
			int codeIndex = xml.indexOf(codePrefix);
			if (codeIndex > -1) {
				int startIndex = codeIndex + codePrefix.length();
				int tagEndIndex = xml.indexOf("<", startIndex);

				String code = xml.substring(startIndex, tagEndIndex);

				Service service = serviceRepository.getById(serviceId);
				String name = service.getName();

				LOG.info(name + " clinical code " + code + " from " + audit.getStarted());
				continue;
			}

			codePrefix = "Failed to find medication CodeableConcept for codeId ";
			codeIndex = xml.indexOf(codePrefix);
			if (codeIndex > -1) {
				int startIndex = codeIndex + codePrefix.length();
				int tagEndIndex = xml.indexOf("<", startIndex);

				String code = xml.substring(startIndex, tagEndIndex);
				Service service = serviceRepository.getById(serviceId);
				String name = service.getName();

				LOG.info(name + " drug code " + code + " from " + audit.getStarted());
				continue;
			}
		}

		LOG.info("Finished finding missing codes");
	}
}

/*class ResourceFiler extends FhirResourceFiler {
	public ResourceFiler(UUID exchangeId, UUID serviceId, UUID systemId, TransformError transformError,
							 List<UUID> batchIdsCreated, int maxFilingThreads) {
		super(exchangeId, serviceId, systemId, transformError, batchIdsCreated, maxFilingThreads);
	}

	private List<Resource> newResources = new ArrayList<>();

	public List<Resource> getNewResources() {
		return newResources;
	}

	@Override
	public void saveAdminResource(CsvCurrentState parserState, boolean mapIds, Resource... resources) throws Exception {
		throw new Exception("shouldn't be calling saveAdminResource");
	}

	@Override
	public void deleteAdminResource(CsvCurrentState parserState, boolean mapIds, Resource... resources) throws Exception {
		throw new Exception("shouldn't be calling deleteAdminResource");
	}

	@Override
	public void savePatientResource(CsvCurrentState parserState, boolean mapIds, String patientId, Resource... resources) throws Exception {

		for (Resource resource: resources) {
			if (mapIds) {
				IdHelper.mapIds(getServiceId(), getSystemId(), resource);
			}
			newResources.add(resource);
		}
	}

	@Override
	public void deletePatientResource(CsvCurrentState parserState, boolean mapIds, String patientId, Resource... resources) throws Exception {
		throw new Exception("shouldn't be calling deletePatientResource");
	}
}*/
