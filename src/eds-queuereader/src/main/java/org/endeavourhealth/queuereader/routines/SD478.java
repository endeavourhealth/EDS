package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirCodeUri;
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
import org.endeavourhealth.transform.common.resourceBuilders.ImmunizationBuilder;
import org.endeavourhealth.transform.tpp.TppCsvToFhirTransformer;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD478 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD478.class);

    private static final String BULK_OPERATION_NAME = "Fixing TPP local codes (SD-478)";

    /**
     * fixes data SD-478
     *
     * problems is that TPP local codes were incorrectly updated by SD-385 fix
     * - need to find all affected records
     * - need to fix all affected FHIR resources (Immunisation ONLY)
     * - send through subscriber queue reader
     */
    public static void fixTppLocalCodes(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing TPP Local Codes at " + odsCodeRegex + " test mode = " + testMode);
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
                fixTppLocalCodesAtService(testMode, service);

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

    private static void fixTppLocalCodesAtService(boolean testMode, Service service) throws Exception {

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

        Map<String, Set<Long>> hmPatientImmunisationIds = findImmunisationIds(exchanges);
        LOG.debug("Cached " + hmPatientImmunisationIds.size() + " patients affected");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created: " + BULK_OPERATION_NAME, exchangeId);
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing imms");
            fixLocalCodes(serviceId, hmPatientImmunisationIds, filer);

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

    private static void fixLocalCodes(UUID serviceId, Map<String, Set<Long>> hmPatientImmunisationIds, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        for (String patientId: hmPatientImmunisationIds.keySet()) {
            Set<Long> cacheObjs = hmPatientImmunisationIds.get(patientId);

            for (Long cacheObj: cacheObjs) {
                long immunisationId = cacheObj.longValue();
                ResourceType resourceType = ResourceType.Immunization;

                UUID resourceUuid = IdHelper.getEdsResourceId(serviceId, resourceType, "" + immunisationId);
                if (resourceUuid == null) {
                    //we don't create appts for patients we've never heard of, so will have some without UUIDs
                    LOG.warn("No " + resourceType + " UUID found for raw ID " + immunisationId);
                    continue;
                }

                Immunization immunization = (Immunization)resourceDal.getCurrentVersionAsResource(serviceId, resourceType, resourceUuid.toString());
                if (immunization == null) {
                    //if the resource has been deleted then it'll be null
                    continue;
                }

                boolean fixed = fixImmunization(immunisationId, immunization, filer);

                if (fixed) {
                    changed ++;
                }
            }


            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients, fixed " + changed + " resources");
            }
        }
        LOG.debug("Finished " + done + " patients, fixed " + changed + " resources");
    }

    private static boolean fixImmunization(long rawId, Immunization resource, FhirResourceFiler filer) throws Exception {

        String changeDesc = fixCodeableConcept(resource.getVaccineCode());
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            ImmunizationBuilder builder = new ImmunizationBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + rawId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }


    private static String fixCodeableConcept(CodeableConcept codeableConcept) throws Exception {

        Coding originalCoding = CodeableConceptHelper.findOriginalCoding(codeableConcept);

        String system = originalCoding.getSystem();
        String code = originalCoding.getCode();

        if (code.startsWith("Y")) {

            if (system.equals(FhirCodeUri.CODE_SYSTEM_CTV3)) {
                //wrong
                originalCoding.setSystem(FhirCodeUri.CODE_SYSTEM_TPP_CTV3);
                return "Changed to TPP local";

            } else if (system.equals(FhirCodeUri.CODE_SYSTEM_TPP_CTV3)) {
                //OK
                return null;

            } else {
                throw new Exception("Unexpected system " + system);
            }

        } else {

            if (system.equals(FhirCodeUri.CODE_SYSTEM_CTV3)) {
                //OK
                return null;

            } else if (system.equals(FhirCodeUri.CODE_SYSTEM_TPP_CTV3)) {
                //wrong
                originalCoding.setSystem(FhirCodeUri.CODE_SYSTEM_CTV3);
                return "Changed to CTV3";

            } else {
                throw new Exception("Unexpected system " + system);
            }
        }
    }



    /**
     * finds a map of affected patient IDs and immunisation IDs
     */
    private static Map<String, Set<Long>> findImmunisationIds(List<Exchange> exchanges) throws Exception {

        Map<String, Set<Long>> ret = new HashMap<>();

        //problem came from SRImmunisation records that WERE Y code but changed to proper CTV3 codes subsequently
        Set<Long> idsWithYCodes = new HashSet<>();

        //list if most-recent-first, so go backwards to to earliest to latest
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String filePath = findFilePathInExchange(exchange, "Immunisation");
            if (filePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
            CSVParser parser = new CSVParser(isr, TppCsvToFhirTransformer.CSV_FORMAT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String id = record.get("RowIdentifier");
                String patientId = record.get("IDPatient");
                String readCode = record.get("ImmsReadCode");

                readCode = readCode.trim(); //CsvCell does this automatically but do manually here

                //not sure if this is possible, but ignore any records like this
                if (Strings.isNullOrEmpty(readCode)) {
                    continue;
                }

                if (readCode.startsWith("Y")) {
                    //if the ID a Y code then add to the set to track
                    idsWithYCodes.add(new Long(id));

                } else if (idsWithYCodes.contains(new Long(id))){

                    //if the ID isn't a Y code but was previously added to the set, then it's affected
                    Set<Long> l = ret.get(patientId);
                    if (l == null) {
                        l = new HashSet<>();
                        ret.put(patientId, l);
                    }

                    l.add(Long.valueOf(id));
                }
            }

            parser.close();
        }

        return ret;
    }


}