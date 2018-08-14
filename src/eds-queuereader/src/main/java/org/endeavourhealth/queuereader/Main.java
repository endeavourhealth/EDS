package org.endeavourhealth.queuereader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.csv.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.configuration.ConfigDeserialiser;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.csv.CsvHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.EnterpriseIdDalI;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.exceptions.TransformException;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.fhirStorage.FhirStorageService;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.helpers.EmisCsvHelper;
import org.hibernate.internal.SessionImpl;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		String configId = args[0];

		LOG.info("Initialising config manager");
		ConfigManager.initialize("queuereader", configId);

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEncounters")) {
			String table = args[1];
			fixEncounters(table);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateHomertonSubset")) {
			String sourceDirPath = args[1];
			String destDirPath = args[2];
			String samplePatientsFile = args[3];
			createHomertonSubset(sourceDirPath, destDirPath, samplePatientsFile);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateAdastraSubset")) {
			String sourceDirPath = args[1];
			String destDirPath = args[2];
			String samplePatientsFile = args[3];
			createAdastraSubset(sourceDirPath, destDirPath, samplePatientsFile);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateVisionSubset")) {
			String sourceDirPath = args[1];
			String destDirPath = args[2];
			String samplePatientsFile = args[3];
			createVisionSubset(sourceDirPath, destDirPath, samplePatientsFile);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateTppSubset")) {
			String sourceDirPath = args[1];
			String destDirPath = args[2];
			String samplePatientsFile = args[3];
			createTppSubset(sourceDirPath, destDirPath, samplePatientsFile);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateBartsSubset")) {
			String sourceDirPath = args[1];
			UUID serviceUuid = UUID.fromString(args[2]);
			UUID systemUuid = UUID.fromString(args[3]);
			String samplePatientsFile = args[4];
			createBartsSubset(sourceDirPath, serviceUuid, systemUuid, samplePatientsFile);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixBartsOrgs")) {
			String serviceId = args[1];
			fixBartsOrgs(serviceId);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestPreparedStatements")) {
			String url = args[1];
			String user = args[2];
			String pass = args[3];
			String serviceId = args[4];
			testPreparedStatements(url, user, pass, serviceId);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("ExportFhirToCsv")) {
			UUID serviceId = UUID.fromString(args[1]);
			String path = args[2];
			exportFhirToCsv(serviceId, path);
			System.exit(0);
		}


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestBatchInserts")) {
			String url = args[1];
			String user = args[2];
			String pass = args[3];
			String num = args[4];
			String batchSize = args[5];
			testBatchInserts(url, user, pass, num, batchSize);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("ApplyEmisAdminCaches")) {
			applyEmisAdminCaches();
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixSubscribers")) {
			fixSubscriberDbs();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEmisProblems")) {
			String serviceId = args[1];
			String systemId = args[2];
			fixEmisProblems(UUID.fromString(serviceId), UUID.fromString(systemId));
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEmisProblems3ForPublisher")) {
			String publisherId = args[1];
			String systemId = args[2];
			fixEmisProblems3ForPublisher(publisherId, UUID.fromString(systemId));
			System.exit(0);
		}


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEmisProblems3")) {
			String serviceId = args[1];
			String systemId = args[2];
			fixEmisProblems3(UUID.fromString(serviceId), UUID.fromString(systemId));
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CheckDeletedObs")) {
			String serviceId = args[1];
			String systemId = args[2];
			checkDeletedObs(UUID.fromString(serviceId), UUID.fromString(systemId));
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixPersonsNoNhsNumber")) {
			fixPersonsNoNhsNumber();
			System.exit(0);
		}


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ConvertExchangeBody")) {
			String systemId = args[1];
			convertExchangeBody(UUID.fromString(systemId));
			System.exit(0);
		}*/


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixReferrals")) {
			fixReferralRequests();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateNewSearchTable")) {
			String table = args[1];
			populateNewSearchTable(table);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixBartsEscapes")) {
			String filePath = args[1];
			fixBartsEscapedFiles(filePath);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PostToInbound")) {
			String serviceId = args[1];
			String systemId = args[2];
			String filePath = args[3];
			postToInboundFromFile(UUID.fromString(serviceId), UUID.fromString(systemId), filePath);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixDisabledExtract")) {
			String serviceId = args[1];
			String systemId = args[2];
			String sharedStoragePath = args[3];
			String tempDir = args[4];
			fixDisabledEmisExtract(serviceId, systemId, sharedStoragePath, tempDir);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestSlack")) {
			testSlack();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PostToInbound")) {
			String serviceId = args[1];
			boolean all = Boolean.parseBoolean(args[2]);
			postToInbound(UUID.fromString(serviceId), all);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixPatientSearch")) {
			String serviceId = args[1];
			fixPatientSearch(serviceId);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("Exit")) {

			String exitCode = args[1];
			LOG.info("Exiting with error code " + exitCode);
			int exitCodeInt = Integer.parseInt(exitCode);
			System.exit(exitCodeInt);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("RunSql")) {

			String host = args[1];
			String username = args[2];
			String password = args[3];
			String sqlFile = args[4];
			runSql(host, username, password, sqlFile);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateProtocolQueue")) {
			String serviceId = null;
			if (args.length > 1) {
				serviceId = args[1];
			}
			String startingExchangeId = null;
			if (args.length > 2) {
				startingExchangeId = args[2];
			}
			populateProtocolQueue(serviceId, startingExchangeId);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindEncounterTerms")) {
			String path = args[1];
			String outputPath = args[2];
			findEncounterTerms(path, outputPath);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindEmisStartDates")) {
			String path = args[1];
			String outputPath = args[2];
			findEmisStartDates(path, outputPath);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ExportHl7Encounters")) {
			String sourceCsvPpath = args[1];
			String outputPath = args[2];
			exportHl7Encounters(sourceCsvPpath, outputPath);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixExchangeBatches")) {
			fixExchangeBatches();
			System.exit(0);
		}*/

		/*if (args.length >= 0
				&& args[0].equalsIgnoreCase("FindCodes")) {
			findCodes();
			System.exit(0);
		}*/

		/*if (args.length >= 0
				&& args[0].equalsIgnoreCase("FindDeletedOrgs")) {
			findDeletedOrgs();
			System.exit(0);
		}*/

		if (args.length != 1) {
			LOG.error("Usage: queuereader config_id");
			return;
		}

		LOG.info("--------------------------------------------------");
		LOG.info("EDS Queue Reader " + configId);
		LOG.info("--------------------------------------------------");

		LOG.info("Fetching queuereader configuration");
		String configXml = ConfigManager.getConfiguration(configId);
		QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);

		/*LOG.info("Registering shutdown hook");
		registerShutdownHook();*/

		// Instantiate rabbit handler
		LOG.info("Creating EDS queue reader");
		RabbitHandler rabbitHandler = new RabbitHandler(configuration, configId);

		// Begin consume
		rabbitHandler.start();
		LOG.info("EDS Queue reader running (kill file location " + TransformConfig.instance().getKillFileLocation() + ")");
	}

	private static void fixPersonsNoNhsNumber() {
		LOG.info("Fixing persons with no NHS number");
		try {

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			List<Service> services = serviceDal.getAll();

			EntityManager entityManager = ConnectionManager.getEdsEntityManager();
			SessionImpl session = (SessionImpl)entityManager.getDelegate();
			Connection patientSearchConnection = session.connection();
			Statement patientSearchStatement = patientSearchConnection.createStatement();

			for (Service service: services) {
				LOG.info("Doing " + service.getName() + " " + service.getId());

				int checked = 0;
				int fixedPersons = 0;
				int fixedSearches = 0;

				String sql = "SELECT patient_id, nhs_number FROM patient_search WHERE service_id = '" + service.getId() + "' AND (nhs_number IS NULL or CHAR_LENGTH(nhs_number) != 10)";
				ResultSet rs = patientSearchStatement.executeQuery(sql);

				while (rs.next()) {
					String patientId = rs.getString(1);
					String nhsNumber = rs.getString(2);

					//find matched person ID
					String personIdSql = "SELECT person_id FROM patient_link WHERE patient_id = '" + patientId + "'";
					Statement s = patientSearchConnection.createStatement();
					ResultSet rsPersonId = s.executeQuery(personIdSql);
					String personId = null;
					if (rsPersonId.next()) {
						personId = rsPersonId.getString(1);
					}
					rsPersonId.close();
					s.close();
					if (Strings.isNullOrEmpty(personId)) {
						LOG.error("Patient " + patientId + " has no person ID");
						continue;
					}

					//see whether person ID used NHS number to match
					String patientLinkSql = "SELECT nhs_number FROM patient_link_person WHERE person_id = '" + personId + "'";
					s = patientSearchConnection.createStatement();
					ResultSet rsPatientLink = s.executeQuery(patientLinkSql);
					String matchingNhsNumber = null;
					if (rsPatientLink.next()) {
						matchingNhsNumber = rsPatientLink.getString(1);
					}
					rsPatientLink.close();
					s.close();

					//if patient link person has a record for this nhs number, update the person link
					if (!Strings.isNullOrEmpty(matchingNhsNumber)) {
						String newPersonId = UUID.randomUUID().toString();

						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String createdAtStr = sdf.format(new Date());


						s = patientSearchConnection.createStatement();

						//new record in patient link history
						String patientHistorySql = "INSERT INTO patient_link_history VALUES ('" + patientId + "', '" + service.getId() + "', '" + createdAtStr + "', '" + newPersonId + "', '" + personId + "')";
						//LOG.debug(patientHistorySql);
						s.execute(patientHistorySql);

						//update patient link
						String patientLinkUpdateSql = "UPDATE patient_link SET person_id = '" + newPersonId + "' WHERE patient_id = '" + patientId + "'";
						s.execute(patientLinkUpdateSql);

						patientSearchConnection.commit();
						s.close();

						fixedPersons ++;
					}

					//if patient search has an invalid NHS number, update it
					if (!Strings.isNullOrEmpty(nhsNumber)) {
						ResourceDalI resourceDal = DalProvider.factoryResourceDal();
						Patient patient = (Patient)resourceDal.getCurrentVersionAsResource(service.getId(), ResourceType.Patient, patientId);

						PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
						patientSearchDal.update(service.getId(), patient);

						fixedSearches ++;
					}

					checked ++;
					if (checked % 50 == 0) {
						LOG.info("Checked " + checked + ", FixedPersons = " + fixedPersons + ", FixedSearches = " + fixedSearches);
					}
				}

				LOG.info("Checked " + checked + ", FixedPersons = " + fixedPersons + ", FixedSearches = " + fixedSearches);

				rs.close();
			}

			patientSearchStatement.close();
			entityManager.close();

			LOG.info("Finished fixing persons with no NHS number");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void checkDeletedObs(UUID serviceId, UUID systemId) {
		LOG.info("Checking Observations for " + serviceId);
		try {
			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();

			List<ResourceType> potentialResourceTypes = new ArrayList<>();
			potentialResourceTypes.add(ResourceType.Procedure);
			potentialResourceTypes.add(ResourceType.AllergyIntolerance);
			potentialResourceTypes.add(ResourceType.FamilyMemberHistory);
			potentialResourceTypes.add(ResourceType.Immunization);
			potentialResourceTypes.add(ResourceType.DiagnosticOrder);
			potentialResourceTypes.add(ResourceType.Specimen);
			potentialResourceTypes.add(ResourceType.DiagnosticReport);
			potentialResourceTypes.add(ResourceType.ReferralRequest);
			potentialResourceTypes.add(ResourceType.Condition);
			potentialResourceTypes.add(ResourceType.Observation);

			List<String> subscriberConfigs = new ArrayList<>();
			subscriberConfigs.add("ceg_data_checking");
			subscriberConfigs.add("ceg_enterprise");
			subscriberConfigs.add("hurley_data_checking");
			subscriberConfigs.add("hurley_deidentified");


			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			for (Exchange exchange : exchanges) {
				List<ExchangePayloadFile> payload = ExchangeHelper.parseExchangeBody(exchange.getBody());
				//String version = EmisCsvToFhirTransformer.determineVersion(payload);

				//if we've reached the point before we process data for this practice, break out
				if (!EmisCsvToFhirTransformer.shouldProcessPatientData(payload)) {
					break;
				}

				ExchangePayloadFile firstItem = payload.get(0);
				String name = FilenameUtils.getBaseName(firstItem.getPath());
				String[] toks = name.split("_");
				String agreementId = toks[4];

				LOG.info("Doing exchange containing " + firstItem.getPath());

				EmisCsvHelper csvHelper = new EmisCsvHelper(serviceId, systemId, exchange.getId(), agreementId, true);

				Map<UUID, ExchangeBatch> hmBatchesByPatient = new HashMap<>();
				List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(exchange.getId());
				for (ExchangeBatch batch : batches) {
					if (batch.getEdsPatientId() != null) {
						hmBatchesByPatient.put(batch.getEdsPatientId(), batch);
					}
				}

				for (ExchangePayloadFile item : payload) {
					String type = item.getType();
					if (type.equals("CareRecord_Observation")) {
						InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(item.getPath());
						CSVParser parser = new CSVParser(isr, EmisCsvToFhirTransformer.CSV_FORMAT);
						Iterator<CSVRecord> iterator = parser.iterator();
						while (iterator.hasNext()) {
							CSVRecord record = iterator.next();

							String deleted = record.get("Deleted");
							if (deleted.equalsIgnoreCase("true")) {

								String patientId = record.get("PatientGuid");
								String observationId = record.get("ObservationGuid");

								//find observation UUID
								UUID uuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Observation, patientId + ":" + observationId);
								if (uuid == null) {
									continue;
								}

								//find TRUE resource type
								Reference edsReference = ReferenceHelper.createReference(ResourceType.Observation, uuid.toString());

								if (fixReference(serviceId, csvHelper, edsReference, potentialResourceTypes)) {
									ReferenceComponents comps = ReferenceHelper.getReferenceComponents(edsReference);
									ResourceType newType = comps.getResourceType();
									String newId = comps.getId();

									//delete resource if not already done
									ResourceWrapper resourceWrapper = resourceDal.getCurrentVersion(serviceId, newType.toString(), UUID.fromString(newId));
									if (resourceWrapper.isDeleted()) {
										continue;
									}

									LOG.debug("Fixing " + edsReference.getReference());

									//create file of IDs to delete for each subscriber DB
									for (String subscriberConfig : subscriberConfigs) {
										EnterpriseIdDalI subscriberDal = DalProvider.factoryEnterpriseIdDal(subscriberConfig);
										Long enterpriseId = subscriberDal.findEnterpriseId(newType.toString(), newId);
										if (enterpriseId == null) {
											continue;
										}

										String sql = null;
										if (newType == ResourceType.AllergyIntolerance) {
											sql = "DELETE FROM allergy_intolerance WHERE id = " + enterpriseId;

										} else if (newType == ResourceType.ReferralRequest) {
											sql = "DELETE FROM referral_request WHERE id = " + enterpriseId;

										} else {
											sql = "DELETE FROM observation WHERE id = " + enterpriseId;
										}
										sql += "\n";

										File f = new File(subscriberConfig + ".sql");
										Files.write(f.toPath(), sql.getBytes(), StandardOpenOption.APPEND);
									}


									ExchangeBatch batch = hmBatchesByPatient.get(resourceWrapper.getPatientId());
									resourceWrapper.setDeleted(true);
									resourceWrapper.setResourceData(null);
									resourceWrapper.setExchangeBatchId(batch.getBatchId());
									resourceWrapper.setVersion(UUID.randomUUID());
									resourceWrapper.setCreatedAt(new Date());
									resourceWrapper.setExchangeId(exchange.getId());

									resourceDal.delete(resourceWrapper);
								}
							}
						}
						parser.close();

					} else {
						//no problem link
					}
				}
			}

			LOG.info("Finished Checking Observations for " + serviceId);
		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}

	private static void testBatchInserts(String url, String user, String pass, String num, String batchSizeStr) {
		LOG.info("Testing Batch Inserts");
		try {
			int inserts = Integer.parseInt(num);
			int batchSize = Integer.parseInt(batchSizeStr);

			LOG.info("Openning Connection");
			Properties props = new Properties();
			props.setProperty("user", user);
			props.setProperty("password", pass);

			Connection conn = DriverManager.getConnection(url, props);
			//String sql = "INSERT INTO drewtest.insert_test VALUES (?, ?, ?);";
			String sql = "INSERT INTO drewtest.insert_test VALUES (?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);

			if (batchSize == 1) {

				LOG.info("Testing non-batched inserts");

				long start = System.currentTimeMillis();
				for (int i = 0; i < inserts; i++) {
					int col = 1;
					ps.setString(col++, UUID.randomUUID().toString());
					ps.setString(col++, UUID.randomUUID().toString());
					ps.setString(col++, randomStr());
					ps.execute();
				}
				long end = System.currentTimeMillis();
				LOG.info("Done " + inserts + " in " + (end - start) + " ms");

			} else {

				LOG.info("Testing batched inserts with batch size " + batchSize);

				long start = System.currentTimeMillis();
				for (int i = 0; i < inserts; i++) {
					int col = 1;
					ps.setString(col++, UUID.randomUUID().toString());
					ps.setString(col++, UUID.randomUUID().toString());
					ps.setString(col++, randomStr());
					ps.addBatch();

					if ((i + 1) % batchSize == 0
							|| i + 1 >= inserts) {
						ps.executeBatch();
					}
				}

				long end = System.currentTimeMillis();
				LOG.info("Done " + inserts + " in " + (end - start) + " ms");
			}

			ps.close();
			conn.close();
			LOG.info("Finished Testing Batch Inserts");
		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}

	private static String randomStr() {
		StringBuffer sb = new StringBuffer();
		Random r = new Random(System.currentTimeMillis());
		while (sb.length() < 1100) {
			sb.append(r.nextLong());
		}
		return sb.toString();
	}

	/*private static void fixEmisProblems(UUID serviceId, UUID systemId) {
		LOG.info("Fixing Emis Problems for " + serviceId);
		try {
			Map<String, List<String>> hmReferences = new HashMap<>();
			Set<String> patientIds = new HashSet<>();

			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			FhirResourceFiler filer = new FhirResourceFiler(null, serviceId, systemId, null, null);

			LOG.info("Caching problem links");

			//Go through all files to work out problem children for every problem
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			for (int i=exchanges.size()-1; i>=0; i--) {
				Exchange exchange = exchanges.get(i);
				List<ExchangePayloadFile> payload = ExchangeHelper.parseExchangeBody(exchange.getBody());
				//String version = EmisCsvToFhirTransformer.determineVersion(payload);

				ExchangePayloadFile firstItem = payload.get(0);
				String name = FilenameUtils.getBaseName(firstItem.getPath());
				String[] toks = name.split("_");
				String agreementId = toks[4];

				LOG.info("Doing exchange containing " + firstItem.getPath());

				EmisCsvHelper csvHelper = new EmisCsvHelper(serviceId, systemId, exchange.getId(), agreementId, true);

				for (ExchangePayloadFile item: payload) {
					String type = item.getType();
					if (type.equals("CareRecord_Observation")) {
						InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(item.getPath());
						CSVParser parser = new CSVParser(isr, EmisCsvToFhirTransformer.CSV_FORMAT);
						Iterator<CSVRecord> iterator = parser.iterator();
						while (iterator.hasNext()) {
							CSVRecord record = iterator.next();

							String parentProblemId = record.get("ProblemGuid");
							String patientId = record.get("PatientGuid");
							patientIds.add(patientId);

							if (!Strings.isNullOrEmpty(parentProblemId)) {

								String observationId = record.get("ObservationGuid");
								String localId = patientId + ":" + observationId;
								ResourceType resourceType = ObservationTransformer.findOriginalTargetResourceType(filer, CsvCell.factoryDummyWrapper(patientId), CsvCell.factoryDummyWrapper(observationId));

								Reference localReference = ReferenceHelper.createReference(resourceType, localId);
								Reference globalReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(localReference, csvHelper);

								String localProblemId = patientId + ":" + parentProblemId;
								Reference localProblemReference = ReferenceHelper.createReference(ResourceType.Condition, localProblemId);
								Reference globalProblemReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(localProblemReference, csvHelper);

								String globalProblemId = ReferenceHelper.getReferenceId(globalProblemReference);
								List<String> problemChildren = hmReferences.get(globalProblemId);
								if (problemChildren == null) {
									problemChildren = new ArrayList<>();
									hmReferences.put(globalProblemId, problemChildren);
								}
								problemChildren.add(globalReference.getReference());
							}
						}
						parser.close();

					} else if (type.equals("Prescribing_DrugRecord")) {
						InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(item.getPath());
						CSVParser parser = new CSVParser(isr, EmisCsvToFhirTransformer.CSV_FORMAT);
						Iterator<CSVRecord> iterator = parser.iterator();
						while (iterator.hasNext()) {
							CSVRecord record = iterator.next();

							String parentProblemId = record.get("ProblemObservationGuid");
							String patientId = record.get("PatientGuid");
							patientIds.add(patientId);

							if (!Strings.isNullOrEmpty(parentProblemId)) {
								String observationId = record.get("DrugRecordGuid");
								String localId = patientId + ":" + observationId;
								Reference localReference = ReferenceHelper.createReference(ResourceType.MedicationStatement, localId);
								Reference globalReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(localReference, csvHelper);

								String localProblemId = patientId + ":" + parentProblemId;
								Reference localProblemReference = ReferenceHelper.createReference(ResourceType.Condition, localProblemId);
								Reference globalProblemReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(localProblemReference, csvHelper);

								String globalProblemId = ReferenceHelper.getReferenceId(globalProblemReference);
								List<String> problemChildren = hmReferences.get(globalProblemId);
								if (problemChildren == null) {
									problemChildren = new ArrayList<>();
									hmReferences.put(globalProblemId, problemChildren);
								}
								problemChildren.add(globalReference.getReference());
							}
						}
						parser.close();

					} else if (type.equals("Prescribing_IssueRecord")) {
						InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(item.getPath());
						CSVParser parser = new CSVParser(isr, EmisCsvToFhirTransformer.CSV_FORMAT);
						Iterator<CSVRecord> iterator = parser.iterator();
						while (iterator.hasNext()) {
							CSVRecord record = iterator.next();

							String parentProblemId = record.get("ProblemObservationGuid");
							String patientId = record.get("PatientGuid");
							patientIds.add(patientId);

							if (!Strings.isNullOrEmpty(parentProblemId)) {
								String observationId = record.get("IssueRecordGuid");
								String localId = patientId + ":" + observationId;
								Reference localReference = ReferenceHelper.createReference(ResourceType.MedicationOrder, localId);

								String localProblemId = patientId + ":" + parentProblemId;
								Reference localProblemReference = ReferenceHelper.createReference(ResourceType.Condition, localProblemId);
								Reference globalProblemReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(localProblemReference, csvHelper);
								Reference globalReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(localReference, csvHelper);

								String globalProblemId = ReferenceHelper.getReferenceId(globalProblemReference);
								List<String> problemChildren = hmReferences.get(globalProblemId);
								if (problemChildren == null) {
									problemChildren = new ArrayList<>();
									hmReferences.put(globalProblemId, problemChildren);
								}
								problemChildren.add(globalReference.getReference());
							}
						}
						parser.close();

					} else {
						//no problem link
					}
				}
			}

			LOG.info("Finished caching problem links, finding " + patientIds.size() + " patients");

			int done = 0;
			int fixed = 0;
			for (String localPatientId: patientIds) {

				Reference localPatientReference = ReferenceHelper.createReference(ResourceType.Patient, localPatientId);
				Reference globalPatientReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(localPatientReference, filer);
				String patientUuid = ReferenceHelper.getReferenceId(globalPatientReference);

				List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, UUID.fromString(patientUuid), ResourceType.Condition.toString());
				for (ResourceWrapper wrapper: wrappers) {
					if (wrapper.isDeleted()) {
						continue;
					}

					String originalJson = wrapper.getResourceData();
					Condition condition = (Condition)FhirSerializationHelper.deserializeResource(originalJson);
					ConditionBuilder conditionBuilder = new ConditionBuilder(condition);

					//sort out the nested extension references
					Extension outerExtension = ExtensionConverter.findExtension(condition, FhirExtensionUri.PROBLEM_LAST_REVIEWED);
					if (outerExtension != null) {
						Extension innerExtension = ExtensionConverter.findExtension(outerExtension, FhirExtensionUri._PROBLEM_LAST_REVIEWED__PERFORMER);
						if (innerExtension != null) {
							Reference performerReference = (Reference)innerExtension.getValue();
							String value = performerReference.getReference();
							if (value.endsWith("}")) {

								Reference globalPerformerReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(performerReference, filer);
								innerExtension.setValue(globalPerformerReference);
							}
						}
					}

					//sort out the contained list of children
					ContainedListBuilder listBuilder = new ContainedListBuilder(conditionBuilder);

					//remove any existing children
					listBuilder.removeContainedList();

					//add all the new ones we've found
					List<String> localChildReferences = hmReferences.get(wrapper.getResourceId().toString());
					if (localChildReferences != null) {
						for (String localChildReference: localChildReferences) {
							Reference reference = ReferenceHelper.createReference(localChildReference);
							listBuilder.addContainedListItem(reference);
						}
					}

					//save the updated condition
					String newJson = FhirSerializationHelper.serializeResource(condition);
					if (!newJson.equals(originalJson)) {

						wrapper.setResourceData(newJson);
						saveResourceWrapper(serviceId, wrapper);
						fixed ++;
					}
				}


				done ++;
				if (done % 1000 == 0) {
					LOG.info("Done " + done + " patients and fixed " + fixed + " problems");
				}
			}
			LOG.info("Done " + done + " patients and fixed " + fixed + " problems");



			LOG.info("Finished Emis Problems for " + serviceId);
		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/


	private static void fixEmisProblems3ForPublisher(String publisher, UUID systemId) {
		try {
			LOG.info("Doing fix for " + publisher);

			ServiceDalI dal = DalProvider.factoryServiceDal();
			List<Service> all = dal.getAll();
			for (Service service: all) {
				if (service.getPublisherConfigName() != null
						&& service.getPublisherConfigName().equals(publisher)) {

					fixEmisProblems3(service.getId(), systemId);
				}
			}

			LOG.info("Done fix for " + publisher);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void fixEmisProblems3(UUID serviceId, UUID systemId) {
		LOG.info("Fixing Emis Problems 3 for " + serviceId);
		try {
			Set<String> patientIds = new HashSet<>();

			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			FhirResourceFiler filer = new FhirResourceFiler(null, serviceId, systemId, null, null);

			LOG.info("Finding patients");

			//Go through all files to work out problem children for every problem
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			for (int i=exchanges.size()-1; i>=0; i--) {
				Exchange exchange = exchanges.get(i);
				List<ExchangePayloadFile> payload = ExchangeHelper.parseExchangeBody(exchange.getBody());

				for (ExchangePayloadFile item: payload) {
					String type = item.getType();
					if (type.equals("Admin_Patient")) {
						InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(item.getPath());
						CSVParser parser = new CSVParser(isr, EmisCsvToFhirTransformer.CSV_FORMAT);
						Iterator<CSVRecord> iterator = parser.iterator();
						while (iterator.hasNext()) {
							CSVRecord record = iterator.next();
							String patientId = record.get("PatientGuid");
							patientIds.add(patientId);
						}
						parser.close();
					}
				}
			}

			LOG.info("Finished checking files, finding " + patientIds.size() + " patients");

			int done = 0;
			int fixed = 0;
			for (String localPatientId: patientIds) {

				Reference localPatientReference = ReferenceHelper.createReference(ResourceType.Patient, localPatientId);
				Reference globalPatientReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(localPatientReference, filer);
				String patientUuid = ReferenceHelper.getReferenceId(globalPatientReference);

				List<ResourceType> potentialResourceTypes = new ArrayList<>();
				potentialResourceTypes.add(ResourceType.Procedure);
				potentialResourceTypes.add(ResourceType.AllergyIntolerance);
				potentialResourceTypes.add(ResourceType.FamilyMemberHistory);
				potentialResourceTypes.add(ResourceType.Immunization);
				potentialResourceTypes.add(ResourceType.DiagnosticOrder);
				potentialResourceTypes.add(ResourceType.Specimen);
				potentialResourceTypes.add(ResourceType.DiagnosticReport);
				potentialResourceTypes.add(ResourceType.ReferralRequest);
				potentialResourceTypes.add(ResourceType.Condition);
				potentialResourceTypes.add(ResourceType.Observation);

				for (ResourceType resourceType: potentialResourceTypes) {

					List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, UUID.fromString(patientUuid), resourceType.toString());
					for (ResourceWrapper wrapper : wrappers) {
						if (wrapper.isDeleted()) {
							continue;
						}

						String originalJson = wrapper.getResourceData();
						DomainResource resource = (DomainResource)FhirSerializationHelper.deserializeResource(originalJson);

						//Also go through all observation records and any that have parent observations - these need fixing too???
						Extension extension = ExtensionConverter.findExtension(resource, FhirExtensionUri.PARENT_RESOURCE);
						if (extension != null) {
							Reference reference = (Reference)extension.getValue();
							fixReference(serviceId, filer, reference, potentialResourceTypes);
						}

						if (resource instanceof Observation) {
							Observation obs = (Observation)resource;
							if (obs.hasRelated()) {
								for (Observation.ObservationRelatedComponent related: obs.getRelated()) {
									if (related.hasTarget()) {
										Reference reference = related.getTarget();
										fixReference(serviceId, filer, reference, potentialResourceTypes);
									}
								}
							}
						}

						if (resource instanceof DiagnosticReport) {
							DiagnosticReport diag = (DiagnosticReport)resource;
							if (diag.hasResult()) {
								for (Reference reference: diag.getResult()) {
									fixReference(serviceId, filer, reference, potentialResourceTypes);
								}
							}
						}

						//Go through all patients, go through all problems, for any child that's Observation, find the true resource type then update and save
						if (resource instanceof Condition) {
							if (resource.hasContained()) {
								for (Resource contained: resource.getContained()) {
									if (contained.getId().equals("Items")) {
										List_ containedList = (List_)contained;
										if (containedList.hasEntry()) {

											for (List_.ListEntryComponent entry: containedList.getEntry()) {
												Reference reference = entry.getItem();
												fixReference(serviceId, filer, reference, potentialResourceTypes);
											}
										}
									}
								}
							}

							//sort out the nested extension references
							Extension outerExtension = ExtensionConverter.findExtension(resource, FhirExtensionUri.PROBLEM_RELATED);
							if (outerExtension != null) {
								Extension innerExtension = ExtensionConverter.findExtension(outerExtension, FhirExtensionUri._PROBLEM_RELATED__TARGET);
								if (innerExtension != null) {
									Reference performerReference = (Reference)innerExtension.getValue();
									String value = performerReference.getReference();
									if (value.endsWith("}")) {

										Reference globalPerformerReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(performerReference, filer);
										innerExtension.setValue(globalPerformerReference);
									}
								}
							}
						}

						//save the updated condition
						String newJson = FhirSerializationHelper.serializeResource(resource);
						if (!newJson.equals(originalJson)) {

							wrapper.setResourceData(newJson);
							saveResourceWrapper(serviceId, wrapper);
							fixed++;
						}
					}
				}

				done ++;
				if (done % 1000 == 0) {
					LOG.info("Done " + done + " patients and fixed " + fixed + " problems");
				}
			}
			LOG.info("Done " + done + " patients and fixed " + fixed + " problems");

			LOG.info("Finished Emis Problems 3 for " + serviceId);
		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}

	private static boolean fixReference(UUID serviceId, HasServiceSystemAndExchangeIdI csvHelper, Reference reference, List<ResourceType> potentialResourceTypes) throws Exception {

		//if it's already something other than observation, we're OK
		ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
		if (comps.getResourceType() != ResourceType.Observation) {
			return false;
		}

		Reference sourceReference = IdHelper.convertEdsReferenceToLocallyUniqueReference(csvHelper, reference);
		String sourceId = ReferenceHelper.getReferenceId(sourceReference);

		String newReferenceValue = findTrueResourceType(serviceId, potentialResourceTypes, sourceId);
		if (newReferenceValue == null) {
			return false;
		}

		reference.setReference(newReferenceValue);
		return true;
	}

	private static String findTrueResourceType(UUID serviceId, List<ResourceType> potentials, String sourceId) throws Exception {

		ResourceDalI dal = DalProvider.factoryResourceDal();
		for (ResourceType resourceType: potentials) {

			UUID uuid = IdHelper.getEdsResourceId(serviceId, resourceType, sourceId);
			if (uuid == null) {
				continue;
			}

			ResourceWrapper wrapper = dal.getCurrentVersion(serviceId, resourceType.toString(), uuid);
			if (wrapper != null) {
				return ReferenceHelper.createResourceReference(resourceType, uuid.toString());
			}
		}

		return null;
	}

	/*private static void convertExchangeBody(UUID systemUuid) {
		try {
			LOG.info("Converting exchange bodies for system " + systemUuid);

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

			List<Service> services = serviceDal.getAll();
			for (Service service: services) {

				List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemUuid, Integer.MAX_VALUE);
				if (exchanges.isEmpty()) {
					continue;
				}

				LOG.debug("doing " + service.getName() + " with " + exchanges.size() + " exchanges");

				for (Exchange exchange: exchanges) {

					String exchangeBody = exchange.getBody();
					try {
						//already done
						ExchangePayloadFile[] files = JsonSerializer.deserialize(exchangeBody, ExchangePayloadFile[].class);
						continue;
					} catch (JsonSyntaxException ex) {
						//if the JSON can't be parsed, then it'll be the old format of body that isn't JSON
					}

					List<ExchangePayloadFile> newFiles = new ArrayList<>();

					String[] files = ExchangeHelper.parseExchangeBodyOldWay(exchangeBody);
					for (String file: files) {
						ExchangePayloadFile fileObj = new ExchangePayloadFile();

						String fileWithoutSharedStorage = file.substring(TransformConfig.instance().getSharedStoragePath().length()+1);
						fileObj.setPath(fileWithoutSharedStorage);

						//size
						List<FileInfo> fileInfos = FileHelper.listFilesInSharedStorageWithInfo(file);
						for (FileInfo info: fileInfos) {
							if (info.getFilePath().equals(file)) {
								long size = info.getSize();
								fileObj.setSize(new Long(size));
							}
						}

						//type
						if (systemUuid.toString().equalsIgnoreCase("991a9068-01d3-4ff2-86ed-249bd0541fb3") //live
								|| systemUuid.toString().equalsIgnoreCase("55c08fa5-ef1e-4e94-aadc-e3d6adc80774")) { //dev
							//emis
							String name = FilenameUtils.getName(file);
							String[] toks = name.split("_");

							String first = toks[1];
							String second = toks[2];
							fileObj.setType(first + "_" + second);

*//*						} else if (systemUuid.toString().equalsIgnoreCase("e517fa69-348a-45e9-a113-d9b59ad13095")
							|| systemUuid.toString().equalsIgnoreCase("b0277098-0b6c-4d9d-86ef-5f399fb25f34")) { //dev

							//cerner
							String name = FilenameUtils.getName(file);
							if (Strings.isNullOrEmpty(name)) {
								continue;
							}
							try {
								String type = BartsCsvToFhirTransformer.identifyFileType(name);
								fileObj.setType(type);
							} catch (Exception ex2) {
								throw new Exception("Failed to parse file name " + name + " on exchange " + exchange.getId());
							}*//*

						} else {
							throw new Exception("Unknown system ID " + systemUuid);
						}

						newFiles.add(fileObj);
					}

					String json = JsonSerializer.serialize(newFiles);
					exchange.setBody(json);

					exchangeDal.save(exchange);
				}
			}

			LOG.info("Finished Converting exchange bodies for system " + systemUuid);
		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static void fixBartsOrgs(String serviceId) {
		try {
			LOG.info("Fixing Barts orgs");

			ResourceDalI dal = DalProvider.factoryResourceDal();
			List<ResourceWrapper> wrappers = dal.getResourcesByService(UUID.fromString(serviceId), ResourceType.Organization.toString());
			LOG.debug("Found " + wrappers.size() + " resources");
			int done = 0;
			int fixed = 0;
			for (ResourceWrapper wrapper: wrappers) {

				if (!wrapper.isDeleted()) {

					List<ResourceWrapper> history = dal.getResourceHistory(UUID.fromString(serviceId), wrapper.getResourceType(), wrapper.getResourceId());
					ResourceWrapper mostRecent = history.get(0);

					String json = mostRecent.getResourceData();
					Organization org = (Organization)FhirSerializationHelper.deserializeResource(json);

					String odsCode = IdentifierHelper.findOdsCode(org);
					if (Strings.isNullOrEmpty(odsCode)
							&& org.hasIdentifier()) {

						boolean hasBeenFixed = false;

						for (Identifier identifier: org.getIdentifier()) {
							if (identifier.getSystem().equals(FhirIdentifierUri.IDENTIFIER_SYSTEM_ODS_CODE)
									&& identifier.hasId()) {

								odsCode = identifier.getId();
								identifier.setValue(odsCode);
								identifier.setId(null);
								hasBeenFixed = true;
							}
						}

						if (hasBeenFixed) {
							String newJson = FhirSerializationHelper.serializeResource(org);
							mostRecent.setResourceData(newJson);

							LOG.debug("Fixed Organization " + org.getId());
							*//*LOG.debug(json);
							LOG.debug(newJson);*//*

							saveResourceWrapper(UUID.fromString(serviceId), mostRecent);

							fixed ++;
						}
					}

				}

				done ++;
				if (done % 100 == 0) {
					LOG.debug("Done " + done + ", Fixed " + fixed);
				}
			}
			LOG.debug("Done " + done + ", Fixed " + fixed);

			LOG.info("Finished Barts orgs");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void testPreparedStatements(String url, String user, String pass, String serviceId) {
		try {
			LOG.info("Testing Prepared Statements");
			LOG.info("Url: " + url);
			LOG.info("user: " + user);
			LOG.info("pass: " + pass);

			//open connection
			Class.forName("com.mysql.cj.jdbc.Driver");

			//create connection
			Properties props = new Properties();
			props.setProperty("user", user);
			props.setProperty("password", pass);

			Connection conn = DriverManager.getConnection(url, props);

			String sql = "SELECT * FROM internal_id_map WHERE service_id = ? AND id_type = ? AND source_id = ?";

			long start = System.currentTimeMillis();

			for (int i=0; i<10000; i++) {

				PreparedStatement ps = null;
				try {
					ps = conn.prepareStatement(sql);
					ps.setString(1, serviceId);
					ps.setString(2, "MILLPERSIDtoMRN");
					ps.setString(3, UUID.randomUUID().toString());

					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						//do nothing
					}

				} finally {
					if (ps != null) {
						ps.close();
					}
				}
			}

			long end = System.currentTimeMillis();
			LOG.info("Took " + (end-start) + " ms");

			//close connection
			conn.close();

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static void fixEncounters(String table) {
		LOG.info("Fixing encounters from " + table);

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date cutoff = sdf.parse("2018-03-14 11:42");

			EntityManager entityManager = ConnectionManager.getAdminEntityManager();
			SessionImpl session = (SessionImpl)entityManager.getDelegate();
			Connection connection = session.connection();
			Statement statement = connection.createStatement();

			List<UUID> serviceIds = new ArrayList<>();
			Map<UUID, UUID> hmSystems = new HashMap<>();

			String sql = "SELECT service_id, system_id FROM " + table + " WHERE done = 0";
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				UUID serviceId = UUID.fromString(rs.getString(1));
				UUID systemId = UUID.fromString(rs.getString(2));
				serviceIds.add(serviceId);
				hmSystems.put(serviceId, systemId);
			}
			rs.close();
			statement.close();
			entityManager.close();

			for (UUID serviceId: serviceIds) {
				UUID systemId = hmSystems.get(serviceId);
				LOG.info("Doing service " + serviceId + " and system " + systemId);

				ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
				List<UUID> exchangeIds = exchangeDal.getExchangeIdsForService(serviceId, systemId);

				List<UUID> exchangeIdsToProcess = new ArrayList<>();

				for (UUID exchangeId: exchangeIds) {

					List<ExchangeTransformAudit> audits = exchangeDal.getAllExchangeTransformAudits(serviceId, systemId, exchangeId);
					for (ExchangeTransformAudit audit: audits) {
						Date d = audit.getStarted();
						if (d.after(cutoff)) {
							exchangeIdsToProcess.add(exchangeId);
							break;
						}
					}
				}

				Map<String, ReferenceList> consultationNewChildMap = new HashMap<>();
				Map<String, ReferenceList> observationChildMap = new HashMap<>();
				Map<String, ReferenceList> newProblemChildren = new HashMap<>();

				for (UUID exchangeId: exchangeIdsToProcess) {
					Exchange exchange = exchangeDal.getExchange(exchangeId);

					String[] files = ExchangeHelper.parseExchangeBodyIntoFileList(exchange.getBody());
					String version = EmisCsvToFhirTransformer.determineVersion(files);

					List<String> interestingFiles = new ArrayList<>();
					for (String file: files) {
						if (file.indexOf("CareRecord_Consultation") > -1
								|| file.indexOf("CareRecord_Observation") > -1
								|| file.indexOf("CareRecord_Diary") > -1
								|| file.indexOf("Prescribing_DrugRecord") > -1
								|| file.indexOf("Prescribing_IssueRecord") > -1
								|| file.indexOf("CareRecord_Problem") > -1) {
							interestingFiles.add(file);
						}
					}
					files = interestingFiles.toArray(new String[0]);

					Map<Class, AbstractCsvParser> parsers = new HashMap<>();
					EmisCsvToFhirTransformer.createParsers(serviceId, systemId, exchangeId, files, version, parsers);

					String dataSharingAgreementGuid = EmisCsvToFhirTransformer.findDataSharingAgreementGuid(parsers);
					EmisCsvHelper csvHelper = new EmisCsvHelper(serviceId, systemId, exchangeId, dataSharingAgreementGuid, true);


					Consultation consultationParser = (Consultation)parsers.get(Consultation.class);
					while (consultationParser.nextRecord()) {
						CsvCell consultationGuid = consultationParser.getConsultationGuid();
						CsvCell patientGuid = consultationParser.getPatientGuid();
						String sourceId = EmisCsvHelper.createUniqueId(patientGuid, consultationGuid);
						consultationNewChildMap.put(sourceId, new ReferenceList());
					}

					Problem problemParser = (Problem)parsers.get(Problem.class);
					while (problemParser.nextRecord()) {
						CsvCell problemGuid = problemParser.getObservationGuid();
						CsvCell patientGuid = problemParser.getPatientGuid();
						String sourceId = EmisCsvHelper.createUniqueId(patientGuid, problemGuid);
						newProblemChildren.put(sourceId, new ReferenceList());
					}

					//run this pre-transformer to pre-cache some stuff in the csv helper, which
					//is needed when working out the resource type that each observation would be saved as
					ObservationPreTransformer.transform(version, parsers, null, csvHelper);

					org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation observationParser = (org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation)parsers.get(org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation.class);

					while (observationParser.nextRecord()) {
						CsvCell observationGuid = observationParser.getObservationGuid();
						CsvCell patientGuid = observationParser.getPatientGuid();
						String obSourceId = EmisCsvHelper.createUniqueId(patientGuid, observationGuid);

						CsvCell codeId = observationParser.getCodeId();
						if (codeId.isEmpty()) {
							continue;
						}

						ResourceType resourceType = ObservationTransformer.getTargetResourceType(observationParser, csvHelper);

						UUID obUuid = IdHelper.getEdsResourceId(serviceId, resourceType, obSourceId);
						if (obUuid == null) {
							continue;
							//LOG.error("Null observation UUID for resource type " + resourceType + " and source ID " + obSourceId);
							//resourceType = ObservationTransformer.getTargetResourceType(observationParser, csvHelper);
						}
						Reference obReference = ReferenceHelper.createReference(resourceType, obUuid.toString());

						CsvCell consultationGuid = observationParser.getConsultationGuid();
						if (!consultationGuid.isEmpty()) {
							String sourceId = EmisCsvHelper.createUniqueId(patientGuid, consultationGuid);
							ReferenceList referenceList = consultationNewChildMap.get(sourceId);
							if (referenceList == null) {
								referenceList = new ReferenceList();
								consultationNewChildMap.put(sourceId, referenceList);
							}
							referenceList.add(obReference);
						}

						CsvCell problemGuid = observationParser.getProblemGuid();
						if (!problemGuid.isEmpty()) {
							String sourceId = EmisCsvHelper.createUniqueId(patientGuid, problemGuid);
							ReferenceList referenceList = newProblemChildren.get(sourceId);
							if (referenceList == null) {
								referenceList = new ReferenceList();
								newProblemChildren.put(sourceId, referenceList);
							}
							referenceList.add(obReference);
						}

						CsvCell parentObGuid = observationParser.getParentObservationGuid();
						if (!parentObGuid.isEmpty()) {
							String sourceId = EmisCsvHelper.createUniqueId(patientGuid, parentObGuid);
							ReferenceList referenceList = observationChildMap.get(sourceId);
							if (referenceList == null) {
								referenceList = new ReferenceList();
								observationChildMap.put(sourceId, referenceList);
							}
							referenceList.add(obReference);
						}
					}

					Diary diaryParser = (Diary)parsers.get(Diary.class);
					while (diaryParser.nextRecord()) {

						CsvCell consultationGuid = diaryParser.getConsultationGuid();
						if (!consultationGuid.isEmpty()) {

							CsvCell diaryGuid = diaryParser.getDiaryGuid();
							CsvCell patientGuid = diaryParser.getPatientGuid();
							String diarySourceId = EmisCsvHelper.createUniqueId(patientGuid, diaryGuid);
							UUID diaryUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.ProcedureRequest, diarySourceId);
							if (diaryUuid == null) {
								continue;
								//LOG.error("Null observation UUID for resource type " + ResourceType.ProcedureRequest + " and source ID " + diarySourceId);
							}
							Reference diaryReference = ReferenceHelper.createReference(ResourceType.ProcedureRequest, diaryUuid.toString());

							String sourceId = EmisCsvHelper.createUniqueId(patientGuid, consultationGuid);
							ReferenceList referenceList = consultationNewChildMap.get(sourceId);
							if (referenceList == null) {
								referenceList = new ReferenceList();
								consultationNewChildMap.put(sourceId, referenceList);
							}
							referenceList.add(diaryReference);
						}
					}

					IssueRecord issueRecordParser = (IssueRecord)parsers.get(IssueRecord.class);
					while (issueRecordParser.nextRecord()) {
						
						CsvCell problemGuid = issueRecordParser.getProblemObservationGuid();
						if (!problemGuid.isEmpty()) {

							CsvCell issueRecordGuid = issueRecordParser.getIssueRecordGuid();
							CsvCell patientGuid = issueRecordParser.getPatientGuid();
							String issueRecordSourceId = EmisCsvHelper.createUniqueId(patientGuid, issueRecordGuid);
							UUID issueRecordUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.MedicationOrder, issueRecordSourceId);
							if (issueRecordUuid == null) {
								continue;
								//LOG.error("Null observation UUID for resource type " + ResourceType.MedicationOrder + " and source ID " + issueRecordSourceId);
							}
							Reference issueRecordReference = ReferenceHelper.createReference(ResourceType.MedicationOrder, issueRecordUuid.toString());

							String sourceId = EmisCsvHelper.createUniqueId(patientGuid, problemGuid);
							ReferenceList referenceList = newProblemChildren.get(sourceId);
							if (referenceList == null) {
								referenceList = new ReferenceList();
								newProblemChildren.put(sourceId, referenceList);
							}
							referenceList.add(issueRecordReference);
						}
					}

					DrugRecord drugRecordParser = (DrugRecord)parsers.get(DrugRecord.class);
					while (drugRecordParser.nextRecord()) {

						CsvCell problemGuid = drugRecordParser.getProblemObservationGuid();
						if (!problemGuid.isEmpty()) {

							CsvCell drugRecordGuid = drugRecordParser.getDrugRecordGuid();
							CsvCell patientGuid = drugRecordParser.getPatientGuid();
							String drugRecordSourceId = EmisCsvHelper.createUniqueId(patientGuid, drugRecordGuid);
							UUID drugRecordUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.MedicationStatement, drugRecordSourceId);
							if (drugRecordUuid == null) {
								continue;
								//LOG.error("Null observation UUID for resource type " + ResourceType.MedicationStatement + " and source ID " + drugRecordSourceId);
							}
							Reference drugRecordReference = ReferenceHelper.createReference(ResourceType.MedicationStatement, drugRecordUuid.toString());

							String sourceId = EmisCsvHelper.createUniqueId(patientGuid, problemGuid);
							ReferenceList referenceList = newProblemChildren.get(sourceId);
							if (referenceList == null) {
								referenceList = new ReferenceList();
								newProblemChildren.put(sourceId, referenceList);
							}
							referenceList.add(drugRecordReference);
						}
					}

					for (AbstractCsvParser parser : parsers.values()) {
						try {
							parser.close();
						} catch (IOException ex) {
							//don't worry if this fails, as we're done anyway
						}
					}
				}

				ResourceDalI resourceDal = DalProvider.factoryResourceDal();

				LOG.info("Found " + consultationNewChildMap.size() + " Encounters to fix");
				for (String encounterSourceId: consultationNewChildMap.keySet()) {

					ReferenceList childReferences = consultationNewChildMap.get(encounterSourceId);

					//map to UUID
					UUID encounterId = IdHelper.getEdsResourceId(serviceId, ResourceType.Encounter, encounterSourceId);
					if (encounterId == null) {
						continue;
					}

					//get history, which is most recent FIRST
					List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Encounter.toString(), encounterId);
					if (history.isEmpty()) {
						continue;
						//throw new Exception("Empty history for Encounter " + encounterId);
					}

					ResourceWrapper currentState = history.get(0);
					if (currentState.isDeleted()) {
						continue;
					}

					//find last instance prior to cutoff and get its linked children
					for (ResourceWrapper wrapper: history) {
						Date d = wrapper.getCreatedAt();
						if (!d.after(cutoff)) {
							if (wrapper.getResourceData() != null) {
								Encounter encounter = (Encounter) FhirSerializationHelper.deserializeResource(wrapper.getResourceData());
								EncounterBuilder encounterBuilder = new EncounterBuilder(encounter);
								ContainedListBuilder containedListBuilder = new ContainedListBuilder(encounterBuilder);

								List<Reference> previousChildren = containedListBuilder.getContainedListItems();
								childReferences.add(previousChildren);
							}

							break;
						}
					}

					if (childReferences.size() == 0) {
						continue;
					}

					String json = currentState.getResourceData();
					Resource resource = FhirSerializationHelper.deserializeResource(json);
					String newJson = FhirSerializationHelper.serializeResource(resource);
					if (!json.equals(newJson)) {
						currentState.setResourceData(newJson);
						currentState.setResourceChecksum(FhirStorageService.generateChecksum(newJson));

						saveResourceWrapper(serviceId, currentState);
					}

					*//*Encounter encounter = (Encounter)FhirSerializationHelper.deserializeResource(currentState.getResourceData());
					EncounterBuilder encounterBuilder = new EncounterBuilder(encounter);
					ContainedListBuilder containedListBuilder = new ContainedListBuilder(encounterBuilder);

					containedListBuilder.addReferences(childReferences);

					String newJson = FhirSerializationHelper.serializeResource(encounter);
					currentState.setResourceData(newJson);
					currentState.setResourceChecksum(FhirStorageService.generateChecksum(newJson));

					saveResourceWrapper(serviceId, currentState);*//*
				}


				LOG.info("Found " + observationChildMap.size() + " Parent Observations to fix");
				for (String sourceId: observationChildMap.keySet()) {

					ReferenceList childReferences = observationChildMap.get(sourceId);

					//map to UUID
					ResourceType resourceType = null;

					UUID resourceId = IdHelper.getEdsResourceId(serviceId, ResourceType.Observation, sourceId);
					if (resourceId != null) {
						resourceType = ResourceType.Observation;

					} else {
						resourceId = IdHelper.getEdsResourceId(serviceId, ResourceType.DiagnosticReport, sourceId);
						if (resourceId != null) {
							resourceType = ResourceType.DiagnosticReport;

						} else {
							continue;
						}
					}


					//get history, which is most recent FIRST
					List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, resourceType.toString(), resourceId);
					if (history.isEmpty()) {
						//throw new Exception("Empty history for " + resourceType + " " + resourceId);
						continue;
					}

					ResourceWrapper currentState = history.get(0);
					if (currentState.isDeleted()) {
						continue;
					}

					//find last instance prior to cutoff and get its linked children
					for (ResourceWrapper wrapper: history) {
						Date d = wrapper.getCreatedAt();
						if (!d.after(cutoff)) {

							if (resourceType == ResourceType.Observation) {
								if (wrapper.getResourceData() != null) {
									Observation observation = (Observation) FhirSerializationHelper.deserializeResource(wrapper.getResourceData());
									if (observation.hasRelated()) {
										for (Observation.ObservationRelatedComponent related : observation.getRelated()) {
											Reference reference = related.getTarget();
											childReferences.add(reference);
										}
									}
								}

							} else {
								if (wrapper.getResourceData() != null) {
									DiagnosticReport report = (DiagnosticReport) FhirSerializationHelper.deserializeResource(wrapper.getResourceData());
									if (report.hasResult()) {
										for (Reference reference : report.getResult()) {
											childReferences.add(reference);
										}
									}
								}
							}

							break;
						}
					}

					if (childReferences.size() == 0) {
						continue;
					}

					String json = currentState.getResourceData();
					Resource resource = FhirSerializationHelper.deserializeResource(json);
					String newJson = FhirSerializationHelper.serializeResource(resource);
					if (!json.equals(newJson)) {
						currentState.setResourceData(newJson);
						currentState.setResourceChecksum(FhirStorageService.generateChecksum(newJson));

						saveResourceWrapper(serviceId, currentState);
					}

					*//*Resource resource = FhirSerializationHelper.deserializeResource(currentState.getResourceData());

					boolean changed = false;

					if (resourceType == ResourceType.Observation) {
						ObservationBuilder resourceBuilder = new ObservationBuilder((Observation)resource);
						for (int i=0; i<childReferences.size(); i++) {
							Reference reference = childReferences.getReference(i);
							if (resourceBuilder.addChildObservation(reference)) {
								changed = true;
							}
						}

					} else {
						DiagnosticReportBuilder resourceBuilder = new DiagnosticReportBuilder((DiagnosticReport)resource);
						for (int i=0; i<childReferences.size(); i++) {
							Reference reference = childReferences.getReference(i);
							if (resourceBuilder.addResult(reference)) {
								changed = true;
							}
						}
					}

					if (changed) {
						String newJson = FhirSerializationHelper.serializeResource(resource);
						currentState.setResourceData(newJson);
						currentState.setResourceChecksum(FhirStorageService.generateChecksum(newJson));

						saveResourceWrapper(serviceId, currentState);
					}*//*
				}

				LOG.info("Found " + newProblemChildren.size() + " Problems to fix");
				for (String sourceId: newProblemChildren.keySet()) {

					ReferenceList childReferences = newProblemChildren.get(sourceId);

					//map to UUID
					UUID conditionId = IdHelper.getEdsResourceId(serviceId, ResourceType.Condition, sourceId);
					if (conditionId == null) {
						continue;
					}

					//get history, which is most recent FIRST
					List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Condition.toString(), conditionId);
					if (history.isEmpty()) {
						continue;
						//throw new Exception("Empty history for Condition " + conditionId);
					}

					ResourceWrapper currentState = history.get(0);
					if (currentState.isDeleted()) {
						continue;
					}

					//find last instance prior to cutoff and get its linked children
					for (ResourceWrapper wrapper: history) {
						Date d = wrapper.getCreatedAt();
						if (!d.after(cutoff)) {
							if (wrapper.getResourceData() != null) {
								Condition previousVersion = (Condition) FhirSerializationHelper.deserializeResource(wrapper.getResourceData());
								ConditionBuilder conditionBuilder = new ConditionBuilder(previousVersion);
								ContainedListBuilder containedListBuilder = new ContainedListBuilder(conditionBuilder);

								List<Reference> previousChildren = containedListBuilder.getContainedListItems();
								childReferences.add(previousChildren);
							}

							break;
						}
					}

					if (childReferences.size() == 0) {
						continue;
					}

					String json = currentState.getResourceData();
					Resource resource = FhirSerializationHelper.deserializeResource(json);
					String newJson = FhirSerializationHelper.serializeResource(resource);
					if (!json.equals(newJson)) {
						currentState.setResourceData(newJson);
						currentState.setResourceChecksum(FhirStorageService.generateChecksum(newJson));

						saveResourceWrapper(serviceId, currentState);
					}

					*//*Condition condition = (Condition)FhirSerializationHelper.deserializeResource(currentState.getResourceData());
					ConditionBuilder conditionBuilder = new ConditionBuilder(condition);
					ContainedListBuilder containedListBuilder = new ContainedListBuilder(conditionBuilder);

					containedListBuilder.addReferences(childReferences);

					String newJson = FhirSerializationHelper.serializeResource(condition);
					currentState.setResourceData(newJson);
					currentState.setResourceChecksum(FhirStorageService.generateChecksum(newJson));

					saveResourceWrapper(serviceId, currentState);*//*
				}

				//mark as done
				String updateSql = "UPDATE " + table + " SET done = 1 WHERE service_id = '" + serviceId + "';";
				entityManager = ConnectionManager.getAdminEntityManager();
				session = (SessionImpl)entityManager.getDelegate();
				connection = session.connection();
				statement = connection.createStatement();
				entityManager.getTransaction().begin();
				statement.executeUpdate(updateSql);
				entityManager.getTransaction().commit();
			}

			*//**
			 * For each practice:
			 Go through all files processed since 14 March
			 Cache all links as above
			 Cache all Encounters saved too

			 For each Encounter referenced at all:
			 Retrieve latest version from resource current
			 Retrieve version prior to 14 March
			 Update current version with old references plus new ones

			 For each parent observation:
			 Retrieve latest version (could be observation or diagnostic report)

			 For each problem:
			 Retrieve latest version from resource current
			 Check if still a problem:
			 Retrieve version prior to 14 March
			 Update current version with old references plus new ones

			 *//*

			LOG.info("Finished Fixing encounters from " + table);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void saveResourceWrapper(UUID serviceId, ResourceWrapper wrapper) throws Exception {

		if (wrapper.getResourceData() != null) {
			long checksum = FhirStorageService.generateChecksum(wrapper.getResourceData());
			wrapper.setResourceChecksum(new Long(checksum));
		}

		EntityManager entityManager = ConnectionManager.getEhrEntityManager(serviceId);
		SessionImpl session = (SessionImpl)entityManager.getDelegate();
		Connection connection = session.connection();
		Statement statement = connection.createStatement();

		entityManager.getTransaction().begin();

		String json = wrapper.getResourceData();
		json = json.replace("'", "''");
		json = json.replace("\\", "\\\\");

		String patientId = "";
		if (wrapper.getPatientId() != null) {
			patientId = wrapper.getPatientId().toString();
		}

		String updateSql = "UPDATE resource_current"
						+ " SET resource_data = '" + json + "',"
						+ " resource_checksum = " + wrapper.getResourceChecksum()
						+ " WHERE service_id = '" + wrapper.getServiceId() + "'"
						+ " AND patient_id = '" + patientId + "'"
						+ " AND resource_type = '" + wrapper.getResourceType() + "'"
						+ " AND resource_id = '" + wrapper.getResourceId() + "'";
		statement.executeUpdate(updateSql);

		//LOG.debug(updateSql);

		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
		//String createdAtStr = sdf.format(wrapper.getCreatedAt());

		updateSql = "UPDATE resource_history"
				+ " SET resource_data = '" + json + "',"
				+ " resource_checksum = " + wrapper.getResourceChecksum()
				+ " WHERE resource_id = '" + wrapper.getResourceId() + "'"
				+ " AND resource_type = '" + wrapper.getResourceType() + "'"
				//+ " AND created_at = '" + createdAtStr + "'"
				+ " AND version = '" + wrapper.getVersion() + "'";
		statement.executeUpdate(updateSql);

		//LOG.debug(updateSql);

		entityManager.getTransaction().commit();
	}

	/*private static void populateNewSearchTable(String table) {
		LOG.info("Populating New Search Table");

		try {

			EntityManager entityManager = ConnectionManager.getEdsEntityManager();
			SessionImpl session = (SessionImpl)entityManager.getDelegate();
			Connection connection = session.connection();
			Statement statement = connection.createStatement();

			List<String> patientIds = new ArrayList<>();
			Map<String, String> serviceIds = new HashMap<>();

			String sql = "SELECT patient_id, service_id FROM " + table + " WHERE done = 0";
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				String patientId = rs.getString(1);
				String serviceId = rs.getString(2);
				patientIds.add(patientId);
				serviceIds.put(patientId, serviceId);
			}
			rs.close();
			statement.close();
			entityManager.close();

			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearch2Dal();

			LOG.info("Found " + patientIds.size() + " to do");

			for (int i=0; i<patientIds.size(); i++) {

				String patientIdStr = patientIds.get(i);
				UUID patientId = UUID.fromString(patientIdStr);
				String serviceIdStr = serviceIds.get(patientIdStr);
				UUID serviceId = UUID.fromString(serviceIdStr);

				Patient patient = (Patient)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientIdStr);
				if (patient != null) {
					patientSearchDal.update(serviceId, patient);

					//find episode of care
					List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, null, patientId, ResourceType.EpisodeOfCare.toString());
					for (ResourceWrapper wrapper: wrappers) {
						if (!wrapper.isDeleted()) {
							EpisodeOfCare episodeOfCare = (EpisodeOfCare)FhirSerializationHelper.deserializeResource(wrapper.getResourceData());
							patientSearchDal.update(serviceId, episodeOfCare);
						}
					}
				}



				String updateSql = "UPDATE " + table + " SET done = 1 WHERE patient_id = '" + patientIdStr + "' AND service_id = '" + serviceIdStr + "';";
				entityManager = ConnectionManager.getEdsEntityManager();
				session = (SessionImpl)entityManager.getDelegate();
				connection = session.connection();
				statement = connection.createStatement();
				entityManager.getTransaction().begin();
				statement.executeUpdate(updateSql);
				entityManager.getTransaction().commit();

				if (i % 5000 == 0) {
					LOG.info("Done " + (i+1) + " of " + patientIds.size());
				}
			}

			entityManager.close();

			LOG.info("Finished Populating New Search Table");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	private static void createBartsSubset(String sourceDir, UUID serviceUuid, UUID systemUuid, String samplePatientsFile) {
		LOG.info("Creating Barts Subset");

		try {

			Set<String> personIds = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
			for (String line: lines) {
				line = line.trim();

				//ignore comments
				if (line.startsWith("#")) {
					continue;
				}
				personIds.add(line);
			}

			createBartsSubsetForFile(sourceDir, serviceUuid, systemUuid, personIds);

			LOG.info("Finished Creating Barts Subset");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	/*private static void createBartsSubsetForFile(File sourceDir, File destDir, Set<String> personIds) throws Exception {

		for (File sourceFile: sourceDir.listFiles()) {

			String name = sourceFile.getName();
			File destFile = new File(destDir, name);

			if (sourceFile.isDirectory()) {

				if (!destFile.exists()) {
					destFile.mkdirs();
				}

				LOG.info("Doing dir " + sourceFile);
				createBartsSubsetForFile(sourceFile, destFile, personIds);

			} else {

				//we have some bad partial files in, so ignore them
				String ext = FilenameUtils.getExtension(name);
				if (ext.equalsIgnoreCase("filepart")) {
					continue;
				}

				//if the file is empty, we still need the empty file in the filtered directory, so just copy it
				if (sourceFile.length() == 0) {
					LOG.info("Copying empty file " + sourceFile);
					if (!destFile.exists()) {
						copyFile(sourceFile, destFile);
					}
					continue;
				}

				String baseName = FilenameUtils.getBaseName(name);
				String fileType = BartsCsvToFhirTransformer.identifyFileType(baseName);

				if (isCerner22File(fileType)) {
					LOG.info("Checking 2.2 file " + sourceFile);

					if (destFile.exists()) {
						destFile.delete();
					}

					FileReader fr = new FileReader(sourceFile);
					BufferedReader br = new BufferedReader(fr);
					int lineIndex = -1;

					PrintWriter pw = null;
					int personIdColIndex = -1;
					int expectedCols = -1;

					while (true) {

						String line = br.readLine();
						if (line == null) {
							break;
						}

						lineIndex ++;

						if (lineIndex == 0) {

							if (fileType.equalsIgnoreCase("FAMILYHISTORY")) {
								//this file has no headers, so needs hard-coding
								personIdColIndex = 5;

							} else {

								//check headings for PersonID col
								String[] toks = line.split("\\|", -1);
								expectedCols = toks.length;

								for (int i=0; i<expectedCols; i++) {
									String col = toks[i];
									if (col.equalsIgnoreCase("PERSON_ID")
											|| col.equalsIgnoreCase("#PERSON_ID")) {
										personIdColIndex = i;
										break;
									}
								}

								//if no person ID, then just copy the entire file
								if (personIdColIndex == -1) {
									br.close();
									br = null;

									LOG.info("   Copying 2.2 file to " + destFile);
									copyFile(sourceFile, destFile);
									break;

								} else {
									LOG.info("   Filtering 2.2 file to " + destFile + ", person ID col at " + personIdColIndex);
								}
							}

							PrintWriter fw = new PrintWriter(destFile);
							BufferedWriter bw = new BufferedWriter(fw);
							pw = new PrintWriter(bw);

						} else {

							//filter on personID
							String[] toks = line.split("\\|", -1);
							if (expectedCols != -1
									&& toks.length != expectedCols) {
								throw new Exception("Line " + (lineIndex+1) + " has " + toks.length + " cols but expecting " + expectedCols);

							} else {
								String personId = toks[personIdColIndex];
								if (!Strings.isNullOrEmpty(personId) //always carry over rows with empty person ID, as Cerner won't send the person ID for deletes
									&& !personIds.contains(personId)) {
									continue;
								}
							}
						}

						pw.println(line);
					}

					if (br != null) {
						br.close();
					}
					if (pw != null) {
						pw.flush();
						pw.close();
					}

				} else {
					//the 2.1 files are going to be a pain to split by patient, so just copy them over
					LOG.info("Copying 2.1 file " + sourceFile);
					if (!destFile.exists()) {
						copyFile(sourceFile, destFile);
					}
				}
			}
		}
	}*/

	private static void createBartsSubsetForFile(String sourceDir, UUID serviceUuid, UUID systemUuid, Set<String> personIds) throws Exception {

		ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
		List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceUuid, systemUuid, Integer.MAX_VALUE);

		for (Exchange exchange: exchanges) {

			List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());

			for (ExchangePayloadFile fileObj : files) {

				String filePathWithoutSharedStorage = fileObj.getPath().substring(TransformConfig.instance().getSharedStoragePath().length()+1);
				String sourceFilePath = FilenameUtils.concat(sourceDir, filePathWithoutSharedStorage);
				File sourceFile = new File(sourceFilePath);

				String destFilePath = fileObj.getPath();
				File destFile = new File(destFilePath);

				File destDir = destFile.getParentFile();
				if (!destDir.exists()) {
					destDir.mkdirs();
				}

				//if the file is empty, we still need the empty file in the filtered directory, so just copy it
				if (sourceFile.length() == 0) {
					LOG.info("Copying empty file " + sourceFile);
					if (!destFile.exists()) {
						copyFile(sourceFile, destFile);
					}
					continue;
				}

				String fileType = fileObj.getType();

				if (isCerner22File(fileType)) {
					LOG.info("Checking 2.2 file " + sourceFile);

					if (destFile.exists()) {
						destFile.delete();
					}

					FileReader fr = new FileReader(sourceFile);
					BufferedReader br = new BufferedReader(fr);
					int lineIndex = -1;

					PrintWriter pw = null;
					int personIdColIndex = -1;
					int expectedCols = -1;

					while (true) {

						String line = br.readLine();
						if (line == null) {
							break;
						}

						lineIndex++;

						if (lineIndex == 0) {

							if (fileType.equalsIgnoreCase("FAMILYHISTORY")) {
								//this file has no headers, so needs hard-coding
								personIdColIndex = 5;

							} else {

								//check headings for PersonID col
								String[] toks = line.split("\\|", -1);
								expectedCols = toks.length;

								for (int i = 0; i < expectedCols; i++) {
									String col = toks[i];
									if (col.equalsIgnoreCase("PERSON_ID")
											|| col.equalsIgnoreCase("#PERSON_ID")) {
										personIdColIndex = i;
										break;
									}
								}

								//if no person ID, then just copy the entire file
								if (personIdColIndex == -1) {
									br.close();
									br = null;

									LOG.info("   Copying 2.2 file to " + destFile);
									copyFile(sourceFile, destFile);
									break;

								} else {
									LOG.info("   Filtering 2.2 file to " + destFile + ", person ID col at " + personIdColIndex);
								}
							}

							PrintWriter fw = new PrintWriter(destFile);
							BufferedWriter bw = new BufferedWriter(fw);
							pw = new PrintWriter(bw);

						} else {

							//filter on personID
							String[] toks = line.split("\\|", -1);
							if (expectedCols != -1
									&& toks.length != expectedCols) {
								throw new Exception("Line " + (lineIndex + 1) + " has " + toks.length + " cols but expecting " + expectedCols);

							} else {
								String personId = toks[personIdColIndex];
								if (!Strings.isNullOrEmpty(personId) //always carry over rows with empty person ID, as Cerner won't send the person ID for deletes
										&& !personIds.contains(personId)) {
									continue;
								}
							}
						}

						pw.println(line);
					}

					if (br != null) {
						br.close();
					}
					if (pw != null) {
						pw.flush();
						pw.close();
					}

				} else {
					//the 2.1 files are going to be a pain to split by patient, so just copy them over
					LOG.info("Copying 2.1 file " + sourceFile);
					if (!destFile.exists()) {
						copyFile(sourceFile, destFile);
					}
				}
			}
		}
	}

	private static void copyFile(File src, File dst) throws Exception {
		FileInputStream fis = new FileInputStream(src);
		BufferedInputStream bis = new BufferedInputStream(fis);
		Files.copy(bis, dst.toPath());
		bis.close();
	}
	
	private static boolean isCerner22File(String fileType) throws Exception {

		if (fileType.equalsIgnoreCase("PPATI")
				|| fileType.equalsIgnoreCase("PPREL")
				|| fileType.equalsIgnoreCase("CDSEV")
				|| fileType.equalsIgnoreCase("PPATH")
				|| fileType.equalsIgnoreCase("RTTPE")
				|| fileType.equalsIgnoreCase("AEATT")
				|| fileType.equalsIgnoreCase("AEINV")
				|| fileType.equalsIgnoreCase("AETRE")
				|| fileType.equalsIgnoreCase("OPREF")
				|| fileType.equalsIgnoreCase("OPATT")
				|| fileType.equalsIgnoreCase("EALEN")
				|| fileType.equalsIgnoreCase("EALSU")
				|| fileType.equalsIgnoreCase("EALOF")
				|| fileType.equalsIgnoreCase("HPSSP")
				|| fileType.equalsIgnoreCase("IPEPI")
				|| fileType.equalsIgnoreCase("IPWDS")
				|| fileType.equalsIgnoreCase("DELIV")
				|| fileType.equalsIgnoreCase("BIRTH")
				|| fileType.equalsIgnoreCase("SCHAC")
				|| fileType.equalsIgnoreCase("APPSL")
				|| fileType.equalsIgnoreCase("DIAGN")
				|| fileType.equalsIgnoreCase("PROCE")
				|| fileType.equalsIgnoreCase("ORDER")
				|| fileType.equalsIgnoreCase("DOCRP")
				|| fileType.equalsIgnoreCase("DOCREF")
				|| fileType.equalsIgnoreCase("CNTRQ")
				|| fileType.equalsIgnoreCase("LETRS")
				|| fileType.equalsIgnoreCase("LOREF")
				|| fileType.equalsIgnoreCase("ORGREF")
				|| fileType.equalsIgnoreCase("PRSNLREF")
				|| fileType.equalsIgnoreCase("CVREF")
				|| fileType.equalsIgnoreCase("NOMREF")
				|| fileType.equalsIgnoreCase("EALIP")
				|| fileType.equalsIgnoreCase("CLEVE")
				|| fileType.equalsIgnoreCase("ENCNT")
				|| fileType.equalsIgnoreCase("RESREF")
				|| fileType.equalsIgnoreCase("PPNAM")
				|| fileType.equalsIgnoreCase("PPADD")
				|| fileType.equalsIgnoreCase("PPPHO")
				|| fileType.equalsIgnoreCase("PPALI")
				|| fileType.equalsIgnoreCase("PPINF")
				|| fileType.equalsIgnoreCase("PPAGP")
				|| fileType.equalsIgnoreCase("SURCC")
				|| fileType.equalsIgnoreCase("SURCP")
				|| fileType.equalsIgnoreCase("SURCA")
				|| fileType.equalsIgnoreCase("SURCD")
				|| fileType.equalsIgnoreCase("PDRES")
				|| fileType.equalsIgnoreCase("PDREF")
				|| fileType.equalsIgnoreCase("ABREF")
				|| fileType.equalsIgnoreCase("CEPRS")
				|| fileType.equalsIgnoreCase("ORDDT")
				|| fileType.equalsIgnoreCase("STATREF")
				|| fileType.equalsIgnoreCase("STATA")
				|| fileType.equalsIgnoreCase("ENCINF")
				|| fileType.equalsIgnoreCase("SCHDETAIL")
				|| fileType.equalsIgnoreCase("SCHOFFER")
				|| fileType.equalsIgnoreCase("PPGPORG")
				|| fileType.equalsIgnoreCase("FAMILYHISTORY")) {
			return true;

		} else {
			return false;
		}
	}

	/*private static void fixSubscriberDbs() {
		LOG.info("Fixing Subscriber DBs");

		try {
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();
			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			UUID emisSystem = UUID.fromString("991a9068-01d3-4ff2-86ed-249bd0541fb3");
			UUID emisSystemDev = UUID.fromString("55c08fa5-ef1e-4e94-aadc-e3d6adc80774");

			PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");

			Date dateError = new SimpleDateFormat("yyyy-MM-dd").parse("2018-05-11");

			List<Service> services = serviceDal.getAll();

			for (Service service: services) {

				String endpointsJson = service.getEndpoints();
				if (Strings.isNullOrEmpty(endpointsJson)) {
					continue;
				}

				UUID serviceId = service.getId();
				LOG.info("Checking " + service.getName() + " " + serviceId);

				List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
				for (JsonServiceInterfaceEndpoint endpoint: endpoints) {
					UUID endpointSystemId = endpoint.getSystemUuid();
					if (!endpointSystemId.equals(emisSystem)
							&& !endpointSystemId.equals(emisSystemDev)) {
						LOG.info("    Skipping system ID " + endpointSystemId + " as not Emis");
						continue;
					}

					List<UUID> exchangeIds = exchangeDal.getExchangeIdsForService(serviceId, endpointSystemId);

					boolean needsFixing = false;

					for (UUID exchangeId: exchangeIds) {

						if (!needsFixing) {
							List<ExchangeTransformAudit> transformAudits = exchangeDal.getAllExchangeTransformAudits(serviceId, endpointSystemId, exchangeId);
							for (ExchangeTransformAudit audit: transformAudits) {
								Date transfromStart = audit.getStarted();
								if (!transfromStart.before(dateError)) {
									needsFixing = true;
									break;
								}
							}
						}

						if (!needsFixing) {
							continue;
						}

						List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(exchangeId);
						Exchange exchange = exchangeDal.getExchange(exchangeId);
						LOG.info("    Posting exchange " + exchangeId + " with " + batches.size() + " batches");

						List<UUID> batchIds = new ArrayList<>();

						for (ExchangeBatch batch: batches) {

							UUID patientId = batch.getEdsPatientId();
							if (patientId == null) {
								continue;
							}

							UUID batchId = batch.getBatchId();
							batchIds.add(batchId);
						}

						String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchIds.toArray());
						exchange.setHeader(HeaderKeys.BatchIdsJson, batchUuidsStr);

						PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
						component.process(exchange);
					}
				}
			}

			LOG.info("Finished Fixing Subscriber DBs");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void fixReferralRequests() {
		LOG.info("Fixing Referral Requests");

		try {
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();
			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			UUID emisSystem = UUID.fromString("991a9068-01d3-4ff2-86ed-249bd0541fb3");
			UUID emisSystemDev = UUID.fromString("55c08fa5-ef1e-4e94-aadc-e3d6adc80774");

			PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");

			Date dateError = new SimpleDateFormat("yyyy-MM-dd").parse("2018-04-24");

			List<Service> services = serviceDal.getAll();

			for (Service service: services) {

				String endpointsJson = service.getEndpoints();
				if (Strings.isNullOrEmpty(endpointsJson)) {
					continue;
				}

				UUID serviceId = service.getId();
				LOG.info("Checking " + service.getName() + " " + serviceId);

				List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
				for (JsonServiceInterfaceEndpoint endpoint: endpoints) {
					UUID endpointSystemId = endpoint.getSystemUuid();
					if (!endpointSystemId.equals(emisSystem)
							&& !endpointSystemId.equals(emisSystemDev)) {
						LOG.info("    Skipping system ID " + endpointSystemId + " as not Emis");
						continue;
					}

					List<UUID> exchangeIds = exchangeDal.getExchangeIdsForService(serviceId, endpointSystemId);

					boolean needsFixing = false;
					Set<UUID> patientIdsToPost = new HashSet<>();

					for (UUID exchangeId: exchangeIds) {

						if (!needsFixing) {
							List<ExchangeTransformAudit> transformAudits = exchangeDal.getAllExchangeTransformAudits(serviceId, endpointSystemId, exchangeId);
							for (ExchangeTransformAudit audit: transformAudits) {
								Date transfromStart = audit.getStarted();
								if (!transfromStart.before(dateError)) {
									needsFixing = true;
									break;
								}
							}
						}

						if (!needsFixing) {
							continue;
						}

						List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(exchangeId);
						Exchange exchange = exchangeDal.getExchange(exchangeId);
						LOG.info("Checking exchange " + exchangeId + " with " + batches.size() + " batches");

						for (ExchangeBatch batch: batches) {

							UUID patientId = batch.getEdsPatientId();
							if (patientId == null) {
								continue;
							}

							UUID batchId = batch.getBatchId();

							List<ResourceWrapper> wrappers = resourceDal.getResourcesForBatch(serviceId, batchId);

							for (ResourceWrapper wrapper: wrappers) {
								String resourceType = wrapper.getResourceType();
								if (!resourceType.equals(ResourceType.ReferralRequest.toString())
										|| wrapper.isDeleted()) {
									continue;
								}

								String json = wrapper.getResourceData();
								ReferralRequest referral = (ReferralRequest)FhirSerializationHelper.deserializeResource(json);

								*//*if (!referral.hasServiceRequested()) {
									continue;
								}

								CodeableConcept reason = referral.getServiceRequested().get(0);
								referral.setReason(reason);
								referral.getServiceRequested().clear();*//*

								if (!referral.hasReason()) {
									continue;
								}

								CodeableConcept reason = referral.getReason();
								referral.setReason(null);
								referral.addServiceRequested(reason);

								json = FhirSerializationHelper.serializeResource(referral);
								wrapper.setResourceData(json);

								saveResourceWrapper(serviceId, wrapper);

								//add to the set of patients we know need sending on to the protocol queue
								patientIdsToPost.add(patientId);

								LOG.info("Fixed " + resourceType + " " + wrapper.getResourceId() + " in batch " + batchId);
							}

							//if our patient has just been fixed or was fixed before, post onto the protocol queue
							if (patientIdsToPost.contains(patientId)) {

								List<UUID> batchIds = new ArrayList<>();
								batchIds.add(batchId);

								String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchIds.toArray());
								exchange.setHeader(HeaderKeys.BatchIdsJson, batchUuidsStr);


								PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
								component.process(exchange);

							}
						}
					}
				}
			}

			LOG.info("Finished Fixing Referral Requests");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void applyEmisAdminCaches() {
		LOG.info("Applying Emis Admin Caches");

		try {
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			UUID emisSystem = UUID.fromString("991a9068-01d3-4ff2-86ed-249bd0541fb3");
			UUID emisSystemDev = UUID.fromString("55c08fa5-ef1e-4e94-aadc-e3d6adc80774");

			List<Service> services = serviceDal.getAll();

			for (Service service: services) {

				String endpointsJson = service.getEndpoints();
				if (Strings.isNullOrEmpty(endpointsJson)) {
					continue;
				}

				UUID serviceId = service.getId();
				LOG.info("Checking " + service.getName() + " " + serviceId);

				List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
				for (JsonServiceInterfaceEndpoint endpoint: endpoints) {
					UUID endpointSystemId = endpoint.getSystemUuid();
					if (!endpointSystemId.equals(emisSystem)
							&& !endpointSystemId.equals(emisSystemDev)) {
						LOG.info("    Skipping system ID " + endpointSystemId + " as not Emis");
						continue;
					}

					if (!exchangeDal.isServiceStarted(serviceId, endpointSystemId)) {
						LOG.info("    Service not started, so skipping");
						continue;
					}

					//get exchanges
					List<UUID> exchangeIds = exchangeDal.getExchangeIdsForService(serviceId, endpointSystemId);
					if (exchangeIds.isEmpty()) {
						LOG.info("    No exchanges found, so skipping");
						continue;
					}
					UUID firstExchangeId = exchangeIds.get(0);

					List<ExchangeEvent> events = exchangeDal.getExchangeEvents(firstExchangeId);
					boolean appliedAdminCache = false;
					for (ExchangeEvent event: events) {
						if (event.getEventDesc().equals("Applied Emis Admin Resource Cache")) {
							appliedAdminCache = true;
						}
					}

					if (appliedAdminCache) {
						LOG.info("    Have already applied admin cache, so skipping");
						continue;
					}

					Exchange exchange = exchangeDal.getExchange(firstExchangeId);
					String body = exchange.getBody();
					String[] files = ExchangeHelper.parseExchangeBodyOldWay(body);
					if (files.length == 0) {
						LOG.info("    No files in exchange " + firstExchangeId + " so skipping");
						continue;
					}

					String firstFilePath = files[0];
					String name = FilenameUtils.getBaseName(firstFilePath); //file name without extension
					String[] toks = name.split("_");
					if (toks.length != 5) {
						throw new TransformException("Failed to extract data sharing agreement GUID from filename " + firstFilePath);
					}
					String sharingAgreementGuid = toks[4];

					List<UUID> batchIds = new ArrayList<>();
					TransformError transformError = new TransformError();
					FhirResourceFiler fhirResourceFiler = new FhirResourceFiler(firstExchangeId, serviceId, endpointSystemId, transformError, batchIds);

					EmisCsvHelper csvHelper = new EmisCsvHelper(fhirResourceFiler.getServiceId(), fhirResourceFiler.getSystemId(),
																		fhirResourceFiler.getExchangeId(), sharingAgreementGuid,
																		true);

					ExchangeTransformAudit transformAudit = new ExchangeTransformAudit();
					transformAudit.setServiceId(serviceId);
					transformAudit.setSystemId(endpointSystemId);
					transformAudit.setExchangeId(firstExchangeId);
					transformAudit.setId(UUID.randomUUID());
					transformAudit.setStarted(new Date());

					LOG.info("    Going to apply admin resource cache");
					csvHelper.applyAdminResourceCache(fhirResourceFiler);

					fhirResourceFiler.waitToFinish();

					for (UUID batchId: batchIds) {
						LOG.info("   Created batch ID " + batchId + " for exchange " + firstExchangeId);
					}

					transformAudit.setEnded(new Date());
					transformAudit.setNumberBatchesCreated(new Integer(batchIds.size()));

					boolean hadError = false;
					if (transformError.getError().size() > 0) {
						transformAudit.setErrorXml(TransformErrorSerializer.writeToXml(transformError));
						hadError = true;
					}

					exchangeDal.save(transformAudit);

					//clear down the cache of reference mappings since they won't be of much use for the next Exchange
					IdHelper.clearCache();

					if (hadError) {
						LOG.error("   <<<<<<Error applying resource cache!");
						continue;
					}

					//add the event to say we've applied the cache
					AuditWriter.writeExchangeEvent(firstExchangeId, "Applied Emis Admin Resource Cache");

					//post that ONE new batch ID onto the protocol queue
					String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchIds.toArray());
					exchange.setHeader(HeaderKeys.BatchIdsJson, batchUuidsStr);

					PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");

					PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
					component.process(exchange);
				}
			}

			LOG.info("Finished Applying Emis Admin Caches");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	/*private static void fixBartsEscapedFiles(String filePath) {
		LOG.info("Fixing Barts Escaped Files in " + filePath);

		try {
			fixBartsEscapedFilesInDir(new File(filePath));

			LOG.info("Finished fixing Barts Escaped Files in " + filePath);

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}


	/**
	 * fixes Emis extract(s) when a practice was disabled then subsequently re-bulked, by
	 * replacing the "delete" extracts with newly generated deltas that can be processed
	 * before the re-bulk is done
	 */
	private static void fixDisabledEmisExtract(String serviceId, String systemId, String sharedStoragePath, String tempDir) {

		LOG.info("Fixing Disabled Emis Extracts Prior to Re-bulk for service " + serviceId);

		try {

			/*File tempDirLast = new File(tempDir, "last");
			if (!tempDirLast.exists()) {
				if (!tempDirLast.mkdirs()) {
					throw new Exception("Failed to create temp dir " + tempDirLast);
				}
				tempDirLast.mkdirs();
			}
			File tempDirEmpty = new File(tempDir, "empty");
			if (!tempDirEmpty.exists()) {
				if (!tempDirEmpty.mkdirs()) {
					throw new Exception("Failed to create temp dir " + tempDirEmpty);
				}
				tempDirEmpty.mkdirs();
			}*/

			UUID serviceUuid = UUID.fromString(serviceId);
			UUID systemUuid = UUID.fromString(systemId);
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

			//get all the exchanges, which are returned in reverse order, so reverse for simplicity
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceUuid, systemUuid, Integer.MAX_VALUE);

			//sorting by timestamp seems unreliable when exchanges were posted close together?
			List<Exchange> tmp = new ArrayList<>();
			for (int i=exchanges.size()-1; i>=0; i--) {
				Exchange exchange = exchanges.get(i);
				tmp.add(exchange);
			}
			exchanges = tmp;
			/*exchanges.sort((o1, o2) -> {
				Date d1 = o1.getTimestamp();
				Date d2 = o2.getTimestamp();
				return d1.compareTo(d2);
			});*/

			LOG.info("Found " + exchanges.size() + " exchanges");
			//continueOrQuit();

			//find the files for each exchange
			Map<Exchange, List<String>> hmExchangeFiles = new HashMap<>();
			Map<Exchange, List<String>> hmExchangeFilesWithoutStoragePrefix = new HashMap<>();
			for (Exchange exchange: exchanges) {

				//populate a map of the files with the shared storage prefix
				String exchangeBody = exchange.getBody();
				String[] files = ExchangeHelper.parseExchangeBodyOldWay(exchangeBody);
				List<String> fileList = Lists.newArrayList(files);
				hmExchangeFiles.put(exchange, fileList);

				//populate a map of the same files without the prefix
				files = ExchangeHelper.parseExchangeBodyOldWay(exchangeBody);
				for (int i=0; i<files.length; i++) {
					String file = files[i].substring(sharedStoragePath.length() + 1);
					files[i] = file;
				}
				fileList = Lists.newArrayList(files);
				hmExchangeFilesWithoutStoragePrefix.put(exchange, fileList);
			}
			LOG.info("Cached files for each exchange");

			int indexDisabled = -1;
			int indexRebulked = -1;
			int indexOriginallyBulked = -1;

			//go back through them to find the extract where the re-bulk is and when it was disabled
			for (int i=exchanges.size()-1; i>=0; i--) {
				Exchange exchange = exchanges.get(i);
				boolean disabled = isDisabledInSharingAgreementFile(exchange, hmExchangeFiles);

				if (disabled) {
					indexDisabled = i;

				} else {
					if (indexDisabled == -1) {
						indexRebulked = i;
					} else {
						//if we've found a non-disabled extract older than the disabled ones,
						//then we've gone far enough back
						break;
					}
				}
			}

			//go back from when disabled to find the previous bulk load (i.e. the first one or one after it was previously not disabled)
			for (int i=indexDisabled-1; i>=0; i--) {
				Exchange exchange = exchanges.get(i);
				boolean disabled = isDisabledInSharingAgreementFile(exchange, hmExchangeFiles);
				if (disabled) {
					break;
				}

				indexOriginallyBulked = i;
			}

			if (indexDisabled == -1
					|| indexRebulked == -1
					|| indexOriginallyBulked == -1) {
				throw new Exception("Failed to find exchanges for disabling (" + indexDisabled + "), re-bulking (" + indexRebulked + ") or original bulk (" + indexOriginallyBulked + ")");
			}

			Exchange exchangeDisabled = exchanges.get(indexDisabled);
			LOG.info("Disabled on " + findExtractDate(exchangeDisabled, hmExchangeFiles) + " " + exchangeDisabled.getId());

			Exchange exchangeRebulked = exchanges.get(indexRebulked);
			LOG.info("Rebulked on " + findExtractDate(exchangeRebulked, hmExchangeFiles) + " " + exchangeRebulked.getId());

			Exchange exchangeOriginallyBulked = exchanges.get(indexOriginallyBulked);
			LOG.info("Originally bulked on " + findExtractDate(exchangeOriginallyBulked, hmExchangeFiles) + " " + exchangeOriginallyBulked.getId());

			//continueOrQuit();

			List<String> rebulkFiles = hmExchangeFiles.get(exchangeRebulked);

			List<String> tempFilesCreated = new ArrayList<>();

			Set<String> patientGuidsDeletedOrTooOld = new HashSet<>();

			for (String rebulkFile: rebulkFiles) {
				String fileType = findFileType(rebulkFile);
				if (!isPatientFile(fileType)) {
					continue;
				}

				LOG.info("Doing " + fileType);

				String guidColumnName = getGuidColumnName(fileType);

				//find all the guids in the re-bulk
				Set<String> idsInRebulk = new HashSet<>();

				InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(rebulkFile);
				CSVParser csvParser = new CSVParser(reader, EmisCsvToFhirTransformer.CSV_FORMAT);

				String[] headers = null;
				try {
					headers = CsvHelper.getHeaderMapAsArray(csvParser);

					Iterator<CSVRecord> iterator = csvParser.iterator();

					while (iterator.hasNext()) {
						CSVRecord record = iterator.next();

						//get the patient and row guid out of the file and cache in our set
						String id = record.get("PatientGuid");
						if (!Strings.isNullOrEmpty(guidColumnName)) {
							id += "//" + record.get(guidColumnName);
						}

						idsInRebulk.add(id);
					}
				} finally {
					csvParser.close();
				}

				LOG.info("Found " + idsInRebulk.size() + " IDs in re-bulk file: " + rebulkFile);

				//create a replacement file for the exchange the service was disabled
				String replacementDisabledFile = null;
				List<String> disabledFiles = hmExchangeFilesWithoutStoragePrefix.get(exchangeDisabled);
				for (String s: disabledFiles) {
					String disabledFileType = findFileType(s);
					if (disabledFileType.equals(fileType)) {

						replacementDisabledFile = FilenameUtils.concat(tempDir, s);

						File dir = new File(replacementDisabledFile).getParentFile();
						if (!dir.exists()) {
							if (!dir.mkdirs()) {
								throw new Exception("Failed to create directory " + dir);
							}
						}

						tempFilesCreated.add(s);
						LOG.info("Created replacement file " + replacementDisabledFile);
					}
				}

				FileWriter fileWriter = new FileWriter(replacementDisabledFile);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, EmisCsvToFhirTransformer.CSV_FORMAT.withHeader(headers));
				csvPrinter.flush();

				Set<String> pastIdsProcessed = new HashSet<>();

				//now go through all files of the same type PRIOR to the service was disabled
				//to find any rows that we'll need to explicitly delete because they were deleted while
				//the extract was disabled
				for (int i=indexDisabled-1; i>=indexOriginallyBulked; i--) {
					Exchange exchange = exchanges.get(i);

					String originalFile = null;

					List<String> files = hmExchangeFiles.get(exchange);
					for (String s: files) {
						String originalFileType = findFileType(s);
						if (originalFileType.equals(fileType)) {
							originalFile = s;
							break;
						}
					}

					if (originalFile == null) {
						continue;
					}

					LOG.info("    Reading " + originalFile);
					reader = FileHelper.readFileReaderFromSharedStorage(originalFile);
					csvParser = new CSVParser(reader, EmisCsvToFhirTransformer.CSV_FORMAT);
					try {
						Iterator<CSVRecord> iterator = csvParser.iterator();

						while (iterator.hasNext()) {
							CSVRecord record = iterator.next();
							String patientGuid = record.get("PatientGuid");

							//get the patient and row guid out of the file and cache in our set
							String uniqueId = patientGuid;
							if (!Strings.isNullOrEmpty(guidColumnName)) {
								uniqueId += "//" + record.get(guidColumnName);
							}

							//if we're already handled this record in a more recent extract, then skip it
							if (pastIdsProcessed.contains(uniqueId)) {
								continue;
							}
							pastIdsProcessed.add(uniqueId);

							//if this ID isn't deleted and isn't in the re-bulk then it means
							//it WAS deleted in Emis Web but we didn't receive the delete, because it was deleted
							//from Emis Web while the extract feed was disabled

							//if the record is deleted, then we won't expect it in the re-bulk
							boolean deleted = Boolean.parseBoolean(record.get("Deleted"));
							if (deleted) {

								//if it's the Patient file, stick the patient GUID in a set so we know full patient record deletes
								if (fileType.equals("Admin_Patient")) {
									patientGuidsDeletedOrTooOld.add(patientGuid);
								}

								continue;
							}

							//if it's not the patient file and we refer to a patient that we know
							//has been deleted, then skip this row, since we know we're deleting the entire patient record
							if (patientGuidsDeletedOrTooOld.contains(patientGuid)) {
								continue;
							}

							//if the re-bulk contains a record matching this one, then it's OK
							if (idsInRebulk.contains(uniqueId)) {
								continue;
							}

							//the rebulk won't contain any data for patients that are now too old (i.e. deducted or deceased > 2 yrs ago),
							//so any patient ID in the original files but not in the rebulk can be treated like this and any data for them can be skipped
							if (fileType.equals("Admin_Patient")) {

								//retrieve the Patient and EpisodeOfCare resource for the patient so we can confirm they are deceased or deducted
								ResourceDalI resourceDal = DalProvider.factoryResourceDal();
								UUID patientUuid = IdHelper.getEdsResourceId(serviceUuid, ResourceType.Patient, patientGuid);
								if (patientUuid == null) {
									throw new Exception("Failed to find patient UUID from GUID [" + patientGuid + "]");
								}

								Patient patientResource = (Patient)resourceDal.getCurrentVersionAsResource(serviceUuid, ResourceType.Patient, patientUuid.toString());
								if (patientResource.hasDeceased()) {
									patientGuidsDeletedOrTooOld.add(patientGuid);
									continue;
								}

								UUID episodeUuid = IdHelper.getEdsResourceId(serviceUuid, ResourceType.EpisodeOfCare, patientGuid); //we use the patient GUID for the episode too
								EpisodeOfCare episodeResource = (EpisodeOfCare)resourceDal.getCurrentVersionAsResource(serviceUuid, ResourceType.EpisodeOfCare, episodeUuid.toString());
								if (episodeResource.hasPeriod()
										&& !PeriodHelper.isActive(episodeResource.getPeriod())) {

									patientGuidsDeletedOrTooOld.add(patientGuid);
									continue;
								}
							}

							//create a new CSV record, carrying over the GUIDs from the original but marking as deleted
							String[] newRecord = new String[headers.length];

							for (int j=0; j<newRecord.length; j++) {
								String header = headers[j];
								if (header.equals("PatientGuid")
										|| header.equals("OrganisationGuid")
										|| (!Strings.isNullOrEmpty(guidColumnName)
										&& header.equals(guidColumnName))) {

									String val = record.get(header);
									newRecord[j] = val;

								} else if (header.equals("Deleted")) {
									newRecord[j] = "true";

								} else {
									newRecord[j] = "";
								}
							}

							csvPrinter.printRecord((Object[])newRecord);
							csvPrinter.flush();

							//log out the raw record that's missing from the original
							StringBuffer sb = new StringBuffer();
							sb.append("Record not in re-bulk: ");
							for (int j=0; j<record.size(); j++) {
								if (j > 0) {
									sb.append(",");
								}
								sb.append(record.get(j));
							}
							LOG.info(sb.toString());
						}
					} finally {
						csvParser.close();
					}
				}

				csvPrinter.flush();
				csvPrinter.close();



				//also create a version of the CSV file with just the header and nothing else in
				for (int i=indexDisabled+1; i<indexRebulked; i++) {
					Exchange ex = exchanges.get(i);
					List<String> exchangeFiles = hmExchangeFilesWithoutStoragePrefix.get(ex);
					for (String s: exchangeFiles) {
						String exchangeFileType = findFileType(s);
						if (exchangeFileType.equals(fileType)) {

							String emptyTempFile = FilenameUtils.concat(tempDir, s);

							File dir = new File(emptyTempFile).getParentFile();
							if (!dir.exists()) {
								if (!dir.mkdirs()) {
									throw new Exception("Failed to create directory " + dir);
								}
							}

							fileWriter = new FileWriter(emptyTempFile);
							bufferedWriter = new BufferedWriter(fileWriter);
							csvPrinter = new CSVPrinter(bufferedWriter, EmisCsvToFhirTransformer.CSV_FORMAT.withHeader(headers));
							csvPrinter.flush();
							csvPrinter.close();

							tempFilesCreated.add(s);
							LOG.info("Created empty file " + emptyTempFile);
						}
					}
				}
			}

			//we also need to copy the restored sharing agreement file to replace all the period it was disabled
			String rebulkedSharingAgreementFile = null;
			for (String s: rebulkFiles) {
				String fileType = findFileType(s);
				if (fileType.equals("Agreements_SharingOrganisation")) {
					rebulkedSharingAgreementFile = s;
				}
			}

			for (int i=indexDisabled; i<indexRebulked; i++) {
				Exchange ex = exchanges.get(i);
				List<String> exchangeFiles = hmExchangeFilesWithoutStoragePrefix.get(ex);
				for (String s: exchangeFiles) {
					String exchangeFileType = findFileType(s);
					if (exchangeFileType.equals("Agreements_SharingOrganisation")) {

						String replacementFile = FilenameUtils.concat(tempDir, s);

						InputStream inputStream = FileHelper.readFileFromSharedStorage(rebulkedSharingAgreementFile);
						Files.copy(inputStream, new File(replacementFile).toPath());
						inputStream.close();

						tempFilesCreated.add(s);
					}
				}
			}

			//create a script to copy the files into S3
			List<String> copyScript = new ArrayList<>();
			copyScript.add("#!/bin/bash");
			copyScript.add("");
			for (String s: tempFilesCreated) {
				String localFile = FilenameUtils.concat(tempDir, s);
				copyScript.add("sudo aws s3 cp " + localFile + " s3://discoverysftplanding/endeavour/" + s);
			}

			String scriptFile = FilenameUtils.concat(tempDir, "copy.sh");
			FileUtils.writeLines(new File(scriptFile), copyScript);

			/*continueOrQuit();

			//back up every file where the service was disabled
			for (int i=indexDisabled; i<indexRebulked; i++) {
				Exchange exchange = exchanges.get(i);
				List<String> files = hmExchangeFiles.get(exchange);
				for (String file: files) {
					//first download from S3 to the local temp dir
					InputStream inputStream = FileHelper.readFileFromSharedStorage(file);
					String fileName = FilenameUtils.getName(file);
					String tempPath = FilenameUtils.concat(tempDir, fileName);
					File downloadDestination = new File(tempPath);

					Files.copy(inputStream, downloadDestination.toPath());

					//then write back to S3 in a sub-dir of the original file
					String backupPath = FilenameUtils.getPath(file);
					backupPath = FilenameUtils.concat(backupPath, "Original");
					backupPath = FilenameUtils.concat(backupPath, fileName);

					FileHelper.writeFileToSharedStorage(backupPath, downloadDestination);
					LOG.info("Backed up " + file + "   ->   " + backupPath);

					//delete from temp dir
					downloadDestination.delete();
				}
			}

			continueOrQuit();

			//copy the new CSV files into the dir where it was disabled
			List<String> disabledFiles = hmExchangeFiles.get(exchangeDisabled);
			for (String disabledFile: disabledFiles) {
				String fileType = findFileType(disabledFile);
				if (!isPatientFile(fileType)) {
					continue;
				}

				String tempFile = FilenameUtils.concat(tempDirLast.getAbsolutePath(), fileType + ".csv");
				File f = new File(tempFile);
				if (!f.exists()) {
					throw new Exception("Failed to find expected temp file " + f);
				}

				FileHelper.writeFileToSharedStorage(disabledFile, f);
				LOG.info("Copied " + tempFile + "   ->   " + disabledFile);
			}

			continueOrQuit();

			//empty the patient files for any extracts while the service was disabled
			for (int i=indexDisabled+1; i<indexRebulked; i++) {
				Exchange otherExchangeDisabled = exchanges.get(i);
				List<String> otherDisabledFiles = hmExchangeFiles.get(otherExchangeDisabled);
				for (String otherDisabledFile: otherDisabledFiles) {
					String fileType = findFileType(otherDisabledFile);
					if (!isPatientFile(fileType)) {
						continue;
					}

					String tempFile = FilenameUtils.concat(tempDirEmpty.getAbsolutePath(), fileType + ".csv");
					File f = new File(tempFile);
					if (!f.exists()) {
						throw new Exception("Failed to find expected empty file " + f);
					}

					FileHelper.writeFileToSharedStorage(otherDisabledFile, f);
					LOG.info("Copied " + tempFile + "   ->   " + otherDisabledFile);
				}
			}

			continueOrQuit();

			//copy the content of the sharing agreement file from when it was re-bulked
			for (String rebulkFile: rebulkFiles) {
				String fileType = findFileType(rebulkFile);
				if (fileType.equals("Agreements_SharingOrganisation")) {

					String tempFile = FilenameUtils.concat(tempDir, fileType + ".csv");
					File downloadDestination = new File(tempFile);

					InputStream inputStream = FileHelper.readFileFromSharedStorage(rebulkFile);
					Files.copy(inputStream, downloadDestination.toPath());

					tempFilesCreated.add(tempFile);
				}
			}

			//replace the sharing agreement file for all disabled extracts with the non-disabled one
			for (int i=indexDisabled; i<indexRebulked; i++) {
				Exchange exchange = exchanges.get(i);
				List<String> files = hmExchangeFiles.get(exchange);
				for (String file: files) {
					String fileType = findFileType(file);
					if (fileType.equals("Agreements_SharingOrganisation")) {

						String tempFile = FilenameUtils.concat(tempDir, fileType + ".csv");
						File f = new File(tempFile);
						if (!f.exists()) {
							throw new Exception("Failed to find expected empty file " + f);
						}

						FileHelper.writeFileToSharedStorage(file, f);
						LOG.info("Copied " + tempFile + "   ->   " + file);
					}
				}
			}

			LOG.info("Finished Fixing Disabled Emis Extracts Prior to Re-bulk for service " + serviceId);
			continueOrQuit();

			for (String tempFileCreated: tempFilesCreated) {
				File f = new File(tempFileCreated);
				if (f.exists()) {
					f.delete();
				}
			}*/

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}

	private static String findExtractDate(Exchange exchange, Map<Exchange, List<String>> fileMap) throws Exception {
		List<String> files = fileMap.get(exchange);
		String file = findSharingAgreementFile(files);
		String name = FilenameUtils.getBaseName(file);
		String[] toks = name.split("_");
		return toks[3];
	}

	private static boolean isDisabledInSharingAgreementFile(Exchange exchange, Map<Exchange, List<String>> fileMap) throws Exception {
		List<String> files = fileMap.get(exchange);
		String file = findSharingAgreementFile(files);

		InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(file);
		CSVParser csvParser = new CSVParser(reader, EmisCsvToFhirTransformer.CSV_FORMAT);
		try {
			Iterator<CSVRecord> iterator = csvParser.iterator();
			CSVRecord record = iterator.next();

			String s = record.get("Disabled");
			boolean disabled = Boolean.parseBoolean(s);
			return disabled;

		} finally {
			csvParser.close();
		}
	}

	private static void continueOrQuit() throws Exception {
		LOG.info("Enter y to continue, anything else to quit");

		byte[] bytes = new byte[10];
		System.in.read(bytes);
		char c = (char)bytes[0];
		if (c != 'y' && c != 'Y') {
			System.out.println("Read " + c);
			System.exit(1);
		}
	}

	private static String getGuidColumnName(String fileType) {
		if (fileType.equals("Admin_Patient")) {
			//patient file just has patient GUID, nothing extra
			return null;

		} else if (fileType.equals("CareRecord_Consultation")) {
			return "ConsultationGuid";

		} else if (fileType.equals("CareRecord_Diary")) {
			return "DiaryGuid";

		} else if (fileType.equals("CareRecord_Observation")) {
			return "ObservationGuid";

		} else if (fileType.equals("CareRecord_Problem")) {
			//there is no separate problem GUID, as it's just a modified observation
			return "ObservationGuid";

		} else if (fileType.equals("Prescribing_DrugRecord")) {
			return "DrugRecordGuid";

		} else if (fileType.equals("Prescribing_IssueRecord")) {
			return "IssueRecordGuid";

		} else {
			throw new IllegalArgumentException(fileType);
		}
	}

	private static String findFileType(String filePath) {
		String fileName = FilenameUtils.getName(filePath);
		String[] toks = fileName.split("_");
		String domain = toks[1];
		String name = toks[2];

		return domain + "_" + name;
	}

	private static boolean isPatientFile(String fileType) {
		if (fileType.equals("Admin_Patient")
				|| fileType.equals("CareRecord_Consultation")
				|| fileType.equals("CareRecord_Diary")
				|| fileType.equals("CareRecord_Observation")
				|| fileType.equals("CareRecord_Problem")
				|| fileType.equals("Prescribing_DrugRecord")
				|| fileType.equals("Prescribing_IssueRecord")) {
			//note the referral file doesn't have a Deleted column, so isn't in this list

			return true;

		} else {
			return false;
		}
	}

	private static String findSharingAgreementFile(List<String> files) throws Exception {

		for (String file : files) {
			String fileType = findFileType(file);
			if (fileType.equals("Agreements_SharingOrganisation")) {
				return file;
			}
		}

		throw new Exception("Failed to find sharing agreement file in " + files.get(0));
	}



	private static void testSlack() {
		LOG.info("Testing slack");

		try {
			SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, "Test Message from Queue Reader");
			LOG.info("Finished testing slack");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}

	/*private static void postToInboundFromFile(UUID serviceId, UUID systemId, String filePath) {

		try {

			ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
			ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();

			Service service = serviceDalI.getById(serviceId);
			LOG.info("Posting to inbound exchange for " + service.getName() + " from file " + filePath);

			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);

			int count = 0;
			List<UUID> exchangeIdBatch = new ArrayList<>();

			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}

				UUID exchangeId = UUID.fromString(line);

				//update the transform audit, so EDS UI knows we've re-queued this exchange
				ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
				if (audit != null
						&& !audit.isResubmitted()) {
					audit.setResubmitted(true);
					auditRepository.save(audit);
				}

				count ++;
				exchangeIdBatch.add(exchangeId);
				if (exchangeIdBatch.size() >= 1000) {
					QueueHelper.postToExchange(exchangeIdBatch, "EdsInbound", null, false);
					exchangeIdBatch = new ArrayList<>();
					LOG.info("Done " + count);
				}
			}

			if (!exchangeIdBatch.isEmpty()) {
				QueueHelper.postToExchange(exchangeIdBatch, "EdsInbound", null, false);
				LOG.info("Done " + count);
			}

			br.close();

		} catch (Exception ex) {
			LOG.error("", ex);
		}

		LOG.info("Finished Posting to inbound for " + serviceId);
	}*/

	/*private static void postToInbound(UUID serviceId, boolean all) {
		LOG.info("Posting to inbound for " + serviceId);

		try {

			ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
			ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();

			Service service = serviceDalI.getById(serviceId);

			List<UUID> systemIds = findSystemIds(service);
			UUID systemId = systemIds.get(0);

			ExchangeTransformErrorState errorState = auditRepository.getErrorState(serviceId, systemId);

			for (UUID exchangeId: errorState.getExchangeIdsInError()) {

				//update the transform audit, so EDS UI knows we've re-queued this exchange
				ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);

				//skip any exchange IDs we've already re-queued up to be processed again
				if (audit.isResubmitted()) {
					LOG.debug("Not re-posting " + audit.getExchangeId() + " as it's already been resubmitted");
					continue;
				}

				LOG.debug("Re-posting " + audit.getExchangeId());
				audit.setResubmitted(true);
				auditRepository.save(audit);

				//then re-submit the exchange to Rabbit MQ for the queue reader to pick up
				QueueHelper.postToExchange(exchangeId, "EdsInbound", null, false);

				if (!all) {
					LOG.info("Posted first exchange, so stopping");
					break;
				}
			}

		} catch (Exception ex) {
			LOG.error("", ex);
		}

		LOG.info("Finished Posting to inbound for " + serviceId);
	}*/

	/*private static void fixPatientSearch(String serviceId) {
		LOG.info("Fixing patient search for " + serviceId);

		try {

			UUID serviceUuid = UUID.fromString(serviceId);

			ExchangeDalI exchangeDalI = DalProvider.factoryExchangeDal();
			ExchangeBatchDalI exchangeBatchDalI = DalProvider.factoryExchangeBatchDal();
			ResourceDalI resourceDalI = DalProvider.factoryResourceDal();
			PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
			ParserPool parser = new ParserPool();

			Set<UUID> patientsDone = new HashSet<>();

			List<UUID> exchanges = exchangeDalI.getExchangeIdsForService(serviceUuid);
			LOG.info("Found " + exchanges.size() + " exchanges");

			for (UUID exchangeId: exchanges) {
				List<ExchangeBatch> batches = exchangeBatchDalI.retrieveForExchangeId(exchangeId);
				LOG.info("Found " + batches.size() + " batches in exchange " + exchangeId);

				for (ExchangeBatch batch: batches) {
					UUID patientId = batch.getEdsPatientId();
					if (patientId == null) {
						continue;
					}

					if (patientsDone.contains(patientId)) {
						continue;
					}

					ResourceWrapper wrapper = resourceDalI.getCurrentVersion(serviceUuid, ResourceType.Patient.toString(), patientId);
					if (wrapper != null) {
						String json = wrapper.getResourceData();
						if (!Strings.isNullOrEmpty(json)) {

							Patient fhirPatient = (Patient) parser.parse(json);
							UUID systemUuid = wrapper.getSystemId();

							patientSearchDal.update(serviceUuid, systemUuid, fhirPatient);
						}
					}

					patientsDone.add(patientId);

					if (patientsDone.size() % 1000 == 0) {
						LOG.info("Done " + patientsDone.size());
					}
				}

			}

		} catch (Exception ex) {
			LOG.error("", ex);
		}

		LOG.info("Finished fixing patient search for " + serviceId);
	}*/

	private static void runSql(String host, String username, String password, String sqlFile) {
		LOG.info("Running SQL on " + host + " from " + sqlFile);

		Connection conn = null;
		Statement statement = null;


		try {
			File f = new File(sqlFile);
			if (!f.exists()) {
				LOG.error("" + f + " doesn't exist");
				return;
			}

			List<String> lines = FileUtils.readLines(f);
			/*String combined = String.join("\n", lines);

			LOG.info("Going to run SQL");
			LOG.info(combined);*/

			//load driver
			Class.forName("com.mysql.cj.jdbc.Driver");

			//create connection
			Properties props = new Properties();
			props.setProperty("user", username);
			props.setProperty("password", password);

			conn = DriverManager.getConnection(host, props);
			LOG.info("Opened connection");
			statement = conn.createStatement();

			long totalStart = System.currentTimeMillis();

			for (String sql: lines) {

				sql = sql.trim();

				if (sql.startsWith("--")
						|| sql.startsWith("/*")
						|| Strings.isNullOrEmpty(sql)) {
					continue;
				}

				LOG.info("");
				LOG.info(sql);

				long start = System.currentTimeMillis();

				boolean hasResultSet = statement.execute(sql);

				long end = System.currentTimeMillis();
				LOG.info("SQL took " + (end - start) + "ms");

				if (hasResultSet) {

					while (true) {
						ResultSet rs = statement.getResultSet();
						int cols = rs.getMetaData().getColumnCount();

						List<String> colHeaders = new ArrayList<>();
						for (int i = 0; i < cols; i++) {
							String header = rs.getMetaData().getColumnName(i + 1);
							colHeaders.add(header);
						}
						String colHeaderStr = String.join(", ", colHeaders);
						LOG.info(colHeaderStr);

						while (rs.next()) {
							List<String> row = new ArrayList<>();
							for (int i = 0; i < cols; i++) {
								Object o = rs.getObject(i + 1);
								if (rs.wasNull()) {
									row.add("<null>");
								} else {
									row.add(o.toString());
								}
							}
							String rowStr = String.join(", ", row);
							LOG.info(rowStr);
						}

						if (!statement.getMoreResults()) {
							break;
						}
					}

				} else {
					int updateCount = statement.getUpdateCount();
					LOG.info("Updated " + updateCount + " Row(s)");
				}
			}

			long totalEnd = System.currentTimeMillis();
			LOG.info("");
			LOG.info("Total time taken " + (totalEnd - totalStart) + "ms");

		} catch (Throwable t) {
			LOG.error("", t);
		} finally {

			if (statement != null) {
				try {
					statement.close();
				} catch (Exception ex) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception ex) {

				}
			}
			LOG.info("Closed connection");
		}

		LOG.info("Finished Testing DB Size Limit");
	}



	/*private static void fixExchangeBatches() {
		LOG.info("Starting Fixing Exchange Batches");

		try {

			ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
			ExchangeDalI exchangeDalI = DalProvider.factoryExchangeDal();
			ExchangeBatchDalI exchangeBatchDalI = DalProvider.factoryExchangeBatchDal();
			ResourceDalI resourceDalI = DalProvider.factoryResourceDal();

			List<Service> services = serviceDalI.getAll();
			for (Service service: services) {
				LOG.info("Doing " + service.getName());

				List<UUID> exchangeIds = exchangeDalI.getExchangeIdsForService(service.getId());
				for (UUID exchangeId: exchangeIds) {
					LOG.info("   Exchange " + exchangeId);

					List<ExchangeBatch> exchangeBatches = exchangeBatchDalI.retrieveForExchangeId(exchangeId);
					for (ExchangeBatch exchangeBatch: exchangeBatches) {

						if (exchangeBatch.getEdsPatientId() != null) {
							continue;
						}

						List<ResourceWrapper> resources = resourceDalI.getResourcesForBatch(exchangeBatch.getBatchId());
						if (resources.isEmpty()) {
							continue;
						}

						ResourceWrapper first = resources.get(0);
						UUID patientId = first.getPatientId();
						if (patientId != null) {
							exchangeBatch.setEdsPatientId(patientId);
							exchangeBatchDalI.save(exchangeBatch);
							LOG.info("Fixed batch " + exchangeBatch.getBatchId() + " -> " + exchangeBatch.getEdsPatientId());
						}
					}
				}
			}

			LOG.info("Finished Fixing Exchange Batches");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/**
	 * exports ADT Encounters for patients based on a CSV file produced using the below SQL
	 --USE EDS DATABASE

	 -- barts b5a08769-cbbe-4093-93d6-b696cd1da483
	 -- homerton 962d6a9a-5950-47ac-9e16-ebee56f9507a

	 create table adt_patients (
	 service_id character(36),
	 system_id character(36),
	 nhs_number character varying(10),
	 patient_id character(36)
	 );

	 -- delete from adt_patients;

	 select * from patient_search limit 10;
	 select * from patient_link limit 10;

	 insert into adt_patients
	 select distinct ps.service_id, ps.system_id, ps.nhs_number, ps.patient_id
	 from patient_search ps
	 join patient_link pl
	 on pl.patient_id = ps.patient_id
	 join patient_link pl2
	 on pl.person_id = pl2.person_id
	 join patient_search ps2
	 on ps2.patient_id = pl2.patient_id
	 where
	 ps.service_id IN ('b5a08769-cbbe-4093-93d6-b696cd1da483', '962d6a9a-5950-47ac-9e16-ebee56f9507a')
	 and ps2.service_id NOT IN ('b5a08769-cbbe-4093-93d6-b696cd1da483', '962d6a9a-5950-47ac-9e16-ebee56f9507a');


	 select count(1) from adt_patients limit 100;
	 select * from adt_patients limit 100;




	 ---MOVE TABLE TO HL7 RECEIVER DB

	 select count(1) from adt_patients;

	 -- top 1000 patients with messages

	 select * from mapping.resource_uuid where resource_type = 'Patient' limit 10;

	 select * from log.message limit 10;

	 create table adt_patient_counts (
	 nhs_number character varying(100),
	 count int
	 );

	 insert into adt_patient_counts
	 select pid1, count(1)
	 from log.message
	 where pid1 is not null
	 and pid1 <> ''
	 group by pid1;

	 select * from adt_patient_counts order by count desc limit 100;

	 alter table adt_patients
	 add count int;

	 update adt_patients
	 set count = adt_patient_counts.count
	 from adt_patient_counts
	 where adt_patients.nhs_number = adt_patient_counts.nhs_number;

	 select count(1) from adt_patients where nhs_number is null;

	 select * from adt_patients
	 where nhs_number is not null
	 and count is not null
	 order by count desc limit 1000;
	 */
	/*private static void exportHl7Encounters(String sourceCsvPath, String outputPath) {
		LOG.info("Exporting HL7 Encounters from " + sourceCsvPath + " to " + outputPath);

		try {

			File sourceFile = new File(sourceCsvPath);
			CSVParser csvParser = CSVParser.parse(sourceFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());

			//"service_id","system_id","nhs_number","patient_id","count"

			int count = 0;
			HashMap<UUID, List<UUID>> serviceAndSystemIds = new HashMap<>();
			HashMap<UUID, Integer> patientIds = new HashMap<>();

			Iterator<CSVRecord> csvIterator = csvParser.iterator();
			while (csvIterator.hasNext()) {
				CSVRecord csvRecord = csvIterator.next();
				count ++;

				String serviceId = csvRecord.get("service_id");
				String systemId = csvRecord.get("system_id");
				String patientId = csvRecord.get("patient_id");

				UUID serviceUuid = UUID.fromString(serviceId);
				List<UUID> systemIds = serviceAndSystemIds.get(serviceUuid);
				if (systemIds == null) {
					systemIds = new ArrayList<>();
					serviceAndSystemIds.put(serviceUuid, systemIds);
				}
				systemIds.add(UUID.fromString(systemId));

				patientIds.put(UUID.fromString(patientId), new Integer(count));
			}

			csvParser.close();

			ExchangeDalI exchangeDalI = DalProvider.factoryExchangeDal();
			ResourceDalI resourceDalI = DalProvider.factoryResourceDal();
			ExchangeBatchDalI exchangeBatchDalI = DalProvider.factoryExchangeBatchDal();
			ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
			ParserPool parser = new ParserPool();

			Map<Integer, List<Object[]>> patientRows = new HashMap<>();
			SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			for (UUID serviceId: serviceAndSystemIds.keySet()) {
				//List<UUID> systemIds = serviceAndSystemIds.get(serviceId);

				Service service = serviceDalI.getById(serviceId);
				String serviceName = service.getName();
				LOG.info("Doing service " + serviceId + " " + serviceName);

				List<UUID> exchangeIds = exchangeDalI.getExchangeIdsForService(serviceId);
				LOG.info("Got " + exchangeIds.size() + " exchange IDs to scan");
				int exchangeCount = 0;

				for (UUID exchangeId: exchangeIds) {

					exchangeCount ++;
					if (exchangeCount % 1000 == 0) {
						LOG.info("Done " + exchangeCount + " exchanges");
					}

					List<ExchangeBatch> exchangeBatches = exchangeBatchDalI.retrieveForExchangeId(exchangeId);
					for (ExchangeBatch exchangeBatch: exchangeBatches) {
						UUID patientId = exchangeBatch.getEdsPatientId();
						if (patientId != null
								&& !patientIds.containsKey(patientId)) {
							continue;
						}

						Integer patientIdInt = patientIds.get(patientId);

						//get encounters for exchange batch
						UUID batchId = exchangeBatch.getBatchId();
						List<ResourceWrapper> resourceWrappers = resourceDalI.getResourcesForBatch(serviceId, batchId);
						for (ResourceWrapper resourceWrapper: resourceWrappers) {
							if (resourceWrapper.isDeleted()) {
								continue;
							}
							String resourceType = resourceWrapper.getResourceType();
							if (!resourceType.equals(ResourceType.Encounter.toString())) {
								continue;
							}

							LOG.info("Processing " + resourceWrapper.getResourceType() + " " + resourceWrapper.getResourceId());
							String json = resourceWrapper.getResourceData();
							Encounter fhirEncounter = (Encounter)parser.parse(json);

							Date date = null;
							if (fhirEncounter.hasPeriod()) {
								Period period = fhirEncounter.getPeriod();
								if (period.hasStart()) {
									date = period.getStart();
								}
							}

							String episodeId = null;
							if (fhirEncounter.hasEpisodeOfCare()) {
								Reference episodeReference = fhirEncounter.getEpisodeOfCare().get(0);
								ReferenceComponents comps = ReferenceHelper.getReferenceComponents(episodeReference);
								EpisodeOfCare fhirEpisode = (EpisodeOfCare)resourceDalI.getCurrentVersionAsResource(serviceId, comps.getResourceType(), comps.getId());
								if (fhirEpisode != null) {
									if (fhirEpisode.hasIdentifier()) {
										episodeId = IdentifierHelper.findIdentifierValue(fhirEpisode.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_BARTS_FIN_EPISODE_ID);

										if (Strings.isNullOrEmpty(episodeId)) {
											episodeId = IdentifierHelper.findIdentifierValue(fhirEpisode.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_HOMERTON_FIN_EPISODE_ID);
										}
									}
								}
							}



							String adtType = null;
							String adtCode = null;
							Extension extension = ExtensionConverter.findExtension(fhirEncounter, FhirExtensionUri.HL7_MESSAGE_TYPE);

							if (extension != null) {
								CodeableConcept codeableConcept = (CodeableConcept) extension.getValue();
								Coding hl7MessageTypeCoding = CodeableConceptHelper.findCoding(codeableConcept, FhirUri.CODE_SYSTEM_HL7V2_MESSAGE_TYPE);
								if (hl7MessageTypeCoding != null) {
									adtType = hl7MessageTypeCoding.getDisplay();
									adtCode = hl7MessageTypeCoding.getCode();
								}

							} else {
								//for older formats of the transformed resources, the HL7 message type can only be found from the raw original exchange body
								try {
									Exchange exchange = exchangeDalI.getExchange(exchangeId);
									String exchangeBody = exchange.getBody();
									Bundle bundle = (Bundle) FhirResourceHelper.deserialiseResouce(exchangeBody);
									for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {
										if (entry.getResource() != null
												&& entry.getResource() instanceof MessageHeader) {

											MessageHeader header = (MessageHeader)entry.getResource();
											if (header.hasEvent()) {
												Coding coding = header.getEvent();
												adtType = coding.getDisplay();
												adtCode = coding.getCode();
											}
										}
									}
								} catch (Exception ex) {
									//if the exchange body isn't a FHIR bundle, then we'll get an error by treating as such, so just ignore them
								}
							}

							String cls = null;
							if (fhirEncounter.hasClass_()) {
								Encounter.EncounterClass encounterClass = fhirEncounter.getClass_();
								if (encounterClass == Encounter.EncounterClass.OTHER
										&& fhirEncounter.hasClass_Element()
										&& fhirEncounter.getClass_Element().hasExtension()) {

									for (Extension classExtension: fhirEncounter.getClass_Element().getExtension()) {
										if (classExtension.getUrl().equals(FhirExtensionUri.ENCOUNTER_CLASS)) {
											//not 100% of the type of the value, so just append to a String
											cls = "" + classExtension.getValue();
										}
									}
								}

								if (Strings.isNullOrEmpty(cls)) {
									cls = encounterClass.toCode();
								}
							}

							String type = null;
							if (fhirEncounter.hasType()) {
								//only seem to ever have one type
								CodeableConcept codeableConcept = fhirEncounter.getType().get(0);
								type = codeableConcept.getText();
							}

							String status = null;
							if (fhirEncounter.hasStatus()) {
								Encounter.EncounterState encounterState = fhirEncounter.getStatus();
								status = encounterState.toCode();
							}

							String location = null;
							String locationType = null;
							if (fhirEncounter.hasLocation()) {
								//first location is always the current location
								Encounter.EncounterLocationComponent encounterLocation = fhirEncounter.getLocation().get(0);
								if (encounterLocation.hasLocation()) {
									Reference locationReference = encounterLocation.getLocation();
									ReferenceComponents comps = ReferenceHelper.getReferenceComponents(locationReference);
									Location fhirLocation = (Location)resourceDalI.getCurrentVersionAsResource(serviceId, comps.getResourceType(), comps.getId());
									if (fhirLocation != null) {
										if (fhirLocation.hasName()) {
											location = fhirLocation.getName();
										}
										if (fhirLocation.hasType()) {
											CodeableConcept typeCodeableConcept = fhirLocation.getType();
											if (typeCodeableConcept.hasCoding()) {
												Coding coding = typeCodeableConcept.getCoding().get(0);
												locationType = coding.getDisplay();
											}
										}
									}
								}
							}

							String clinician = null;

							if (fhirEncounter.hasParticipant()) {
								//first participant seems to be the interesting one
								Encounter.EncounterParticipantComponent encounterParticipant = fhirEncounter.getParticipant().get(0);
								if (encounterParticipant.hasIndividual()) {
									Reference practitionerReference = encounterParticipant.getIndividual();
									ReferenceComponents comps = ReferenceHelper.getReferenceComponents(practitionerReference);
									Practitioner fhirPractitioner = (Practitioner)resourceDalI.getCurrentVersionAsResource(serviceId, comps.getResourceType(), comps.getId());
									if (fhirPractitioner != null) {
										if (fhirPractitioner.hasName()) {
											HumanName name = fhirPractitioner.getName();
											clinician = name.getText();
											if (Strings.isNullOrEmpty(clinician)) {
												clinician = "";

												for (StringType s: name.getPrefix()) {
													clinician += s.getValueNotNull();
													clinician += " ";
												}
												for (StringType s: name.getGiven()) {
													clinician += s.getValueNotNull();
													clinician += " ";
												}
												for (StringType s: name.getFamily()) {
													clinician += s.getValueNotNull();
													clinician += " ";
												}
												clinician = clinician.trim();
											}
										}
									}
								}
							}

							Object[] row = new Object[12];

							row[0] = serviceName;
							row[1] = patientIdInt.toString();
							row[2] = sdfOutput.format(date);
							row[3] = episodeId;
							row[4] = adtCode;
							row[5] = adtType;
							row[6] = cls;
							row[7] = type;
							row[8] = status;
							row[9] = location;
							row[10] = locationType;
							row[11] = clinician;

							List<Object[]> rows = patientRows.get(patientIdInt);
							if (rows == null) {
								rows = new ArrayList<>();
								patientRows.put(patientIdInt, rows);
							}
							rows.add(row);
						}
					}
				}
			}


			String[] outputColumnHeaders = new String[] {"Source", "Patient", "Date", "Episode ID", "ADT Message Code", "ADT Message Type", "Class", "Type", "Status", "Location", "Location Type", "Clinician"};

			FileWriter fileWriter = new FileWriter(outputPath);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			CSVFormat format = CSVFormat.DEFAULT
					.withHeader(outputColumnHeaders)
					.withQuote('"');
			CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, format);

			for (int i=0; i <= count; i++) {
				Integer patientIdInt = new Integer(i);
				List<Object[]> rows = patientRows.get(patientIdInt);
				if (rows != null) {
					for (Object[] row: rows) {
						csvPrinter.printRecord(row);
					}
				}
			}

			csvPrinter.close();
			bufferedWriter.close();

		} catch (Exception ex) {
			LOG.error("", ex);
		}

		LOG.info("Finished Exporting Encounters from " + sourceCsvPath + " to " + outputPath);
	}*/

	/*private static void registerShutdownHook() {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				LOG.info("");
				try {
					Thread.sleep(5000);
				} catch (Throwable ex) {
					LOG.error("", ex);
				}
				LOG.info("Done");
			}
		});
	}*/


	private static void findEmisStartDates(String path, String outputPath) {
		LOG.info("Finding EMIS Start Dates in " + path + ", writing to " + outputPath);

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss");

			Map<String, Date> startDates = new HashMap<>();
			Map<String, String> servers = new HashMap<>();

			Map<String, String> names = new HashMap<>();
			Map<String, String> odsCodes = new HashMap<>();
			Map<String, String> cdbNumbers = new HashMap<>();
			Map<String, Set<String>> distinctPatients = new HashMap<>();

			File root = new File(path);
			for (File sftpRoot: root.listFiles()) {
				LOG.info("Checking " + sftpRoot);

				Map<Date, File> extracts = new HashMap<>();
				List<Date> extractDates = new ArrayList<>();

				for (File extractRoot: sftpRoot.listFiles()) {
					Date d = sdf.parse(extractRoot.getName());

					//LOG.info("" + extractRoot.getName() + " -> " + d);

					extracts.put(d, extractRoot);
					extractDates.add(d);
				}

				Collections.sort(extractDates);

				for (Date extractDate: extractDates) {
					File extractRoot = extracts.get(extractDate);
					LOG.info("Checking " + extractRoot);

					//read the sharing agreements file
					//e.g. 291_Agreements_SharingOrganisation_20150211164536_45E7CD20-EE37-41AB-90D6-DC9D4B03D102.csv
					File sharingAgreementsFile = null;
					for (File f: extractRoot.listFiles()) {
						String name = f.getName().toLowerCase();
						if (name.indexOf("agreements_sharingorganisation") > -1
								&& name.endsWith(".csv")) {
							sharingAgreementsFile = f;
							break;
						}
					}

					if (sharingAgreementsFile == null) {
						LOG.info("Null agreements file for " + extractRoot);
						continue;
					}

					CSVParser csvParser = CSVParser.parse(sharingAgreementsFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
					try {
						Iterator<CSVRecord> csvIterator = csvParser.iterator();

						while (csvIterator.hasNext()) {
							CSVRecord csvRecord = csvIterator.next();

							String orgGuid = csvRecord.get("OrganisationGuid");
							String activated = csvRecord.get("IsActivated");
							String disabled = csvRecord.get("Disabled");

							servers.put(orgGuid, sftpRoot.getName());

							if (activated.equalsIgnoreCase("true")) {
								if (disabled.equalsIgnoreCase("false")) {

									Date d = sdf.parse(extractRoot.getName());
									Date existingDate = startDates.get(orgGuid);
									if (existingDate == null) {
										startDates.put(orgGuid, d);
									}

								} else {
									if (startDates.containsKey(orgGuid)) {
										startDates.put(orgGuid, null);
									}
								}
							}
						}
					} finally {
						csvParser.close();
					}

					//go through orgs file to get name, ods and cdb codes
					File orgsFile = null;
					for (File f: extractRoot.listFiles()) {
						String name = f.getName().toLowerCase();
						if (name.indexOf("admin_organisation_") > -1
								&& name.endsWith(".csv")) {
							orgsFile = f;
							break;
						}
					}

					csvParser = CSVParser.parse(orgsFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
					try {
						Iterator<CSVRecord> csvIterator = csvParser.iterator();

						while (csvIterator.hasNext()) {
							CSVRecord csvRecord = csvIterator.next();

							String orgGuid = csvRecord.get("OrganisationGuid");
							String name = csvRecord.get("OrganisationName");
							String odsCode = csvRecord.get("ODSCode");
							String cdb = csvRecord.get("CDB");

							names.put(orgGuid, name);
							odsCodes.put(orgGuid, odsCode);
							cdbNumbers.put(orgGuid, cdb);
						}
					} finally {
						csvParser.close();
					}

					//go through patients file to get count
					File patientFile = null;
					for (File f: extractRoot.listFiles()) {
						String name = f.getName().toLowerCase();
						if (name.indexOf("admin_patient_") > -1
								&& name.endsWith(".csv")) {
							patientFile = f;
							break;
						}
					}

					csvParser = CSVParser.parse(patientFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
					try {
						Iterator<CSVRecord> csvIterator = csvParser.iterator();

						while (csvIterator.hasNext()) {
							CSVRecord csvRecord = csvIterator.next();

							String orgGuid = csvRecord.get("OrganisationGuid");
							String patientGuid = csvRecord.get("PatientGuid");
							String deleted = csvRecord.get("Deleted");

							Set<String> distinctPatientSet = distinctPatients.get(orgGuid);
							if (distinctPatientSet == null) {
								distinctPatientSet = new HashSet<>();
								distinctPatients.put(orgGuid, distinctPatientSet);
							}

							if (deleted.equalsIgnoreCase("true")) {
								distinctPatientSet.remove(patientGuid);
							} else {
								distinctPatientSet.add(patientGuid);
							}
						}
					} finally {
						csvParser.close();
					}
				}
			}

			SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd");

			StringBuilder sb = new StringBuilder();
			sb.append("Name,OdsCode,CDB,OrgGuid,StartDate,Server,Patients");

			for (String orgGuid: startDates.keySet()) {
				Date startDate = startDates.get(orgGuid);
				String server = servers.get(orgGuid);
				String name = names.get(orgGuid);
				String odsCode = odsCodes.get(orgGuid);
				String cdbNumber = cdbNumbers.get(orgGuid);
				Set<String> distinctPatientSet = distinctPatients.get(orgGuid);

				String startDateDesc = null;
				if (startDate != null) {
					startDateDesc = sdfOutput.format(startDate);
				}

				Long countDistinctPatients = null;
				if (distinctPatientSet != null) {
					countDistinctPatients = new Long(distinctPatientSet.size());
				}

				sb.append("\n");
				sb.append("\"" + name + "\"");
				sb.append(",");
				sb.append("\"" + odsCode + "\"");
				sb.append(",");
				sb.append("\"" + cdbNumber + "\"");
				sb.append(",");
				sb.append("\"" + orgGuid + "\"");
				sb.append(",");
				sb.append(startDateDesc);
				sb.append(",");
				sb.append("\"" + server + "\"");
				sb.append(",");
				sb.append(countDistinctPatients);
			}

			LOG.info(sb.toString());

			FileUtils.writeStringToFile(new File(outputPath), sb.toString());

		} catch (Exception ex) {
			LOG.error("", ex);
		}

		LOG.info("Finished Finding Start Dates in " + path + ", writing to " + outputPath);
	}

	private static void findEncounterTerms(String path, String outputPath) {
		LOG.info("Finding Encounter Terms from " + path);

		Map<String, Long> hmResults = new HashMap<>();

		//source term, source term snomed ID, source term snomed term - count

		try {
			File root = new File(path);
			File[] files = root.listFiles();
			for (File readerRoot: files) { //emis001
				LOG.info("Finding terms in " + readerRoot);

				//first read in all the coding files to build up our map of codes
				Map<String, String> hmCodes = new HashMap<>();

				for (File dateFolder: readerRoot.listFiles()) {
					LOG.info("Looking for codes in " + dateFolder);

					File f = findFile(dateFolder, "Coding_ClinicalCode");
					if (f == null) {
						LOG.error("Failed to find coding file in " + dateFolder.getAbsolutePath());
						continue;
					}

					CSVParser csvParser = CSVParser.parse(f, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
					Iterator<CSVRecord> csvIterator = csvParser.iterator();

					while (csvIterator.hasNext()) {
						CSVRecord csvRecord = csvIterator.next();

						String codeId = csvRecord.get("CodeId");
						String term = csvRecord.get("Term");
						String snomed = csvRecord.get("SnomedCTConceptId");

						hmCodes.put(codeId, snomed + ",\"" + term + "\"");
					}

					csvParser.close();
				}

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date cutoff = dateFormat.parse("2017-01-01");

				//now process the consultation files themselves
				for (File dateFolder: readerRoot.listFiles()) {
					LOG.info("Looking for consultations in " + dateFolder);

					File f = findFile(dateFolder, "CareRecord_Consultation");
					if (f == null) {
						LOG.error("Failed to find consultation file in " + dateFolder.getAbsolutePath());
						continue;
					}

					CSVParser csvParser = CSVParser.parse(f, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
					Iterator<CSVRecord> csvIterator = csvParser.iterator();

					while (csvIterator.hasNext()) {
						CSVRecord csvRecord = csvIterator.next();

						String term = csvRecord.get("ConsultationSourceTerm");
						String codeId = csvRecord.get("ConsultationSourceCodeId");

						if (Strings.isNullOrEmpty(term)
								&& Strings.isNullOrEmpty(codeId)) {
							continue;
						}

						String date = csvRecord.get("EffectiveDate");
						if (Strings.isNullOrEmpty(date)) {
							continue;
						}

						Date d = dateFormat.parse(date);
						if (d.before(cutoff)) {
							continue;
						}

						String line = "\"" + term + "\",";

						if (!Strings.isNullOrEmpty(codeId)) {

							String codeLookup = hmCodes.get(codeId);
							if (codeLookup == null) {
								LOG.error("Failed to find lookup for codeID " + codeId);
								continue;
							}

							line += codeLookup;

						} else {

							line += ",";
						}

						Long count = hmResults.get(line);
						if (count == null) {
							count = new Long(1);
						} else {
							count = new Long(count.longValue() + 1);
						}
						hmResults.put(line, count);
					}

					csvParser.close();
				}


			}

			//save results to file
			StringBuilder output = new StringBuilder();
			output.append("\"consultation term\",\"snomed concept ID\",\"snomed term\",\"count\"");
			output.append("\r\n");

			for (String line: hmResults.keySet()) {
				Long count = hmResults.get(line);
				String combined = line + "," + count;

				output.append(combined);
				output.append("\r\n");
			}
			LOG.info("FInished");
			LOG.info(output.toString());

			FileUtils.writeStringToFile(new File(outputPath), output.toString());

			LOG.info("written output to " + outputPath);


		} catch (Exception ex) {
			LOG.error("", ex);
		}

		LOG.info("Finished finding Encounter Terms from " + path);
	}

	private static File findFile(File root, String token) throws Exception {
		for (File f: root.listFiles()) {
			String s = f.getName();
			if (s.indexOf(token) > -1) {
				return f;
			}
		}

		return null;
	}

	/*private static void populateProtocolQueue(String serviceIdStr, String startingExchangeId) {
		LOG.info("Starting Populating Protocol Queue for " + serviceIdStr);

		ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
		ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();

		if (serviceIdStr.equalsIgnoreCase("All")) {
			serviceIdStr = null;
		}

		try {

			List<Service> services = new ArrayList<>();
			if (Strings.isNullOrEmpty(serviceIdStr)) {
				services = serviceRepository.getAll();
			} else {
				UUID serviceId = UUID.fromString(serviceIdStr);
				Service service = serviceRepository.getById(serviceId);
				services.add(service);
			}

			for (Service service: services) {

				List<UUID> exchangeIds = auditRepository.getExchangeIdsForService(service.getId());
				LOG.info("Found " + exchangeIds.size() + " exchangeIds for " + service.getName());

				if (startingExchangeId != null) {
					UUID startingExchangeUuid = UUID.fromString(startingExchangeId);
					if (exchangeIds.contains(startingExchangeUuid)) {
						//if in the list, remove everything up to and including the starting exchange
						int index = exchangeIds.indexOf(startingExchangeUuid);
						LOG.info("Found starting exchange " + startingExchangeId + " at " + index + " so removing up to this point");
						for (int i=index; i>=0; i--) {
							exchangeIds.remove(i);
						}
						startingExchangeId = null;

					} else {
						//if not in the list, skip all these exchanges
						LOG.info("List doesn't contain starting exchange " + startingExchangeId + " so skipping");
						continue;
					}
				}

				QueueHelper.postToExchange(exchangeIds, "edsProtocol", null, true);
			}

		} catch (Exception ex) {
			LOG.error("", ex);
		}

		LOG.info("Finished Populating Protocol Queue for " + serviceIdStr);
	}*/

	/*private static void findDeletedOrgs() {
		LOG.info("Starting finding deleted orgs");

		ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
		ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();

		List<Service> services = new ArrayList<>();
		try {
			for (Service service: serviceRepository.getAll()) {
				services.add(service);
			}
		} catch (Exception ex) {
			LOG.error("", ex);
		}

		services.sort((o1, o2) -> {
			String name1 = o1.getName();
			String name2 = o2.getName();
			return name1.compareToIgnoreCase(name2);
		});

		for (Service service: services) {

			try {
				UUID serviceUuid = service.getId();
				List<Exchange> exchangeByServices = auditRepository.getExchangesByService(serviceUuid, 1, new Date(0), new Date());

				LOG.info("Service: " + service.getName() + " " + service.getLocalId());

				if (exchangeByServices.isEmpty()) {
					LOG.info("    no exchange found!");
					continue;
				}


				Exchange exchangeByService = exchangeByServices.get(0);
				UUID exchangeId = exchangeByService.getId();
				Exchange exchange = auditRepository.getExchange(exchangeId);

				Map<String, String> headers = exchange.getHeaders();

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
					exchangeId = exchangeByService.getId();
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
	}*/

	private static int countBatches(UUID exchangeId, UUID serviceId, UUID systemId) throws Exception {
		int batches = 0;
		ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
		List<ExchangeTransformAudit> audits = exchangeDal.getAllExchangeTransformAudits(serviceId, systemId, exchangeId);
		for (ExchangeTransformAudit audit: audits) {
			if (audit.getNumberBatchesCreated() != null) {
				batches += audit.getNumberBatchesCreated();
			}
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

	/*private static void findCodes() {

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
	}*/

	private static void createTppSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
		LOG.info("Creating TPP Subset");

		try {

			Set<String> personIds = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
			for (String line: lines) {
				line = line.trim();

				//ignore comments
				if (line.startsWith("#")) {
					continue;
				}
				personIds.add(line);
			}

			File sourceDir = new File(sourceDirPath);
			File destDir = new File(destDirPath);

			if (!destDir.exists()) {
				destDir.mkdirs();
			}

			createTppSubsetForFile(sourceDir, destDir, personIds);

			LOG.info("Finished Creating TPP Subset");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void createTppSubsetForFile(File sourceDir, File destDir, Set<String> personIds) throws Exception {

		File[] files = sourceDir.listFiles();
		LOG.info("Found " + files.length + " files in " + sourceDir);

		for (File sourceFile: files) {

			String name = sourceFile.getName();
			File destFile = new File(destDir, name);

			if (sourceFile.isDirectory()) {

				if (!destFile.exists()) {
					destFile.mkdirs();
				}

				//LOG.info("Doing dir " + sourceFile);
				createTppSubsetForFile(sourceFile, destFile, personIds);

			} else {

				if (destFile.exists()) {
					destFile.delete();
				}

				LOG.info("Checking file " + sourceFile);

				//skip any non-CSV file
				String ext = FilenameUtils.getExtension(name);
				if (!ext.equalsIgnoreCase("csv")) {
					LOG.info("Skipping as not a CSV file");
					continue;
				}

				Charset encoding = Charset.forName("CP1252");
				InputStreamReader reader =
						new InputStreamReader(
								new BufferedInputStream(
										new FileInputStream(sourceFile)), encoding);

				CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withHeader();

				CSVParser parser = new CSVParser(reader, format);

				String filterColumn = null;

				Map<String, Integer> headerMap = parser.getHeaderMap();
				if (headerMap.containsKey("IDPatient")) {
					filterColumn = "IDPatient";

				} else if (name.equalsIgnoreCase("SRPatient.csv")) {
					filterColumn = "RowIdentifier";

				} else {
					//if no patient column, just copy the file
					parser.close();

					LOG.info("Copying non-patient file " + sourceFile);
					copyFile(sourceFile, destFile);
					continue;
				}

				String[] columnHeaders = new String[headerMap.size()];
				Iterator<String> headerIterator = headerMap.keySet().iterator();
				while (headerIterator.hasNext()) {
					String headerName = headerIterator.next();
					int headerIndex = headerMap.get(headerName);
					columnHeaders[headerIndex] = headerName;
				}

				BufferedWriter bw =
						new BufferedWriter(
								new OutputStreamWriter(
										new FileOutputStream(destFile), encoding));

				CSVPrinter printer = new CSVPrinter(bw, format.withHeader(columnHeaders));

				Iterator<CSVRecord> csvIterator = parser.iterator();
				while (csvIterator.hasNext()) {
					CSVRecord csvRecord = csvIterator.next();

					String patientId = csvRecord.get(filterColumn);
					if (personIds.contains(patientId)) {

						printer.printRecord(csvRecord);
						printer.flush();
					}
				}

				parser.close();
				printer.close();

				/*} else {
					//the 2.1 files are going to be a pain to split by patient, so just copy them over
					LOG.info("Copying 2.1 file " + sourceFile);
					copyFile(sourceFile, destFile);
				}*/
			}
		}
	}

	private static void createVisionSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
		LOG.info("Creating Vision Subset");

		try {

			Set<String> personIds = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
			for (String line: lines) {
				line = line.trim();

				//ignore comments
				if (line.startsWith("#")) {
					continue;
				}
				personIds.add(line);
			}

			File sourceDir = new File(sourceDirPath);
			File destDir = new File(destDirPath);

			if (!destDir.exists()) {
				destDir.mkdirs();
			}

			createVisionSubsetForFile(sourceDir, destDir, personIds);

			LOG.info("Finished Creating Vision Subset");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void createVisionSubsetForFile(File sourceDir, File destDir, Set<String> personIds) throws Exception {

		File[] files = sourceDir.listFiles();
		LOG.info("Found " + files.length + " files in " + sourceDir);

		for (File sourceFile: files) {

			String name = sourceFile.getName();
			File destFile = new File(destDir, name);

			if (sourceFile.isDirectory()) {

				if (!destFile.exists()) {
					destFile.mkdirs();
				}

				createVisionSubsetForFile(sourceFile, destFile, personIds);

			} else {

				if (destFile.exists()) {
					destFile.delete();
				}

				LOG.info("Checking file " + sourceFile);

				//skip any non-CSV file
				String ext = FilenameUtils.getExtension(name);
				if (!ext.equalsIgnoreCase("csv")) {
					LOG.info("Skipping as not a CSV file");
					continue;
				}

				FileReader fr = new FileReader(sourceFile);
				BufferedReader br = new BufferedReader(fr);

				CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);

				CSVParser parser = new CSVParser(br, format);

				int filterColumn = -1;

				if (name.contains("encounter_data") || name.contains("journal_data") ||
						name.contains("patient_data") || name.contains("referral_data")) {

					filterColumn = 0;
				} else {
					//if no patient column, just copy the file
					parser.close();

					LOG.info("Copying non-patient file " + sourceFile);
					copyFile(sourceFile, destFile);
					continue;
				}

				PrintWriter fw = new PrintWriter(destFile);
				BufferedWriter bw = new BufferedWriter(fw);

				CSVPrinter printer = new CSVPrinter(bw, format);

				Iterator<CSVRecord> csvIterator = parser.iterator();
				while (csvIterator.hasNext()) {
					CSVRecord csvRecord = csvIterator.next();

					String patientId = csvRecord.get(filterColumn);
					if (personIds.contains(patientId)) {

						printer.printRecord(csvRecord);
						printer.flush();
					}
				}

				parser.close();
				printer.close();
			}
		}
	}

	private static void createHomertonSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
		LOG.info("Creating Homerton Subset");

		try {

			Set<String> PersonIds = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
			for (String line: lines) {
				line = line.trim();

				//ignore comments
				if (line.startsWith("#")) {
					continue;
				}

				PersonIds.add(line);
			}

			File sourceDir = new File(sourceDirPath);
			File destDir = new File(destDirPath);

			if (!destDir.exists()) {
				destDir.mkdirs();
			}

			createHomertonSubsetForFile(sourceDir, destDir, PersonIds);

			LOG.info("Finished Creating Homerton Subset");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void createHomertonSubsetForFile(File sourceDir, File destDir, Set<String> personIds) throws Exception {

		File[] files = sourceDir.listFiles();
		LOG.info("Found " + files.length + " files in " + sourceDir);

		for (File sourceFile: files) {

			String name = sourceFile.getName();
			File destFile = new File(destDir, name);

			if (sourceFile.isDirectory()) {

				if (!destFile.exists()) {
					destFile.mkdirs();
				}

				createHomertonSubsetForFile(sourceFile, destFile, personIds);

			} else {

				if (destFile.exists()) {
					destFile.delete();
				}

				LOG.info("Checking file " + sourceFile);

				//skip any non-CSV file
				String ext = FilenameUtils.getExtension(name);
				if (!ext.equalsIgnoreCase("csv")) {
					LOG.info("Skipping as not a CSV file");
					continue;
				}

				FileReader fr = new FileReader(sourceFile);
				BufferedReader br = new BufferedReader(fr);

				//fully quote destination file to fix CRLF in columns
				CSVFormat format = CSVFormat.DEFAULT.withHeader();

				CSVParser parser = new CSVParser(br, format);

				int filterColumn = -1;

				//PersonId column at 1
				if (name.contains("ENCOUNTER") || name.contains("PATIENT")) {
					filterColumn = 1;

				} else if (name.contains("DIAGNOSIS")) {
					//PersonId column at 13
					filterColumn = 13;
				} else if (name.contains("ALLERGY")) {
						//PersonId column at 2
						filterColumn = 2;

				} else if (name.contains("PROBLEM")) {
					//PersonId column at 4
					filterColumn = 4;
				} else {
					//if no patient column, just copy the file (i.e. PROCEDURE)
					parser.close();

					LOG.info("Copying file without PatientId " + sourceFile);
					copyFile(sourceFile, destFile);
					continue;
				}

				Map<String, Integer> headerMap = parser.getHeaderMap();
				String[] columnHeaders = new String[headerMap.size()];
				Iterator<String> headerIterator = headerMap.keySet().iterator();
				while (headerIterator.hasNext()) {
					String headerName = headerIterator.next();
					int headerIndex = headerMap.get(headerName);
					columnHeaders[headerIndex] = headerName;
				}

				PrintWriter fw = new PrintWriter(destFile);
				BufferedWriter bw = new BufferedWriter(fw);

				CSVPrinter printer = new CSVPrinter(bw, format.withHeader(columnHeaders));

				Iterator<CSVRecord> csvIterator = parser.iterator();
				while (csvIterator.hasNext()) {
					CSVRecord csvRecord = csvIterator.next();

					String patientId = csvRecord.get(filterColumn);
					if (personIds.contains(patientId)) {

						printer.printRecord(csvRecord);
						printer.flush();
					}
				}

				parser.close();
				printer.close();
			}
		}
	}

	private static void createAdastraSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
		LOG.info("Creating Adastra Subset");

		try {

			Set<String> caseIds = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
			for (String line: lines) {
				line = line.trim();

				//ignore comments
				if (line.startsWith("#")) {
					continue;
				}

				//adastra extract files are all keyed on caseId
				caseIds.add(line);
			}

			File sourceDir = new File(sourceDirPath);
			File destDir = new File(destDirPath);

			if (!destDir.exists()) {
				destDir.mkdirs();
			}

			createAdastraSubsetForFile(sourceDir, destDir, caseIds);

			LOG.info("Finished Creating Adastra Subset");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void createAdastraSubsetForFile(File sourceDir, File destDir, Set<String> caseIds) throws Exception {

		File[] files = sourceDir.listFiles();
		LOG.info("Found " + files.length + " files in " + sourceDir);

		for (File sourceFile: files) {

			String name = sourceFile.getName();
			File destFile = new File(destDir, name);

			if (sourceFile.isDirectory()) {

				if (!destFile.exists()) {
					destFile.mkdirs();
				}

				createAdastraSubsetForFile(sourceFile, destFile, caseIds);

			} else {

				if (destFile.exists()) {
					destFile.delete();
				}

				LOG.info("Checking file " + sourceFile);

				//skip any non-CSV file
				String ext = FilenameUtils.getExtension(name);
				if (!ext.equalsIgnoreCase("csv")) {
					LOG.info("Skipping as not a CSV file");
					continue;
				}

				FileReader fr = new FileReader(sourceFile);
				BufferedReader br = new BufferedReader(fr);

				//fully quote destination file to fix CRLF in columns
				CSVFormat format = CSVFormat.DEFAULT.withDelimiter('|');

				CSVParser parser = new CSVParser(br, format);

				int filterColumn = -1;

				//CaseRef column at 0
				if (name.contains("NOTES") || name.contains("CASEQUESTIONS") ||
						name.contains("OUTCOMES") || name.contains("CONSULTATION") ||
						name.contains("CLINICALCODES") || name.contains("PRESCRIPTIONS") ||
						name.contains("PATIENT")) {

					filterColumn = 0;

				} else if (name.contains("CASE")) {
					//CaseRef column at 2
					filterColumn = 2;

				} else if (name.contains("PROVIDER")) {
					//CaseRef column at 7
					filterColumn = 7;

				} else {
					//if no patient column, just copy the file
					parser.close();

					LOG.info("Copying non-patient file " + sourceFile);
					copyFile(sourceFile, destFile);
					continue;
				}

				PrintWriter fw = new PrintWriter(destFile);
				BufferedWriter bw = new BufferedWriter(fw);

				CSVPrinter printer = new CSVPrinter(bw, format);

				Iterator<CSVRecord> csvIterator = parser.iterator();
				while (csvIterator.hasNext()) {
					CSVRecord csvRecord = csvIterator.next();

					String caseId = csvRecord.get(filterColumn);
					if (caseIds.contains(caseId)) {

						printer.printRecord(csvRecord);
						printer.flush();
					}
				}

				parser.close();
				printer.close();
			}
		}
	}

	private static void exportFhirToCsv(UUID serviceId, String destinationPath) {
		try {

			File dir = new File(destinationPath);
			if (dir.exists()) {
				dir.mkdirs();
			}

			Map<String, CSVPrinter> hmPrinters = new HashMap<>();

			EntityManager entityManager = ConnectionManager.getEhrEntityManager(serviceId);
			SessionImpl session = (SessionImpl) entityManager.getDelegate();
			Connection connection = session.connection();

			PreparedStatement ps = connection.prepareStatement("SELECT resource_id, resource_type, resource_data FROM resource_current");
			LOG.debug("Running query");
			ResultSet rs = ps.executeQuery();
			LOG.debug("Got result set");

			while (rs.next()) {
				String id = rs.getString(1);
				String type = rs.getString(2);
				String json = rs.getString(3);

				CSVPrinter printer = hmPrinters.get(type);
				if (printer == null) {

					String path = FilenameUtils.concat(dir.getAbsolutePath(), type + ".tsv");
					FileWriter fileWriter = new FileWriter(new File(path));
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

					CSVFormat format = CSVFormat.DEFAULT
							.withHeader("resource_id", "resource_json")
							.withDelimiter('\t')
							.withEscape((Character) null)
							.withQuote((Character) null)
							.withQuoteMode(QuoteMode.MINIMAL);

					printer = new CSVPrinter(bufferedWriter, format);
					hmPrinters.put(type, printer);
				}

				printer.printRecord(id, json);
			}

			for (String type : hmPrinters.keySet()) {
				CSVPrinter printer = hmPrinters.get(type);
				printer.flush();
				printer.close();
			}

			ps.close();
			entityManager.close();

		} catch (Throwable t) {
			LOG.error("", t);
		}
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
