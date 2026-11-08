package org.endeavourhealth.queuereader.routines;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformAudit;
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
import org.endeavourhealth.transform.common.resourceBuilders.EncounterBuilder;
import org.endeavourhealth.transform.tpp.TppCsvToFhirTransformer;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SD472 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD472.class);

    private static final String BULK_OPERATION_NAME = "Fixing TPP FHIR Encounter-Appointment links (SD-SD472)";

    /**
     * fixes data SD-472
     * problems is that TPP transform didn't deal with SREvent re-bulks without corresponding
     * SREventLink data, so has lost the link from FHIR Encounters to FHIR Appointments
     */
    public static void fixTppEncounters(boolean includeStartedButNotFinishedServices, boolean testMode, int minsDelay, String odsCodeRegex) {
        LOG.debug("Fixing TPP FHIR Encounters at " + odsCodeRegex + " test mode = " + testMode);
        LOG.debug("Mins delay = " + minsDelay);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

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
                    //check to see if already done this services
                    if (isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME, includeStartedButNotFinishedServices)) {
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                boolean doneSome = fixTppEncountersAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);

                    //throttle a bit so we don't hammer RabbitMQ too hard
                    if (doneSome) {
                        LOG.debug("Waiting " + minsDelay + " mins");
                        Thread.sleep(minsDelay * 60 * 1000);
                    }
                }
            }

            LOG.debug("Finished Fixing TPP FHIR Encounters at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static boolean fixTppEncountersAtService(boolean testMode, Service service) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No TPP endpoint found for " + service);
            return false;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();
        UUID exchangeId = UUID.randomUUID();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");

        List<UUID> affectedExchangeIds = findExchangeIds(exchanges);
        LOG.debug("Found " + affectedExchangeIds.size() + " exchange IDs");
        if (affectedExchangeIds.isEmpty()) {
            return false;
        }

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created: " + BULK_OPERATION_NAME, exchangeId);
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing Encounters");
            fixEncounters(serviceId, affectedExchangeIds, filer);

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

        return true;


    }

    /**
     * finds any recent exchange ID where the SRManifest says it was an SREvent re-bulk but
     * not an SREventLink re-bulk
     */
    private static List<UUID> findExchangeIds(List<Exchange> exchanges) throws Exception {

        List<UUID> ret = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startCutoff = dateFormat.parse("2021-05-01 00:00"); //started happening about 6th May
        Date endCutoff = dateFormat.parse("2021-05-07 14:03"); //fixed at this time

        //list if most-recent-first, so go backwards to to earliest to latest
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String filePath = findFilePathInExchange(exchange, "Manifest");
            if (filePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
            CSVParser parser = new CSVParser(isr, TppCsvToFhirTransformer.CSV_FORMAT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            boolean eventBulk = false;
            boolean eventLinkBulk = false;

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String fileName = record.get("FileName");
                String isDelta = record.get("IsDelta");
                if (isDelta.equalsIgnoreCase("Y")) {
                    continue;
                }

                if (fileName.equals("SREvent")) {
                    eventBulk = true;

                } else if (fileName.equals("SREventLink")) {
                    eventLinkBulk = true;
                }
            }

            parser.close();

            //if no SREvent re-bulk, it wasn't affected
            if (!eventBulk || eventLinkBulk) {
                continue;
            }

            //see if it was affected in the date range
            ExchangeDalI dal = DalProvider.factoryExchangeDal();
            List<ExchangeTransformAudit> transformAudits = dal.getAllExchangeTransformAudits(exchange.getServiceId(), exchange.getSystemId(), exchange.getId());
            boolean affected = false;
            for (ExchangeTransformAudit audit: transformAudits) {
                if (!audit.getStarted().before(startCutoff)
                        && !audit.getStarted().after(endCutoff)) {
                    affected = true;
                }
            }
            if (!affected) {
                continue;
            }

            //if here, it was affected
            ret.add(exchange.getId());
        }

        return ret;
    }

    private static void fixEncounters(UUID serviceId, List<UUID> exchangeIds, FhirResourceFiler filer) throws Exception {

        int doneBatches = 0;
        int checkedEncounters = 0;
        int changedEncounters = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        ExchangeBatchDalI dal = DalProvider.factoryExchangeBatchDal();

        for (UUID exchangeId: exchangeIds) {

            List<ExchangeBatch> batches = dal.retrieveForExchangeId(exchangeId);
            if (filer == null) {
                LOG.debug("Doing exchange " + exchangeId + " with " + batches.size() + " batches");
            }

            for (ExchangeBatch batch: batches) {

                UUID batchId = batch.getBatchId();
                List<ResourceWrapper> wrappers = resourceDal.getCurrentVersionOfResourcesForBatch(serviceId, batchId);
                for (ResourceWrapper wrapper: wrappers) {

                    if (wrapper.isDeleted()) {
                        continue;
                    }

                    if (wrapper.getResourceTypeObj() != ResourceType.Encounter) {
                        continue;
                    }

                    Encounter encounter = (Encounter) wrapper.getResource();
                    if (encounter.hasAppointment()) {
                        continue;
                    }

                    Reference apptRef = null;

                    //history is most-recent-first
                    List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Encounter.toString(), wrapper.getResourceId());
                    for (int i = 1; i < history.size(); i++) {
                        ResourceWrapper historyWrapper = history.get(i);
                        if (historyWrapper.isDeleted()) {
                            continue;
                        }
                        Encounter historyEncounter = (Encounter) historyWrapper.getResource();
                        if (historyEncounter.hasAppointment()) {
                            apptRef = historyEncounter.getAppointment();
                            break;
                        }
                    }

                    checkedEncounters++;

                    if (apptRef != null) {
                        changedEncounters++;

                        EncounterBuilder builder = new EncounterBuilder(encounter);
                        builder.setAppointment(apptRef);

                        if (filer == null) {
                            //LOG.debug("Would fix encounter " + wrapper.getResourceId() + " with appt ref " + apptRef.getReference());

                        } else {
                            filer.savePatientResource(null, false, builder);
                        }
                    }
                }

                doneBatches ++;

                if (doneBatches % 1000 == 0) {
                    LOG.debug("Done " + doneBatches + " batches, checked " + checkedEncounters + " encounters, fixed " + changedEncounters);
                }
            }

        }

        LOG.debug("Finished " + doneBatches + " batches, checked " + checkedEncounters + " encounters, fixed " + changedEncounters);

    }




}