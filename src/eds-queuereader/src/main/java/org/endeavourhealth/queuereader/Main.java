package org.endeavourhealth.queuereader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.core.configuration.ConfigDeserialiser;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		LOG.info("Initialising config manager");
		ConfigManager.Initialize("queuereader");

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("Exit")) {

			String exitCode = args[1];
			LOG.info("Exiting with error code " + exitCode);
			int exitCodeInt = Integer.parseInt(exitCode);
			System.exit(exitCodeInt);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("RunSql")) {

			String host = args[1];
			String username = args[2];
			String password = args[3];
			String sqlFile = args[4];
			runSql(host, username, password, sqlFile);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateProtocolQueue")) {
			String serviceId = args[1];
			populateProtocolQueue(serviceId);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindEncounterTerms")) {
			String path = args[1];
			String outputPath = args[2];
			findEncounterTerms(path, outputPath);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindEmisStartDates")) {
			String path = args[1];
			String outputPath = args[2];
			findEmisStartDates(path, outputPath);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("ExportHl7Encounters")) {
			String sourceCsvPpath = args[1];
			String outputPath = args[2];
			exportHl7Encounters(sourceCsvPpath, outputPath);
			System.exit(0);
		}

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

		String configId = args[0];

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
		LOG.info("Starting message consumption");
		rabbitHandler.start();
		LOG.info("EDS Queue reader running");
	}

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

			String sql = FileUtils.readFileToString(f);
			LOG.info("Going to run SQL");
			LOG.info(sql);

			//load driver
			Class.forName("com.mysql.cj.jdbc.Driver");

			//create connection
			Properties props = new Properties();
			props.setProperty("user", username);
			props.setProperty("password", password);

			conn = DriverManager.getConnection(host, props);
			LOG.info("Opened connection");
			statement = conn.createStatement();

			//run SQL
			boolean hasResultSet = statement.execute(sql);
			if (hasResultSet) {

				while (true) {
					ResultSet rs = statement.getResultSet();
					int cols = rs.getMetaData().getColumnCount();

					List<String> colHeaders = new ArrayList<>();
					for (int i=0; i<cols; i++) {
						String header = rs.getMetaData().getColumnName(i+1);
						colHeaders.add(header);
					}
					String colHeaderStr = String.join(", ", colHeaders);
					LOG.info(colHeaderStr);

					while (rs.next()) {
						List<String> row = new ArrayList<>();
						for (int i=0; i<cols; i++) {
							Object o = rs.getObject(i+1);
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

			LOG.info("Closed connection");

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
	private static void exportHl7Encounters(String sourceCsvPath, String outputPath) {
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
						List<ResourceWrapper> resourceWrappers = resourceDalI.getResourcesForBatch(batchId);
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
								EpisodeOfCare fhirEpisode = (EpisodeOfCare)resourceDalI.getCurrentVersionAsResource(comps.getResourceType(), comps.getId());
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
									Location fhirLocation = (Location)resourceDalI.getCurrentVersionAsResource(comps.getResourceType(), comps.getId());
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
									Practitioner fhirPractitioner = (Practitioner)resourceDalI.getCurrentVersionAsResource(comps.getResourceType(), comps.getId());
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
	}

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

	private static void populateProtocolQueue(String serviceIdStr) {
		LOG.info("Starting Populating Protocol Queue for " + serviceIdStr);

		UUID serviceId = UUID.fromString(serviceIdStr);

		ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
		ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();

		try {
			Service service = serviceRepository.getById(serviceId);

			//for (Service service: serviceRepository.getAll()) {
			List<UUID> exchangeIds = auditRepository.getExchangeIdsForService(service.getId());
			LOG.info("Found " + exchangeIds.size() + " exchangeIds");

			QueueHelper.postToExchange(exchangeIds, "edsProtocol", null, true);

		} catch (Exception ex) {
			LOG.error("", ex);
		}

		LOG.info("Finished Populating Protocol Queue for " + serviceIdStr);
	}

	private static void findDeletedOrgs() {
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
	}

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
