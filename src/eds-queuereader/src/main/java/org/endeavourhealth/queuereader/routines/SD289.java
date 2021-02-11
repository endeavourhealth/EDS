package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.publisherTransform.InternalIdDalI;
import org.endeavourhealth.core.database.dal.publisherTransform.models.InternalIdMap;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.AppointmentBuilder;
import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD289 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD289.class);

    private static final String BULK_OPERATION_NAME = "Fixing Emis deleted slots (SD-289)";

    /**
     * fixes data SD-289
     *
     * problem is that we un-cancelled appts back to booked or similar
     * DONE need to find all patient IDs associated with a slot
     * DONE update the internal map table to have all patient GUIDs in there
     * DONE make sure all appts are cancelled that should be
     */
    public static void fixEmisDeletedSlots(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing Emis Deleted Slots at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

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
                    //check to see if already done this services
                    if (isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME, includeStartedButNotFinishedServices)) {
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                fixEmisDeletedSlotsAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }

            }

            LOG.debug("Finished Fixing Emis Deleted Slots at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixEmisDeletedSlotsAtService(boolean testMode, Service service) throws Exception {

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
        Map<String, List<String>> hmSlotsAndPatients = findSlots(exchanges);
        LOG.debug("Cached " + hmSlotsAndPatients.size() + " slot GUIDs");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.EMIS_CSV, "Manually created: " + BULK_OPERATION_NAME);
            UUID exchangeId = newExchange.getId();
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing slots");
            fixSlots(serviceId, hmSlotsAndPatients, filer);

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

    private static void fixSlots(UUID serviceId, Map<String, List<String>> hmSlotsAndPatients, FhirResourceFiler filer) throws Exception {

        int done = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        InternalIdDalI internalIdDal = DalProvider.factoryInternalIdDal();

        for (String slotGuid: hmSlotsAndPatients.keySet()) {
            List<String> patientGuids = hmSlotsAndPatients.get(slotGuid);

            //update the internal map table to have all patient GUIDs in there
            Set<String> hsPatientGuids = new HashSet<>();
            for (String patientGuid: patientGuids) {
                if (!Strings.isNullOrEmpty(patientGuid)) {
                    hsPatientGuids.add(patientGuid);
                }
            }

            //if we've never had any patient associated with this slot, then skip it entirely
            if (hsPatientGuids.isEmpty()) {
                continue;
            }

            //only save if filer is NOT NULL
            if (filer != null) {

                String mappingStr = String.join("|", patientGuids);
                InternalIdMap m = new InternalIdMap();
                m.setServiceId(serviceId);
                m.setIdType("EmisSlotLatestPatientGuid");
                m.setSourceId(slotGuid);
                m.setDestinationId(mappingStr);
                List<InternalIdMap> l = new ArrayList<>();
                l.add(m);

                internalIdDal.save(l);

            } else {
                LOG.debug("Slot " + slotGuid + " associated with " + hsPatientGuids.size() + " patient GUIDS: [" + hsPatientGuids + "]");
            }

            //now ensure that all patients except the last (if still in the slot) have their appts cancelled
            for (int i=0; i<patientGuids.size()-1; i++) { //doing ALL BUT THE LAST
                String patientGuid = patientGuids.get(i);
                String combinedId = patientGuid + ":" + slotGuid;

                UUID appointmentUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Appointment, combinedId);
                if (appointmentUuid == null) {
                    //we don't create appts for patients we've never heard of, so will have some without UUIDs
                    LOG.warn("No appointment UUID found for raw ID " + combinedId);
                    continue;
                }

                Appointment appointment = (Appointment)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Appointment, appointmentUuid.toString());
                if (appointment == null) {
                    //appts may have been deleted, so that's nothing to worry about
                    LOG.warn("Missing or deleted appointment for UUID " + appointmentUuid + ", raw ID " + combinedId);
                    continue;
                }

                //if the appt is already cancelled or DNA, then it's OK
                if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED
                        || appointment.getStatus() == Appointment.AppointmentStatus.NOSHOW) {
                    continue;
                }

                if (filer != null) {
                    AppointmentBuilder appointmentBuilder = new AppointmentBuilder(appointment);
                    appointmentBuilder.setStatus(Appointment.AppointmentStatus.CANCELLED);

                    //save without mapping IDs as this has been retrieved from the DB
                    filer.savePatientResource(null, false, appointmentBuilder);

                } else {
                    LOG.debug("Setting appointment UUID " + appointmentUuid + ", raw ID " + combinedId + " to cancelled (currently " + appointment.getStatus() + ")");
                }
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done);
            }
        }
        LOG.debug("Finished " + done);
    }


    /**
     * finds all patient guids associated with a slot, in order, including when it was blank
     */
    private static Map<String, List<String>> findSlots(List<Exchange> exchanges) throws Exception {

        Map<String, List<String>> ret = new HashMap<>();

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

                List<String> patientGuids = ret.get(slotGuid);
                if (patientGuids == null) {
                    patientGuids = new ArrayList<>();
                    ret.put(slotGuid, patientGuids);
                }

                //if it's the same as the last in the list, skip it
                if (!patientGuids.isEmpty()) {
                    String lastGuid = patientGuids.get(patientGuids.size()-1);
                    if (lastGuid.equals(patientGuid)) {
                        continue;
                    }
                }

                patientGuids.add(patientGuid);
            }

            parser.close();

        }

        return ret;
    }

}