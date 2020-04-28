package org.endeavourhealth.queuereader;

import OpenPseudonymiser.Crypto;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.apache.commons.csv.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.configuration.ConfigDeserialiser;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.datagenerator.SubscriberZipFileUUIDsDalI;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.publisherTransform.models.ResourceFieldMappingAudit;
import org.endeavourhealth.core.database.dal.reference.PostcodeDalI;
import org.endeavourhealth.core.database.dal.reference.models.PostcodeLookup;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberOrgMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberPersonMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberId;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.enterprise.EnterpriseConnector;
import org.endeavourhealth.core.exceptions.TransformException;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.components.MessageTransformOutbound;
import org.endeavourhealth.core.messaging.pipeline.components.OpenEnvelope;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.Arg;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.enterprise.transforms.AbstractEnterpriseTransformer;
import org.endeavourhealth.transform.subscriber.targetTables.OutputContainer;
import org.endeavourhealth.transform.subscriber.targetTables.SubscriberTableId;
import org.endeavourhealth.transform.ui.helpers.BulkHelper;
import org.hibernate.internal.SessionImpl;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.*;
import java.lang.System;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.endeavourhealth.core.xml.QueryDocument.ServiceContractType.PUBLISHER;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		String configId = args[0];
		LOG.info("Initialising config manager");
		ConfigManager.initialize("queuereader", configId);

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindEmisServicesNeedReprocessing")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			SpecialRoutines.findEmisServicesNeedReprocessing(odsCodeRegex);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TransformBartsEncounters")) {

			String odsCode = args[1];
			String tableName = args[2];
			SpecialRoutines.transformAdtEncounters(odsCode, tableName);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CatptureBartsEncounters")) {
			Integer count = Integer.parseInt(args[1]);
			String toFile = args[2];
			SpecialRoutines.catptureBartsEncounters(count, toFile);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("LoadTppStagingData")) {
			String odsCode = args[1];
			UUID fromExchange = null;
			if (args.length > 2) {
				fromExchange = UUID.fromString(args[2]);
			}
			SpecialRoutines.loadTppStagingData(odsCode, fromExchange);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("LoadEmisStagingData")) {
			String odsCode = args[1];
			UUID fromExchange = null;
			if (args.length > 2) {
				fromExchange = UUID.fromString(args[2]);
			}
			SpecialRoutines.loadEmisStagingData(odsCode, fromExchange);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("RequeueTppSkippedAdminData")) {
			boolean tpp = Boolean.valueOf(args[1]);
			boolean onAtATime = Boolean.valueOf(args[2]);
			SpecialRoutines.requeueSkippedAdminData(tpp, onAtATime);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestCallToDDSUI")) {
			SpecialRoutines.testCallToDdsUi();
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestBulkLoad")) {
			String s3Path = args[1];
			String tableName = args[2];
			SpecialRoutines.testBulkLoad(s3Path, tableName);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestInformationModel")) {
			SpecialRoutines.testInformationModel();
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("BreakUpAdminBatches")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			SpecialRoutines.breakUpAdminBatches(odsCodeRegex);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("GetJarDetails")) {
			SpecialRoutines.getJarDetails();
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("ValidateNhsNumbers")) {
			String filePath = args[1];
			boolean addCommas = Boolean.parseBoolean(args[2]);
			SpecialRoutines.validateNhsNumbers(filePath, addCommas);
			System.exit(0);
		}


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("GetResourceHistory")) {
			String serviceId = args[1];
			String resourceType = args[2];
			String resourceId = args[3];
			SpecialRoutines.getResourceHistory(serviceId, resourceType, resourceId);
			System.exit(0);
		}


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindOutOfOrderTppServices")) {
			SpecialRoutines.findOutOfOrderTppServices();
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateExchangeFileSizes")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			SpecialRoutines.populateExchangeFileSizes(odsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindEmisMissingCodes")) {
			String ccgCodeRegex = null;
			if (args.length > 1) {
				ccgCodeRegex = args[1];
			}
			findEmisMissingCodes(ccgCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEncounters")) {
			String table = args[1];
			fixEncounters(table);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("DeleteEnterpriseObs")) {
			String filePath = args[1];
			String configName = args[2];
			int batchSize = Integer.parseInt(args[3]);
			deleteEnterpriseObs(filePath, configName, batchSize);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixTppStaffBulks")) {
			boolean testMode = Boolean.parseBoolean(args[1]);
			String odsCodeRegex = null;
			if (args.length > 2) {
				odsCodeRegex = args[2];
			}
			fixTppStaffBulks(testMode, odsCodeRegex);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestDSM")) {
			String odsCode = args[1];
			SpecialRoutines.testDsm(odsCode);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CompareDSM")) {
			boolean logDifferencesOnly = Boolean.parseBoolean(args[1]);
			String toFile = args[2];
			String odsCode = null;
			if (args.length > 3) {
				odsCode = args[3];
			}
			SpecialRoutines.compareDsm(logDifferencesOnly, toFile, odsCode);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("GenerateConfigForDSM")) {

			String ddsUiProtocolName = args[1];
			String dsmDsaId = args[2];
			SpecialRoutines.createConfigJsonForDSM(ddsUiProtocolName, dsmDsaId);
			System.exit(0);
		}


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("UPRN")) {
			String configName = args[1];
			String protocolName = args[2];
			String outputFormat = args[3];
			String fileName = args[4];
			String debug = args[5];
			bulkProcessUPRN(configName, protocolName, outputFormat, fileName, debug);

			System.exit(0);
		}

		if (args.length >=1 && args[0].equalsIgnoreCase("UPRNTHREADED")) {
			String configName = args[1];
			String protocolName = args[2];
			String outputFormat = args[3];
			String filePath = args[4];
			String debug = args[5];
			bulkProcessUPRNThreaded(configName, protocolName, outputFormat, filePath, debug);

			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindMissedExchanges")) {
			String tableName = args[1];
			String odsCodeRegex = null;
			if (args.length > 2) {
				odsCodeRegex = args[2];
			}
			findMissedExchanges(tableName, odsCodeRegex);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("SendPatientsToSubscriber")) {
			String tableName = args[1];
			String reason = args[2];
			sendPatientsToSubscriber(tableName, reason);
			System.exit(0);
		}



		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateDeleteZipsForSubscriber")) {
			int batchSize = Integer.parseInt(args[1]);
			String sourceTable = args[2];
			int subscriberId = Integer.parseInt(args[3]);
			createDeleteZipsForSubscriber(batchSize, sourceTable, subscriberId);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestJMX")) {
			testJmx();
			System.exit(0);
		}*/


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestDatabases")) {
			String serviceIdStr = args[1];
			String subscriberConfigName = args[2];
			testDatabases(serviceIdStr, subscriberConfigName);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulatePatientSearchEpisodeOdsCode")) {
			populatePatientSearchEpisodeOdsCode();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEmisSnomedCodes")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			fixEmisSnomedCodes(odsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEmisDrugRecords")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			fixEmisDrugRecords(odsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateSubscriberDBPseudoId")) {
			String subscriberConfigName = args[1];
			String saltKeyName = args[2];
			populateSubscriberPseudoId(subscriberConfigName, saltKeyName);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("InvestigateMissingPatients")) {
			String nhsNumberFile = args[1];
			String protocolName = args[2];
			String subscriberConfigName = args[3];
			String odsCodeRegex = args[4];
			investigateMissingPatients(nhsNumberFile, protocolName, subscriberConfigName, odsCodeRegex);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixMedicationStatementIsActive")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			fixMedicationStatementIsActive(odsCodeRegex);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixMissingEmisEthnicities")) {
			String filePath = args[1];
			String odsCodeRegex = null;
			if (args.length > 2) {
				odsCodeRegex = args[2];
			}
			fixMissingEmisEthnicities(filePath, odsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("UpdatePatientSearch")) {
			String filePath = args[1];
			updatePatientSearch(filePath);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("SubscriberFullLoad")) {
			UUID serviceId = UUID.fromString(args[1]);
			UUID protocolId = UUID.fromString(args[2]);
			QueueHelper.queueUpFullServiceForPopulatingSubscriber(serviceId, protocolId);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("SubscriberFullLoadFilteredFiles")) {
			UUID serviceId = UUID.fromString(args[1]);
			UUID protocolId = UUID.fromString(args[2]);
			String filteredFiles = args[3];
			QueueHelper.queueUpFullServiceForPopulatingSubscriberFilteredFiles(serviceId, protocolId, filteredFiles);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TransformAndFilePatientsAndEpisodesForProtocolServices")) {
			String protocolName = args[1];
			String subscriberConfigName= args[2];

			SpecialRoutines.transformAndFilePatientsAndEpisodesForProtocolServices(protocolName, subscriberConfigName);
			System.exit(0);
		}


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("RunPersonUpdater")) {
			String enterpriseConfigName = args[1];
			runPersonUpdater(enterpriseConfigName);
			System.exit(0);
		}*/


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("CountNhsNumberChanges")) {
			String odsCode = args[1];
			countNhsNumberChanges(odsCode);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TransformPatients")) {
			String sourceFile = args[1];
			String reason = args[2];
			transformPatients(sourceFile, reason);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindPatientsThatNeedTransforming")) {
			String file = args[1];
			String odsCode = null;
			if (args.length > 2) {
				odsCode = args[2];
			}
			findPatientsThatNeedTransforming(file, odsCode);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateDigest")) {
			String url = args[1];
			String user = args[2];
			String pass = args[3];
			String table = args[4];
			String columnFrom = args[5];
			String columnTo = args[6];
			String base64Salt = args[7];
			String validNhsNumberCol = null;
			if (args.length > 8) {
				validNhsNumberCol = args[8];
			}
			createDigest(url, user, pass, table, columnFrom, columnTo, base64Salt, validNhsNumberCol);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ConvertAudits2")) {
			String configName = args[1];
			String tempTable = args[2];
			int threads = Integer.parseInt(args[3]);
			int batchSize = Integer.parseInt(args[4]);
			boolean testMode = Boolean.parseBoolean(args[5]);
			convertFhirAudits2(configName, tempTable, threads, batchSize, testMode);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ConvertAudits")) {
			String configName = args[2];
			int threads = Integer.parseInt(args[3]);
			int batchSize = Integer.parseInt(args[4]);
			convertFhirAudits(configName, threads, batchSize);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestRabbit")) {
			String nodes = args[1];
			String username = args[2];
			String password = args[3];
			String exchangeName = args[4];
			String queueName = args[5];

			String sslProtocol = null;
			if (args.length > 6) {
				sslProtocol = args[6];
			}
			testRabbit(nodes, username, password, sslProtocol, exchangeName, queueName);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEmisEpisodes1")) {
			String odsCode = args[1];
			//fixEmisEpisodes1(odsCode);
			fixEmisEpisodes2(odsCode);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestS3Listing")) {
			String path = args[1];
			testS3Listing(path);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("CheckForBartsMissingFiles")) {
			String sinceDate = args[1];
			checkForBartsMissingFiles(sinceDate);
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

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateBartsSubset")) {
			String sourceDirPath = args[1];
			UUID serviceUuid = UUID.fromString(args[2]);
			UUID systemUuid = UUID.fromString(args[3]);
			String samplePatientsFile = args[4];
			createBartsSubset(sourceDirPath, serviceUuid, systemUuid, samplePatientsFile);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateEmisSubset")) {
			String sourceDirPath = args[1];
			String destDirPath = args[2];
			String samplePatientsFile = args[3];
			createEmisSubset(sourceDirPath, destDirPath, samplePatientsFile);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindBartsPersonIds")) {
			String sourceFile = args[1];
			UUID serviceUuid = UUID.fromString(args[2]);
			UUID systemUuid = UUID.fromString(args[3]);
			String dateCutoffStr = args[4];
			String dstFile = args[5];
			findBartsPersonIds(sourceFile, serviceUuid, systemUuid, dateCutoffStr, dstFile);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixTPPNullOrgs")) {
			String sourceDirPath = args[1];
			String orgODS = args[2];

			LOG.info("Fixing TPP Null Organisations");
			fixTPPNullOrgs(sourceDirPath, orgODS);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEmisDeletedPatients")) {
			String odsCode = args[1];
			fixEmisDeletedPatients(odsCode);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PostPatientToProtocol")) {
			String odsCode = args[1];
			String patientUuid = args[2];
			postPatientToProtocol(odsCode, patientUuid);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("PostPatientsToProtocol")) {
			UUID serviceId = UUID.fromString(args[1]);
			UUID systemId = UUID.fromString(args[2]);
			String sourceFile = args[3];
			postPatientsToProtocol(serviceId, systemId, sourceFile);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestMetrics")) {
			testMetrics();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestXML")) {
			testXml();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestGraphiteMetrics")) {
			String host = args[1];
			String port = args[2];
			testGraphiteMetrics(host, port);
			System.exit(0);
		}*/


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

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateTransformMap")) {
			UUID serviceId = UUID.fromString(args[1]);
			String table = args[2];
			String dstFile = args[3];
			createTransforMap(serviceId, table, dstFile);
			System.exit(0);
		}*/


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ExportFhirToCsv")) {
			UUID serviceId = UUID.fromString(args[1]);
			String path = args[2];
			exportFhirToCsv(serviceId, path);
			System.exit(0);
		}*/


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestBatchInserts")) {
			String url = args[1];
			String user = args[2];
			String pass = args[3];
			String num = args[4];
			String batchSize = args[5];
			testBatchInserts(url, user, pass, num, batchSize);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ApplyEmisAdminCaches")) {
			applyEmisAdminCaches();
			System.exit(0);
		}*/

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

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestS3Read")) {
			String s3Bucket = args[1];
			String s3Key = args[2];
			String start = args[3];
			String len = args[4];
			testS3Read(s3Bucket, s3Key, start, len);
			System.exit(0);
		}*/

		/*if (args.length >= 1
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
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("CheckDeletedObs")) {
			String serviceId = args[1];
			String systemId = args[2];
			checkDeletedObs(UUID.fromString(serviceId), UUID.fromString(systemId));
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixPersonsNoNhsNumber")) {
			fixPersonsNoNhsNumber();
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CalculateUprnPseudoIds")) {
			String subscriberConfigName = args[1];
			String targetTable = args[2];
			calculateUprnPseudoIds(subscriberConfigName, targetTable);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateSubscriberUprnTable")) {
			String subscriberConfigName = args[1];
			Integer overrideBatchSize = null;
			if (args.length > 2) {
				overrideBatchSize = Integer.valueOf(args[2]);
			}
			String patientId = null;
			if (args.length > 3) {
				patientId = args[3];
			}
			populateSubscriberUprnTable(subscriberConfigName, overrideBatchSize, patientId);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ConvertEmisGuid")) {
			convertEmisGuids();
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("PostToRabbit")) {
			String exchangeName = args[1];
			String srcFile = args[2];
			Integer throttle = null;
			if (args.length > 3) {
				throttle = Integer.parseInt(args[3]);
			}
			postToRabbit(exchangeName, srcFile, throttle);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("PostExchangesToProtocol")) {
			String srcFile = args[1];
			postExchangesToProtocol(srcFile);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixBartsPatients")) {
			UUID serviceId = UUID.fromString(args[1]);
			fixBartsPatients(serviceId);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixDeceasedPatients")) {
			String subscriberConfig = args[1];
			fixDeceasedPatients(subscriberConfig);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixPseudoIds")) {
			String subscriberConfig = args[1];
			int threads = Integer.parseInt(args[2]);
			fixPseudoIds(subscriberConfig, threads);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("MoveS3ToAudit")) {
			int threads = Integer.parseInt(args[1]);
			moveS3ToAudit(threads);
			System.exit(0);
		}*/


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ConvertExchangeBody")) {
			convertExchangeBody();
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

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixDisabledExtract")) {
			String sharedStoragePath = args[1];
			String tempDir = args[2];
			String systemId = args[3];
			String serviceOdsCode = args[4];

			fixDisabledEmisExtract(serviceOdsCode, systemId, sharedStoragePath, tempDir);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixEmisMissingSlots")) {
			String serviceOdsCode = args[1];
			fixEmisMissingSlots(serviceOdsCode);
			System.exit(0);
		}*/


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateLastDataDate")) {
			int threads = Integer.parseInt(args[1]);
			int batchSize = Integer.parseInt(args[2]);
			populateLastDataDate(threads, batchSize);
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
			String systemId = null;
			if (args.length > 2) {
				systemId = args[2];
			}
			if (serviceId.equalsIgnoreCase("All")) {
				fixPatientSearchAllServices(systemId);
			} else {
				fixPatientSearch(serviceId, systemId);
			}
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixSlotReferences")) {
			String serviceId = args[1];
			try {
				UUID serviceUuid = UUID.fromString(serviceId);
				fixSlotReferences(serviceUuid);
			} catch (Exception ex) {
				fixSlotReferencesForPublisher(serviceId);
			}
			System.exit(0);
		}*/

		/*if (args.length >= 0
				&& args[0].equalsIgnoreCase("TestAuditingFile")) {
			UUID serviceId = UUID.fromString(args[1]);
			UUID systemId = UUID.fromString(args[2]);
			UUID exchangeId = UUID.fromString(args[3]);
			String version = args[4];
			String filePath = args[5];
			testAuditingFile(serviceId, systemId, exchangeId, version, filePath);
			System.exit(0);
		}*/


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestS3VsMySQL")) {
			UUID serviceUuid = UUID.fromString(args[1]);
			int count = Integer.parseInt(args[2]);
			int sqlBatchSize = Integer.parseInt(args[3]);
			String bucketName = args[4];
			testS3VsMySql(serviceUuid, count, sqlBatchSize, bucketName);
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

		if (args.length >= 0
				&& args[0].equalsIgnoreCase("LoadEmisData")) {
			String serviceId = args[1];
			String systemId = args[2];
			String dbUrl = args[3];
			String dbUsername = args[4];
			String dbPassword = args[5];
			String onlyThisFileType = null;
			if (args.length > 6) {
				onlyThisFileType = args[6];
			}
			loadEmisData(serviceId, systemId, dbUrl, dbUsername, dbPassword, onlyThisFileType);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateEmisDataTables")) {
			createEmisDataTables();
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("LoadBartsData")) {
			String serviceId = args[1];
			String systemId = args[2];
			String dbUrl = args[3];
			String dbUsername = args[4];
			String dbPassword = args[5];
			String startDate = args[6];
			String onlyThisFileType = null;
			if (args.length > 7) {
				onlyThisFileType = args[7];
			}
			loadBartsData(serviceId, systemId, dbUrl, dbUsername, dbPassword, startDate, onlyThisFileType);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateBartsDataTables")) {
			createBartsDataTables();
			System.exit(0);
		}

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
		RabbitHandler rabbitHandler = new RabbitHandler(configuration, configId);
		rabbitHandler.start();
		LOG.info("EDS Queue reader running (kill file location " + TransformConfig.instance().getKillFileLocation() + ")");
	}



	/*private static void findEmisMissingCodes(String ccgCodeRegex) {
		LOG.info("Finding Emis Missing Codes");
		try {

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			ServiceDalI serviceRepository = DalProvider.factoryServiceDal();

			List<ExchangeTransformErrorState> errors = exchangeDal.getAllErrorStates();

			List<String> missingCodeLines = new ArrayList<>();
			missingCodeLines.add("\"Name\",\"ODS Code\",\"CDB/Comments\",\"Date Used\",\"Code ID\"");

			List<String> missingDrugLines = new ArrayList<>();
			missingDrugLines.add("\"Name\",\"ODS Code\",\"CDB/Comments\",\"Date Used\",\"Code ID\"");

			List<String> otherLines = new ArrayList<>();

			for (ExchangeTransformErrorState error: errors) {
				UUID serviceId = error.getServiceId();
				List<UUID> exchangeIds = error.getExchangeIdsInError();
				UUID exchangeId = exchangeIds.get(0);
				UUID systemId = error.getSystemId();

				Service service = serviceRepository.getById(serviceId);

				String ccgCode = service.getCcgCode();
				if (!Strings.isNullOrEmpty(ccgCodeRegex)
					&& !Pattern.matches(ccgCodeRegex, ccgCode)) {
					continue;
				}

				Exchange exchange = exchangeDal.getExchange(exchangeId);
				Date exchangeDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);
				String exchangeDateStr = "";
				if (exchangeDate != null) {
					exchangeDateStr = new SimpleDateFormat("yyyy-MM-dd").format(exchangeDate);
				}

				ExchangeTransformAudit transformAudit = exchangeDal.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
				List<String> lines = formatTransformAuditErrorLines(transformAudit);

				String drugId = null;
				String codeId = null;

				for (String line: lines) {
					if (line.contains("Failed to find drug code for codeId")) {
						String[] toks = line.split(" ");
						drugId = toks[toks.length-1];
						break;

					} else if (line.contains("Failed to find clinical code for codeId")) {
						String[] toks = line.split(" ");
						codeId = toks[toks.length-1];
						break;

					}
				}

				String name = service.getName();
				name = name.replace(",", "");

				String ods = service.getLocalId();

				String tagStr = "";
				if (service.getTags() != null) {
					List<String> tagKeys = new ArrayList<>(service.getTags().keySet());
					tagKeys.sort(((o1, o2) -> o1.compareToIgnoreCase(o2)));

					for (String tagKey: tagKeys) {
						if (tagKey.equals("Error")
								|| tagKey.equals("Bulk received")
								|| tagKey.equals("Notes")) {
							continue;
						}

						if (!tagStr.isEmpty()) {
							tagStr += ", ";
						}
						tagStr += tagKey;
						String tagVal = service.getTags().get(tagKey);
						if (!Strings.isNullOrEmpty(tagVal)) {
							tagStr += " ";
							tagStr += tagVal;
						}
					}
				}

				if (drugId != null) {
					missingDrugLines.add("\"" + name + "\",\"" + ods + "\",\"" + tagStr + "\",\"" + exchangeDateStr + "\",\"" + drugId + "\"");

				} else if (codeId != null) {
					missingCodeLines.add("\"" + name + "\",\"" + ods + "\",\"" + tagStr + "\",\"" + exchangeDateStr + "\",\"" + codeId + "\"");

				} else {
					otherLines.add("\"" + name + "\",\"" + ods + "\",\"" + tagStr + "\"");
				}
			}

			System.out.println("Missing drugs");
			for (String line: missingDrugLines) {
				System.out.println(line);
			}

			System.out.println("");
			System.out.println("");
			System.out.println("Missing codes");
			for (String line: missingCodeLines) {
				System.out.println(line);
			}

			System.out.println("");
			System.out.println("");
			System.out.println("Others");
			for (String line: otherLines) {
				System.out.println(line);
			}

			LOG.info("Finished Finding Emis Missing Codes");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/


	private static List<String> formatTransformAuditErrorLines(ExchangeTransformAudit transformAudit) throws Exception {

		//until we need something more powerful, I'm displaying the errors just as a string, to
		//save sending complex JSON objects back to the client
		List<String> lines = new ArrayList<>();

		if (Strings.isNullOrEmpty(transformAudit.getErrorXml())) {
			return lines;
		}

		TransformError errors = TransformErrorSerializer.readFromXml(transformAudit.getErrorXml());

		for (Error error : errors.getError()) {

			//the error will only be null for older errors, from before the field was introduced
			if (error.getDatetime() != null) {
				Calendar calendar = error.getDatetime().toGregorianCalendar();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
				formatter.setTimeZone(calendar.getTimeZone());
				String dateString = formatter.format(calendar.getTime());
				lines.add(dateString);
			}

			for (Arg arg : error.getArg()) {
				String argName = arg.getName();
				String argValue = arg.getValue();
				if (argValue != null) {
					lines.add(argName + " = " + argValue);
				} else {
					lines.add(argName);
				}
			}
			lines.add("");

			org.endeavourhealth.core.xml.transformError.Exception exception = error.getException();
			while (exception != null) {

				if (exception.getMessage() != null) {
					lines.add(exception.getMessage());
				}

				for (ExceptionLine line : exception.getLine()) {
					String cls = line.getClazz();
					String method = line.getMethod();
					Integer lineNumber = line.getLine();

					lines.add("\u00a0\u00a0\u00a0\u00a0at " + cls + "." + method + ":" + lineNumber);

					//lines.add("&nbsp;&nbsp;&nbsp;&nbsp;at " + cls + "." + method + ":" + lineNumber);
				}

				exception = exception.getCause();
				if (exception != null) {
					lines.add("Caused by:");
				}
			}

			//add some space between the separate errors in the audit
			lines.add("");
			lines.add("------------------------------------------------------------------------");
		}

		return lines;
	}

	/*private static void fixTppStaffBulks(boolean testMode, String odsCodeRegex) {
		LOG.info("Fixing TPP Staff Bulks using testMode " + testMode + " and regex " + odsCodeRegex);
		try {

			Set<String> hsNonPatientFiles = new HashSet<>();
			hsNonPatientFiles.add("Ccg");
			hsNonPatientFiles.add("Ctv3");
			hsNonPatientFiles.add("Mapping");
			hsNonPatientFiles.add("MappingGroup");
			hsNonPatientFiles.add("ConfiguredListOption");
			hsNonPatientFiles.add("Ctv3ToVersion2");
			hsNonPatientFiles.add("Ctv3ToSnomed");
			hsNonPatientFiles.add("Ctv3Hierarchy");
			hsNonPatientFiles.add("ImmunisationContent");
			hsNonPatientFiles.add("MedicationReadCodeDetails");
			hsNonPatientFiles.add("Organisation");
			hsNonPatientFiles.add("OrganisationBranch");
			hsNonPatientFiles.add("Staff");
			hsNonPatientFiles.add("StaffMemberProfile");
			hsNonPatientFiles.add("StaffMember");
			hsNonPatientFiles.add("StaffMemberProfileRole");
			hsNonPatientFiles.add("Trust");
			hsNonPatientFiles.add("Questionnaire");
			hsNonPatientFiles.add("Template");
			hsNonPatientFiles.add("Manifest");


			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			List<Service> services = serviceDal.getAll();

			for (Service service: services) {
				//check regex
				if (SpecialRoutines.shouldSkipService(service, odsCodeRegex)) {
					continue;
				}

				//skip non-TPP
				if (service.getTags() == null
						|| !service.getTags().containsKey("TPP")) {
					continue;
				}

				LOG.debug("Doing " + service);

				List<UUID> systemIds = findSystemIds(service);
				for (UUID systemId: systemIds) {

					//find all exchanges
					ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
					List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);

					for (Exchange exchange: exchanges) {

						//check if the exchange contains ONLY the non-patient files
						List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());
						if (files.isEmpty()) {
							continue;
						}

						boolean hasPatientFile = false;
						for (ExchangePayloadFile file: files) {
							String fileType = file.getType();
							if (!hsNonPatientFiles.contains(fileType)) {
								hasPatientFile = true;
							}
						}

						//if we have a patient file in the exchange it wasn't affected by the bug
						if (hasPatientFile) {
							continue;
						}
						LOG.debug("    Exchange " + exchange.getId() + " only contains non-patient files");

						//if the exchange only contains non-patient files, then we need to check the manifest file
						//to see if those files were bulks
						ExchangePayloadFile firstFile = files.get(0);
						File f = new File(firstFile.getPath()); //e.g. s3://<bucket>/<root>/sftpReader/TPP/YDDH3_07Y_GWR/2020-01-18T18.41.00/Split/E85697/SRCtv3Hierarchy.csv
						f = f.getParentFile(); //e.g. s3://<bucket>/<root>/sftpReader/TPP/YDDH3_07Y_GWR/2020-01-18T18.41.00/Split/E85697/
						f = f.getParentFile(); //e.g. s3://<bucket>/<root>/sftpReader/TPP/YDDH3_07Y_GWR/2020-01-18T18.41.00/Split/
						f = f.getParentFile(); //e.g. s3://<bucket>/<root>/sftpReader/TPP/YDDH3_07Y_GWR/2020-01-18T18.41.00/
						f = new File(f, "SRManifest.csv");
						String manifestPath = f.getPath();
						LOG.debug("    Checking manifest at " + manifestPath);

						InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(manifestPath, TppCsvToFhirTransformer.ENCODING);
						CSVParser csvParser = new CSVParser(reader, TppCsvToFhirTransformer.CSV_FORMAT.withHeader());

						Map<String, Boolean> hmManifestContents = new HashMap<>();

						try {
							Iterator<CSVRecord> csvIterator = csvParser.iterator();

							while (csvIterator.hasNext()) {
								CSVRecord csvRecord = csvIterator.next();
								String fileName = csvRecord.get("FileName");
								String isDeltaStr = csvRecord.get("IsDelta");

								if (isDeltaStr.equalsIgnoreCase("Y")) {
									hmManifestContents.put(fileName, Boolean.TRUE);

								} else if (isDeltaStr.equalsIgnoreCase("N")) {
									hmManifestContents.put(fileName, Boolean.FALSE);

								} else {
									//something wrong
									throw new Exception("Unexpected value [" + isDeltaStr + "] in " + manifestPath);
								}
							}
						} finally {
							csvParser.close();
						}

						Boolean firstIsDelta = null;
						String firstFileName = null;

						for (ExchangePayloadFile file: files) {
							String name = FilenameUtils.getBaseName(file.getPath());

							//the Manifest file doesn't contain itself or the SRMapping files
							//and the Mapping file is processed into publisher_common so we don't need to worry about copying
							//that to every split directory
							if (name.equals("SRManifest")
									|| name.equals("SRMapping")
									|| name.equals("SRMappingGroup")) {
								continue;
							}

							//the map doesn't contain file extensions
							Boolean isDelta = hmManifestContents.get(name);
							if (isDelta == null) {
								throw new Exception("Failed to find file " + name + " in SRManifest in " + manifestPath);
							}

							if (firstIsDelta == null) {
								firstIsDelta = isDelta;
								firstFileName = name;

							} else if (firstIsDelta.booleanValue() != isDelta.booleanValue()) {
								//if this file is different to a previous one, we don't have a way to handle this
								throw new Exception("Mis-match in delta state for non-patient files in " + manifestPath
										+ " " + name + " isDelta = " + isDelta + " but "
										+ firstFileName + " isDelta = " + firstIsDelta);
							}
						}

						//if all the files were bulk files then these non-patient were wrongly copied over to our
						//service from the bulk of another service so this exchange should not have been created
						if (firstIsDelta == null
								|| firstIsDelta.booleanValue()) {
							continue;
						}
						LOG.debug("    Exchange " + exchange.getId() + " should not have been created");

						if (testMode) {
							LOG.debug("    NOT FIXING AS TEST MODE");
							continue;
						}

						//add header
						exchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false));

						//save exchange
						AuditWriter.writeExchange(exchange);
					}
				}

			}

			LOG.info("Finished Fixing TPP Staff Bulks");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/



	private static void sendPatientsToSubscriber(String tableName, String reason) {
		LOG.info("Sending patients to subscriber from " + tableName);
		try {

			Connection conn = ConnectionManager.getEdsConnection();

			String sql = "SELECT service_id, protocol_id, patient_id FROM " + tableName + " ORDER BY service_id, protocol_id";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setFetchSize(5000);

			List<UUID> batchPatientIds = new ArrayList<>();
			UUID batchServiceId = null;
			UUID batchProtocolId = null;

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int col = 1;

				UUID serviceId = UUID.fromString(rs.getString(col++));
				UUID protocolId = UUID.fromString(rs.getString(col++));
				UUID patientId = UUID.fromString(rs.getString(col++));

				if (batchServiceId == null
						|| batchProtocolId == null
						|| !serviceId.equals(batchServiceId)
						|| !protocolId.equals(batchProtocolId)) {

					//send any found previously
					if (!batchPatientIds.isEmpty()) {
						LOG.debug("Doing batch of " + batchPatientIds.size() + " for service " + batchServiceId + " and protocol " + batchProtocolId);
						QueueHelper.queueUpFullServiceForPopulatingSubscriber(batchServiceId, batchProtocolId, batchPatientIds, reason, null);
					}

					batchServiceId = serviceId;
					batchProtocolId = protocolId;
					batchPatientIds = new ArrayList<>();
				}

				batchPatientIds.add(patientId);
			}

			//do the remainder
			if (!batchPatientIds.isEmpty()) {
				LOG.debug("Doing batch of " + batchPatientIds.size() + " for service " + batchServiceId + " and protocol " + batchProtocolId);
				QueueHelper.queueUpFullServiceForPopulatingSubscriber(batchServiceId, batchProtocolId, batchPatientIds, reason, null);
			}

			conn.close();

			LOG.info("Finished sending patients to subscriber from " + tableName);
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}

	/**
	 * checks Services to see if any queued up exchange was not yet processed when a bulk subscriber load was started,
	 * meaning that some data was not sent to that subscriber. Populates a table with IDs that can then be queued up
	 * for sending
	 *
	 * tableName should be of a table with this schema:
	 create table tmp.patients_to_requeue (
	 service_id char(36),
	 protocol_id char(36),
	 bulk_exchange_id char(36),
	 patient_id char(36)
	 );
	 */
	private static void findMissedExchanges(String tableName, String odsCodeRegex) {
		LOG.info("Finding missed exchanges filtering on orgs using " + odsCodeRegex + ", storing results in " + tableName);
		try {
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			List<Service> services = serviceDal.getAll();

			for (Service service: services) {
				if (SpecialRoutines.shouldSkipService(service, odsCodeRegex)) {
					continue;
				}

				LOG.debug("Doing " + service);

				List<UUID> systemIds = findSystemIds(service);
				for (UUID systemId: systemIds) {
					LOG.debug("Doing system " + systemId);

					ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
					List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);

					//go through exchanges and look for ones that were created for a bulk subscriber load
					for (int i=0; i<exchanges.size(); i++) {
						Exchange bulkExchange = exchanges.get(i);

						//if the exchange contains the header key to prevent re-queueing then it's possible
						//it's one for the bulk load
						boolean isBulkLoad = false;
						Boolean allowRequeuing = bulkExchange.getHeaderAsBoolean(HeaderKeys.AllowQueueing);
						if (allowRequeuing != null
								&& !allowRequeuing.booleanValue()) {

							List<ExchangeEvent> events = exchangeDal.getExchangeEvents(bulkExchange.getId());
							for (ExchangeEvent event: events) {
								String eventDesc = event.getEventDesc();
								//note weird text check to handle the two versions of this message used
								if (eventDesc.contains("reated exchange to populate subscribers in protocol")) {
									isBulkLoad = true;
									LOG.debug("Bulk load found in exchange " + bulkExchange.getId() + " on " + bulkExchange.getTimestamp() + ": " + eventDesc);
									break;
								}
							}
						}

						if (!isBulkLoad) {
							continue;
						}

						//if this exchange is a bulk load, then we need to check any exchanges received BEFORE it
						//that didn't contain the protocol in their headers were 100% finished with their inbound
						//transform before the bulk load
						String[] protocolIds = bulkExchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);
						if (protocolIds.length != 1) {
							throw new Exception("Bulk Exchange " + bulkExchange.getId() + " has " + protocolIds.length + " protocol IDs in its header");
						}
						String protocolId = protocolIds[0];
						Date dtBulk = bulkExchange.getTimestamp();

						Set<UUID> patientsToFix = new HashSet<>();

						for (int j=i+1; j<exchanges.size(); j++) {
							Exchange priorExchange = exchanges.get(j);

							//skip any other special exchanges that are for bulk loads etc
							Boolean priorAllowRequeuing = priorExchange.getHeaderAsBoolean(HeaderKeys.AllowQueueing);
							if (priorAllowRequeuing != null
									&& !priorAllowRequeuing.booleanValue()) {
								continue;
							}

							//skip any where the header contains the same protocol ID, as this data will have gone
							//to the subscriber anyway
							boolean hadSameProtocol = false;
							String[] priorProtocolIds = priorExchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);
							if (priorProtocolIds == null) {
								throw new Exception("Null protocol IDs for exchange " + priorExchange.getId());
							}
							for (String priorProtocolId: priorProtocolIds) {
								if (priorProtocolId.equals(protocolId)) {
									hadSameProtocol = true;
								}
							}
							if (hadSameProtocol) {
								continue;
							}

							//skip any that didn't actually transform any dta
							ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();
							List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(priorExchange.getId());
							if (batches.isEmpty()) {
								continue;
							}

							List<ExchangeTransformAudit> transformAudits = exchangeDal.getAllExchangeTransformAudits(service.getId(), systemId, priorExchange.getId());
							if (transformAudits.isEmpty()) {
								throw new Exception("No transform audits for exchange " + priorExchange.getId());
							}
							ExchangeTransformAudit firstTransformAudit = null;
							for (ExchangeTransformAudit transformAudit: transformAudits) {
								if (transformAudit.getEnded() != null) {
									firstTransformAudit = transformAudit;
									break;
								}
							}
							if (firstTransformAudit == null) {
								throw new Exception("No finished transform audit found for exchange " + priorExchange.getId());
							}
							Date dtTransform = firstTransformAudit.getEnded();
							if (dtTransform.before(dtBulk)) {
								//if the transform finished before the bulk, then we're OK and don't need to look at any more exchanges
								break;
							}

							//if the transform didn't finish until AFTER the bulk was started, then this exchange's data
							//won't have gone to the subscriber
							LOG.debug("Exchange " + priorExchange.getId() + " finished transform on " + dtTransform + " so missed going to subscriber");

							for (ExchangeBatch b: batches) {
								UUID patientId = b.getEdsPatientId();
								if (patientId != null) {
									patientsToFix.add(patientId);
								}
							}
							LOG.debug("Found " + batches.size() + " batches, patients to fix = " + patientsToFix.size());
						}

						LOG.debug("Found total " + patientsToFix.size() + " patients to fix");

						//save the list of patients to a table
						if (!patientsToFix.isEmpty()) {
							Connection conn = ConnectionManager.getEdsConnection();
							PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?)");
							for (UUID patientId : patientsToFix) {
								int col = 1;
								ps.setString(col++, service.getId().toString());
								ps.setString(col++, protocolId);
								ps.setString(col++, bulkExchange.getId().toString());
								ps.setString(col++, patientId.toString());
								ps.addBatch();
							}
							ps.executeBatch();
							conn.commit();

							ps.close();
							conn.close();
						}
					}
				}


			}

			LOG.info("Finished finding missed exchanges");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}



	private static void createDeleteZipsForSubscriber(int batchSize, String sourceTable, int subscriberId) {
		LOG.info("Create Zips For Subscriber from " + sourceTable + " subscriberId " + subscriberId + " and batchSize " + batchSize);
		try {

			Connection conn = ConnectionManager.getEdsNonPooledConnection();
			String sql = "SELECT enterprise_id FROM " + sourceTable + " WHERE done = 0 AND subscriber_id = ? LIMIT " + batchSize;
			PreparedStatement psSelect = conn.prepareStatement(sql);

			sql = "UPDATE " + sourceTable + " SET done = 1 WHERE enterprise_id = ?";
			PreparedStatement psDone = conn.prepareStatement(sql);

			int batchesDone = 0;
			int idsDone = 0;

			while (true) {

				List<Long> ids = new ArrayList<>();

				psSelect.setInt(1, subscriberId);
				ResultSet rs = psSelect.executeQuery();


				while (rs.next()) {
					long id = rs.getLong(1);
					ids.add(new Long(id));
				}

				if (ids.isEmpty()) {
					break;
				}

				OutputContainer container = new OutputContainer();
				org.endeavourhealth.transform.subscriber.targetTables.Observation obsWriter = container.getObservations();

				for (Long id: ids) {
					SubscriberId idWrapper = new SubscriberId(SubscriberTableId.OBSERVATION.getId(), id.longValue(), null, null);
					obsWriter.writeDelete(idWrapper);
				}

				byte[] bytes = container.writeToZip();
				String base64 = Base64.getEncoder().encodeToString(bytes);

				SubscriberZipFileUUIDsDalI szfudi = DalProvider.factorySubscriberZipFileUUIDs();
				szfudi.createSubscriberZipFileUUIDsEntity(subscriberId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), base64);

				//update the table to say done
				batchesDone ++;

				for (Long id: ids) {
					psDone.setLong(1, id.longValue());
					psDone.addBatch();
					idsDone ++;
				}

				psDone.executeBatch();
				conn.commit();

				LOG.debug("Done " + batchesDone + ", total = " + idsDone);

				if (ids.size() < batchSize) {
					break;
				}
			}

			psSelect.close();
			psDone.close();
			conn.close();

			LOG.debug("Finished at " + batchesDone + ", total = " + idsDone);

			LOG.info("Finished Create Zips For Subscriber");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	static class UPRNCallable implements Callable {
		private UUID serviceUUID;
		private String ResourceType;
		private UUID patientId;
		private String debug;
		private ResourceDalI dal;
		private String outputFormat;
		private String subscriberConfigName;
		private UUID batchUUID;
		public UPRNCallable(UUID serviceUUID, String ResourceType, UUID patientId, String debug, ResourceDalI dal, String outputFormat, String subscriberConfigName, UUID batchUUID) {
			this.serviceUUID = serviceUUID;
			this.ResourceType = ResourceType;
			this.patientId = patientId;
			this.debug = debug;
			this.dal = dal;
			this.outputFormat = outputFormat;
			this.subscriberConfigName = subscriberConfigName;
			this.batchUUID = batchUUID;
		}
		@Override
		public Object call() throws Exception {

			try {
				List<ResourceWrapper> resources = new ArrayList<>();

				ResourceWrapper patientWrapper = dal.getCurrentVersion(serviceUUID, ResourceType, patientId);

				if (patientWrapper == null) {
					// LOG.warn("Null patient resource for Patient " + patientId);
					return null;
				}

				resources.add(patientWrapper);

				if (debug.equals("1")) {
					LOG.info("Service: " + serviceUUID.toString());
					LOG.info("Configname: " + subscriberConfigName);
					LOG.info("Patientid: " + patientId.toString());
				}

				if (outputFormat.equals("SUBSCRIBER")) {

					String containerString = BulkHelper.getSubscriberContainerForUPRNData(resources, serviceUUID, batchUUID, subscriberConfigName, patientId);

					//  Is a random UUID ok to use as a queued message ID
					if (containerString != null) {
						org.endeavourhealth.subscriber.filer.SubscriberFiler.file(batchUUID, UUID.randomUUID(), containerString, subscriberConfigName);
					}

				} else {
					String containerString = BulkHelper.getEnterpriseContainerForUPRNData(resources, serviceUUID, batchUUID, subscriberConfigName, patientId, debug);

					//  Is a random UUID ok to use as a queued message ID
					if (containerString != null) {
						EnterpriseFiler.file(batchUUID, UUID.randomUUID(), containerString, subscriberConfigName);
					}
				}
				return null;
			}
			catch(Exception e) {
				LOG.error(e.toString());
			}
			return null;
		}
	}

	/*
	private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
		if (errors == null || errors.isEmpty()) {
			return;
		}

		//if we've had multiple errors, just throw the first one, since they'll most-likely be the same
		ThreadPoolError first = errors.get(0);
		Throwable cause = first.getException();
		//the cause may be an Exception or Error so we need to explicitly
		//cast to the right type to throw it without changing the method signature
		if (cause instanceof Exception) {
			throw (Exception) cause;
		} else if (cause instanceof Error) {
			throw (Error) cause;
		}
	}
	*/

	private static void bulkProcessUPRNThreaded(String subscriberConfigName, String protocolName, String outputFormat, String filePath, String debug) throws Exception {

		Set<UUID> hsPatientUuids = new HashSet<>();
		File f = new File(filePath);
		if (f.exists()) {
			List<String> lines = Files.readAllLines(f.toPath());
			for (String line : lines) {
				hsPatientUuids.add(UUID.fromString(line));
			}
		}

		LibraryItem matchedLibraryItem = BulkHelper.findProtocolLibraryItem(protocolName);

		if (matchedLibraryItem == null) {
			System.out.println("Protocol not found : " + protocolName);
			return;
		}
		List<ServiceContract> l = matchedLibraryItem.getProtocol().getServiceContract();
		String serviceId = "";
		ResourceDalI dal = DalProvider.factoryResourceDal();
		PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

		SubscriberResourceMappingDalI enterpriseIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

		ThreadPool threadPool = new ThreadPool(5, 10);

		Long ret;

		for (ServiceContract serviceContract : l) {
			if (serviceContract.getType().equals(PUBLISHER)
					&& serviceContract.getActive() == ServiceContractActive.TRUE) {

				UUID batchUUID = UUID.randomUUID();
				serviceId = serviceContract.getService().getUuid();
				UUID serviceUUID = UUID.fromString(serviceId);
				List<UUID> patientIds = patientSearchDal.getPatientIds(serviceUUID);

				for (UUID patientId : patientIds) {

					// check if we have processed the patient already
					if (hsPatientUuids.contains(patientId)) {
						continue;
					}
					List<String> newLines = new ArrayList<>();
					newLines.add(patientId.toString());
					Files.write(f.toPath(), newLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

					LOG.info(patientId.toString());
					ret = enterpriseIdDal.findEnterpriseIdOldWay("Patient", patientId.toString());
					if (ret != null) {
						// check if the patient has previously been processed?
						LOG.info(ret.toString());
					}
					List<ThreadPoolError> errors = threadPool.submit(new UPRNCallable(serviceUUID, ResourceType.Patient.toString(), patientId, debug, dal, outputFormat, subscriberConfigName, batchUUID));
					//handleErrors(errors);
				}
			}
		}

		List<ThreadPoolError> errors = threadPool.waitAndStop();
		//handleErrors(errors);
	}

	private static void bulkProcessUPRN(String subscriberConfigName, String protocolName, String outputFormat, String filePath, String debug) throws Exception {

		Set<UUID> hsPatientUuids = new HashSet<>();
		File f = new File(filePath);
		if (f.exists()) {
			List<String> lines = Files.readAllLines(f.toPath());
			for (String line : lines) {
				hsPatientUuids.add(UUID.fromString(line));
			}
		}

		LibraryItem matchedLibraryItem = BulkHelper.findProtocolLibraryItem(protocolName);

		if (matchedLibraryItem == null) {
			System.out.println("Protocol not found : " + protocolName);
			return;
		}
		List<ServiceContract> l = matchedLibraryItem.getProtocol().getServiceContract();
		String serviceId = "";
		ResourceDalI dal = DalProvider.factoryResourceDal();
		PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

		//List<ResourceWrapper> resources = new ArrayList<>();
		for (ServiceContract serviceContract : l) {
			if (serviceContract.getType().equals(PUBLISHER)
					&& serviceContract.getActive() == ServiceContractActive.TRUE) {

				UUID batchUUID = UUID.randomUUID();
				serviceId = serviceContract.getService().getUuid();
				UUID serviceUUID = UUID.fromString(serviceId);
				List<UUID> patientIds = patientSearchDal.getPatientIds(serviceUUID);
				for (UUID patientId : patientIds) {

					// check if we have processed the patient already
					if (hsPatientUuids.contains(patientId)) {
						continue;
					}
					List<String> newLines = new ArrayList<>();
					newLines.add(patientId.toString());
					Files.write(f.toPath(), newLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

					//resources.clear();
					List<ResourceWrapper> resources = new ArrayList<>();

					ResourceWrapper patientWrapper = dal.getCurrentVersion(serviceUUID, ResourceType.Patient.toString(), patientId);
					if (patientWrapper == null) {
						LOG.warn("Null patient resource for Patient " + patientId);
						continue;
					}

					resources.add(patientWrapper);

					if (debug.equals("1")) {
						LOG.info("Service: " + serviceUUID.toString());
						LOG.info("Configname: " + subscriberConfigName);
						LOG.info("Patientid: " + patientId.toString());
						//LOG.info("resources: " + resources.toString());
						//System.out.println("Press Enter key to continue...");
						//Scanner scan = new Scanner(System.in);
						//scan.nextLine();
					}

					if (outputFormat.equals("SUBSCRIBER")) {

						String containerString = BulkHelper.getSubscriberContainerForUPRNData(resources, serviceUUID, batchUUID, subscriberConfigName, patientId);

						//  Is a random UUID ok to use as a queued message ID
						if (containerString != null) {
							org.endeavourhealth.subscriber.filer.SubscriberFiler.file(batchUUID,  UUID.randomUUID(), containerString, subscriberConfigName);
						}

					} else {
						String containerString = BulkHelper.getEnterpriseContainerForUPRNData(resources, serviceUUID, batchUUID, subscriberConfigName, patientId, debug);

						//  Is a random UUID ok to use as a queued message ID
						if (containerString != null) {
							EnterpriseFiler.file(batchUUID, UUID.randomUUID(), containerString, subscriberConfigName);
						}
					}
				}
			}
		}
	}

	/*private static void testJmx() {
		LOG.info("Testing JMX");
		try {
			LOG.debug("----OperatingSystemMXBean--------------------------------");
			OperatingSystemMXBean osb = ManagementFactory.getOperatingSystemMXBean();
			LOG.debug("getName = " + osb.getName());
			LOG.debug("getSystemLoadAverage = " + osb.getSystemLoadAverage());
			LOG.debug("getArch = " + osb.getArch());
			LOG.debug("getVersion = " + osb.getVersion());
			LOG.debug("getAvailableProcessors = " + osb.getAvailableProcessors());

			LOG.debug("----MemoryMXBean--------------------------------");
			MemoryMXBean mb = ManagementFactory.getMemoryMXBean();
			LOG.debug("getNonHeapMemoryUsage = " + mb.getNonHeapMemoryUsage());
			LOG.debug("getHeapMemoryUsage = " + mb.getHeapMemoryUsage());
			LOG.debug("getObjectPendingFinalizationCount = " + mb.getObjectPendingFinalizationCount());

			LOG.debug("----MemoryMXBean--------------------------------");
			com.sun.management.OperatingSystemMXBean sosb = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
			LOG.debug("getSystemCpuLoad = " + sosb.getSystemCpuLoad());
			LOG.debug("getTotalPhysicalMemorySize = " + sosb.getTotalPhysicalMemorySize());

			LOG.info("Finished Testing JMX");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void testDatabases(String odsCodesStr, String subscriberConfigNamesStr) {
		LOG.info("Testing all databases");
		try {

			String[] odsCodes = odsCodesStr.split("\\|");
			String[] subscriberConfigNames = subscriberConfigNamesStr.split("\\|");

			for (String odsCode: odsCodes) {
				LOG.debug("---------------------------------------------------------------");
				LOG.debug("Doing " + odsCode);

				//admin
				LOG.debug("Doing admin");
				ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
				Service service = serviceDalI.getByLocalIdentifier(odsCode);
				LOG.debug("Admin test " + service);
				UUID serviceId = service.getId();

				//EDS
				LOG.debug("Doing EDS");
				PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
				List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId);
				LOG.debug("EDS test = " + patientIds.size());

				PatientLinkDalI patientLinkDalI = DalProvider.factoryPatientLinkDal();
				List<PatientLinkPair> changes = patientLinkDalI.getChangesSince(new Date());
				LOG.debug("EDS (hibernate) test = " + changes.size());

				//reference
				LOG.debug("Doing reference");
				String snomedTerm = TerminologyService.lookupSnomedTerm("10000006");
				LOG.debug("Reference test = " + snomedTerm);

				//HL7 Receiver
				LOG.debug("Doing HL7 Receiver");
				Hl7ResourceIdDalI hl7ResourceIdDal = DalProvider.factoryHL7ResourceDal();
				ResourceId id = hl7ResourceIdDal.getResourceId("B", "Patient", "PIdAssAuth=2.16.840.1.113883.3.2540.1-PatIdValue=N7619764");
				LOG.debug("HL7 receiver test = " + id);


				//audit
				LOG.debug("Doing audit");
				ExchangeDalI exchangeDalI = DalProvider.factoryExchangeDal();
				List<UUID> systemIds = findSystemIds(service);
				UUID systemId = systemIds.get(0);
				List<Exchange> exchanges = exchangeDalI.getExchangesByService(serviceId, systemId, 100);
				LOG.debug("Audit test " + exchanges.size());

				//publisher common
				LOG.debug("Doing publisher common");
				EmisTransformDalI emisTransformDalI = DalProvider.factoryEmisTransformDal();
				EmisCsvCodeMap codeMap = emisTransformDalI.getCodeMapping(false, 654010L);
				LOG.debug("Publisher common test " + codeMap);

				boolean wasAdminApplied = emisTransformDalI.wasAdminCacheApplied(serviceId);
				LOG.debug("Publisher common (hibernate) test " + wasAdminApplied);

				//sftp reader
				LOG.debug("Doing SFTP reader");
				EntityManager entityManager = ConnectionManager.getSftpReaderEntityManager();
				PreparedStatement ps = null;
				SessionImpl session = (SessionImpl) entityManager.getDelegate();
				Connection connection = session.connection();
				String sql = null;
				if (ConnectionManager.isPostgreSQL(connection)) {
					sql = "SELECT instance_name FROM configuration.instance ORDER BY instance_name";
				} else {
					sql = "SELECT instance_name FROM instance ORDER BY instance_name";
				}
				ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				rs.next();
				LOG.debug("SFTP Reader test " + rs.getString(1));
				ps.close();
				entityManager.close();

				//publisher transform
				LOG.debug("Doing publisher transform");
				ResourceIdTransformDalI resourceIdTransformDalI = DalProvider.factoryResourceIdTransformDal();
				List<Reference> references = new ArrayList<>();
				UUID patientId = patientIds.get(0);
				references.add(ReferenceHelper.createReference(ResourceType.Patient, patientId.toString()));
				Map<Reference, Reference> map = resourceIdTransformDalI.findSourceReferencesFromEdsReferences(serviceId, references);
				LOG.debug("publisher transform done " + map);

				//ehr
				LOG.debug("Doing EHR");
				ResourceDalI resourceDalI = DalProvider.factoryResourceDal();
				ResourceWrapper wrapper = resourceDalI.getCurrentVersion(serviceId, ResourceType.Patient.toString(), patientId);
				LOG.debug("EHR done " + (wrapper != null));

				for (String subscriberConfigName: subscriberConfigNames) {

					//subscriber transform
					LOG.debug("Doing subscriber transform " + subscriberConfigName);
					SubscriberOrgMappingDalI subscriberOrgMappingDalI = DalProvider.factorySubscriberOrgMappingDal(subscriberConfigName);
					Long enterpriseId = subscriberOrgMappingDalI.findEnterpriseOrganisationId(serviceId.toString());
					LOG.debug("Subscriber transform on " + subscriberConfigName + " done " + enterpriseId);

					//subscriber
					LOG.debug("Doing subscribers from " + subscriberConfigName);
					List<EnterpriseConnector.ConnectionWrapper> subscriberConnections = EnterpriseConnector.openConnection(subscriberConfigName);
					for (EnterpriseConnector.ConnectionWrapper subscriberConnection : subscriberConnections) {
						Connection connection1 = subscriberConnection.getConnection();

						sql = "SELECT name FROM organization WHERE id = ?";
						ps = connection1.prepareStatement(sql);
						if (enterpriseId != null) {
							ps.setLong(1, enterpriseId);
						} else {
							//if no ID found, just use a substitute number
							ps.setLong(1, 999);
						}
						rs = ps.executeQuery();
						String orgId = null;
						if (rs.next()) {
							orgId = rs.getString(1);
						}
						LOG.debug("subscriber on " + subscriberConfigName + " (" + subscriberConnection.toString() + ") done " + orgId);
						ps.close();
						connection1.close();
					}
				}

				*//*
						FhirAudit("db_fhir_audit", true, "FhirAuditDb"),
						PublisherStaging("db_publisher_staging", false, "PublisherStagingDb"),
						DataGenerator("db_data_generator", true, "DataGeneratorDb"),
				*//*
			}

			LOG.info("Finished testing all databases");
		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static void fixEmisSnomedCodes(String odsCodeRegex) {
		LOG.info("Finished Fixing Emis Snomed codes for orgs " + odsCodeRegex);
		try {

			//find affected Code IDs
			LOG.info("Finding affected code IDs");
			Set<Long> codeIds = new HashSet<>();
			Map<Long, EmisCsvCodeMap> hmCodeCache = new HashMap<>();

			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			EmisTransformDalI mappingRepository = DalProvider.factoryEmisTransformDal();

			EntityManager publisherCommonEntityManager = ConnectionManager.getPublisherCommonEntityManager();
			SessionImpl publisherCommonSession = (SessionImpl)publisherCommonEntityManager.getDelegate();
			Connection publisherCommonConnection = publisherCommonSession.connection();

			String sql = "SELECT code_id FROM emis_csv_code_map WHERE medication = false and read_code like '%-%'";
			PreparedStatement ps = publisherCommonConnection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				long codeId = rs.getLong(1);
				codeIds.add(new Long(codeId));
			}
			ps.close();
			publisherCommonEntityManager.close();
			LOG.info("Found " + codeIds.size() + " affected code IDs");

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			List<Service> services = serviceDal.getAll();
			for (Service service: services) {

				if (odsCodeRegex != null) {

					String odsCode = service.getLocalId();
					if (Strings.isNullOrEmpty(odsCode)
							|| !Pattern.matches(odsCodeRegex, odsCode)) {
						LOG.debug("Skipping " + service + " due to regex");
						continue;
					}
				}

				LOG.info("-----------------------------------------------------------------");
				LOG.info("Doing " + service);


				List<UUID> systems = findSystemIds(service);
				for (UUID systemId: systems) {

					LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItem(systemId);
					if (!libraryItem.getName().toUpperCase().contains("EMIS")) {
						LOG.info("Skipping system " + libraryItem.getName());
						continue;
					}

					LOG.info("Doing system ID " + libraryItem.getName());

					Set<String> hsObservationsDone = new HashSet<>();
					Set<String> hsDiariesDone = new HashSet<>();
					Set<String> hsConsultationsDone = new HashSet<>();
					Set<String> hsSlotsDone = new HashSet<>();

					EmisCsvHelper helper = new EmisCsvHelper(service.getId(), systemId, null, null, null);

					ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
					List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
					LOG.info("Found " + exchanges.size() + " exchanges");

					int fixed = 0;
					int found = 0;
					int exchangesDone = 0;

					for (Exchange exchange: exchanges) {

						exchangesDone ++;
						if (exchangesDone % 30 == 0) {
							LOG.info("Done " + exchangesDone + " of " + exchanges.size() + " exchanges");
						}

						List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());
						if (files.isEmpty()
								|| files.size() == 1) { //custom extract
							continue;
						}

						if (!EmisCsvToFhirTransformer.shouldProcessPatientData(helper)) {
							continue;
						}

						ExchangePayloadFile observationFile = findFileOfType(files, "CareRecord_Observation");
						if (observationFile != null) {
							LOG.debug("Doing " + observationFile.getPath());

							int obsRecordsDone = 0;

							InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(observationFile.getPath());
							CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
							Iterator<CSVRecord> iterator = parser.iterator();
							while (iterator.hasNext()) {

								obsRecordsDone ++;
								if (obsRecordsDone % 1000 == 0) {
									LOG.info("Done " + obsRecordsDone + " records");
								}

								CSVRecord record = iterator.next();
								String observationGuid = record.get("ObservationGuid");
								if (hsObservationsDone.contains(observationGuid)) {
									continue;
								}
								hsObservationsDone.add(observationGuid);

								String deleted = record.get("Deleted");
								if (deleted.equalsIgnoreCase("true")) {
									continue;
								}

								String codeIdStr = record.get("CodeId");
								Long codeId = Long.valueOf(codeIdStr);
								if (!codeIds.contains(codeId)) {
									continue;
								}

								found++;

								if (found % 100 == 0) {
									LOG.info("Found " + found + " records and fixed " + fixed);
								}

								EmisCsvCodeMap codeObj = hmCodeCache.get(codeId);
								if (codeObj == null) {
									codeObj = mappingRepository.getCodeMapping(false, codeId);
									hmCodeCache.put(codeId, codeObj);
								}
								String desiredCode = codeObj.getAdjustedCode();

								String patientGuid = record.get("PatientGuid");
								CsvCell observationCell = CsvCell.factoryDummyWrapper(observationGuid);
								CsvCell patientCell = CsvCell.factoryDummyWrapper(patientGuid);

								Set<ResourceType> resourceTypes = ObservationTransformer.findOriginalTargetResourceTypes(helper, patientCell, observationCell);
								for (ResourceType resourceType : resourceTypes) {

									String sourceId = EmisCsvHelper.createUniqueId(patientCell, observationCell);
									UUID uuid = IdHelper.getEdsResourceId(service.getId(), resourceType, sourceId);

									//need to get from history, so we get the version UUID
									//ResourceWrapper wrapper = resourceDal.getCurrentVersion(service.getId(), resourceType.toString(), uuid);
									List<ResourceWrapper> history = resourceDal.getResourceHistory(service.getId(), resourceType.toString(), uuid);
									if (history.isEmpty()) {
										continue;
									}
									ResourceWrapper wrapper = history.get(0);
									if (wrapper.isDeleted()) {
										continue;
									}

									Resource resource = wrapper.getResource();

									String oldCode = null;

									if (resourceType == ResourceType.Condition) {
										Condition condition = (Condition) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(condition.getCode());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.Procedure) {
										Procedure procedure = (Procedure) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(procedure.getCode());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.AllergyIntolerance) {
										AllergyIntolerance allergyIntolerance = (AllergyIntolerance) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(allergyIntolerance.getSubstance());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.FamilyMemberHistory) {
										FamilyMemberHistory familyMemberHistory = (FamilyMemberHistory) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(familyMemberHistory.getCondition().get(0).getCode());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.Immunization) {
										Immunization immunization = (Immunization) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(immunization.getVaccineCode());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.DiagnosticOrder) {
										DiagnosticOrder diagnosticOrder = (DiagnosticOrder) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(diagnosticOrder.getItem().get(0).getCode());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.Specimen) {
										Specimen specimen = (Specimen) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(specimen.getType());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.DiagnosticReport) {
										DiagnosticReport spediagnosticReportimen = (DiagnosticReport) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(spediagnosticReportimen.getCode());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.ReferralRequest) {
										ReferralRequest referralRequest = (ReferralRequest) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(referralRequest.getServiceRequested().get(0));
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);

									} else if (resourceType == ResourceType.Observation) {
										Observation observation = (Observation) resource;
										Coding coding = ObservationCodeHelper.findOriginalCoding(observation.getCode());
										oldCode = coding.getCode();
										if (oldCode.equals(desiredCode)) {
											continue;
										}

										coding.setCode(desiredCode);
									} else {
										throw new Exception("Unexpected resource type " + resourceType + " for ID " + uuid);
									}

									String newJson = FhirSerializationHelper.serializeResource(resource);
									wrapper.setResourceData(newJson);

									//service_id, resource_id, resource_type, patient_id, term, old_original_code, new_original_code
									sql = "INSERT INTO tmp.emis_code_fix VALUES (?, ?, ?, ?, ?, ?, ?)";

									EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
									SessionImpl edsSession = (SessionImpl) edsEntityManager.getDelegate();
									Connection edsConnection = edsSession.connection();

									ps = edsConnection.prepareStatement(sql);

									edsEntityManager.getTransaction().begin();

									int col = 1;
									ps.setString(col++, service.getId().toString());
									ps.setString(col++, wrapper.getPatientId().toString());
									ps.setString(col++, wrapper.getResourceId().toString());
									ps.setString(col++, wrapper.getResourceType());
									ps.setString(col++, codeObj.getReadTerm());
									ps.setString(col++, oldCode);
									ps.setString(col++, desiredCode);

									ps.executeUpdate();
									edsEntityManager.getTransaction().commit();

									ps.close();
									edsEntityManager.close();

									saveResourceWrapper(service.getId(), wrapper);

									fixed++;
								}
							}
							parser.close();
						}

						ExchangePayloadFile diaryFile = findFileOfType(files, "CareRecord_Diary");
						if (diaryFile != null) {
							LOG.debug("Doing " + diaryFile.getPath());

							int diaryRecords = 0;

							InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(diaryFile.getPath());
							CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
							Iterator<CSVRecord> iterator = parser.iterator();
							while (iterator.hasNext()) {

								diaryRecords ++;
								if (diaryRecords % 1000 == 0) {
									LOG.info("Done " + diaryRecords + " records");
								}

								CSVRecord record = iterator.next();
								String diaryGuid = record.get("DiaryGuid");
								if (hsDiariesDone.contains(diaryGuid)) {
									continue;
								}
								hsDiariesDone.add(diaryGuid);

								String deleted = record.get("Deleted");
								if (deleted.equalsIgnoreCase("true")) {
									continue;
								}

								String codeIdStr = record.get("CodeId");
								Long codeId = Long.valueOf(codeIdStr);
								if (!codeIds.contains(codeId)) {
									continue;
								}

								found++;

								if (found % 100 == 0) {
									LOG.info("Found " + found + " records and fixed " + fixed);
								}

								EmisCsvCodeMap codeObj = hmCodeCache.get(codeId);
								if (codeObj == null) {
									codeObj = mappingRepository.getCodeMapping(false, codeId);
									hmCodeCache.put(codeId, codeObj);
								}
								String desiredCode = codeObj.getAdjustedCode();

								String patientGuid = record.get("PatientGuid");
								CsvCell diaryCell = CsvCell.factoryDummyWrapper(diaryGuid);
								CsvCell patientCell = CsvCell.factoryDummyWrapper(patientGuid);


								String sourceId = EmisCsvHelper.createUniqueId(patientCell, diaryCell);
								UUID uuid = IdHelper.getEdsResourceId(service.getId(), ResourceType.ProcedureRequest, sourceId);
								if (uuid == null) {
									continue;
								}

								//need to get from history, so we get the version UUID
								//ResourceWrapper wrapper = resourceDal.getCurrentVersion(service.getId(), resourceType.toString(), uuid);
								List<ResourceWrapper> history = resourceDal.getResourceHistory(service.getId(), ResourceType.ProcedureRequest.toString(), uuid);
								if (history.isEmpty()) {
									continue;
								}
								ResourceWrapper wrapper = history.get(0);
								if (wrapper.isDeleted()) {
									continue;
								}

								Resource resource = wrapper.getResource();

								String oldCode = null;

								ProcedureRequest procedureRequest = (ProcedureRequest) resource;
								Coding coding = ObservationCodeHelper.findOriginalCoding(procedureRequest.getCode());
								oldCode = coding.getCode();
								if (oldCode.equals(desiredCode)) {
									continue;
								}

								coding.setCode(desiredCode);


								String newJson = FhirSerializationHelper.serializeResource(resource);
								wrapper.setResourceData(newJson);

								//service_id, resource_id, resource_type, patient_id, term, old_original_code, new_original_code
								sql = "INSERT INTO tmp.emis_code_fix VALUES (?, ?, ?, ?, ?, ?, ?)";

								EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
								SessionImpl edsSession = (SessionImpl) edsEntityManager.getDelegate();
								Connection edsConnection = edsSession.connection();

								ps = edsConnection.prepareStatement(sql);

								edsEntityManager.getTransaction().begin();

								int col = 1;
								ps.setString(col++, service.getId().toString());
								ps.setString(col++, wrapper.getPatientId().toString());
								ps.setString(col++, wrapper.getResourceId().toString());
								ps.setString(col++, wrapper.getResourceType());
								ps.setString(col++, codeObj.getReadTerm());
								ps.setString(col++, oldCode);
								ps.setString(col++, desiredCode);

								ps.executeUpdate();
								edsEntityManager.getTransaction().commit();

								ps.close();
								edsEntityManager.close();

								saveResourceWrapper(service.getId(), wrapper);

								fixed++;
							}
							parser.close();
						}

						ExchangePayloadFile consultationFile = findFileOfType(files, "CareRecord_Consultation");
						if (consultationFile != null) {
							LOG.debug("Doing " + consultationFile.getPath());

							int consultationRecordsDone = 0;

							InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(consultationFile.getPath());
							CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
							Iterator<CSVRecord> iterator = parser.iterator();
							while (iterator.hasNext()) {

								consultationRecordsDone ++;
								if (consultationRecordsDone % 1000 == 0) {
									LOG.info("Done " + consultationRecordsDone + " records");
								}

								CSVRecord record = iterator.next();
								String consultationGuid = record.get("ConsultationGuid");
								if (hsConsultationsDone.contains(consultationGuid)) {
									continue;
								}
								hsConsultationsDone.add(consultationGuid);

								String deleted = record.get("Deleted");
								if (deleted.equalsIgnoreCase("true")) {
									continue;
								}

								String codeIdStr = record.get("ConsultationSourceCodeId");
								if (Strings.isNullOrEmpty(codeIdStr)) {
									continue;
								}
								Long codeId = Long.valueOf(codeIdStr);
								if (!codeIds.contains(codeId)) {
									continue;
								}

								found++;

								if (found % 100 == 0) {
									LOG.info("Found " + found + " records and fixed " + fixed);
								}

								EmisCsvCodeMap codeObj = hmCodeCache.get(codeId);
								if (codeObj == null) {
									codeObj = mappingRepository.getCodeMapping(false, codeId);
									hmCodeCache.put(codeId, codeObj);
								}
								String desiredCode = codeObj.getAdjustedCode();

								String patientGuid = record.get("PatientGuid");
								CsvCell consultationCell = CsvCell.factoryDummyWrapper(consultationGuid);
								CsvCell patientCell = CsvCell.factoryDummyWrapper(patientGuid);


								String sourceId = EmisCsvHelper.createUniqueId(patientCell, consultationCell);
								UUID uuid = IdHelper.getEdsResourceId(service.getId(), ResourceType.Encounter, sourceId);

								//need to get from history, so we get the version UUID
								//ResourceWrapper wrapper = resourceDal.getCurrentVersion(service.getId(), resourceType.toString(), uuid);
								List<ResourceWrapper> history = resourceDal.getResourceHistory(service.getId(), ResourceType.Encounter.toString(), uuid);
								if (history.isEmpty()) {
									continue;
								}
								ResourceWrapper wrapper = history.get(0);
								if (wrapper.isDeleted()) {
									continue;
								}

								Resource resource = wrapper.getResource();

								String oldCode = null;

								Encounter encounter = (Encounter) resource;
								Extension extension = ExtensionConverter.findExtension(encounter, FhirExtensionUri.ENCOUNTER_SOURCE);
								if (extension == null
										|| !extension.hasValue()) {
									continue;
								}
								CodeableConcept codeableConcept = (CodeableConcept)extension.getValue();
								Coding coding = ObservationCodeHelper.findOriginalCoding(codeableConcept);
								oldCode = coding.getCode();
								if (oldCode.equals(desiredCode)) {
									continue;
								}

								coding.setCode(desiredCode);

								String newJson = FhirSerializationHelper.serializeResource(resource);
								wrapper.setResourceData(newJson);

								//service_id, resource_id, resource_type, patient_id, term, old_original_code, new_original_code
								sql = "INSERT INTO tmp.emis_code_fix VALUES (?, ?, ?, ?, ?, ?, ?)";

								EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
								SessionImpl edsSession = (SessionImpl) edsEntityManager.getDelegate();
								Connection edsConnection = edsSession.connection();

								ps = edsConnection.prepareStatement(sql);

								edsEntityManager.getTransaction().begin();

								int col = 1;
								ps.setString(col++, service.getId().toString());
								ps.setString(col++, wrapper.getPatientId().toString());
								ps.setString(col++, wrapper.getResourceId().toString());
								ps.setString(col++, wrapper.getResourceType());
								ps.setString(col++, codeObj.getReadTerm());
								ps.setString(col++, oldCode);
								ps.setString(col++, desiredCode);

								ps.executeUpdate();
								edsEntityManager.getTransaction().commit();

								ps.close();
								edsEntityManager.close();

								saveResourceWrapper(service.getId(), wrapper);

								fixed++;
							}
							parser.close();
						}

						ExchangePayloadFile slotFile = findFileOfType(files, "Appointment_slot");
						if (slotFile != null) {
							LOG.debug("Doing " + slotFile.getPath());

							int slotRecordsDone = 0;

							InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(slotFile.getPath());
							CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
							Iterator<CSVRecord> iterator = parser.iterator();
							while (iterator.hasNext()) {

								slotRecordsDone ++;
								if (slotRecordsDone % 1000 == 0) {
									LOG.info("Done " + slotRecordsDone + " records");
								}

								CSVRecord record = iterator.next();
								String slotGuid = record.get("SlotGuid");
								if (hsSlotsDone.contains(slotGuid)) {
									continue;
								}
								hsSlotsDone.add(slotGuid);

								String deleted = record.get("Deleted");
								if (deleted.equalsIgnoreCase("true")) {
									continue;
								}

								String codeIdStr = record.get("DnaReasonCodeId");
								Long codeId = Long.valueOf(codeIdStr);
								if (!codeIds.contains(codeId)) {
									continue;
								}

								found++;

								if (found % 100 == 0) {
									LOG.info("Found " + found + " records and fixed " + fixed);
								}

								EmisCsvCodeMap codeObj = hmCodeCache.get(codeId);
								if (codeObj == null) {
									codeObj = mappingRepository.getCodeMapping(false, codeId);
									hmCodeCache.put(codeId, codeObj);
								}
								String desiredCode = codeObj.getAdjustedCode();

								String patientGuid = record.get("PatientGuid");
								CsvCell slotCell = CsvCell.factoryDummyWrapper(slotGuid);
								CsvCell patientCell = CsvCell.factoryDummyWrapper(patientGuid);


								String sourceId = EmisCsvHelper.createUniqueId(patientCell, slotCell);
								UUID uuid = IdHelper.getEdsResourceId(service.getId(), ResourceType.Appointment, sourceId);

								//need to get from history, so we get the version UUID
								//ResourceWrapper wrapper = resourceDal.getCurrentVersion(service.getId(), resourceType.toString(), uuid);
								List<ResourceWrapper> history = resourceDal.getResourceHistory(service.getId(), ResourceType.Appointment.toString(), uuid);
								if (history.isEmpty()) {
									continue;
								}
								ResourceWrapper wrapper = history.get(0);
								if (wrapper.isDeleted()) {
									continue;
								}

								Resource resource = wrapper.getResource();

								String oldCode = null;

								Appointment encounter = (Appointment) resource;
								Extension extension = ExtensionConverter.findExtension(encounter, FhirExtensionUri.APPOINTMENT_DNA_REASON_CODE);
								if (extension == null
										|| !extension.hasValue()) {
									continue;
								}
								CodeableConcept codeableConcept = (CodeableConcept)extension.getValue();
								Coding coding = ObservationCodeHelper.findOriginalCoding(codeableConcept);
								oldCode = coding.getCode();
								if (oldCode.equals(desiredCode)) {
									continue;
								}

								coding.setCode(desiredCode);

								String newJson = FhirSerializationHelper.serializeResource(resource);
								wrapper.setResourceData(newJson);

								//service_id, resource_id, resource_type, patient_id, term, old_original_code, new_original_code
								sql = "INSERT INTO tmp.emis_code_fix VALUES (?, ?, ?, ?, ?, ?, ?)";

								EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
								SessionImpl edsSession = (SessionImpl) edsEntityManager.getDelegate();
								Connection edsConnection = edsSession.connection();

								ps = edsConnection.prepareStatement(sql);

								edsEntityManager.getTransaction().begin();

								int col = 1;
								ps.setString(col++, service.getId().toString());
								ps.setString(col++, wrapper.getPatientId().toString());
								ps.setString(col++, wrapper.getResourceId().toString());
								ps.setString(col++, wrapper.getResourceType());
								ps.setString(col++, codeObj.getReadTerm());
								ps.setString(col++, oldCode);
								ps.setString(col++, desiredCode);

								ps.executeUpdate();
								edsEntityManager.getTransaction().commit();

								ps.close();
								edsEntityManager.close();

								saveResourceWrapper(service.getId(), wrapper);

								fixed++;
							}
							parser.close();
						}
					}

					LOG.info("Done " + exchangesDone + " of " + exchanges.size() + " exchanges");
					LOG.info("Found " + found + " records and fixed " + fixed);
				}
			}

			LOG.info("Finished Fixing Emis Snomed codes for orgs " + odsCodeRegex);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void populatePatientSearchEpisodeOdsCode() {
		LOG.info("Populating Patient Search Episode ODS Codes");
		try {

			EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
			SessionImpl edsSession = (SessionImpl)edsEntityManager.getDelegate();
			Connection edsConnection = edsSession.connection();

			int done = 0;

			String sql = "SELECT service_id, patient_id, episode_id FROM tmp.patient_search_episode_tmp WHERE done = 0";
			PreparedStatement ps = edsConnection.prepareStatement(sql);
			ps.setFetchSize(1000);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {

				String serviceId = rs.getString(1);
				String patientId = rs.getString(2);
				String episodeId = rs.getString(3);

				ResourceDalI resourceDal = DalProvider.factoryResourceDal();
				EpisodeOfCare episodeOfCare = (EpisodeOfCare)resourceDal.getCurrentVersionAsResource(UUID.fromString(serviceId), ResourceType.EpisodeOfCare, episodeId);
				if (episodeOfCare != null
						&& episodeOfCare.hasManagingOrganization()) {

					Reference orgReference = episodeOfCare.getManagingOrganization();
					ReferenceComponents comps = org.endeavourhealth.common.fhir.ReferenceHelper.getReferenceComponents(orgReference);
					ResourceType type = comps.getResourceType();
					String id = comps.getId();

					resourceDal = DalProvider.factoryResourceDal();
					Organization org = (Organization)resourceDal.getCurrentVersionAsResource(UUID.fromString(serviceId), type, id);
					if (org != null) {
						String orgOdsCode = IdentifierHelper.findOdsCode(org);

						EntityManager edsEntityManager2 = ConnectionManager.getEdsEntityManager();
						SessionImpl edsSession2 = (SessionImpl)edsEntityManager2.getDelegate();
						Connection edsConnection2 = edsSession2.connection();

						sql = "UPDATE patient_search_episode SET ods_code = ? WHERE service_id = ? AND patient_id = ? AND episode_id = ?";
						PreparedStatement ps2 = edsConnection2.prepareStatement(sql);

						edsEntityManager2.getTransaction().begin();

						ps2.setString(1, orgOdsCode);
						ps2.setString(2, serviceId);
						ps2.setString(3, patientId);
						ps2.setString(4, episodeId);
						ps2.executeUpdate();
						edsEntityManager2.getTransaction().commit();

						ps2.close();
						edsEntityManager2.close();

					}
				}

				EntityManager edsEntityManager2 = ConnectionManager.getEdsEntityManager();
				SessionImpl edsSession2 = (SessionImpl)edsEntityManager2.getDelegate();
				Connection edsConnection2 = edsSession2.connection();

				sql = "UPDATE tmp.patient_search_episode_tmp SET done = ? WHERE service_id = ? AND patient_id = ? AND episode_id = ?";
				PreparedStatement ps2 = edsConnection2.prepareStatement(sql);

				edsEntityManager2.getTransaction().begin();
				ps2.setBoolean(1, true);
				ps2.setString(2, serviceId);
				ps2.setString(3, patientId);
				ps2.setString(4, episodeId);
				ps2.executeUpdate();

				edsEntityManager2.getTransaction().commit();

				ps2.close();
				edsEntityManager2.close();

				done ++;
				if (done % 100 == 0) {
					LOG.debug("Done " + done);
				}
			}
			rs.close();
			ps.close();

			LOG.debug("Done " + done);
			LOG.info("Finished Populating Patient Search Episode ODS Codes");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void fixEmisDrugRecords(String odsCodeRegex) {
		LOG.info("Fixing Emis drug records");
		try {
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			List<Service> services = serviceDal.getAll();
			for (Service service: services) {

				if (odsCodeRegex != null) {

					String odsCode = service.getLocalId();
					if (Strings.isNullOrEmpty(odsCode)
						|| !Pattern.matches(odsCodeRegex, odsCode)) {
						LOG.debug("Skipping " + service + " due to regex");
						continue;
					}
				}

				//check if Emis
				String notes = service.getNotes();
				if (notes == null || !notes.contains("CDB")) {
					LOG.info("Skipping as not Emis: " + service);
					continue;
				}

				LOG.info("Doing " + service);


				List<UUID> systems = findSystemIds(service);
				for (UUID systemId: systems) {
					LOG.info("Doing system ID  " + systemId);

					LOG.info("Finding patients");
					PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
					List<UUID> patientIds = patientSearchDal.getPatientIds(service.getId());
					LOG.info("Found " + patientIds.size() + " patients");

					//create dummy exchange
					String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
					String odsCode = service.getLocalId();

					Exchange exchange = null;
					UUID exchangeId = UUID.randomUUID();

					List<UUID> batchIdsCreated = new ArrayList<>();
					FhirResourceFiler filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

					int done = 0;
					for (UUID patientId : patientIds) {

						ResourceDalI resourceDal = DalProvider.factoryResourceDal();
						List<ResourceWrapper> statementWrappers = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.MedicationStatement.toString());
						List<ResourceWrapper> orderWrappers = null;  //get on demand

						for (ResourceWrapper statementWrapper : statementWrappers) {

							MedicationStatement medicationStatement = (MedicationStatement) statementWrapper.getResource();
							if (!medicationStatement.hasStatus()) {
								continue;
							}

							MedicationStatementBuilder builder = new MedicationStatementBuilder(medicationStatement);
							boolean fixed = false;

							Date cancellationDate = null;
							Extension outerExtension = ExtensionConverter.findExtension(medicationStatement, FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION);
							if (outerExtension != null) {
								Extension innerExtension = ExtensionConverter.findExtension(outerExtension, "date");
								if (innerExtension != null) {
									DateType dt = (DateType) innerExtension.getValue();
									cancellationDate = dt.getValue();
								}
							}


							if (medicationStatement.getStatus() == MedicationStatement.MedicationStatementStatus.ACTIVE) {
								//if active then ensure there's no cancellation date
								if (cancellationDate != null) {
									builder.setCancellationDate(null);
									fixed = true;
								}

							} else if (medicationStatement.getStatus() == MedicationStatement.MedicationStatementStatus.COMPLETED) {
								//if non-active, then ensure there IS a cancellation date
								if (cancellationDate == null) {

									IssueRecordIssueDate mostRecentDate = null;

									Reference medicationStatementReference = ReferenceHelper.createReferenceExternal(medicationStatement);

									if (orderWrappers == null) {
										orderWrappers = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.MedicationOrder.toString());
									}

									for (ResourceWrapper orderWrapper : orderWrappers) {

										//quick check against the raw JSON so we don't have to deserialise the bulk of them
										String orderJson = orderWrapper.getResourceData();
										if (!orderJson.contains(medicationStatementReference.getReference())) {
											continue;
										}

										MedicationOrder order = (MedicationOrder) orderWrapper.getResource();
										MedicationOrderBuilder medicationOrderBuilder = new MedicationOrderBuilder(order);

										Reference reference = medicationOrderBuilder.getMedicationStatementReference();
										if (reference != null
												&& ReferenceHelper.equals(reference, medicationStatementReference)) {

											DateTimeType started = medicationOrderBuilder.getDateWritten();
											Integer duration = medicationOrderBuilder.getDurationDays();

											IssueRecordIssueDate obj = new IssueRecordIssueDate(started, duration);
											if (obj.afterOrOtherIsNull(mostRecentDate)) {
												mostRecentDate = obj;
											}
										}
									}

									//if no issues exist for it, use the start date of the DrugRecord
									if (mostRecentDate == null) {
										Date d = medicationStatement.getDateAsserted();
										mostRecentDate = new IssueRecordIssueDate(new DateTimeType(d), new Integer(0));
									}

									Date d = mostRecentDate.getIssueDateType().getValue();

									int duration = 0;
									Integer intObj = mostRecentDate.getIssueDuration();
									if (intObj != null) {
										duration = intObj.intValue();
									}

									Calendar cal = Calendar.getInstance();
									cal.setTime(d);
									cal.add(Calendar.DAY_OF_YEAR, duration);

									cancellationDate = cal.getTime();

									builder.setCancellationDate(cancellationDate);
									fixed = true;
								}

							} else {
								LOG.error("Unexpected status " + medicationStatement.getStatus() + " on resource " + statementWrapper);
							}

							if (fixed) {

								if (exchange == null) {
									exchange = new Exchange();
									exchange.setId(exchangeId);
									exchange.setBody(bodyJson);
									exchange.setTimestamp(new Date());
									exchange.setHeaders(new HashMap<>());
									exchange.setHeaderAsUuid(HeaderKeys.SenderServiceUuid, service.getId());
									exchange.setHeader(HeaderKeys.ProtocolIds, ""); //just set to non-null value, so postToExchange(..) can safely recalculate
									exchange.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);
									exchange.setHeaderAsUuid(HeaderKeys.SenderSystemUuid, systemId);
									exchange.setHeader(HeaderKeys.SourceSystem, MessageFormat.EMIS_CSV);
									exchange.setServiceId(service.getId());
									exchange.setSystemId(systemId);

									AuditWriter.writeExchange(exchange);
									AuditWriter.writeExchangeEvent(exchange, "Manually created to re-process Emis DrugRecord data");
								}

								//save resource
								filer.savePatientResource(null, false, builder);
							}
						}

						done++;
						if (done % 100 == 0) {
							LOG.info("Done " + done + " patients");
						}
					}
					LOG.info("Done " + done + " patients");

					//close down filer
					filer.waitToFinish();

					if (exchange != null) {
						//set multicast header
						String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
						exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

						//post to Rabbit protocol queue
						List<UUID> exchangeIds = new ArrayList<>();
						exchangeIds.add(exchange.getId());
						QueueHelper.postToExchange(exchangeIds, "EdsProtocol", null, true);
					}
				}
			}

			LOG.info("Finished Fixing Emis drug records");
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}*/

	/**
	 * populates the pseudo_id table on a new-style subscriber DB (MySQL or SQL Server) with pseudo_ids generated
	 * from a salt
	 */
	/*private static void populateSubscriberPseudoId(String subscriberConfigName, String saltKeyName) {
		LOG.info("Populating subscriber DB pseudo ID for " + subscriberConfigName + " using " + saltKeyName);
		try {
			//find salt details
			JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");
			JsonNode linkDistributorsNode = config.get("pseudo_salts");
			if (linkDistributorsNode == null) {
				throw new Exception("No pseudo_salts found in config");
			}
			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(linkDistributorsNode.toString(), Object.class);
			String linkDistributors = mapper.writeValueAsString(json);
			LinkDistributorConfig[] arr = ObjectMapperPool.getInstance().readValue(linkDistributors, LinkDistributorConfig[].class);

			LinkDistributorConfig saltConfig = null;
			for (LinkDistributorConfig l : arr) {
				if (l.getSaltKeyName().equals(saltKeyName)) {
					saltConfig = l;
				}
			}
			if (saltConfig == null) {
				throw new Exception("No salt config found for " + saltKeyName);
			}

			String sql = "SELECT source_id, subscriber_id"
					+ " FROM subscriber_id_map"
					+ " WHERE source_id LIKE '" + ResourceType.Patient.toString() + "%'"
					+ " AND subscriber_table = " + SubscriberTableId.PATIENT.getId();

			Map<String, Long> hmPatients = new HashMap<>();

			EntityManager subscriberTransformEntityManager = ConnectionManager.getSubscriberTransformEntityManager(subscriberConfigName);
			SessionImpl subscriberTransformSession = (SessionImpl)subscriberTransformEntityManager.getDelegate();
			Connection subscriberTransformConnection = subscriberTransformSession.connection();
			PreparedStatement ps = subscriberTransformConnection.prepareStatement(sql);
			ps.setFetchSize(1000);

			LOG.info("Running query to find patients");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String sourceId = rs.getString(1);
				Long subscriberId = rs.getLong(2);
				hmPatients.put(sourceId, subscriberId);
				if (hmPatients.size() % 5000 == 0) {
					LOG.info("Found " + hmPatients.size());
				}
			}
			ps.close();
			subscriberTransformEntityManager.clear();
			LOG.info("Query done, found " + hmPatients.size() + " patients");

			int done = 0;
			int skipped = 0;

			File fixFile = new File("FIX_" + subscriberConfigName + "_" + saltKeyName + ".sql");
			PrintWriter fixWriter = new PrintWriter(new BufferedWriter(new FileWriter(fixFile)));

			File errorFile = new File("ERRORS_" + subscriberConfigName + "_" + saltKeyName + ".txt");
			PrintWriter errorWriter = new PrintWriter(new BufferedWriter(new FileWriter(errorFile)));

			LOG.info("Starting to process patients");

			String fixSql = "DROP TABLE IF EXISTS pseudo_id_tmp;";
			fixWriter.println(fixSql);

			fixSql = "CREATE TABLE pseudo_id_tmp (id bigint, patient_id bigint, salt_key_name varchar(50), pseudo_id varchar(255));";
			fixWriter.println(fixSql);

			List<String> batch = new ArrayList<>();

			for (String sourceId: hmPatients.keySet()) {
				Long subscriberId = hmPatients.get(sourceId);

				Reference ref = ReferenceHelper.createReference(sourceId);
				String patientUuidStr = ReferenceHelper.getReferenceId(ref);
				UUID patientUuid = UUID.fromString(patientUuidStr);

				//need to find the service ID
				PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
				PatientSearch patientSearch = patientSearchDal.searchByPatientId(patientUuid);
				if (patientSearch == null) {
					errorWriter.println("Failed to find patient search record for " + sourceId + " with subscriber ID " + subscriberId);
					skipped ++;
					continue;
				}

				//find current FHIR patient
				UUID serviceId = patientSearch.getServiceId();
				ResourceDalI resourceDal = DalProvider.factoryResourceDal();
				Patient patient = (Patient)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientUuidStr);
				if (patient == null) {
					errorWriter.println("Null FHIR Patient for " + sourceId + " with subscriber ID " + subscriberId);
					skipped ++;
					continue;
				}

				String pseudoId = PseudoIdBuilder.generatePsuedoIdFromConfig(subscriberConfigName, saltConfig, patient);

				//need to store in our pseudo ID mapping table
				if (pseudoId != null) {
					PseudoIdDalI pseudoIdDal = DalProvider.factoryPseudoIdDal(subscriberConfigName);
					pseudoIdDal.saveSubscriberPseudoId(patientUuid, subscriberId.longValue(), saltKeyName, pseudoId);

					String pseudoIdRowSourceId = ReferenceHelper.createReferenceExternal(patient).getReference() + PatientTransformer.PREFIX_PSEUDO_ID + saltKeyName;

					SubscriberResourceMappingDalI enterpriseIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
					SubscriberId pseudoIdRowId = enterpriseIdDal.findOrCreateSubscriberId(SubscriberTableId.PSEUDO_ID.getId(), pseudoIdRowSourceId);

					batch.add("(" + pseudoIdRowId.getSubscriberId() + ", " + subscriberId + ", '" + saltKeyName + "', '" + pseudoId + "')");
					if (batch.size() >= 50) {
						fixSql = "INSERT INTO pseudo_id_tmp (id, patient_id, salt_key_name, pseudo_id) VALUES " + String.join(", ", batch) + ";";
						fixWriter.println(fixSql);
						batch.clear();
					}

					//fixSql = "INSERT INTO pseudo_id_tmp (id, patient_id, salt_key_name, pseudo_id) VALUES (" + pseudoIdRowId.getSubscriberId() + ", " + subscriberId + ", '" + saltKeyName + "', '" + pseudoId + "');";
					//fixWriter.println(fixSql);
				}

				done ++;
				if (done % 1000 == 0) {
					LOG.info("Done " + done + ", skipped " + skipped);
				}
			}

			if (!batch.isEmpty()) {
				fixSql = "INSERT INTO pseudo_id_tmp (id, patient_id, salt_key_name, pseudo_id) VALUES " + String.join(", ", batch) + ";";
				fixWriter.println(fixSql);
			}

			fixSql = "CREATE INDEX ix ON pseudo_id_tmp (patient_id);";
			fixWriter.println(fixSql);

			fixSql = "DELETE FROM pseudo_id WHERE salt_key_name = '" + saltKeyName + "';";
			fixWriter.println(fixSql);

			fixSql = "INSERT INTO pseudo_id SELECT t.id, t.patient_id, t.salt_key_name, t.pseudo_id FROM pseudo_id_tmp t INNER JOIN patient p ON p.id = t.patient_id;";
			fixWriter.println(fixSql);

			fixSql = "DROP TABLE pseudo_id_tmp;";
			fixWriter.println(fixSql);

			fixWriter.close();
			errorWriter.close();

			LOG.info("Finished Populating subscriber DB pseudo ID for " + subscriberConfigName + " using " + saltKeyName);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void investigateMissingPatients(String nhsNumberFile, String protocolName, String subscriberConfigName, String ccgCodeRegex) {
		LOG.info("Investigating Missing Patients from " + nhsNumberFile + " in Protocol " + protocolName);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

			String salt = null;
			JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");
			ArrayNode linked = (ArrayNode)config.get("linkedDistributors");

			for (int i=0; i<linked.size(); i++) {
				JsonNode linkedElement = linked.get(i);
				String name = linkedElement.get("saltKeyName").asText();
				if (name.equals("EGH")) {
					salt = linkedElement.get("salt").asText();
				}
			}

			//go through file and check
			File inputFile = new File(nhsNumberFile);
			if (!inputFile.exists()) {
				throw new Exception(nhsNumberFile + " doesn't exist");
			}
			List<String> nhsNumbers = Files.readAllLines(inputFile.toPath());
			LOG.info("Found " + nhsNumbers.size());

			String fileName = FilenameUtils.getBaseName(nhsNumberFile);
			File outputCsvFile = new File("OUTPUT_" + fileName + ".csv");
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputCsvFile));
			CSVPrinter outputPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT.withHeader("nhs_number", "pseudo_id", "finding", "comment"));

			File outputTextFile = new File("OUTPUT_" + nhsNumberFile);

			List<String> lines = new ArrayList<>();

			for (String nhsNumber: nhsNumbers) {
				LOG.debug("Doing " + nhsNumber);

				PseudoIdBuilder b = new PseudoIdBuilder(subscriberConfigName, "EGH", salt);
				b.addValueNhsNumber("NhsNumber", nhsNumber, null);
				String calcPseudoId = b.createPseudoId();

				String finding = null;
				String comment = null;

				lines.add(">>>>>>>>> " + nhsNumber + " <<<<<<<<<<");

				EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
				SessionImpl edsSession = (SessionImpl)edsEntityManager.getDelegate();
				Connection edsConnection = edsSession.connection();

				String sql = "select patient_id, service_id, local_id, ccg_code" +
						" from eds.patient_link_person p" +
						" inner join eds.patient_link_history h" +
						" on h.new_person_id = p.person_id" +
						" inner join admin.service s" +
						" on s.id = service_id" +
						" where nhs_number = ?" +
						" and organisation_type = 'PR'";

				PreparedStatement ps = edsConnection.prepareStatement(sql);
				ps.setString(1, nhsNumber);

				List<PatientInfo> patientInfos = new ArrayList<>();

				//LOG.debug(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {

					PatientInfo info = new PatientInfo();
					info.patientUuid = rs.getString(1);
					info.serviceUuid = rs.getString(2);
					info.odsCode = rs.getString(3);
					info.ccgCode = rs.getString(4);
					patientInfos.add(info);
				}

				ps.close();
				edsEntityManager.close();

				//check to see if the patient does exist in the CCG but has been deleted or had their NHS number changed
				for (PatientInfo info: patientInfos) {

					lines.add("Found " + info);

					if (!Pattern.matches(ccgCodeRegex, info.ccgCode)) {
						lines.add("Ignoring as out of CCG area");
						continue;
					}

					ResourceDalI resourceDal = DalProvider.factoryResourceDal();
					List<ResourceWrapper> history = resourceDal.getResourceHistory(UUID.fromString(info.serviceUuid), ResourceType.Patient.toString(), UUID.fromString(info.patientUuid));
					if (history.isEmpty()) {
						lines.add("No history found for patient");

						finding = "ERROR";
						comment = "Couldn't find FHIR resource history";

						continue;
					}

					ResourceWrapper current = history.get(0);
					if (current.isDeleted()) {
						lines.add("Patient resource is deleted");

						*//*finding = "Deleted";
						comment = "Patient record has been deleted from DDS";*//*
						continue;
					}

					Patient currentFhir = (Patient) current.getResource();
					String currentNhsNumber = IdentifierHelper.findNhsNumber(currentFhir);
					lines.add("Current NHS number = " + currentNhsNumber);
					if (!currentNhsNumber.equals(nhsNumber)) {

						boolean nhsNumberChanged = false;

						for (int i=1; i<history.size(); i++) {
							ResourceWrapper wrapper = history.get(i);
							if (wrapper.isDeleted()) {
								continue;
							}
							Patient past = (Patient) wrapper.getResource();
							String pastNhsNumber = IdentifierHelper.findNhsNumber(past);
							lines.add("History " + i + " has NHS number " + pastNhsNumber);

							if (pastNhsNumber != null
								&& pastNhsNumber.equals(nhsNumber)) {

								ResourceWrapper wrapperChanged = history.get(i-1);
								String changedNhsNumber = IdentifierHelper.findNhsNumber(past);
								lines.add("NHS number changed from " + nhsNumber + " to " + changedNhsNumber + " on " + sdf.format(wrapperChanged.getCreatedAt()));

								finding = "NHS number changed";
								comment = "NHS number changed on " + sdf.format(wrapperChanged.getCreatedAt());
								nhsNumberChanged = true;
								break;
							}
						}

						if (nhsNumberChanged) {
							continue;
						}
					}

					//if NHS number didn't change, then it SHOULD match the existing DB

					SubscriberResourceMappingDalI subscriberResourceMappingDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
					Long enterpriseId = subscriberResourceMappingDal.findEnterpriseIdOldWay(ResourceType.Patient.toString(), info.patientUuid);
					if (enterpriseId == null) {
						finding = "ERROR";
						comment = "Matches current NHS number, so should be in subscriber DB but can't find enterprise ID";

						lines.add("" + info.patientUuid + ": no enterprise ID found");
						continue;
					}

					List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openConnection(subscriberConfigName);
					EnterpriseConnector.ConnectionWrapper first = connectionWrappers.get(0);

					sql = "SELECT id, target_skid"
							+ " FROM patient"
							+ " LEFT OUTER JOIN link_distributor"
							+ " ON patient.pseudo_id = link_distributor.source_skid"
							+ " WHERE patient.id = ?";

					Connection enterpriseConnection = first.getConnection();
					PreparedStatement enterpriseStatement = enterpriseConnection.prepareStatement(sql);
					enterpriseStatement.setLong(1, enterpriseId.longValue());

					rs = enterpriseStatement.executeQuery();
					if (rs.next()) {

						long id = rs.getLong(1);
						String pseudoId = rs.getString(2);

						lines.add("" + info.patientUuid + ": enterprise ID " + id + " with pseudo ID " + pseudoId);
						lines.add("" + info.patientUuid + ": expected pseudo ID " + calcPseudoId);

						if (pseudoId.equals(calcPseudoId)) {
							finding = "Match";
							comment = "Matches current NHS number and is in subscriber DB";

							lines.add("" + info.patientUuid + ": found in subscriber DB with right pseudo ID");

						} else {
							finding = "Mis-match";
							comment = "Matches current NHS number and is in subscriber DB but pseudo ID is different";

							lines.add("" + info.patientUuid + ": found in subscriber DB but with wrong pseudo ID");
						}

					} else {

						finding = "ERROR";
						comment = "Matches current NHS number and enterprise ID = " + enterpriseId + " but not in DB";
					}

					enterpriseStatement.close();
					enterpriseConnection.close();

					continue;
				}

				//if we've not found anything above, check patient_search for the NHS number to see if we can work out where they are
				if (finding == null) {
					lines.add("Checking patient_search");

					//check patient search
					edsEntityManager = ConnectionManager.getEdsEntityManager();
					edsSession = (SessionImpl)edsEntityManager.getDelegate();
					edsConnection = edsSession.connection();

					sql = "select local_id, ccg_code, pse.registration_start"
							+ " from eds.patient_search ps"
							+ " inner join admin.service s"
							+ " on s.id = ps.service_id"
							+ " and s.organisation_type = 'PR'"
							+ " inner join eds.patient_search_episode pse"
							+ " on pse.service_id = ps.service_id"
							+ " and pse.patient_id = ps.patient_id"
							+ " and pse.registration_end is null"
							+ " where nhs_number = ?"
							+ " order by pse.registration_start desc"
							+ " limit 1";

					ps = edsConnection.prepareStatement(sql);
					ps.setString(1, nhsNumber);

					//LOG.debug(sql);
					rs = ps.executeQuery();
					if (rs.next()) {

						String odsCode = rs.getString(1);
						String ccgCode = rs.getString(2);
						Date regDate = new Date(rs.getTimestamp(3).getTime());

						OdsOrganisation odsOrg = OdsWebService.lookupOrganisationViaRest(odsCode);
						OdsOrganisation parentOdsOrg = null;
						if (!Strings.isNullOrEmpty(ccgCode)) {
							parentOdsOrg = OdsWebService.lookupOrganisationViaRest(ccgCode);
						}

						if (odsOrg == null) {
							lines.add("Registered at " + odsCode + " but failed to find ODS record for " + odsCode);

							finding = "ERROR";
							comment = "Registered at " + odsCode + " but not found in open ODS";

						} else if (parentOdsOrg == null) {
							finding = "ERROR";
							comment = "Registered at " + odsOrg.getOdsCode() + " " + odsOrg.getOrganisationName() + " but no ODS record found for parent " + ccgCode;

						} else {
							finding = "Out of area";
							comment = "Patient registered in " + parentOdsOrg.getOdsCode() + " " + parentOdsOrg.getOrganisationName() + " since " + sdf.format(regDate);
						}

					} else {

						finding = "Unknown";
						comment = "No data for NHS number found (within scope of DDS)";
					}

					ps.close();
					edsEntityManager.close();

				}

				outputPrinter.printRecord(nhsNumber, calcPseudoId, finding, comment);
			}


			Files.write(outputTextFile.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

			outputPrinter.close();

			LOG.info("Finished Investigating Missing Patients from " + nhsNumberFile + " in Protocol " + protocolName);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	static class PatientInfo {
		String patientUuid;
		String serviceUuid;
		String odsCode;
		String ccgCode;

		@Override
		public String toString() {
			return "ods " + odsCode + ", ccgCode " + ccgCode + ", serviceUuid " + serviceUuid + ", patientUUID " + patientUuid;
		}
	}*/

	/*static class NhsNumberInfo {
		String odsCode;
		String date;
		String patientGuid;
		String patientUuid;
		String nhsNumber;
		String deleted;

		@Override
		public String toString() {
			return "ods " + odsCode + ", date " + date + ", patientGuid " + patientGuid + ", patientUUID " + patientUuid + ", NHS " + nhsNumber + ", deleted " + deleted;
		}
	}

	private static void investigateMissingPatients(String nhsNumberFile, String protocolName, String subscriberConfigName, String odsCodeRegex) {
		LOG.info("Investigating Missing Patients from " + nhsNumberFile + " in Protocol " + protocolName);
		try {

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

			//go through all publishers and find all history of NHS numbers
			File nhsNumberHistoryFile = new File(protocolName.replace(" ", "_") + "_NHS_number_history.txt");
			if (!nhsNumberHistoryFile.exists()) {
				LOG.info("Need to create NHS number history file " + nhsNumberHistoryFile);

				LibraryItem matchedLibraryItem = null;

				LibraryDalI repository = DalProvider.factoryLibraryDal();
				List<ActiveItem> activeItems = repository.getActiveItemByTypeId(Integer.valueOf(DefinitionItemType.Protocol.getValue()), Boolean.valueOf(false));
				for (ActiveItem activeItem: activeItems) {
					Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
					String xml = item.getXmlContent();
					LibraryItem libraryItem = (LibraryItem) XmlSerializer.deserializeFromString(LibraryItem.class, xml, (String)null);
					String name = libraryItem.getName();
					if (name.equals(protocolName)) {
						matchedLibraryItem = libraryItem;
						break;
					}
				}
				if (matchedLibraryItem == null) {
					throw new Exception("Failed to find protocol");
				}

				for (ServiceContract serviceContract: matchedLibraryItem.getProtocol().getServiceContract()) {
					if (serviceContract.getType() == ServiceContractType.SUBSCRIBER) {
						continue;
					}

					String serviceIdStr = serviceContract.getService().getUuid();
					UUID serviceId = UUID.fromString(serviceIdStr);
					Service service = serviceDal.getById(serviceId);

					String odsCode = service.getLocalId();
					if (odsCodeRegex != null
							&& !Pattern.matches(odsCodeRegex, odsCode)) {
						LOG.debug("Skipping " + service + " due to regex");
						continue;
					}

					LOG.info("Doing " + service);

					List<String> lines = new ArrayList<>();

					List<UUID> systemIds = findSystemIds(service);
					for (UUID systemId: systemIds) {
						List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
						for (int i=exchanges.size()-1; i>=0; i--) {
							Exchange exchange = exchanges.get(i);
							List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());
							ExchangePayloadFile patientFile = findFileOfType(files, "Admin_Patient");
							if (patientFile == null) {
								continue;
							}

							Date dataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);
							String dateDateStr = sdf.format(dataDate);

							//work out file version
							List<ExchangePayloadFile> filesTmp = new ArrayList<>();
							filesTmp.add(patientFile);
							String version = EmisCsvToFhirTransformer.determineVersion(filesTmp);

							//create the parser
							String path = patientFile.getPath();
							org.endeavourhealth.transform.emis.csv.schema.admin.Patient parser = new org.endeavourhealth.transform.emis.csv.schema.admin.Patient(serviceId, systemId, exchange.getId(), version, path);

							while (parser.nextRecord()) {

								CsvCell patientGuidCell = parser.getPatientGuid();
								String patientGuid = patientGuidCell.getString();

								String patientUuidStr = null;
								UUID patientUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Patient, patientGuid);
								if (patientUuid == null) {
									patientUuidStr = "NOUUID";
								} else {
									patientUuidStr = patientUuid.toString();
								}

								CsvCell nhsNumberCell = parser.getNhsNumber();
								String nhsNumber = nhsNumberCell.getString();
								if (Strings.isNullOrEmpty(nhsNumber)) {
									nhsNumber = "BLANK";
								} else {
									nhsNumber.replace(" ", "");
								}

								CsvCell deletedCell = parser.getDeleted();
								String deletedStr = deletedCell.getString();

								lines.add(odsCode + "_" + dateDateStr + "_" + patientGuid + "_" + patientUuidStr + "_" + nhsNumber + "_" + deletedStr);
							}

							parser.close();
						}
					}

					Files.write(nhsNumberHistoryFile.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

					LOG.debug("Done " + service);
				}

				LOG.info("Created NHS number history file");
			}

			Map<String, Set<String>> hmNhsNumberToPatientGuid = new HashMap<>();
			Map<String, List<NhsNumberInfo>> hmPatientGuidHistory = new HashMap<>();

			LOG.info("Reading in NHS number history");
			int total = 0;
			String currentOdsCode = null;
			int odsCodesDone = 0;
			int totalAtOdsCode = 0;

			FileReader fr = new FileReader(nhsNumberHistoryFile);
			BufferedReader br = new BufferedReader(fr);
			while (true) {
				String historyLine = br.readLine();
				if (historyLine == null) {
					break;
				}

				try {
					String[] toks = historyLine.split("_");

					NhsNumberInfo info = new NhsNumberInfo();
					info.odsCode = toks[0];
					info.date = toks[1];
					info.patientGuid = toks[2];
					info.patientUuid = toks[3];
					info.nhsNumber = toks[4];
					info.deleted = toks[5];

					//skip the Community services
					if (info.odsCode.equals("16441")
							|| info.odsCode.equals("16456")
							|| info.odsCode.equals("16962")
							|| info.odsCode.equals("16998")
							|| info.odsCode.equals("19594")
							|| info.odsCode.equals("29605")
							|| info.odsCode.equals("30159")
							|| info.odsCode.equals("R1H")
							|| info.odsCode.equals("R1H14")
							|| info.odsCode.equals("R1H15")
							|| info.odsCode.equals("RQX")
							|| info.odsCode.equals("RWKGY")) {
						continue;
					}

					if (currentOdsCode == null
							|| !currentOdsCode.equals(info.odsCode)) {
						currentOdsCode = info.odsCode;
						totalAtOdsCode = 0;
						odsCodesDone ++;
						LOG.info("Starting " + currentOdsCode + " org " + odsCodesDone);
					}


					Set<String> s = hmNhsNumberToPatientGuid.get(info.nhsNumber);
					if (s == null) {
						s = new HashSet<>();
						hmNhsNumberToPatientGuid.put(info.nhsNumber, s);
					}
					s.add(info.patientGuid);

					List<NhsNumberInfo> l2 = hmPatientGuidHistory.get(info.patientGuid);
					if (l2 == null) {
						l2 = new ArrayList<>();
						hmPatientGuidHistory.put(info.patientGuid, l2);
					}
					boolean addNew = true;
					if (!l2.isEmpty()) {
						//if this is just telling us the same as the previous one, ignore it
						NhsNumberInfo previous = l2.get(l2.size()-1);
						if (previous.nhsNumber.equals(info.nhsNumber)) {
							addNew = false;
						}
					}

					if (addNew) {
						l2.add(info);

						total ++;
						totalAtOdsCode ++;
						if (totalAtOdsCode % 10000 == 0) {
							LOG.info("Done " + totalAtOdsCode + " at " + currentOdsCode + " (total " + total + ")");
						}
					}

				} catch (Exception ex) {
					throw new Exception("Error parsing line [" + historyLine + "]", ex);
				}
			}
			LOG.info("Read in NHS number history");

			String salt = null;
			JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");
			ArrayNode linked = (ArrayNode)config.get("linkedDistributors");

			for (int i=0; i<linked.size(); i++) {
				JsonNode linkedElement = linked.get(i);
				String name = linkedElement.get("saltKeyName").asText();
				if (name.equals("EGH")) {
					salt = linkedElement.get("salt").asText();
				}
			}

			//go through file and check
			File inputFile = new File(nhsNumberFile);
			if (!inputFile.exists()) {
				throw new Exception(nhsNumberFile + " doesn't exist");
			}
			List<String> nhsNumbers = Files.readAllLines(inputFile.toPath());
			LOG.info("Found " + nhsNumbers.size());

			String fileName = FilenameUtils.getBaseName(nhsNumberFile);
			File outputCsvFile = new File("OUTPUT_" + fileName + ".csv");
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputCsvFile));
			CSVPrinter outputPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT.withHeader("nhs_number", "pseudo_id", "finding", "comment"));

			List<String> lines = new ArrayList<>();

			for (String nhsNumber: nhsNumbers) {
				LOG.debug("Doing " + nhsNumber);

				PseudoIdBuilder b = new PseudoIdBuilder(subscriberConfigName, "EGH", salt);
				b.addValueNhsNumber("NhsNumber", nhsNumber, null);
				String calcPseudoId = b.createPseudoId();

				String finding = null;
				String comment = null;

				lines.add(">>>>>>>>> " + nhsNumber + " <<<<<<<<<<");

				Set<String> patientGuids = hmNhsNumberToPatientGuid.get(nhsNumber);
				if (patientGuids != null
						&& !patientGuids.isEmpty()) {

					lines.add("Matches " + patientGuids.size() + " patient GUIDs: " + patientGuids);

					for (String patientGuid : patientGuids) {
						List<NhsNumberInfo> history = hmPatientGuidHistory.get(patientGuid);
						if (history == null) {
							throw new Exception("No history for patient GUID " + patientGuid);
						}

						//some very old data was received into AWS out of order (e.g. F84081), so need to sort them
						history.sort((o1, o2) -> o1.date.compareTo(o2.date));

						//see if it matches the CURRENT NHS number from the Emis data
						NhsNumberInfo currentInfo = history.get(history.size() - 1);
						if (currentInfo.nhsNumber.equals(nhsNumber)) {
							lines.add("" + patientGuid + ": matches CURRENT NHS number (at " + currentInfo.odsCode + "), so SHOULD be in subscriber DB");

							SubscriberResourceMappingDalI subscriberResourceMappingDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
							Long enterpriseId = subscriberResourceMappingDal.findEnterpriseIdOldWay(ResourceType.Patient.toString(), currentInfo.patientUuid);
							if (enterpriseId == null) {

								String startDateStr = TransformConfig.instance().getEmisStartDate(currentInfo.odsCode);
								Date startDate = new SimpleDateFormat("dd/MM/yyyy").parse(startDateStr);
								lines.add("Org start date " + startDateStr);

								Date receivedDate = new SimpleDateFormat("yyyyMMdd").parse(currentInfo.date);

								//if only received before the start date, then we won't have processed it
								if (receivedDate.before(startDate)) {
									lines.add("Patient data received before org start date so won't have been processed");
									//leave the finding null so we check patient_search

								} else {

									finding = "ERROR";
									comment = "Matches current NHS number, so should be in subscriber DB but can't find enterprise ID";

									lines.add("" + patientGuid + ": no enterprise ID found");
									for (NhsNumberInfo info : history) {
										lines.add("" + patientGuid + ": " + info);
									}
								}

							} else {

								List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openConnection(subscriberConfigName);
								EnterpriseConnector.ConnectionWrapper first = connectionWrappers.get(0);

								String sql = "SELECT id, target_skid"
										+ " FROM patient"
										+ " LEFT OUTER JOIN link_distributor"
										+ " ON patient.pseudo_id = link_distributor.source_skid"
										+ " WHERE patient.id = ?";

								Connection enterpriseConnection = first.getConnection();
								PreparedStatement enterpriseStatement = enterpriseConnection.prepareStatement(sql);
								enterpriseStatement.setLong(1, enterpriseId.longValue());

								ResultSet rs = enterpriseStatement.executeQuery();
								if (rs.next()) {

									long id = rs.getLong(1);
									String pseudoId = rs.getString(2);

									lines.add("" + patientGuid + ": enterprise ID " + id + " with pseudo ID " + pseudoId);


									//LOG.debug("Salt = " + salt);



									lines.add("" + patientGuid + ": expected pseudo ID " + calcPseudoId);

									if (pseudoId.equals(calcPseudoId)) {
										finding = "Match";
										comment = "Matches current NHS number and is in subscriber DB";

										lines.add("" + patientGuid + ": found in subscriber DB with right pseudo ID");

									} else {
										finding = "Mis-match";
										comment = "Matches current NHS number and is in subscriber DB but pseudo ID is different";

										lines.add("" + patientGuid + ": found in subscriber DB but with wrong pseudo ID");
									}

								} else {

									finding = "ERROR";
									comment = "Matches current NHS number and enterprise ID = " + enterpriseId + " but not in DB";
								}

								enterpriseStatement.close();
								enterpriseConnection.close();
							}

						} else {

							lines.add("" + patientGuid + ": doesn't match current NHS number (at " + currentInfo.odsCode + ") which is " + currentInfo.nhsNumber);

							for (NhsNumberInfo info : history) {
								lines.add("" + patientGuid + ": " + info);
							}

							//find out when the NHS number changed
							NhsNumberInfo infoChanged = null;
							for (int i = history.size() - 1; i >= 0; i--) {
								NhsNumberInfo info = history.get(i);
								if (info.nhsNumber.equals(nhsNumber)) {
									infoChanged = history.get(i + 1);
									break;
								}
							}

							if (infoChanged != null) {
								lines.add("" + patientGuid + ": NHS number changed on " + infoChanged.date + " (at " + infoChanged.odsCode + ") to " + currentInfo.nhsNumber);

								finding = "NHS number changed";
								comment = "NHS number changed on " + infoChanged.date;
								//comment = "NHS number changed on " + infoChanged.date + " to " + currentInfo.nhsNumber;

							} else {
								lines.add("" + patientGuid + ": ERROR - FAILED TO FIND MATCHING NHS NUMBER IN HISTORY");

								finding = "ERROR";
								comment = "FAILED TO FIND MATCHING NHS NUMBER IN HISTORY";
							}
						}
					}
				}

				//if we've not found anything above, check patient_search for the NHS number to see if we can work out where they are
				if (finding == null) {
					lines.add("Checking patient_search");

					//check patient search
					EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
					SessionImpl edsSession = (SessionImpl)edsEntityManager.getDelegate();
					Connection edsConnection = edsSession.connection();

					String sql = "select local_id, ccg_code"
							+ " from eds.patient_search ps"
							+ " inner join admin.service s"
							+ " on s.id = ps.service_id"
							+ " and s.organisation_type = 'PR'"
							+ " inner join eds.patient_search_episode pse"
							+ " on pse.service_id = ps.service_id"
							+ " and pse.patient_id = ps.patient_id"
							+ " and pse.registration_end is null"
							+ " where nhs_number = ?"
							+ " order by pse.registration_start desc"
							+ " limit 1";

					PreparedStatement ps = edsConnection.prepareStatement(sql);
					ps.setString(1, nhsNumber);

					//LOG.debug(sql);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {

						String odsCode = rs.getString(1);
						String ccgCode = rs.getString(2);

						OdsOrganisation odsOrg = OdsWebService.lookupOrganisationViaRest(odsCode);
						OdsOrganisation parentOdsOrg = null;
						if (!Strings.isNullOrEmpty(ccgCode)) {
							parentOdsOrg = OdsWebService.lookupOrganisationViaRest(ccgCode);
						}

						if (odsOrg == null) {
							lines.add("Registered at " + odsCode + " but failed to find ODS record for " + odsCode);

							finding = "ERROR";
							comment = "Registered at " + odsCode + " but not found in open ODS";

						} else if (parentOdsOrg == null) {
							finding = "ERROR";
							comment = "Registered at " + odsOrg.getOdsCode() + " " + odsOrg.getOrganisationName() + " but no ODS record found for parent " + ccgCode;

						} else {
							finding = "Out of area";
							comment = "Patient registered in " + parentOdsOrg.getOdsCode() + " " + parentOdsOrg.getOrganisationName();
						}

					} else {

						finding = "Unknown";
						comment = "No data for NHS number found (within scope of DDS)";
					}

					ps.close();
					edsEntityManager.close();

				}

				outputPrinter.printRecord(nhsNumber, calcPseudoId, finding, comment);
			}

			File outputFile = new File("OUTPUT_" + nhsNumberFile);
			Files.write(outputFile.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

			outputPrinter.close();

			LOG.info("Finished Investigating Missing Patients from " + nhsNumberFile + " in Protocol " + protocolName);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void fixMedicationStatementIsActive(String odsCodeRegex) {
		LOG.info("Fixing MedicationStatement IsActive for using " + odsCodeRegex);
		try {


			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

			List<Service> services = serviceDal.getAll();
			for (Service service: services) {

				UUID serviceId = service.getId();

				String odsCode = service.getLocalId();
				if (odsCodeRegex != null
						&& !Pattern.matches(odsCodeRegex, odsCode)) {
					LOG.debug("Skipping " + service + " due to regex");
					continue;
				}

				String serviceIdStr = serviceId.toString();

				//find protocols
				List<LibraryItem> publisherLibraryItems = new ArrayList<>();
				List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceIdStr, null);
				for (LibraryItem libraryItem: libraryItems) {
					for (ServiceContract serviceContract: libraryItem.getProtocol().getServiceContract()) {
						if (serviceContract.getService().getUuid().equals(serviceIdStr)
								&& serviceContract.getType() == PUBLISHER
								&& serviceContract.getActive() == ServiceContractActive.TRUE) {
							publisherLibraryItems.add(libraryItem);
							break;
						}
					}
				}

				if (publisherLibraryItems.isEmpty()) {
					LOG.debug("Skipping " + service + " as not a publisher to any protocol");
					continue;
				}

				LOG.debug("Doing " + service);

				//find subscriber config name and software name for each protocol
				Map<LibraryItem, String> hmSubscriberConfigNames = new HashMap<>();
				Map<LibraryItem, String> hmSoftwareNames = new HashMap<>();

				for (LibraryItem libraryItem: publisherLibraryItems) {

					List<String> subscriberConfigNames = new ArrayList<>();
					List<String> softwareNames = new ArrayList<>();
//LOG.debug("Protocol name = " + libraryItem.getName());
					for (ServiceContract serviceContract : libraryItem.getProtocol().getServiceContract()) {
						if (serviceContract.getType() == ServiceContractType.SUBSCRIBER
								&& serviceContract.getActive() == ServiceContractActive.TRUE) {

							String subscriberConfigName = MessageTransformOutbound.getSubscriberEndpoint(serviceContract);
//LOG.debug("    subscriber config = [" + subscriberConfigName + "]");
							if (!Strings.isNullOrEmpty(subscriberConfigName)) {

								String technicalInterfaceUuidStr = serviceContract.getTechnicalInterface().getUuid();
								String systemUuidStr = serviceContract.getSystem().getUuid();
								TechnicalInterface technicalInterface = LibraryRepositoryHelper.getTechnicalInterfaceDetailsUsingCache(systemUuidStr, technicalInterfaceUuidStr);
								String software = technicalInterface.getMessageFormat();

								//ignore any service contracts not for these formats
								if (!software.equals(MessageFormat.ENTERPRISE_CSV)) {
								/*if (!software.equals(MessageFormat.ENTERPRISE_CSV)
										&& !software.equals(MessageFormat.SUBSCRIBER_CSV)) {*/
									continue;
								}

								if (!subscriberConfigNames.contains(subscriberConfigName)) {
									subscriberConfigNames.add(subscriberConfigName);
								}

								if (!softwareNames.contains(software)) {
									softwareNames.add(software);
								}
							}
						}
					}

					//the DPA protocols won't have any subscribers
					if (subscriberConfigNames.size() == 0) {
						LOG.debug("Failed to find subscriber config name for protocol " + libraryItem.getName());
						continue;
					}

					if (subscriberConfigNames.size() > 1) {
						throw new Exception("Found more than one subscriber config name for protocol " + libraryItem.getName() + ": " + subscriberConfigNames);
					}

					String subscriberConfigName = subscriberConfigNames.get(0);
					String softwareName = softwareNames.get(0);
					hmSubscriberConfigNames.put(libraryItem, subscriberConfigName);
					hmSoftwareNames.put(libraryItem, softwareName);

					LOG.info("Protocol " + libraryItem.getName() + " -> " + softwareName + " @ " + subscriberConfigName);
				}

				List<UUID> patientUuids = patientSearchDal.getPatientIds(serviceId);
				LOG.info("Found " + patientUuids.size() + " patient UUIDs at service");

				Map<String, List<Long>> hmMedicationStatementIdsForService = new HashMap<>();
				int found = 0;

				for (int i = 0; i < patientUuids.size(); i++) {

					if (i % 1000 == 0) {
						LOG.info("Done " + i + " and found " + found);
					}

					UUID patientUuid = patientUuids.get(i);
					List<ResourceWrapper> resourceWrappers = resourceDal.getResourcesByPatient(serviceId, patientUuid, ResourceType.MedicationStatement.toString());

					for (ResourceWrapper resourceWrapper: resourceWrappers) {
						MedicationStatement medicationStatement = (MedicationStatement)resourceWrapper.getResource();

						boolean isActive = medicationStatement.hasStatus()
								&& medicationStatement.getStatus() == MedicationStatement.MedicationStatementStatus.ACTIVE;
						if (!isActive) {
							continue;
						}

						found ++;

						for (LibraryItem libraryItem: publisherLibraryItems) {
							String subscriberConfigName = hmSubscriberConfigNames.get(libraryItem);
							if (Strings.isNullOrEmpty(subscriberConfigName)) {
								continue;
							}

							String softwareName = hmSoftwareNames.get(libraryItem);
							SubscriberResourceMappingDalI subscriberDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

							Long id = null;

							if (softwareName.equals(MessageFormat.ENTERPRISE_CSV)) {
								Long enterpriseId = subscriberDal.findEnterpriseIdOldWay(ResourceType.MedicationStatement.toString(), resourceWrapper.getResourceId().toString());
								if (enterpriseId != null) {
									id = enterpriseId;
								}

							} else if (softwareName.equals(MessageFormat.SUBSCRIBER_CSV)) {
								String ref = resourceWrapper.getReferenceString();
								SubscriberId subscriberId = subscriberDal.findSubscriberId(SubscriberTableId.MEDICATION_STATEMENT.getId(), ref);
								if (subscriberId != null) {
									id = subscriberId.getSubscriberId();
								}

							} else {
								//throw new Exception("Unexpected software name " + softwareName);
							}

							if (id != null) {
								List<Long> l = hmMedicationStatementIdsForService.get(subscriberConfigName);
								if (l == null) {
									l = new ArrayList<>();
									hmMedicationStatementIdsForService.put(subscriberConfigName, l);
								}
								l.add(id);
							}
						}
					}
				}

				LOG.info("Finished, Done " + patientUuids.size() + " and found " + found);

				for (String subscriberConfigName: hmMedicationStatementIdsForService.keySet()) {
					List<Long> medicationStatementIdsForService = hmMedicationStatementIdsForService.get(subscriberConfigName);

					List<String> lines = new ArrayList<>();
					lines.add("#" + odsCode);

					List<String> batch = new ArrayList<>();
					while (!medicationStatementIdsForService.isEmpty()) {
						Long l = medicationStatementIdsForService.remove(0);
						batch.add(l.toString());

						if (medicationStatementIdsForService.isEmpty()
								|| batch.size() > 50) {

							String sql = "UPDATE medication_statement SET is_active = 1 WHERE cancellation_date IS NULL AND id IN (" + String.join(",", batch) + ");";
							lines.add(sql);
							batch.clear();
						}
					}

					LOG.debug("Going to write to file");
					File f = new File(subscriberConfigName + ".sql");
					Files.write(f.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
					LOG.debug("Done write to file");
				}
			}

			LOG.info("Fixing MedicationStatement IsActive for using " + odsCodeRegex);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	/*private static void fixMedicationStatementIsActive(String protocolName, String filePath, String odsCodeRegex) {
		LOG.info("Fixing MedicationStatement IsActive for " + protocolName + " to " + filePath + " matching orgs using " + odsCodeRegex);
		try {
			Set<String> odsCodesDone = new HashSet<>();

			File f = new File(filePath);
			if (f.exists()) {
				List<String> lines = FileUtils.readLines(f);

				for (String line: lines) {
					if (line.startsWith("#")) {
						String odsCode = line.substring(1);
						odsCodesDone.add(odsCode);
					}
				}
			}

			//find services in protocol
			//find subscriber config details


			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

			List<Service> services = serviceDal.getAll();

			for (Service service: services) {

				UUID serviceId = service.getId();
				String serviceIdStr = serviceId.toString();

				//find protocol
				LibraryItem matchedProtocol = null;
				List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceId.toString(), null);
				for (LibraryItem protocol: libraryItems) {
					if (protocol.getName().equalsIgnoreCase(protocolName)) {
						matchedProtocol = protocol;
						break;
					}
				}
				if (matchedProtocol == null) {
					LOG.debug("Skipping " + service + " as not in protocol " + protocolName);
					continue;
				}

				//ensure in protocol as a publisher
				boolean isPublisher = false;
				for (ServiceContract serviceContract: matchedProtocol.getProtocol().getServiceContract()) {
					if (serviceContract.getService().getUuid().equals(serviceIdStr)
							&& serviceContract.getType() == ServiceContractType.PUBLISHER
							&& serviceContract.getActive() == ServiceContractActive.TRUE) {
						isPublisher = true;
						break;
					}
				}

				if (!isPublisher) {
					LOG.debug("Skipping " + service + " as not a publisher to protocol " + protocolName);
					continue;
				}

				String odsCode = service.getLocalId();
				if (odsCodeRegex != null
						&& !Pattern.matches(odsCodeRegex, odsCode)) {
					LOG.debug("Skipping " + service + " due to regex");
					continue;
				}

				if (odsCodesDone.contains(odsCode)) {
					LOG.debug("Already done " + service);
					continue;
				}

				LOG.debug("Doing " + service);

				//find subscriber config name
				List<String> subscriberConfigNames = new ArrayList<>();
				List<String> softwareNames = new ArrayList<>();

				for (ServiceContract serviceContract: matchedProtocol.getProtocol().getServiceContract()) {
					if (serviceContract.getType() == ServiceContractType.SUBSCRIBER
							&& serviceContract.getActive() == ServiceContractActive.TRUE) {

						String subscriberConfigName = MessageTransformOutbound.getSubscriberEndpoint(serviceContract);
						if (!Strings.isNullOrEmpty(subscriberConfigName)) {
							subscriberConfigNames.add(subscriberConfigName);

							String technicalInterfaceUuidStr = serviceContract.getTechnicalInterface().getUuid();
							String systemUuidStr = serviceContract.getSystem().getUuid();
							TechnicalInterface technicalInterface = LibraryRepositoryHelper.getTechnicalInterfaceDetailsUsingCache(systemUuidStr, technicalInterfaceUuidStr);
							String software = technicalInterface.getMessageFormat();
							softwareNames.add(software);
						}
					}
				}
				if (subscriberConfigNames.size() == 0) {
					throw new Exception("Failed to find subscriber config name for protocol " + protocolName);
				}
				if (subscriberConfigNames.size() > 1) {
					throw new Exception("Found more than one subscriber config name for protocol " + protocolName);
				}
				String subscriberConfigName = subscriberConfigNames.get(0);
				String softwareName = softwareNames.get(0);
				LOG.info("Protocol " + protocolName + " -> " + softwareName + " @ " + subscriberConfigName);

				List<UUID> patientUuids = patientSearchDal.getPatientIds(serviceId);
				LOG.info("Found " + patientUuids.size() + " patient UUIDs at service");

				SubscriberResourceMappingDalI subscriberDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

				List<Long> medicationStatementIdsForService = new ArrayList<>();

				for (int i = 0; i < patientUuids.size(); i++) {

					if (i % 1000 == 0) {
						LOG.info("Done " + i + " and found " + medicationStatementIdsForService.size());
					}

					UUID patientUuid = patientUuids.get(i);
					List<ResourceWrapper> resourceWrappers = resourceDal.getResourcesByPatient(serviceId, patientUuid, ResourceType.MedicationStatement.toString());

					for (ResourceWrapper resourceWrapper: resourceWrappers) {
						MedicationStatement medicationStatement = (MedicationStatement)resourceWrapper.getResource();

						boolean isActive = medicationStatement.hasStatus()
								&& medicationStatement.getStatus() == MedicationStatement.MedicationStatementStatus.ACTIVE;
						if (isActive) {

							if (softwareName.equals(MessageFormat.ENTERPRISE_CSV)) {
								Long enterpriseId = subscriberDal.findEnterpriseIdOldWay(ResourceType.MedicationStatement.toString(), resourceWrapper.getResourceId().toString());
								if (enterpriseId != null) {
									medicationStatementIdsForService.add(enterpriseId);
								}

							} else if (softwareName.equals(MessageFormat.SUBSCRIBER_CSV)) {
								String ref = resourceWrapper.getReferenceString();
								SubscriberId subscriberId = subscriberDal.findSubscriberId(SubscriberTableId.MEDICATION_STATEMENT.getId(), ref);
								if (subscriberId != null) {
									medicationStatementIdsForService.add(subscriberId.getSubscriberId());
								}

							} else {
								throw new Exception("Unexpected software name " + softwareName);
							}
						}
					}
				}

				LOG.info("Done " + patientUuids.size() + " and found " + medicationStatementIdsForService.size());

				odsCodesDone.add(odsCode);

				List<String> lines = new ArrayList<>();
				lines.add("#" + odsCode);

				List<String> batch = new ArrayList<>();
				while (!medicationStatementIdsForService.isEmpty()) {
					Long l = medicationStatementIdsForService.remove(0);
					batch.add(l.toString());

					if (medicationStatementIdsForService.isEmpty()
							|| batch.size() > 50) {

						String sql = "UPDATE medication_statement SET is_active = 1 WHERE cancellation_date IS NULL AND id IN (" + String.join(",", batch) + ");";
						lines.add(sql);
						batch.clear();
					}

					if (lines.size() % 10 == 0) {
						LOG.debug("Created " + lines.size() + " lines with " + medicationStatementIdsForService.size() + " IDs remaining");
					}
				}

				LOG.debug("Going to write to file");
				Files.write(f.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
				LOG.debug("Done write to file");
			}

			LOG.debug("Written to " + f);

			LOG.info("Finished Fixing Missing Emis Ethnicities to " + filePath);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/**
	 * restores the ethnicity and marital statuses on the Patient resources that were lost
	 * if the "re-registrated patients" fix was run before the "deleted patients" fix. This meant that
	 * the patient resource was re-created from the patient file but the ethnicity and marital status weren't carried
	 * over from the pre-deleted version.
	 */
	/*private static void fixMissingEmisEthnicities(String filePath, String filterRegexOdsCode) {
		LOG.info("Fixing Missing Emis Ethnicities to " + filePath + " matching orgs using " + filterRegexOdsCode);
		try {

			Map<String, List<UUID>> hmPatientIds = new HashMap<>();

			File f = new File(filePath);
			if (f.exists()) {
				List<String> lines = FileUtils.readLines(f);

				String currentOdsCode = null;

				for (String line: lines) {
					if (line.startsWith("#")) {
						currentOdsCode = line.substring(1);
					} else {
						UUID patientId = UUID.fromString(line);
						List<UUID> s = hmPatientIds.get(currentOdsCode);
						if (s == null) {
							s = new ArrayList<>();
							hmPatientIds.put(currentOdsCode, s);
						}
						s.add(patientId);
					}

				}
			}

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			List<Service> services = serviceDal.getAll();

			for (Service service: services) {

				String odsCode = service.getLocalId();
				if (filterRegexOdsCode != null
						&& !Pattern.matches(filterRegexOdsCode, odsCode)) {
					LOG.debug("Skipping " + service + " due to regex");
					continue;
				}

				if (hmPatientIds.containsKey(odsCode)) {
					LOG.debug("Already done " + service);
					continue;
				}

				LOG.debug("Doing " + service);
				UUID serviceId = service.getId();

				PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
				List<UUID> patientUuids = patientSearchDal.getPatientIds(serviceId);
				LOG.info("Found " + patientUuids.size() + " patient UUIDs at service");

				ResourceDalI resourceDal = DalProvider.factoryResourceDal();
				List<UUID> patientIdsForService = new ArrayList<>();
				List<ResourceWrapper> resourceWrappersToSave = new ArrayList<>();

				for (int i = 0; i < patientUuids.size(); i++) {

					if (i % 1000 == 0) {
						LOG.info("Done " + i + " and found " + patientIdsForService.size());
					}

					UUID patientUuid = patientUuids.get(i);
					List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Patient.toString(), patientUuid);

					ResourceWrapper current = history.get(0);
					if (current.isDeleted()) {
						continue;
					}

					//if only one history record, no point looking back
					if (history.size() == 1) {
						continue;
					}

					Patient p = (Patient)current.getResource();
					PatientBuilder patientBuilder = new PatientBuilder(p);

					//see if both already present
					EthnicCategory currentEthnicCategory = patientBuilder.getEthnicity();
					MaritalStatus currentMaritalStatus = patientBuilder.getMaritalStatus();
					if (currentEthnicCategory != null
							&& currentMaritalStatus != null) {
						continue;
					}

					EthnicCategory newEthnicCategory = null;
					MaritalStatus newMaritalStatus = null;

					for (int j=1; j<history.size(); j++) {
						ResourceWrapper previousWrapper = history.get(j);
						if (previousWrapper.isDeleted()) {
							continue;
						}

						Patient previous = (Patient)previousWrapper.getResource();
						PatientBuilder previousPatientBuilder = new PatientBuilder(previous);

						if (newEthnicCategory == null) {
							newEthnicCategory = previousPatientBuilder.getEthnicity();
						}
						if (newMaritalStatus == null) {
							newMaritalStatus = previousPatientBuilder.getMaritalStatus();
						}
					}

					if (newEthnicCategory == currentEthnicCategory
							&& newMaritalStatus == currentMaritalStatus) {
						continue;
					}

					boolean fixed = false;

					if (newEthnicCategory != null) {
						patientBuilder.setEthnicity(newEthnicCategory);
						fixed = true;
					}
					if (newMaritalStatus != null) {
						patientBuilder.setMaritalStatus(newMaritalStatus);
						fixed = true;
					}

					if (fixed) {
						p = (Patient) patientBuilder.getResource();
						String newJson = FhirSerializationHelper.serializeResource(p);
						current.setResourceData(newJson);

						patientIdsForService.add(patientUuid);
						resourceWrappersToSave.add(current);
					}


				}

				LOG.info("Done " + patientUuids.size() + " and found " + patientIdsForService.size());

				hmPatientIds.put(odsCode, patientIdsForService);

				List<String> lines = new ArrayList<>();
				for (String odsCodeDone: hmPatientIds.keySet()) {
					lines.add("#" + odsCodeDone);
					List<UUID> patientIdsDone = hmPatientIds.get(odsCodeDone);
					for (UUID patientIdDone: patientIdsDone) {
						lines.add(patientIdDone.toString());
					}
				}

				FileUtils.writeLines(f, lines);

				//only now we've stored the affected patient IDs in the file should we actually update the DB
				for (ResourceWrapper wrapper: resourceWrappersToSave) {
					saveResourceWrapper(serviceId, wrapper);
				}

				//and re-queue the affected patients for sending to subscribers
				QueueHelper.queueUpPatientsForTransform(patientIdsForService);
			}

			LOG.debug("Written to " + f);

			LOG.info("Finished Fixing Missing Emis Ethnicities to " + filePath);
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}*/

	/**
	 * updates patient_search and patient_link tables for explicit list of patient UUIDs
	 */
	/*private static void updatePatientSearch(String filePath) throws Exception {
		LOG.info("Updating patient search from " + filePath);
		try {
			File f = new File(filePath);
			if (!f.exists()) {
				LOG.error("File " + f + " doesn't exist");
				return;
			}

			List<UUID> patientIds = new ArrayList<>();

			List<String> lines = FileUtils.readLines(f);
			for (String line: lines) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}

				UUID uuid = UUID.fromString(line);
				patientIds.add(uuid);
			}
			LOG.info("Found " + patientIds.size() + " patient UUIDs");

			Map<String, UUID> hmPublishers = new HashMap<>();

			List<String> publishers = new ArrayList<>();
			publishers.add("publisher_01");
			publishers.add("publisher_02");
			publishers.add("publisher_03");
			publishers.add("publisher_04");
			publishers.add("publisher_04b");
			publishers.add("publisher_05");
			publishers.add("publisher_05_nwl_tmp");
			publishers.add("publisher_05_sel_tmp");

			File changedFile = new File(filePath + "changed");

			for (String publisher: publishers) {
				UUID serviceId = findSuitableServiceIdForPublisherConfig(publisher);
				hmPublishers.put(publisher, serviceId);
			}

			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
			PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();

			int done = 0;
			int skipped = 0;

			for (UUID patientId: patientIds) {
				LOG.info("Doing patient " + patientId);

				//we need to find a service ID for the patient, so we know where to get the resources from
				UUID serviceId = null;

				//try the patient_search table first
				PatientSearch ps = patientSearchDal.searchByPatientId(patientId);
				if (ps != null) {
					serviceId = ps.getServiceId();
				}

				//if service ID is still null, then try looking in the patient_link table
				if (serviceId == null) {
					String personId = patientLinkDal.getPersonId(patientId.toString());
					Map<String, String> map = patientLinkDal.getPatientAndServiceIdsForPerson(personId);
					if (map.containsKey(patientId.toString())) {
						serviceId = UUID.fromString(map.get(patientId.toString()));
					}
				}

				List<ResourceWrapper> history = null;

				if (serviceId != null) {
					//if we have a service ID, then retrieve the resource history directly from that DB
					history = resourceDal.getResourceHistory(serviceId, ResourceType.Patient.toString(), patientId);

				} else {
					//if we still don't have a service ID, then test each Corexx DB in turn
					for (String publisher: hmPublishers.keySet()) {
						UUID exampleServiceId = hmPublishers.get(publisher);

						List<ResourceWrapper> publisherHistory = resourceDal.getResourceHistory(exampleServiceId, ResourceType.Patient.toString(), patientId);
						if (!publisherHistory.isEmpty()) {
							history = publisherHistory;
							LOG.info("Found resource history for patient " + patientId + " on " + publisher);
							break;
						}
					}
				}


				if (history == null
						|| history.isEmpty()) {
					LOG.error("Failed to find any resource history for patient " + patientId);
					skipped ++;
					continue;
				}

				ResourceWrapper mostRecent = history.get(0);
				serviceId = mostRecent.getServiceId();

				PatientLinkPair patientLink = null;

				if (mostRecent.isDeleted()) {
					//find most recent non-deleted
					ResourceWrapper nonDeleted = null;
					for (ResourceWrapper wrapper: history) {
						if (!wrapper.isDeleted()) {
							nonDeleted = wrapper;
							break;
						}
					}

					if (nonDeleted == null) {
						LOG.error("No non-deleted Patient resource for " + patientId);
						skipped ++;
						continue;
					}

					Patient p = (Patient)nonDeleted.getResource();
					patientSearchDal.update(serviceId, p);
					patientLink = patientLinkDal.updatePersonId(serviceId, p);

					//and call this to mark the patient_search record as deleted
					patientSearchDal.deletePatient(serviceId, p);

				} else {
					//LOG.debug("Patient wasn't deleted");
					Patient p = (Patient)mostRecent.getResource();
					patientSearchDal.update(serviceId, p);
					patientLink = patientLinkDal.updatePersonId(serviceId, p);
				}

				//if the person ID was changed, write this to a file
				if (patientLink.getNewPersonId() != null) {
					List<String> updateLines = new ArrayList<>();
					updateLines.add(patientId.toString());
					Files.write(changedFile.toPath(), updateLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
				}

				done ++;
				if (done % 500 == 0) {
					LOG.debug("Done " + done + " Skipped " + skipped);
				}
			}

			LOG.debug("Done " + done + " Skipped " + skipped);
			LOG.info("Finished Updating patient search from " + filePath);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/


	/*private static void runPersonUpdater(String enterpriseConfigName) throws Exception {

		try {
			LOG.info("Person updater starting for " + enterpriseConfigName);

			//create this date BEFORE we get the date we last run, so there's no risk of a gap
			Date dateNextRun = new Date();

			EnterprisePersonUpdaterHistoryDalI enterprisePersonUpdaterHistoryDal = DalProvider.factoryEnterprisePersonUpdateHistoryDal(enterpriseConfigName);
			Date dateLastRun = enterprisePersonUpdaterHistoryDal.findDatePersonUpdaterLastRun();
			LOG.info("Looking for Person ID changes since " + dateLastRun);

			PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
			List<PatientLinkPair> changes = patientLinkDal.getChangesSince(dateLastRun);

			LOG.info("Found " + changes.size() + " changes in Person ID");

			//find the Enterprise Person ID for each of the changes, hashing them by the enterprise instance they're on
			List<UpdateJob> updates = convertChangesToEnterprise(enterpriseConfigName, changes);

			List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openConnection(enterpriseConfigName);
			for (EnterpriseConnector.ConnectionWrapper connectionWrapper: connectionWrappers) {

				LOG.info("Updating " + updates.size() + " person IDs on " + connectionWrapper.getUrl());
				Connection connection = connectionWrapper.getConnection();

				try {
					List<String> tables = findTablesWithPersonId(connection);

					for (UpdateJob update: updates) {
						changePersonId(update, connection, tables);
					}

					//and delete any person records that no longer have any references to them
					LOG.info("Going to delete orphaned persons");
					deleteOrphanedPersons(connection);

				} finally {
					connection.close();
				}


			}


			enterprisePersonUpdaterHistoryDal.updatePersonUpdaterLastRun(dateNextRun);

			LOG.info("Person updates complete");

		} catch (Throwable t) {
			LOG.error("", t);
		}

	}*/

	/*private static void deleteOrphanedPersons(Connection connection) throws Exception {

		String sql = "SELECT id FROM person"
				+ " WHERE NOT EXISTS ("
				+ " SELECT 1"
				+ " FROM patient"
				+ " WHERE patient.person_id = person.id)";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		List<Long> ids = new ArrayList<>();
		while (rs.next()) {
			long id = rs.getLong(1);
			ids.add(new Long(id));
		}
		LOG.info("Found " + ids.size() + " orphaned persons to delete");

		rs.close();
		ps.close();

		sql = "DELETE FROM person WHERE id = ?";


		ps = connection.prepareStatement(sql);

		for (int i=0; i<ids.size(); i++) {

			Long id = ids.get(i);
			ps.setLong(1, id);
			ps.addBatch();

			//execute the batch every 50 and at the end
			if (i % 50 == 0
					|| i+1 == ids.size()) {
				ps.executeBatch();
			}
		}

		connection.commit();
	}*/

	/*private static List<UpdateJob> convertChangesToEnterprise(String enterpriseConfigName, List<PatientLinkPair> changes) throws Exception {
		List<UpdateJob> updatesForConfig = new ArrayList<>();

		for (PatientLinkPair change: changes) {

			String oldDiscoveryPersonId = change.getPreviousPersonId();
			String newDiscoveryPersonId = change.getNewPersonId();
			String discoveryPatientId = change.getPatientId();

			SubscriberResourceMappingDalI enterpriseIdDalI = DalProvider.factorySubscriberResourceMappingDal(enterpriseConfigName);
			Long enterprisePatientId = enterpriseIdDalI.findEnterpriseIdOldWay(ResourceType.Patient.toString(), discoveryPatientId);

			//if this patient has never gone to enterprise, then skip it
			if (enterprisePatientId == null) {
				continue;
			}

			SubscriberPersonMappingDalI personMappingDal = DalProvider.factorySubscriberPersonMappingDal(enterpriseConfigName);
			List<Long> mappings = personMappingDal.findEnterprisePersonIdsForPersonId(oldDiscoveryPersonId);
			for (Long oldEnterprisePersonId: mappings) {
				Long newEnterprisePersonId = personMappingDal.findOrCreateEnterprisePersonId(newDiscoveryPersonId);

				updatesForConfig.add(new UpdateJob(enterprisePatientId, oldEnterprisePersonId, newEnterprisePersonId));
			}
		}

		return updatesForConfig;
	}*/

	private static void changePersonId(UpdateJob change, Connection connection, List<String> tables) throws Exception {

		for (String tableName: tables) {
			changePersonIdOnTable(tableName, change, connection);
		}

		connection.commit();

		LOG.info("Updated person ID from " + change.getOldEnterprisePersonId() + " to " + change.getNewEnterprisePersonId() + " for patient " + change.getEnterprisePatientId());
	}
    /*private static void changePersonId(UpdateJob change, Connection connection) throws Exception {
        OutputContainer outputContainer = new OutputContainer(true); //doesn't matter what we pass into the constructor
        //the csv writers are mapped to the tables in the database, so we can use them to discover
        //what tables have person and patient ID columns
        List<AbstractEnterpriseCsvWriter> csvWriters = outputContainer.getCsvWriters();
        //the writers are in dependency order (least dependent -> most) so we need to go backwards to avoid
        //upsetting any foreign key constraints
        for (int i=csvWriters.size()-1; i>=0; i--) {
            AbstractEnterpriseCsvWriter csvWriter = csvWriters.get(i);
            String[] csvHeaders = csvWriter.getCsvHeaders();
            for (String header: csvHeaders) {
                if (header.equalsIgnoreCase("person_id")) {
                    String fileName = csvWriter.getFileName();
                    String tableName = FilenameUtils.removeExtension(fileName);
                    changePersonIdOnTable(tableName, change, connection);
                    break;
                }
            }
        }
        connection.commit();
        LOG.info("Updated person ID from " + change.getOldEnterprisePersonId() + " to " + change.getNewEnterprisePersonId() + " for patient " + change.getEnterprisePatientId());
    }*/

	/*private static List<String> findTablesWithPersonId(Connection connection) throws Exception {

		Statement statement = connection.createStatement();

		String dbNameSql = "SELECT DATABASE()";
		ResultSet rs = statement.executeQuery(dbNameSql);
		rs.next();
		String dbName = rs.getString(1);
		rs.close();

		String tableNameSql = "SELECT t.table_name"
				+ " FROM information_schema.tables t"
				+ " INNER JOIN information_schema.columns c"
				+ " ON c.table_name = t.table_name"
				+ " AND c.table_schema = t.table_schema"
				+ " WHERE t.table_schema = '" + dbName + "'"
				+ " AND c.column_name = 'person_id'";
		rs = statement.executeQuery(tableNameSql);

		List<String> ret = new ArrayList<>();

		while (rs.next()) {
			String tableName = rs.getString(1);
			ret.add(tableName);
		}

		rs.close();
		statement.close();

		return ret;
	}*/


	private static void changePersonIdOnTable(String tableName, UpdateJob change, Connection connection) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(tableName);
		sb.append(" SET ");
		sb.append("person_id = ? ");
		sb.append("WHERE ");

		if (tableName.equals("patient")) {
			sb.append("id = ? ");
		} else {
			sb.append("patient_id = ? ");
		}

		sb.append("AND person_id = ?");

		PreparedStatement update = connection.prepareStatement(sb.toString());

		update.setLong(1, change.getNewEnterprisePersonId());
		update.setLong(2, change.getEnterprisePatientId());
		update.setLong(3, change.getOldEnterprisePersonId());

		update.addBatch();
		update.executeBatch();
	}

	static class UpdateJob {
		private Long enterprisePatientId = null;
		private Long oldEnterprisePersonId = null;
		private Long newEnterprisePersonId = null;

		public UpdateJob(Long enterprisePatientId, Long oldEnterprisePersonId, Long newEnterprisePersonId) {
			this.enterprisePatientId = enterprisePatientId;
			this.oldEnterprisePersonId = oldEnterprisePersonId;
			this.newEnterprisePersonId = newEnterprisePersonId;

		}

		public Long getEnterprisePatientId() {
			return enterprisePatientId;
		}

		public Long getOldEnterprisePersonId() {
			return oldEnterprisePersonId;
		}

		public Long getNewEnterprisePersonId() {
			return newEnterprisePersonId;
		}
	}

	/*private static void findPatientsThatNeedTransforming(String file, String filterOdsCode) {
		LOG.info("Finding patients that need transforming for " + filterOdsCode + " for " + file);
		try {

			Map<String, List<UUID>> hmPatientIds = new HashMap<>();

			File f = new File(file);
			if (f.exists()) {
				List<String> lines = FileUtils.readLines(f);

				String currentOdsCode = null;

				for (String line: lines) {
					if (line.startsWith("#")) {
						currentOdsCode = line.substring(1);
					} else {
						UUID patientId = UUID.fromString(line);
						List<UUID> s = hmPatientIds.get(currentOdsCode);
						if (s == null) {
							s = new ArrayList<>();
							hmPatientIds.put(currentOdsCode, s);
						}
						s.add(patientId);
					}

				}
			}

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			List<Service> services = serviceDal.getAll();

			for (Service service: services) {

				String odsCode = service.getLocalId();
				if (filterOdsCode != null
						&& filterOdsCode.equals(odsCode)) {
					continue;
				}

				if (hmPatientIds.containsKey(odsCode)) {
					LOG.debug("Already done " + service);
					continue;
				}

				LOG.debug("Doing " + service);
				UUID serviceId = service.getId();

				PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
				List<UUID> patientUuids = patientSearchDal.getPatientIds(serviceId);
				LOG.info("Found " + patientUuids.size() + " patient UUIDs");

				ResourceDalI resourceDal = DalProvider.factoryResourceDal();
				List<UUID> patientIdsForService = new ArrayList<>();


				for (int i = 0; i < patientUuids.size(); i++) {

					UUID patientUuid = patientUuids.get(i);

					boolean shouldBeInSubscriber = false;

					List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Patient.toString(), patientUuid);

					boolean addPatient = false;

					for (int j = history.size() - 1; j >= 0; j--) {
						ResourceWrapper wrapper = history.get(j);
						if (wrapper.isDeleted()) {
							continue;
						}

						Patient patient = (Patient) wrapper.getResource();

						//any confidential patient should be in the DB because they were previously filtered out
						BooleanType bt = (BooleanType) ExtensionConverter.findExtensionValue(patient, FhirExtensionUri.IS_CONFIDENTIAL);
						if (bt != null
								&& bt.hasValue()
								&& bt.getValue().booleanValue()) {
							addPatient = true;
							break;
						}

						//and patient w/o NHS number should be in the DB because they were previously filtered out
						//any patient with 999999 NHS number should be added so they get stripped out
						String nhsNumber = IdentifierHelper.findNhsNumber(patient);
						if (Strings.isNullOrEmpty(nhsNumber)
								|| nhsNumber.startsWith("999999")) {
							addPatient = true;
							break;
						}

						if (j == history.size() - 1) {
							//find first NHS number known
							shouldBeInSubscriber = SubscriberTransformHelper.shouldPatientBePresentInSubscriber(patient);

						} else {
							boolean thisShouldBeInSubscriber = SubscriberTransformHelper.shouldPatientBePresentInSubscriber(patient);

							if (shouldBeInSubscriber != thisShouldBeInSubscriber) {

								addPatient = true;
								break;
							}
						}
					}

					if (addPatient) {
						patientIdsForService.add(patientUuid);
					}

					if (i % 1000 == 0) {
						LOG.info("Done " + i + " and found " + patientIdsForService.size());
					}
				}

				hmPatientIds.put(odsCode, patientIdsForService);
				LOG.debug("Found " + patientIdsForService.size() + " affected");

				QueueHelper.queueUpPatientsForTransform(patientIdsForService);

				List<String> lines = new ArrayList<>();
				for (String odsCodeDone: hmPatientIds.keySet()) {
					lines.add("#" + odsCodeDone);
					List<UUID> patientIdsDone = hmPatientIds.get(odsCodeDone);
					for (UUID patientIdDone: patientIdsDone) {
						lines.add(patientIdDone.toString());
					}
				}

				FileUtils.writeLines(f, lines);
			}

			LOG.debug("Written to " + f);

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void transformPatients(String sourceFile, String reason) {
		LOG.info("Transforming patients from " + sourceFile);
		try {
			List<UUID> patientIds = new ArrayList<>();

			File f = new File(sourceFile);
			if (!f.exists()) {
				LOG.error("File " + f + " doesn't exist");
				return;
			}
			List<String> lines = FileUtils.readLines(f);
			for (String line: lines) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}

				UUID uuid = UUID.fromString(line);
				patientIds.add(uuid);
			}

			if (patientIds.isEmpty()) {
				LOG.error("No patient IDs found");
				return;
			}
			LOG.info("Found " + patientIds.size() + " patient IDs");

			QueueHelper.queueUpPatientsForTransform(patientIds, reason);

			LOG.info("Finished transforming patients from " + sourceFile);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	/*private static void countNhsNumberChanges(String odsCodes) {
		LOG.info("Counting NHS number changes for " + odsCodes);
		try {

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ResourceDalI resourceDal = DalProvider.factoryResourceDal();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

			Map<String, Date> hmEarliestDate = new HashMap<>();
			Map<String, Integer> hmPatientCount = new HashMap<>();
			Map<String, Map<Date, List<UUID>>> hmCounts = new HashMap<>();

			String[] toks = odsCodes.split(",");
			for (String odsCode: toks) {

				Service service = serviceDal.getByLocalIdentifier(odsCode);
				LOG.info("Doing " + service.getName() + " " + service.getLocalId());
				UUID serviceId = service.getId();

				PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
				List<UUID> patientUuids = patientSearchDal.getPatientIds(serviceId);
				LOG.info("Found " + patientUuids.size() + " patient UUIDs");

				Date earliestDate = null;
				Map<Date, List<UUID>> hmChanges = new HashMap<>();

				for (int i = 0; i < patientUuids.size(); i++) {

					UUID patientUuid = patientUuids.get(i);
					String previousNhsNumber = null;

					List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Patient.toString(), patientUuid);

					for (int j = history.size() - 1; j >= 0; j--) {
						ResourceWrapper wrapper = history.get(j);
						Date d = wrapper.getCreatedAt();

						//work out bulk date
						if (earliestDate == null
								|| d.before(earliestDate)) {
							earliestDate = d;
						}

						if (wrapper.isDeleted()) {
							continue;
						}

						Patient patient = (Patient) wrapper.getResource();

						if (j == history.size() - 1) {
							//find first NHS number known
							previousNhsNumber = IdentifierHelper.findNhsNumber(patient);

						} else {
							String thisNhsNumber = IdentifierHelper.findNhsNumber(patient);
							if ((thisNhsNumber == null && previousNhsNumber != null)
									//|| (thisNhsNumber != null && previousNhsNumber == null) //don't count it going FROM null to non-null as a change
									|| (thisNhsNumber != null && previousNhsNumber != null && !thisNhsNumber.equals(previousNhsNumber))) {

								//changed
								LOG.info("" + patientUuid + " changed NHS number on " + sdf.format(d));

								List<UUID> l = hmChanges.get(d);
								if (l == null) {
									l = new ArrayList<>();
									hmChanges.put(d, l);
								}
								l.add(patientUuid);

								previousNhsNumber = thisNhsNumber;
							}
						}
					}

					if (i % 1000 == 0) {
						LOG.info("Done " + i);
					}
				}

				hmEarliestDate.put(odsCode, earliestDate);
				hmPatientCount.put(odsCode, new Integer(patientUuids.size()));
				hmCounts.put(odsCode, hmChanges);
			}

			List<String> colHeaders = new ArrayList<>();
			colHeaders.add("Year");
			colHeaders.add("Month");
			colHeaders.addAll(Arrays.asList(toks));
			String[] headerArray = colHeaders.toArray(new String[]{});

			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headerArray);
			FileWriter fileWriter = new FileWriter("NHS_number_changes.csv");
			CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFormat);

			//patient count
			List<String> row = new ArrayList<>();
			row.add("Patient Count");
			row.add("");
			for (String odsCode: toks) {
				Integer count = hmPatientCount.get(odsCode);
				if (count == null) {
					row.add("0");
				} else {
					row.add("" + count);
				}
			}
			csvPrinter.printRecord(row.toArray());

			//start date
			row = new ArrayList<>();
			row.add("Bulk Date");
			row.add("");
			for (String odsCode: toks) {
				Date startDate = hmEarliestDate.get(odsCode);
				if (startDate == null) {
					row.add("not found");
				} else {
					row.add("" + sdf.format(startDate));
				}
			}
			csvPrinter.printRecord(row.toArray());

			for (int year=2017; year<=2019; year++) {

				for (int month=Calendar.JANUARY; month<=Calendar.DECEMBER; month++) {

					String monthStr = "" + month;
					if (monthStr.length() < 2) {
						monthStr = "0" + monthStr;
					}
					Date monthStart = sdf.parse("" + year + monthStr + "01");

					Calendar cal = Calendar.getInstance();
					cal.setTime(monthStart);
					cal.add(Calendar.MONTH, 1);
					cal.add(Calendar.DAY_OF_YEAR, -1);
					Date monthEnd = cal.getTime();

					row = new ArrayList<>();
					row.add("" + year);
					row.add("" + (month+1));

					for (String odsCode: toks) {
						Date startDate = hmEarliestDate.get(odsCode);
						if (startDate == null || startDate.after(monthStart)) {
							row.add("");

						} else {

							int changes = 0;

							Map<Date, List<UUID>> hmChanges = hmCounts.get(odsCode);
							if (hmChanges != null) {
								for (Date d : hmChanges.keySet()) {
									if (!d.before(monthStart)
											&& !d.after(monthEnd)) {
										List<UUID> uuids = hmChanges.get(d);
										changes += uuids.size();
									}
								}
							}

							row.add("" + changes);
						}
					}

					csvPrinter.printRecord(row.toArray());
				}
			}

			csvPrinter.close();

			LOG.info("Finished counting NHS number changes for " + odsCodes);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void createDigest(String url, String user, String pass, String table, String columnFrom, String columnTo, String base64Salt, String validNhsNumberCol) {
		LOG.info("Creating Digest value from " + table + "." + columnFrom + " -> " + columnTo);
		try {

			byte[] saltBytes = Base64.getDecoder().decode(base64Salt);

			//open connection
			Class.forName("com.mysql.cj.jdbc.Driver");

			//create connection
			Properties props = new Properties();
			props.setProperty("user", user);
			props.setProperty("password", pass);

			Connection conn = DriverManager.getConnection(url, props);

			String sql = "SELECT DISTINCT " + columnFrom + " FROM " + table;
			PreparedStatement psSelect = conn.prepareStatement(sql);
			psSelect.setFetchSize(1000);

			Connection conn2 = DriverManager.getConnection(url, props);

			if (validNhsNumberCol != null) {
				sql = "UPDATE " + table + " SET " + validNhsNumberCol + " = ?, " + columnTo + " = ? WHERE " + columnFrom + " = ?";
			} else {
				sql = "UPDATE " + table + " SET " + columnTo + " = ? WHERE " + columnFrom + " = ?";
			}

			PreparedStatement psUpdate = conn2.prepareStatement(sql);

			Connection conn3 = DriverManager.getConnection(url, props);

			if (validNhsNumberCol != null) {
				sql = "UPDATE " + table + " SET " + validNhsNumberCol + " = ?, " + columnTo + " = ? WHERE " + columnFrom + " IS NULL";
			} else {
				sql = "UPDATE " + table + " SET " + columnTo + " = ? WHERE " + columnFrom + " IS NULL";
			}

			PreparedStatement psUpdateNull = conn3.prepareStatement(sql);


			LOG.trace("Starting query");
			ResultSet rs = psSelect.executeQuery();
			LOG.trace("Query results back");

			int done = 0;
			int batchSize = 0;

			while (rs.next()) {
				Object o = rs.getObject(1);
				String value = "";
				if (o != null) {
					value = o.toString();
				}

				TreeMap<String, String> keys = new TreeMap<>();
				keys.put("DoesntMatter", value);

				Crypto crypto = new Crypto();
				crypto.SetEncryptedSalt(saltBytes);
				String pseudoId = crypto.GetDigest(keys);

				if (o == null) {
					int col = 1;
					if (validNhsNumberCol != null) {
						Boolean isValid = IdentifierHelper.isValidNhsNumber(value);
						int validNhsNumber;
						if (isValid == null) {
							validNhsNumber = -1;
						} else if (!isValid.booleanValue()) {
							validNhsNumber = 0;
						} else {
							validNhsNumber = 1;
						}
						psUpdateNull.setInt(col++, validNhsNumber);
					}
					psUpdateNull.setString(col++, pseudoId);
					psUpdateNull.executeUpdate();

				} else {

					int col = 1;
					if (validNhsNumberCol != null) {
						Boolean isValid = IdentifierHelper.isValidNhsNumber(value);
						int validNhsNumber;
						if (isValid == null) {
							validNhsNumber = -1;
						} else if (!isValid.booleanValue()) {
							validNhsNumber = 0;
						} else {
							validNhsNumber = 1;
						}
						psUpdate.setInt(col++, validNhsNumber);
					}
					psUpdate.setString(col++, pseudoId);
					psUpdate.setString(col++, value);

					psUpdate.addBatch();
					batchSize++;

					if (batchSize >= 10) {
						psUpdate.executeBatch();
					}
				}

				done ++;
				if (done % 1000 == 0) {
					LOG.debug("Done " + done);
				}
			}

			if (batchSize >= 0) {
				psUpdate.executeBatch();
			}

			rs.close();
			psSelect.close();
			psUpdate.close();
			psUpdateNull.close();
			conn.close();
			conn2.close();
			conn3.close();

			LOG.debug("Done " + done);

			LOG.info("Finished Creating Digest value from " + table + "." + columnFrom + " -> " + columnTo);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}


	/*private static void checkForBartsMissingFiles(String sinceDate) {
		LOG.info("Checking for Barts missing files");
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date start2019 = sdf.parse(sinceDate);
			LOG.info("Checking files since " + sinceDate);

			UUID serviceId = UUID.fromString("b5a08769-cbbe-4093-93d6-b696cd1da483");
			UUID systemId = UUID.fromString("e517fa69-348a-45e9-a113-d9b59ad13095");
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			LOG.info("Found " + exchanges.size() + " exchanges");

			Map<String, List<String>> hmByFileType = new HashMap<>();
			Map<String, Date> hmReceivedDate = new HashMap<>();

			for (Exchange exchange: exchanges) {
				String body = exchange.getBody();

				//skip any exchanges pre-2019
				Date d = exchange.getHeaderAsDate(HeaderKeys.DataDate);
				if (d.before(start2019)) {
					continue;
				}

				List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(body);
				for (ExchangePayloadFile file: files) {
					String type = file.getType();
					String path = file.getPath();
					String name = FilenameUtils.getName(path);

					List<String> l = hmByFileType.get(type);
					if (l == null) {
						l = new ArrayList<>();
						hmByFileType.put(type, l);
					}
					l.add(name);
					hmReceivedDate.put(name, d);
				}
			}
			LOG.info("Parsed exchange bodies");

			List<String> types = new ArrayList<>(hmByFileType.keySet());
			types.sort((o1, o2) -> o1.compareToIgnoreCase(o2));

			for (String type: types) {
				List<String> files = hmByFileType.get(type);
				LOG.info("---------------------------------------------------------------------");
				LOG.info("Checking " + type + " with " + files.size());

				if (type.equals("MaternityServicesDataSet")
						|| type.equals("SusEmergency")
						|| type.equals("SusEmergencyTail")) {
					continue;
				}

				if (type.equals("CriticalCare")) { //cc_BH_192575_susrnj.dat
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "_", 2);

				} else if (type.equals("Diagnosis")) { //rnj_pc_diag_20190330-011515.dat
					checkForMissingFilesByDate(type, hmReceivedDate, files, "yyyyMMdd", "_|-", 3);

				} else if (type.equals("HomeDeliveryAndBirth")) { //hdb_BH_192576_susrnj.dat
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "_", 2);

				} else if (type.equals("MaternityBirth")) { //GETL_MAT_BIRTH_2019-03-30_001020_1431392750.txt
					checkForMissingFilesByDate(type, hmReceivedDate, files, "yyyy-MM-dd", "_", 3);

				} else if (type.equals("Pregnancy")) { //GETL_MAT_PREG_2019-03-30_001020_1431392781.txt
					checkForMissingFilesByDate(type, hmReceivedDate, files, "yyyy-MM-dd", "_", 3);

				} else if (type.equals("Problem")) { //rnj_pc_prob_20190328-011001.dat
					checkForMissingFilesByDate(type, hmReceivedDate, files, "yyyyMMdd", "_|-", 3);

				} else if (type.equals("Procedure")) { //rnj_pc_proc_20180716-010530.dat
					checkForMissingFilesByDate(type, hmReceivedDate, files, "yyyyMMdd", "_|-", 3);

				} else if (type.equals("SurginetCaseInfo")) { //spfit_sn_case_info_rnj_20190812-093823.dat
					checkForMissingFilesByDate(type, hmReceivedDate, files, "yyyyMMdd", "_|-", 5);

				} else if (type.equals("SusEmergencyCareDataSet")) { //susecd.190360  AND  susecd_BH.190039
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "\\.", 1);

				} else if (type.equals("SusEmergencyCareDataSetTail")) { //tailecd_DIS.190362
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "\\.", 1);

				} else if (type.equals("SusInpatient")) { //ip_BH_193174_susrnj.dat
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "_", 2);

				} else if (type.equals("SusInpatientTail")) { //tailip_DIS.203225_susrnj.dat
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "\\.|_", 2);

				} else if (type.equals("SusOutpatient")) { //susopa_BH.204612
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "\\.", 1);

				} else if (type.equals("SusOutpatientTail")) { //tailopa_DIS.204610
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "\\.", 1);

				} else if (type.equals("SusOutpatientTail")) { //tailopa_DIS.204610
					checkForMissingFilesByNumber(type, hmReceivedDate, files, "\\.", 1);

				} else if (type.equals("APPSL2")) { //GETL_APPSL2_80130_RNJ_10072018_065345_1.TXT
					checkForMissingFilesByDate(type, hmReceivedDate, files, "ddMMyyyy", "_", 4);

				} else if (type.equals("BlobContent")) { //Blob_Con_20190502_00198.csv
					checkForMissingFilesByDate(type, hmReceivedDate, files, "yyyyMMdd", "_", 2);

				} else if (type.equals("FamilyHistory")) { //Fam_Hist_20190417_00326.csv
					checkForMissingFilesByDate(type, hmReceivedDate, files, "yyyyMMdd", "_", 2);

				} else {

					String first = files.get(0);
					if (first.contains("_RNJ_")) { //CLEVE_80130_RNJ_15072018_045416_6.TXT
						checkForMissingFilesByDate(type, hmReceivedDate, files, "ddMMyyyy", "_", 3);

					} else { //Blob_Con_20190328_00170.csv
						checkForMissingFilesByDate(type, hmReceivedDate, files, "ddMMyyyy", "_", 2);
					}
				}

			}

			LOG.info("Finished Checking for Barts missing files");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void checkForMissingFilesByDate(String fileType, Map<String, Date> hmReceivedDate, List<String> files, String dateFormat, String delimiter, int token) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd");

		Date minDate = null;
		Date maxDate = null;
		Map<Date, List<String>> hmByDate = new HashMap<>();

		for (String file: files) {
			String[] toks = file.split(delimiter);
			String tok = null;
			Date d = null;
			try {
				tok = toks[token];
				d = sdf.parse(tok);
			} catch (ParseException pe) {
				LOG.error("Error parsing " + tok + " with format " + dateFormat + " toks " + toks, pe);
				return;
			}
			//LOG.debug("File " + file + " -> " + tok + " -> " + sdf.format(d));

			if (minDate == null
					|| d.before(minDate)) {
				minDate = d;
			}
			if (maxDate == null
					|| d.after(maxDate)) {
				maxDate = d;
			}

			List<String> l = hmByDate.get(d);
			if (l == null) {
				l = new ArrayList<>();
				hmByDate.put(d, l);
			}
			l.add(file);
		}

		LOG.info("Checking for date range " + sdfOutput.format(minDate) + " to " + sdfOutput.format(maxDate));

		Calendar cal = Calendar.getInstance();

		Date d = new Date(minDate.getTime());
		while (!d.after(maxDate)) {

			List<String> l = hmByDate.get(d);
			if (l == null) {

				cal.setTime(d);
				cal.add(Calendar.DAY_OF_YEAR, -1);
				Date dateBefore = cal.getTime();
				List<String> before = hmByDate.get(dateBefore);
				String beforeDesc = null;
				if (before != null) {
					String firstBefore = before.get(0);
					Date beforeReceived = hmReceivedDate.get(firstBefore);
					beforeDesc = firstBefore + " on " + sdfOutput.format(beforeReceived);
				}

				cal.setTime(d);
				cal.add(Calendar.DAY_OF_YEAR, 1);
				Date dateAfter = cal.getTime();
				List<String> after = hmByDate.get(dateAfter);
				String afterDesc = null;
				if (after != null) {
					String firstAfter = after.get(0);
					Date afterReceived = hmReceivedDate.get(firstAfter);
					afterDesc = firstAfter + " on " + sdfOutput.format(afterReceived);
				}

				LOG.error("No " + fileType + " file found for " + sdfOutput.format(d) + " previous [" + beforeDesc + "] after [" + afterDesc + "]");
			}

			cal.setTime(d);
			cal.add(Calendar.DAY_OF_YEAR, 1);
			d = cal.getTime();
		}
	}

	private static void checkForMissingFilesByNumber(String fileType, Map<String, Date> hmReceivedDate, List<String> files, String delimiter, int token) {

		SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd");

		int maxNum = 0;
		int minNum = Integer.MAX_VALUE;
		Map<Integer, List<String>> hmByNum = new HashMap<>();

		for (String file: files) {
			String[] toks = file.split(delimiter);
			String tok = null;
			int num = 0;
			try {
				tok = toks[token];
				num = Integer.parseInt(tok);
			} catch (Exception ex) {
				LOG.error("Exception with " + file + " tok = [" + tok + "] and toks " + toks , ex);
				return;
			}

			maxNum = Math.max(num, maxNum);
			minNum = Math.min(num, minNum);

			List<String> l = hmByNum.get(new Integer(num));
			if (l == null) {
				l = new ArrayList<>();
				hmByNum.put(new Integer(num), l);
			}
			l.add(file);
		}

		LOG.info("Checking for number range " + minNum + " to " + maxNum);

		for (int i=minNum; i<=maxNum; i++) {
			List<String> l = hmByNum.get(new Integer(i));
			if (l == null) {
				List<String> before = hmByNum.get(new Integer(i-1));
				String beforeDesc = null;
				if (before != null) {
					String firstBefore = before.get(0);
					Date beforeReceived = hmReceivedDate.get(firstBefore);
					beforeDesc = firstBefore + " on " + sdfOutput.format(beforeReceived);
				}

				List<String> after = hmByNum.get(new Integer(i+1));
				String afterDesc = null;
				if (after != null) {
					String firstAfter = after.get(0);
					Date afterReceived = hmReceivedDate.get(firstAfter);
					afterDesc = firstAfter + " on " + sdfOutput.format(afterReceived);
				}

				LOG.error("No " + fileType + " file found for " + i + " previous [" + beforeDesc + "] after [" + afterDesc + "]");
			}
		}
	}*/

	/*private static void deleteEnterpriseObs(String filePath, String configName, int batchSize) {
		LOG.info("Deleting Enterprise Observations");
		try {

			String parent = FilenameUtils.getFullPath(filePath);
			String name = FilenameUtils.getName(filePath);
			String doneFilePath = FilenameUtils.concat(parent, "DONE" + name);

			Set<String> doneIds = new HashSet<>();
			File f = new File(doneFilePath);
			if (f.exists()) {
				List<String> doneLines = Files.readAllLines(f.toPath());
				for (String doneLine: doneLines) {
					doneIds.add(doneLine);
				}
			}
			LOG.debug("Previously done " + doneIds.size());

			List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openConnection(configName);

			CSVParser parser = CSVParser.parse(new File(filePath), Charset.defaultCharset(), CSVFormat.TDF.withHeader());
			Iterator<CSVRecord> iterator = parser.iterator();

			List<String> batch = new ArrayList<>();

			while (iterator.hasNext()) {
				CSVRecord record = iterator.next();
				String id = record.get("id");
				if (doneIds.contains(id)) {
					continue;
				}
				doneIds.add(id);
				batch.add(id);

				if (batch.size() >= batchSize) {
					saveBatch(batch, connectionWrappers, doneFilePath);
				}
				if (doneIds.size() % 1000 == 0) {
					LOG.debug("Done " + doneIds.size());
				}
			}

			if (!batch.isEmpty()) {
				saveBatch(batch, connectionWrappers, doneFilePath);
				LOG.debug("Done " + doneIds.size());
			}

			parser.close();

			LOG.info("Finished Deleting Enterprise Observations");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void saveBatch(List<String> batch, List<EnterpriseConnector.ConnectionWrapper> connectionWrappers, String doneFilePath) throws Exception {

		for (EnterpriseConnector.ConnectionWrapper connectionWrapper: connectionWrappers) {
			String sql = "DELETE FROM observation WHERE id = ?";

			Connection connection = connectionWrapper.getConnection();
			PreparedStatement ps = connection.prepareStatement(sql);


			for (String id: batch) {
				ps.setLong(1, Long.parseLong(id));
				ps.addBatch();
			}

			ps.executeBatch();

			connection.commit();

			ps.close();
			connection.close();
		}

		//update audit
		Files.write(new File(doneFilePath).toPath(), batch, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

		batch.clear();
	}

	/*private static void testS3Listing(String path) {
		LOG.info("Testing S3 Listing");
		try {

			LOG.info("Trying with full path: " + path);
			List<FileInfo> l = FileHelper.listFilesInSharedStorageWithInfo(path);
			LOG.info("Found " + l.size());
			*//*for (FileInfo info: l) {
				LOG.info("Got " + info.getFilePath());
			}*//*

			String parent = FilenameUtils.getFullPath(path);
			LOG.info("Trying with parent: " + parent);
			l = FileHelper.listFilesInSharedStorageWithInfo(parent);
			LOG.info("Found " + l.size());
			*//*for (FileInfo info: l) {
				LOG.info("Got " + info.getFilePath());
			}*//*

			LOG.info("Finished Testing S3 Listing");
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}*/

	/*private static void testAuditingFile(UUID serviceId, UUID systemId, UUID exchangeId, String version, String filePath) {
		LOG.info("Testing Auditing File");
		try {

			LOG.info("Creating parser");
			//org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation obsParser = new org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation(serviceId, systemId, exchangeId, version, filePath);
			org.endeavourhealth.transform.tpp.csv.schema.staff.SRStaffMemberProfile obsParser = new org.endeavourhealth.transform.tpp.csv.schema.staff.SRStaffMemberProfile(serviceId, systemId, exchangeId, version, filePath);

			LOG.info("Created parser");
			obsParser.nextRecord();
			LOG.info("Done auditing");
			obsParser.close();
			LOG.info("Closed");

			LOG.info("Finish Testing Auditing File");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void postPatientToProtocol(String odsCode, String patientUuid) {
		LOG.info("Posting patient " + patientUuid + " for " + odsCode + " to Protocol queue");
		try {
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			Service service = serviceDal.getByLocalIdentifier(odsCode);
			LOG.info("Service " + service.getId() + " -> " + service.getName());

			UUID patientId = UUID.fromString(patientUuid);

			List<UUID> systemIds = findSystemIds(service);
			if (systemIds.size() != 1) {
				throw new Exception("Found " + systemIds.size() + " for service");
			}
			UUID systemId = systemIds.get(0);
			UUID serviceId = service.getId();

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			LOG.info("Found " + exchanges.size() + " exchanges");

			ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();

			//exchanges are in order most recent first, so iterate backwards to get them in date order
			for (int i=exchanges.size()-1; i>=0; i--) {
				Exchange exchange = exchanges.get(i);


				List<UUID> batchesForPatient = new ArrayList<>();

				List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(exchange.getId());
				for (ExchangeBatch batch: batches) {
					if (batch.getEdsPatientId() != null
							&& batch.getEdsPatientId().equals(patientId)) {

						batchesForPatient.add(batch.getBatchId());
					}
				}

				if (!batchesForPatient.isEmpty()) {
					LOG.debug("Posting " + batchesForPatient.size() + " for exchange " + exchange.getId() + " to rabbit");

					//set new batch ID in exchange header
					String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchesForPatient.toArray());
					exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

					//post new batch to protocol Q
					PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");
					PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
					component.process(exchange);
				}
			}

			LOG.info("Finished posting patient " + patientUuid + " for " + odsCode + " to Protocol queue");
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}*/

	private static void postPatientsToProtocol(UUID serviceId, UUID systemId, String sourceFile) {

		try {
			LOG.info("Posting patient from " + sourceFile + " for " + serviceId + " to Protocol queue");
			Set<UUID> hsPatientUuids = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(sourceFile).toPath());
			for (String line: lines) {
				hsPatientUuids.add(UUID.fromString(line));
			}
			LOG.info("Found " + hsPatientUuids.size() + " patient IDs");

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			Service service = serviceDal.getById(serviceId);
			LOG.info("Service " + service.getId() + " -> " + service.getName());

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			LOG.info("Found " + exchanges.size() + " exchanges");

			ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();

			//exchanges are in order most recent first, so iterate backwards to get them in date order
			for (int i=exchanges.size()-1; i>=0; i--) {
				Exchange exchange = exchanges.get(i);

				List<UUID> batchesForPatient = new ArrayList<>();

				List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(exchange.getId());
				for (ExchangeBatch batch: batches) {
					UUID patientId = batch.getEdsPatientId();
					if (patientId != null
							&& hsPatientUuids.contains(patientId)) {

						batchesForPatient.add(batch.getBatchId());
					}
				}

				if (!batchesForPatient.isEmpty()) {
					LOG.debug("Posting " + batchesForPatient.size() + " for exchange " + exchange.getId() + " to rabbit");

					//set new batch ID in exchange header
					String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchesForPatient.toArray());
					exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

					//post new batch to protocol Q
					PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");
					PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
					component.process(exchange);
				}
			}

			LOG.info("Finished posting patients from " + sourceFile + " for " + serviceId + " to Protocol queue");
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}

	/*private static void testXml() {
		LOG.info("Testing XML");
		try {

			//PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");

			Map<String, String> queueReadConfigs = ConfigManager.getConfigurations("queuereader");
			for (String configId: queueReadConfigs.keySet()) {
				LOG.debug("Checking config XML for " + configId);
				String configXml = queueReadConfigs.get(configId);

				if (configXml.startsWith("{")) {
					LOG.debug("Skipping JSON");
					continue;
				}

				try {
					ApiConfiguration config = ConfigWrapper.deserialise(configXml);
					//LOG.debug("Deserialised as messaging API XML");
					ApiConfiguration.PostMessageAsync postConfig = config.getPostMessageAsync();

				} catch (Exception ex) {

					try {
						QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);

					} catch (Exception ex2) {
						LOG.error(configXml);
						LOG.error("", ex2);
					}
				}

			}
			LOG.info("Testing XML");
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}*/

	/*private static void testMetrics() {
		LOG.info("Testing Metrics");
		try {


			Random r = new Random(System.currentTimeMillis());

			while (true) {

				String metric1 = "frailty-api.ms-duration";
				Integer value1 = new Integer(r.nextInt(1000));
				MetricsHelper.recordValue(metric1, value1);

				if (r.nextBoolean()) {
					MetricsHelper.recordEvent("frailty-api.response-code-200");
				} else {
					MetricsHelper.recordEvent("frailty-api.response-code-400");
				}

				int sleep = r.nextInt(10 * 1000);
				LOG.debug("Waiting " + sleep + " ms");

				Thread.sleep(sleep);

			}

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void testGraphiteMetrics(String host, String port) {
		LOG.info("Testing Graphite metrics to " + host + " " + port);
		try {

			InetAddress ip = InetAddress.getLocalHost();
			String hostname = ip.getHostName();
			LOG.debug("Hostname = " + hostname);

			String appId = ConfigManager.getAppId();
			LOG.debug("AppID = " + appId);

			Random r = new Random(System.currentTimeMillis());

			while (true) {

				Map<String, Object> metrics = new HashMap<>();

				String metric1 = hostname + "." + appId + ".frailty-api.duration-ms";
				Integer value1 = new Integer(r.nextInt(1000));
				metrics.put(metric1, value1);

				String metric2 = hostname + "." + appId+ ".frailty-api.response-code";
				Integer value2 = null;
				if (r.nextBoolean()) {
					value2 = new Integer(200);
				} else {
					value2 = new Integer(400);
				}
				metrics.put(metric2, value2);

				long timestamp = System.currentTimeMillis() / 1000;

				LOG.debug("Sending metrics");
				sendMetrics(host, Integer.parseInt(port), metrics, timestamp);

				int sleep = r.nextInt(10 * 1000);
				LOG.debug("Waiting " + sleep + " ms");

				Thread.sleep(sleep);
			}

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static  void sendMetrics(String graphiteHost, int graphitePort, Map<String, Object> metrics, long timeStamp) throws Exception {

		Socket socket = new Socket(graphiteHost, graphitePort);
		OutputStream s = socket.getOutputStream();
		PrintWriter out = new PrintWriter(s, true);
		for (Map.Entry<String, Object> metric: metrics.entrySet()) {
			if (metric.getValue() instanceof Integer) {
				out.printf("%s %d %d%n", metric.getKey(), ((Integer)metric.getValue()).intValue(), timeStamp);
			}
			else if (metric.getValue() instanceof Float) {
				out.printf("%s %f %d%n", metric.getKey(), ((Float)metric.getValue()).floatValue(), timeStamp);

			} else {
				throw new RuntimeException("Unsupported type " + metric.getValue().getClass());
			}
		}
		out.close();
		socket.close();
	}*/

	/*private static void fixEmisDeletedPatients(String odsCode) {
		LOG.info("Fixing Emis Deleted Patients for " + odsCode);
		try {

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			Service service = serviceDal.getByLocalIdentifier(odsCode);
			LOG.info("Service " + service.getId() + " -> " + service.getName());

			List<UUID> systemIds = findSystemIds(service);
			if (systemIds.size() != 1) {
				throw new Exception("Found " + systemIds.size() + " for service");
			}
			UUID systemId = systemIds.get(0);
			UUID serviceId = service.getId();

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			LOG.info("Found " + exchanges.size() + " exchanges");

			Set<String> hsPatientGuidsDeductedDeceased = new HashSet<>();
			Map<String, List<UUID>> hmPatientGuidsDeleted = new HashMap<>();

			Map<String, List<String>> hmPatientGuidsToFix = new HashMap<>();

			//exchanges are in REVERSE order (most recent first)
			for (int i=exchanges.size()-1; i>=0; i--) {

				Exchange exchange = exchanges.get(i);
				List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());

				//skip exchanges that are for custom extracts
				if (files.size() <= 1) {
					continue;
				}

				//skip if we're ignoring old data
				boolean processPatientData = EmisCsvToFhirTransformer.shouldProcessPatientData(serviceId, files);
				if (!processPatientData) {
					continue;
				}

				//find patient file
				ExchangePayloadFile patientFile = findFileOfType(files, "Admin_Patient");
				if (patientFile == null) {
					throw new Exception("Failed to find Admin_Patient file in exchange " + exchange.getId());
				}

				ExchangePayloadFile agreementFile = findFileOfType(files, "Agreements_SharingOrganisation");
				if (agreementFile == null) {
					throw new Exception("Failed to find Agreements_SharingOrganisation file in exchange " + exchange.getId());
				}

				//work out file version
				List<ExchangePayloadFile> filesTmp = new ArrayList<>();
				filesTmp.add(patientFile);
				filesTmp.add(agreementFile);
				String version = EmisCsvToFhirTransformer.determineVersion(filesTmp);

				//see if sharing agreement is disabled
				String path = agreementFile.getPath();
				org.endeavourhealth.transform.emis.csv.schema.agreements.SharingOrganisation agreementParser = new org.endeavourhealth.transform.emis.csv.schema.agreements.SharingOrganisation(serviceId, systemId, exchange.getId(), version, path);

				agreementParser.nextRecord();
				CsvCell disabled = agreementParser.getDisabled();
				boolean isDisabled = disabled.getBoolean();

				//create the parser
				path = patientFile.getPath();
				org.endeavourhealth.transform.emis.csv.schema.admin.Patient parser = new org.endeavourhealth.transform.emis.csv.schema.admin.Patient(serviceId, systemId, exchange.getId(), version, path);

				while (parser.nextRecord()) {

					CsvCell patientGuidCell = parser.getPatientGuid();
					String patientGuid = patientGuidCell.getString();

					CsvCell dateOfDeathCell = parser.getDateOfDeath();
					CsvCell dateOfDeductionCell = parser.getDateOfDeactivation();

					CsvCell deletedCell = parser.getDeleted();
					if (deletedCell.getBoolean()) {

						List<UUID> exchangesDeleted = hmPatientGuidsDeleted.get(patientGuid);
						if (exchangesDeleted == null) {
							exchangesDeleted = new ArrayList<>();
							hmPatientGuidsDeleted.put(patientGuid, exchangesDeleted);
						}
						exchangesDeleted.add(exchange.getId());

						//if this patient was previously updated with a deduction date or date of death, and the sharing
						//agreement isn't disabled, then we will have deleted them and need to undelete
						if (hsPatientGuidsDeductedDeceased.contains(patientGuid)
								&& !isDisabled) {

							List<String> exchangesToFix = hmPatientGuidsToFix.get(patientGuid);
							if (exchangesToFix == null) {
								exchangesToFix = new ArrayList<>();
								hmPatientGuidsToFix.put(patientGuid, exchangesToFix);
							}
							exchangesToFix.add(exchange.getId().toString() + ": Deducted/Dead and Deleted after");
						}

					} else {

						//if the date of death of deduction is set then we need to track this
						//because we're going to possibly get a delete in a years time
						if (!dateOfDeathCell.isEmpty() || !dateOfDeductionCell.isEmpty()) {
							hsPatientGuidsDeductedDeceased.add(patientGuid);
						} else {
							hsPatientGuidsDeductedDeceased.remove(patientGuid);
						}

						//if this patient was previously deleted and is now UN-deleted, then we'll
						//need to fix the record
						if (hmPatientGuidsDeleted.containsKey(patientGuid)) {
							List<UUID> exchangesDeleted = hmPatientGuidsDeleted.remove(patientGuid);

							List<String> exchangesToFix = hmPatientGuidsToFix.get(patientGuid);
							if (exchangesToFix == null) {
								exchangesToFix = new ArrayList<>();
								hmPatientGuidsToFix.put(patientGuid, exchangesToFix);
							}

							for (UUID exchangeId: exchangesDeleted) {
								exchangesToFix.add(exchangeId.toString() + ": Deleted and subsequently undeleted");
							}

						}
					}
				}

				parser.close();
			}

			LOG.info("Finished checking for affected patients - found " + hmPatientGuidsToFix.size() + " patients to fix");

			for (String patientGuid: hmPatientGuidsToFix.keySet()) {
				List<String> exchangeIds = hmPatientGuidsToFix.get(patientGuid);
				LOG.info("Patient " + patientGuid);
				for (String exchangeId: exchangeIds) {
					LOG.info("    Exchange Id " + exchangeId);
				}

				//log out the UUID for the patient too
				EmisCsvHelper csvHelper = new EmisCsvHelper(serviceId, systemId, null, null, false, null);
				Reference ref = ReferenceHelper.createReference(ResourceType.Patient, patientGuid);
				ref = IdHelper.convertLocallyUniqueReferenceToEdsReference(ref, csvHelper);
				LOG.debug("    Patient UUID " + ref.getReference());

				String patientUuidStr = ReferenceHelper.getReferenceId(ref);
				UUID patientUuid = UUID.fromString(patientUuidStr);

				Set<UUID> hsExchangeIdsDone = new HashSet<>();
				Set<String> resourcesDone = new HashSet<>();

				for (String exchangeId: exchangeIds) {
					UUID exchangeUuid = UUID.fromString(exchangeId.split(":")[0]);

					//in some cases, the same exchange was found twice
					if (hsExchangeIdsDone.contains(exchangeUuid)) {
						continue;
					}
					hsExchangeIdsDone.add(exchangeUuid);

					Exchange exchange = exchangeDal.getExchange(exchangeUuid);

					ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();
					ResourceDalI resourceDal = DalProvider.factoryResourceDal();

					List<UUID> batchIdsCreated = new ArrayList<>();
					TransformError transformError = new TransformError();
					FhirResourceFiler filer = new FhirResourceFiler(exchangeUuid, serviceId, systemId, transformError, batchIdsCreated);

					//get all exchange batches for our patient
					List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(exchangeUuid);
					for (ExchangeBatch batch: batches) {
						UUID batchPatient = batch.getEdsPatientId();
						if (batchPatient == null || !batchPatient.equals(patientUuid)) {
							continue;
						}

						//get all resources for this batch
						List<ResourceWrapper> resourceWrappers = resourceDal.getResourcesForBatch(serviceId, batch.getBatchId());

						//restore each resource
						for (ResourceWrapper resourceWrapper: resourceWrappers) {

							//if an exchange was processed multiple times, we might try to pick up the same resource twice, so skip it
							String resourceRef = ReferenceHelper.createResourceReference(resourceWrapper.getResourceType(), resourceWrapper.getResourceId().toString());
							if (resourcesDone.contains(resourceRef)) {
								continue;
							}
							resourcesDone.add(resourceRef);

							List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, resourceWrapper.getResourceType(), resourceWrapper.getResourceId());

							//most recent is first
							ResourceWrapper mostRecent = history.get(0);
							if (!mostRecent.isDeleted()) {
								continue;
							}

							//find latest non-deleted version and save it over the deleted version
							for (ResourceWrapper historyItem: history) {
								if (!historyItem.isDeleted()) {

									org.hl7.fhir.instance.model.Resource resource = FhirSerializationHelper.deserializeResource(historyItem.getResourceData());
									GenericBuilder builder = new GenericBuilder(resource);
									filer.savePatientResource(null, false, builder);

									break;
								}
							}
						}
					}

					filer.waitToFinish();

					//set new batch ID in exchange header
					String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
					exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

					//post new batch to protocol Q
					PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");
					PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
					component.process(exchange);
				}

			}

			LOG.info("Finished Fixing Emis Deleted Patients for " + odsCode);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static ExchangePayloadFile findFileOfType(List<ExchangePayloadFile> files, String fileType) {

		for (ExchangePayloadFile file: files) {
			if (file.getType().equals(fileType)) {
				return file;
			}
		}
		return null;
	}

	/*private static void fixEmisEpisodes2(String odsCode) {
		LOG.info("Fixing Emis Episodes (2) for " + odsCode);
		try {

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			Service service = serviceDal.getByLocalIdentifier(odsCode);
			LOG.info("Service " + service.getId() + " -> " + service.getName());

			List<UUID> systemIds = findSystemIds(service);
			if (systemIds.size() != 1) {
				throw new Exception("Found " + systemIds.size() + " for service");
			}
			UUID systemId = systemIds.get(0);
			UUID serviceId = service.getId();

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			LOG.info("Found " + exchanges.size() + " exchanges");

			InternalIdDalI internalIdDal = DalProvider.factoryInternalIdDal();

			Set<String> patientGuidsDone = new HashSet<>();

			//exchanges are in REVERSE order (most recent first)
			for (int i=exchanges.size()-1; i>=0; i--) {

				Exchange exchange = exchanges.get(i);
				List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());

				//skip exchanges that are for custom extracts
				if (files.size() <= 1) {
					continue;
				}

				//skip if we're ignoring old data
				boolean processPatientData = EmisCsvToFhirTransformer.shouldProcessPatientData(serviceId, files);
				if (!processPatientData) {
					continue;
				}

				//find patient file
				ExchangePayloadFile patientFile = null;
				for (ExchangePayloadFile file: files) {
					if (file.getType().equals("Admin_Patient")) {
						patientFile = file;
						break;
					}
				}
				if (patientFile == null) {
					throw new Exception("Failed to find Admin_Patient file in exchange " + exchange.getId());
				}

				String path = patientFile.getPath();
				List<ExchangePayloadFile> filesTmp = new ArrayList<>();
				filesTmp.add(patientFile);


				String version = EmisCsvToFhirTransformer.determineVersion(filesTmp);
				org.endeavourhealth.transform.emis.csv.schema.admin.Patient parser = new org.endeavourhealth.transform.emis.csv.schema.admin.Patient(serviceId, systemId, exchange.getId(), version, path);

				while (parser.nextRecord()) {

					CsvCell deletedCell = parser.getDeleted();
					if (deletedCell.getBoolean()) {
						continue;
					}

					//skip patients already done
					CsvCell patientGuidCell = parser.getPatientGuid();
					String patientGuid = patientGuidCell.getString();
					if (patientGuidsDone.contains(patientGuid)) {
						continue;
					}
					patientGuidsDone.add(patientGuid);

					//check we've not already converted this patient previously (i.e. re-running this conversion)
					CsvCell startDateCell = parser.getDateOfRegistration();
					if (startDateCell.isEmpty()) {
						LOG.error("Missing start date for patient " + patientGuid + " in exchange " + exchange.getId());
						startDateCell = CsvCell.factoryDummyWrapper("1900-01-01");
					}

					//save internal ID map
					String key = patientGuidCell.getString();
					String value = startDateCell.getString();
					internalIdDal.save(serviceId, "Emis_Latest_Reg_Date", key, value);
				}

				parser.close();
			}

			LOG.info("Finished Fixing Emis Episodes (2) for " + odsCode);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void fixEmisEpisodes1(String odsCode) {
		LOG.info("Fixing Emis Episodes (1) for " + odsCode);
		try {

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			Service service = serviceDal.getByLocalIdentifier(odsCode);
			LOG.info("Service " + service.getId() + " -> " + service.getName());

			List<UUID> systemIds = findSystemIds(service);
			if (systemIds.size() != 1) {
				throw new Exception("Found " + systemIds.size() + " for service");
			}
			UUID systemId = systemIds.get(0);
			UUID serviceId = service.getId();

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			LOG.info("Found " + exchanges.size() + " exchanges");

			InternalIdDalI internalIdDal = DalProvider.factoryInternalIdDal();

			Set<String> patientGuidsDone = new HashSet<>();

			//exchanges are in REVERSE order (most recent first)
			for (Exchange exchange: exchanges) {
				List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());

				//skip exchanges that are for custom extracts
				if (files.size() <= 1) {
					continue;
				}

				//skip if we're ignoring old data
				boolean processPatientData = EmisCsvToFhirTransformer.shouldProcessPatientData(serviceId, files);
				if (!processPatientData) {
					continue;
				}

				//find patient file
				ExchangePayloadFile patientFile = null;
				for (ExchangePayloadFile file: files) {
					if (file.getType().equals("Admin_Patient")) {
						patientFile = file;
						break;
					}
				}
				if (patientFile == null) {
					throw new Exception("Failed to find Admin_Patient file in exchange " + exchange.getId());
				}

				String path = patientFile.getPath();
				List<ExchangePayloadFile> filesTmp = new ArrayList<>();
				filesTmp.add(patientFile);

				String version = EmisCsvToFhirTransformer.determineVersion(filesTmp);
				org.endeavourhealth.transform.emis.csv.schema.admin.Patient parser = new org.endeavourhealth.transform.emis.csv.schema.admin.Patient(serviceId, systemId, exchange.getId(), version, path);

				while (parser.nextRecord()) {

					CsvCell deletedCell = parser.getDeleted();
					if (deletedCell.getBoolean()) {
						continue;
					}

					//skip patients already done
					CsvCell patientGuidCell = parser.getPatientGuid();
					String patientGuid = patientGuidCell.getString();
					if (patientGuidsDone.contains(patientGuid)) {
						continue;
					}
					patientGuidsDone.add(patientGuid);

					//check we've not already converted this patient previously (i.e. re-running this conversion)
					String key = patientGuidCell.getString();
					String existingIdMapValue = internalIdDal.getDestinationId(serviceId, "Emis_Latest_Reg_Date", key);
					if (existingIdMapValue != null) {
						continue;
					}

					CsvCell startDateCell = parser.getDateOfRegistration();
					if (startDateCell.isEmpty()) {
						LOG.error("Missing start date for patient " + patientGuid + " in exchange " + exchange.getId());
						startDateCell = CsvCell.factoryDummyWrapper("1900-01-01");
					}

					//find the existing UUID we've previously allocated
					String oldSourceId = patientGuid;
					UUID episodeUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.EpisodeOfCare, oldSourceId);
					if (episodeUuid == null) {
						LOG.error("Null episode UUID for old source ID " + oldSourceId + " in exchange " + exchange.getId());
						continue;
					}

					//save ID reference mapping
					String newSourceId = patientGuid + ":" + startDateCell.getString();
 					UUID newEpisodeUuid = IdHelper.getOrCreateEdsResourceId(serviceId, ResourceType.EpisodeOfCare, newSourceId, episodeUuid);
					if (!newEpisodeUuid.equals(episodeUuid)) {
						throw new Exception("Failed to carry over UUID for episode. Old UUID was " + episodeUuid + " new UUID is " + newEpisodeUuid + " in exchange " + exchange.getId());
					}

					//save internal ID map
					String value = startDateCell.getString();
					internalIdDal.save(serviceId, "Emis_Latest_Reg_Date", key, value);

				}

				parser.close();
			}

			LOG.info("Finished Fixing Emis Episodes (1) for " + odsCode);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void testRabbit(String nodes, String username, String password, String sslProtocol, String exchangeName, String queueName) {
		LOG.info("Testing RabbitMQ Connectivity on " + nodes);
		LOG.info("SSL Protocol = " + sslProtocol);
		LOG.info("Exchange = " + exchangeName);
		LOG.info("Queue = " + queueName);

		try {

			//test publishing
			LOG.info("Testing publishing...");
			com.rabbitmq.client.Connection publishConnection = org.endeavourhealth.core.queueing.ConnectionManager.getConnection(username, password, nodes, sslProtocol);
			Channel publishChannel = org.endeavourhealth.core.queueing.ConnectionManager.getPublishChannel(publishConnection, exchangeName);
			publishChannel.confirmSelect();

			for (int i=0; i<5; i++) {

				Map<String, Object> headers = new HashMap<>();
				headers.put("HeaderIndex", "" + i);

				AMQP.BasicProperties properties = new AMQP.BasicProperties()
						.builder()
						.deliveryMode(2)    // Persistent message
						.headers(headers)
						.build();

				String body = "MessageIndex = " + i;
				byte[] bytes = body.getBytes();

				publishChannel.basicPublish(
						exchangeName,
						"All", //routing key
						properties,
						bytes);
			}
			publishChannel.close();
			publishConnection.close();
			LOG.info("...Finished testing publishing");

			//test consuming
			LOG.info("Testing reading...");

			com.rabbitmq.client.Connection readConnection = org.endeavourhealth.core.queueing.ConnectionManager.getConnection(username, password, nodes, sslProtocol);
			Channel readChannel = readConnection.createChannel();
			readChannel.basicQos(1);

			Consumer consumer = new TestRabbitConsumer(readChannel);
			readChannel.basicConsume(queueName, false, "TestRabbitConsumer", false, true, null, consumer);

			LOG.info("Reader Connected (ctrl+c to close) will quit in 30s");
			Thread.sleep(30 * 1000);

			LOG.info("Finished Testing RabbitMQ Connectivity");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/



	private static void populateLastDataDate(int threads, int batchSize) {
		LOG.debug("Populating last data date");
		try {

			int processed = 0;
			AtomicInteger fixed = new AtomicInteger();

			ThreadPool threadPool = new ThreadPool(threads, batchSize);

			while (true) {

				String sql = "SELECT id FROM drewtest.exchange_ids WHERE done = 0 LIMIT " + batchSize;
				//LOG.debug("Getting new batch using: " + sql);

				EntityManager auditEntityManager = ConnectionManager.getAuditEntityManager();
				SessionImpl auditSession = (SessionImpl)auditEntityManager.getDelegate();
				Connection auditConnection = auditSession.connection();

				Statement statement = auditConnection.createStatement();
				ResultSet rs = statement.executeQuery(sql);

				List<UUID> exchangeIds = new ArrayList<>();
				while (rs.next()) {
					String s = rs.getString(1);
					//LOG.debug("Got back exchange ID " + s);
					exchangeIds.add(UUID.fromString(s));
				}

				rs.close();
				statement.close();
				auditEntityManager.close();

				for (UUID exchangeId: exchangeIds) {
					threadPool.submit(new PopulateDataDateCallable(exchangeId, fixed));
				}

				List<ThreadPoolError> errs = threadPool.waitUntilEmpty();
				if (!errs.isEmpty()) {
					LOG.debug("Got " + errs.size() + " errors");
					for (ThreadPoolError err: errs) {
						LOG.error("", err.getException());
					}
					break;
				}

				processed += exchangeIds.size();
				LOG.debug("processed " + processed + " fixed " + fixed.get());

				//if finished
				if (exchangeIds.size() < batchSize) {
					break;
				}
			}

			threadPool.waitAndStop();

			LOG.debug("Finished Populating last data date");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}



	/*private static void fixEmisMissingSlots(String serviceOdsCode) {
		LOG.debug("Fixing Emis Missing Slots for " + serviceOdsCode);
		try {
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			Service service = serviceDal.getByLocalIdentifier(serviceOdsCode);
			LOG.info("Service " + service.getId() + " " + service.getName() + " " + service.getLocalId());

			List<UUID> systemIds = findSystemIds(service);
			if (systemIds.size() != 1) {
				throw new Exception("Found " + systemIds.size() + " for service");
			}
			UUID systemId = systemIds.get(0);
			UUID serviceId = service.getId();

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);

			Set<String> hsSlotsToSkip = new HashSet<>();
			ResourceDalI resourceDal = DalProvider.factoryResourceDal();

			File auditFile = new File("SlotAudit_" + serviceOdsCode + ".csv");
			LOG.debug("Auditing to " + auditFile);

			PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");
			if (exchangeConfig == null) {
				throw new Exception("Failed to find PostMessageToExchange config details for exchange EdsProtocol");
			}

			//the list of exchanges is most-recent-first, so iterate backwards to do them in order
			for (Exchange exchange : exchanges) {

				List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());

				//skip exchanges that are for custom extracts
				if (files.size() <= 1) {
					continue;
				}
				boolean processPatientData = EmisCsvToFhirTransformer.shouldProcessPatientData(serviceId, files);
				if (!processPatientData) {
					continue;
				}

				ExchangeTransformAudit transformAudit = new ExchangeTransformAudit();
				transformAudit.setServiceId(serviceId);
				transformAudit.setSystemId(systemId);
				transformAudit.setExchangeId(exchange.getId());
				transformAudit.setId(UUID.randomUUID());
				transformAudit.setStarted(new Date());

				String version = EmisCsvToFhirTransformer.determineVersion(files);

				EmisCsvHelper csvHelper = new EmisCsvHelper(serviceId, systemId, exchange.getId(), null, processPatientData, null);

				//the processor is responsible for saving FHIR resources
				TransformError transformError = new TransformError();
				List<UUID> batchIdsCreated = new ArrayList<>();
				FhirResourceFiler fhirResourceFiler = new FhirResourceFiler(exchange.getId(), serviceId, systemId, transformError, batchIdsCreated);

				Map<Class, AbstractCsvParser> parsers = new HashMap<>();
				EmisCsvToFhirTransformer.createParsers(serviceId, systemId, exchange.getId(), files, version, parsers);

				try {

					//cache the practitioners for each session
					SessionUserTransformer.transform(parsers, fhirResourceFiler, csvHelper);

					Slot parser = (Slot) parsers.get(Slot.class);
					while (parser.nextRecord()) {

						//should this record be transformed?

						//the slots CSV contains data on empty slots too; ignore them
						CsvCell patientGuid = parser.getPatientGuid();
						if (patientGuid.isEmpty()) {
							continue;
						}

						//the EMIS data contains thousands of appointments that refer to patients we don't have, so I'm explicitly
						//handling this here, and ignoring any Slot record that is in this state
						UUID patientEdsId = IdHelper.getEdsResourceId(fhirResourceFiler.getServiceId(), ResourceType.Patient, patientGuid.getString());
						if (patientEdsId == null) {
							continue;
						}

						//see if this appointment has previously been transformed
						CsvCell slotGuid = parser.getSlotGuid();
						String uniqueId = patientGuid.getString() + ":" + slotGuid.getString();
						if (!hsSlotsToSkip.contains(uniqueId)) {

							//transform this slot record if no appt already exists for it
							boolean alreadyExists = false;
							UUID discoveryId = IdHelper.getEdsResourceId(serviceId, ResourceType.Slot, uniqueId);
							if (discoveryId != null) {
								List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Slot.toString(), discoveryId);
								if (!history.isEmpty()) {
									alreadyExists = true;
								}
							}

							if (alreadyExists) {
								hsSlotsToSkip.add(uniqueId);
							}
						}

						if (hsSlotsToSkip.contains(uniqueId)) {
							continue;
						}
						hsSlotsToSkip.add(uniqueId);

						try {
							LOG.debug("Creating slot for " + uniqueId);
							SlotTransformer.createSlotAndAppointment((Slot) parser, fhirResourceFiler, csvHelper);
						} catch (Exception ex) {
							fhirResourceFiler.logTransformRecordError(ex, parser.getCurrentState());
						}
					}

					csvHelper.clearCachedSessionPractitioners();

					fhirResourceFiler.failIfAnyErrors();
					fhirResourceFiler.waitToFinish();

				} catch (Throwable ex) {

					Map<String, String> args = new HashMap<>();
					args.put(TransformErrorUtility.ARG_FATAL_ERROR, ex.getMessage());
					TransformErrorUtility.addTransformError(transformError, ex, args);

					LOG.error("", ex);
				}

				transformAudit.setEnded(new Date());
				transformAudit.setNumberBatchesCreated(new Integer(batchIdsCreated.size()));

				if (transformError.getError().size() > 0) {
					transformAudit.setErrorXml(TransformErrorSerializer.writeToXml(transformError));
				}

				//save our audit if something went wrong or was saved
				if (transformError.getError().size() > 0
						|| !batchIdsCreated.isEmpty()) {

					exchangeDal.save(transformAudit);
				}

				//send to Rabbit protocol queue
				if (!batchIdsCreated.isEmpty()) {

					//write batch ID to file, so we have an audit of what we created
					List<String> lines = new ArrayList<>();
					for (UUID batchId : batchIdsCreated) {
						lines.add("\"" + exchange.getId() + "\",\"" + batchId + "\"");
					}
					Files.write(auditFile.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

					String batchesJson = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
					exchange.setHeader(HeaderKeys.BatchIdsJson, batchesJson);

					//send to Rabbit
					PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
					component.process(exchange);
				}

				if (transformError.getError().size() > 0) {
					throw new Exception("Dropping out due to error in transform");
				}
			}

			LOG.debug("Finished Fixing Emis Missing Slots for " + serviceOdsCode);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}


	private static void findBartsPersonIds(String sourceFile, UUID serviceUuid, UUID systemUuid, String dateCutoffStr, String destFile) {
		LOG.debug("Finding Barts person IDs for " + sourceFile);
		try {

			//read NHS numbers into memory
			Set<String> hsNhsNumbers = new HashSet<>();
			List<String> listNhsNumbers = new ArrayList<>();
			File src = new File(sourceFile);
			List<String> lines = Files.readAllLines(src.toPath());
			for (String line : lines) {
				String s = line.trim();
				hsNhsNumbers.add(s);
				listNhsNumbers.add(s); //maintain a list so we can preserve the ordering
			}
			LOG.debug("Looking for Person IDs for " + hsNhsNumbers.size() + " nhs numbers or any since " + dateCutoffStr);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dateCutoff = sdf.parse(dateCutoffStr);

			Map<String, Set<String>> hmMatches = new HashMap<>();

			ExchangeDalI exchangeDalI = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDalI.getExchangesByService(serviceUuid, systemUuid, Integer.MAX_VALUE);
			for (Exchange exchange : exchanges) {
				List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());
				for (ExchangePayloadFile file : files) {


					String parentPath = new File(file.getPath()).getParent();
					String parentDir = FilenameUtils.getBaseName(parentPath);
					Date extractDate = sdf.parse(parentDir);
					boolean inDateRange = !extractDate.before(dateCutoff);

					String type = file.getType();
					if (type.equals("PPATI")) {

						PPATI parser = new PPATI(null, null, null, null, file.getPath());
						while (parser.nextRecord()) {
							CsvCell nhsNumberCell = parser.getNhsNumber();
							String nhsNumber = nhsNumberCell.getString();
							nhsNumber = nhsNumber.replace("-", "");
							if (hsNhsNumbers.contains(nhsNumber)
									|| inDateRange) {

								CsvCell personIdCell = parser.getMillenniumPersonId();
								String personId = personIdCell.getString();

								Set<String> s = hmMatches.get(nhsNumber);
								if (s == null) {
									s = new HashSet<>();
									hmMatches.put(nhsNumber, s);
								}
								s.add(personId);
							}
						}

						parser.close();

					} else if (type.equals("PPALI")) {

						PPALI parser = new PPALI(null, null, null, null, file.getPath());
						while (parser.nextRecord()) {

							CsvCell aliasCell = parser.getAlias();
							//not going to bother trying to filter on alias type, since it won't hurt to include
							//extra patients, if they have an MRN that accidentally matches one of the NHS numbers being searched for
							String alias = aliasCell.getString();
							if (hsNhsNumbers.contains(alias)
									|| inDateRange) {
								//NHS numbers in PPALI don't have the extra hyphens

								CsvCell personIdCell = parser.getMillenniumPersonIdentifier();
								String personId = personIdCell.getString();

								Set<String> s = hmMatches.get(alias);
								if (s == null) {
									s = new HashSet<>();
									hmMatches.put(alias, s);
								}
								s.add(personId);
							}
						}

						parser.close();

					} else {
						//just ignore other file types
					}
				}
			}

			LOG.debug("" + hmMatches.size() + " / " + hsNhsNumbers.size() + " NHS numbers had person IDs found");

			List<String> newLines = new ArrayList<>();

			for (String nhsNumber : listNhsNumbers) {
				Set<String> personIds = hmMatches.get(nhsNumber);
				if (personIds == null) {
					LOG.error("Failed to find person ID for " + nhsNumber);
					continue;
				}

				newLines.add("#NHS " + nhsNumber + ":");
				for (String personId : personIds) {
					newLines.add(personId);
				}
			}

			File dst = new File(destFile);
			if (dst.exists()) {
				dst.delete();
			}
			Files.write(dst.toPath(), newLines);
			LOG.debug("Finished Finding Barts person IDs for " + sourceFile);

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void createEmisDataTables() {
		LOG.debug("Creating Emis data tables");
		try {
			List<String> fileTypes = new ArrayList<>();

			fileTypes.add("Admin_Location");
			fileTypes.add("Admin_OrganisationLocation");
			fileTypes.add("Admin_Organisation");
			fileTypes.add("Admin_Patient");
			fileTypes.add("Admin_UserInRole");
			fileTypes.add("Agreements_SharingOrganisation");
			fileTypes.add("Appointment_SessionUser");
			fileTypes.add("Appointment_Session");
			fileTypes.add("Appointment_Slot");
			fileTypes.add("CareRecord_Consultation");
			fileTypes.add("CareRecord_Diary");
			fileTypes.add("CareRecord_ObservationReferral");
			fileTypes.add("CareRecord_Observation");
			fileTypes.add("CareRecord_Problem");
			fileTypes.add("Coding_ClinicalCode");
			fileTypes.add("Coding_DrugCode");
			fileTypes.add("Prescribing_DrugRecord");
			fileTypes.add("Prescribing_IssueRecord");
			fileTypes.add("Audit_PatientAudit");
			fileTypes.add("Audit_RegistrationAudit");

			for (String fileType : fileTypes) {
				createEmisDataTable(fileType);
			}

			LOG.debug("Finished Creating Emis data tables");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void createEmisDataTable(String fileType) throws Exception {

		ParserI parser = createParserForEmisFileType(fileType, null);
		if (parser == null) {
			return;
		}

		System.out.println("-- " + fileType);

		String table = fileType.replace(" ", "_");

		String dropSql = "DROP TABLE IF EXISTS `" + table + "`;";
		System.out.println(dropSql);

		String sql = "CREATE TABLE `" + table + "` (";

		sql += "file_name varchar(100)";
		sql += ", ";
		sql += "extract_date datetime";

		if (parser instanceof AbstractFixedParser) {

			AbstractFixedParser fixedParser = (AbstractFixedParser) parser;
			List<FixedParserField> fields = fixedParser.getFieldList();

			for (FixedParserField field : fields) {
				String col = field.getName();
				int len = field.getFieldlength();
				sql += ", ";
				sql += col.replace(" ", "_").replace("#", "").replace("/", "");
				sql += " varchar(";
				sql += len;
				sql += ")";
			}

		} else {

			List<String> cols = parser.getColumnHeaders();
			for (String col : cols) {
				sql += ", ";
				sql += col.replace(" ", "_").replace("#", "").replace("/", "");

				if (col.equals("BLOB_CONTENTS")
						|| col.equals("VALUE_LONG_TXT")
						|| col.equals("COMMENT_TXT")
						|| col.equals("NONPREG_REL_PROBLM_SCT_CD")) {

					sql += " mediumtext";

				} else if (col.indexOf("Date") > -1
						|| col.indexOf("Time") > -1) {
					sql += " varchar(10)";

				} else {
					sql += " varchar(255)";
				}
			}
		}

		sql += ");";
		/*LOG.debug("-- fileType");
		LOG.debug(sql);*/
		System.out.println(sql);
	}


	/*private static void convertFhirAudits(String publisherConfigName, int threads, int batchSize) throws Exception {
		LOG.info("Converting FHIR audit for " + publisherConfigName);
		try {
			//find a suitable service ID
			UUID dummyServiceId = null;
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			for (Service s : serviceDal.getAll()) {
				if (s.getPublisherConfigName() != null
						&& s.getPublisherConfigName().equalsIgnoreCase(publisherConfigName)) {
					dummyServiceId = s.getId();
					LOG.info("Found sample service ID " + s.getId() + " " + s.getName() + " " + s.getLocalId());
					break;
				}
			}

			EntityManager entityManager = ConnectionManager.getPublisherTransformEntityManager(dummyServiceId);
			SessionImpl session = (SessionImpl) entityManager.getDelegate();
			Connection connection = session.connection();

			//ensure all source files are mapped to published files
			LOG.debug("Mapping source files to published files");

			String sql = "SELECT id, service_id, system_id, file_path, exchange_id, description"
					+ " FROM source_file_mapping"
					+ " WHERE new_published_file_id IS NULL";
			PreparedStatement ps = connection.prepareStatement(sql);

			List<FileDesc> fileDescs = new ArrayList<>();

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int col = 1;

				FileDesc f = new FileDesc();
				f.id = rs.getInt(col++);
				f.serviceId = UUID.fromString(rs.getString(col++));
				f.systemId = UUID.fromString(rs.getString(col++));
				f.filePath = rs.getString(col++);
				f.exchangeId = UUID.fromString(rs.getString(col++));
				f.fileDesc = rs.getString(col++);

				fileDescs.add(f);
			}

			ps.close();
			entityManager.close();
			LOG.debug("Found " + fileDescs.size() + " files to map");

			List<FileDesc> batch = new ArrayList<>();

			for (int i = 0; i < fileDescs.size(); i++) {
				FileDesc f = fileDescs.get(i);
				Integer newFileAuditId = auditParser(f.serviceId, f.systemId, f.exchangeId, f.filePath, f.fileDesc);
				if (newFileAuditId == null) {
					continue;
				}
				f.newId = newFileAuditId;

				batch.add(f);
				if (batch.size() >= batchSize
						|| i + 1 >= fileDescs.size()) {

					entityManager = ConnectionManager.getPublisherTransformEntityManager(dummyServiceId);
					session = (SessionImpl) entityManager.getDelegate();
					connection = session.connection();

					sql = "UPDATE source_file_mapping"
							+ " SET new_published_file_id = ?"
							+ " WHERE id = ?";
					ps = connection.prepareStatement(sql);

					entityManager.getTransaction().begin();

					for (FileDesc toSave : batch) {

						int col = 1;
						ps.setInt(col++, toSave.newId);
						ps.setInt(col++, toSave.id);
						ps.addBatch();
					}

					ps.executeBatch();
					entityManager.getTransaction().commit();
					ps.close();
					entityManager.close();
				}

				if (i % 100 == 0) {
					LOG.debug("Audited " + i + " files");
				}
			}
			LOG.info("Finished Converting FHIR audit for " + publisherConfigName);
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}*/

	private static UUID findSuitableServiceIdForPublisherConfig(String publisherConfigName) throws Exception {
		ServiceDalI serviceDal = DalProvider.factoryServiceDal();
		for (Service s: serviceDal.getAll()) {
			if (s.getPublisherConfigName() != null
					&& s.getPublisherConfigName().equalsIgnoreCase(publisherConfigName)) {
				return s.getId();
			}
		}
		throw new Exception("Failed to find suitable service ID for publisher [" + publisherConfigName + "]");
	}

	/*private static void convertFhirAudits2(String publisherConfigName, String tempTable, int threads, int batchSize, boolean testMode) throws Exception {
		LOG.info("Converting FHIR audit for " + publisherConfigName);
		try {
			//find a suitable service ID
			UUID dummyServiceId = findSuitableServiceIdForPublisherConfig(publisherConfigName);

			ThreadPool threadPool = new ThreadPool(threads, 1000);
			int done = 0;

			while (true) {

				String sql = "SELECT c.resource_id, c.resource_type, c.created_at, m.version, m.mappings_json"
						+ " FROM " + tempTable + " c"
						+ " INNER JOIN resource_field_mappings m"
						+ " ON c.resource_id = m.resource_id"
						+ " AND c.resource_type = m.resource_type"
						+ " AND c.created_at = m.created_at"
						+ " WHERE c.done = false"
						+ " LIMIT " + batchSize;

				Map<ResourceWrapper, ResourceFieldMappingAudit> map = new HashMap<>();

				EntityManager entityManager = ConnectionManager.getPublisherTransformEntityManager(dummyServiceId);
				SessionImpl session = (SessionImpl) entityManager.getDelegate();
				Connection connection = session.connection();


				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					int col = 1;
					ResourceWrapper r = new ResourceWrapper();
					r.setResourceId(UUID.fromString(rs.getString(col++)));
					r.setResourceType(rs.getString(col++));
					r.setCreatedAt(new Date(rs.getTimestamp(col++).getTime()));
					r.setVersion(UUID.fromString(rs.getString(col++)));

					ResourceFieldMappingAudit audit = ResourceFieldMappingAudit.readFromJson(rs.getString(col++));

					map.put(r, audit);
				}
				ps.close();
				entityManager.close();

				boolean lastOne = map.size() < batchSize;

				for (ResourceWrapper r: map.keySet()) {
					ResourceFieldMappingAudit audit = map.get(r);

					ConvertFhirAuditCallable c = new ConvertFhirAuditCallable(testMode, dummyServiceId, audit, r);
					List<ThreadPoolError> errors = threadPool.submit(c);
					handleErrors(errors);
				}

				//now save everything

				List<ThreadPoolError> errors = threadPool.waitUntilEmpty();
				handleErrors(errors);

				done += map.size();

				if (!testMode) {

					entityManager = ConnectionManager.getPublisherTransformEntityManager(dummyServiceId);
					session = (SessionImpl) entityManager.getDelegate();
					connection = session.connection();

					//save all audits
					sql = "UPDATE resource_field_mappings"
							+ " SET mappings_json = ?"
							+ " WHERE resource_id = ?"
							+ " AND resource_type = ?"
							+ " AND created_at = ?"
							+ " AND version = ?";
					ps = connection.prepareStatement(sql);

					entityManager.getTransaction().begin();

					for (ResourceWrapper r : map.keySet()) {
						ResourceFieldMappingAudit audit = map.get(r);
						String auditJson = audit.writeToJson();

						int col = 1;
						ps.setString(col++, auditJson);
						ps.setString(col++, r.getResourceId().toString());
						ps.setString(col++, r.getResourceType());
						ps.setTimestamp(col++, new Timestamp(r.getCreatedAt().getTime()));
						ps.setString(col++, r.getVersion().toString());

						ps.addBatch();
					}

					ps.executeBatch();
					entityManager.getTransaction().commit();
					ps.close();
					entityManager.close();

					entityManager = ConnectionManager.getPublisherTransformEntityManager(dummyServiceId);
					session = (SessionImpl) entityManager.getDelegate();
					connection = session.connection();


					//mark temp table as done
					sql = "UPDATE " + tempTable
							+ " SET done = true"
							+ " WHERE done = false"
							+ " AND resource_id = ?"
							+ " AND resource_type = ?"
							+ " AND created_at = ?";
					ps = connection.prepareStatement(sql);

					entityManager.getTransaction().begin();

					for (ResourceWrapper r : map.keySet()) {

						int col = 1;
						ps.setString(col++, r.getResourceId().toString());
						ps.setString(col++, r.getResourceType());
						ps.setTimestamp(col++, new Timestamp(r.getCreatedAt().getTime()));

						ps.addBatch();
					}

					ps.executeBatch();
					entityManager.getTransaction().commit();
					ps.close();
					entityManager.close();
				}

				if (done % 1000 == 0) {
					LOG.info("Done " + done);
				}

				if (lastOne
						|| testMode) {
					break;
				}
			}

			LOG.info("Done " + done);
			LOG.info("Finished Converting FHIR audit for " + publisherConfigName);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
		if (errors == null || errors.isEmpty()) {
			return;
		}

		//if we've had multiple errors, just throw the first one, since they'll most-likely be the same
		ThreadPoolError first = errors.get(0);
		Throwable cause = first.getException();
		//the cause may be an Exception or Error so we need to explicitly
		//cast to the right type to throw it without changing the method signature
		if (cause instanceof Exception) {
			throw (Exception)cause;
		} else if (cause instanceof Error) {
			throw (Error)cause;
		}
	}*/

	static class ConvertFhirAuditCallable implements Callable {

		private Map<String, UUID> hmPublishers = null;

		private ResourceWrapper r;
		private ResourceFieldMappingAudit audit;
		private UUID dummyServiceId;
		private boolean testMode;

		public ConvertFhirAuditCallable(boolean testMode, UUID dummyServiceId, ResourceFieldMappingAudit audit, ResourceWrapper r) {
			this.testMode = testMode;
			this.dummyServiceId = dummyServiceId;
			this.audit = audit;
			this.r = r;
		}

		@Override
		public Object call() throws Exception {

			String auditJson = audit.writeToJson();

			List<ResourceFieldMappingAudit.ResourceFieldMappingAuditRow> auditRows = audit.getAudits();
			for (ResourceFieldMappingAudit.ResourceFieldMappingAuditRow auditRow: auditRows) {
				Long oldStyleAuditId = auditRow.getOldStyleAuditId();

				//got some records with a mix of old and new-style audits so skip any rows that are new-style
				if (oldStyleAuditId == null) {
					continue;
				}

				//need to work out if it's one of the audits where the record ID is potentially on a different server
				boolean isPotentiallyOnAnotherServer = false;
				String desiredFileName = null;

				for (ResourceFieldMappingAudit.ResourceFieldMappingAuditCol auditCol: auditRow.getCols()) {

					if (r.getResourceType().equals(ResourceType.MedicationOrder.toString())
							|| r.getResourceType().equals(ResourceType.MedicationStatement.toString())) {

						if (auditCol.getField().equals("medicationCodeableConcept.text")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "DrugCode";
						}

					} else if (r.getResourceType().equals(ResourceType.Observation.toString())
							|| r.getResourceType().equals(ResourceType.Condition.toString())
							|| r.getResourceType().equals(ResourceType.Procedure.toString())
							|| r.getResourceType().equals(ResourceType.DiagnosticReport.toString())) {

						if (auditCol.getField().equals("code.text")
								|| auditCol.getField().equals("component[1].code.text")
								|| auditCol.getField().equals("component[0].code.text")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "ClinicalCode";
						}

					} else if (r.getResourceType().equals(ResourceType.AllergyIntolerance.toString())) {

						if (auditCol.getField().equals("substance.text")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "ClinicalCode";
						}

					} else if (r.getResourceType().equals(ResourceType.FamilyMemberHistory.toString())) {

						if (auditCol.getField().equals("condition[0].code.text")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "ClinicalCode";
						}

					} else if (r.getResourceType().equals(ResourceType.Immunization.toString())) {

						if (auditCol.getField().equals("vaccineCode.text")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "ClinicalCode";
						}

					} else if (r.getResourceType().equals(ResourceType.DiagnosticOrder.toString())) {

						if (auditCol.getField().equals("item[0].code.text")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "ClinicalCode";
						}

					} else if (r.getResourceType().equals(ResourceType.ReferralRequest.toString())) {

						if (auditCol.getField().equals("serviceRequested[0].text")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "ClinicalCode";
						}

					} else if (r.getResourceType().equals(ResourceType.Specimen.toString())) {

						if (auditCol.getField().equals("type.text")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "ClinicalCode";
						}

					} else if (r.getResourceType().equals(ResourceType.Location.toString())) {

						if (auditCol.getField().equals("managingOrganization.reference")) {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "OrganisationLocation";
						} else {
							isPotentiallyOnAnotherServer = true;
							desiredFileName = "Location";
						}

					} else if (r.getResourceType().equals(ResourceType.Organization.toString())) {
						isPotentiallyOnAnotherServer = true;
						desiredFileName = "Organisation";

					} else if (r.getResourceType().equals(ResourceType.Practitioner.toString())) {
						isPotentiallyOnAnotherServer = true;
						desiredFileName = "UserInRole";

					}

					if (isPotentiallyOnAnotherServer) {
						break;
					}
				}

				List<Integer> newIds = null;

				if (isPotentiallyOnAnotherServer) {
					newIds = findNewAuditIdOnAnyServer(oldStyleAuditId, desiredFileName);

				} else {
					newIds = findNewAuditIdOnThisServer(oldStyleAuditId);
				}

				Integer newFileAuditId = newIds.get(0);
				Integer newRecordNum = newIds.get(1);

				auditRow.setOldStyleAuditId(null);
				auditRow.setFileId(newFileAuditId.intValue());
				auditRow.setRecord(newRecordNum.intValue());


			}

			if (testMode) {
				String newAuditJson = audit.writeToJson();
				String str = "Testing " + r.getResourceType() + " " + r.getResourceId() + " version " + r.getVersion() + " from " + r.getCreatedAt()
						+ "\nOld JSON:"
						+ "\n" + auditJson
						+ "\nNew JSON:"
						+ "\n" + newAuditJson;
				LOG.info(str);
			}

			return null;
		}


		private List<Integer> findNewAuditIdOnThisServer(Long oldStyleAuditId) throws Exception {

			EntityManager entityManager = ConnectionManager.getPublisherTransformEntityManager(dummyServiceId);
			SessionImpl session = (SessionImpl) entityManager.getDelegate();
			Connection connection = session.connection();

			//need to convert oldStyleID to a fileID and record number
			String sql = "select r.source_location, f.new_published_file_id"
					+ " from source_file_record r"
					+ " inner join source_file_mapping f"
					+ " on f.id = r.source_file_id"
					+ " where r.id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setLong(1, oldStyleAuditId.longValue());

			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				throw new Exception("Failed to find source record details for old style audit ID " + oldStyleAuditId + " in audit for " + r.getResourceType() + " " + r.getResourceId() + " from " + r.getCreatedAt());
			}

			int col = 1;
			String recordNumStr = rs.getString(col++);
			int newPublishedFileId = rs.getInt(col++);

			ps.close();
			entityManager.close();

			List<Integer> ret = new ArrayList<>();
			ret.add(new Integer(newPublishedFileId));
			ret.add(Integer.valueOf(recordNumStr));
			return ret;
		}


		private List<Integer> findNewAuditIdOnAnyServer(Long oldStyleAuditId, String desiredFileName) throws Exception {

			if (hmPublishers == null) {
				Map<String, UUID> map = new HashMap<>();

				List<String> publishers = new ArrayList<>();
				publishers.add("publisher_01");
				publishers.add("publisher_02");
				publishers.add("publisher_03");
				publishers.add("publisher_04");
				publishers.add("publisher_05");

				for (String publisher: publishers) {
					UUID serviceId = findSuitableServiceIdForPublisherConfig(publisher);
					map.put(publisher, serviceId);
				}

				hmPublishers = map;
			}

			Integer foundRecordNum = null;
			Integer foundPublishedFileId = null;
			String foundOnPublisher = null;

			for (String publisher: hmPublishers.keySet()) {
				UUID serviceId = hmPublishers.get(publisher);

				EntityManager entityManager = ConnectionManager.getPublisherTransformEntityManager(serviceId);
				SessionImpl session = (SessionImpl) entityManager.getDelegate();
				Connection connection = session.connection();

				//need to convert oldStyleID to a fileID and record number
				String sql = "select r.source_location, f.new_published_file_id"
						+ " from source_file_record r"
						+ " inner join source_file_mapping f"
						+ " on f.id = r.source_file_id"
						+ " where r.id = ?"
						+ " and f.file_path LIKE '%" + desiredFileName + "%'";
				PreparedStatement ps = connection.prepareStatement(sql);

				ps.setLong(1, oldStyleAuditId.longValue());

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {

					int col = 1;
					String recordNumStr = rs.getString(col++);
					int newPublishedFileId = rs.getInt(col++);

					ps.close();
					entityManager.close();

					if (foundPublishedFileId == null) {
						foundPublishedFileId = new Integer(newPublishedFileId);
						foundRecordNum = Integer.valueOf(recordNumStr);
						foundOnPublisher = publisher;

					} else {
						LOG.error("Old style audit = " + oldStyleAuditId);
						LOG.error("On " + foundOnPublisher + " found " + foundPublishedFileId + " published file ID and record number " + foundRecordNum);
						LOG.error("On " + publisher + " found " + newPublishedFileId + " published file ID and record number " + recordNumStr);
						throw new Exception("Found more than one matching file for old-style audit ID " + oldStyleAuditId + " and desired file name " + desiredFileName + " over all servers");
					}
				}
			}

			if (foundPublishedFileId == null) {
				throw new Exception("Failed to find published file ID and record number for old-style audit ID " + oldStyleAuditId + " and desired file name " + desiredFileName + " over all servers");
			}

			List<Integer> ret = new ArrayList<>();
			ret.add(foundPublishedFileId);
			ret.add(foundRecordNum);
			return ret;
		}
	}



	/*static class FileDesc {
		int id;
		UUID serviceId;
		UUID systemId;
		String filePath;
		UUID exchangeId;
		String fileDesc;
		int newId;
	}

	private static Integer auditParser(UUID serviceId, UUID systemId, UUID exchangeId, String filePath, String fileDesc) throws Exception {

		ParserI parser = createParser(serviceId, systemId, exchangeId, filePath, fileDesc);
		if (parser == null) {
			LOG.debug("No parser created for " + fileDesc + " " + filePath);
			return null;
		}

		Integer newId = parser.ensureFileAudited();
		if (newId == null) {
			throw new Exception("Null new ID for auditing file " + filePath);
		}

		return new Integer(newId);
	}

	private static ParserI createParser(UUID serviceId, UUID systemId, UUID exchangeId, String filePath, String fileDesc) throws Exception {

		if (fileDesc.startsWith("Vision ")) {

			if (fileDesc.equals("Vision organisations file")) {
				return new Practice(serviceId, systemId, exchangeId, null, filePath);
			} else if (fileDesc.equals("Vision staff file")) {
				return new Staff(serviceId, systemId, exchangeId, null, filePath);
			} else if (fileDesc.equals("Vision patient file")) {
				return new Patient(serviceId, systemId, exchangeId, null, filePath);
			} else if (fileDesc.equals("Vision encounter file")) {
				return new Encounter(serviceId, systemId, exchangeId, null, filePath);
			} else if (fileDesc.equals("Vision referrals file")) {
				return new Referral(serviceId, systemId, exchangeId, null, filePath);
			} else if (fileDesc.equals("Vision journal file")) {
				return new Journal(serviceId, systemId, exchangeId, null, filePath);
			} else {
				throw new Exception("Unknown vision file [" + fileDesc + "]");
			}
		}


		if (fileDesc.equals("Bespoke Emis registration status extract")
				|| fileDesc.equals("RegistrationStatus")) {
			String DATE_FORMAT = "dd/MM/yyyy";
			String TIME_FORMAT = "hh:mm:ss";
			CSVFormat CSV_FORMAT = CSVFormat.TDF
					.withHeader()
					.withEscape((Character)null)
					.withQuote((Character)null)
					.withQuoteMode(QuoteMode.MINIMAL); //ideally want Quote Mdde NONE, but validation in the library means we need to use this;

			List<String> possibleVersions = new ArrayList<>();
			possibleVersions.add(RegistrationStatus.VERSION_WITH_PROCESSING_ID);
			possibleVersions.add(RegistrationStatus.VERSION_WITHOUT_PROCESSING_ID);

			RegistrationStatus testParser = new RegistrationStatus(null, null, null, null, filePath, CSV_FORMAT, DATE_FORMAT, TIME_FORMAT);
			possibleVersions = testParser.testForValidVersions(possibleVersions);
			String version = possibleVersions.get(0);
			return new RegistrationStatus(serviceId, systemId, exchangeId, version, filePath, CSV_FORMAT, DATE_FORMAT, TIME_FORMAT);
		}

		if (fileDesc.equals("OriginalTerms")) {
			String DATE_FORMAT2 = "dd/MM/yyyy";
			String TIME_FORMAT2 = "hh:mm:ss";
			CSVFormat CSV_FORMAT2 = CSVFormat.TDF
					.withHeader()
					.withEscape((Character)null)
					.withQuote((Character)null)
					.withQuoteMode(QuoteMode.MINIMAL); //ideally want Quote Mdde NONE, but validation in the library means we need to use this;
			return new OriginalTerms(serviceId, systemId, exchangeId, null, filePath, CSV_FORMAT2, DATE_FORMAT2, TIME_FORMAT2);
		}

		if (filePath.contains("EMIS")) {

			if (fileDesc.equals("Emis appointments file")) {
				fileDesc = "Slot";
			} else if (fileDesc.equals("Emis appointments session file")) {
				fileDesc = "Session";
			} else if (fileDesc.equals("Emis clinical code reference file")) {
				fileDesc = "ClinicalCode";
			} else if (fileDesc.equals("Emis consultations file")) {
				fileDesc = "Consultation";
			} else if (fileDesc.equals("Emis diary file")) {
				fileDesc = "Diary";
			} else if (fileDesc.equals("Emis drug code reference file")) {
				fileDesc = "DrugCode";
			} else if (fileDesc.equals("Emis drug record file")) {
				fileDesc = "DrugRecord";
			} else if (fileDesc.equals("Emis issue records file")) {
				fileDesc = "IssueRecord";
			} else if (fileDesc.equals("Emis observations file")) {
				fileDesc = "Observation";
			} else if (fileDesc.equals("Emis organisation location file")) {
				fileDesc = "Location";
			} else if (fileDesc.equals("Emis organisation-location link file")) {
				fileDesc = "OrganisationLocation";
			} else if (fileDesc.equals("Emis organisations file")) {
				fileDesc = "Organisation";
			} else if (fileDesc.equals("Emis patient file")) {
				fileDesc = "Patient";
			} else if (fileDesc.equals("Emis problems file")) {
				fileDesc = "Problem";
			} else if (fileDesc.equals("Emis referrals file")) {
				fileDesc = "ObservationReferral";
			} else if (fileDesc.equals("Emis session-user link file")) {
				fileDesc = "SessionUser";
			} else if (fileDesc.equals("Emis sharing agreements file")) {
				fileDesc = "SharingOrganisation";
			} else if (fileDesc.equals("Emis staff file")) {
				fileDesc = "UserInRole";
			}

			String fileType = null;
			switch (fileDesc) {
				case "ClinicalCode":
					fileType = "Coding_ClinicalCode";
					break;
				case "Consultation":
					fileType = "CareRecord_Consultation";
					break;
				case "Diary":
					fileType = "CareRecord_Diary";
					break;
				case "DrugCode":
					fileType = "Coding_DrugCode";
					break;
				case "DrugRecord":
					fileType = "Prescribing_DrugRecord";
					break;
				case "IssueRecord":
					fileType = "Prescribing_IssueRecord";
					break;
				case "Location":
					fileType = "Admin_Location";
					break;
				case "Observation":
					fileType = "CareRecord_Observation";
					break;
				case "ObservationReferral":
					fileType = "CareRecord_ObservationReferral";
					break;
				case "Organisation":
					fileType = "Admin_Organisation";
					break;
				case "OrganisationLocation":
					fileType = "Admin_OrganisationLocation";
					break;
				case "Patient":
					fileType = "Admin_Patient";
					break;
				case "Problem":
					fileType = "CareRecord_Problem";
					break;
				case "Session":
					fileType = "Appointment_Session";
					break;
				case "SessionUser":
					fileType = "Appointment_SessionUser";
					break;
				case "SharingOrganisation":
					fileType = "Agreements_SharingOrganisation";
					break;
				case "Slot":
					fileType = "Appointment_Slot";
					break;
				case "UserInRole":
					fileType = "Admin_UserInRole";
					break;
				default:
					throw new Exception("Unknown file type [" + fileDesc + "]");
			}

			*//*String prefix = TransformConfig.instance().getSharedStoragePath();
			prefix += "/";
			if (!filePath.startsWith(prefix)) {
				throw new Exception("File path [" + filePath + "] doesn't start with " + prefix);
			}
			filePath = filePath.substring(prefix.length());*//*

			ExchangePayloadFile p = new ExchangePayloadFile();
			p.setPath(filePath);
			p.setType(fileType);

			List<ExchangePayloadFile> files = new ArrayList<>();
			files.add(p);

			String version = EmisCsvToFhirTransformer.determineVersion(files);

			Map<Class, AbstractCsvParser> parsers = new HashMap<>();

			EmisCsvToFhirTransformer.createParsers(serviceId, systemId, exchangeId, files, version, parsers);
			Iterator<AbstractCsvParser> it = parsers.values().iterator();

			return it.next();
		}

		if (filePath.contains("BARTSDW")) {
			return null;
		}

		throw new Exception("Unknown file desc [" + fileDesc + "] for " + filePath);
	}*/

	/*private static void moveS3ToAudit(int threads) {
		LOG.info("Moving S3 to Audit");
		try {
			//list S3 contents
			List<FileInfo> files = FileHelper.listFilesInSharedStorageWithInfo("s3://discoveryaudit/audit");
			LOG.debug("Found " + files.size() + " audits");

			int countPerThread = files.size() / threads;
			int pos = 0;

			AtomicInteger done = new AtomicInteger();
			List<Thread> threadList = new ArrayList<>();

			for (int i=0; i<threads; i++) {

				List<FileInfo> perThread = new ArrayList<>();

				int countThisThread = countPerThread;
				if (i+1 == threads) {
					countThisThread = files.size() - pos;
				}

				for (int j=0; j<countThisThread; j++) {
					FileInfo fileInfo = files.get(pos);
					pos ++;
					perThread.add(fileInfo);
				}

				MoveToS3Runnable r = new MoveToS3Runnable(perThread, done);
				Thread t = new Thread(r);
				threadList.add(t);
				t.start();
			}

			while (true) {
				Thread.sleep(5000);

				boolean allDone = true;
				for (Thread t: threadList) {
					if (t.getState() != Thread.State.TERMINATED) {
						//if (!t.isAlive()) {
						allDone = false;
						break;
					}
				}

				if (allDone) {
					break;
				}
			}

			LOG.debug("Finished with " + done.get() + " / " + files.size());

			LOG.info("Finished Moving S3 to Audit");
		} catch (Throwable t) {
			LOG.error("", t);
		}

	}*/

	/*private static void convertEmisGuids() {
		LOG.debug("Converting Emis Guid");
		try {
			Map<String, String> map = new HashMap<>();

			//this list of guids and dates is based off the live Emis extracts, giving the most recent bulk date for each organisation
			//only practices where the extract started before the move to AWS and where the extract was disabled and re-bulked need to be in here.
			//Practices disabled and re-bulked since the move to AWS are handled differently.
			map.put("{DD31E915-7076-46CF-99CD-8378AB588B69}", "20/07/2017");
			map.put("{87A8851C-3DA4-4BE0-869C-3BF6BA7C0612}", "15/10/2017");
			map.put("{612DCB3A-5BE6-4D50-909B-F0F20565F9FC}", "09/08/2017");
			map.put("{15667F8D-46A0-4A87-9FA8-0C56B157A0A9}", "05/05/2017");
			map.put("{3CFEFBF9-B856-4A40-A39A-4EB6FA39295E}", "31/01/2017");
			map.put("{3F481450-AD19-4793-B1F0-40D5C2C57EF7}", "04/11/2017");
			map.put("{83939542-20E4-47C5-9883-BF416294BB22}", "13/10/2017");
			map.put("{73AA7E3A-4331-4167-8711-FE07DDBF4657}", "15/10/2017");
			map.put("{3B703CCF-C527-4EC8-A802-00D3B1535DD0}", "01/02/2017");
			map.put("{ED442CA3-351F-43E4-88A2-2EEACE39A402}", "13/10/2017");
			map.put("{86537B5B-7CF3-4964-8906-7C10929FBC20}", "13/05/2017");
			map.put("{9A4518C4-82CE-4509-8039-1B5F49F9C1FA}", "12/08/2017");
			map.put("{16D7F8F9-4A35-44B1-8F1D-DD0162584684}", "11/07/2017");
			map.put("{D392C499-345C-499B-898C-93F2CB8CC1B9}", "15/10/2017");
			map.put("{5B87882A-0EE8-4233-93D0-D2F5F4F94040}", "15/03/2017");
			map.put("{CFE3B460-9058-47FB-BF1D-6BEC13A2257D}", "19/04/2017");
			map.put("{7B03E105-9275-47CC-8022-1469FE2D6AE4}", "20/04/2017");
			map.put("{94470227-587C-47D7-A51F-9893512424D8}", "27/04/2017");
			map.put("{734F4C99-6326-4CA4-A22C-632F0AC12FFC}", "17/10/2017");
			map.put("{03C5B4B4-1A70-45F8-922E-135C826D48E0}", "20/04/2017");
			map.put("{1BB17C3F-CE80-4261-AF6C-BE987E3A5772}", "09/05/2017");
			map.put("{16F6DD42-2140-4395-95D5-3FA50E252896}", "20/04/2017");
			map.put("{3B6FD632-3FFB-48E6-9775-287F6C486752}", "15/10/2017");
			map.put("{F987F7BD-E19C-46D2-A446-913489F1BB7A}", "05/02/2017");
			map.put("{BE7CC1DC-3CAB-4BB1-A5A2-B0C854C3B78E}", "06/07/2017");
			map.put("{303EFA4E-EC8F-4CBC-B629-960E4D799E0D}", "15/10/2017");
			map.put("{5EE8FD1F-F23A-4209-A1EE-556F9350C900}", "01/02/2017");
			map.put("{04F6C555-A298-45F1-AC5E-AC8EBD2BB720}", "17/10/2017");
			map.put("{67383254-F7F1-4847-9AA9-C7DCF32859B8}", "17/10/2017");
			map.put("{31272E4E-40E0-4103-ABDC-F40A7B75F278}", "19/10/2017");
			map.put("{09CA2E3B-7143-4999-9934-971F3F2E6D8C}", "15/10/2017");
			map.put("{0527BCE2-4315-47F2-86A1-2E9F3E50399B}", "15/10/2017");
			map.put("{16DD14B5-D1D5-4B0C-B886-59AC4DACDA7A}", "04/07/2017");
			map.put("{411D0A79-6913-473C-B486-C01F6430D8A6}", "21/09/2017");
			map.put("{0862FADA-594A-415E-B971-7A4312E0A58C}", "10/06/2017");
			map.put("{249C3F3C-24F0-44CE-97A9-B535982BD70C}", "15/10/2017");
			map.put("{5D7A1915-6E22-4B20-A8AE-4768C06D3BBF}", "28/09/2017"); //Barts community
			map.put("{131AE556-8B50-4C17-9D7D-A4B19F7B1FEA}", "15/10/2017"); //Aberfeldy practice F84698
			map.put("{C0D2D0DF-EF78-444D-9A6D-B9EDEF5EF350}", "13/10/2017");
			map.put("{F174B354-4156-4BCB-960F-35D0145075EA}", "01/02/2017");
			map.put("{38600D63-1DE0-4910-8ED6-A38DC28A9DAA}", "19/02/2018"); //THE SPITALFIELDS PRACTICE (CDB 16);F84081
			map.put("{B3ECA2DE-D926-4594-B0EA-CF2F28057CE1}", "19/10/2017");
			map.put("{18F7C28B-2A54-4F82-924B-38C60631FFFA}", "04/02/2018"); //Rowans Surgery (CDB 18174);H85035
			map.put("{16FB5EE8-5039-4068-BC42-1DB56DC2A530}", "08/06/2017");
			map.put("{4BA4A5AC-7B25-40B2-B0EA-135702A72F9D}", "15/10/2017");
			map.put("{01B8341F-BC8F-450E-8AFA-4CDA344A5009}", "15/10/2017");
			map.put("{E6FBEA1C-BDA2-40B7-A461-C262103F08D7}", "08/06/2017");
			map.put("{141C68EB-1BC8-4E99-A9D9-0E63A8944CA9}", "15/10/2017");
			map.put("{A3EA804D-E7EB-43EE-8F1F-E860F6337FF7}", "15/10/2017");
			map.put("{771B42CC-9C0C-46E2-8143-76F04AF91AD5}", "13/11/2017"); //cranwich road
			map.put("{16EA8D5C-C667-4818-B629-5D6F4300FEEF}", "11/05/2017");
			map.put("{29E51964-C94D-4CB4-894E-EB18E27DEFC1}", "15/10/2017");
			map.put("{3646CCA5-7FE4-4DFE-87CD-DA3CE1BA885D}", "27/09/2017");
			map.put("{3EC82820-702F-4218-853B-D3E5053646A8}", "05/05/2017");
			map.put("{37F3E676-B203-4329-97F8-2AF5BFEAEE5A}", "19/10/2017");
			map.put("{A0E3208B-95E9-4284-9B5A-D4D387CCC9F9}", "07/06/2017");
			map.put("{0BEAF1F0-9507-4AC2-8997-EC0BA1D0247E}", "19/10/2017");
			map.put("{071A50E7-1764-4210-94EF-6A4BF96CF753}", "21/02/2017");
			map.put("{0C1983D8-FB7D-4563-84D0-1F8F6933E786}", "20/07/2017");
			map.put("{871FEEB2-CE30-4603-B9A3-6FA6CC47B5D4}", "15/10/2017");
			map.put("{42906EBE-8628-486D-A52F-27B935C9937A}", "01/02/2017");
			map.put("{1AB7ABF3-2572-4D07-B719-CFB2FE3AAC80}", "15/10/2017");
			map.put("{E312A5B7-13E7-4E43-BE35-ED29F6216D3C}", "20/04/2017");
			map.put("{55E60891-8827-40CD-8011-B0223D5C8970}", "15/10/2017");
			map.put("{03A63F52-7FEE-4592-9B54-83CEBCF67B5D}", "26/04/2017");
			map.put("{DB39B649-B48D-4AC2-BAB1-AC807AABFAC4}", "15/10/2017");
			map.put("{0AF9B2AF-A0FB-40B0-BA05-743BA6845DB1}", "26/08/2017");
			map.put("{A7600092-319C-4213-92C2-738BEEFC1609}", "31/01/2017");
			map.put("{5A1AABA9-7E96-41E7-AF18-E02F4CF1DFB6}", "15/10/2017");
			map.put("{7D8CE31D-66AA-4D6A-9EFD-313646BD1D73}", "15/10/2017");
			map.put("{03EA4A79-B6F1-4524-9D15-992B47BCEC9A}", "15/10/2017");
			map.put("{4588C493-2EA3-429A-8428-E610AE6A6D76}", "28/09/2017"); //Barts community
			map.put("{B13F3CC9-C317-4E0D-9C57-C545E4A53CAF}", "15/10/2017");
			map.put("{463DA820-6EC4-48CB-B915-81B31AFBD121}", "13/10/2017");
			map.put("{16F0D65C-B2A8-4186-B4E7-BBAF4390EC55}", "13/10/2017");
			map.put("{0039EF15-2DCF-4F70-B371-014C807210FD}", "24/05/2017");
			map.put("{E132BF05-78D9-4E4B-B875-53237E76A0FA}", "19/10/2017");
			map.put("{3DFC2DA6-AD8C-4836-945D-A6F8DB22AA49}", "15/10/2017");
			map.put("{BCB43B1D-2857-4186-918B-460620F98F81}", "13/10/2017");
			map.put("{E134C74E-FA3E-4E14-A4BB-314EA3D3AC16}", "15/10/2017");
			map.put("{C0F40044-C2CA-4D1D-95D3-553B29992385}", "26/08/2017");
			map.put("{B174A018-538D-4065-838C-023A245B53DA}", "14/02/2017");
			map.put("{43380A69-AE7D-4ED7-B014-0708675D0C02}", "08/06/2017");
			map.put("{E503F0E0-FE56-4CEF-BAB5-0D25B834D9BD}", "13/10/2017");
			map.put("{08946F29-1A53-4AF2-814B-0B8758112F21}", "07/02/2018"); //NEWHAM MEDICAL CENTRE (CDB 3461);F84669
			map.put("{09857684-535C-4ED6-8007-F91F366611C6}", "19/10/2017");
			map.put("{C409A597-009A-4E11-B828-A595755DE0EA}", "17/10/2017");
			map.put("{58945A1C-2628-4595-8F8C-F75D93045949}", "15/10/2017");
			map.put("{16FF2874-20B0-4188-B1AF-69C97055AA60}", "17/10/2017");
			map.put("{2C91E9DA-3F92-464E-B6E6-61D3DE52E62F}", "15/10/2017");
			map.put("{16E7AD27-2AD9-43C0-A473-1F39DF93E981}", "10/06/2017");
			map.put("{A528478D-65DB-435C-9E98-F8BDB49C9279}", "20/04/2017");
			map.put("{A2BDB192-E79C-44C5-97A2-1FD4517C456F}", "21/08/2017");
			map.put("{73DFF193-E917-4DBC-B5CF-DD2797B29377}", "15/10/2017");
			map.put("{62825316-9107-4E2C-A22C-86211B4760DA}", "13/10/2017");
			map.put("{006E8A30-2A45-4DBE-91D7-1C53FADF38B1}", "28/01/2018"); //The Lawson Practice (CDB 4334);F84096
			map.put("{E32AA6A6-46B1-4198-AA13-058038AB8746}", "13/10/2017");
			map.put("{B51160F1-79E3-4BA7-AA3D-1112AB341146}", "30/09/2017");
			map.put("{234503E5-56B4-45A0-99DA-39854FBE78E9}", "01/02/2017");
			map.put("{7D1852DA-E264-4599-B9B4-8F40207F967D}", "09/10/2017");
			map.put("{44716213-7FEE-4247-A09E-7285BD6B69C6}", "13/10/2017");
			map.put("{19BCC870-2704-4D21-BA7B-56F2F472AF35}", "15/10/2017");
			map.put("{FEF842DA-FD7C-480F-945A-D097910A81EB}", "13/10/2017");
			map.put("{1C980E19-4A39-4ACD-BA8A-925D3E525765}", "13/10/2017");
			map.put("{AABDDC3A-93A4-4A87-9506-AAF52E74012B}", "07/02/2018"); //DR N DRIVER AND PARTNERS (CDB 4419);F84086
			map.put("{90C2959C-0C2D-43DC-A81B-4AD594C17999}", "20/04/2017");
			map.put("{1F1669CF-1BB0-47A7-8FBF-BE65651644C1}", "15/10/2017");
			map.put("{C1800BE8-4C1D-4340-B0F2-7ED208586ED3}", "15/10/2017");
			map.put("{55A94703-4582-46FB-808A-1990E9CBCB6F}", "19/02/2018"); //Stamford Hill Group Practice (CDB 56);F84013
			map.put("{D4996E62-268F-4759-83A6-7A68D0B38CEC}", "27/04/2017");
			map.put("{3C843BBA-C507-4A95-9934-1A85B977C7B8}", "01/02/2017");
			map.put("{2216253B-705D-4C46-ADB3-ED48493D6A39}", "03/02/2018"); //RIVERSIDE MEDICAL PRACTICE (CDB 14675);Y01962
			map.put("{00123F97-4557-44AD-81B5-D9902DD72EE9}", "28/04/2017");
			map.put("{E35D4D12-E7D2-484B-BFF6-4653B3FED228}", "15/10/2017");
			map.put("{6D8B4D28-838B-4915-A148-6FEC2CEBCE77}", "05/07/2017");
			map.put("{188D5B4D-4BF6-46E3-AF11-3AD32C68D251}", "19/10/2017");
			map.put("{16F7DDE1-3763-4D3A-A58D-F12F967718CF}", "02/11/2017");
			map.put("{03148933-6E1C-4A8A-A6D2-A3D488E14DDD}", "30/12/2017");
			map.put("{16DE1A3C-875B-4AB2-B227-8A42604E029C}", "05/11/2017");
			map.put("{D628D1BC-D02E-4101-B8CD-5B3DB2D06FC1}", "05/05/2017");
			map.put("{1EA6259A-6A49-46DB-991D-D604675F87E2}", "15/10/2017");
			map.put("{817F9B46-AEE0-45D5-95E3-989F75C4844E}", "20/04/2017");
			map.put("{1C422471-F52A-4C30-8D23-140BEB7AAEFC}", "15/08/2017");
			map.put("{A6467E73-0F15-49D6-AFAB-4DFB487E7963}", "10/05/2017");
			map.put("{CC7D1781-1B85-4AD6-A5DD-9AD5E092E8DB}", "13/10/2017");
			map.put("{167CD5C8-148F-4D78-8997-3B22EC0AF6B6}", "13/10/2017");
			map.put("{9DD5D2CE-2585-49D8-AF04-2CB1BD137594}", "15/10/2017");
			map.put("{D6696BB5-DE69-49D1-BC5E-C56799E42640}", "07/02/2018"); //BOLEYN MEDICAL CENTRE (CDB 4841);F84050
			map.put("{169375A9-C3AB-4C5E-82B0-DFF7656AD1FA}", "20/04/2017");
			map.put("{0A8ECFDE-95EE-4811-BC05-668D49F5C799}", "19/11/2017");
			map.put("{79C898A1-BB92-48F9-B0C3-6725370132B5}", "20/10/2017");
			map.put("{472AC9BA-AFFE-4E81-81CA-40DD8389784D}", "27/04/2017");
			map.put("{00121CB7-76A6-4D57-8260-E9CA62FFCD77}", "13/10/2017");
			map.put("{0FCBA0A7-7CAB-4E75-AC81-5041CD869CA1}", "15/10/2017");
			map.put("{00A9C32D-2BB2-4A20-842A-381B3F2031C0}", "19/10/2017");
			map.put("{26597C5A-3E29-4960-BE11-AC75D0430615}", "03/05/2017");
			map.put("{D945FEF7-F5EF-422B-AB35-6937F9792B54}", "15/10/2017");
			map.put("{16D685C6-130A-4B19-BCA9-90AC7DC16346}", "08/07/2017");
			map.put("{F09E9CEF-2615-4C9D-AA3D-79E0AB10D0B3}", "13/10/2017");
			map.put("{CD7EF748-DB88-49CF-AA6E-24F65029391F}", "15/10/2017");
			map.put("{B22018CF-2B52-4A1A-9F6A-CEA13276DB2E}", "19/10/2017");
			map.put("{0DF8CFC7-5DE6-4DDB-846A-7F28A2740A00}", "02/12/2017");
			map.put("{50F439E5-DB18-43A0-9F25-825957013A07}", "11/01/2018"); //DR PI ABIOLA (CDB 5681);F84631
			map.put("{00A3BA25-21C6-42DE-82AA-55FF0D85A6C3}", "31/10/2018"); //MARKET STREET HEALTH GROUP (CDB 381);F84004
			map.put("{77B59D29-0FD9-4737-964F-5DBA49D94AB6}", "31/10/2018"); //Star Lane Medical Centre (CDB 40);F84017
			map.put("{91239362-A105-4DEA-8E8E-239C3BCEDFD2}", "11/01/2018"); //BEECHWOOD MEDICAL CENTRE (CDB 5661);F84038
			map.put("{53A113F5-6E3B-410F-A473-53E38A79335B}", "01/06/2018"); //ELFT Community RWKGY CDB 25362
			map.put("{164BE8EC-E2D5-40DE-A5FC-25E058A5C47E}", "17/10/2018"); //Haiderian Medical Centre F82002
			map.put("{164CE1B0-F7B3-44AF-B1E4-3DA6C64DEA4C}", "26/11/2018"); //THE GREEN WOOD PRACTICE F82007
			map.put("{A30A4BB7-B17B-11D9-AD5F-00D0B77FCBFC}", "26/11/2018"); //Tulasi Medical Practice F82660

			LOG.debug("Starting with map size " + map.size());

			Map<String, String> hmGuidToOdsMap = new HashMap<>();

			UUID systemId = UUID.fromString("991a9068-01d3-4ff2-86ed-249bd0541fb3");

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ExchangeDalI exchangeDalI = DalProvider.factoryExchangeDal();

			List<Service> services = serviceDal.getAll();
			for (Service service: services) {
				UUID serviceId = service.getId();
				String ods = service.getLocalId();
				String orgGuid = null;

				List<Exchange> exchanges = exchangeDalI.getExchangesByService(serviceId, systemId, 5);
				for (Exchange exchange: exchanges) {
					String exchangeBody = exchange.getBody();
					List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);
					if (!files.isEmpty()) {
						ExchangePayloadFile first = files.get(0);
						String path = first.getPath();
						if (path.indexOf("EMIS_CUSTOM") > -1) {
							continue;
						}
						File f = new File(path);
						f = f.getParentFile(); //org GUID
						orgGuid = f.getName();
						break;
					}
				}

				if (orgGuid == null) {
					LOG.error("Failed to find OrgGuid for " + service.getName() + " " + ods);
				} else {
					hmGuidToOdsMap.put(orgGuid, ods);
				}
			}

			//create new code
			for (String orgGuid: map.keySet()) {
				String dateStr = map.get(orgGuid);
				String odsCode = hmGuidToOdsMap.get(orgGuid);
				if (Strings.isNullOrEmpty(odsCode)) {
					LOG.error("Missing ODS code for " + orgGuid);
				} else {
					System.out.println("map.put(\"" + odsCode + "\", \"" + dateStr + "\");");
				}
			}

			LOG.debug("Finished Converting Emis Guid");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void testS3VsMySql(UUID serviceUuid, int count, int sqlBatchSize, String bucketName) {
		LOG.debug("Testing S3 vs MySQL for service " + serviceUuid);
		try {
			//retrieve some audit JSON from the DB
			EntityManager entityManager = ConnectionManager.getPublisherTransformEntityManager(serviceUuid);
			SessionImpl session = (SessionImpl) entityManager.getDelegate();
			Connection connection = session.connection();

			String sql = "select resource_id, resource_type, version, mappings_json"
					+ " from resource_field_mappings"
					+ " where mappings_json != '[]'";
			if (count > -1) {
				sql += "limit " + count + ";";
			}

			Statement statement = connection.createStatement();
			statement.setFetchSize(1000);
			ResultSet rs = statement.executeQuery(sql);

			List<ResourceFieldMapping> list = new ArrayList<>();

			while (rs.next()) {
				int col = 1;
				String resourceId = rs.getString(col++);
				String resourceType = rs.getString(col++);
				String version = rs.getString(col++);
				String json = rs.getString(col++);

				ResourceFieldMapping obj = new ResourceFieldMapping();
				obj.setResourceId(UUID.fromString(resourceId));
				obj.setResourceType(resourceType);
				obj.setVersion(UUID.fromString(version));
				obj.setResourceField(json);
				list.add(obj);
			}

			rs.close();
			statement.close();
			entityManager.close();

			int done = 0;

			//test writing to S3
			long s3Start = System.currentTimeMillis();
			LOG.debug("Doing S3 test");

			for (int i=0; i<list.size(); i++) {
				ResourceFieldMapping mapping = list.get(i);

				String entryName = mapping.getVersion().toString() + ".json";
				String keyName = "auditTest/" + serviceUuid + "/" + mapping.getResourceType() + "/" + mapping.getResourceId() + "/" + mapping.getVersion() + ".zip";
				String jsonStr = mapping.getResourceField();

				//may as well zip the data, since it will compress well
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(baos);

				zos.putNextEntry(new ZipEntry(entryName));
				zos.write(jsonStr.getBytes());
				zos.flush();
				zos.close();

				byte[] bytes = baos.toByteArray();
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

				//ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
				DefaultAWSCredentialsProviderChain credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();

				AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder
						.standard()
						.withCredentials(credentialsProvider)
						.withRegion(Regions.EU_WEST_2);

				AmazonS3 s3Client = clientBuilder.build();

				ObjectMetadata objectMetadata = new ObjectMetadata();
				objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
				objectMetadata.setContentLength(bytes.length);

				PutObjectRequest putRequest = new PutObjectRequest(bucketName, keyName, byteArrayInputStream, objectMetadata);
				s3Client.putObject(putRequest);

				done ++;
				if (done % 1000 == 0) {
					LOG.debug("Done " + done + " / " + list.size());
				}
			}

			long s3End = System.currentTimeMillis();
			LOG.debug("S3 took " + (s3End - s3Start) + " ms");

			//test inserting into a DB
			long sqlStart = System.currentTimeMillis();
			LOG.debug("Doing SQL test");

			sql = "insert into drewtest.json_speed_test (resource_id, resource_type, created_at, version, mappings_json) values (?, ?, ?, ?, ?)";

			entityManager = ConnectionManager.getPublisherTransformEntityManager(serviceUuid);
			session = (SessionImpl) entityManager.getDelegate();
			connection = session.connection();
			PreparedStatement ps = connection.prepareStatement(sql);
			entityManager.getTransaction().begin();

			done = 0;

			int currentBatchSize = 0;
			for (int i=0; i<list.size(); i++) {
				ResourceFieldMapping mapping = list.get(i);

				int col = 1;
				ps.setString(col++, mapping.getResourceId().toString());
				ps.setString(col++, mapping.getResourceType());
				ps.setDate(col++, new java.sql.Date(System.currentTimeMillis()));
				ps.setString(col++, mapping.getVersion().toString());
				ps.setString(col++, mapping.getResourceField());

				ps.addBatch();
				currentBatchSize ++;

				if (currentBatchSize >= sqlBatchSize
						|| i+1 == list.size()) {

					ps.executeBatch();

					entityManager.getTransaction().commit();

					//mirror what would happen normally
					ps.close();

					entityManager.close();

					if (i+1 < list.size()) {

						entityManager = ConnectionManager.getPublisherTransformEntityManager(serviceUuid);
						session = (SessionImpl) entityManager.getDelegate();
						connection = session.connection();

						ps = connection.prepareStatement(sql);
						entityManager.getTransaction().begin();
					}
				}

				done ++;
				if (done % 1000 == 0) {
					LOG.debug("Done " + done + " / " + list.size());
				}
			}

			long sqlEnd = System.currentTimeMillis();
			LOG.debug("SQL took " + (sqlEnd - sqlStart) + " ms");



			LOG.debug("Finished Testing S3 vs MySQL for service " + serviceUuid);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void loadEmisData(String serviceId, String systemId, String dbUrl, String dbUsername, String dbPassword, String onlyThisFileType) {
		LOG.debug("Loading Emis data from into " + dbUrl);
		try {
			//hash file type of every file
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(UUID.fromString(serviceId), UUID.fromString(systemId), Integer.MAX_VALUE);

			//open connection
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

			SimpleDateFormat sdfStart = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = sdfStart.parse("2000-01-01");

			for (int i = exchanges.size() - 1; i >= 0; i--) {
				Exchange exchange = exchanges.get(i);
				String exchangeBody = exchange.getBody();
				List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

				if (files.isEmpty()) {
					continue;
				}

				for (ExchangePayloadFile file : files) {
					String type = file.getType();
					String path = file.getPath();

					//if only doing a specific file type, skip all others
					if (onlyThisFileType != null
							&& !type.equals(onlyThisFileType)) {
						continue;
					}

					String name = FilenameUtils.getBaseName(path);
					String[] toks = name.split("_");
					if (toks.length != 5) {
						throw new TransformException("Failed to find extract date in filename " + path);
					}
					String dateStr = toks[3];
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					Date extractDate = sdf.parse(dateStr);

					boolean processFile = false;

					if (type.equalsIgnoreCase("OriginalTerms")
							|| type.equalsIgnoreCase("RegistrationStatus")) {
						//can't process these custom files in this routine

					} else if (type.equalsIgnoreCase("Coding_ClinicalCode")
							|| type.equalsIgnoreCase("Coding_DrugCode")) {
						processFile = true;

					} else {

						if (!extractDate.before(startDate)) {
							processFile = true;
						}
					}

					if (processFile) {
						loadEmisDataFromFile(conn, path, type, extractDate);
					}
				}
			}

			conn.close();

			LOG.debug("Finished Emis data from into " + dbUrl);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static ParserI createParserForEmisFileType(String fileType, String filePath) {

		String[] toks = fileType.split("_");

		String domain = toks[0];
		String name = toks[1];

		String first = domain.substring(0, 1);
		String last = domain.substring(1);
		domain = first.toLowerCase() + last;

		try {
			String clsName = "org.endeavourhealth.transform.emis.csv.schema." + domain + "." + name;
			Class cls = Class.forName(clsName);

			//now construct an instance of the parser for the file we've found
			Constructor<AbstractCsvParser> constructor = cls.getConstructor(UUID.class, UUID.class, UUID.class, String.class, String.class);
			return constructor.newInstance(null, null, null, EmisCsvToFhirTransformer.VERSION_5_4, filePath);

		} catch (Exception ex) {
			LOG.error("No parser for file type [" + fileType + "]");
			LOG.error("", ex);
			return null;
		}
	}

	private static void loadEmisDataFromFile(Connection conn, String filePath, String fileType, Date extractDate) throws Exception {
		LOG.debug("Loading " + fileType + ": " + filePath);

		String fileName = FilenameUtils.getName(filePath);

		ParserI parser = createParserForEmisFileType(fileType, filePath);
		if (parser == null) {
			return;
		}

		String table = fileType.replace(" ", "_");

		//check table is there
		String sql = "SELECT 1 FROM information_schema.tables WHERE table_schema = database() AND table_name = '" + table + "' LIMIT 1";
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(sql);
		boolean tableExists = rs.next();
		rs.close();
		statement.close();

		if (!tableExists) {
			LOG.error("No table exists for " + table);
			return;
		}

		//create insert statement
		sql = "INSERT INTO `" + table + "` (";
		sql += "file_name, extract_date";
		List<String> cols = parser.getColumnHeaders();
		for (String col : cols) {
			sql += ", ";
			sql += col.replace(" ", "_").replace("#", "").replace("/", "");
		}
		sql += ") VALUES (";
		sql += "?, ?";
		for (String col : cols) {
			sql += ", ";
			sql += "?";
		}
		sql += ")";
		PreparedStatement ps = conn.prepareStatement(sql);

		List<String> currentBatchStrs = new ArrayList<>();

		//load table
		try {
			int done = 0;
			int currentBatchSize = 0;
			while (parser.nextRecord()) {

				int col = 1;

				//file name is always first
				ps.setString(col++, fileName);
				ps.setDate(col++, new java.sql.Date(extractDate.getTime()));

				for (String colName : cols) {
					CsvCell cell = parser.getCell(colName);
					if (cell == null) {
						ps.setNull(col++, Types.VARCHAR);
					} else {
						ps.setString(col++, cell.getString());
					}
				}

				ps.addBatch();
				currentBatchSize++;
				currentBatchStrs.add((ps.toString())); //for error handling

				if (currentBatchSize >= 5) {
					ps.executeBatch();
					currentBatchSize = 0;
					currentBatchStrs.clear();
				}

				done++;
				if (done % 5000 == 0) {
					LOG.debug("Done " + done);
				}
			}

			if (currentBatchSize >= 0) {
				ps.executeBatch();
			}

			ps.close();
		} catch (Throwable t) {
			LOG.error("Failed on batch with statements:");
			for (String currentBatchStr : currentBatchStrs) {
				LOG.error(currentBatchStr);
			}
			throw t;
		}

		LOG.debug("Finished " + fileType + ": " + filePath);
	}

	private static void createBartsDataTables() {
		LOG.debug("Creating Barts data tables");
		try {
			List<String> fileTypes = new ArrayList<>();
			fileTypes.add("AEATT");
			fileTypes.add("Birth");
			//fileTypes.add("BulkDiagnosis");
			//fileTypes.add("BulkProblem");
			//fileTypes.add("BulkProcedure");
			fileTypes.add("CLEVE");
			fileTypes.add("CVREF");
			fileTypes.add("DIAGN");
			fileTypes.add("Diagnosis");
			fileTypes.add("ENCINF");
			fileTypes.add("ENCNT");
			fileTypes.add("FamilyHistory");
			fileTypes.add("IPEPI");
			fileTypes.add("IPWDS");
			fileTypes.add("LOREF");
			fileTypes.add("NOMREF");
			fileTypes.add("OPATT");
			fileTypes.add("ORDER");
			fileTypes.add("ORGREF");
			fileTypes.add("PPADD");
			fileTypes.add("PPAGP");
			fileTypes.add("PPALI");
			fileTypes.add("PPATI");
			fileTypes.add("PPINF");
			fileTypes.add("PPNAM");
			fileTypes.add("PPPHO");
			fileTypes.add("PPREL");
			fileTypes.add("Pregnancy");
			fileTypes.add("Problem");
			fileTypes.add("PROCE");
			fileTypes.add("Procedure");
			fileTypes.add("PRSNLREF");
			fileTypes.add("SusEmergency");
			fileTypes.add("SusInpatient");
			fileTypes.add("SusOutpatient");
			fileTypes.add("EventCode");
			fileTypes.add("EventSetCanon");
			fileTypes.add("EventSet");
			fileTypes.add("EventSetExplode");
			fileTypes.add("BlobContent");
			fileTypes.add("SusInpatientTail");
			fileTypes.add("SusOutpatientTail");
			fileTypes.add("SusEmergencyTail");
			fileTypes.add("AEINV");
			fileTypes.add("AETRE");
			fileTypes.add("OPREF");
			fileTypes.add("STATREF");
			fileTypes.add("RTTPE");
			fileTypes.add("PPATH");
			fileTypes.add("DOCRP");
			fileTypes.add("SCHAC");
			fileTypes.add("EALEN");
			fileTypes.add("DELIV");
			fileTypes.add("EALOF");
			fileTypes.add("SusEmergencyCareDataSet");
			fileTypes.add("SusEmergencyCareDataSetTail");


			for (String fileType : fileTypes) {
				createBartsDataTable(fileType);
			}

			LOG.debug("Finished Creating Barts data tables");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void createBartsDataTable(String fileType) throws Exception {

		ParserI parser = null;
		try {
			String clsName = "org.endeavourhealth.transform.barts.schema." + fileType;
			Class cls = Class.forName(clsName);

			//now construct an instance of the parser for the file we've found
			Constructor<AbstractCsvParser> constructor = cls.getConstructor(UUID.class, UUID.class, UUID.class, String.class, String.class);
			parser = constructor.newInstance(null, null, null, null, null);

		} catch (ClassNotFoundException cnfe) {
			System.out.println("-- No parser for file type [" + fileType + "]");
			return;
		}

		System.out.println("-- " + fileType);

		String table = fileType.replace(" ", "_");

		String dropSql = "DROP TABLE IF EXISTS `" + table + "`;";
		System.out.println(dropSql);

		String sql = "CREATE TABLE `" + table + "` (";

		sql += "file_name varchar(100)";

		if (parser instanceof AbstractFixedParser) {

			AbstractFixedParser fixedParser = (AbstractFixedParser) parser;
			List<FixedParserField> fields = fixedParser.getFieldList();

			for (FixedParserField field : fields) {
				String col = field.getName();
				int len = field.getFieldlength();
				sql += ", ";
				sql += col.replace(" ", "_").replace("#", "").replace("/", "");
				sql += " varchar(";
				sql += len;
				sql += ")";
			}

		} else {

			List<String> cols = parser.getColumnHeaders();
			for (String col : cols) {
				sql += ", ";
				sql += col.replace(" ", "_").replace("#", "").replace("/", "");

				if (col.equals("BLOB_CONTENTS")
						|| col.equals("VALUE_LONG_TXT")
						|| col.equals("COMMENT_TXT")
						|| col.equals("NONPREG_REL_PROBLM_SCT_CD")
						|| col.equals("ORDER_COMMENTS_TXT")) {

					sql += " mediumtext";

				} else if (col.indexOf("Date") > -1
						|| col.indexOf("Time") > -1) {
					sql += " varchar(10)";

				} else {
					sql += " varchar(255)";
				}
			}
		}

		sql += ");";
		/*LOG.debug("-- fileType");
		LOG.debug(sql);*/
		System.out.println(sql);

	}

	private static void loadBartsData(String serviceId, String systemId, String dbUrl, String dbUsername, String dbPassword, String startDateStr, String onlyThisFileType) {
		LOG.debug("Loading Barts data from into " + dbUrl);
		try {
			//hash file type of every file
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			List<Exchange> exchanges = exchangeDal.getExchangesByService(UUID.fromString(serviceId), UUID.fromString(systemId), Integer.MAX_VALUE);

			//open connection
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = sdf.parse(startDateStr);

			for (int i = exchanges.size() - 1; i >= 0; i--) {
				Exchange exchange = exchanges.get(i);
				String exchangeBody = exchange.getBody();
				List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

				if (files.isEmpty()) {
					continue;
				}

				for (ExchangePayloadFile file : files) {
					String type = file.getType();
					String path = file.getPath();

					//if only doing a specific file type, skip all others
					if (onlyThisFileType != null
							&& !type.equals(onlyThisFileType)) {
						continue;
					}

					boolean processFile = false;
					if (type.equalsIgnoreCase("CVREF")
							|| type.equalsIgnoreCase("LOREF")
							|| type.equalsIgnoreCase("ORGREF")
							|| type.equalsIgnoreCase("PRSNLREF")
							|| type.equalsIgnoreCase("NOMREF")) {
						processFile = true;

					} else {

						File f = new File(path);
						File parentFile = f.getParentFile();
						String parentDir = parentFile.getName();
						Date extractDate = sdf.parse(parentDir);
						if (!extractDate.before(startDate)) {
							processFile = true;
						}
						/*if (!extractDate.before(startDate)
								&& !extractDate.after(endDate)) {
							processFile = true;
						}*/
					}

					if (processFile) {
						loadBartsDataFromFile(conn, path, type);
					}
				}
			}

			conn.close();

			LOG.debug("Finished Loading Barts data from into " + dbUrl);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void loadBartsDataFromFile(Connection conn, String filePath, String fileType) throws Exception {
		LOG.debug("Loading " + fileType + ": " + filePath);

		String fileName = FilenameUtils.getName(filePath);

		ParserI parser = null;
		try {
			String clsName = "org.endeavourhealth.transform.barts.schema." + fileType;
			Class cls = Class.forName(clsName);

			//now construct an instance of the parser for the file we've found
			Constructor<AbstractCsvParser> constructor = cls.getConstructor(UUID.class, UUID.class, UUID.class, String.class, String.class);
			parser = constructor.newInstance(null, null, null, null, filePath);

		} catch (ClassNotFoundException cnfe) {
			LOG.error("No parser for file type [" + fileType + "]");
			return;
		}

		String table = fileType.replace(" ", "_");

		//check table is there
		String sql = "SELECT 1 FROM information_schema.tables WHERE table_schema = database() AND table_name = '" + table + "' LIMIT 1";
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(sql);
		boolean tableExists = rs.next();
		rs.close();
		statement.close();

		if (!tableExists) {
			LOG.error("No table exists for " + table);
			return;
		}

		//create insert statement
		sql = "INSERT INTO `" + table + "` (";
		sql += "file_name";
		List<String> cols = parser.getColumnHeaders();
		for (String col : cols) {
			sql += ", ";
			sql += col.replace(" ", "_").replace("#", "").replace("/", "");
		}
		sql += ") VALUES (";
		sql += "?";
		for (String col : cols) {
			sql += ", ";
			sql += "?";
		}
		sql += ")";
		PreparedStatement ps = conn.prepareStatement(sql);

		List<String> currentBatchStrs = new ArrayList<>();

		//load table
		try {
			int done = 0;
			int currentBatchSize = 0;
			while (parser.nextRecord()) {

				int col = 1;

				//file name is always first
				ps.setString(col++, fileName);

				for (String colName : cols) {
					CsvCell cell = parser.getCell(colName);
					if (cell == null) {
						ps.setNull(col++, Types.VARCHAR);
					} else {
						ps.setString(col++, cell.getString());
					}
				}

				ps.addBatch();
				currentBatchSize++;
				currentBatchStrs.add((ps.toString())); //for error handling

				if (currentBatchSize >= 5) {
					ps.executeBatch();
					currentBatchSize = 0;
					currentBatchStrs.clear();
				}

				done++;
				if (done % 5000 == 0) {
					LOG.debug("Done " + done);
				}
			}

			if (currentBatchSize >= 0) {
				ps.executeBatch();
			}

			ps.close();
		} catch (Throwable t) {
			LOG.error("Failed on batch with statements:");
			for (String currentBatchStr : currentBatchStrs) {
				LOG.error(currentBatchStr);
			}
			throw t;
		}

		LOG.debug("Finished " + fileType + ": " + filePath);
	}


	/*private static void fixPseudoIds(String subscriberConfig, int threads) {
		LOG.debug("Fixing Pseudo IDs for " + subscriberConfig);
		try {

			//update psuedo ID on patient table
			//update psuedo ID on person table
			//update pseudo ID on subscriber_transform mapping table

			JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfig, "db_subscriber");

			JsonNode saltNode = config.get("pseudonymisation");

			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(saltNode.toString(), Object.class);
			String linkDistributors = mapper.writeValueAsString(json);
			LinkDistributorConfig salt = ObjectMapperPool.getInstance().readValue(linkDistributors, LinkDistributorConfig.class);

			LinkDistributorConfig[] arr = null;
			JsonNode linkDistributorsNode = config.get("linkedDistributors");
			if (linkDistributorsNode != null) {
				json = mapper.readValue(linkDistributorsNode.toString(), Object.class);
				linkDistributors = mapper.writeValueAsString(json);
				arr = ObjectMapperPool.getInstance().readValue(linkDistributors, LinkDistributorConfig[].class);
			}


			Connection subscriberConnection = EnterpriseFiler.openConnection(config);


			List<Long> patientIds = new ArrayList<>();
			Map<Long, Long> hmOrgIds = new HashMap<>();
			Map<Long, Long> hmPersonIds = new HashMap<>();

			String sql = "SELECT id, organization_id, person_id FROM patient";
			Statement statement = subscriberConnection.createStatement();
			statement.setFetchSize(10000);
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				long patientId = rs.getLong(1);
				long orgId = rs.getLong(2);
				long personId = rs.getLong(3);

				patientIds.add(new Long(patientId));
				hmOrgIds.put(new Long(patientId), new Long(orgId));
				hmPersonIds.put(new Long(patientId), new Long(personId));
			}
			rs.close();
			subscriberConnection.close();

			LOG.debug("Found " + patientIds.size() + " patients");

			AtomicInteger done = new AtomicInteger();
			int pos = 0;

			List<Thread> threadList = new ArrayList<>();
			for (int i=0; i<threads; i++) {

				List<Long> patientSubset = new ArrayList<>();

				int count = patientIds.size() / threads;
				if (i+1 == threads) {
					count = patientIds.size() - pos;
				}

				for (int j=0; j<count; j++) {
					Long patientId = patientIds.get(pos);
					patientSubset.add(patientId);
					pos ++;
				}

				FixPseudoIdRunnable runnable = new FixPseudoIdRunnable(subscriberConfig, patientSubset, hmOrgIds, hmPersonIds, done);

				Thread t = new Thread(runnable);
				t.start();

				threadList.add(t);
			}

			while (true) {
				Thread.sleep(5000);

				boolean allDone = true;
				for (Thread t: threadList) {
					if (t.getState() != Thread.State.TERMINATED) {
					//if (!t.isAlive()) {
						allDone = false;
						break;
					}
				}

				if (allDone) {
					break;
				}
			}

			LOG.debug("Finished Fixing Pseudo IDs for " + subscriberConfig);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}



	static class FixPseudoIdRunnable implements Runnable {

		private String subscriberConfig = null;
		private List<Long> patientIds = null;
		private Map<Long, Long> hmOrgIds = null;
		private Map<Long, Long> hmPersonIds = null;
		private AtomicInteger done = null;

		public FixPseudoIdRunnable(String subscriberConfig, List<Long> patientIds, Map<Long, Long> hmOrgIds, Map<Long, Long> hmPersonIds, AtomicInteger done) {
			this.subscriberConfig = subscriberConfig;
			this.patientIds = patientIds;
			this.hmOrgIds = hmOrgIds;
			this.hmPersonIds = hmPersonIds;
			this.done = done;
		}

		@Override
		public void run() {
			try {
				doRun();
			} catch (Throwable t) {
				LOG.error("", t);
			}
		}

		private void doRun() throws Exception {
			JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfig, "db_subscriber");

			Connection subscriberConnection = EnterpriseFiler.openConnection(config);
			Statement statement = subscriberConnection.createStatement();

			JsonNode saltNode = config.get("pseudonymisation");

			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(saltNode.toString(), Object.class);
			String linkDistributors = mapper.writeValueAsString(json);
			LinkDistributorConfig salt = ObjectMapperPool.getInstance().readValue(linkDistributors, LinkDistributorConfig.class);

			LinkDistributorConfig[] arr = null;
			JsonNode linkDistributorsNode = config.get("linkedDistributors");
			if (linkDistributorsNode != null) {
				json = mapper.readValue(linkDistributorsNode.toString(), Object.class);
				linkDistributors = mapper.writeValueAsString(json);
				arr = ObjectMapperPool.getInstance().readValue(linkDistributors, LinkDistributorConfig[].class);
			}

			//PseudoIdDalI pseudoIdDal = DalProvider.factoryPseudoIdDal(subscriberConfig);
			ResourceDalI resourceDal = DalProvider.factoryResourceDal();

			EntityManager entityManager = ConnectionManager.getSubscriberTransformEntityManager(subscriberConfig);
			SessionImpl session = (SessionImpl) entityManager.getDelegate();
			Connection subscriberTransformConnection = session.connection();

			Statement subscriberTransformStatement = subscriberTransformConnection.createStatement();

			String sql = null;
			ResultSet rs = null;

			for (Long patientId: patientIds) {
				Long orgId = hmOrgIds.get(patientId);
				Long personId = hmPersonIds.get(patientId);

				//find service ID
				sql = "SELECT service_id FROM enterprise_organisation_id_map WHERE enterprise_id = " + orgId;
				rs = subscriberTransformStatement.executeQuery(sql);
				if (!rs.next()) {
					throw new Exception("Failed to find service iD for patient ID " + patientId + " and org ID " + orgId);
				}
				String serviceId = rs.getString(1);
				rs.close();

				//find patient ID
				sql = "SELECT resource_type, resource_id FROM enterprise_id_map WHERE enterprise_id = " + patientId;
				rs = subscriberTransformStatement.executeQuery(sql);
				if (!rs.next()) {
					throw new Exception("Failed to find resource iD for patient ID " + patientId);
				}
				String resourceType = rs.getString(1);
				String resourceId = rs.getString(2);
				rs.close();

				if (!resourceType.equals("Patient")) {
					throw new Exception("Not a patient resource type for enterprise ID " + patientId);
				}

				//get patient
				Resource resource = null;
				try {
					resource = resourceDal.getCurrentVersionAsResource(UUID.fromString(serviceId), ResourceType.Patient, resourceId);
				} catch (Exception ex) {
					throw new Exception("Failed to get patient " + resourceId + " for service " + serviceId, ex);
				}

				if (resource == null) {
					LOG.error("Failed to find patient resource for " + ResourceType.Patient + " " + resourceId + ", service ID = " + serviceId + " and patient ID = " + patientId);
					continue;
					//throw new Exception("Failed to find patient resource for " + resourceType + " " + resourceId + ", service ID = " + serviceId + " and patient ID = " + patientId);
				}
				Patient patient = (Patient)resource;

				//generate new pseudo ID
				String pseudoId = PatientTransformer.pseudonymiseUsingConfig(patient, salt);

				//save to person
				if (Strings.isNullOrEmpty(pseudoId)) {
					sql = "UPDATE person"
							+ " SET pseudo_id = null"
							+ " WHERE id = " + personId;
					statement.executeUpdate(sql);

				} else {
					sql = "UPDATE person"
							+ " SET pseudo_id = '" + pseudoId + "'"
							+ " WHERE id = " + personId;
					statement.executeUpdate(sql);
				}

				//save to patient
				if (Strings.isNullOrEmpty(pseudoId)) {
					sql = "UPDATE patient"
							+ " SET pseudo_id = null"
							+ " WHERE id = " + patientId;
					statement.executeUpdate(sql);

				} else {
					sql = "UPDATE patient"
							+ " SET pseudo_id = '" + pseudoId + "'"
							+ " WHERE id = " + patientId;
					statement.executeUpdate(sql);
				}

				//linked distributers
				if (arr != null) {
					for (LinkDistributorConfig linked: arr) {
						String linkedPseudoId = PatientTransformer.pseudonymiseUsingConfig(patient, linked);
						sql = "INSERT INTO link_distributor (source_skid, target_salt_key_name, target_skid) VALUES ('" + pseudoId + "', '" + linked.getSaltKeyName() + "', '" + linkedPseudoId + "')"
								+ " ON DUPLICATE KEY UPDATE"
								+ " target_salt_key_name = VALUES(target_salt_key_name),"
								+ " target_skid = VALUES(target_skid)";
						statement.executeUpdate(sql);
					}
				}

				//save to subscriber transform
				sql = "DELETE FROM pseudo_id_map WHERE patient_id = '" + resourceId + "'";
				subscriberTransformStatement.executeUpdate(sql);

				if (!Strings.isNullOrEmpty(pseudoId)) {
					sql = "INSERT INTO pseudo_id_map (patient_id, pseudo_id) VALUES ('" + resourceId + "', '" + pseudoId + "')";
					subscriberTransformStatement.executeUpdate(sql);
				}



				subscriberConnection.commit();
				subscriberTransformConnection.commit();

				int doneLocal = done.incrementAndGet();
				if (doneLocal % 1000 == 0) {
					LOG.debug("Done " + doneLocal);
				}
			}

			statement.close();
			subscriberTransformStatement.close();

			subscriberConnection.close();
			subscriberTransformConnection.close();
		}
	}*/

	/*private static void fixDeceasedPatients(String subscriberConfig) {
		LOG.debug("Fixing Deceased Patients for " + subscriberConfig);
		try {
			JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfig, "db_subscriber");

			Connection subscriberConnection = EnterpriseFiler.openConnection(config);

			Map<Long, Long> patientIds = new HashMap<>();
			String sql = "SELECT id, organization_id FROM patient WHERE date_of_death IS NOT NULL";
			Statement statement = subscriberConnection.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				long patientId = rs.getLong(1);
				long orgId = rs.getLong(2);
				patientIds.put(new Long(patientId), new Long(orgId));
			}
			rs.close();
			statement.close();

			EnterpriseAgeUpdaterlDalI dal = DalProvider.factoryEnterpriseAgeUpdaterlDal(subscriberConfig);

			EntityManager entityManager = ConnectionManager.getSubscriberTransformEntityManager(subscriberConfig);
			SessionImpl session = (SessionImpl) entityManager.getDelegate();
			Connection subscriberTransformConnection = session.connection();

			ResourceDalI resourceDal = DalProvider.factoryResourceDal();

			for (Long patientId: patientIds.keySet()) {
				Long orgId = patientIds.get(patientId);

				statement = subscriberTransformConnection.createStatement();

				sql = "SELECT service_id FROM enterprise_organisation_id_map WHERE enterprise_id = " + orgId;
				rs = statement.executeQuery(sql);
				if (!rs.next()) {
					throw new Exception("Failed to find service iD for patient ID " + patientId + " and org ID " + orgId);
				}
				String serviceId = rs.getString(1);
				rs.close();

				sql = "SELECT resource_type, resource_id FROM enterprise_id_map WHERE enterprise_id = " + patientId;
				rs = statement.executeQuery(sql);
				if (!rs.next()) {
					throw new Exception("Failed to find resource iD for patient ID " + patientId);
				}
				String resourceType = rs.getString(1);
				String resourceId = rs.getString(2);
				rs.close();

				statement.close();

				Resource resource = resourceDal.getCurrentVersionAsResource(UUID.fromString(serviceId), ResourceType.valueOf(resourceType), resourceId);
				if (resource == null) {
					LOG.error("Failed to find patient resource for " + resourceType + " " + resourceId + ", service ID = " + serviceId + " and patient ID = " + patientId);
					continue;
					//throw new Exception("Failed to find patient resource for " + resourceType + " " + resourceId + ", service ID = " + serviceId + " and patient ID = " + patientId);
				}
				Patient patient = (Patient)resource;
				Date dob = patient.getBirthDate();
				Date dod = patient.getDeceasedDateTimeType().getValue();

				Integer[] ages = dal.calculateAgeValuesAndUpdateTable(patientId, dob, dod);

				updateEnterprisePatient(patientId, ages, subscriberConnection);
				updateEnterprisePerson(patientId, ages, subscriberConnection);
			}

			subscriberConnection.close();
			subscriberTransformConnection.close();

			LOG.debug("Finished Fixing Deceased Patients for " + subscriberConfig);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/


	/*private static void updateEnterprisePatient(long enterprisePatientId, Integer[] ages, Connection connection) throws Exception {

		//the enterprise patient database isn't managed using hibernate, so we need to simply write a simple update statement
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE patient SET ");
		sb.append("age_years = ?, ");
		sb.append("age_months = ?, ");
		sb.append("age_weeks = ? ");
		sb.append("WHERE id = ?");

		PreparedStatement update = connection.prepareStatement(sb.toString());

		if (ages[EnterpriseAge.UNIT_YEARS] == null) {
			update.setNull(1, Types.INTEGER);
		} else {
			update.setInt(1, ages[EnterpriseAge.UNIT_YEARS]);
		}

		if (ages[EnterpriseAge.UNIT_MONTHS] == null) {
			update.setNull(2, Types.INTEGER);
		} else {
			update.setInt(2, ages[EnterpriseAge.UNIT_MONTHS]);
		}

		if (ages[EnterpriseAge.UNIT_WEEKS] == null) {
			update.setNull(3, Types.INTEGER);
		} else {
			update.setInt(3, ages[EnterpriseAge.UNIT_WEEKS]);
		}

		update.setLong(4, enterprisePatientId);

		update.addBatch();
		update.executeBatch();

		connection.commit();

		LOG.info("Updated patient " + enterprisePatientId + " to ages " + ages[EnterpriseAge.UNIT_YEARS] + " y, " + ages[EnterpriseAge.UNIT_MONTHS] + " m " + ages[EnterpriseAge.UNIT_WEEKS] + " wks");
	}

	private static void updateEnterprisePerson(long enterprisePatientId, Integer[] ages, Connection connection) throws Exception {

		//update the age fields on the person table where the person is for our patient and their pseudo IDs match
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE patient, person SET ");
		sb.append("person.age_years = ?, ");
		sb.append("person.age_months = ?, ");
		sb.append("person.age_weeks = ? ");
		sb.append("WHERE patient.id = ? ");
		sb.append("AND patient.person_id = person.id ");
		sb.append("AND patient.pseudo_id = person.pseudo_id");

		PreparedStatement update = connection.prepareStatement(sb.toString());

		if (ages[EnterpriseAge.UNIT_YEARS] == null) {
			update.setNull(1, Types.INTEGER);
		} else {
			update.setInt(1, ages[EnterpriseAge.UNIT_YEARS]);
		}

		if (ages[EnterpriseAge.UNIT_MONTHS] == null) {
			update.setNull(2, Types.INTEGER);
		} else {
			update.setInt(2, ages[EnterpriseAge.UNIT_MONTHS]);
		}

		if (ages[EnterpriseAge.UNIT_WEEKS] == null) {
			update.setNull(3, Types.INTEGER);
		} else {
			update.setInt(3, ages[EnterpriseAge.UNIT_WEEKS]);
		}

		update.setLong(4, enterprisePatientId);

		update.addBatch();
		update.executeBatch();

		connection.commit();
	}*/

	/*private static void testS3Read(String s3BucketName, String keyName, String start, String len) {
		LOG.debug("Testing S3 Read from " + s3BucketName + " " + keyName + " from " + start + " " + len + " bytes");
		try {

			AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder
					.standard()
					.withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
					.withRegion(Regions.EU_WEST_2);

			AmazonS3 s3Client = clientBuilder.build();

			GetObjectRequest request = new GetObjectRequest(s3BucketName, keyName);

			long startInt = Long.parseLong(start);
			long lenInt = Long.parseLong(len);
			long endInt = startInt + lenInt;

			request.setRange(startInt, endInt);

			long startMs = System.currentTimeMillis();

			S3Object object = s3Client.getObject(request);
			InputStream inputStream = object.getObjectContent();

			InputStreamReader reader = new InputStreamReader(inputStream, Charset.defaultCharset());

			StringBuilder sb = new StringBuilder();

			char[] buf = new char[100];
			while (true) {
				int read = reader.read(buf);
				if (read == -1
						|| sb.length() >= lenInt) {
					break;
				}

				sb.append(buf, 0, read);
			}

			reader.close();

			long endMs = System.currentTimeMillis();

			LOG.debug("Read " + sb.toString() + " in " + (endMs - startMs) + " ms");

			LOG.debug("Finished Testing S3 Read from " + s3BucketName + " " + keyName + " from " + start + " " + len + " bytes");
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void createTransforMap(UUID serviceId, String table, String outputFile) {
		LOG.debug("Creating transform map for " + serviceId + " from " + table);
		try {

			//retrieve from table
			EntityManager transformEntityManager = ConnectionManager.getPublisherTransformEntityManager(serviceId);
			SessionImpl session2 = (SessionImpl)transformEntityManager.getDelegate();
			Connection mappingConnection = session2.connection();

			EntityManager ehrEntityManager = ConnectionManager.getEhrEntityManager(serviceId);
			SessionImpl session3 = (SessionImpl)ehrEntityManager.getDelegate();
			Connection ehrConnection = session3.connection();

			String sql = "SELECT resource_type, resource_id, version FROM " + table;
			Statement statement = mappingConnection.createStatement();
			statement.setFetchSize(1000);
			ResultSet rs = statement.executeQuery(sql);
			LOG.debug("Got resource IDs from DB");

			Map<String, Map<String, List<String>>> hm = new HashMap<>();
			int count = 0;

			//build map up per resource
			while (rs.next()) {
				String resourceType = rs.getString("resource_type");
				String resourceId = rs.getString("resource_id");
				String resourceVersion  = rs.getString("version");


				*//*sql = "SELECT * FROM resource_field_mappings WHERE version = 'a905db26-1357-4710-90ef-474f256567ed';";
				PreparedStatement statement1 = mappingConnection.prepareStatement(sql);*//*

	 *//*sql = "SELECT * FROM resource_field_mappings WHERE version = ?";
				PreparedStatement statement1 = mappingConnection.prepareStatement(sql);*//*

				sql = "SELECT * FROM resource_field_mappings WHERE resource_type = '" + resourceType + "' AND resource_id = '" + resourceId + "' AND version = '" + resourceVersion + "';";
				PreparedStatement statement1 = mappingConnection.prepareStatement(sql);

				//sql = "SELECT * FROM resource_field_mappings WHERE resource_type = ? AND resource_id = ? AND version = ?";
				//sql = "SELECT * FROM resource_field_mappings WHERE resource_type = ? AND resource_id = ? AND version = ?";

				//statement1.setString(1, resourceVersion);
				*//*statement1.setString(1, resourceType);
				statement1.setString(2, resourceId);
				statement1.setString(3, resourceVersion);*//*

				ResultSet rs1 = null;
				try {
					rs1 = statement1.executeQuery(sql);

 				} catch (Exception ex) {
					LOG.error("" + statement1);
					throw ex;
				}
				rs1.next();
				String jsonStr = rs1.getString("mappings_json");
				rs1.close();
				statement1.close();

				sql = "SELECT * FROM resource_history WHERE resource_type = ? AND resource_id = ? AND version = ?";
				statement1 = ehrConnection.prepareStatement(sql);
				statement1.setString(1, resourceType);
				statement1.setString(2, resourceId);
				statement1.setString(3, resourceVersion);
				rs1 = statement1.executeQuery();
				if (!rs1.next()) {
					throw new Exception("Failed to find resource_history for " + statement1.toString());
				}
				String s = rs1.getString("resource_data");
				rs1.close();
				statement1.close();

				if (Strings.isNullOrEmpty(s)) {
					continue;
				}

				JsonNode resourceJson = ObjectMapperPool.getInstance().readTree(s);

				Map<String, List<String>> hmResourceType = hm.get(resourceType);
				if (hmResourceType == null) {
					hmResourceType = new HashMap<>();
					hm.put(resourceType, hmResourceType);
				}

				JsonNode json = ObjectMapperPool.getInstance().readTree(jsonStr);

				for (int i=0; i<json.size(); i++) {
					JsonNode child = json.get(i);

					JsonNode idNode = child.get("auditId");
					JsonNode colsNode = child.get("cols");
					if (idNode == null) {
						throw new Exception("No ID node in " + jsonStr);
					}
					if (colsNode == null) {
						throw new Exception("No cols node in " + jsonStr);
					}

					long id = idNode.asLong();

					//get source file ID
					sql = "SELECT * FROM source_file_record WHERE id = ?";
					statement1 = mappingConnection.prepareStatement(sql);
					statement1.setLong(1, id);
					rs1 = statement1.executeQuery();
					rs1.next();
					long sourceFileId = rs1.getLong("source_file_id");
					rs1.close();
					statement1.close();

					//get source file type
					sql = "SELECT * FROM source_file WHERE id = ?";
					statement1 = mappingConnection.prepareStatement(sql);
					statement1.setLong(1, sourceFileId);
					rs1 = statement1.executeQuery();
					rs1.next();
					long sourceFileType = rs1.getLong("source_file_type_id");
					rs1.close();
					statement1.close();

					//get the type desc
					sql = "SELECT * FROM source_file_type WHERE id = ?";
					statement1 = mappingConnection.prepareStatement(sql);
					statement1.setLong(1, sourceFileType);
					rs1 = statement1.executeQuery();
					rs1.next();
					String fileTypeDesc = rs1.getString("description");
					rs1.close();
					statement1.close();

					//get the cols
					Map<Integer, String> hmCols = new HashMap<>();

					sql = "SELECT * FROM source_file_type_column WHERE source_file_type_id = ?";
					statement1 = mappingConnection.prepareStatement(sql);
					statement1.setLong(1, sourceFileType);
					rs1 = statement1.executeQuery();
					while (rs1.next()) {
						int index = rs1.getInt("column_index");
						String name = rs1.getString("column_name");
						hmCols.put(new Integer(index), name);
					}
					rs1.close();
					statement1.close();

					for (int j=0; j<colsNode.size(); j++) {
						JsonNode colNode = colsNode.get(j);
						int col = colNode.get("col").asInt();
						String jsonField = colNode.get("field").asText();

						int index = jsonField.indexOf("[");
						while (index > -1) {
							int endIndex = jsonField.indexOf("]", index);
							String prefix = jsonField.substring(0, index + 1);
							String suffix = jsonField.substring(endIndex);

							if (prefix.equals("extension[")) {
								String val = jsonField.substring(index+1, endIndex);
								int extensionIndex = Integer.parseInt(val);

								JsonNode extensionArray = resourceJson.get("extension");
								JsonNode extensionRoot = extensionArray.get(extensionIndex);
								String extensionUrl = extensionRoot.get("url").asText();
								extensionUrl = extensionUrl.replace("http://endeavourhealth.org/fhir/StructureDefinition/", "");
								extensionUrl = extensionUrl.replace("http://hl7.org/fhir/StructureDefinition/", "");

								jsonField = prefix + extensionUrl + suffix;

							} else {
								jsonField = prefix + "n" + suffix;
							}

							index = jsonField.indexOf("[", endIndex);
						}

						String colName = hmCols.get(new Integer(col));
						String fileTypeAndCol = fileTypeDesc + ":" + colName;

						List<String> fieldNameMappings = hmResourceType.get(jsonField);
						if (fieldNameMappings == null) {
							fieldNameMappings = new ArrayList<>();
							hmResourceType.put(jsonField, fieldNameMappings);
						}

						if (!fieldNameMappings.contains(fileTypeAndCol)) {
							fieldNameMappings.add(fileTypeAndCol);
						}
					}
				}

				count ++;
				if (count % 500 == 0) {
					LOG.debug("Done " + count);
				}
			}
			LOG.debug("Done " + count);

			rs.close();
			ehrEntityManager.close();

			//create output file
			List<String> lines = new ArrayList<>();

			List<String> resourceTypes = new ArrayList<>(hm.keySet());
			Collections.sort(resourceTypes, String.CASE_INSENSITIVE_ORDER);
			for (String resourceType: resourceTypes) {

				lines.add("============================================================");
				lines.add(resourceType);
				lines.add("============================================================");

				Map<String, List<String>> hmResourceType = hm.get(resourceType);
				List<String> fields = new ArrayList<>(hmResourceType.keySet());
				Collections.sort(fields, String.CASE_INSENSITIVE_ORDER);

				for (String field: fields) {

					String linePrefix = field + " = ";

					List<String> sourceRecords = hmResourceType.get(field);
					for (String sourceRecord: sourceRecords) {

						lines.add(linePrefix + sourceRecord);
						linePrefix = Strings.repeat(" ", linePrefix.length());
					}
					lines.add("");
				}
				lines.add("");
			}

			File f = new File(outputFile);
			Path p = f.toPath();
			Files.write(p, lines, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

			LOG.debug("Finished creating transform map from " + table);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	/*private static void fixBartsPatients(UUID serviceId) {
		LOG.debug("Fixing Barts patients at service " + serviceId);
		try {

			EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
			SessionImpl session = (SessionImpl)edsEntityManager.getDelegate();
			Connection edsConnection = session.connection();

			int checked = 0;
			int fixed = 0;

			ResourceDalI resourceDal = DalProvider.factoryResourceDal();

			String sql = "SELECT patient_id FROM patient_search WHERE service_id = '" + serviceId + "';";

			Statement s = edsConnection.createStatement();
			s.setFetchSize(10000); //don't get all rows at once
			ResultSet rs = s.executeQuery(sql);
			LOG.info("Got raw results back");
			while (rs.next()) {
				String patientId = rs.getString(1);

				ResourceWrapper wrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Patient.toString(), UUID.fromString(patientId));
				if (wrapper == null) {
					LOG.error("Failed to get recource current for ID " + patientId);
					continue;
				}
				String oldJson = wrapper.getResourceData();
				Patient patient = (Patient)FhirSerializationHelper.deserializeResource(oldJson);

				PatientBuilder patientBuilder = new PatientBuilder(patient);

				List<String> numbersFromCsv = new ArrayList<>();
				if (patient.hasTelecom()) {
					for (ContactPoint contactPoint: patient.getTelecom()) {
						if (contactPoint.hasId()) {
							numbersFromCsv.add(contactPoint.getValue());
						}
					}

					for (String numberFromCsv: numbersFromCsv) {
						PPPHOTransformer.removeExistingContactPointWithoutIdByValue(patientBuilder, numberFromCsv);
					}
				}

				List<HumanName> namesFromCsv = new ArrayList<>();
				if (patient.hasName()) {
					for (HumanName name: patient.getName()) {
						if (name.hasId()) {
							namesFromCsv.add(name);
						}
					}

					for (HumanName name: namesFromCsv) {
						PPNAMTransformer.removeExistingNameWithoutIdByValue(patientBuilder, name);
					}
				}

				List<Address> addressesFromCsv = new ArrayList<>();
				if (patient.hasAddress()) {
					for (Address address: patient.getAddress()) {
						if (address.hasId()) {
							addressesFromCsv.add(address);
						}
					}

					for (Address address: addressesFromCsv) {
						PPADDTransformer.removeExistingAddressWithoutIdByValue(patientBuilder, address);
					}
				}


				String newJson = FhirSerializationHelper.serializeResource(patient);
				if (!newJson.equals(oldJson)) {

					wrapper.setResourceData(newJson);
					saveResourceWrapper(serviceId, wrapper);
					fixed ++;
				}

				checked ++;
				if (checked % 1000 == 0) {
					LOG.debug("Checked " + checked + " fixed " + fixed);
				}
			}

			LOG.debug("Checked " + checked + " fixed " + fixed);

			rs.close();
			s.close();
			edsEntityManager.close();

			LOG.debug("Finish Fixing Barts patients at service " + serviceId);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/

	private static void postToRabbit(String exchangeName, String srcFile, Integer throttle) {
		LOG.info("Posting to " + exchangeName + " from " + srcFile);
		if (throttle != null) {
			LOG.info("Throttled to " + throttle + " messages/second");
		}

		try {
			File src = new File(srcFile);

			//create file of ones done
			File dir = src.getParentFile();
			String name = "DONE" + src.getName();
			File dst = new File(dir, name);

			Set<UUID> hsAlreadyDone = new HashSet<>();
			if (dst.exists()) {
				List<String> lines = Files.readAllLines(dst.toPath());
				for (String line : lines) {
					if (!Strings.isNullOrEmpty(line)) {
						try {
							UUID uuid = UUID.fromString(line);
							hsAlreadyDone.add(uuid);
						} catch (Exception ex) {
							LOG.error("Skipping line " + line);
						}
					}
				}

				LOG.info("Already done " + hsAlreadyDone.size());
			}

			List<UUID> exchangeIds = new ArrayList<>();
			int countTotal = 0;

			List<String> lines = Files.readAllLines(src.toPath());
			for (String line : lines) {
				if (!Strings.isNullOrEmpty(line)) {
					try {
						UUID uuid = UUID.fromString(line);
						countTotal++;

						if (!hsAlreadyDone.contains(uuid)) {
							exchangeIds.add(uuid);
						}

					} catch (Exception ex) {
						LOG.error("Skipping line " + line);
					}
				}
			}
			LOG.info("Found " + countTotal + " down to " + exchangeIds.size() + " skipping ones already done, to post to " + exchangeName);

			continueOrQuit();

			FileWriter fileWriter = new FileWriter(dst, true);
			PrintWriter printWriter = new PrintWriter(fileWriter);

			long startMs = System.currentTimeMillis();
			int doneThisSecond = 0;

			LOG.info("Posting " + exchangeIds.size() + " to " + exchangeName);
			for (int i = 0; i < exchangeIds.size(); i++) {

				UUID exchangeId = exchangeIds.get(i);
				List<UUID> tmp = new ArrayList<>();
				tmp.add(exchangeId);
				QueueHelper.postToExchange(tmp, exchangeName, null, true, null);

				printWriter.println(exchangeId.toString());
				printWriter.flush();

				if (i % 5000 == 0) {
					LOG.debug("Done " + i + " / " + exchangeIds.size());
				}

				if (throttle != null) {
					doneThisSecond++;

					if (doneThisSecond > throttle.intValue()) {
						long now = System.currentTimeMillis();
						long sleep = 1000 - (now - startMs);

						if (sleep > 0) {
							Thread.sleep(sleep);
						}

						startMs = System.currentTimeMillis();
						doneThisSecond = 0;
					}
				}
			}

			printWriter.close();

			LOG.info("Finished Posting to " + exchangeName + " from " + srcFile);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void postExchangesToProtocol(String srcFile) {
		LOG.info("Posting to protocol from " + srcFile);
		try {
			List<UUID> exchangeIds = new ArrayList<>();

			List<String> lines = Files.readAllLines(new File(srcFile).toPath());
			for (String line: lines) {
				if (!Strings.isNullOrEmpty(line)) {
					UUID uuid = UUID.fromString(line);
					exchangeIds.add(uuid);
				}
			}

			LOG.info("Posting " + exchangeIds.size() + " to Protocol queue");
			QueueHelper.postToExchange(exchangeIds, "EdsProtocol", null, false, null);

			LOG.info("Finished Posting to protocol from " + srcFile);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	/*

create table uprn_pseudo_map (
	uprn bigint,
    pseudo_uprn varchar(255),
    property_class varchar(10)
);

	 */
	private static void calculateUprnPseudoIds(String subscriberConfigName, String targetTable) throws Exception {
		LOG.info("Calculating UPRN Pseudo IDs " + subscriberConfigName);
		try {

			JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");
			JsonNode pseudoNode = config.get("pseudonymisation");
			if (pseudoNode == null){
				LOG.error("No salt key found!");
				return;
			}
			JsonNode saltNode = pseudoNode.get("salt");
			String base64Salt = saltNode.asText();
			byte[] saltBytes = Base64.getDecoder().decode(base64Salt);

			EntityManager subscrberEntityManager = ConnectionManager.getSubscriberTransformEntityManager(subscriberConfigName);
			SessionImpl session = (SessionImpl) subscrberEntityManager.getDelegate();
			Connection subscriberConnection = session.connection();

			String upsertSql = "INSERT INTO " + targetTable + " (uprn, pseudo_uprn, property_class) VALUES (?, ?, ?)";

			PreparedStatement psUpsert = subscriberConnection.prepareStatement(upsertSql);
			int inBatch = 0;
			int done = 0;

			EntityManager referenceEntityManager = ConnectionManager.getReferenceEntityManager();
			session = (SessionImpl) referenceEntityManager.getDelegate();
			Connection referenceConnection = session.connection();

			String selectSql = "SELECT uprn, property_class FROM uprn_property_class";

			PreparedStatement psSelect = referenceConnection.prepareStatement(selectSql);
			psSelect.setFetchSize(2000);

			LOG.info("Starting query on EDS database");
			ResultSet rs = psSelect.executeQuery();
			LOG.info("Got raw results back");

			while (rs.next()) {
				long uprn = rs.getLong(1);
				String cls = rs.getString(2);

				String pseuoUprn = null;
				TreeMap<String, String> keys = new TreeMap<>();
				keys.put("UPRN", "" + uprn);

				Crypto crypto = new Crypto();
				crypto.SetEncryptedSalt(saltBytes);
				pseuoUprn = crypto.GetDigest(keys);

				psUpsert.setLong(1, uprn);
				psUpsert.setString(2, pseuoUprn);
				psUpsert.setString(3, cls);

				psUpsert.addBatch();
				inBatch++;
				done++;

				if (inBatch >= TransformConfig.instance().getResourceSaveBatchSize()) {
					psUpsert.executeBatch();
					subscriberConnection.commit();
					inBatch = 0;
				}

				if (done % 5000 == 0) {
					LOG.debug("Done " + done);
				}
			}

			if (inBatch > 0) {
				psUpsert.executeBatch();
				subscriberConnection.commit();
			}
			LOG.debug("Done " + done);

			psUpsert.close();
			subscrberEntityManager.close();

			psSelect.close();
			referenceEntityManager.close();

			LOG.info("Finished Calculating UPRN Pseudo IDs " + subscriberConfigName);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void populateSubscriberUprnTable(String subscriberConfigName, Integer overrideBatchSize, String specificPatientId) throws Exception {
		LOG.info("Populating Subscriber UPRN Table for " + subscriberConfigName);
		try {

			int saveBatchSize = TransformConfig.instance().getResourceSaveBatchSize();
			if (overrideBatchSize != null) {
				saveBatchSize = overrideBatchSize.intValue();
			}

			JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");

			//changed the format of the JSON
			JsonNode pseudoNode = config.get("pseudonymisation");
			boolean pseudonymised = pseudoNode != null;
			byte[] saltBytes = null;

			if (pseudonymised) {
				JsonNode saltNode = pseudoNode.get("salt");
				String base64Salt = saltNode.asText();
				saltBytes = Base64.getDecoder().decode(base64Salt);
			}
			/*boolean pseudonymised = config.get("pseudonymised").asBoolean();

			byte[] saltBytes = null;
			if (pseudonymised) {
				JsonNode saltNode = config.get("salt");
				String base64Salt = saltNode.asText();
				saltBytes = Base64.getDecoder().decode(base64Salt);
			}*/

			List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openConnection(subscriberConfigName);
			for (EnterpriseConnector.ConnectionWrapper connectionWrapper: connectionWrappers) {
				Connection subscriberConnection = connectionWrapper.getConnection();
				LOG.info("Populating " + connectionWrapper);

				String upsertSql;
				if (pseudonymised) {
					upsertSql = "INSERT INTO patient_uprn"
							+ " (patient_id, organization_id, person_id, lsoa_code, pseudo_uprn, qualifier, `algorithm`, `match`, no_address, invalid_address, missing_postcode, invalid_postcode, property_class)"
							+ " VALUES"
							+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
							+ " ON DUPLICATE KEY UPDATE"
							+ " organization_id = VALUES(organization_id),"
							+ " person_id = VALUES(person_id),"
							+ " lsoa_code = VALUES(lsoa_code),"
							+ " pseudo_uprn = VALUES(pseudo_uprn),"
							+ " qualifier = VALUES(qualifier),"
							+ " `algorithm` = VALUES(`algorithm`),"
							+ " `match` = VALUES(`match`),"
							+ " no_address = VALUES(no_address),"
							+ " invalid_address = VALUES(invalid_address),"
							+ " missing_postcode = VALUES(missing_postcode),"
							+ " invalid_postcode = VALUES(invalid_postcode),"
							+ " property_class = VALUES(property_class)";

				} else {
					upsertSql = "INSERT INTO patient_uprn"
							+ " (patient_id, organization_id, person_id, lsoa_code, uprn, qualifier, `algorithm`, `match`, no_address, invalid_address, missing_postcode, invalid_postcode, property_class)"
							+ " VALUES"
							+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
							+ " ON DUPLICATE KEY UPDATE"
							+ " organization_id = VALUES(organization_id),"
							+ " person_id = VALUES(person_id),"
							+ " lsoa_code = VALUES(lsoa_code),"
							+ " uprn = VALUES(uprn),"
							+ " qualifier = VALUES(qualifier),"
							+ " `algorithm` = VALUES(`algorithm`),"
							+ " `match` = VALUES(`match`),"
							+ " no_address = VALUES(no_address),"
							+ " invalid_address = VALUES(invalid_address),"
							+ " missing_postcode = VALUES(missing_postcode),"
							+ " invalid_postcode = VALUES(invalid_postcode),"
							+ " property_class = VALUES(property_class)";
				}

				PreparedStatement psUpsert = subscriberConnection.prepareStatement(upsertSql);
				int inBatch = 0;

				EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
				SessionImpl session = (SessionImpl) edsEntityManager.getDelegate();
				Connection edsConnection = session.connection();

				SubscriberResourceMappingDalI enterpriseIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
				PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
				PostcodeDalI postcodeDal = DalProvider.factoryPostcodeDal();

				int checked = 0;
				int saved = 0;

				Map<String, Boolean> hmPermittedPublishers = new HashMap<>();

				//join to the property class table - this isn't the best way of doing it as it will only work while
				//the reference and eds databases are on the same server
				//String sql = "SELECT service_id, patient_id, uprn, qualifier, abp_address, `algorithm`, `match`, no_address, invalid_address, missing_postcode, invalid_postcode FROM patient_address_uprn";
				String sql = "SELECT a.service_id, a.patient_id, a.uprn, a.qualifier, a.abp_address, a.`algorithm`,"
						+ " a.`match`, a.no_address, a.invalid_address, a.missing_postcode, a.invalid_postcode, c.property_class"
						+ " FROM patient_address_uprn a"
						+ " LEFT OUTER JOIN reference.uprn_property_class c"
						+ " ON c.uprn = a.uprn";

				//support one patient at a time for debugging
				if (specificPatientId != null) {
					sql += " WHERE a.patient_id = '" + specificPatientId + "'";
					LOG.debug("Restricting to patient " + specificPatientId);
				}

				Statement s = edsConnection.createStatement();
				s.setFetchSize(2000); //don't get all rows at once

				LOG.info("Starting query on EDS database");
				ResultSet rs = s.executeQuery(sql);
				LOG.info("Got raw results back");

				while (rs.next()) {
					int col = 1;
					String serviceId = rs.getString(col++);
					String patientId = rs.getString(col++);
					Long uprn = rs.getLong(col++);
					if (rs.wasNull()) {
						uprn = null;
					}
					String qualifier = rs.getString(col++);
					String abpAddress = rs.getString(col++);
					String algorithm = rs.getString(col++);
					String match = rs.getString(col++);
					boolean noAddress = rs.getBoolean(col++);
					boolean invalidAddress = rs.getBoolean(col++);
					boolean missingPostcode = rs.getBoolean(col++);
					boolean invalidPostcode = rs.getBoolean(col++);
					String propertyClass = rs.getString(col++);


					//because of past mistakes, we have Discovery->Enterprise mappings for patients that
					//shouldn't, so we also need to check that the service ID is definitely a publisher to this subscriber
					Boolean isPublisher = hmPermittedPublishers.get(serviceId);
					if (isPublisher == null) {

						List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceId, null); //passing null means don't filter on system ID
						for (LibraryItem libraryItem : libraryItems) {
							Protocol protocol = libraryItem.getProtocol();
							if (protocol.getEnabled() != ProtocolEnabled.TRUE) {
								continue;
							}

							//check to make sure that this service is actually a PUBLISHER to this protocol
							boolean isProtocolPublisher = false;
							for (ServiceContract serviceContract : protocol.getServiceContract()) {
								if (serviceContract.getType().equals(PUBLISHER)
										&& serviceContract.getService().getUuid().equals(serviceId)
										&& serviceContract.getActive() == ServiceContractActive.TRUE) {

									isProtocolPublisher = true;
									break;
								}
							}
							if (!isProtocolPublisher) {
								continue;
							}

							//check to see if this subscriber config is a subscriber to this DB
							for (ServiceContract serviceContract : protocol.getServiceContract()) {
								if (serviceContract.getType().equals(ServiceContractType.SUBSCRIBER)
										&& serviceContract.getActive() == ServiceContractActive.TRUE) {

									ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
									UUID subscriberServiceId = UUID.fromString(serviceContract.getService().getUuid());
									UUID subscriberTechnicalInterfaceId = UUID.fromString(serviceContract.getTechnicalInterface().getUuid());
									Service subscriberService = serviceRepository.getById(subscriberServiceId);
									List<ServiceInterfaceEndpoint> serviceEndpoints = subscriberService.getEndpointsList();
									for (ServiceInterfaceEndpoint serviceEndpoint : serviceEndpoints) {
										if (serviceEndpoint.getTechnicalInterfaceUuid().equals(subscriberTechnicalInterfaceId)) {
											String protocolSubscriberConfigName = serviceEndpoint.getEndpoint();
											if (protocolSubscriberConfigName.equals(subscriberConfigName)) {
												isPublisher = new Boolean(true);
												break;
											}
										}
									}
								}
							}
						}

						if (isPublisher == null) {
							isPublisher = new Boolean(false);
						}

						hmPermittedPublishers.put(serviceId, isPublisher);
					}

					if (specificPatientId != null) {
						LOG.debug("Org is publisher = " + isPublisher);
					}

					if (!isPublisher.booleanValue()) {
						continue;
					}

					//check if patient ID already exists in the subscriber DB
					Long subscriberPatientId = enterpriseIdDal.findEnterpriseIdOldWay(ResourceType.Patient.toString(), patientId);

					if (specificPatientId != null) {
						LOG.debug("Got patient " + patientId + " with UPRN " + uprn + " and property class " + propertyClass + " and subscriber patient ID " + subscriberPatientId);
					}

					//if the patient doesn't exist on this subscriber DB, then don't transform this record
					if (subscriberPatientId == null) {
						continue;
					}

					//see if the patient actually exists in the subscriber DB (might not if the patient is deleted or confidential)
					String checkSql = "SELECT id FROM patient WHERE id = ?";
					Connection subscriberConnection2 = connectionWrapper.getConnection();
					PreparedStatement psCheck = subscriberConnection2.prepareStatement(checkSql);
					psCheck.setLong(1, subscriberPatientId);
					ResultSet checkRs = psCheck.executeQuery();
					boolean inSubscriberDb = checkRs.next();
					psCheck.close();
					subscriberConnection2.close();
					if (!inSubscriberDb) {
						LOG.info("Skipping patient " + patientId + " -> " + subscriberPatientId + " as not found in enterprise DB");
						continue;
					}

					SubscriberOrgMappingDalI orgMappingDal = DalProvider.factorySubscriberOrgMappingDal(subscriberConfigName);
					Long subscriberOrgId = orgMappingDal.findEnterpriseOrganisationId(serviceId);

					String discoveryPersonId = patientLinkDal.getPersonId(patientId);
					SubscriberPersonMappingDalI personMappingDal = DalProvider.factorySubscriberPersonMappingDal(subscriberConfigName);
					Long subscriberPersonId = personMappingDal.findOrCreateEnterprisePersonId(discoveryPersonId);

					String lsoaCode = null;
					if (!Strings.isNullOrEmpty(abpAddress)) {
						String[] toks = abpAddress.split(" ");
						String postcode = toks[toks.length - 1];
						PostcodeLookup postcodeReference = postcodeDal.getPostcodeReference(postcode);
						if (postcodeReference != null) {
							lsoaCode = postcodeReference.getLsoaCode();
						}
					}


					col = 1;
					psUpsert.setLong(col++, subscriberPatientId);
					psUpsert.setLong(col++, subscriberOrgId);
					psUpsert.setLong(col++, subscriberPersonId);
					psUpsert.setString(col++, lsoaCode);

					if (pseudonymised) {

						String pseuoUprn = null;
						if (uprn != null) {

							TreeMap<String, String> keys = new TreeMap<>();
							keys.put("UPRN", "" + uprn);

							Crypto crypto = new Crypto();
							crypto.SetEncryptedSalt(saltBytes);
							pseuoUprn = crypto.GetDigest(keys);
						}

						psUpsert.setString(col++, pseuoUprn);
					} else {
						if (uprn != null) {

							psUpsert.setLong(col++, uprn.longValue());
						} else {
							psUpsert.setNull(col++, Types.BIGINT);
						}
					}
					psUpsert.setString(col++, qualifier);
					psUpsert.setString(col++, algorithm);
					psUpsert.setString(col++, match);
					psUpsert.setBoolean(col++, noAddress);
					psUpsert.setBoolean(col++, invalidAddress);
					psUpsert.setBoolean(col++, missingPostcode);
					psUpsert.setBoolean(col++, invalidPostcode);
					psUpsert.setString(col++, propertyClass);

					if (specificPatientId != null) {
						LOG.debug("" + psUpsert);
					}

					psUpsert.addBatch();
					inBatch++;
					saved++;

					if (inBatch >= saveBatchSize) {
						try {
							psUpsert.executeBatch();
							subscriberConnection.commit();
							inBatch = 0;
						} catch (Exception ex) {
							LOG.error("Error saving UPRN for " + patientId + " -> " + subscriberPatientId + " for org " + subscriberOrgId);
							LOG.error("" + psUpsert);
							throw ex;
						}
					}

					checked++;
					if (checked % 1000 == 0) {
						LOG.info("Checked " + checked + " Saved " + saved);
					}
				}

				if (inBatch > 0) {
					psUpsert.executeBatch();
					subscriberConnection.commit();
				}

				LOG.info("Chcked " + checked + " Saved " + saved);

				psUpsert.close();

				subscriberConnection.close();
				edsEntityManager.close();

				subscriberConnection.close();
			}

			LOG.info("Finished Populating Subscriber UPRN Table for " + subscriberConfigName);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	/*private static void fixPersonsNoNhsNumber() {
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
	}*/

	/*private static void checkDeletedObs(UUID serviceId, UUID systemId) {
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

			Set<String> observationsNotDeleted = new HashSet<>();

			List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
			for (Exchange exchange : exchanges) {
				List<ExchangePayloadFile> payload = ExchangeHelper.parseExchangeBody(exchange.getBody());
				ExchangePayloadFile firstItem = payload.get(0);
				//String version = EmisCsvToFhirTransformer.determineVersion(payload);

				//if we've reached the point before we process data for this practice, break out
				try {
					if (!EmisCsvToFhirTransformer.shouldProcessPatientData(payload)) {
						break;
					}
				} catch (TransformException e) {
					LOG.info("Skipping exchange containing " + firstItem.getPath());
					continue;
				}

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
							String observationId = record.get("ObservationGuid");

							if (deleted.equalsIgnoreCase("true")) {

								//if observation was reinstated at some point, skip it
								if (observationsNotDeleted.contains(observationId)) {
									continue;
								}

								String patientId = record.get("PatientGuid");

								CsvCell patientCell = CsvCell.factoryDummyWrapper(patientId);
								CsvCell observationCell = CsvCell.factoryDummyWrapper(observationId);


								Set<ResourceType> resourceTypes = org.endeavourhealth.transform.emis.csv.transforms.careRecord.ObservationTransformer.findOriginalTargetResourceTypes(csvHelper, patientCell, observationCell);
								for (ResourceType resourceType: resourceTypes) {

									//will already have been done OK
									if (resourceType == ResourceType.Observation) {
										continue;
									}

									String sourceId = patientId + ":" + observationId;
									UUID uuid = IdHelper.getEdsResourceId(serviceId, resourceType, sourceId);
									if (uuid == null) {
										throw new Exception("Failed to find UUID for " + resourceType + " " + sourceId);
									}

									LOG.debug("Fixing " + resourceType + " " + uuid);

									//create file of IDs to delete for each subscriber DB
									for (String subscriberConfig : subscriberConfigs) {
										EnterpriseIdDalI subscriberDal = DalProvider.factoryEnterpriseIdDal(subscriberConfig);
										Long enterpriseId = subscriberDal.findEnterpriseId(resourceType.toString(), uuid.toString());
										if (enterpriseId == null) {
											continue;
										}

										String sql = null;
										if (resourceType == ResourceType.AllergyIntolerance) {
											sql = "DELETE FROM allergy_intolerance WHERE id = " + enterpriseId;

										} else if (resourceType == ResourceType.ReferralRequest) {
											sql = "DELETE FROM referral_request WHERE id = " + enterpriseId;

										} else {
											sql = "DELETE FROM observation WHERE id = " + enterpriseId;
										}
										sql += "\n";

										File f = new File(subscriberConfig + ".sql");
										Files.write(f.toPath(), sql.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
									}

									//delete resource if not already done
									ResourceWrapper resourceWrapper = resourceDal.getCurrentVersion(serviceId, resourceType.toString(), uuid);
									if (resourceWrapper != null && !resourceWrapper.isDeleted()) {

										ExchangeBatch batch = hmBatchesByPatient.get(resourceWrapper.getPatientId());
										resourceWrapper.setDeleted(true);
										resourceWrapper.setResourceData(null);
										resourceWrapper.setResourceMetadata("");
										resourceWrapper.setExchangeBatchId(batch.getBatchId());
										resourceWrapper.setVersion(UUID.randomUUID());
										resourceWrapper.setCreatedAt(new Date());
										resourceWrapper.setExchangeId(exchange.getId());

										resourceDal.delete(resourceWrapper);
									}
								}
							} else {
								observationsNotDeleted.add(observationId);
							}
						}
						parser.close();
					}
				}
			}

			LOG.info("Finished Checking Observations for " + serviceId);
		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static void testBatchInserts(String url, String user, String pass, String num, String batchSizeStr) {
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
	}*/

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


	/*private static void fixEmisProblems3ForPublisher(String publisher, UUID systemId) {
		try {
			LOG.info("Doing fix for " + publisher);

			String[] done = new String[]{
					"01fcfe94-5dfd-4951-b74d-129f874209b0",
					"07a267d3-189b-4968-b9b0-547de28edef5",
					"0b9601d1-f7ab-4f5d-9f77-1841050f75ab",
					"0fd2ff5d-2c25-4707-afe8-707e81a250b8",
					"14276da8-c344-4841-a36d-aa38940e78e7",
					"158251ca-0e1d-4471-8fae-250b875911e1",
					"160131e2-a5ff-49c8-b62e-ae499a096193",
					"16490f2b-62ce-44c6-9816-528146272340",
					"18fa1bed-b9a0-4d55-a0cc-dfc31831259a",
					"19cba169-d41e-424a-812f-575625c72305",
					"19ff6a03-25df-4e61-9ab1-4573cfd24729",
					"1b3d1627-f49e-4103-92d6-af6016476da3",
					"1e198fbb-c9cd-429a-9b50-0f124d0d825c",
					"20444fbe-0802-46fc-8203-339a36f52215",
					"21e27bf3-8071-48dd-924f-1d8d21f9216f",
					"23203e72-a3b0-4577-9942-30f7cdff358e",
					"23be1f4a-68ec-4a49-b2ec-aa9109c99dcd",
					"2b56033f-a9b4-4bab-bb53-c619bdb38895",
					"2ba26f2d-8068-4b77-8e62-431edfc2c2e2",
					"2ed89931-0ce7-49ea-88ac-7266b6c03be0",
					"3abf8ded-f1b1-495b-9a2d-5d0223e33fa7",
					"3b0f6720-2ffd-4f8a-afcd-7e3bb311212d",
					"415b509a-cf39-45bc-9acf-7f982a00e159",
					"4221276f-a3b0-4992-b426-ec2d8c7347f2",
					"49868211-d868-4b55-a201-5acac0be0cc0",
					"55fdcbd0-9b2d-493a-b874-865ccc93a156",
					"56124545-d266-4da9-ba1f-b3a16edc7f31",
					"6c11453b-dbf8-4749-a0ec-ab705920e316"
			};

			ServiceDalI dal = DalProvider.factoryServiceDal();
			List<Service> all = dal.getAll();
			for (Service service: all) {
				if (service.getPublisherConfigName() != null
						&& service.getPublisherConfigName().equals(publisher)) {

					boolean alreadyDone = false;
					String idStr = service.getId().toString();
					for (String doneId: done) {
						if (idStr.equalsIgnoreCase(doneId)) {
							alreadyDone = true;
							break;
						}
					}
					if (alreadyDone) {
						continue;
					}

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
	}*/

	/*private static void convertExchangeBody() {
		try {
			LOG.info("Converting exchange bodies to JSON");

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

			List<Service> services = serviceDal.getAll();
			for (Service service: services) {

				List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
				for (UUID systemUuid: systemIds) {

					//skip ADT feeds because they will already be JSON
					if (systemUuid.equals(UUID.fromString("d874c58c-91fd-41bb-993e-b1b8b22038b2"))//live
						|| systemUuid.equals(UUID.fromString("68096181-9e5d-4cca-821f-a9ecaa0ebc50"))) { //dev
						continue;
					}

					List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemUuid, Integer.MAX_VALUE);
					if (exchanges.isEmpty()) {
						continue;
					}

					LOG.debug("doing " + service.getName() + " with " + exchanges.size() + " exchanges");

					for (Exchange exchange : exchanges) {

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
						for (String file : files) {
							ExchangePayloadFile fileObj = new ExchangePayloadFile();

							String fileWithoutSharedStorage = file.substring(TransformConfig.instance().getSharedStoragePath().length() + 1);
							fileObj.setPath(fileWithoutSharedStorage);

							//size
							List<FileInfo> fileInfos = FileHelper.listFilesInSharedStorageWithInfo(file);
							for (FileInfo info : fileInfos) {
								if (info.getFilePath().equals(file)) {
									long size = info.getSize();
									fileObj.setSize(new Long(size));
								}
							}

							//emis
							*//*if (systemUuid.toString().equalsIgnoreCase("991a9068-01d3-4ff2-86ed-249bd0541fb3") //live
									|| systemUuid.toString().equalsIgnoreCase("55c08fa5-ef1e-4e94-aadc-e3d6adc80774")) { //dev

								String name = FilenameUtils.getName(file);
								String[] toks = name.split("_");

								String first = toks[1];
								String second = toks[2];
								fileObj.setType(first + "_" + second);

							}*//*

							//Barts Cerner
							*//*if (systemUuid.toString().equalsIgnoreCase("e517fa69-348a-45e9-a113-d9b59ad13095") //live
								|| systemUuid.toString().equalsIgnoreCase("b0277098-0b6c-4d9d-86ef-5f399fb25f34")) { //dev

								String name = FilenameUtils.getName(file);
								if (Strings.isNullOrEmpty(name)) {
									continue;
								}
								try {
									String type = BartsCsvToFhirTransformer.identifyFileType(name);
									fileObj.setType(type);
								} catch (Exception ex2) {
									throw new Exception("Failed to parse file name " + name + " on exchange " + exchange.getId());
								}
							}*//*

							//Vision
							if (systemUuid.toString().equalsIgnoreCase("4809b277-6b8d-4e5c-be9c-d1f1d62975c6") //live
									|| systemUuid.toString().equalsIgnoreCase("bdb30ed3-8fe4-4436-8d5a-dabcda938d3a")) { //dev

								String name = FilenameUtils.getName(file);
								if (name.contains("_encounter_data_extract")) {
									fileObj.setType("encounter_data_extract");

								} else if (name.contains("journal_data_extract")) {
									fileObj.setType("journal_data_extract");

								} else if (name.contains("patient_data_extract")) {
									fileObj.setType("patient_data_extract");

								} else if (name.contains("staff_data_extract")) {
									fileObj.setType("staff_data_extract");

								} else if (name.contains("referral_data_extract")) {
									fileObj.setType("referral_data_extract");

								} else if (name.contains("practice_data_extract")) {
									fileObj.setType("practice_data_extract");

								} else if (name.contains("patient_check_sum")) {
									fileObj.setType("patient_check_sum_data_extract");

								} else if (name.contains("active_user_data_extract")) {
									fileObj.setType("active_user_data_extract");

								} else {
									throw new Exception("Unknown Vision file type for [" + name + "]");
								}
							}

							//Homerton
							if (systemUuid.toString().equalsIgnoreCase("bf9a81ff-e167-4d80-a6e8-884f573ebce2")) { //dev only

								//only patient files present
								fileObj.setType("PATIENT");
							}

							if (fileObj.getType() == null) {
								throw new Exception("Unhandled system ID " + systemUuid);
							}

							newFiles.add(fileObj);
						}

						String json = JsonSerializer.serialize(newFiles);
						exchange.setBody(json);

						LOG.debug("Fixing exchange " + exchange.getId());
						exchangeDal.save(exchange);
					}
				}
			}

			LOG.info("Finished Converting exchange bodies to JSON");
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

			*/

	/**
	 * For each practice:
	 * Go through all files processed since 14 March
	 * Cache all links as above
	 * Cache all Encounters saved too
	 * <p>
	 * For each Encounter referenced at all:
	 * Retrieve latest version from resource current
	 * Retrieve version prior to 14 March
	 * Update current version with old references plus new ones
	 * <p>
	 * For each parent observation:
	 * Retrieve latest version (could be observation or diagnostic report)
	 * <p>
	 * For each problem:
	 * Retrieve latest version from resource current
	 * Check if still a problem:
	 * Retrieve version prior to 14 March
	 * Update current version with old references plus new ones
	 *//*

			LOG.info("Finished Fixing encounters from " + table);
		} catch (Throwable t) {
			LOG.error("", t);
		}
	}*/
	/*private static void saveResourceWrapper(UUID serviceId, ResourceWrapper wrapper) throws Exception {

		if (true) {
			throw new Exception("This function needs updating to deal with the resource_current triggers = can't directly update resource_history");
		}

		if (wrapper.getVersion() == null) {
			throw new Exception("Can't update resource history without version UUID");
		}

		if (wrapper.getResourceData() != null) {
			long checksum = FhirStorageService.generateChecksum(wrapper.getResourceData());
			wrapper.setResourceChecksum(new Long(checksum));
		}

		EntityManager entityManager = ConnectionManager.getEhrEntityManager(serviceId);
		SessionImpl session = (SessionImpl) entityManager.getDelegate();
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
	}*/

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
			PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

			LOG.info("Found " + patientIds.size() + " to do");

			for (int i=0; i<patientIds.size(); i++) {

				String patientIdStr = patientIds.get(i);
				UUID patientId = UUID.fromString(patientIdStr);
				String serviceIdStr = serviceIds.get(patientIdStr);
				UUID serviceId = UUID.fromString(serviceIdStr);

				Patient patient = (Patient)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientIdStr);
				if (patient != null) {
					LOG.debug("Updating for patient " + patientIdStr);
					patientSearchDal.update(serviceId, patient);
					LOG.debug("Done");
				} else {

					List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Patient.toString(), patientId);
					if (history.isEmpty()) {
						LOG.debug("No history found for patient " + patientIdStr);
					} else {
						ResourceWrapper first = history.get(0);
						if (!first.isDeleted()) {
							throw new Exception("Resource current null for " + ResourceType.Patient + " " + patientIdStr + " but not deleted in resource_history");
						}

						//find first non-deleted instance and update for it, then delete
						for (ResourceWrapper historyItem: history) {
							if (!historyItem.isDeleted()) {
								patient = (Patient)FhirSerializationHelper.deserializeResource(historyItem.getResourceData());
								LOG.debug("Patient is deleted, so updating for deleted patient " + patientIdStr);
								patientSearchDal.update(serviceId, patient);
								patientSearchDal.deletePatient(serviceId, patient);
								LOG.debug("Done");
								break;
							}
						}
					}
				}



				//find episode of care
				//note, we don't have any current way to retrieve deleted episodes of care for a patient, so can only do this for non-deleted ones
				List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.EpisodeOfCare.toString());
				for (ResourceWrapper wrapper: wrappers) {
					if (!wrapper.isDeleted()) {
						LOG.debug("Updating for episodeOfCare resource " + wrapper.getResourceId());
						EpisodeOfCare episodeOfCare = (EpisodeOfCare)FhirSerializationHelper.deserializeResource(wrapper.getResourceData());
						patientSearchDal.update(serviceId, episodeOfCare);
						LOG.debug("Done");
					} else {
						LOG.debug("EpisodeOfCare " + wrapper.getResourceId() + " is deleted");
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

	/*private static void createBartsSubset(String sourceDir, UUID serviceUuid, UUID systemUuid, String samplePatientsFile) {
		LOG.info("Creating Barts Subset");

		try {

			Set<String> personIds = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
			for (String line : lines) {
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
	}*/

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

	/*private static void createBartsSubsetForFile(String sourceDir, UUID serviceUuid, UUID systemUuid, Set<String> personIds) throws Exception {

		ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
		List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceUuid, systemUuid, Integer.MAX_VALUE);

		for (Exchange exchange : exchanges) {

			List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());

			for (ExchangePayloadFile fileObj : files) {

				String filePathWithoutSharedStorage = fileObj.getPath().substring(TransformConfig.instance().getSharedStoragePath().length() + 1);
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
	}*/

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

	/*private static void applyEmisAdminCaches() {
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
	}*/

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
	/*private static void fixDisabledEmisExtract(String serviceOdsCode, String systemId, String sharedStoragePath, String tempDirParent) {

		LOG.info("Fixing Disabled Emis Extracts Prior to Re-bulk for service " + serviceOdsCode);
		try {

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			Service service = serviceDal.getByLocalIdentifier(serviceOdsCode);
			LOG.info("Service " + service.getId() + " " + service.getName() + " " + service.getLocalId());

			*//*File tempDirLast = new File(tempDir, "last");
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
			}*//*

			String tempDir = FilenameUtils.concat(tempDirParent, serviceOdsCode);

			File f = new File(tempDir);
			if (f.exists()) {
				FileUtils.deleteDirectory(f);
			}

			UUID serviceUuid = service.getId();
			UUID systemUuid = UUID.fromString(systemId);
			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

			//get all the exchanges, which are returned in reverse order, most recent first
			List<Exchange> exchangesDesc = exchangeDal.getExchangesByService(serviceUuid, systemUuid, Integer.MAX_VALUE);

			Map<Exchange, List<String>> hmExchangeFiles = new HashMap<>();
			Map<Exchange, List<String>> hmExchangeFilesWithoutStoragePrefix = new HashMap<>();

			//reverse the exchange list and cache the files for each one
			List<Exchange> exchanges = new ArrayList<>();

			for (int i = exchangesDesc.size() - 1; i >= 0; i--) {
				Exchange exchange = exchangesDesc.get(i);

				String exchangeBody = exchange.getBody();
				String[] files = ExchangeHelper.parseExchangeBodyOldWay(exchangeBody);

				//drop out and ignore any exchanges containing the singular bespoke reg status files
				if (files.length <= 1) {
					continue;
				}

				//drop out and ignore any exchanges for the left and dead extracts, since we don't
				//expect to receive re-bulked data for the dead patients
				String firstFile = files[0];
				if (firstFile.indexOf("LEFT_AND_DEAD") > -1) {
					continue;
				}

				exchanges.add(exchange);

				//populate the map of the files with the shared storage prefix
				List<String> fileList = Lists.newArrayList(files);
				hmExchangeFiles.put(exchange, fileList);

				//populate a map of the same files without the prefix
				files = ExchangeHelper.parseExchangeBodyOldWay(exchangeBody);
				for (int j = 0; j < files.length; j++) {
					String file = files[j].substring(sharedStoragePath.length() + 1);
					files[j] = file;
				}
				fileList = Lists.newArrayList(files);
				hmExchangeFilesWithoutStoragePrefix.put(exchange, fileList);
			}

			*//*exchanges.sort((o1, o2) -> {
				Date d1 = o1.getTimestamp();
				Date d2 = o2.getTimestamp();
				return d1.compareTo(d2);
			});*//*

			LOG.info("Found " + exchanges.size() + " exchanges and cached their files");

			int indexDisabled = -1;
			int indexRebulked = -1;
			int indexOriginallyBulked = -1;

			//go back through them to find the extract where the re-bulk is and when it was disabled (the list is in date order, so we're iterating most-recent first)
			for (int i = exchanges.size() - 1; i >= 0; i--) {
				Exchange exchange = exchanges.get(i);

				List<String> files = hmExchangeFiles.get(exchange);
				boolean disabled = isDisabledInSharingAgreementFile(files);

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
			for (int i = indexDisabled - 1; i >= 0; i--) {
				Exchange exchange = exchanges.get(i);

				List<String> files = hmExchangeFiles.get(exchange);
				boolean disabled = isDisabledInSharingAgreementFile(files);
				if (disabled) {
					break;
				}

				indexOriginallyBulked = i;
			}

			if (indexOriginallyBulked > -1) {
				Exchange exchangeOriginallyBulked = exchanges.get(indexOriginallyBulked);
				LOG.info("Originally bulked on " + findExtractDate(exchangeOriginallyBulked, hmExchangeFiles) + " " + exchangeOriginallyBulked.getId());
			}

			if (indexDisabled > -1) {
				Exchange exchangeDisabled = exchanges.get(indexDisabled);
				LOG.info("Disabled on " + findExtractDate(exchangeDisabled, hmExchangeFiles) + " " + exchangeDisabled.getId());
			}

			if (indexRebulked > -1) {
				Exchange exchangeRebulked = exchanges.get(indexRebulked);
				LOG.info("Rebulked on " + findExtractDate(exchangeRebulked, hmExchangeFiles) + " " + exchangeRebulked.getId());
			}

			if (indexDisabled == -1
					|| indexRebulked == -1
					|| indexOriginallyBulked == -1) {
				throw new Exception("Failed to find exchanges for original bulk (" + indexOriginallyBulked + ") disabling (" + indexDisabled + ") or re-bulking (" + indexRebulked + ")");
			}

			//continueOrQuit();

			Exchange exchangeRebulked = exchanges.get(indexRebulked);
			List<String> rebulkFiles = hmExchangeFiles.get(exchangeRebulked);

			List<String> tempFilesCreated = new ArrayList<>();

			Set<String> patientGuidsDeletedOrTooOld = new HashSet<>();

			for (String rebulkFile : rebulkFiles) {
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
				Exchange exchangeDisabled = exchanges.get(indexDisabled);
				List<String> disabledFiles = hmExchangeFilesWithoutStoragePrefix.get(exchangeDisabled);
				for (String s : disabledFiles) {
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
				for (int i = indexDisabled - 1; i >= indexOriginallyBulked; i--) {
					Exchange exchange = exchanges.get(i);

					String originalFile = null;

					List<String> files = hmExchangeFiles.get(exchange);

					for (String s : files) {
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

								Patient patientResource = (Patient) resourceDal.getCurrentVersionAsResource(serviceUuid, ResourceType.Patient, patientUuid.toString());
								if (patientResource.hasDeceased()) {
									patientGuidsDeletedOrTooOld.add(patientGuid);
									continue;
								}

								UUID episodeUuid = IdHelper.getEdsResourceId(serviceUuid, ResourceType.EpisodeOfCare, patientGuid); //we use the patient GUID for the episode too
								EpisodeOfCare episodeResource = (EpisodeOfCare) resourceDal.getCurrentVersionAsResource(serviceUuid, ResourceType.EpisodeOfCare, episodeUuid.toString());
								if (episodeResource.hasPeriod()
										&& !PeriodHelper.isActive(episodeResource.getPeriod())) {

									patientGuidsDeletedOrTooOld.add(patientGuid);
									continue;
								}
							}

							//create a new CSV record, carrying over the GUIDs from the original but marking as deleted
							String[] newRecord = new String[headers.length];

							for (int j = 0; j < newRecord.length; j++) {
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

							csvPrinter.printRecord((Object[]) newRecord);
							csvPrinter.flush();

							//log out the raw record that's missing from the original
							StringBuffer sb = new StringBuffer();
							sb.append("Record not in re-bulk: ");
							for (int j = 0; j < record.size(); j++) {
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
				for (int i = indexDisabled + 1; i < indexRebulked; i++) {
					Exchange ex = exchanges.get(i);
					List<String> exchangeFiles = hmExchangeFilesWithoutStoragePrefix.get(ex);

					for (String s : exchangeFiles) {
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
			for (String s : rebulkFiles) {
				String fileType = findFileType(s);
				if (fileType.equals("Agreements_SharingOrganisation")) {
					rebulkedSharingAgreementFile = s;
				}
			}

			for (int i = indexDisabled; i < indexRebulked; i++) {
				Exchange ex = exchanges.get(i);
				List<String> exchangeFiles = hmExchangeFilesWithoutStoragePrefix.get(ex);

				for (String s : exchangeFiles) {
					String exchangeFileType = findFileType(s);
					if (exchangeFileType.equals("Agreements_SharingOrganisation")) {

						String replacementFile = FilenameUtils.concat(tempDir, s);

						InputStream inputStream = FileHelper.readFileFromSharedStorage(rebulkedSharingAgreementFile);
						File replacementFileObj = new File(replacementFile);
						Files.copy(inputStream, replacementFileObj.toPath());
						inputStream.close();

						tempFilesCreated.add(s);
					}
				}
			}

			//create a script to copy the files into S3
			List<String> copyScript = new ArrayList<>();
			copyScript.add("#!/bin/bash");
			copyScript.add("");
			for (String s : tempFilesCreated) {
				String localFile = FilenameUtils.concat(tempDir, s);
				copyScript.add("sudo aws s3 cp " + localFile + " s3://discoverysftplanding/endeavour/" + s);
			}

			String scriptFile = FilenameUtils.concat(tempDir, "copy.sh");
			FileUtils.writeLines(new File(scriptFile), copyScript);

			LOG.info("Finished - written files to " + tempDir);

			dumpFileSizes(new File(tempDir));

			*//*continueOrQuit();

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
			}*//*

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

	/*private static void dumpFileSizes(File f) {
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				dumpFileSizes(child);
			}
		} else {
			String totalSizeReadable = FileUtils.byteCountToDisplaySize(f.length());
			LOG.info("" + f + " = " + totalSizeReadable);
		}
	}*/

	/*private static String findExtractDate(Exchange exchange, Map<Exchange, List<String>> fileMap) throws Exception {
		List<String> files = fileMap.get(exchange);
		String file = findSharingAgreementFile(files);
		String name = FilenameUtils.getBaseName(file);
		String[] toks = name.split("_");
		return toks[3];
	}*/

	private static boolean isDisabledInSharingAgreementFile(List<String> files) throws Exception {
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
		char c = (char) bytes[0];
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


	/*private static void testSlack() {
		LOG.info("Testing slack");

		try {
			SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, "Test Message from Queue Reader");
			LOG.info("Finished testing slack");

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}*/

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

	/*private static void fixPatientSearchAllServices(String filterSystemId) {
		LOG.info("Fixing patient search for all services and system " + filterSystemId);

		try {

			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			List<Service> services = serviceDal.getAll();
			for (Service service: services) {
				fixPatientSearch(service.getId().toString(), filterSystemId);
			}

			LOG.info("Finished Fixing patient search for all services and system " + filterSystemId);

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void fixPatientSearch(String serviceId, String filterSystemId) {
		LOG.info("Fixing patient search for service " + serviceId);

		try {

			UUID serviceUuid = UUID.fromString(serviceId);

			UUID filterSystemUuid = null;
			if (!Strings.isNullOrEmpty(filterSystemId)) {
				filterSystemUuid = UUID.fromString(filterSystemId);
			}

			ExchangeDalI exchangeDalI = DalProvider.factoryExchangeDal();
			ExchangeBatchDalI exchangeBatchDalI = DalProvider.factoryExchangeBatchDal();
			ResourceDalI resourceDalI = DalProvider.factoryResourceDal();
			PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();

			Set<UUID> patientsDone = new HashSet<>();

			Service service = serviceDal.getById(serviceUuid);
			List<UUID> systemIds = findSystemIds(service);
			for (UUID systemId: systemIds) {

				if (filterSystemUuid != null
						&& !filterSystemUuid.equals(systemId)) {
					continue;
				}

				List<UUID> exchanges = exchangeDalI.getExchangeIdsForService(serviceUuid, systemId);
				LOG.info("Found " + exchanges.size() + " exchanges for system " + systemId);

				for (UUID exchangeId : exchanges) {
					List<ExchangeBatch> batches = exchangeBatchDalI.retrieveForExchangeId(exchangeId);
					LOG.info("Found " + batches.size() + " batches in exchange " + exchangeId);

					for (ExchangeBatch batch : batches) {
						UUID patientId = batch.getEdsPatientId();
						if (patientId == null) {
							continue;
						}

						if (patientsDone.contains(patientId)) {
							continue;
						}
						patientsDone.add(patientId);

						ResourceWrapper wrapper = resourceDalI.getCurrentVersion(serviceUuid, ResourceType.Patient.toString(), patientId);
						if (wrapper != null) {
							String json = wrapper.getResourceData();
							if (!Strings.isNullOrEmpty(json)) {

								Patient fhirPatient = (Patient)FhirSerializationHelper.deserializeResource(json);
								patientSearchDal.update(serviceUuid, fhirPatient);
							}
						}

						if (patientsDone.size() % 1000 == 0) {
							LOG.info("Done " + patientsDone.size());
						}
					}
				}
			}
			LOG.info("Done " + patientsDone.size());

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

			for (String sql : lines) {

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
	 * --USE EDS DATABASE
	 * <p>
	 * -- barts b5a08769-cbbe-4093-93d6-b696cd1da483
	 * -- homerton 962d6a9a-5950-47ac-9e16-ebee56f9507a
	 * <p>
	 * create table adt_patients (
	 * service_id character(36),
	 * system_id character(36),
	 * nhs_number character varying(10),
	 * patient_id character(36)
	 * );
	 * <p>
	 * -- delete from adt_patients;
	 * <p>
	 * select * from patient_search limit 10;
	 * select * from patient_link limit 10;
	 * <p>
	 * insert into adt_patients
	 * select distinct ps.service_id, ps.system_id, ps.nhs_number, ps.patient_id
	 * from patient_search ps
	 * join patient_link pl
	 * on pl.patient_id = ps.patient_id
	 * join patient_link pl2
	 * on pl.person_id = pl2.person_id
	 * join patient_search ps2
	 * on ps2.patient_id = pl2.patient_id
	 * where
	 * ps.service_id IN ('b5a08769-cbbe-4093-93d6-b696cd1da483', '962d6a9a-5950-47ac-9e16-ebee56f9507a')
	 * and ps2.service_id NOT IN ('b5a08769-cbbe-4093-93d6-b696cd1da483', '962d6a9a-5950-47ac-9e16-ebee56f9507a');
	 * <p>
	 * <p>
	 * select count(1) from adt_patients limit 100;
	 * select * from adt_patients limit 100;
	 * <p>
	 * <p>
	 * <p>
	 * <p>
	 * ---MOVE TABLE TO HL7 RECEIVER DB
	 * <p>
	 * select count(1) from adt_patients;
	 * <p>
	 * -- top 1000 patients with messages
	 * <p>
	 * select * from mapping.resource_uuid where resource_type = 'Patient' limit 10;
	 * <p>
	 * select * from log.message limit 10;
	 * <p>
	 * create table adt_patient_counts (
	 * nhs_number character varying(100),
	 * count int
	 * );
	 * <p>
	 * insert into adt_patient_counts
	 * select pid1, count(1)
	 * from log.message
	 * where pid1 is not null
	 * and pid1 <> ''
	 * group by pid1;
	 * <p>
	 * select * from adt_patient_counts order by count desc limit 100;
	 * <p>
	 * alter table adt_patients
	 * add count int;
	 * <p>
	 * update adt_patients
	 * set count = adt_patient_counts.count
	 * from adt_patient_counts
	 * where adt_patients.nhs_number = adt_patient_counts.nhs_number;
	 * <p>
	 * select count(1) from adt_patients where nhs_number is null;
	 * <p>
	 * select * from adt_patients
	 * where nhs_number is not null
	 * and count is not null
	 * order by count desc limit 1000;
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
	/*private static void findEmisStartDates(String path, String outputPath) {
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
			for (File sftpRoot : root.listFiles()) {
				LOG.info("Checking " + sftpRoot);

				Map<Date, File> extracts = new HashMap<>();
				List<Date> extractDates = new ArrayList<>();

				for (File extractRoot : sftpRoot.listFiles()) {
					Date d = sdf.parse(extractRoot.getName());

					//LOG.info("" + extractRoot.getName() + " -> " + d);

					extracts.put(d, extractRoot);
					extractDates.add(d);
				}

				Collections.sort(extractDates);

				for (Date extractDate : extractDates) {
					File extractRoot = extracts.get(extractDate);
					LOG.info("Checking " + extractRoot);

					//read the sharing agreements file
					//e.g. 291_Agreements_SharingOrganisation_20150211164536_45E7CD20-EE37-41AB-90D6-DC9D4B03D102.csv
					File sharingAgreementsFile = null;
					for (File f : extractRoot.listFiles()) {
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
					for (File f : extractRoot.listFiles()) {
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
					for (File f : extractRoot.listFiles()) {
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

			for (String orgGuid : startDates.keySet()) {
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
			for (File readerRoot : files) { //emis001
				LOG.info("Finding terms in " + readerRoot);

				//first read in all the coding files to build up our map of codes
				Map<String, String> hmCodes = new HashMap<>();

				for (File dateFolder : readerRoot.listFiles()) {
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
				for (File dateFolder : readerRoot.listFiles()) {
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

			for (String line : hmResults.keySet()) {
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
		for (File f : root.listFiles()) {
			String s = f.getName();
			if (s.indexOf(token) > -1) {
				return f;
			}
		}

		return null;
	}*/

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
		for (ExchangeTransformAudit audit : audits) {
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

		List<ServiceInterfaceEndpoint> endpoints = null;
		try {
			endpoints = service.getEndpointsList();
			for (ServiceInterfaceEndpoint endpoint : endpoints) {
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

	/*private static void fixSlotReferencesForPublisher(String publisher) {
		try {
			ServiceDalI dal = DalProvider.factoryServiceDal();
			List<Service> services = dal.getAll();
			for (Service service: services) {
				if (service.getPublisherConfigName() != null
						&& service.getPublisherConfigName().equals(publisher)) {

					fixSlotReferences(service.getId());
				}
			}

		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}

	private static void fixSlotReferences(UUID serviceId) {
		LOG.info("Fixing Slot References in Appointments for " + serviceId);
		try {
			//get patient IDs from patient search
			List<UUID> patientIds = new ArrayList<>();


			EntityManager entityManager = ConnectionManager.getPublisherTransformEntityManager(serviceId);
			SessionImpl session = (SessionImpl) entityManager.getDelegate();
			Connection connection = session.connection();
			Statement statement = connection.createStatement();

			String sql = "SELECT eds_id FROM resource_id_map WHERE service_id = '" + serviceId + "' AND resource_type = '" + ResourceType.Patient + "';";
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				String patientUuid = rs.getString(1);
				patientIds.add(UUID.fromString(patientUuid));
			}
			rs.close();
			statement.close();
			connection.close();

			LOG.debug("Found " + patientIds.size() + " patients");
			int done = 0;
			int fixed = 0;

			ResourceDalI resourceDal = DalProvider.factoryResourceDal();

			EmisCsvHelper csvHelper = new EmisCsvHelper(serviceId, null, null, null, true, null);

			//for each patient
			for (UUID patientUuid: patientIds) {
				//LOG.debug("Checking patient " + patientUuid);

				//get all appointment resources
				List<ResourceWrapper> appointmentWrappers = resourceDal.getResourcesByPatient(serviceId, patientUuid, ResourceType.Appointment.toString());
				for (ResourceWrapper apptWrapper: appointmentWrappers) {
					//LOG.debug("Checking appointment " + apptWrapper.getResourceId());

					List<ResourceWrapper> historyWrappers = resourceDal.getResourceHistory(serviceId, apptWrapper.getResourceType(), apptWrapper.getResourceId());

					//the above returns most recent first, but we want to do them in order
					historyWrappers = Lists.reverse(historyWrappers);

					for (ResourceWrapper historyWrapper : historyWrappers) {

						if (historyWrapper.isDeleted()) {
							//LOG.debug("Appointment " + historyWrapper.getResourceId() + " is deleted");
							continue;
						}

						String json = historyWrapper.getResourceData();
						Appointment appt = (Appointment) FhirSerializationHelper.deserializeResource(json);
						if (!appt.hasSlot()) {
							//LOG.debug("Appointment " + historyWrapper.getResourceId() + " has no slot");
							continue;
						}

						if (appt.getSlot().size() != 1) {
							throw new Exception("Appointment " + appt.getId() + " has " + appt.getSlot().size() + " slot refs");
						}

						Reference slotRef = appt.getSlot().get(0);

						//test if slot reference exists
						Reference slotLocalRef = IdHelper.convertEdsReferenceToLocallyUniqueReference(csvHelper, slotRef);
						String slotSourceId = ReferenceHelper.getReferenceId(slotLocalRef);
						if (slotSourceId.indexOf(":") > -1) {
							//LOG.debug("Appointment " + historyWrapper.getResourceId() + " has a valid slot");
							continue;
						}

						//if not, correct slot reference
						Reference apptEdsReference = ReferenceHelper.createReference(appt.getResourceType(), appt.getId());
						Reference apptLocalReference = IdHelper.convertEdsReferenceToLocallyUniqueReference(csvHelper, apptEdsReference);
						String sourceId = ReferenceHelper.getReferenceId(apptLocalReference);
						Reference slotLocalReference = ReferenceHelper.createReference(ResourceType.Slot, sourceId);
						Reference slotEdsReference = IdHelper.convertLocallyUniqueReferenceToEdsReference(slotLocalReference, csvHelper);
						String slotEdsReferenceValue = slotEdsReference.getReference();

						String oldSlotRefValue = slotRef.getReference();
						slotRef.setReference(slotEdsReferenceValue);
						//LOG.debug("Appointment " + historyWrapper.getResourceId() + " slot ref changed from " + oldSlotRefValue + " to " + slotEdsReferenceValue);

						//save appointment
						json = FhirSerializationHelper.serializeResource(appt);
						historyWrapper.setResourceData(json);
						saveResourceWrapper(serviceId, historyWrapper);

						fixed++;
					}
				}

				done ++;
				if (done % 1000 == 0) {
					LOG.debug("Done " + done + " / " + patientIds.size() + " and fixed " + fixed + " appts");
				}
			}

			LOG.debug("Done " + done + " / " + patientIds.size() + " and fixed " + fixed + " appts");
			LOG.info("Finished Fixing Slot References in Appointments for " + serviceId);
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

	private static void createEmisSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
		LOG.info("Creating Emis Subset");

		try {

			Set<String> patientGuids = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
			for (String line : lines) {
				line = line.trim();

				//ignore comments
				if (line.startsWith("#")) {
					continue;
				}
				patientGuids.add(line);
			}

			File sourceDir = new File(sourceDirPath);
			File destDir = new File(destDirPath);

			if (!destDir.exists()) {
				destDir.mkdirs();
			}

			createEmisSubsetForFile(sourceDir, destDir, patientGuids);

			LOG.info("Finished Creating Emis Subset");

		} catch (Throwable t) {
			LOG.error("", t);
		}
	}

	private static void createEmisSubsetForFile(File sourceDir, File destDir, Set<String> patientGuids) throws Exception {

		File[] files = sourceDir.listFiles();
		LOG.info("Found " + files.length + " files in " + sourceDir);

		for (File sourceFile : files) {

			String name = sourceFile.getName();
			File destFile = new File(destDir, name);

			if (sourceFile.isDirectory()) {

				if (!destFile.exists()) {
					destFile.mkdirs();
				}

				createEmisSubsetForFile(sourceFile, destFile, patientGuids);

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

				CSVFormat format = CSVFormat.DEFAULT.withHeader();

				InputStreamReader reader = new InputStreamReader(
						new BufferedInputStream(
								new FileInputStream(sourceFile)));

				CSVParser parser = new CSVParser(reader, format);

				String filterColumn = null;

				Map<String, Integer> headerMap = parser.getHeaderMap();
				if (headerMap.containsKey("PatientGuid")) {
					filterColumn = "PatientGuid";

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
										new FileOutputStream(destFile)));

				CSVPrinter printer = new CSVPrinter(bw, format.withHeader(columnHeaders));

				Iterator<CSVRecord> csvIterator = parser.iterator();
				while (csvIterator.hasNext()) {
					CSVRecord csvRecord = csvIterator.next();

					String patientGuid = csvRecord.get(filterColumn);
					if (Strings.isNullOrEmpty(patientGuid) //if empty, carry over this record
							|| patientGuids.contains(patientGuid)) {

						printer.printRecord(csvRecord);
						printer.flush();
					}
				}

				parser.close();
				printer.close();
			}
		}
	}

	private static void createTppSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
		LOG.info("Creating TPP Subset");

		try {

			Set<String> personIds = new HashSet<>();
			List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
			for (String line : lines) {
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

		for (File sourceFile : files) {

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
			for (String line : lines) {
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

		for (File sourceFile : files) {

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
			for (String line : lines) {
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

		for (File sourceFile : files) {

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
			for (String line : lines) {
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

		for (File sourceFile : files) {

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

	/*private static void exportFhirToCsv(UUID serviceId, String destinationPath) {
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
	}*/

	/*private static void fixTPPNullOrgs(String sourceDir, String orgODS) throws Exception {

		final String COLUMN_ORG = "IDOrganisationVisibleTo";

		File[] files = new File(sourceDir).listFiles();

		if (files == null)
			return;

		LOG.info("Found " + files.length + " files in " + sourceDir);

		for (File sourceFile : files) {

			String sourceFileName = sourceFile.getName();

			if (sourceFile.isDirectory()) {

				fixTPPNullOrgs(sourceFileName, orgODS);

			} else {

				LOG.info("Checking file " + sourceFile);

				//skip any non-CSV file
				String ext = FilenameUtils.getExtension(sourceFileName);
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

				Map<String, Integer> headerMap = parser.getHeaderMap();
				if (!headerMap.containsKey(COLUMN_ORG)) {

					//if no COLUMN_ORG column, ignore
					LOG.info("Ignoring file with no " + COLUMN_ORG + " column: " + sourceFile);
					parser.close();

					continue;
				}

				String[] columnHeaders = new String[headerMap.size()];
				Iterator<String> headerIterator = headerMap.keySet().iterator();
				while (headerIterator.hasNext()) {
					String headerName = headerIterator.next();
					int headerIndex = headerMap.get(headerName);
					columnHeaders[headerIndex] = headerName;
				}

				String destFileName = sourceFileName.concat(".FIXED");

				BufferedWriter bw =
						new BufferedWriter(
								new OutputStreamWriter(
										new FileOutputStream(destFileName), encoding));

				CSVPrinter printer = new CSVPrinter(bw, format.withHeader(columnHeaders));

				//iterate down the file and look at Org Column
				Iterator<CSVRecord> csvIterator = parser.iterator();
				while (csvIterator.hasNext()) {
					CSVRecord csvRecord = csvIterator.next();

					String fileOrgODS = csvRecord.get(COLUMN_ORG);

					//set the empty value to that orgODS value passed in
					if (Strings.isNullOrEmpty(fileOrgODS)) {

						Map <String, String> recordMap = csvRecord.toMap();
						recordMap.put(COLUMN_ORG, String.valueOf(orgODS));
						List<String> alteredCsvRecord = new ArrayList<String>();
						for (String key : columnHeaders) {
							alteredCsvRecord.add(recordMap.get(key));
						}

						printer.printRecord(alteredCsvRecord);
						printer.flush();

					} else {

						if (!fileOrgODS.equalsIgnoreCase(orgODS)) {
							parser.close();
							printer.flush();
							printer.close();

							throw new Exception("File contains different ODS codes to parameter value - aborting");
						}

						//write the record back unchanged
						printer.printRecord(csvRecord);
						printer.flush();
					}
				}

				parser.close();
				printer.close();

				//Finally, delete source file and rename the fixed destination file back to source
				sourceFile.delete();
				new File (destFileName).renameTo(new File (sourceFileName));
			}
		}
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

/*
class MoveToS3Runnable implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(MoveToS3Runnable.class);

	private List<FileInfo> files = null;
	private AtomicInteger done = null;

	public MoveToS3Runnable(List<FileInfo> files, AtomicInteger done) {
		this.files = files;
		this.done = done;
	}

	@Override
	public void run() {
		try {
			doWork();
		} catch (Exception ex) {
			LOG.error("", ex);
		}
	}

	private void doWork() throws Exception {

		SourceFileMappingDalI db = DalProvider.factorySourceFileMappingDal();

		//write to database
		//Map<ResourceWrapper, ResourceFieldMappingAudit> batch = new HashMap<>();
		for (FileInfo info: files) {

			String path = info.getFilePath();

			InputStream inputStream = FileHelper.readFileFromSharedStorage(path);
			ZipInputStream zis = new ZipInputStream(inputStream);

			ZipEntry entry = zis.getNextEntry();
			if (entry == null) {
				throw new Exception("No entry in zip file " + path);
			}
			byte[] entryBytes = IOUtils.toByteArray(zis);
			String json = new String(entryBytes);

			inputStream.close();

			ResourceFieldMappingAudit audit = ResourceFieldMappingAudit.readFromJson(json);

			ResourceWrapper wrapper = new ResourceWrapper();

			String versionStr = FilenameUtils.getBaseName(path);
			wrapper.setVersion(UUID.fromString(versionStr));

			Date d = info.getLastModified();
			wrapper.setCreatedAt(d);

			File f = new File(path);
			f = f.getParentFile();
			String resourceIdStr = f.getName();
			wrapper.setResourceId(UUID.fromString(resourceIdStr));

			f = f.getParentFile();
			String resourceTypeStr = f.getName();
			wrapper.setResourceType(resourceTypeStr);

			f = f.getParentFile();
			String serviceIdStr = f.getName();
			wrapper.setServiceId(UUID.fromString(serviceIdStr));

			Map<ResourceWrapper, ResourceFieldMappingAudit> batch = new HashMap<>();
			batch.put(wrapper, audit);

			try {
				db.saveResourceMappings(batch);
			} catch (Exception ex) {
				String msg = ex.getMessage();
				if (msg.indexOf("Duplicate entry") == -1) {
					throw ex;
				}
			}


			*/
/*if (batch.size() > 5) {
				db.saveResourceMappings(batch);
				batch.clear();
			}*//*


			int nowDone = done.incrementAndGet();
			if (nowDone % 1000 == 0) {
				LOG.debug("Done " + nowDone + " / " + files.size());
			}
		}

		*/
/*if (!batch.isEmpty()) {
			db.saveResourceMappings(batch);
			batch.clear();
		}*//*


	}
}*/


class PopulateDataDateCallable implements Callable {
	private static final Logger LOG = LoggerFactory.getLogger(PopulateDataDateCallable.class);

	private static ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

	private UUID exchangeId = null;
	private AtomicInteger fixed = null;

	public PopulateDataDateCallable(UUID exchangeId, AtomicInteger fixed) {
		this.exchangeId = exchangeId;
		this.fixed = fixed;
	}


	private void doWork() throws Exception {

		Exchange exchange = exchangeDal.getExchange(exchangeId);

		//check if already done
		String existingVal = exchange.getHeader(HeaderKeys.DataDate);
		String software = exchange.getHeader(HeaderKeys.SourceSystem);
		String version = exchange.getHeader(HeaderKeys.SystemVersion);

		if (!Strings.isNullOrEmpty(existingVal)) {
			LOG.info("Already done exchange " + exchange.getId() + " software " + software + " version " + version);
			markAsDone();
			return;
		}

		String body = exchange.getBody();

		if (body.equals("[]")) {
			LOG.error("Empty body found in exchange " + exchange.getId() + " software " + software + " version " + version);
			markAsDone();
			return;
		}

		Date lastDataDate = OpenEnvelope.calculateLastDataDate(software, version, body);
		if (lastDataDate == null) {
			LOG.error("Failed to calculate data for exchange " + exchange.getId() + " software " + software + " version " + version);
			markAsDone();
			return;
		}

		exchange.setHeaderAsDate(HeaderKeys.DataDate, lastDataDate);

		exchangeDal.save(exchange);

		//mark as done
		markAsDone();

		fixed.incrementAndGet();
	}

	private void markAsDone() throws Exception {
		EntityManager auditEntityManager = ConnectionManager.getAuditEntityManager();

		auditEntityManager.getTransaction().begin();

		SessionImpl auditSession = (SessionImpl)auditEntityManager.getDelegate();
		Connection auditConnection = auditSession.connection();

		String sql = "UPDATE drewtest.exchange_ids SET done = 1 WHERE id = ?";
		PreparedStatement ps = auditConnection.prepareStatement(sql);
		ps.setString(1, exchangeId.toString());

		ps.executeUpdate();

		auditEntityManager.getTransaction().commit();

		ps.close();
		auditEntityManager.close();
		//LOG.debug("Marked as done using: " + sql);
	}


	@Override
	public Object call() throws Exception {
		try {
			doWork();
		} catch (Throwable ex) {
			LOG.error("Error with " + exchangeId, ex);
			markAsDone(); //get out of the way
		}
		return null;
	}
}


/*
class TestRabbitConsumer extends DefaultConsumer {
	private static final Logger LOG = LoggerFactory.getLogger(TestRabbitConsumer.class);

	public TestRabbitConsumer(Channel channel) {
		super(channel);
	}


	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws IOException {

		long deliveryTag = envelope.getDeliveryTag();

		String bodyStr = new String(bytes, "UTF-8");
		LOG.info("Received exchange body: " + bodyStr);
		try {
			Thread.sleep(1000);
		} catch (Throwable t) {
			LOG.error("", t);
		}

		this.getChannel().basicAck(deliveryTag, false);

	}
}*/
