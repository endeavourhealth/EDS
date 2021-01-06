package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.ReferenceComponents;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.CsvCell;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.AppointmentBuilder;
import org.endeavourhealth.transform.common.resourceBuilders.ScheduleBuilder;
import org.endeavourhealth.transform.tpp.TppCsvToFhirTransformer;
import org.endeavourhealth.transform.tpp.csv.helpers.TppCsvHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SD280 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD283.class);

    /**
     * fixes data for SD-280, SD-281 and SD-283
     */
    public static void fixTppSessionsAndSlots(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing TPP Sessions and Slots at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            String bulkOperationName = "Fixing TPP sessions and slots (SD-280, SD-281 and SD-282)";

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                if (!testMode) {
                    if (includeStartedButNotFinishedServices) {
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
                }

                LOG.debug("Doing " + service);
                fixTppSessionsAndSlotsAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, bulkOperationName);
                }

            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixTppSessionsAndSlotsAtService(boolean testMode, Service service) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No TPP endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");
        Map<Long, Long> hmRotaStaffProfiles = new HashMap<>();
        Map<Long, Set<Long>> hmRotaAppointments = new HashMap<>();
        Map<Long, Date> hmAppointmentStartDates = new HashMap<>();
        findAppointmentDetailsForRotas(exchanges, hmRotaStaffProfiles, hmRotaAppointments, hmAppointmentStartDates);
        LOG.debug("Cached " + hmAppointmentStartDates.size() + " appointments");
        Map<Long, Date> hmRotasAndStartDates = findRotaIdAndStartDates(exchanges);
        LOG.debug("Cached " + hmRotasAndStartDates.size() + " rota");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created to fix TPP slot and session practitioners (SD-283 and SD-284)");
            UUID exchangeId = newExchange.getId();
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing appointments");
            fixAppointments(serviceId, hmAppointmentStartDates, filer);
            LOG.debug("Fixing rotas");
            fixRotas(serviceId, hmRotasAndStartDates, hmRotaStaffProfiles, hmRotaAppointments, hmAppointmentStartDates, filer);
            

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

            //we'll have a load of stuff cached in here, so clear it down as it won't be applicable to the next service
            IdHelper.clearCache();
        }
    }

    /**
     * SD-280 - set StartDate on FHIR Schedules (from SRRota file or SRAppointment)
     * SD-281 - set Practitioner on FHIR Schedules (from SRAppointment)
     */
    private static void fixRotas(UUID serviceId,
                                 Map<Long, Date> hmRotasAndStartDates,
                                 Map<Long, Long> hmRotaStaffProfiles,
                                 Map<Long, Set<Long>> hmRotaAppointments,
                                 Map<Long, Date> hmAppointmentStartDates,
                                 FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        for (Long rotaId: hmRotasAndStartDates.keySet()) {

            UUID scheduleUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Schedule, "" + rotaId);
            if (scheduleUuid == null) {
                //seen this a few times but only for cases where new data had arrived and was in the inbound queue
                LOG.warn("No schedule UUID found for raw ID " + rotaId);
                continue;
            }

            Schedule schedule = (Schedule)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Schedule, scheduleUuid.toString());
            if (schedule == null) {
                LOG.warn("Missing or deleted schedule for UUID " + scheduleUuid + ", raw ID " + rotaId);
                continue;
            }

            ScheduleBuilder builder = new ScheduleBuilder(schedule);
            boolean madeChange = false;

            //clear any existing practitioner since they'll all be garbage
            if (schedule.hasActor()) {
                builder.clearActors();
                madeChange = true;
            }

            //get the profile ID cached from the appointment data and forwards map to a UUID
            Long profileId = hmRotaStaffProfiles.get(rotaId);
            if (profileId != null) { //may be null for rotas without any appts
                UUID practitionerUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Practitioner, "" + profileId);
                if (practitionerUuid == null) {
                    LOG.warn("No practitioner UUID found for profile ID " + profileId + " when doing Rota " + scheduleUuid + ", raw ID " + rotaId);
                    continue;
                }

                //set the mapped practitioner reference on the Schedule
                Reference mappedPractitionerRef = ReferenceHelper.createReference(ResourceType.Practitioner, practitionerUuid.toString());
                builder.addActor(mappedPractitionerRef);
                madeChange = true;
            }

            //get the cached start date from the SRRota files
            Date rotaStartDate = hmRotasAndStartDates.get(rotaId);

            //if we didn't find a start date in the SRRota file, then we need to work it out from the SRAppointment data
            if (rotaStartDate == null) {
                rotaStartDate = calculateRotaStartDate(rotaId, hmRotaAppointments, hmAppointmentStartDates);
            }

            //we may still have a null start date for rotas, if we've never received any appointment data for them, in which case we leave it
            if (rotaStartDate != null) {
                builder.setPlanningHorizonStart(rotaStartDate);
                madeChange = true;
            }

            if (madeChange) {
                changed ++;
                if (filer != null) {
                    filer.saveAdminResource(null, false, builder);
                }
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " / " + hmRotasAndStartDates.size() + " rotas, changed " + changed);
            }
        }

        LOG.debug("Finished " + done + " rotas, changed " + changed);
    }

    /**
     * in the absence of a start date in the SRRota file, we try to work out a start date from
     * date in the SRAppointment file
     */
    private static Date calculateRotaStartDate(Long rotaId, Map<Long, Set<Long>> hmRotaAppointments, Map<Long, Date> hmAppointmentStartDates) {

        //get the appt IDs for our rota
        Set<Long> hsApptsInRota = hmRotaAppointments.get(rotaId);
        if (hsApptsInRota == null) {
            return null;
        }

        Date earliestStart = null;

        //for each appt ID, get its start date and find the earliest
        for (Long apptId: hsApptsInRota) {
            Date apptStart = hmAppointmentStartDates.get(apptId);

            //we'll have a null start if the appt was deleted
            if (apptStart == null) {
                continue;
            }

            if (earliestStart == null
                    || apptStart.before(earliestStart)) {
                earliestStart = apptStart;
            }
        }

        return earliestStart;

    }

    private static Date findResourceDate(UUID serviceId, ResourceType resourceType, String resourceUuid, boolean findMostRecentDate) throws Exception {

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, resourceType.toString(), UUID.fromString(resourceUuid));
        ResourceWrapper entry = null;

        //this routine specifcally fixes missing Practitioners, so it's possible we'll be trying to
        //find the creation date of a Practitioner that doesn't exist (Emis data has missing practitioners)
        /*if (history.isEmpty() && resourceType == ResourceType.Practitioner) {
            return null;
        }*/

        //history is most-recent-first
        if (findMostRecentDate) {
            entry = history.get(0);
        } else {
            entry = history.get(history.size()-1);
        }

        return entry.getCreatedAt();
    }

    /**
     * FHIR Appointments have Practitioner references set but the Practitioner doesn't exist or was created AFTER (see SD-282)
     */
    private static void fixAppointments(UUID serviceId, Map<Long, Date> hmAppointmentStartDates, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        Map<String, Date> hmPractitionerDates = new HashMap<>();

        TppCsvHelper csvHelper = new TppCsvHelper(serviceId, null, null);

        for (Long appointmentId: hmAppointmentStartDates.keySet()) {

            UUID appointmentUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Appointment, "" + appointmentId);
            if (appointmentUuid == null) {
                LOG.warn("No appointment UUID found for raw ID " + appointmentId);
                continue;
            }

            Appointment appointment = (Appointment)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Appointment, appointmentUuid.toString());
            if (appointment == null) {
                LOG.warn("Missing or deleted appointment for UUID " + appointmentUuid + ", raw ID " + appointment);
                continue;
            }

            boolean needToSaveAppointment = false;

            String practitionerUuidStr = findFirstPractitionerUuid(appointment);
            if (Strings.isNullOrEmpty(practitionerUuidStr)) {
                LOG.warn("No practitioner on appointment UUID " + appointmentUuid + ", raw ID " + appointment);
                continue;
            }

            //if practitioner NOT exists - create it and update appt
            Practitioner practitioner = (Practitioner)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Practitioner, practitionerUuidStr.toString());
            if (practitioner == null) {

                //convert the practitioner UUID back to a TPP staff profile ID
                Reference practitionerRef = ReferenceHelper.createReference(ResourceType.Practitioner, practitionerUuidStr);
                Reference rawPractitionerRef = IdHelper.convertEdsReferenceToLocallyUniqueReference(csvHelper, practitionerRef);
                String profileId = ReferenceHelper.getReferenceId(rawPractitionerRef);
                CsvCell cell = CsvCell.factoryDummyWrapper(profileId);

                //and call this fn with the profile ID to make sure it's registered as a missing practitioner
                Reference practitionerReference = csvHelper.createPractitionerReferenceForProfileId(cell);

                needToSaveAppointment = true;

                LOG.debug("Will create missing practitioner for Appointment " + appointmentUuid + ", raw ID " + appointmentId + " - profile ID " + profileId + " UUID " + ReferenceHelper.getReferenceId(practitionerRef));
                /*if (filer == null) {
                    LOG.debug("Will create missing practitioner for Appointment " + appointmentUuid + ", raw ID " + appointmentId + " - profile ID " + profileId + " UUID " + ReferenceHelper.getReferenceId(practitionerRef));
                }*/

            } else {
                //if practitioner created AFTER appointment (later exchange) - update appointment

                //find date this schedule was LAST sent through to subscribers
                Date dtAppointment = findResourceDate(serviceId, ResourceType.Appointment, appointmentUuid.toString(), true);

                //find date the practitioner was FIRST sent through to subscribers (use a cache since practitioners will be referenced by lots of schedules)
                Date dtPractitioner = hmPractitionerDates.get(practitionerUuidStr);
                if (dtPractitioner == null) {
                    dtPractitioner = findResourceDate(serviceId, ResourceType.Practitioner, practitionerUuidStr, false);
                    hmPractitionerDates.put(practitionerUuidStr, dtPractitioner);
                }

                //if the schedule went through before the practitioner then the schedule
                //needs to go through again to refresh the schedule table record
                if (dtAppointment.before(dtPractitioner)) {

                    needToSaveAppointment = true;

                    if (filer == null) {
                        LOG.debug("Need to refresh appointment " + appointmentUuid + ", raw ID " + appointmentId + " because transformed before practitioner existed");
                    }
                }
            }

            //if we need to send the schedule through again, then we need to change it in some way that will bypass the checksum checking
            //in FhirResourceFiler
            if (needToSaveAppointment) {

                ExtensionConverter.setResourceChanged(appointment); //creates an artificial change so FhirResourceFiler will save it
                AppointmentBuilder builder = new AppointmentBuilder(appointment);

                if (filer != null) {
                    filer.savePatientResource(null, false, builder);
                }
                changed ++;
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " / " + hmAppointmentStartDates.size() + " appointments, changed " + changed);
            }
        }

        //make sure to create any missing practitioners we found
        if (filer != null) {
            csvHelper.getStaffMemberCache().processChangedStaffMembers(csvHelper, filer);

            //close this down properly
            csvHelper.stopThreadPool();

            //need to make sure all Practitioners are saved to the DB before going on to do Slots
            filer.waitUntilEverythingIsSaved();
        }

        LOG.debug("Finished " + done + " appointments, changed " + changed);

    }

    /*private static int findPractitionerCount(Appointment appointment) {
        int ret = 0;
        if (appointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participantComponent : appointment.getParticipant()) {
                Reference reference = participantComponent.getActor();
                ReferenceComponents components = ReferenceHelper.getReferenceComponents(reference);
                if (components.getResourceType() == ResourceType.Practitioner) {
                    ret ++;
                }
            }
        }
        return ret;
    }*/

    private static String findFirstPractitionerUuid(Appointment appointment) {

        if (appointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participantComponent : appointment.getParticipant()) {
                Reference reference = participantComponent.getActor();
                ReferenceComponents components = ReferenceHelper.getReferenceComponents(reference);
                if (components.getResourceType() == ResourceType.Practitioner) {
                    return components.getId();
                }
            }
        }
        return null;
    }


    private static void findAppointmentDetailsForRotas(List<Exchange> exchanges,
                                                       Map<Long, Long> hmRotaStaffProfiles,
                                                       Map<Long, Set<Long>> hmRotaAppointments,
                                                       Map<Long, Date> hmAppointmentStartDates) throws Exception {

        DateFormat dateFormat = new SimpleDateFormat(TppCsvToFhirTransformer.DATE_FORMAT + " " + TppCsvToFhirTransformer.TIME_FORMAT);

        //list if most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String filePath = findFilePathInExchange(exchange, "Appointment");
            if (filePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String apptIdStr = record.get("RowIdentifier");
                String rotaIdStr = record.get("IDRota");
                String startDateStr = record.get("DateStart");
                String profileIdStr = record.get("IDProfileClinician");

                //check if deleted (deleted column isn't always present)
                boolean isDeleted = false;
                if (parser.getHeaderMap().containsKey("RemovedData")) {
                    String removedStr = record.get("RemovedData");
                    if (removedStr.equals("1")) {
                        isDeleted = true;
                    }
                }

                if (!isDeleted) {
                    if (Strings.isNullOrEmpty(rotaIdStr)) {
                        throw new Exception("Null rota ID for appt " + apptIdStr + " in " + filePath);
                    }
                    if (Strings.isNullOrEmpty(startDateStr)) {
                        throw new Exception("Null start date for appt " + apptIdStr + " in " + filePath);
                    }
                    if (Strings.isNullOrEmpty(profileIdStr)) {
                        throw new Exception("Null profile ID for appt " + apptIdStr + " in " + filePath);
                    }


                    Long apptId = Long.valueOf(apptIdStr);
                    Long rotaId = Long.valueOf(rotaIdStr);
                    Date startDate = dateFormat.parse(startDateStr);
                    Long profileId = Long.valueOf(profileIdStr);

                    //all appts for a rota have the same profile, so just set in the map
                    hmRotaStaffProfiles.put(rotaId, profileId);

                    //link between rotas and appts
                    Set<Long> hsApptIds = hmRotaAppointments.get(rotaId);
                    if (hsApptIds == null) {
                        hsApptIds = new HashSet<>();
                        hmRotaAppointments.put(rotaId, hsApptIds);
                    }
                    hsApptIds.add(apptId);

                    //cache appt start times
                    hmAppointmentStartDates.put(apptId, startDate);

                } else {

                    Long apptId = Long.valueOf(apptIdStr);

                    //if an appt is deleted, remove its cached start date so we
                    //don't factor it in when working out a rotas date
                    hmAppointmentStartDates.remove(apptId);
                }
            }

            parser.close();
        }
    }

    private static Map<Long, Date> findRotaIdAndStartDates(List<Exchange> exchanges) throws Exception {

        //the session user file doesn't have an org ID, so we need to check the session file first, which does
        Map<Long, Date> ret = new HashMap<>();

        DateFormat dateFormat = new SimpleDateFormat(TppCsvToFhirTransformer.DATE_FORMAT + " " + TppCsvToFhirTransformer.TIME_FORMAT);

        //list if most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String sessionFilePath = findFilePathInExchange(exchange, "Rota");
            if (sessionFilePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(sessionFilePath);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String rotaIdStr = record.get("RowIdentifier");
                Long rotaId = Long.valueOf(rotaIdStr);

                //check if deleted (deleted column isn't always present)
                boolean isDeleted = false;
                if (parser.getHeaderMap().containsKey("RemovedData")) {
                    String removedStr = record.get("RemovedData");
                    if (removedStr.equals("1")) {
                        isDeleted = true;
                    }
                }

                if (isDeleted) {
                    ret.remove(rotaId);
                } else {

                    Date startDate = null;

                    //see if we have the start date column
                    if (parser.getHeaderMap().containsKey("DateStart")) {
                        String startDateStr = record.get("DateStart");
                        startDate = dateFormat.parse(startDateStr);
                    }

                    ret.put(rotaId, startDate); //note this start date may still be null
                }
            }

            parser.close();
        }

        return ret;
    }


}