package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.common.resourceBuilders.*;
import org.endeavourhealth.transform.tpp.csv.helpers.TppCsvHelper;
import org.endeavourhealth.transform.tpp.csv.transforms.clinical.SRCodeTransformer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD86 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD86.class);

    /**
     * tests time taken to do lookups
     *
     * largest GP practice I could find had 120,000 distinct staff members and profiles in their SRCode bulk
     * it took 2.5 mins to look up that many profile ID mappings
     * it took 5 mins to look up that many staff IDs -> profile IDs then look for profile IDs
     * so this would add <10 mins to the bulk for that practice, which took nearly a day to run
     */
    /*public static void testLookupTiming() {
        LOG.debug("Testing Lookup Timing");
        try {
            UUID serviceId = UUID.fromString("ccd1a468-12bd-4407-b9ad-33f3547f16ec");
            Random r = new Random(System.currentTimeMillis());

            long msStart = System.currentTimeMillis();
            LOG.debug("Testing profile ID lookups");
            for (int i=0; i<120000; i++) {

                //look up ID mapping for profile ID
                Set<Reference> refs = new HashSet<>();
                refs.add(ReferenceHelper.createReference(ResourceType.Practitioner, "" + r.nextInt()));
                Map<Reference, UUID> mappings = IdHelper.getEdsResourceIds(serviceId, refs);
                if (i % 1000 == 0) {
                    LOG.debug("DOne " + i);
                }
            }
            long msEnd = System.currentTimeMillis();
            LOG.debug("Finished profile ID lookups at " + (msEnd - msStart) + "ms");


            msStart = System.currentTimeMillis();
            LOG.debug("Testing profile ID lookups");
            for (int i=0; i<120000; i++) {

                //look up profile ID for staff ID
                TppStaffDalI dal = DalProvider.factoryTppStaffMemberDal();
                Set<Integer> staffIds = new HashSet<>();
                staffIds.add(new Integer(r.nextInt()));
                Map<Integer, Integer> hmStaffAndProfileIds = dal.findProfileIdsForStaffMemberIdsAtOrg("" + r.nextInt(), staffIds);

                //look up ID mapping for profile ID
                Set<Reference> refs = new HashSet<>();
                refs.add(ReferenceHelper.createReference(ResourceType.Practitioner, "" + r.nextInt()));
                Map<Reference, UUID> mappings = IdHelper.getEdsResourceIds(serviceId, refs);
                if (i % 1000 == 0) {
                    LOG.debug("DOne " + i);
                }
            }
            msEnd = System.currentTimeMillis();
            LOG.debug("Finished staff ID lookups at " + (msEnd - msStart) + "ms");


            LOG.debug("Finished Testing Lookup Timing");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }*/

    /**
     * routine to fix SD-86
     */
    public static void fixTppMissingPractitioners(boolean onlySkipCompletedOnes, String orgOdsCodeRegex) {
        LOG.debug("Fixing missing TPP practitioner at " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

            List<Service> services = serviceDal.getAll();

            String bulkOperationName = "fix null TPP practitioners SD-86";

            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                //check if already done
                if (onlySkipCompletedOnes) {
                    //check if already done, so we can make sure EVERY service is done
                    if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already done");
                        continue;
                    }

                } else {
                    //check if already started, to allow us to run multiple instances of this at once
                    if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already started or done");
                        continue;
                    }
                }

                LOG.debug("Doing " + service);

                UUID serviceId = service.getId();
                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("" + systemIds.size() + " system IDs found");
                }
                UUID systemId = systemIds.get(0);

                List<UUID> batchIdsCreated = new ArrayList<>();

                FhirResourceFiler filer = null;
                Exchange newExchange = null;

                UUID exchangeId = UUID.randomUUID();
                String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
                String odsCode = service.getLocalId();

                filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

                newExchange = new Exchange();
                newExchange.setId(exchangeId);
                newExchange.setBody(bodyJson);
                newExchange.setTimestamp(new Date());
                newExchange.setHeaders(new HashMap<>());
                newExchange.setHeaderAsUuid(HeaderKeys.SenderServiceUuid, service.getId());
                newExchange.setHeader(HeaderKeys.ProtocolIds, ""); //just set to non-null value, so postToExchange(..) can safely recalculate
                newExchange.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);
                newExchange.setHeaderAsUuid(HeaderKeys.SenderSystemUuid, systemId);
                newExchange.setHeader(HeaderKeys.SourceSystem, MessageFormat.TPP_CSV);
                newExchange.setServiceId(service.getId());
                newExchange.setSystemId(systemId);

                AuditWriter.writeExchange(newExchange);
                AuditWriter.writeExchangeEvent(newExchange, "Manually created to correct null TPP practitioners (SD-86)");

                List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
                LOG.debug("Found " + exchanges.size() + " exchanges");

                TppCsvHelper csvHelper = new TppCsvHelper(serviceId, systemId, exchangeId);

                try {

                    //go through the exchanges and fix all the affected file types and resources
                    LOG.debug("Doing drug sensitivities");
                    doDrugSensitivity(exchanges, csvHelper, filer);
                    LOG.debug("Doing events");
                    doEvent(exchanges, csvHelper, filer);
                    LOG.debug("Doing immunisations");
                    doImmunisation(exchanges, csvHelper, filer);
                    LOG.debug("Doing primary care medication");
                    doPrimaryCareMedication(exchanges, csvHelper, filer);
                    LOG.debug("Doing recall");
                    doRecall(exchanges, csvHelper, filer);
                    LOG.debug("Doing referral out");
                    doReferralOut(exchanges, csvHelper, filer);
                    LOG.debug("Doing repeat templates");
                    doRepeatTemplate(exchanges, csvHelper, filer);
                    LOG.debug("Doing codes");
                    doCode(exchanges, csvHelper, filer);

                    //call this to actually generate any FHIR Practitioners required
                    LOG.debug("Saving newly generated staff");
                    csvHelper.getStaffMemberCache().processChangedStaffMembers(csvHelper, filer);

                } catch (Throwable ex) {
                    LOG.error("Error doing service " + service);
                    throw ex;

                } finally {

                    //close down filer
                    LOG.debug("Waiting to finish");
                    filer.waitToFinish();

                    //set multicast header
                    String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                    newExchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                    //post to Rabbit protocol queue
                    List<UUID> exchangeIds = new ArrayList<>();
                    exchangeIds.add(newExchange.getId());
                    QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, null, null);

                    //set this after posting to rabbit so we can't re-queue it later
                    newExchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                    newExchange.getHeaders().remove(HeaderKeys.BatchIdsJson);
                    AuditWriter.writeExchange(newExchange);
                }

                //audit that we've done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished fixing missing TPP practitioner at " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void doCode(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {
        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "Code");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);

                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)) {
                    continue;
                }

                Object referenceObj = csvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(filer.getServiceId(), CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                if (referenceObj == null || referenceObj instanceof Integer) {
                    continue;
                }

                //SRCode records may go to multiple resource types, so work out the resource type
                Set<ResourceType> resourceTypes = SRCodeTransformer.findOriginalTargetResourceTypes(filer, CsvCell.factoryDummyWrapper(recordIdStr));
                
                if (resourceTypes.contains(ResourceType.Procedure)) {
                    UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.Procedure, "" + recordIdStr);
                    if (uuid == null) {
                        LOG.debug("Failed to find resource UUID for " + ResourceType.Procedure + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);

                    } else {
                        ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.Procedure.toString(), uuid);
                        Procedure resource = (Procedure) wrapper.getResource();

                        if (!resource.hasPerformer()) {
                            ProcedureBuilder builder = new ProcedureBuilder(resource);

                            Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String) referenceObj);
                            reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                            builder.addPerformer(reference);

                            filer.savePatientResource(null, false, builder);
                        }
                    }
                }

                if (resourceTypes.contains(ResourceType.AllergyIntolerance)) {
                    UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.AllergyIntolerance, "" + recordIdStr);
                    if (uuid == null) {
                        LOG.debug("Failed to find resource UUID for " + ResourceType.AllergyIntolerance + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);

                    } else {
                        ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.AllergyIntolerance.toString(), uuid);
                        AllergyIntolerance resource = (AllergyIntolerance) wrapper.getResource();

                        //remember "recorder" was mis-used so actually is the clinician field
                        if (!resource.hasRecorder()) {
                            AllergyIntoleranceBuilder builder = new AllergyIntoleranceBuilder(resource);

                            Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String) referenceObj);
                            reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                            builder.setClinician(reference);

                            filer.savePatientResource(null, false, builder);
                        }
                    }
                }

                if (resourceTypes.contains(ResourceType.FamilyMemberHistory)) {
                    UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.FamilyMemberHistory, "" + recordIdStr);
                    if (uuid == null) {
                        LOG.debug("Failed to find resource UUID for " + ResourceType.FamilyMemberHistory + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);

                    } else {
                        ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.FamilyMemberHistory.toString(), uuid);
                        FamilyMemberHistory resource = (FamilyMemberHistory) wrapper.getResource();

                        //since Encounter supports multiple ones, just ensure we're not duplicating it
                        if (!ExtensionConverter.hasExtension(resource, FhirExtensionUri.FAMILY_MEMBER_HISTORY_REPORTED_BY)) {
                            FamilyMemberHistoryBuilder builder = new FamilyMemberHistoryBuilder(resource);

                            Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String) referenceObj);
                            reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                            builder.setClinician(reference);

                            filer.savePatientResource(null, false, builder);
                        }
                    }
                }

                if (resourceTypes.contains(ResourceType.Condition)) {
                    UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.Condition, "" + recordIdStr);
                    if (uuid == null) {
                        LOG.debug("Failed to find resource UUID for " + ResourceType.Condition + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);

                    } else {
                        ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.Condition.toString(), uuid);
                        Condition resource = (Condition) wrapper.getResource();

                        //since Encounter supports multiple ones, just ensure we're not duplicating it
                        if (!resource.hasAsserter()) {
                            ConditionBuilder builder = new ConditionBuilder(resource);

                            Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String) referenceObj);
                            reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                            builder.setClinician(reference);

                            filer.savePatientResource(null, false, builder);
                        }
                    }
                }

                if (resourceTypes.contains(ResourceType.Observation)) {
                    UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.Observation, "" + recordIdStr);
                    if (uuid == null) {
                        LOG.debug("Failed to find resource UUID for " + ResourceType.Observation + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);

                    } else {
                        ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.Observation.toString(), uuid);
                        Observation resource = (Observation) wrapper.getResource();

                        //since Encounter supports multiple ones, just ensure we're not duplicating it
                        if (!resource.hasPerformer()) {
                            ObservationBuilder builder = new ObservationBuilder(resource);

                            Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String) referenceObj);
                            reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                            builder.setClinician(reference);

                            filer.savePatientResource(null, false, builder);
                        }
                    }
                }
            }
        }
    }

    private static void doRepeatTemplate(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {
        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "RepeatTemplate");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);

                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)) {
                    continue;
                }

                Object referenceObj = csvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(filer.getServiceId(), CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                if (referenceObj == null || referenceObj instanceof Integer) {
                    continue;
                }

                UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.MedicationStatement, "" + recordIdStr);
                if (uuid == null) {
                    LOG.debug("Failed to find resource UUID for " + ResourceType.MedicationStatement + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);
                    return;
                }

                ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.MedicationStatement.toString(), uuid);
                MedicationStatement resource = (MedicationStatement)wrapper.getResource();

                //since Encounter supports multiple ones, just ensure we're not duplicating it
                if (resource.hasInformationSource()) {
                    return;
                }

                MedicationStatementBuilder builder = new MedicationStatementBuilder(resource);

                Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String)referenceObj);
                reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                builder.setInformationSource(reference);

                filer.savePatientResource(null, false, builder);
            }
        }
    }

    private static void doReferralOut(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {
        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "ReferralOut");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);

                //referrals actually give a profile ID, which we may use
                String profileReferrer = record.get("IDProfileReferrer");
                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)
                        && Strings.isNullOrEmpty(profileReferrer)) {
                    continue;
                }

                Object referenceObj = null;

                if (!Strings.isNullOrEmpty(profileReferrer) && Integer.parseInt(profileReferrer) > 0) {
                    referenceObj = csvHelper.getStaffMemberCache().findProfileId(filer.getServiceId(), CsvCell.factoryDummyWrapper(profileReferrer));

                } else {
                    referenceObj = csvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(filer.getServiceId(), CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                }

                //given how much the referral transform has changed, we should set the practitioner if it's an int too
                //as it may well have not been done properly before
                //if (referenceObj == null || referenceObj instanceof Integer) {
                if (referenceObj == null) {
                    continue;
                }

                UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.ReferralRequest, "" + recordIdStr);
                if (uuid == null) {
                    LOG.warn("Failed to find resource UUID for " + ResourceType.ReferralRequest + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);
                    return;
                }

                ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.ReferralRequest.toString(), uuid);
                ReferralRequest resource = (ReferralRequest)wrapper.getResource();

                //since Encounter supports multiple ones, just ensure we're not duplicating it
                if (resource.hasRequester()) {
                    return;
                }

                ReferralRequestBuilder builder = new ReferralRequestBuilder(resource);

                Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, "" + referenceObj);
                reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                builder.setRequester(reference);

                filer.savePatientResource(null, false, builder);
            }
        }
    }

    private static void doRecall(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {
        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "Recall");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);

                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)) {
                    continue;
                }

                Object referenceObj = csvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(filer.getServiceId(), CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                if (referenceObj == null || referenceObj instanceof Integer) {
                    continue;
                }

                UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.ProcedureRequest, "" + recordIdStr);
                if (uuid == null) {
                    LOG.warn("Failed to find resource UUID for " + ResourceType.ProcedureRequest + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);
                    return;
                }

                ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.ProcedureRequest.toString(), uuid);
                //will have missing ProcedureRequests due to bug that meant they weren't transformed
                if (wrapper == null
                        || wrapper.isDeleted()) {
                    return;
                }
                ProcedureRequest resource = (ProcedureRequest)wrapper.getResource();

                //since Encounter supports multiple ones, just ensure we're not duplicating it
                if (resource.hasPerformer()) {
                    return;
                }

                ProcedureRequestBuilder builder = new ProcedureRequestBuilder(resource);

                Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String)referenceObj);
                reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                builder.setPerformer(reference);

                filer.savePatientResource(null, false, builder);
            }
        }
    }

    private static void doPrimaryCareMedication(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {
        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "PrimaryCareMedication");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);

                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)) {
                    continue;
                }

                Object referenceObj = csvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(filer.getServiceId(), CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                if (referenceObj == null || referenceObj instanceof Integer) {
                    continue;
                }

                //records from this file may end up as one or both types of medication FHIR resource, so we need to check for and update both
                UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.MedicationOrder, "" + recordIdStr);
                if (uuid != null) {
                    ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.MedicationOrder.toString(), uuid);
                    MedicationOrder resource = (MedicationOrder) wrapper.getResource();

                    //since Encounter supports multiple ones, just ensure we're not duplicating it
                    if (!resource.hasPrescriber()) {
                        MedicationOrderBuilder builder = new MedicationOrderBuilder(resource);

                        Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String) referenceObj);
                        reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                        builder.setPrescriber(reference);

                        filer.savePatientResource(null, false, builder);
                    }
                }

                uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.MedicationStatement, "" + recordIdStr);
                if (uuid != null) {
                    ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.MedicationStatement.toString(), uuid);
                    MedicationStatement resource = (MedicationStatement) wrapper.getResource();

                    if (!resource.hasInformationSource()) {
                        MedicationStatementBuilder builder = new MedicationStatementBuilder(resource);

                        Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String) referenceObj);
                        reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                        builder.setInformationSource(reference);

                        filer.savePatientResource(null, false, builder);
                    }
                }
            }
        }
    }

    private static void doImmunisation(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {
        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "Immunisation");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);

                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)) {
                    continue;
                }

                Object referenceObj = csvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(filer.getServiceId(), CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                if (referenceObj == null || referenceObj instanceof Integer) {
                    continue;
                }

                UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.Immunization, "" + recordIdStr);
                if (uuid == null) {
                    LOG.warn("Failed to find resource UUID for " + ResourceType.Immunization + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);
                    return;
                }

                ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.Immunization.toString(), uuid);
                Immunization resource = (Immunization)wrapper.getResource();

                //since Encounter supports multiple ones, just ensure we're not duplicating it
                if (resource.hasPerformer()) {
                    return;
                }

                ImmunizationBuilder builder = new ImmunizationBuilder(resource);

                Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String)referenceObj);
                reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                builder.setPerformer(reference);

                filer.savePatientResource(null, false, builder);
            }
        }
    }

    private static void doEvent(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {
        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "Event");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);

                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)) {
                    continue;
                }

                Object referenceObj = csvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(filer.getServiceId(), CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                if (referenceObj == null || referenceObj instanceof Integer) {
                    continue;
                }

                UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.Encounter, "" + recordIdStr);
                if (uuid == null) {
                    LOG.warn("Failed to find resource UUID for " + ResourceType.Encounter + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);
                    return;
                }

                ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.Encounter.toString(), uuid);
                Encounter resource = (Encounter)wrapper.getResource();

                //since Encounter supports multiple ones, just ensure we're not duplicating it
                if (resource.hasParticipant()) {
                    return;
                }

                EncounterBuilder builder = new EncounterBuilder(resource);

                Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String)referenceObj);
                reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                builder.addParticipant(reference, EncounterParticipantType.PRIMARY_PERFORMER);

                filer.savePatientResource(null, false, builder);
            }
        }
    }


    private static void doDrugSensitivity(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {

        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "DrugSensitivity");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);

                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)) {
                    continue;
                }

                Object referenceObj = csvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(filer.getServiceId(), CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                if (referenceObj == null || referenceObj instanceof Integer) {
                    continue;
                }

                UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.AllergyIntolerance, "" + recordIdStr);
                if (uuid == null) {
                    LOG.warn("Failed to find resource UUID for " + ResourceType.AllergyIntolerance + " " + recordIdStr + " in exchange " + exchange.getId() + " and file " + path);
                    return;
                }

                ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.AllergyIntolerance.toString(), uuid);
                AllergyIntolerance resource = (AllergyIntolerance)wrapper.getResource();

                //remember "recorder" was mis-used so actually is the clinician field
                if (resource.hasRecorder()) {
                    return;
                }

                AllergyIntoleranceBuilder builder = new AllergyIntoleranceBuilder(resource);

                Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String)referenceObj);
                reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);
                builder.setClinician(reference); //sets the recorder field

                filer.savePatientResource(null, false, builder);
            }
        }
    }


    private static String findFilePath(Exchange exchange, String fileType) {

        String exchangeBody = exchange.getBody();
        List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

        for (ExchangePayloadFile file : files) {
            if (file.getType().equals(fileType)) {
                return file.getPath();
            }
        }

        return null;
    }
}
