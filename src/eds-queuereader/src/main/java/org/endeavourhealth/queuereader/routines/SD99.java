package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.*;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.RegistrationType;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.common.resourceBuilders.EncounterBuilder;
import org.endeavourhealth.transform.common.resourceBuilders.EpisodeOfCareBuilder;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SD99 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD99.class);

    /**
     * finds cases of Emis patients changing registration state date and writes to CSV for investigation
     */
    public static void findEmisEpisodesChangingDate(String orgOdsCodeRegex) {
        LOG.debug("Find Emis episodes changing date at " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

            File dstFile = new File("EmisEpisodesChangingDate.csv");
            FileOutputStream fos = new FileOutputStream(dstFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);

            CSVFormat format = EmisCsvToFhirTransformer.CSV_FORMAT
                    .withHeader("Name", "ODS Code", "PatientGuid", "PreviousStart", "ChangedStart", "Direction", "PreviousRegType", "ChangedRegType", "RegTypeChanged", "PreviousFile", "ChangedFile"
                    );
            CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            List<Service> services = serviceDal.getAll();

            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                LOG.debug("Doing " + service);

                UUID serviceId = service.getId();
                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("" + systemIds.size() + " system IDs found");
                }
                UUID systemId = systemIds.get(0);

                Map<String, String> hmPatientStartDates = new HashMap<>();
                Map<String, String> hmPatientStartDatePaths = new HashMap<>();
                Map<String, String> hmPatientRegTypes = new HashMap<>();

                List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);

                for (int i = exchanges.size() - 1; i >= 0; i--) {
                    Exchange exchange = exchanges.get(i);

                    String exchangeBody = exchange.getBody();
                    List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);
                    if (files.isEmpty() || files.size() == 1) {
                        continue;
                    }

                    ExchangePayloadFile patientFile = null;
                    for (ExchangePayloadFile file : files) {
                        if (file.getType().equals("Admin_Patient")) {
                            patientFile = file;
                            break;
                        }
                    }

                    if (patientFile == null) {
                        LOG.warn("No patient file for exchange " + exchange.getId());
                        continue;
                    }

                    String path = patientFile.getPath();
                    InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);

                    CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
                    Iterator<CSVRecord> iterator = parser.iterator();

                    while (iterator.hasNext()) {
                        CSVRecord record = iterator.next();

                        String patientGuid = record.get("PatientGuid");
                        String regDate = record.get("DateOfRegistration");
                        String dedDate = record.get("DateOfDeactivation");
                        String deleted = record.get("Deleted");
                        String regType = record.get("PatientTypeDescription");

                        if (deleted.equals("true")) {
                            hmPatientStartDates.remove(patientGuid);
                            hmPatientStartDatePaths.remove(patientGuid);
                            hmPatientRegTypes.remove(patientGuid);
                            continue;
                        }

                        if (!Strings.isNullOrEmpty(dedDate)) {
                            hmPatientStartDates.remove(patientGuid);
                            hmPatientStartDatePaths.remove(patientGuid);
                            hmPatientRegTypes.remove(patientGuid);
                            continue;
                        }

                        String previousDate = hmPatientStartDates.get(patientGuid);
                        String previousPath = hmPatientStartDatePaths.get(patientGuid);
                        String previousRegType = hmPatientRegTypes.get(patientGuid);
                        if (previousDate != null
                                && !previousDate.equals(regDate)) {

                            //reg date has changed
                            LOG.debug("Patient " + patientGuid + " start date changed from " + previousDate + " to " + regDate);
                            LOG.debug("Previous file = " + previousPath);
                            LOG.debug("This file = " + path);

                            Date dPrevious = sdf.parse(previousDate);
                            Date dNow = sdf.parse(regDate);

                            String direction = null;
                            if (dPrevious.before(dNow)) {
                                direction = "Forwards";
                            } else {
                                direction = "Backwards";
                            }

                            String regTypeChanged = null;
                            if (regType.equals(previousRegType)) {
                                regTypeChanged = "false";
                            } else {
                                regTypeChanged = "true";
                            }

                            printer.printRecord(service.getName(), service.getLocalId(), patientGuid, previousDate, regDate, direction, previousRegType, regType, regTypeChanged, previousPath, path);
                        }

                        hmPatientStartDates.put(patientGuid, regDate);
                        hmPatientStartDatePaths.put(patientGuid, path);
                        hmPatientRegTypes.put(patientGuid, regType);
                    }

                    parser.close();
                }
            }

            printer.close();

            LOG.debug("Finished Find Emis episodes changing date at " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    /**
     * the inbound Emis transform ended up creating extra EpisodeOfCare resources if
     * a patient's registration date changed (see SD-99). The transform has been fixed, and this
     * routine sorts out any existing affected data.
     */
    public static void fixEmisEpisodesChangingDate(boolean testMode, String orgOdsCodeRegex) {
        LOG.info("Fixing Emis episode of cares changing date at " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();

            List<Service> services = serviceDal.getAll();

            String bulkOperationName = "Fix Emis duplicate episodes SD-99";

            SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                //check if already done
                if (!testMode) {
                    if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already done");
                        continue;
                    }
                }

                LOG.debug("Doing " + service);

                Map<String, List<RegRecord>> hmRegRecords = new HashMap<>();
                Map<String, List<RegStatus>> hmRegStatuses = new HashMap<>();
                String latestRegStatusPath = null;
                UUID latestRegStatusExchange = null;

                UUID serviceId = service.getId();
                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("" + systemIds.size() + " system IDs found");
                }
                UUID systemId = systemIds.get(0);

                //go through files to find state from files
                List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
                LOG.trace("Going to check " + exchanges.size() + " exchanges");
                for (int i = exchanges.size() - 1; i >= 0; i--) {
                    Exchange exchange = exchanges.get(i);

                    String exchangeBody = exchange.getBody();
                    List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

                    if (files.isEmpty()) {
                        continue;
                    }

                    if (files.size() == 1) {
                        ExchangePayloadFile file = files.get(0);
                        if (file.getType().equals("RegistrationStatus")) {
                            latestRegStatusPath = file.getPath();
                            latestRegStatusExchange = exchange.getId();
                        }
                        continue;
                    }


                    ExchangePayloadFile patientFile = null;
                    for (ExchangePayloadFile file : files) {
                        if (file.getType().equals("Admin_Patient")) {
                            patientFile = file;
                            break;
                        }
                    }

                    if (patientFile == null) {
                        LOG.warn("No patient file for exchange " + exchange.getId());
                        continue;
                    }

                    String path = patientFile.getPath();
                    InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);

                    CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
                    Iterator<CSVRecord> iterator = parser.iterator();

                    while (iterator.hasNext()) {
                        CSVRecord record = iterator.next();

                        String patientGuid = record.get("PatientGuid");
                        String regDate = record.get("DateOfRegistration");
                        String dedDate = record.get("DateOfDeactivation");
                        String deleted = record.get("Deleted");
                        String regType = record.get("PatientTypeDescription");
                        String dummyType = record.get("DummyType");


                        if (deleted.equals("true")) {
                            hmRegRecords.remove(patientGuid);
                            continue;
                        }

                        RegRecord rr = new RegRecord(patientGuid);

                        if (Strings.isNullOrEmpty(regDate)) {
                            throw new Exception("Empty start date for " + patientGuid + " in " + patientFile.getPath());
                        }
                        rr.setStart(regDate);
                        rr.setDStart(csvDateFormat.parse(regDate));

                        if (!Strings.isNullOrEmpty(dedDate)) {
                            //Date d = csvDateFormat.parse(dedDate);
                            rr.setEnd(dedDate);
                        }
                        if (!Strings.isNullOrEmpty(regType)) {
                            rr.setRegType(regType.trim());
                        }
                        rr.setDummy(dummyType.equalsIgnoreCase("true"));

                        List<RegRecord> l = hmRegRecords.get(patientGuid);
                        if (l == null) {
                            l = new ArrayList<>();
                            hmRegRecords.put(patientGuid, l);
                        }

                        l.add(rr);
                    }

                    parser.close();
                }

                //read the reg status file too
                if (latestRegStatusPath == null) {
                    throw new Exception("Failed to find recent reg status extract file");
                }

                CSVFormat regStatusCsvFormat = CSVFormat.TDF
                        .withHeader()
                        .withEscape((Character) null)
                        .withQuote((Character) null)
                        .withQuoteMode(QuoteMode.MINIMAL); //ideally want Quote Mdde NONE, but validation in the library means we need to use this;

                InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(latestRegStatusPath);
                CSVParser parser = new CSVParser(isr, regStatusCsvFormat);
                Iterator<CSVRecord> iterator = parser.iterator();

                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                while (iterator.hasNext()) {
                    CSVRecord record = iterator.next();
                    String patientGuid = record.get("PatientGuid");
                    String date = record.get("Date");
                    String regStatus = record.get("RegistrationStatus");
                    String regType = record.get("RegistrationType");
                    String processingOrder = record.get("ProcessingOrder");

                    RegStatus s = new RegStatus();
                    s.setDate(dateFormat.parse(date));
                    s.setStatus(regStatus);
                    s.setType(regType);
                    s.setProcessingOrder(Integer.valueOf(processingOrder));

                    List<RegStatus> l = hmRegStatuses.get(patientGuid);
                    if (l == null) {
                        l = new ArrayList<>();
                        hmRegStatuses.put(patientGuid, l);
                    }
                    l.add(s);
                }

                parser.close();

                //for each patient, tidy up episodes to match
                int done = 0;
                LOG.debug("Found " + hmRegRecords.size() + " patients");


                List<UUID> batchIdsCreated = new ArrayList<>();

                FhirResourceFiler filer = null;
                Exchange exchange = null;

                try {

                    if (!testMode) {
                        UUID exchangeId = UUID.randomUUID();
                        String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
                        String odsCode = service.getLocalId();

                        filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

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
                        AuditWriter.writeExchangeEvent(exchange, "Manually created to correct Emis episodes of care (SD-99)");
                    }

                    //now actually do the fixing
                    for (String patientGuid : hmRegRecords.keySet()) {

                        List<RegRecord> regRecords = hmRegRecords.get(patientGuid);
                        List<RegStatus> statuses = hmRegStatuses.get(patientGuid);
                        if (statuses == null) {
                            statuses = new ArrayList<>();
                        }

                        fixEmisEpisodesChangingDatePatient(serviceId, patientGuid, regRecords, statuses, filer, testMode);

                        done++;
                        if (done % 1000 == 0) {
                            LOG.debug("Done " + done + " patients");
                        }
                    }
                    LOG.debug("Finished on " + done + " patients");


                } finally {


                    if (testMode) {
//TODO

                    } else {

                        //close down filer
                        filer.waitToFinish();

                        //set multicast header
                        String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                        exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                        //post to Rabbit protocol queue
                        List<UUID> exchangeIds = new ArrayList<>();
                        exchangeIds.add(exchange.getId());
                        QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, null, null);

                        //set this after posting to rabbit so we can't re-queue it later
                        exchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                        AuditWriter.writeExchange(exchange);

                        //find and re-queue the Reg Status latest extract
/*
                        exchangeIds = new ArrayList<>();
                        exchangeIds.add(latestRegStatusExchange);
                        QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.INBOUND, null, "Re-queue reg status after fixing Emis episodes SD-99");
*/

                        //audit that we've done
                        setServiceDoneBulkOperation(service, bulkOperationName);
                    }
                }
            }

            LOG.info("Finished fixing Emis episode of cares changing date at " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixEmisEpisodesChangingDatePatient(UUID serviceId, String patientGuid,
                                                           List<RegRecord> regRecords, List<RegStatus> statuses,
                                                           FhirResourceFiler filer, boolean testMode) throws Exception {


        if (testMode) {
            LOG.trace("Doing patient " + patientGuid + " with " + regRecords.size() + " reg records and " + statuses.size() + " status records");
        }


        //delete all mappings that are NOT from the proper files
        //delete all Episodes that are only from reg status file
        //correct remaining mappings to point to single episode ID
        //correct Episode, saving most recent version over the top
        //save everything
        //TODO - update ENCOUNTERS to point to new EPISODES!!!
        //DONE - get the LATEST version of each episode and save over the new UUID
        //TODO - update resource ID map to use new mappings
        //TODO - delete any episode NOT mapped to now
        //TODO - re-process the reg status file
        //TODO - what about mappings left by old reg status transform?
        //TODO - there will be old reg-status mappings from start ID to UUID - if we get data through for one of those dates
        //TODO - update builder accordingly
        //TODO - factor in reg type
        //find an episode with the start date
        //TODO - update START DATE -> UUID mappings to be correct
        //TODO - set latest dates on episodes
        //TODO - delete other episodes
        //TODO - re-run reg status file to create others
//TODO - add closing Filer and sending to ProtocolQueue in FINALLY block

        UUID patientUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Patient, patientGuid);
        if (patientUuid == null) {
            throw new Exception("Failed to find patient UUID for GUID " + patientGuid);
        }

        //retrieve existing episodes
        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        List<ResourceWrapper> episodeWrappers = resourceDal.getResourcesByPatient(serviceId, patientUuid, ResourceType.EpisodeOfCare.toString());

        //if only one episode then nothing to fix
        if (episodeWrappers.size() == 1) {
            if (testMode) {
                LOG.trace("Patient " + patientGuid + " only has one episode so skipping");
            }
            return;
        }

        statuses.sort((o1, o2) -> {
            return o1.compareTo(o2);
        });


        //find the source IDs that came from the proper extracts
        Set<String> properExtractSourceIds = new HashSet<>();
        for (RegRecord regRecord : regRecords) {
            properExtractSourceIds.add(regRecord.getSourceId());
        }

        //get the resource_id_map records for all mappings to these episodes
        Map<UUID, ResourceWrapper> hmEpisodeWrappersByUuid = new HashMap<>();
        Map<String, UUID> hmEpisodeMappingsBySourceId = new HashMap<>();

        Set<String> sourceIdsToDelete = new HashSet<>();
        Map<UUID, EpisodeOfCareBuilder> hmEpisodesToDelete = new HashMap<>();

        //retrieve all mappings to these existing episodes
        for (ResourceWrapper w : episodeWrappers) {
            UUID uuid = w.getResourceId();
            hmEpisodeWrappersByUuid.put(uuid, w);

            String sql = "SELECT source_id FROM resource_id_map WHERE resource_type = ? AND eds_id = ?";
            Connection ptConnection = ConnectionManager.getPublisherTransformConnection(serviceId);
            PreparedStatement ps = ptConnection.prepareStatement(sql);
            ps.setString(1, ResourceType.EpisodeOfCare.toString());
            ps.setString(2, uuid.toString());
            ResultSet rs = ps.executeQuery();

            List<String> sourceIds = new ArrayList<>();
            while (rs.next()) {
                String sourceId = rs.getString(1);
                sourceIds.add(sourceId);

                hmEpisodeMappingsBySourceId.put(sourceId, uuid);
            }

            ps.close();
            ptConnection.close();

            //find any source IDs that didn't come from the proper exract files (i.e. came from
            //the reg status file)
            boolean cameFromProperExtract = false;

            for (String sourceId : sourceIds) {
                if (!properExtractSourceIds.contains(sourceId)) {
                    sourceIdsToDelete.add(sourceId);
                } else {
                    cameFromProperExtract = true;
                }
            }

            //if this episode has no mappings that came from the proper extract, then it was generated
            //from the reg status extract, so should be deleted
            if (!cameFromProperExtract) {
                EpisodeOfCare episodeOfCare = (EpisodeOfCare) w.getResource();
                EpisodeOfCareBuilder builder = new EpisodeOfCareBuilder(episodeOfCare);
                hmEpisodesToDelete.put(uuid, builder);
            }
        }

        //go through the reg records from the proper extracts and work out the NEW version
        //of reality based on the new transform implementation
        List<RegRecord> runningState = new ArrayList<>();

        for (RegRecord regRecord : regRecords) {

            String end = regRecord.getEnd();
            RegistrationType registrationType = regRecord.getRegistrationType();

            //look at the current state to see which state our new record would map to
            RegRecord regRecordMatch = null;
            for (int i = 0; i < runningState.size(); i++) {
                RegRecord check = runningState.get(i);
                if (check.getEnd() == null
                        && check.getRegistrationType() == registrationType) {
                    regRecordMatch = check;
                    regRecord.setReplacement(regRecordMatch);
                    runningState.set(i, regRecord); //replace match with latest record
                    break;
                }
            }

            //if no active registration, then match to an ended on with the same end date
            if (regRecordMatch == null
                    && end != null) {

                for (int i = 0; i < runningState.size(); i++) {
                    RegRecord check = runningState.get(i);
                    if (check.getEnd() != null
                            && check.getEnd().equals(end)
                            && check.getRegistrationType() == registrationType) {
                        regRecordMatch = check;
                        regRecord.setReplacement(regRecordMatch);
                        runningState.set(i, regRecord); //replace match with latest record
                        break;
                    }
                }
            }

            //if no match, then it would create a new episode so carry over the UUID it's already mapped to
            if (regRecordMatch == null) {
                runningState.add(regRecord);
            }

            //always re-sort by start date so our currentState is consistent with how the regular transform
            //would have the data
            runningState.sort((o1, o2) -> {
                Date d1 = o1.getDStart();
                Date d2 = o2.getDStart();
                return d1.compareTo(d2);
            });
        }

        //each record in the running state list represents an episode we want to keep
        Map<String, UUID> hmMappingsToUpdate = new HashMap<>();
        Map<UUID, EpisodeOfCareBuilder> hmEpisodesToSave = new HashMap<>();
        Map<UUID, UUID> hmOldToNewEpisodeId = new HashMap<>();

        for (RegRecord regRecord : runningState) {

            //the UUID we actually want to keep should be the oldest one in our chain
            RegRecord last = regRecord;
            while (true) {
                if (last.getReplacement() == null) {
                    break;
                }
                last = last.getReplacement();
            }
            String lastSourceId = last.getSourceId();
            UUID uuidToKeep = hmEpisodeMappingsBySourceId.get(lastSourceId);

            //but the most recent record point to the Episode with the most recent data,
            //so this should be saved over the UUID we want to keep
            UUID uuidToWrite = hmEpisodeMappingsBySourceId.get(regRecord.getSourceId());

            //if the two UUIDs aren't the same we need to write Episode
            if (!uuidToKeep.equals(uuidToWrite)) {

                ResourceWrapper wrapper = hmEpisodeWrappersByUuid.get(uuidToWrite);
                EpisodeOfCare episodeOfCare = (EpisodeOfCare) wrapper.getResource();
                EpisodeOfCareBuilder builder = new EpisodeOfCareBuilder(episodeOfCare);
                builder.setId(uuidToKeep.toString());
                hmEpisodesToSave.put(uuidToKeep, builder);
            }

            //all the source IDs in the chain should be updated to point to the UUID to keep
            //and all other episodes referenced in the chain should be deleted
            last = regRecord;
            while (last != null) {

                String sourceId = last.getSourceId();
                UUID mappedUuid = hmEpisodeMappingsBySourceId.get(sourceId);
                if (!mappedUuid.equals(uuidToKeep)) {
                    hmMappingsToUpdate.put(sourceId, uuidToKeep);
                    hmOldToNewEpisodeId.put(mappedUuid, uuidToKeep);

                    //delete that episode
                    if (!hmEpisodesToDelete.containsKey(mappedUuid)) {
                        ResourceWrapper wrapper = hmEpisodeWrappersByUuid.get(mappedUuid);
                        EpisodeOfCare episodeOfCare = (EpisodeOfCare) wrapper.getResource();
                        EpisodeOfCareBuilder builder = new EpisodeOfCareBuilder(episodeOfCare);
                        hmEpisodesToDelete.put(mappedUuid, builder);
                    }
                }

                last = last.getReplacement();
            }
        }

        //update encounters
        Map<EncounterBuilder, UUID> hmEncounterBuilderToOldEpisode = new HashMap<>();
        List<ResourceWrapper> encounterWrappers = resourceDal.getResourcesByPatient(serviceId, patientUuid, ResourceType.Encounter.toString());
        for (ResourceWrapper w : encounterWrappers) {
            Encounter e = (Encounter) w.getResource();
            if (!e.hasEpisodeOfCare()) {
                continue;
            }
            Reference ref = e.getEpisodeOfCare().get(0);
            UUID episodeUuid = UUID.fromString(ReferenceHelper.getReferenceId(ref));
            //if the encounter points to an episode we're deleting, then
            //update the encounter to point to the episode that's replacing it
            if (hmEpisodesToDelete.containsKey(episodeUuid)) {
                throw new Exception("Encounter " + e.getId() + " points to episode " + episodeUuid + " that is being deleted");
            }
            UUID newEpisodeUuid = hmOldToNewEpisodeId.get(episodeUuid);
            if (newEpisodeUuid != null) {
                ref = ReferenceHelper.createReference(ResourceType.EpisodeOfCare, newEpisodeUuid.toString());
                EncounterBuilder builder = new EncounterBuilder(e);
                builder.setEpisodeOfCare(ref);

                hmEncounterBuilderToOldEpisode.put(builder, episodeUuid);
            }
        }

                    /*
                    Map<UUID, ResourceWrapper> hmEpisodeWrappersByUuid = new HashMap<>();
                    Map<String, UUID> hmEpisodeMappingsBySourceId = new HashMap<>();


                    Set<String> sourceIdsToDelete = new HashSet<>();
                    Map<String, UUID> hmMappingsToUpdate = new HashMap<>();
                    Map<UUID, EpisodeOfCareBuilder> hmEpisodesToDelete = new HashMap<>();
                    Map<UUID, EpisodeOfCareBuilder> hmEpisodesToSave = new HashMap<>();
                    */

        //if an encounter is in both maps, then something is wrong
        Set<UUID> hsEpisodeIds = new HashSet<>(hmEpisodesToDelete.keySet());
        hsEpisodeIds.retainAll(hmEpisodesToSave.keySet());
        if (!hsEpisodeIds.isEmpty()) {
            throw new Exception("Episode UUIDs found in both sets " + hsEpisodeIds);
        }

        //if a mapping is in both maps, then something is wrong
        Set<String> hsSourceIds = new HashSet<>(sourceIdsToDelete);
        hsSourceIds.retainAll(hmMappingsToUpdate.keySet());
        if (!hsSourceIds.isEmpty()) {
            throw new Exception("Source IDs found in both sets " + hsEpisodeIds);
        }


        if (testMode) {

                        /*LOG.trace("Dumping source map size " + hmEpisodeMappingsBySourceId.size());
                        for (String sourceId: hmEpisodeMappingsBySourceId.keySet()) {
                            UUID mappedUuid = hmEpisodeMappingsBySourceId.get(sourceId);
                            LOG.trace("    SourceID [" + sourceId + "] -> " + mappedUuid);
                        }*/

            LOG.debug("Got " + hmEpisodeWrappersByUuid.size() + " episodes");
            for (UUID episodeUuid : hmEpisodeWrappersByUuid.keySet()) {
                ResourceWrapper wrapper = hmEpisodeWrappersByUuid.get(episodeUuid);

                //find all mappings to that ID
                //LOG.trace("Episode UUID = " + episodeUuid);
                List<String> sourceIds = new ArrayList<>();
                for (String sourceId : hmEpisodeMappingsBySourceId.keySet()) {
                    UUID mappedUuid = hmEpisodeMappingsBySourceId.get(sourceId);
                    //LOG.trace("Compare against [" + sourceId + "] -> " + mappedUuid + " = " + (mappedUuid.equals(episodeUuid)));
                    if (mappedUuid.equals(episodeUuid)) {
                        sourceIds.add(sourceId);
                        //LOG.trace("Matches");
                    }
                }
                //LOG.trace("Source IDs " + sourceIds.size() + " -> " + sourceIds + " -> [" + String.join("], [", sourceIds) + "]");
                LOG.debug("    Episode " + wrapper.getResourceId() + ", mapped from [" + String.join("], [", sourceIds) + "]");
            }

            LOG.debug("Got " + regRecords.size() + " proper extract records");
            for (RegRecord regRecord : regRecords) {
                String sourceId = regRecord.getSourceId();
                LOG.debug("    Start " + regRecord.getStart() + " End " + regRecord.getEnd() + " Type " + regRecord.getRegType() + " Source ID [" + sourceId + "]");
            }

            LOG.debug("Will delete " + sourceIdsToDelete.size() + " source ID mappings (from reg status extract)");
            for (String sourceIdToDelete : sourceIdsToDelete) {
                LOG.debug("    " + sourceIdToDelete);
            }

            LOG.debug("Will delete " + hmEpisodesToDelete.size() + " episodes (from reg status extract)");
            for (UUID episodeUuid : hmEpisodesToDelete.keySet()) {
                EpisodeOfCareBuilder builder = hmEpisodesToDelete.get(episodeUuid);
                LOG.debug("    " + builder.getResourceId());
            }

            LOG.debug("Will update " + hmMappingsToUpdate.size() + " source ID mappings");
            for (String sourceId : hmMappingsToUpdate.keySet()) {
                UUID newUuid = hmMappingsToUpdate.get(sourceId);
                UUID oldUuid = hmEpisodeMappingsBySourceId.get(sourceId);
                LOG.debug("    " + sourceId + " -> " + newUuid + " (was " + oldUuid + ")");
            }

            LOG.debug("Will save " + hmEpisodesToSave.size() + " episodes");
            for (UUID episodeUuid : hmEpisodesToSave.keySet()) {
                EpisodeOfCareBuilder builder = hmEpisodesToDelete.get(episodeUuid);
                LOG.debug("    " + builder.getResourceId());
                LOG.debug("        " + builder);
            }

            LOG.debug("Will update " + hmEncounterBuilderToOldEpisode.size() + " encounters");
            for (EncounterBuilder builder : hmEncounterBuilderToOldEpisode.keySet()) {
                UUID oldEpisodeId = hmEncounterBuilderToOldEpisode.get(builder);
                Encounter e = (Encounter) builder.getResource();
                Reference ref = e.getEpisodeOfCare().get(0);
                UUID newEpisodeUuid = UUID.fromString(ReferenceHelper.getReferenceId(ref));
                LOG.debug("   " + builder.getResourceId() + " -> episode " + newEpisodeUuid + " (from " + oldEpisodeId + ")");
            }

        } else {

            //update encounters
            for (EncounterBuilder builder : hmEncounterBuilderToOldEpisode.keySet()) {
                filer.savePatientResource(null, false, builder);
            }

            for (UUID episodeUuid : hmEpisodesToSave.keySet()) {
                EpisodeOfCareBuilder builder = hmEpisodesToDelete.get(episodeUuid);
                filer.savePatientResource(null, false, builder);
            }

            for (UUID episodeUuid : hmEpisodesToDelete.keySet()) {
                EpisodeOfCareBuilder builder = hmEpisodesToDelete.get(episodeUuid);
                filer.deletePatientResource(null, false, builder);
            }


            Connection ptConnection = ConnectionManager.getPublisherTransformConnection(serviceId);

            String sql = "UPDATE resource_id_map SET eds_id = ? WHERE service_id = ? AND resource_type = ? AND source_id = ?";
            PreparedStatement ps = ptConnection.prepareStatement(sql);

            for (String sourceId : hmMappingsToUpdate.keySet()) {
                UUID newUuid = hmMappingsToUpdate.get(sourceId);

                ps.setString(1, newUuid.toString());
                ps.setString(2, serviceId.toString());
                ps.setString(3, ResourceType.EpisodeOfCare.toString());
                ps.setString(4, sourceId);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();

            sql = "DELETE FROM resource_id_map WHERE service_id = ? AND resource_type = ? AND source_id = ?";
            ps = ptConnection.prepareStatement(sql);

            for (String sourceId : sourceIdsToDelete) {
                ps.setString(1, serviceId.toString());
                ps.setString(2, ResourceType.EpisodeOfCare.toString());
                ps.setString(3, sourceId);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();

            ptConnection.commit();
        }
    }


    static class RegStatus {
        private Date date;
        private String status;
        private String type;
        private Integer processingOrder;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getProcessingOrder() {
            return processingOrder;
        }

        public void setProcessingOrder(Integer processingOrder) {
            this.processingOrder = processingOrder;
        }


        public int compareTo(RegStatus other) {

            //after going round the houses for over a year, we've agreed that the records should be sorted by DATE TIME
            //and then only by PROCESSING ORDER if the date times are the same

            //sort by datetime
            try {
                int comp = getDate().compareTo(other.getDate());
                if (comp != 0) {
                    return comp;
                }
            } catch (Exception ex) {
                //need to handle potential exception from date format errors
                throw new RuntimeException("Failed to compare reg status objects", ex);
            }

            // if the processingOrder value of the compared data is a 1 then this always takes reverse precedence in the
            // list as its the "current" registration status and needs to reverse head up the list where there is a matching
            // date so it gets transformed as the latest registration_status_id
            if (other.getProcessingOrder().intValue() == 1) {
                return -1;
            }

            int comp = processingOrder.compareTo(other.getProcessingOrder());
            if (comp != 0) {
                return comp;
            }

            return 0;
        }
    }


    static class RegRecord {


        private String patientGuid;
        private Date dStart;
        private String start;
        private String end;
        private String regType;
        private boolean dummy;
        private RegRecord replacement;

        public RegRecord(String patientGuid) {
            this.patientGuid = patientGuid;
        }

        public Date getDStart() {
            return dStart;
        }

        public void setDStart(Date dStart) {
            this.dStart = dStart;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getRegType() {
            return regType;
        }

        public void setRegType(String regType) {
            this.regType = regType;
        }

        public String getSourceId() {
            return patientGuid + ":" + start;
        }

        public RegistrationType getRegistrationType() throws Exception {
            return org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer.convertRegistrationType(this.regType, dummy);
        }


        public String getPatientGuid() {
            return patientGuid;
        }

        public void setPatientGuid(String patientGuid) {
            this.patientGuid = patientGuid;
        }

        public boolean isDummy() {
            return dummy;
        }

        public void setDummy(boolean dummy) {
            this.dummy = dummy;
        }

        public RegRecord getReplacement() {
            return replacement;
        }

        public void setReplacement(RegRecord replacement) {
            this.replacement = replacement;
        }

       /*@Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RegRecord regRecord = (RegRecord) o;

            if (dummy != regRecord.dummy) return false;
            if (patientGuid != null ? !patientGuid.equals(regRecord.patientGuid) : regRecord.patientGuid != null)
                return false;
            if (start != null ? !start.equals(regRecord.start) : regRecord.start != null) return false;
            if (end != null ? !end.equals(regRecord.end) : regRecord.end != null) return false;
            return regType != null ? regType.equals(regRecord.regType) : regRecord.regType == null;

        }

        @Override
        public int hashCode() {
            int result = patientGuid != null ? patientGuid.hashCode() : 0;
            result = 31 * result + (start != null ? start.hashCode() : 0);
            result = 31 * result + (end != null ? end.hashCode() : 0);
            result = 31 * result + (regType != null ? regType.hashCode() : 0);
            result = 31 * result + (dummy ? 1 : 0);
            return result;
        }*/
    }
}


