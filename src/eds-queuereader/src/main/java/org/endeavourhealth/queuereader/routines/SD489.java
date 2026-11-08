package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ExtensionConverter;
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
import org.endeavourhealth.transform.common.resourceBuilders.AllergyIntoleranceBuilder;
import org.endeavourhealth.transform.subscriber.transforms.AllergyIntoleranceTransformer;
import org.endeavourhealth.transform.tpp.TppCsvToFhirTransformer;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD489 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD478.class);

    private static final String BULK_OPERATION_NAME = "Fixing TPP ended SRDrugSensntivities (SD-489)";

    /**
     * fixes data SD-489
     *
     * problem is that the outbound transform sent ended allergy_intolerance resources to subscriber DBs. The transform
     * has been fixed to delete them from subscribers, but we need to find any ended allergies from TPP data and trigger them
     * going through the outbound transform again. This only affects data from SRDrugSensitivity.csv since only that has an end date
     */
    public static void fixTppEndedDrugSensitivities(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing TPP ended SRDrugSensitivities at " + odsCodeRegex + " test mode = " + testMode);
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
                fixTppEndedDrugSensitivitiesAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }

            }

            LOG.debug("Finished Fixing TPP Local Codes at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixTppEndedDrugSensitivitiesAtService(boolean testMode, Service service) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No TPP endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();
        UUID exchangeId = UUID.randomUUID();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");

        Map<Long, Set<Long>> hmPatientIds = findAffectedDrugSensitivities(exchanges);
        LOG.debug("Found " + hmPatientIds.size() + " patients affected");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created: " + BULK_OPERATION_NAME, exchangeId);
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing drug sensitivities");
            fixDrugSensitivities(serviceId, hmPatientIds, filer);

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

    private static Map<Long, Set<Long>> findAffectedDrugSensitivities(List<Exchange> exchanges) throws Exception {

        Map<Long, Set<Long>> ret = new HashMap<>();

        //list if most-recent-first, so go backwards to to earliest to latest
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String filePath = findFilePathInExchange(exchange, "DrugSensitivity");
            if (filePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
            CSVParser parser = new CSVParser(isr, TppCsvToFhirTransformer.CSV_FORMAT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String idStr = record.get("RowIdentifier");
                String patientIdStr = record.get("IDPatient");
                String dateEndedStr = record.get("DateEnded");

                //not ended
                if (Strings.isNullOrEmpty(dateEndedStr)) {
                    continue;
                }

                Long patientId = Long.valueOf(patientIdStr);
                Long id = Long.valueOf(idStr);

                Set<Long> s = ret.get(patientId);
                if (s == null) {
                    s = new HashSet<>();
                    ret.put(patientId, s);
                }
                s.add(id);
            }

            parser.close();
        }

        return ret;
    }

    private static void fixDrugSensitivities(UUID serviceId, Map<Long, Set<Long>> hmPatientIds, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        for (Long patientId: hmPatientIds.keySet()) {
            Set<Long> sensitivityIds = hmPatientIds.get(patientId);

            for (Long sensitivityId: sensitivityIds) {
                ResourceType resourceType = ResourceType.AllergyIntolerance;

                //TPP transform just uses the row ID to derive the DDS UUID
                UUID resourceUuid = IdHelper.getEdsResourceId(serviceId, resourceType, "" + sensitivityId);
                if (resourceUuid == null) {
                    //may have been deleted or skipped so just warn
                    LOG.warn("No " + resourceType + " UUID found for raw ID " + sensitivityId + " on patient " + patientId);
                    continue;
                }

                AllergyIntolerance allergyIntolerance = (AllergyIntolerance)resourceDal.getCurrentVersionAsResource(serviceId, resourceType, resourceUuid.toString());
                if (allergyIntolerance == null) {
                    //if the resource has been deleted then it'll be null
                    continue;
                }

                //see if actually still ended
                if (!AllergyIntoleranceTransformer.isEnded(allergyIntolerance)) {
                    continue;
                }

                //make a token change to allow it to be saved
                ExtensionConverter.setResourceChanged(allergyIntolerance);
                changed ++;

                if (filer == null) {
                    LOG.debug("Allergy " + resourceUuid + " is ended");

                } else {
                    AllergyIntoleranceBuilder builder = new AllergyIntoleranceBuilder(allergyIntolerance);
                    filer.savePatientResource(null, false, builder);
                }
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients, fixed " + changed + " resources");
            }
        }
        LOG.debug("Finished " + done + " patients, fixed " + changed + " resources");
    }




}