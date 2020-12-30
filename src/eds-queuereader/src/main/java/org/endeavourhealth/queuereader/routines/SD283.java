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
import org.endeavourhealth.transform.emis.csv.helpers.EmisCsvHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD283 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD283.class);

    /**
     * fixes data for both SD-283 and SD-284
     */
    public static void fixEmisSessionsAndSlots(boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing Emis Sessions and Slots at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            String operationName = "Fixing Emis sessions and slots (SD-283 and SD-284)";

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                if (!testMode) {
                    if (isServiceDoneBulkOperation(service, operationName)) {
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                fixEmisSessionsAndSlotsAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, operationName);
                }

            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixEmisSessionsAndSlotsAtService(boolean testMode, Service service) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.EMIS_CSV);
        if (endpoint == null) {
            LOG.warn("No emis endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");
        Map<String, List<String>> hmSessionsAndPractitioners = findSessionPractitioners(exchanges);
        LOG.debug("Cached " + hmSessionsAndPractitioners.size() + " session GUIDs");
        Map<String, String> hmSlotsAndSessions = findSlots(exchanges);
        LOG.debug("Cached " + hmSlotsAndSessions.size() + " slot GUIDs");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.EMIS_CSV, "Manually created to Emis slot and session practitioners (SD-283 and SD-284)");
            UUID exchangeId = newExchange.getId();
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing sessions");
            fixSessions(serviceId, hmSessionsAndPractitioners, filer);
            LOG.debug("Fixing slots");
            fixSlots(serviceId, hmSessionsAndPractitioners, hmSlotsAndSessions, filer);

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

    private static void fixSessions(UUID serviceId, Map<String, List<String>> hmSessionsAndPractitioners, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        Map<String, Date> hmPractitionerDates = new HashMap<>();

        EmisCsvHelper csvHelper = new EmisCsvHelper(serviceId, null, null, null, null);

        for (String sessionGuid: hmSessionsAndPractitioners.keySet()) {
            List<String> practitionerGuids = hmSessionsAndPractitioners.get(sessionGuid);

            UUID scheduleUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Schedule, sessionGuid);
            if (scheduleUuid == null) {
                LOG.warn("No schedule UUID found for raw ID " + sessionGuid);
                continue;
            }

            /*boolean extraLogging = false;
            LOG.debug("Checking schedule " + scheduleUuid);
            if (scheduleUuid.toString().equals("1f317a5c-2291-49a3-811c-29c5fc71dd0b")) {
                LOG.debug("UUID matched one for extra logging");
                extraLogging = true;
            }*/

            Schedule schedule = (Schedule)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Schedule, scheduleUuid.toString());
            if (schedule == null) {
                LOG.warn("Missing or deleted schedule for UUID " + scheduleUuid + ", raw ID " + sessionGuid);
                continue;
            }

            if (!schedule.hasActor()) {
                LOG.warn("Schedule " + scheduleUuid + ", raw ID " + sessionGuid + " has no actor but raw data has " + practitionerGuids.size());
                continue;
            }

            Reference actorRef = schedule.getActor();
            String practitionerUuidStr = ReferenceHelper.getReferenceId(actorRef);
            boolean needToSaveSchedule = false;

            /*if (extraLogging) {
                LOG.debug("Schedule " + scheduleUuid + " has practitioner " + actorRef.getReference());
            }*/

            //if practitioner NOT exists - create it and update schedule
            Practitioner practitioner = (Practitioner)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Practitioner, practitionerUuidStr.toString());
            if (practitioner == null) {

                /*if (extraLogging) {
                    LOG.debug("Practitioner is NULL");
                }*/

                //convert practitioner UUID back to Emis GUID
                Reference rawActorRef = IdHelper.convertEdsReferenceToLocallyUniqueReference(csvHelper, actorRef);
                String rawPractitionerGuid = ReferenceHelper.getReferenceId(rawActorRef);
                CsvCell cell = CsvCell.factoryDummyWrapper(rawPractitionerGuid);

                //because the GUID->UUID mapping already exists, we need to log this as a user that has CHANGED
                csvHelper.getAdminHelper().addUserInRoleChanged(cell);
                //csvHelper.getAdminHelper().addRequiredUserInRole(cell);
                needToSaveSchedule = true;

                if (filer == null) {
                    LOG.debug("Will create missing practitioner for schedule " + scheduleUuid + ", raw ID " + sessionGuid + " - practitioner GUID " + rawPractitionerGuid + " UUID " + ReferenceHelper.getReferenceId(actorRef));
                }

            } else {
                //if practitioner created AFTER schedule (later exchange) - update schedule

                /*if (extraLogging) {
                    LOG.debug("Practitioner is not null");
                }*/

                //find date this schedule was LAST sent through to subscribers
                Date dtSchedule = findResourceDate(serviceId, ResourceType.Schedule, scheduleUuid.toString(), true);
                /*if (extraLogging) {
                    LOG.debug("Schedule was dated " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dtSchedule));
                }*/

                //find date the practitioner was FIRST sent through to subscribers (use a cache since practitioners will be referenced by lots of schedules)
                Date dtPractitioner = hmPractitionerDates.get(practitionerUuidStr);
                if (dtPractitioner == null) {
                    dtPractitioner = findResourceDate(serviceId, ResourceType.Practitioner, practitionerUuidStr, false);
                    hmPractitionerDates.put(practitionerUuidStr, dtPractitioner);
                }
                /*if (extraLogging) {
                    LOG.debug("Practitioner was dated " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dtPractitioner));
                }*/

                //if the schedule went through before the practitioner then the schedule
                //needs to go through again to refresh the schedule table record
                if (dtSchedule.before(dtPractitioner)) {

                    /*if (extraLogging) {
                        LOG.debug("Schedule was created before practitioner so needs updating");
                    }*/

                    needToSaveSchedule = true;

                    if (filer == null) {
                        LOG.debug("Need to refresh schedule " + scheduleUuid + ", raw ID " + sessionGuid + " because transformed before practitioner existed");
                    }
                }
            }

            //if we need to send the schedule through again, then we need to change it in some way that will bypass the checksum checking
            //in FhirResourceFiler
            if (needToSaveSchedule) {

                ExtensionConverter.setResourceChanged(schedule); //creates an artificial change so FhirResourceFiler will save it
                ScheduleBuilder builder = new ScheduleBuilder(schedule);

                if (filer != null) {
                    filer.saveAdminResource(null, false, builder);
                }
                changed ++;
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " / " + hmSessionsAndPractitioners.size() + " schedules, changed " + changed);
            }
        }

        LOG.debug("Finished " + done + " schedules, changed " + changed);

        //generate new practitioners
        if (filer != null) {
            LOG.debug("Generating missing Practitioners");
            csvHelper.getAdminHelper().processAdminChanges(filer, csvHelper);
        }
    }

    private static Date findResourceDate(UUID serviceId, ResourceType resourceType, String resourceUuid, boolean findMostRecentDate) throws Exception {

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, resourceType.toString(), UUID.fromString(resourceUuid));
        ResourceWrapper entry = null;

        //history is most-recent-first
        if (findMostRecentDate) {
            entry = history.get(0);
        } else {
            entry = history.get(history.size()-1);
        }

        return entry.getCreatedAt();

        //we need to get the timestamp of the exchange because resources will be saved at a range of times from processing
        //a single exchange, but we need to work out if they were processed in the same exchange or a later one
        /*UUID batchId = entry.getExchangeBatchId();

        ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();
        ExchangeBatch batch = exchangeBatchDal.getForBatchId(batchId);

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        Exchange exchange = exchangeDal.getExchange(batch.getExchangeId());

        return exchange.getTimestamp();*/
    }

    /**
     * FHIR Slots don't have their Practitioner references set (see SD-284)
     */
    private static void fixSlots(UUID serviceId, Map<String, List<String>> hmSessionsAndPractitioners, Map<String, String> hmSlotsAndSessions, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        Map<String, Date> hmPractitionerDates = new HashMap<>();

        for (String combinedRawId: hmSlotsAndSessions.keySet()) {
            String sessionGuid = hmSlotsAndSessions.get(combinedRawId);

            UUID appointmentUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Appointment, combinedRawId);
            if (appointmentUuid == null) {
                LOG.warn("No appointment UUID found for raw ID " + combinedRawId);
                continue;
            }

            Appointment appointment = (Appointment)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Appointment, appointmentUuid.toString());
            if (appointment == null) {
                LOG.warn("Missing or deleted appointment for UUID " + appointmentUuid + ", raw ID " + combinedRawId);
                continue;
            }

            List<String> practitionerGuids = hmSessionsAndPractitioners.get(sessionGuid);
            if (practitionerGuids == null) {
                LOG.warn("No practitioner GUIDs found for appointment " + appointmentUuid + ", raw ID " + combinedRawId + ", session GUID " + sessionGuid);
                continue;
            }

            AppointmentBuilder builder = new AppointmentBuilder(appointment);
            boolean madeChange = false;

            for (String practitionerGuid: practitionerGuids) {

                UUID practitionerUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Practitioner, practitionerGuid);
                if (practitionerUuid == null) {
                    LOG.warn("No practitioner UUID found for GUID " + practitionerGuid + " when doing appointment " + appointmentUuid + ", raw ID " + combinedRawId);
                    continue;
                }

                Reference mappedPractitionerRef = ReferenceHelper.createReference(ResourceType.Practitioner, practitionerUuid.toString());

                //only add if not found
                boolean found = false;
                for (Appointment.AppointmentParticipantComponent participant: appointment.getParticipant()) {
                    Reference ref = participant.getActor();
                    if (ReferenceHelper.equals(ref, mappedPractitionerRef)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    builder.addParticipant(mappedPractitionerRef, Appointment.ParticipationStatus.ACCEPTED);
                    madeChange = true;
                    changed ++;

                    //if no filer, we're in test mode, so log it out
                    if (filer == null) {
                        LOG.debug("Appointment " + appointmentUuid + ", raw ID " + combinedRawId + " was missing practitioner " + practitionerUuid + ", raw ID " + practitionerGuid);
                    }
                }
            }

            //the FHIR->Compass transforms for Appointments used the LAST practitioner, but the Schedule transform used the FIRST. The Appointment
            //transform has been changed to be consistent, but any appt with multiple practitioners should go through the outbound transform again to fix it
            if (!madeChange) {
                int practitionerCount = findPractitionerCount(appointment);
                if (practitionerCount > 1) {

                    ExtensionConverter.setResourceChanged(appointment);
                    madeChange = true;
                    changed ++;

                    if (filer == null) {
                        LOG.debug("Need to refresh appointment " + appointmentUuid + ", raw ID " + combinedRawId + " because has " + practitionerCount + " practitioners");
                    }
                }
            }

            //if we didn't make a change, we still may need to change the Appt so that it goes through the outbound transform again,
            //if the Practitioner wasn't created until AFTER the Appointment was created. This is the same problem as affected the Schedules too.
            if (!madeChange) {
                Date dtAppointment = findResourceDate(serviceId, ResourceType.Appointment, appointmentUuid.toString(), true);

                //find the first practitioner
                String practitionerUuidStr = findFirstPractitionerUuid(appointment);
                if (!Strings.isNullOrEmpty(practitionerUuidStr)) {

                    //find date the practitioner was FIRST sent through to subscribers (use a cache since practitioners will be referenced by lots of schedules)
                    Date dtPractitioner = hmPractitionerDates.get(practitionerUuidStr);
                    if (dtPractitioner == null) {
                        dtPractitioner = findResourceDate(serviceId, ResourceType.Practitioner, practitionerUuidStr, false);
                        hmPractitionerDates.put(practitionerUuidStr, dtPractitioner);
                    }

                    if (dtAppointment.before(dtPractitioner)) {

                        ExtensionConverter.setResourceChanged(appointment);
                        madeChange = true;
                        changed ++;

                        if (filer == null) {
                            LOG.debug("Need to refresh appointment " + appointmentUuid + ", raw ID " + combinedRawId + " because transformed before practitioner existed");
                        }
                    }
                }
            }


            if (filer != null && madeChange) {
                filer.savePatientResource(null, false, builder);
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " / " + hmSlotsAndSessions.size() + " slots, changed " + changed);
            }
        }

        LOG.debug("Finished " + done + " slots, changed " + changed);

    }

    private static int findPractitionerCount(Appointment appointment) {
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
    }

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


    private static Map<String, String> findSlots(List<Exchange> exchanges) throws Exception {

        Map<String, String> ret = new HashMap<>();

        //list if most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String filePath = findFilePathInExchange(exchange, "Appointment_Slot");
            if (filePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String slotGuid = record.get("SlotGuid");
                String patientGuid = record.get("PatientGuid");
                if (Strings.isNullOrEmpty(patientGuid)) {
                    continue;
                }
                String combinedId = patientGuid + ":" + slotGuid;
                String deleted = record.get("Deleted");
                String sessionGuid = record.get("SessionGuid");
                boolean isDeleted = Boolean.parseBoolean(deleted);

                if (isDeleted) {
                    ret.remove(combinedId);
                } else {
                    ret.put(combinedId, sessionGuid);
                }
            }

            parser.close();

        }

        return ret;
    }

    private static Map<String, List<String>> findSessionPractitioners(List<Exchange> exchanges) throws Exception {

        //the session user file doesn't have an org ID, so we need to check the session file first, which does
        Set<String> sessionGuids = new HashSet<>();

        //list if most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String sessionFilePath = findFilePathInExchange(exchange, "Appointment_Session");
            if (sessionFilePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(sessionFilePath);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String sessionGuid = record.get("AppointmentSessionGuid");
                String deleted = record.get("Deleted");
                boolean isDeleted = Boolean.parseBoolean(deleted);

                if (isDeleted) {
                    sessionGuids.remove(sessionGuid);

                } else {
                    sessionGuids.add(sessionGuid);
                }
            }

            parser.close();
        }

        Map<String, List<String>> ret = new HashMap<>();

        //list if most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String sessionUserFilePath = findFilePathInExchange(exchange, "Appointment_SessionUser");
            if (sessionUserFilePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(sessionUserFilePath);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String sessionGuid = record.get("SessionGuid");
                //skip any session GUIDs for other orgs
                if (!sessionGuids.contains(sessionGuid)) {
                    continue;
                }
                String userInRoleGuid = record.get("UserInRoleGuid");
                String deleted = record.get("Deleted");
                boolean isDeleted = Boolean.parseBoolean(deleted);

                List<String> l = ret.get(sessionGuid);
                if (l == null) {
                    l = new ArrayList<>();
                    ret.put(sessionGuid, l);
                }

                if (isDeleted) {
                    l.remove(userInRoleGuid);

                } else if (!l.contains(userInRoleGuid)) { //don't add duplicates
                    l.add(userInRoleGuid);
                }
            }

            parser.close();

        }

        return ret;
    }


}