package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
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
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.ScheduleBuilder;
import org.endeavourhealth.transform.tpp.csv.transforms.appointment.SRVisitTransformer;
import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD322 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD322.class);

    /**
     * finds affected TPP services for SD-322
     * simply updated all FHIR Schedules with a free-text location (location type extension) so
     * that they go through the outbound transform again
     */
    public static void fixTppSessions(boolean includeStartedButNotFinishedServices, String odsCodeRegex) {
        LOG.debug("Fixing TPP Sessions at " + odsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            String bulkOperationName = "Fixing TPP sessions and slots (SD-322)";

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

                LOG.debug("Doing " + service);
                fixTppSessionsAtService(service);

                //record as done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished Fixing TPP Sessions at " + odsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixTppSessionsAtService(Service service) throws Exception {

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

        Set<String> hsRotaIds = findRotaIds(exchanges);
        LOG.debug("Cached " + hsRotaIds.size() + " rota");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created to fix TPP sessions (SD-322)");
        UUID exchangeId = newExchange.getId();
        filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

        try {
            LOG.debug("Fixing rotas");
            fixRotas(serviceId, hsRotaIds, filer);


        } catch (Throwable ex) {
            LOG.error("Error doing service " + service, ex);
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

            //we'll have a load of stuff cached in here, so clear it down as it won't be applicable to the next service
            IdHelper.clearCache();
        }
    }

    /**
     * SD-280 - set StartDate on FHIR Schedules (from SRRota file or SRAppointment)
     * SD-281 - set Practitioner on FHIR Schedules (from SRAppointment)
     */
    private static void fixRotas(UUID serviceId,
                                 Set<String> hsRotaIds,
                                 FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        for (String rotaId: hsRotaIds) {

            UUID scheduleUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Schedule, rotaId);
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

            boolean makeChange = ExtensionConverter.hasExtension(schedule, FhirExtensionUri.SCHEDULE_LOCATION_TYPE);

            if (makeChange) {

                //just make a change so it will go through the queues
                ExtensionConverter.setResourceChanged(schedule);

                changed ++;
                if (filer != null) {
                    filer.saveAdminResource(null, false, builder);
                }
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " / " + hsRotaIds.size() + " rotas, changed " + changed);
            }
        }

        //make sure to create any missing practitioners we found
        if (filer != null) {
            //need to make sure all Practitioners are saved to the DB before going on to do Slots
            filer.waitUntilEverythingIsSaved();
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


    private static Set<String> findRotaIds(List<Exchange> exchanges) throws Exception {

        //the session user file doesn't have an org ID, so we need to check the session file first, which does
        Set<String> ret = new HashSet<>();

        //list if most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String rotaFilePath = findFilePathInExchange(exchange, "Rota");
            if (!Strings.isNullOrEmpty(rotaFilePath)) {

                InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(rotaFilePath);
                CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
                Iterator<CSVRecord> iterator = parser.iterator();

                while (iterator.hasNext()) {
                    CSVRecord record = iterator.next();

                    String rotaIdStr = record.get("RowIdentifier");

                    //check if deleted (deleted column isn't always present)
                    boolean isDeleted = false;
                    if (parser.getHeaderMap().containsKey("RemovedData")) {
                        String removedStr = record.get("RemovedData");
                        if (removedStr.equals("1")) {
                            isDeleted = true;
                        }
                    }

                    if (isDeleted) {
                        ret.remove(rotaIdStr);
                    } else {
                        ret.add(rotaIdStr);
                    }
                }

                parser.close();
            }

            //we also generate FHIR Schedules from SRVisit, so need to parse that too
            String visitFilePath = findFilePathInExchange(exchange, "Visit");
            if (!Strings.isNullOrEmpty(visitFilePath)) {

                InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(visitFilePath);
                CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
                Iterator<CSVRecord> iterator = parser.iterator();

                while (iterator.hasNext()) {
                    CSVRecord record = iterator.next();

                    String visitIdStr = record.get("RowIdentifier");
                    String rotaIdStr = SRVisitTransformer.VISIT_ID_PREFIX + visitIdStr;

                    //check if deleted (deleted column isn't always present)
                    boolean isDeleted = false;
                    if (parser.getHeaderMap().containsKey("RemovedData")) {
                        String removedStr = record.get("RemovedData");
                        if (removedStr.equals("1")) {
                            isDeleted = true;
                        }
                    }

                    if (isDeleted) {
                        ret.remove(rotaIdStr);
                    } else {
                        ret.add(rotaIdStr);
                    }
                }

                parser.close();
            }
        }

        return ret;
    }


}