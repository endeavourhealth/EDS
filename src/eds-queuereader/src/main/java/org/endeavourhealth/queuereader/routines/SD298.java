package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
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
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.EncounterBuilder;
import org.endeavourhealth.transform.tpp.csv.transforms.appointment.SRVisitTransformer;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD298 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD298.class);


    /**
     * fixes data for SD-298
     */
    public static void fixTppEncounterLinks(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing TPP Encounter Links to Appointments and Visits at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            String bulkOperationName = "Fixing TPP encounter links (SD-298)";

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
                fixTppEncounterLinksAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, bulkOperationName);
                }

            }

            LOG.debug("Finished Fixing TPP Encounter Links to Appointments and Visits at " + odsCodeRegex + " test mode = " + testMode);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixTppEncounterLinksAtService(boolean testMode, Service service) throws Exception {

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

        Map<Long, String> hmEncounterLinks = findEncounterLinks(exchanges);
        LOG.debug("Cached " + hmEncounterLinks.size() + " links");


        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created to fix TPP encounter links (SD-298)");
            UUID exchangeId = newExchange.getId();
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing encounters");
            fixEncounters(serviceId, hmEncounterLinks, filer);

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

    private static void fixEncounters(UUID serviceId, Map<Long, String> hmEncounterLinks, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;
        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        for (Long eventId: hmEncounterLinks.keySet()) {

            String target = hmEncounterLinks.get(eventId);

            UUID encounterUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Encounter, "" + eventId);
            if (encounterUuid == null) {
                LOG.warn("No Encounter UUID found for Event ID " + eventId);
                continue;
            }

            Encounter encounter = (Encounter)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Encounter, encounterUuid.toString());
            if (encounter == null) {
                LOG.warn("Missing or deleted Encounter for UUID " + encounterUuid + ", raw ID " + eventId);
                continue;
            }

            EncounterBuilder builder = new EncounterBuilder(encounter);

            if (!encounter.hasAppointment()) {

                UUID targetUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Appointment, target);
                if (targetUuid == null) {
                    LOG.warn("No Appointment UUID found for source ID " + target);
                    continue;
                }

                Reference reference = ReferenceHelper.createReference(ResourceType.Appointment, targetUuid.toString());
                builder.setAppointment(reference);

                changed ++;
                if (filer != null) {
                    filer.savePatientResource(null, false, builder);

                } else {
                    LOG.debug("Updated Encounter " + encounterUuid + ", raw Event ID " + eventId + " to link to Appointment " + targetUuid + ", raw ID " + target);
                }
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " / " + hmEncounterLinks.size() + " events, changed " + changed);
            }
        }

        LOG.debug("Finished " + done + " / " + hmEncounterLinks.size() + " events, changed " + changed);

    }

    private static Map<Long, String> findEncounterLinks(List<Exchange> exchanges) throws Exception {

        Map<Long, String> ret = new HashMap<>();


        //list if most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String filePath = findFilePathInExchange(exchange, "EventLink");
            if (filePath == null) {
                continue;
            }

            try {
                InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
                CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
                Iterator<CSVRecord> iterator = parser.iterator();

                while (iterator.hasNext()) {
                    CSVRecord record = iterator.next();

                    String eventIdStr = record.get("IDEvent");
                    String apptIdStr = record.get("IDAppointment");
                    String visitIdStr = record.get("IDVisit");

                    Long eventId = Long.valueOf(eventIdStr);

                    Long apptId = null;
                    if (!Strings.isNullOrEmpty(apptIdStr)
                            && !apptIdStr.equals("-1")) {
                        apptId = Long.valueOf(apptIdStr);
                    }

                    Long visitId = null;
                    if (!Strings.isNullOrEmpty(visitIdStr)
                            && !visitIdStr.equals("-1")) {
                        visitId = Long.valueOf(visitIdStr);
                    }

                    if (apptId != null && visitId != null) {
                        throw new Exception("Got both appt ID " + apptId + " and visit ID " + visitId + " for event " + eventId + " in " + filePath);

                    } else if (apptId != null) {
                        ret.put(eventId, "" + apptId);

                    } else if (visitId != null) {
                        ret.put(eventId, SRVisitTransformer.VISIT_ID_PREFIX + visitId); //SRVisit has a prefix to differentiate from SRAppointment

                    } else {
                        //do nothing
                    }

                }

                parser.close();
            } catch (Exception ex) {
                throw new Exception("Exception reading file " + filePath, ex);
            }
        }

        return ret;
    }
}
