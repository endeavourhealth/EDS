package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirCodeUri;
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
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.common.resourceBuilders.*;
import org.endeavourhealth.transform.vision.VisionCsvHelper;
import org.endeavourhealth.transform.vision.VisionCsvToFhirTransformer;
import org.endeavourhealth.transform.vision.helpers.VisionCodeHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD363 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD363.class);

    private static final String BULK_OPERATION_NAME = "Fixing Vision local codes (SD-363)";

    /**
     * fixes data SD-363
     *
     * problems is that Vision local codes were flagged as being true Read2 codes
     * - need to find all affected JOURNAL records
     * - need to fix all affected FHIR resources (Observation, Procedure etc.)
     * - send through subscriber queue reader
     */
    public static void fixVisionLocalCodes(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing Vision Local Codes at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("Vision")) {
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
                fixVisionLocalCodesAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }

            }

            LOG.debug("Finished Fixing Vision Local Codes at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixVisionLocalCodesAtService(boolean testMode, Service service) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.VISION_CSV);
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

        VisionCsvHelper csvHelper = new VisionCsvHelper(serviceId, systemId, exchangeId);
        Map<String, List<CacheObj>> hmPatientJournalIds = findJournalIds(exchanges, csvHelper);
        LOG.debug("Cached " + hmPatientJournalIds.size() + " patients affected");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.VISION_CSV, "Manually created: " + BULK_OPERATION_NAME, exchangeId);
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing slots");
            fixLocalCodes(serviceId, hmPatientJournalIds, filer);

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

    private static void fixLocalCodes(UUID serviceId, Map<String, List<CacheObj>> hmPatientJournalIds, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        for (String patientId: hmPatientJournalIds.keySet()) {
            List<CacheObj> journalIds = hmPatientJournalIds.get(patientId);

            for (CacheObj journalObj: journalIds) {
                String journalId = journalObj.getJournalId();

                String combinedId = VisionCsvHelper.createUniqueId(patientId, journalId);
                Set<ResourceType> resourceTypes = findOriginalTargetResourceTypes(serviceId, combinedId);
                if (resourceTypes.isEmpty()) {
                    LOG.warn("No resource types found for combined ID " + combinedId);
                    continue;
                }

                for (ResourceType resourceType: resourceTypes) {

                    UUID resourceUuid = IdHelper.getEdsResourceId(serviceId, resourceType, combinedId);
                    if (resourceUuid == null) {
                        //we don't create appts for patients we've never heard of, so will have some without UUIDs
                        LOG.warn("No " + resourceType + " UUID found for raw ID " + combinedId);
                        continue;
                    }

                    Resource resource = resourceDal.getCurrentVersionAsResource(serviceId, resourceType, resourceUuid.toString());
                    if (resource == null) {
                        //if the resource has been deleted then it'll be null
                        continue;
                    }

                    boolean fixed = false;

                    if (resource instanceof Observation) {
                        fixed = fixObservation(combinedId, (Observation)resource, filer, journalObj);

                    } else if (resource instanceof Condition) {
                        fixed = fixCondition(combinedId, (Condition)resource, filer, journalObj);

                    } else if (resource instanceof Procedure) {
                        fixed = fixProcedure(combinedId, (Procedure)resource, filer, journalObj);

                    } else if (resource instanceof AllergyIntolerance) {
                        fixed = fixAllergyIntolerance(combinedId, (AllergyIntolerance)resource, filer, journalObj);

                    } else if (resource instanceof FamilyMemberHistory) {
                        fixed = fixFamilyMemberHistory(combinedId, (FamilyMemberHistory)resource, filer, journalObj);

                    } else if (resource instanceof Immunization) {
                        fixed = fixImmunization(combinedId, (Immunization)resource, filer, journalObj);

                    } else if (resource instanceof MedicationStatement) {
                        fixed = fixMedicationStatement(combinedId, (MedicationStatement)resource, filer, journalObj);

                    } else if (resource instanceof MedicationOrder) {
                        fixed = fixMedicationOrder(combinedId, (MedicationOrder)resource, filer, journalObj);

                    } else {
                        LOG.error("Unknown resource type " + resource.getResourceType());
                    }

                    if (fixed) {
                        changed ++;
                    }
                }
            }


            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients, fixed " + changed + " resources");
            }
        }
        LOG.debug("Finished " + done + " patients, fixed " + changed + " resources");
    }

    private static boolean fixMedicationOrder(String combinedId, MedicationOrder resource, FhirResourceFiler filer, CacheObj journalObj) throws Exception {

        String changeDesc = fixCodeableConcept(resource.getMedicationCodeableConcept(), journalObj);
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            MedicationOrderBuilder builder = new MedicationOrderBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + combinedId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }

    private static boolean fixMedicationStatement(String combinedId, MedicationStatement resource, FhirResourceFiler filer, CacheObj journalObj) throws Exception {

        String changeDesc = fixCodeableConcept(resource.getMedicationCodeableConcept(), journalObj);
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            MedicationStatementBuilder builder = new MedicationStatementBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + combinedId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }

    private static boolean fixImmunization(String combinedId, Immunization resource, FhirResourceFiler filer, CacheObj journalObj) throws Exception {

        String changeDesc = fixCodeableConcept(resource.getVaccineCode(), journalObj);
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            ImmunizationBuilder builder = new ImmunizationBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + combinedId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }

    private static boolean fixFamilyMemberHistory(String combinedId, FamilyMemberHistory resource, FhirResourceFiler filer, CacheObj journalObj) throws Exception {

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent condition = resource.getCondition().get(0);
        String changeDesc = fixCodeableConcept(condition.getCode(), journalObj);
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            FamilyMemberHistoryBuilder builder = new FamilyMemberHistoryBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + combinedId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }

    private static boolean fixAllergyIntolerance(String combinedId, AllergyIntolerance resource, FhirResourceFiler filer, CacheObj journalObj) throws Exception {

        String changeDesc = fixCodeableConcept(resource.getSubstance(), journalObj);
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            AllergyIntoleranceBuilder builder = new AllergyIntoleranceBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + combinedId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }

    private static boolean fixProcedure(String combinedId, Procedure resource, FhirResourceFiler filer, CacheObj journalObj) throws Exception {

        String changeDesc = fixCodeableConcept(resource.getCode(), journalObj);
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            ProcedureBuilder builder = new ProcedureBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + combinedId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }

    private static boolean fixCondition(String combinedId, Condition resource, FhirResourceFiler filer, CacheObj journalObj) throws Exception {

        String changeDesc = fixCodeableConcept(resource.getCode(), journalObj);
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            ConditionBuilder builder = new ConditionBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + combinedId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }

    private static boolean fixObservation(String combinedId, Observation resource, FhirResourceFiler filer, CacheObj journalObj) throws Exception {

        String changeDesc = fixCodeableConcept(resource.getCode(), journalObj);
        if (Strings.isNullOrEmpty(changeDesc)) {
            return false;
        }

        if (filer != null) {
            ObservationBuilder builder = new ObservationBuilder(resource);
            filer.savePatientResource(null, false, builder);
        } else {
            LOG.debug("Fixed " + combinedId + " " + resource.getResourceType() + "/" + resource.getId() + ": " + changeDesc);
        }
        return true;
    }

    private static String fixCodeableConcept(CodeableConcept codeableConcept, CacheObj journalObj) {

        Coding coding = CodeableConceptHelper.findCoding(codeableConcept, FhirCodeUri.CODE_SYSTEM_READ2);
        if (coding == null) {
            return null;
        }

        coding.setSystem(FhirCodeUri.CODE_SYSTEM_VISION_CODE);
        String ret = "Changed system";

        String newFormattedCode = journalObj.getFormattedReadCode();
        String code = coding.getCode();
        if (!code.equals(newFormattedCode)) {
            coding.setCode(newFormattedCode);
            ret += ", changed code [" + code + "] -> [" + newFormattedCode + "]";
        }

        return ret;
    }

    private static Set<ResourceType> findOriginalTargetResourceTypes(UUID serviceId, String combinedId) throws Exception {

        List<ResourceType> potentialResourceTypes = new ArrayList<>();
        potentialResourceTypes.add(ResourceType.Observation);
        potentialResourceTypes.add(ResourceType.Condition);
        potentialResourceTypes.add(ResourceType.Procedure);
        potentialResourceTypes.add(ResourceType.AllergyIntolerance);
        potentialResourceTypes.add(ResourceType.FamilyMemberHistory);
        potentialResourceTypes.add(ResourceType.Immunization);
        potentialResourceTypes.add(ResourceType.MedicationStatement);
        potentialResourceTypes.add(ResourceType.MedicationOrder);

        Set<Reference> sourceReferences = new HashSet<>();
        for (ResourceType resourceType: potentialResourceTypes) {
            Reference ref = ReferenceHelper.createReference(resourceType, combinedId);
            sourceReferences.add(ref);
        }

        Map<Reference, UUID> idMap = IdHelper.getEdsResourceIds(serviceId, sourceReferences);

        Set<ResourceType> ret = new HashSet<>();

        for (Reference ref: sourceReferences) {
            UUID id = idMap.get(ref);
            if (id != null) {
                ResourceType resourceType = ReferenceHelper.getResourceType(ref);
                ret.add(resourceType);
            }
        }

        return ret;
    }


    /**
     * finds all patient guids associated with a slot, in order, including when it was blank
     */
    private static Map<String, List<CacheObj>> findJournalIds(List<Exchange> exchanges, VisionCsvHelper csvHelper) throws Exception {

        Map<String, List<CacheObj>> ret = new HashMap<>();

        String[] headers = new String[]{
                "PID",
                "ID",
                "DATE",
                "RECORDED_DATE",
                "CODE",
                "SNOMED_CODE",
                "BNF_CODE",
                "HCP",
                "HCP_TYPE",
                "GMS",
                "EPISODE",
                "TEXT",
                "RUBRIC",
                "DRUG_FORM",
                "DRUG_STRENGTH",
                "DRUG_PACKSIZE",
                "DMD_CODE",
                "IMMS_STATUS",
                "IMMS_COMPOUND",
                "IMMS_SOURCE",
                "IMMS_BATCH",
                "IMMS_REASON",
                "IMMS_METHOD",
                "IMMS_SITE",
                "ENTITY",
                "VALUE1_NAME",
                "VALUE1",
                "VALUE1_UNITS",
                "VALUE2_NAME",
                "VALUE2",
                "VALUE2_UNITS",
                "END_DATE",
                "TIME",
                "CONTEXT",
                "CERTAINTY",
                "SEVERITY",
                "LINKS",
                "LINKS_EXT",
                "SERVICE_ID",
                "ACTION",
                "SUBSET",
                "DOCUMENT_ID"
        };
        CSVFormat csvFormat = VisionCsvToFhirTransformer.CSV_FORMAT.withHeader(headers);

        //list if most-recent-first, so go backwards to to earliest to latest
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String filePath = findFilePathInExchange(exchange, "journal_data_extract");
            if (filePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
            CSVParser parser = new CSVParser(isr, csvFormat);
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String journalId = record.get("ID");
                String patientId = record.get("PID");
                //String action = record.get("ACTION");
                String readCode = record.get("CODE");

                String formattedReadCode = VisionCodeHelper.formatReadCode(CsvCell.factoryDummyWrapper(readCode), csvHelper);
                if (!Strings.isNullOrEmpty(formattedReadCode)) {

                    //look up if this code is a proper Read2 code
                    String term = Read2Cache.lookUpRead2TermForCode(formattedReadCode);
                    if (term == null) {
                        //if no lookup, then it's a local code

                        List<CacheObj> l = ret.get(patientId);
                        if (l == null) {
                            l = new ArrayList<>();
                            ret.put(patientId, l);
                        }

                        CacheObj o = new CacheObj();
                        o.setJournalId(journalId);
                        o.setReadCode(readCode);
                        o.setFormattedReadCode(formattedReadCode);
                        l.add(o);
                    }
                }
            }

            parser.close();

        }

        return ret;
    }

    static class CacheObj {
        private String journalId;
        private String readCode;
        private String formattedReadCode;

        public String getJournalId() {
            return journalId;
        }

        public void setJournalId(String journalId) {
            this.journalId = journalId;
        }

        public String getReadCode() {
            return readCode;
        }

        public void setReadCode(String readCode) {
            this.readCode = readCode;
        }

        public String getFormattedReadCode() {
            return formattedReadCode;
        }

        public void setFormattedReadCode(String formattedReadCode) {
            this.formattedReadCode = formattedReadCode;
        }
    }

}