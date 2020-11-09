package org.endeavourhealth.queuereader;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.configuration.ConfigDeserialiser;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.queuereader.routines.*;
import org.endeavourhealth.transform.common.TransformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		String configId = args[0];
		LOG.info("Initialising config manager");
		ConfigManager.initialize("queuereader", configId);


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("findExchangesNotSentToSubscriber")) {
			boolean onlySkipCompletedOnes = Boolean.parseBoolean(args[1]);
			String orgOdsCodeRegex = null;
			if (args.length > 2) {
				orgOdsCodeRegex = args[2];
			}
			SD156.findExchangesNotSentToSubscriber(onlySkipCompletedOnes, orgOdsCodeRegex);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("testInformationModelFromJson")) {
			String filePath = args[1];
			SpecialRoutines.testInformationModelFromJson(filePath);
			System.exit(0);
		}*/


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("populatePatientSearchFields")) {
			SpecialRoutines.populatePatientSearchFields();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("testUprnToken")) {
			SpecialRoutines.testUprnToken(args[1]);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("testNewFrailtySql")) {
			SpecialRoutines.testNewFrailtySql();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("populateSubscriberAuditTables")) {
			SpecialRoutines.populateSubscriberAuditTables();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("findPatientsWithConfidentialData")) {

			String orgOdsCodeRegex = null;
			if (args.length > 1) {
				orgOdsCodeRegex = args[1];
			}
			SpecialRoutines.findPatientsWithConfidentialData(orgOdsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("fixEmisAppointmentsAt12")) {

			String orgOdsCodeRegex = null;
			if (args.length > 1) {
				orgOdsCodeRegex = args[1];
			}
			SpecialRoutines.fixEmisAppointmentsAt12(orgOdsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("fixAppointmentTimes")) {

			String subscriberConfigName = args[1];
			String orgOdsCodeRegex = null;
			if (args.length > 2) {
				orgOdsCodeRegex = args[2];
			}
			SpecialRoutines.fixAppointmentTimesInCompass(subscriberConfigName, orgOdsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("quickRefreshForAllTpp")) {

			String orgOdsCodeRegex = null;
			if (args.length > 1) {
				orgOdsCodeRegex = args[1];
			}

			SpecialRoutines.quickRefreshForAllTpp(orgOdsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("quickRefreshForAllEmis")) {

			String orgOdsCodeRegex = null;
			if (args.length > 1) {
				orgOdsCodeRegex = args[1];
			}

			SpecialRoutines.quickRefreshForAllEmis(orgOdsCodeRegex);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("fixEmisEpisodesChangingDate")) {

			boolean testMode = Boolean.parseBoolean(args[1]);
			String orgOdsCodeRegex = null;
			if (args.length > 2) {
				orgOdsCodeRegex = args[2];
			}

			SD99.fixEmisEpisodesChangingDate(testMode, orgOdsCodeRegex);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("findEmisEpisodesChangingDate")) {

			String orgOdsCodeRegex = null;
			if (args.length > 1) {
				orgOdsCodeRegex = args[1];
			}

			SD99.findEmisEpisodesChangingDate(orgOdsCodeRegex);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("fixTppMissingPractitioners")) {
			boolean onlySkipCompletedOnes = Boolean.parseBoolean(args[1]);
			String orgOdsCodeRegex = null;
			if (args.length > 2) {
				orgOdsCodeRegex = args[2];
			}
			//SD86.testLookupTiming();
			//SD86.fixTppMissingPractitioners(onlySkipCompletedOnes, orgOdsCodeRegex);
			SD86.requeueFailedExchanges();
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("populateCompassPatientPseudoIdTable")) {

			String sourceSubscriberConfigName = args[1];
			String orgOdsCodeRegex = null;
			if (args.length > 2) {
				orgOdsCodeRegex = args[2];
			}

			SpecialRoutines.populateCompassPatientPseudoIdTable(sourceSubscriberConfigName, orgOdsCodeRegex);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("deleteCore06DataFromSubscribers")) {

			boolean testMode = Boolean.parseBoolean(args[1]);
			String sourceSubscriberConfigName = args[2];
			String tableOfPatientIds = args[3];
			String tableForAudit = args[4];
			List<String> subscriberNames = new ArrayList<>();
			for (int i=5; i<args.length; i++) {
				subscriberNames.add(args[i]);
			}

			SpecialRoutines.deleteCore06DataFromSubscribers(testMode, sourceSubscriberConfigName, tableOfPatientIds, tableForAudit, subscriberNames);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("BulkSubscriberTransformAdmin")) {
			String reason = args[1];
			String odsCodes = null;
			if (args.length > 2) {
				odsCodes = args[2];
			}
			SpecialRoutines.bulkSubscriberTransformAdmin(reason, odsCodes);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("CountVaccinationCodes")) {
			String sinceDateStr = args[1];
			String ccgOdsCodes = null;
			if (args.length > 2) {
				ccgOdsCodes = args[2];
			}
			SpecialRoutines.countVaccinationCodes(sinceDateStr, ccgOdsCodes);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("ValidateProtocolCohorts")) {
			SpecialRoutines.validateProtocolCohorts();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestSubscriberConfigs")) {
			SpecialRoutines.testSubscriberConfigs();
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindTppServicesNeedReprocessing")) {
			boolean showLogging = Boolean.valueOf(args[1]);
			String odsCodeRegex = null;
			if (args.length > 2) {
				odsCodeRegex = args[2];
			}
			SpecialRoutines.findTppServicesNeedReprocessing(showLogging, odsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("DeleteResourcesForDeletedPatients")) {
			boolean testMode = Boolean.parseBoolean(args[1]);
			String odsCodeRegex = null;
			if (args.length > 2) {
				odsCodeRegex = args[2];
			}
			SpecialRoutines.deleteResourcesForDeletedPatients(testMode, odsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestHashedFileFilteringForSRCode")) {

			String filePath = args[1];
			String uniqueKey = args[2];
			String dataDateStr = args[3];
			SpecialRoutines.testHashedFileFilteringForSRCode(filePath, uniqueKey, dataDateStr);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateMissingOrgsInCompassV1")) {

			String subscriberConfigName = args[1];
			boolean testMode = Boolean.parseBoolean(args[2]);
			SpecialRoutines.populateMissingOrgsInCompassV1(subscriberConfigName, testMode);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateMissingOrgsInCompassV2")) {

			String subscriberConfigName = args[1];
			boolean testMode = Boolean.parseBoolean(args[2]);
			SpecialRoutines.populateMissingOrgsInCompassV2(subscriberConfigName, testMode);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("DeleteDataFromOldCoreDB")) {

			UUID serviceId = UUID.fromString(args[1]);
			String previousPublisherConfig = args[2];
			SpecialRoutines.deleteDataForOldCoreDBFromSubscribers(serviceId, previousPublisherConfig);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("DeleteTppEpisodesElsewhere")) {

			boolean testMode = Boolean.parseBoolean(args[1]);
			String odsCodeRegex = null;
			if (args.length > 2) {
				odsCodeRegex = args[2];
			}
			SpecialRoutines.deleteTppEpisodesElsewhere(odsCodeRegex, testMode);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindEmisServicesNeedReprocessing")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			SpecialRoutines.findEmisServicesNeedReprocessing(odsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TransformBartsEncounters")) {

			String odsCode = args[1];
			String tableName = args[2];
			SpecialRoutines.transformAdtEncounters(odsCode, tableName);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("CatptureBartsEncounters")) {
			Integer count = Integer.parseInt(args[1]);
			String toFile = args[2];
			SpecialRoutines.catptureBartsEncounters(count, toFile);
			System.exit(0);
		}*/

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

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestBulkLoad")) {
			String s3Path = args[1];
			String tableName = args[2];
			SpecialRoutines.testBulkLoad(s3Path, tableName);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestInformationModel")) {
			SpecialRoutines.testInformationModel();
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestInformationModelMapping")) {
			SpecialRoutines.testInformationModelMapping();
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("BreakUpAdminBatches")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			SpecialRoutines.breakUpAdminBatches(odsCodeRegex);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("GetJarDetails")) {
			SpecialRoutines.getJarDetails();
			System.exit(0);
		}*/

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

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("TestDSM")) {
			String odsCode = args[1];
			SpecialRoutines.testDsm(odsCode);
			System.exit(0);
		}*/

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("CompareDSM")) {
			boolean logDifferencesOnly = Boolean.parseBoolean(args[1]);
			String odsCode = null;
			if (args.length > 2) {
				odsCode = args[2];
			}
			SpecialRoutines.compareDsmPublishers(logDifferencesOnly, odsCode);
			//SpecialRoutines.compareDsmSubscribers();
			System.exit(0);
		}*/


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("UPRN")) {
			String configName = args[1];
			String protocolName = args[2];
			String outputFormat = args[3];
			String fileName = args[4];
			String debug = args[5];
			Uprn.bulkProcessUPRN(configName, protocolName, outputFormat, fileName, debug);

			System.exit(0);
		}

		if (args.length >= 1 && args[0].contains("UPRNTHREADED")) {

			Integer threads = 5;
			Integer QBeforeBlock = 10;
			String[] tb = args[0].split(":", -1);
			if (!tb[1].isEmpty()) {
				threads = Integer.parseInt(tb[1]);
			}
			if (!tb[2].isEmpty()) {
				QBeforeBlock = Integer.parseInt(tb[2]);
			}

			String configName = args[1];
			String protocolName = args[2];
			String outputFormat = args[3];
			String filePath = args[4];
			String debug = args[5];
			Uprn.bulkProcessUPRNThreaded(configName, protocolName, outputFormat, filePath, debug, threads, QBeforeBlock);

			System.exit(0);
		}

		if (args.length >= 1 && args[0].contains("UPRNTHREADEDNEWWAY")) {

			Integer threads = 5;
			Integer QBeforeBlock = 10;
			String[] tb = args[0].split(":", -1);
			if (!tb[1].isEmpty()) {
				threads = Integer.parseInt(tb[1]);
			}
			if (!tb[2].isEmpty()) {
				QBeforeBlock = Integer.parseInt(tb[2]);
			}

			String configName = args[1];
			String odsCodeRegex = args[2];
			String outputFormat = args[3];
			String filePath = args[4];
			String debug = args[5];
			Uprn.bulkProcessUPRNThreadedNewWay(configName, odsCodeRegex, outputFormat, filePath, debug, threads, QBeforeBlock);

			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("FindMissedExchanges")) {
			String tableName = args[1];
			String odsCodeRegex = null;
			if (args.length > 2) {
				odsCodeRegex = args[2];
			}
			OldRoutines.findMissedExchanges(tableName, odsCodeRegex);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("SendPatientsToSubscriber")) {
			String tableName = args[1];
			String reason = args[2];
			sendPatientsToSubscriber(tableName, reason);
			System.exit(0);
		}*/


		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateDeleteZipsForSubscriber")) {
			int batchSize = Integer.parseInt(args[1]);
			String sourceTable = args[2];
			int subscriberId = Integer.parseInt(args[3]);
			OldRoutines.createDeleteZipsForSubscriber(batchSize, sourceTable, subscriberId);
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

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("FixMedicationStatementIsActive")) {
			String odsCodeRegex = null;
			if (args.length > 1) {
				odsCodeRegex = args[1];
			}
			fixMedicationStatementIsActive(odsCodeRegex);
			System.exit(0);
		}*/

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

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("updatePatientSearch")) {
			String filePath = args[1];
			OldRoutines.updatePatientSearch(filePath);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("SubscriberFullLoad")) {
			UUID serviceId = UUID.fromString(args[1]);
			String reason = args[2];
			boolean bulkSendAllData = Boolean.valueOf(args[3]);
			Set<String> subscribers = new HashSet<>();
			for (int i = 4; i < args.length; i++) {
				String subscriberConfigName = args[i];
				subscribers.add(subscriberConfigName);
			}
			if (subscribers.isEmpty()) {
				LOG.debug("NO SUBSCRIBERS SPECIFICED - THIS WILL SEND TO ALL SUBSCRIBERS");
				OldRoutines.continueOrQuit();
			}
			QueueHelper.queueUpFullServiceForPopulatingSubscriber(serviceId, false, bulkSendAllData, true, subscribers, reason);
			System.exit(0);
		}

		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("SubscriberFullLoadFilteredFiles")) {
			UUID serviceId = UUID.fromString(args[1]);
			String subscriberConfigName = args[2];
			String reason = args[3];
			String filteredFiles = args[4];
			QueueHelper.queueUpFullServiceForPopulatingSubscriberFilteredFiles(serviceId, subscriberConfigName, reason, filteredFiles);
			System.exit(0);
		}*/

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TransformAndFilePatientsAndEpisodesForProtocolServices")) {
			String subscriberConfigName = args[1];
			String orgOdsCodeRegex = args[2];

			SpecialRoutines.transformAndFilePatientsAndEpisodesForProtocolServices(subscriberConfigName, orgOdsCodeRegex);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].contains("TransformAndFilePatientsAndEpisodesForProtocolServicesThreaded")) {

			Integer threads = 5;
			Integer QBeforeBlock = 10;
			String[] tb = args[0].split(":", -1);
			if (tb.length == 3) {
				if (!tb[1].isEmpty()) {
					threads = Integer.parseInt(tb[1]);
				}
				if (!tb[2].isEmpty()) {
					QBeforeBlock = Integer.parseInt(tb[2]);
				}
			}

			String protocolName = args[1];
			String subscriberConfigName = args[2];
			String compassVersion = args[3];
			String filePath = args[4];
			String debug = args[5];

			Subscribers.bulkProcessTransformAndFilePatientsAndEpisodesForProtocolServices(subscriberConfigName, protocolName,
					compassVersion, filePath, debug, threads, QBeforeBlock);

			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("TransformAndFilePatientAddressV2DataForProtocolServices")) {
			String subscriberConfigName = args[1];
			String orgOdsCodeRegex = args[2];

			SpecialRoutines.transformAndFilePatientAddressV2DataForProtocolServices(subscriberConfigName, orgOdsCodeRegex);
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
				&& args[0].equalsIgnoreCase("subscriberTransformPatients")) {
			String sourceFile = args[1];
			boolean bulkDelete = Boolean.parseBoolean(args[2]);
			String reason = args[3];
			Set<String> subscriberConfigNames = new HashSet<>();
			for (int i = 4; i < args.length; i++) {
				subscriberConfigNames.add(args[i]);
			}
			OldRoutines.subscriberTransformPatients(sourceFile, bulkDelete, reason, subscriberConfigNames);
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
			OldRoutines.createDigest(url, user, pass, table, columnFrom, columnTo, base64Salt, validNhsNumberCol);
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
			Transforms.createHomertonSubset(sourceDirPath, destDirPath, samplePatientsFile);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateAdastraSubset")) {
			String sourceDirPath = args[1];
			String destDirPath = args[2];
			String samplePatientsFile = args[3];
			Transforms.createAdastraSubset(sourceDirPath, destDirPath, samplePatientsFile);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateVisionSubset")) {
			String sourceDirPath = args[1];
			String destDirPath = args[2];
			String samplePatientsFile = args[3];
			Transforms.createVisionSubset(sourceDirPath, destDirPath, samplePatientsFile);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateTppSubset")) {
			String sourceDirPath = args[1];
			String destDirPath = args[2];
			String samplePatientsFile = args[3];
			Transforms.createTppSubset(sourceDirPath, destDirPath, samplePatientsFile);
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
			Transforms.createEmisSubset(sourceDirPath, destDirPath, samplePatientsFile);
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
			Queuing.postPatientsToProtocol(serviceId, systemId, sourceFile);
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
			Uprn.calculateUprnPseudoIds(subscriberConfigName, targetTable);
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
			Uprn.populateSubscriberUprnTable(subscriberConfigName, overrideBatchSize, patientId);
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
			String reason = args[3];
			Integer throttle = null;
			if (args.length > 4) {
				throttle = Integer.parseInt(args[4]);
			}
			Queuing.postToRabbit(exchangeName, srcFile, reason, throttle);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("PostExchangesToProtocol")) {
			String srcFile = args[1];
			String reason = args[2];
			Queuing.postExchangesToProtocol(srcFile, reason);
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

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("PostToInbound")) {
			String filePath = args[1];
			String reason = null;
			if (args.length > 2) {
				reason = args[2];
			}
			SpecialRoutines.postToInboundFromFile(filePath, reason);
			System.exit(0);
		}

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


		/*if (args.length >= 1
				&& args[0].equalsIgnoreCase("PopulateLastDataDate")) {
			int threads = Integer.parseInt(args[1]);
			int batchSize = Integer.parseInt(args[2]);
			OldRoutines.populateLastDataDate(threads, batchSize);
			System.exit(0);
		}*/


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
			Transforms.loadEmisData(serviceId, systemId, dbUrl, dbUsername, dbPassword, onlyThisFileType);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateEmisDataTables")) {
			Transforms.createEmisDataTables();
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
			Transforms.loadBartsData(serviceId, systemId, dbUrl, dbUsername, dbPassword, startDate, onlyThisFileType);
			System.exit(0);
		}

		if (args.length >= 1
				&& args[0].equalsIgnoreCase("CreateBartsDataTables")) {
			Transforms.createBartsDataTables();
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
}