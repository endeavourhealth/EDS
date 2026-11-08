package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.*;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.PeriodHelper;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.RegistrationStatus;
import org.endeavourhealth.common.fhir.schema.RegistrationType;
import org.endeavourhealth.common.utility.FileHelper;
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
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.common.resourceBuilders.EncounterBuilder;
import org.endeavourhealth.transform.common.resourceBuilders.EpisodeOfCareBuilder;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.helpers.EmisMappingHelper;
import org.endeavourhealth.transform.emis.custom.transforms.RegistrationStatusTransformer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SD99 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD99.class);

    private static SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static final String BULK_OPERATION_NAME = "Fix Emis duplicate episodes SD-99";

    /**
     * the inbound Emis transform ended up creating extra EpisodeOfCare resources if
     * a patient's registration date changed (see SD-99). The transform has been fixed, and this
     * routine sorts out any existing affected data.
     */
    public static void fixEmisEpisodesChangingDate(boolean includeStartedButNotFinishedServices, boolean testMode, String orgOdsCodeRegex, UUID patientUuidFilter) {

        LOG.info("Fixing Emis episode of cares changing date at " + orgOdsCodeRegex + ", testMode = " + testMode);
        if (patientUuidFilter != null) {
            LOG.info("Restricting to patient " + patientUuidFilter);
        }

        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
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

                //check if already done
                if (!testMode) {
                    //check to see if already done this services
                    if (isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME, includeStartedButNotFinishedServices)) {
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                fixEmisEpisodesChangingDateForService(testMode, service, patientUuidFilter);


                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }
            }

            LOG.info("Finished fixing Emis episode of cares changing date at " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixEmisEpisodesChangingDateForService(boolean testMode, Service service, UUID patientUuidFilter) throws Exception {

        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.EMIS_CSV);
        if (endpoint == null) {
            LOG.warn("No vision endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();
        UUID exchangeId = UUID.randomUUID();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");

        //cache all the data from the raw data
        Map<String, List<RegRecord>> hmRegRecords = findRegistrationRecords(exchanges);
        Map<String, List<RegStatus>> hmRegStatuses = findRegistrationStatusRecords(exchanges);
        LOG.debug("Found " + hmRegRecords.size() + " patients");


        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.EMIS_CSV, "Manually created: " + BULK_OPERATION_NAME, exchangeId);
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

        } else {
            clearTempTable(serviceId);
        }

        try {
            LOG.debug("Fixing episodes");
            fixEpisodes(serviceId, hmRegRecords, hmRegStatuses, filer, patientUuidFilter);

        } catch (Throwable ex) {
            LOG.error("Error doing service " + service, ex);
            throw ex;

        } finally {

            //close down filer
            if (filer != null) {
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
        }

        //if run for all patients, find the most recent reg status exchange and re-queue it
        if (patientUuidFilter == null) {
            findAndReQueueRegStatusExchange(testMode, exchanges);
        }

    }

    private static void findAndReQueueRegStatusExchange(boolean testMode, List<Exchange> exchanges) throws Exception {

        //list is newest-to-oldest
        for (Exchange exchange : exchanges) {

            String filePath = findFilePathInExchange(exchange, "RegistrationStatus");
            if (filePath == null) {
                continue;
            }

            List<UUID> exchangeIds = new ArrayList<>();
            exchangeIds.add(exchange.getId());

            if (testMode) {
                LOG.debug("Would re-queue reg status extract exchange " + exchange.getId());

            } else {
                QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.INBOUND, null, "Reprocess reg status data after SD-99 fix", null, null);
            }
            break;
        }
    }

    private static void fixEpisodes(UUID serviceId, Map<String, List<RegRecord>> hmRegRecords, Map<String, List<RegStatus>> hmRegStatuses,
                                    FhirResourceFiler filer, UUID patientUuidFilter) throws Exception {

        int done = 0;

        //now actually do the fixing
        for (String patientGuid : hmRegRecords.keySet()) {

            List<RegRecord> regRecords = hmRegRecords.get(patientGuid);
            List<RegStatus> statuses = hmRegStatuses.get(patientGuid);
            if (statuses == null) {
                statuses = new ArrayList<>();
            }

            fixEmisEpisodesChangingDatePatient(serviceId, patientGuid, regRecords, statuses, filer, patientUuidFilter);

            done++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients");
            }
        }
        LOG.debug("Finished on " + done + " patients");

    }

    private static Map<String, List<RegStatus>> findRegistrationStatusRecords(List<Exchange> exchanges) throws Exception {

        LOG.debug("Looking for custom registration status records");

        Map<String, List<RegStatus>> ret = new HashMap<>();

        //each reg status file is a re-bulk over the last, so just find the most recent one by going newest-to-oldest
        for (Exchange exchange: exchanges) {

            String filePath = findFilePathInExchange(exchange, "RegistrationStatus");
            if (filePath == null) {
                continue;
            }

            CSVFormat regStatusCsvFormat = CSVFormat.TDF
                    .withHeader()
                    .withEscape((Character) null)
                    .withQuote((Character) null)
                    .withQuoteMode(QuoteMode.MINIMAL); //ideally want Quote Mode NONE, but validation in the library means we need to use this;

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
            CSVParser parser = new CSVParser(isr, regStatusCsvFormat);
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String patientGuid = record.get("PatientGuid");
                String date = record.get("Date");
                String regStatus = record.get("RegistrationStatus");
                String regType = record.get("RegistrationType");
                String processingOrder = record.get("ProcessingOrder");

                //make consistent with the IM1 extract
                patientGuid = "{" + patientGuid.toUpperCase() + "}";

                RegStatus s = new RegStatus();
                s.setPatientGuid(patientGuid);
                s.setDateStr(date);
                s.setStatus(regStatus);
                s.setType(regType);
                s.setProcessingOrder(Integer.valueOf(processingOrder));

                List<RegStatus> l = ret.get(patientGuid);
                if (l == null) {
                    l = new ArrayList<>();
                    ret.put(patientGuid, l);
                }
                l.add(s);
            }

            parser.close();

            //once we've found one reg status file, we don't need to do any more
            break;
        }

        return ret;
    }

    private static Map<String, List<RegRecord>> findRegistrationRecords(List<Exchange> exchanges) throws Exception {

        Map<String, List<RegRecord>> ret = new HashMap<>();

        LOG.debug("Looking for registration records from Admin_Patient");
        int done = 0;

        //the list is most-recent-first, so go backwards to go oldest-to-newest
        for (int i = exchanges.size() - 1; i >= 0; i--) {
            Exchange exchange = exchanges.get(i);

            String filePath = findFilePathInExchange(exchange, "Admin_Patient");
            if (filePath == null) {
                //LOG.warn("No patient file for exchange " + exchange.getId());
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);

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
                    //ignore the raw data telling us to delete. The inbound transform ignores some delete instructions,
                    //so we need to do the same. Just skip this specific record
                    //ret.remove(patientGuid);
                    continue;
                }

                RegRecord rr = new RegRecord(patientGuid);

                if (Strings.isNullOrEmpty(regDate)) {
                    //got some bad data in test packs, so just log out that it's bad and swap in the same "dawn of time" value as the transform does
                    LOG.warn("Empty start date for " + patientGuid + " in " + filePath);
                    regDate = "1900-01-01";
                    //throw new Exception("Empty start date for " + patientGuid + " in " + filePath);
                }
                rr.setStart(regDate);
                rr.setEnd(dedDate);

                if (!Strings.isNullOrEmpty(regType)) {
                    rr.setRegType(regType.trim());
                }
                rr.setDummy(dummyType.equalsIgnoreCase("true"));

                List<RegRecord> l = ret.get(patientGuid);
                if (l == null) {
                    l = new ArrayList<>();
                    ret.put(patientGuid, l);
                }

                //only add this record if it's actually different from the proceeding one
                //so we can cut down on the number of records we end up needing to look at
                if (!l.isEmpty()) {
                    RegRecord last = l.get(l.size()-1);
                    if (last.equalsValues(rr)) {
                        continue;
                    }
                }

                l.add(rr);
            }

            parser.close();

            done ++;
            if (done % 50 == 0) {
                LOG.debug("Done " + done);
            }
        }
        LOG.debug("Finished " + done);

        return ret;
    }

    private static void fixEmisEpisodesChangingDatePatient(UUID serviceId, String patientGuid,
                                                           List<RegRecord> regRecords, List<RegStatus> statuses,
                                                           FhirResourceFiler filer, UUID patientUuidFilter) throws Exception {


        UUID patientUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Patient, patientGuid);
        if (patientUuid == null) {
            //happens if we
            LOG.warn("Failed to find patient UUID for GUID " + patientGuid);
            return;
            //throw new Exception("Failed to find patient UUID for GUID " + patientGuid);
        }

        //skip any other record if filtering on one
        if (patientUuidFilter != null
                && !patientUuid.equals(patientUuidFilter)) {
            return;
        }

        if (filer == null) {
            LOG.trace(">>>>>>>>>>>Doing patient " + patientGuid + " with " + regRecords.size() + " reg records and " + statuses.size() + " status records");
        }

        //retrieve existing episodes
        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        List<ResourceWrapper> episodeWrappers = resourceDal.getResourcesByPatient(serviceId, patientUuid, ResourceType.EpisodeOfCare.toString());

        //if all episodes have been deleted, then don't worry
        if (episodeWrappers.isEmpty()) {
            if (filer == null) {
                LOG.trace("    Patient " + patientGuid + " has no episodes so skipping");
            }
            return;
        }

        //if only one episode then nothing to fix
        if (episodeWrappers.size() == 1) {
            if (filer == null) {
                LOG.trace("    Patient " + patientGuid + " only has one episode so skipping");
            }
            return;
        }

        //go through the reg records to work out the actual status we'd want if the transform was right originally
        Map<String, RegRecord> hmCurrentStatusById = new HashMap<>();
        Map<RegRecord, Set<String>> hmCurrentStatus = new HashMap<>();

        for (RegRecord record : regRecords) {

            String sourceId = record.getSourceId();
            RegistrationType regType = record.getRegistrationType();
            Date newEndDate = record.getDEnd();

            //if this already exists in the set of IDs to keep, then it's OK and we just need to update the running status
            if (hmCurrentStatusById.containsKey(sourceId)) {
                RegRecord currentStatus = hmCurrentStatusById.get(sourceId);
                currentStatus.copyFrom(record);
                continue;
            }

            //if the source ID is unknown, then we need to follow the same logic as the transform does now
            List<RegRecord> currentStatusList = new ArrayList<>(hmCurrentStatus.keySet());
            currentStatusList.sort((o1, o2) -> {
                Date d1 = o1.getDStart();
                Date d2 = o2.getDStart();
                return d1.compareTo(d2);
            });


            List<RegRecord> sameRegTypeList = new ArrayList<>();
            for (RegRecord status : currentStatusList) {
                if (status.getRegistrationType() == regType) {
                    sameRegTypeList.add(status);
                }
            }

            RegRecord bestMatchingStatus = null;

            //always attempt to match to the earliest active episode if there is one
            //if there are multiple active ones, then just select the first as we just sorted them
            if (bestMatchingStatus == null) {
                for (RegRecord status : sameRegTypeList) {
                    if (status.getDEnd() == null) {
                        bestMatchingStatus = status;
                        break;
                    }
                }
            }

            //if we don't have an active episode, then it may be possible we're changing the start date
            //of an already-ended episode (which does happen, rarely) so see if it's possible to match on end date
            if (bestMatchingStatus == null
                    && newEndDate != null) {

                for (RegRecord status : sameRegTypeList) {
                    if (status.getDEnd() != null
                            && status.getDEnd().equals(newEndDate)) {
                        bestMatchingStatus = status;
                        break;
                    }
                }
            }

            if (bestMatchingStatus == null) {
                //if we didn't match to an existing status, then add it to the maps
                RegRecord newStatus = new RegRecord(record.getPatientGuid());
                ;
                newStatus.copyFrom(record);

                hmCurrentStatusById.put(sourceId, newStatus);

                Set<String> l = new HashSet<>();
                l.add(sourceId);
                hmCurrentStatus.put(newStatus, l);

            } else {
                //if we did match to a status, then update it with the latest details
                bestMatchingStatus.copyFrom(record);

                //and make sure our source ID is associated with it
                hmCurrentStatusById.put(sourceId, bestMatchingStatus);

                Set<String> l = hmCurrentStatus.get(bestMatchingStatus);
                l.add(sourceId); //don't need to check if already present
            }

            //make sure all other episodes are ended (except the one to be transformed). We are only ever in this
            //function when processing brand NEW data (never when re-processing) so just end everything except
            //the episode UUID we found.
            for (RegRecord status : currentStatusList) {

                //end any others that are currently active and are not the one matched to the current record
                if (status.getDEnd() == null //active
                        && (bestMatchingStatus == null || bestMatchingStatus != status)) {

                    //end using the start of the current one
                    status.setEnd(record.getStart());
                }
            }
        }

        //go through the reg status records to find out what additional episodes we should have created (see SD-424)
        Map<String, RegStatus> hmRegStatusEpisodesRawFormat = new HashMap<>(); //ones with ID mappings formatted DD/MM/YYYY HH:MM:SS
        Map<String, RegStatus> hmRegStatusEpisodesCsvFormat = new HashMap<>(); //ones with ID mappings formatted YYYY-MM-DD
        Map<String, RegStatus> hmRegStatusEpisodesNewFormat = new HashMap<>(); //ones with ID mappings formatted RegStatus:YYYY-MM-DD

        statuses.sort((o1, o2) -> {
            return o1.compareTo(o2);
        });

        Map<String, RegStatus> possibleStartsOfEpisodes = new HashMap<>();

        for (RegStatus statusObj : statuses) {
            //LOG.debug("Checking reg status " + statusObj);

            RegistrationStatus status = statusObj.convertRegistrationStatus();
            String regType = statusObj.getType();

            if (!RegistrationStatusTransformer.isDeductionRegistrationStatus(status)) {
                //LOG.debug("    Is not deduction");

                //see if we're the start of a new run
                if (!possibleStartsOfEpisodes.containsKey(regType)) {
                    possibleStartsOfEpisodes.put(regType, statusObj);
                    //LOG.debug("    Found possible episode start");
                }

            } else {
                //LOG.debug("    Is a deduction status");

                //if we've got a deduction status after a registration status, then that is a complete past-episode
                RegStatus possibleStartOfEpisode = possibleStartsOfEpisodes.get(regType);
                if (possibleStartOfEpisode != null) {
                    //LOG.trace("    Got start of start of registration " + possibleStartOfEpisode);

                    //find an episode for the date range we've got
                    Date startDate = possibleStartOfEpisode.getDate(); //specifically using dates, not date times
                    Date endDate = statusObj.getDate(); //specifically using dates, not date times
                    Period proposedPeriod = PeriodHelper.createPeriod(startDate, endDate);

                    boolean createNewEpisode = true;

                    //got some very confusing data with one-day registations and then subsquence re-registrations,
                    //so we now skip any episode that would just be a one-day episode
                    if (startDate.equals(endDate)) {
                        createNewEpisode = false;
                    }

                    for (RegRecord expectedReg : hmCurrentStatus.keySet()) {

                        Period period = new Period();
                        period.setStart(expectedReg.getDStart());
                        period.setEnd(expectedReg.getDEnd());
                        //LOG.trace("    Checking against expected episode " + expectedReg + ", start OK = " + PeriodHelper.isWithin(period, startDate) + " end OK = " + PeriodHelper.isWithin(period, endDate));

                        if (PeriodHelper.overlaps(period, proposedPeriod)) {
                            createNewEpisode = false;
                            break;
                        }
                    }

                    //create a new one
                    if (createNewEpisode) {

                        //link the start to the deduction
                        possibleStartOfEpisode.setSubsequentDeduction(statusObj);

                        //reg status was mapped to episode in one of two ways, so add both to the map
                        String sourceId = possibleStartOfEpisode.getSourceIdRawFormat();
                        hmRegStatusEpisodesRawFormat.put(sourceId, possibleStartOfEpisode);

                        sourceId = possibleStartOfEpisode.getSourceIdCsvFormat();
                        hmRegStatusEpisodesCsvFormat.put(sourceId, possibleStartOfEpisode);

                        sourceId = possibleStartOfEpisode.getSourceIdNewFormat();
                        hmRegStatusEpisodesNewFormat.put(sourceId, possibleStartOfEpisode);
                    }
                }

                //reset these so we can spot the next start of a registration
                possibleStartsOfEpisodes.clear();
            }
        }

        //find the current state of mappings
        Map<String, UUID> hmActualMappings = new HashMap<>();
        Map<UUID, ResourceWrapper> hmActualEpisodes = new HashMap<>();

        for (ResourceWrapper wrapper : episodeWrappers) {
            UUID resourceId = wrapper.getResourceId();
            Set<String> sourceIds = findSourceIdsForEpisode(resourceId, serviceId);

            hmActualEpisodes.put(resourceId, wrapper);
            for (String sourceId : sourceIds) {
                hmActualMappings.put(sourceId, resourceId);
            }
        }


        if (filer == null) {
            LOG.debug("Patient GUID = " + patientGuid + " -> Patient UUID " + patientUuid);

            LOG.debug("Raw reg records:");
            for (RegRecord r : regRecords) {
                LOG.debug("    " + r);
            }

            LOG.debug("Raw reg status");
            for (RegStatus status : statuses) {
                LOG.debug("    " + status);
            }

            LOG.debug("Expected mappings:");
            List<String> l = new ArrayList<>(hmCurrentStatusById.keySet());
            l.sort((o1, o2) -> o1.compareToIgnoreCase(o2));
            for (String sourceId : l) {
                RegRecord regRecord = hmCurrentStatusById.get(sourceId);
                LOG.debug("    " + sourceId + " -> " + regRecord);
            }

            LOG.debug("Expected reg status episodes:");
            for (String sourceId: hmRegStatusEpisodesNewFormat.keySet()) {
                RegStatus status = hmRegStatusEpisodesNewFormat.get(sourceId);
                LOG.debug("    " + sourceId + " -> " + status);
            }

            LOG.debug("Current mappings:");
            l = new ArrayList<>(hmActualMappings.keySet());
            l.sort((o1, o2) -> o1.compareToIgnoreCase(o2));
            for (String sourceId : l) {
                UUID currentUuid = hmActualMappings.get(sourceId);
                ResourceWrapper wrapper = hmActualEpisodes.get(currentUuid);
                LOG.debug("    " + sourceId + " -> " + currentUuid + " -> " + formatWrapper(wrapper));
            }
        }

        //work out what we need to change
        Set<String> hsMappingsToDelete = new HashSet<>();
        Map<String, UUID> hmMappingsToUpdate = new HashMap<>();
        Map<UUID, UUID> hmEpisodesToDelete = new HashMap<>();
        Map<UUID, String> hmEpisodeToDeleteReason = new HashMap<>();
        Set<UUID> hsEpisodesToKeep = new HashSet<>();

        for (String sourceId : hmActualMappings.keySet()) {
            UUID currentUuid = hmActualMappings.get(sourceId);

            //if the source ID is one of the very old ones, from before the start date was factored in, then we may as well just delete the mapping
            if (!sourceId.contains(":")) {
                if (filer == null) {
                    LOG.trace("Deleting old-style mapping for " + sourceId);
                }
                hsMappingsToDelete.add(sourceId);
                continue;
            }

            //if there's nothing in the first map for the source ID, then this implies that the episode came from the Emis Reg Status custom extract
            if (hmCurrentStatusById.containsKey(sourceId)) {
                RegRecord regRecord = hmCurrentStatusById.get(sourceId);

                //work out which episode the source ID *SHOULD* point to
                String regRecordSourceId = regRecord.getSourceId();
                UUID expectedUuid = hmActualMappings.get(regRecordSourceId);
                if (expectedUuid == null) {
                    throw new Exception("Failed to find expected UUID for source ID " + regRecordSourceId + " doing mapping " + sourceId + " mapped to " + regRecord);
                }

                //if the current mapping matches what's expected, this is OK
                if (!expectedUuid.equals(currentUuid)) {

                    //if the mapping is different, then we need to flag the mapping for being updated
                    if (filer == null) {
                        LOG.trace("Existing mapping " + sourceId + " -> " + currentUuid + " is wrong and should change to " + expectedUuid + " (same as " + regRecordSourceId + ")");
                    }

                    hmMappingsToUpdate.put(sourceId, expectedUuid);

                    hmEpisodesToDelete.put(currentUuid, expectedUuid);
                    hmEpisodeToDeleteReason.put(currentUuid, "Reg date changed without deduction for " + sourceId);

                } else {
                    if (filer == null) {
                        LOG.trace("Mapping " + sourceId + " -> " + currentUuid + " is OK");
                    }
                    hsEpisodesToKeep.add(currentUuid);
                }

                continue;
            }

            //if the source ID is in one of the reg status episodes maps, then it's OK and can be left alone
            if (hmRegStatusEpisodesRawFormat.containsKey(sourceId)
                    || hmRegStatusEpisodesCsvFormat.containsKey(sourceId)
                    || hmRegStatusEpisodesNewFormat.containsKey(sourceId)) {

                RegStatus status = null;

                //if the source ID is a "bad" format one, then we want to swap it for a "good" format one
                if (hmRegStatusEpisodesRawFormat.containsKey(sourceId)) {

                    status = hmRegStatusEpisodesRawFormat.get(sourceId);
                    String goodFormatSourceId = status.getSourceIdNewFormat();
                    hsMappingsToDelete.add(sourceId);
                    hmMappingsToUpdate.put(goodFormatSourceId, currentUuid);

                    if (filer == null) {
                        LOG.trace("Source ID " + sourceId + " found in raw reg status format map, so will replace with " + goodFormatSourceId);
                    }

                } else if (hmRegStatusEpisodesCsvFormat.containsKey(sourceId)) {

                    status = hmRegStatusEpisodesCsvFormat.get(sourceId);
                    String goodFormatSourceId = status.getSourceIdNewFormat();
                    hsMappingsToDelete.add(sourceId);
                    hmMappingsToUpdate.put(goodFormatSourceId, currentUuid);

                    if (filer == null) {
                        LOG.trace("Source ID " + sourceId + " found in CSV reg status format map, so will replace with " + goodFormatSourceId);
                    }

                } else {

                    status = hmRegStatusEpisodesNewFormat.get(sourceId);

                    if (filer == null) {
                        LOG.trace("Source ID " + sourceId + " found in reg status map, so is OK");
                    }
                }

                //validate if the end date matches what's on the episode
                Date startDate = status.getDate();
                Date endDate = status.getSubsequentDeduction().getDate();
                Period expectedPeriod = PeriodHelper.createPeriod(startDate, endDate);

                ResourceWrapper wrapper = hmActualEpisodes.get(currentUuid);
                if (wrapper == null) {
                    throw new Exception("Null wrapper for source ID " + sourceId + " and UUID " + currentUuid);
                }
                EpisodeOfCare episode = (EpisodeOfCare)wrapper.getResource();
                Period actualPeriod = episode.getPeriod();
                int diff = PeriodHelper.comparePeriods(expectedPeriod, actualPeriod);
                if (diff != 0) {

                    //if the episode doesn't match what's expected then we have an episode and mapping to delete
                    if (filer == null) {
                        LOG.trace("Difference in Period between expected " + PeriodHelper.toString(expectedPeriod) + " and actual period on episode " + currentUuid + " " + PeriodHelper.toString(actualPeriod) + " so will delete episode and mapping");
                    }

                    hsMappingsToDelete.add(sourceId);

                    hmEpisodesToDelete.put(currentUuid, null);
                    hmEpisodeToDeleteReason.put(currentUuid, "Episode periods do not match from " + sourceId);

                    //throw new Exception("Difference in Period between expected " + PeriodHelper.toString(expectedPeriod) + " and actual period on episode " + currentUuid + " " + PeriodHelper.toString(actualPeriod));
                } else {
                    if (filer == null) {
                        LOG.debug("Existing expected reg status period " + PeriodHelper.toString(expectedPeriod) + " matches existing episode " + PeriodHelper.toString(actualPeriod));
                    }

                    hsEpisodesToKeep.add(currentUuid);
                }

                continue;
            }

            //if we get here and the source ID isn't in the list of ones from our reg status file, then we want to delete the episode
            //this is due to bugs in the reg status transform which meant it previously created too many episodes
            if (filer == null) {
                LOG.trace("Source ID from " + sourceId + " not found in either map, so should be deleted");
            }

            hsMappingsToDelete.add(sourceId);

            hmEpisodesToDelete.put(currentUuid, null);
            hmEpisodeToDeleteReason.put(currentUuid, "Source ID from " + sourceId + " not found in new reg status results");
        }

        //now we've worked out which mappings should be deleted and updated, and which episodes are no longer needed, make sure
        //that the episodes to delete don't contain any that we actually want to keep, which can happen if we have two mappings pointing
        //to the same episode
        for (UUID episodeId: hsEpisodesToKeep) {
            hmEpisodesToDelete.remove(episodeId);
        }

        //we've now removed "duplicates" created by the regular Emis transform, but that will also
        //have removed non-duplicates that should have been created by the reg status transform, so
        //go through the reg status episodes and create any missing episodes, re-using episodes that are
        //scheduled to be deleted
        for (String sourceId: hmRegStatusEpisodesNewFormat.keySet()) {
            RegStatus status = hmRegStatusEpisodesNewFormat.get(sourceId);

            Date startDate = status.getDate();
            Date endDate = status.getSubsequentDeduction().getDate();
            Period expectedPeriod = PeriodHelper.createPeriod(startDate, endDate);
            RegistrationType expectedRegType = status.getRegistrationType();

            boolean overlaps = false;
            for (UUID episodeId: hmActualEpisodes.keySet()) {

                //if scheduled for deletion, don't count it
                if (hmEpisodesToDelete.containsKey(episodeId)) {
                    continue;
                }
                ResourceWrapper wrapper = hmActualEpisodes.get(episodeId);
                EpisodeOfCare episode = (EpisodeOfCare)wrapper.getResource();
                Period episodePeriod = episode.getPeriod();
                if (PeriodHelper.overlaps(expectedPeriod, episodePeriod)) {
                    overlaps = true;
                    break;
                }
            }

            if (overlaps) {
                continue;
            }

            if (filer == null) {
                LOG.debug("Going to look for Episode to reuse for reg status " + status);
            }

            //if made it here we want to create an episode or rescue one due to be deleted
            EpisodeOfCare episodeToReuse = null;

            for (UUID episodeId: new HashSet<>(hmEpisodesToDelete.keySet())) {

                ResourceWrapper wrapper = hmActualEpisodes.get(episodeId);
                EpisodeOfCare episode = (EpisodeOfCare)wrapper.getResource();
                Period actualPeriod = episode.getPeriod();
                int diff = PeriodHelper.comparePeriods(expectedPeriod, actualPeriod);

                if (filer == null) {
                    LOG.debug("Episode " + episodeId + " for " + PeriodHelper.toString(actualPeriod) + " matches = " + diff);
                }

                if (diff == 0) {

                    //check reg type
                    EpisodeOfCareBuilder builder = new EpisodeOfCareBuilder(episode);
                    RegistrationType actualRegType = builder.getRegistrationType();
                    if (actualRegType == expectedRegType) {

                        if (filer == null) {
                            LOG.debug("Found existing episode " + episodeId + " with expected period and reg type so will reuse");
                        }
                        episodeToReuse = episode;

                        //remove from delete maps
                        hmEpisodesToDelete.remove(episodeId);
                        hmEpisodeToDeleteReason.remove(episodeId);

                        //need to set up mappings
                        hmMappingsToUpdate.put(sourceId, episodeId);

                        break;

                    } else {

                        if (filer == null) {
                            LOG.debug("Periods match but episode has reg type " + actualRegType + " and status wants " + expectedRegType);
                        }
                    }
                }
            }

            //don't bother creating new episodes - let the proper reg status transformer do this next month
            /*if (episodeToReuse == null) {

                if (filer == null) {
                    LOG.debug("Did not find existing episode to reuse so will create new");
                }

                //TODO - create new episode for saving and mapping
            }*/
        }

        if (filer != null) {

            //update encounters to link to the retained episodes
            if (!hmEpisodesToDelete.isEmpty()) {
                List<ResourceWrapper> encounterWrappers = resourceDal.getResourcesByPatient(serviceId, patientUuid, ResourceType.Encounter.toString());
                for (ResourceWrapper wrapper : encounterWrappers) {
                    Encounter encounter = (Encounter) wrapper.getResource();

                    if (encounter.hasEpisodeOfCare()) {
                        List<Reference> episodeRefs = encounter.getEpisodeOfCare();
                        Reference episodeRef = episodeRefs.get(0);
                        String episodeRefStr = ReferenceHelper.getReferenceId(episodeRef);
                        UUID episodeRefUuid = UUID.fromString(episodeRefStr);

                        UUID newEpisodeRefUuid = hmEpisodesToDelete.get(episodeRefUuid);
                        if (hmEpisodesToDelete.containsKey(episodeRefUuid)) {
                            if (newEpisodeRefUuid == null) {
                                throw new Exception("Failed to find replacement UUID for episode " + episodeRefStr + " in Encounter " + encounter.getId());
                            }

                            episodeRef = ReferenceHelper.createReference(ResourceType.EpisodeOfCare, newEpisodeRefUuid.toString());
                            episodeRefs.set(0, episodeRef);

                            EncounterBuilder builder = new EncounterBuilder(encounter);
                            filer.savePatientResource(null, false, builder);
                        }
                    }
                }
            }

            //delete the unwanted episodes
            for (UUID episodeToDelete : hmEpisodesToDelete.keySet()) {

                ResourceWrapper wrapper = hmActualEpisodes.get(episodeToDelete);
                if (wrapper.isDeleted()) {
                    throw new Exception("Trying to delete deleting episode " + episodeToDelete);
                }
                EpisodeOfCare episodeOfCare = (EpisodeOfCare) wrapper.getResource();
                EpisodeOfCareBuilder builder = new EpisodeOfCareBuilder(episodeOfCare);
                filer.deletePatientResource(null, false, builder);
            }

            //update the mappings in publisher transform DB
            if (!hmMappingsToUpdate.isEmpty()) {
                updateResourceIdMappings(serviceId, hmMappingsToUpdate);
            }

            //delete any very old mappings
            if (!hsMappingsToDelete.isEmpty()) {
                deleteResourceIdMappings(serviceId, hsMappingsToDelete);
            }


        } else {



            if (!hmEpisodesToDelete.isEmpty()) {
                LOG.debug("Would delete episodes: " + hmEpisodesToDelete.size());
                writeEpisodesToTempTable(serviceId, patientUuid, hmEpisodesToDelete, hmEpisodeToDeleteReason);

                for (UUID episodeToDelete : hmEpisodesToDelete.keySet()) {
                    UUID newEpisodeRefUuid = hmEpisodesToDelete.get(episodeToDelete);
                    String reason = hmEpisodeToDeleteReason.get(episodeToDelete);
                    LOG.debug("    " + episodeToDelete + " replaced with " + newEpisodeRefUuid + " (" + reason + ")");
                }
            }

            if (!hmMappingsToUpdate.isEmpty()) {
                LOG.debug("Would update source mappings: " + hmMappingsToUpdate.size());
                for (String sourceId: hmMappingsToUpdate.keySet()) {
                    UUID newUuid = hmMappingsToUpdate.get(sourceId);
                    LOG.debug("    " + sourceId + " to UUID " + newUuid);
                }
            }

            //delete any very old mappings
            if (!hsMappingsToDelete.isEmpty()) {
                LOG.debug("Would delete source mappings: " + hsMappingsToDelete.size());
                for (String sourceId: hsMappingsToDelete) {
                    LOG.debug("    " + sourceId);
                }
            }

            //ensure Encounters can be mapped to new Episodes
            if (!hmEpisodesToDelete.isEmpty()) {
                List<ResourceWrapper> encounterWrappers = resourceDal.getResourcesByPatient(serviceId, patientUuid, ResourceType.Encounter.toString());
                for (ResourceWrapper wrapper : encounterWrappers) {
                    Encounter encounter = (Encounter) wrapper.getResource();

                    if (encounter.hasEpisodeOfCare()) {
                        List<Reference> episodeRefs = encounter.getEpisodeOfCare();
                        Reference episodeRef = episodeRefs.get(0);
                        String episodeRefStr = ReferenceHelper.getReferenceId(episodeRef);
                        UUID episodeRefUuid = UUID.fromString(episodeRefStr);

                        if (hmEpisodesToDelete.containsKey(episodeRefUuid)) {
                            UUID newEpisodeRefUuid = hmEpisodesToDelete.get(episodeRefUuid);
                            if (newEpisodeRefUuid == null) {
                                throw new Exception("Failed to find replacement UUID for episode " + episodeRefStr + " in Encounter " + encounter.getId());
                            }
                        }
                    }
                }
            }
        }
    }


    private static void clearTempTable(UUID serviceId) throws Exception {
        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "DELETE FROM tmp.SD99_episodes_to_delete"
                    + " WHERE service_id = ?";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, serviceId.toString());
            ps.executeUpdate();
            connection.commit();

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /**
     * writes the UUIDs of the episodes to delete to a temp table for assurance of the routine
     */
    private static void writeEpisodesToTempTable(UUID serviceId, UUID patientUuid, Map<UUID, UUID> hmEpisodesToDelete, Map<UUID, String> hmEpisodeToDeleteReason) throws Exception {
        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "INSERT INTO tmp.SD99_episodes_to_delete (service_id, patient_id, episode_id, replacement_episode_id, reason) VALUES (?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(sql);

            for (UUID episodeId: hmEpisodesToDelete.keySet()) {
                int col = 1;
                ps.setString(col++, serviceId.toString());
                ps.setString(col++, patientUuid.toString());
                ps.setString(col++, episodeId.toString());

                UUID replacementId = hmEpisodesToDelete.get(episodeId);
                if (replacementId == null) {
                    ps.setNull(col++, Types.VARCHAR);
                } else {
                    ps.setString(col++, replacementId.toString());
                }

                String reason = hmEpisodeToDeleteReason.get(episodeId);
                ps.setString(col++, reason);

                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }


    }

    private static void deleteResourceIdMappings(UUID serviceId, Set<String> hsMappingsToDelete) throws Exception {

        Connection connection = ConnectionManager.getPublisherTransformConnection(serviceId);
        PreparedStatement ps = null;
        try {
            String sql = "DELETE FROM resource_id_map "
                    + " WHERE service_id = ?"
                    + " AND resource_type = ?"
                    + " AND source_id = ?";
            ps = connection.prepareStatement(sql);

            for (String sourceId : hsMappingsToDelete) {

                int col = 1;
                ps.setString(col++, serviceId.toString());
                ps.setString(col++, ResourceType.EpisodeOfCare.toString());
                ps.setString(col++, sourceId);

                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw ex;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    private static void updateResourceIdMappings(UUID serviceId, Map<String, UUID> hmMappingsToUpdate) throws Exception {

        Connection connection = ConnectionManager.getPublisherTransformConnection(serviceId);
        PreparedStatement ps = null;
        try {
            String sql = "REPLACE INTO resource_id_map (service_id, resource_type, source_id, eds_id)"
                    + " VALUES (?, ?, ?, ?)";
            ps = connection.prepareStatement(sql);

            for (String sourceId : hmMappingsToUpdate.keySet()) {
                UUID newTargetUuid = hmMappingsToUpdate.get(sourceId);

                int col = 1;
                ps.setString(col++, serviceId.toString());
                ps.setString(col++, ResourceType.EpisodeOfCare.toString());
                ps.setString(col++, sourceId);
                ps.setString(col++, newTargetUuid.toString());
                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw ex;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    private static String formatWrapper(ResourceWrapper wrapper) throws Exception {

        if (wrapper.isDeleted()) {
            return "<DELETED>";
        }

        EpisodeOfCare episode = (EpisodeOfCare)wrapper.getResource();
        Period period = episode.getPeriod();

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (period == null) {
            sb.append("NULL PERIOD");
        } else {
            Date start = period.getStart();
            if (start == null) {
                sb.append("NULL START");
            } else {
                sb.append(csvDateFormat.format(start));
            }

            sb.append(" to ");

            Date end = period.getEnd();
            if (end == null) {
                sb.append("");
            } else {
                sb.append(csvDateFormat.format(end));
            }
        }

        sb.append(", type ");

        Extension regTypeExtension = ExtensionConverter.findExtension(episode, FhirExtensionUri.EPISODE_OF_CARE_REGISTRATION_TYPE);
        if (regTypeExtension == null) {
            sb.append("NULL EXTENSION");
        } else {
            Coding coding = (Coding)regTypeExtension.getValue();
            RegistrationType fhirRegistrationType = RegistrationType.fromCode(coding.getCode());

            sb.append(fhirRegistrationType.getDescription());
        }

        sb.append("]");

        return sb.toString();
    }

    private static Set<String> findSourceIdsForEpisode(UUID resourceId, UUID serviceId) throws Exception {
        Connection connection = ConnectionManager.getPublisherTransformConnection(serviceId);
        PreparedStatement ps = null;
        try {
            String sql = "SELECT source_id"
                    + " FROM resource_id_map"
                    + " WHERE service_id = ?"
                    + " AND resource_type = ?"
                    + " AND eds_id = ?";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, serviceId.toString());
            ps.setString(col++, ResourceType.EpisodeOfCare.toString());
            ps.setString(col++, resourceId.toString());

            Set<String> ret = new HashSet<>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String sourceId = rs.getString(1);
                ret.add(sourceId);
            }

            return ret;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }


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

    static class RegStatus {

        private static DateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        private String patientGuid;
        private String dateStr;
        private String status;
        private String type;
        private Integer processingOrder;
        private RegStatus subsequentDeduction = null;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(dateStr);
            sb.append(" to ");

            if (subsequentDeduction != null) {
                sb.append(subsequentDeduction.dateStr);

            } else {
                sb.append("NULL");
            }

            sb.append(", status: ");

            try {
                sb.append(getRegistrationStatus());
            } catch (Exception ex) {
                sb.append("[UNKNOWN STATUS]");
            }

            sb.append(", type: ");
            sb.append(type);
            sb.append("]");
            return sb.toString();
        }

        public RegistrationType getRegistrationType() throws Exception {
            Integer i = Integer.valueOf(type);
            return EmisMappingHelper.findRegistrationTypeFromCode(i);
        }

        public RegistrationStatus getRegistrationStatus() throws Exception {
            Integer i = Integer.valueOf(status);
            return RegistrationStatusTransformer.convertRegistrationStatus(i);
        }

        public String getPatientGuid() {
            return patientGuid;
        }

        public void setPatientGuid(String patientGuid) {
            this.patientGuid = patientGuid;
        }

        public String getDateStr() {
            return dateStr;
        }

        public void setDateStr(String dateStr) {
            this.dateStr = dateStr;
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

        public RegStatus getSubsequentDeduction() {
            return subsequentDeduction;
        }

        public void setSubsequentDeduction(RegStatus subsequentDeduction) {
            this.subsequentDeduction = subsequentDeduction;
        }

        /**
         * in older code we mapped reg status to Episode by using the raw start date string
         */
        public String getSourceIdRawFormat() {
            return patientGuid + ":" + dateStr;
        }

        /**
         * we also want the start date (without time) formatted properly
         */
        public String getSourceIdCsvFormat() throws Exception {
            Date d = getDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return patientGuid + ":" + dateFormat.format(d);
        }

        /**
         * we now want the start date (without time) formatted properly
         */
        public String getSourceIdNewFormat() throws Exception {
            Date d = getDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return patientGuid + ":" + "RegStatus:" + dateFormat.format(d);
        }

        public Date getDateTime() throws Exception {
            return dateTimeFormat.parse(dateStr);
        }

        public Date getDate() throws Exception {
            return dateFormat.parse(dateStr);
        }

        public RegistrationType convertRegistrationType() throws Exception {
            return EmisMappingHelper.findRegistrationTypeFromCode(Integer.parseInt(type));
        }

        public RegistrationStatus convertRegistrationStatus() throws Exception {
            return RegistrationStatusTransformer.convertRegistrationStatus(Integer.parseInt(status));
        }

        public int compareTo(RegStatus other) {

            //after going round the houses for over a year, we've agreed that the records should be sorted by DATE TIME
            //and then only by PROCESSING ORDER if the date times are the same

            //sort by datetime
            try {
                int comp = getDateTime().compareTo(other.getDateTime());
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

        private SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private String patientGuid;
        private String start;
        private String end;
        private String regType;
        private boolean dummy;
        //private RegRecord replacement;

        public void copyFrom(RegRecord other) {
            //this.patientGuid = other.getPatientGuid();
            this.start = other.getStart();
            this.end = other.getEnd();
            this.regType = other.getRegType();
            this.dummy = other.isDummy();
        }

        @Override
        public String toString() {
            return "[" + start + " to " + end + ", type " + regType + ", dummy " + dummy + "]";
        }

        /**
         * specifically not called "equals" because I want this to NOT be used when looking up in maps and sets
         */
        public boolean equalsValues(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RegRecord regRecord = (RegRecord) o;

            if (dummy != regRecord.dummy) return false;
            if (!patientGuid.equals(regRecord.patientGuid)) return false;
            if (!start.equals(regRecord.start)) return false;
            if (end != null ? !end.equals(regRecord.end) : regRecord.end != null) return false;
            return regType.equals(regRecord.regType);

        }

        /*@Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RegRecord regRecord = (RegRecord) o;

            if (dummy != regRecord.dummy) return false;
            if (!patientGuid.equals(regRecord.patientGuid)) return false;
            if (!start.equals(regRecord.start)) return false;
            if (end != null ? !end.equals(regRecord.end) : regRecord.end != null) return false;
            return regType.equals(regRecord.regType);

        }

        @Override
        public int hashCode() {
            int result = patientGuid.hashCode();
            result = 31 * result + start.hashCode();
            result = 31 * result + (end != null ? end.hashCode() : 0);
            result = 31 * result + regType.hashCode();
            result = 31 * result + (dummy ? 1 : 0);
            return result;
        }*/

        public RegRecord(String patientGuid) {
            this.patientGuid = patientGuid;
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


        public Date getDStart() {
            if (Strings.isNullOrEmpty(start)) {
                return null;
            } else {
                try {
                    return csvDateFormat.parse(start);
                } catch (ParseException pe) {
                    throw new RuntimeException("Error parsing " + start, pe);
                }
            }
        }

        public Date getDEnd() {
            if (Strings.isNullOrEmpty(end)) {
                return null;
            } else {
                try {
                    return csvDateFormat.parse(end);
                } catch (ParseException pe) {
                    throw new RuntimeException("Error parsing " + end, pe);
                }
            }
        }

        public String getUniqueId() {
            return patientGuid + ":" + start;
        }

        public RegistrationType getRegistrationType() throws Exception {
            return org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer.convertRegistrationType(this.regType, dummy);
        }

        /*public RegRecord getReplacement() {
            return replacement;
        }

        public void setReplacement(RegRecord replacement) {
            this.replacement = replacement;
        }*/

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


