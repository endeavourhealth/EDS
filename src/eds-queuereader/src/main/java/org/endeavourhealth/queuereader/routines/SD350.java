package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirCodeUri;
import org.endeavourhealth.common.fhir.schema.EthnicCategory;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.publisherCommon.models.EmisClinicalCode;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.PatientBuilder;
import org.endeavourhealth.transform.emis.csv.helpers.EmisMappingHelper;
import org.endeavourhealth.transform.tpp.csv.helpers.TppMappingHelper;
import org.endeavourhealth.transform.vision.helpers.VisionMappingHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SD350 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD350.class);

    private static final String BULK_OPERATION_NAME = "Fixing ethnicity mappings (SD-350)";

    /**
     * fixes data SD-350
     *
     * problem is that we had bad/missing ethnicity mappings for all three GP systems, so need to fix
     * the mapped value on the FHIR patients
     */
    public static void fixEthnicityMappings(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing Ethnicity Mappings at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !(tags.containsKey("EMIS") || tags.containsKey("TPP") || tags.containsKey("Vision"))) {
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
                fixEthnicityMappingsAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }

            }

            LOG.debug("Finished Fixing Ethnicity Mappings at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixEthnicityMappingsAtService(boolean testMode, Service service) throws Exception {

        String messageFormat = null;
        Map<String, String> tags = service.getTags();
        if (tags.containsKey("EMIS")) {
            messageFormat = MessageFormat.EMIS_CSV;

        } else if (tags.containsKey("TPP")) {
            messageFormat = MessageFormat.TPP_CSV;

        } else if (tags.containsKey("Vision")) {
            messageFormat = MessageFormat.VISION_CSV;

        } else {
            throw new Exception("Unknown system type for " + service);
        }

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, messageFormat);
        if (endpoint == null) {
            LOG.warn("No endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();


        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, messageFormat, "Manually created: " + BULK_OPERATION_NAME);
            UUID exchangeId = newExchange.getId();
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            fixEthnicities(serviceId, filer);

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

    private static void fixEthnicities(UUID serviceId, FhirResourceFiler filer) throws Exception {

        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
        LOG.debug("Found " + patientIds.size() + " patient IDs");

        int done = 0;

        for (UUID patientId: patientIds) {

            fixEthnicityForPatient(serviceId, patientId, filer);

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients");
            }
        }
        LOG.debug("Finished " + done + " patients");
    }

    private static void fixEthnicityForPatient(UUID serviceId, UUID patientId, FhirResourceFiler filer) throws Exception {

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.Observation.toString());
        wrappers.addAll(resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.Condition.toString()));

        CodeableConcept latestEthnicity = null;
        Date latestEthnicityDate = null;

        if (filer == null) {
            LOG.trace("Doing patient " + patientId);
        }

        for (ResourceWrapper wrapper: wrappers) {

            Resource resource = wrapper.getResource();
            CodeableConcept codeableConcept = CodeableConceptHelper.findMainCodeableConcept(resource);
            Date effectiveDate = CodeableConceptHelper.findMainEffectiveDate(resource);


            Coding coding = CodeableConceptHelper.findOriginalCoding(codeableConcept);
            if (coding == null) {
                LOG.warn("No original coding found for " + resource.getResourceType() + " " + resource.getId());
                continue;
            }

            if (isEthnicityCode(coding)) {
                if (latestEthnicityDate == null
                        || (effectiveDate != null && effectiveDate.after(latestEthnicityDate))) {

                    latestEthnicity = codeableConcept;
                    latestEthnicityDate = effectiveDate;
                    if (filer == null) {
                        LOG.debug("Patient " + patientId + " found ethnicity in " + resource.getResourceType() + " " + resource.getId() + " with date " + effectiveDate);
                    }
                }
            }
        }

        if (latestEthnicity != null) {
            Coding coding = CodeableConceptHelper.findOriginalCoding(latestEthnicity);
            EthnicCategory newEc = findEthnicCategory(coding);

            Patient patient = (Patient)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientId.toString());
            PatientBuilder patientBuilder = new PatientBuilder(patient);
            EthnicCategory currentEc = patientBuilder.getEthnicity();

            if (((newEc == null) != (currentEc == null))
                || (newEc != null && currentEc != null && newEc != currentEc)) {

                patientBuilder.setEthnicity(newEc);

                if (filer != null) {
                    filer.savePatientResource(null, false, patientBuilder);

                } else {
                    LOG.debug("Patient ID " + patientId + " changed ethnicity from " + currentEc + " -> " + newEc);
                }
            }
        }
    }


    private static EthnicCategory findEthnicCategory(Coding coding) throws Exception {
        String system = coding.getSystem();
        String code = coding.getCode();

        if (system.equals(FhirCodeUri.CODE_SYSTEM_READ2)) {

            //need to check Emis and Vision for this
            //note I've already verified that any code present in both Emis and Vision do have the same mappings
            //so it doesn't matter that we prefer Vision over Emis
            EthnicCategory ec = VisionMappingHelper.findEthnicityCode(code);
            if (ec == null) {
                ec = findEmisEthnicityCode(code);
            }
            return ec;

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_EMIS_CODE)) {
            return findEmisEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_CTV3)) {
            return TppMappingHelper.findEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_TPP_CTV3)) {
            return TppMappingHelper.findEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_VISION_CODE)) {
            return VisionMappingHelper.findEthnicityCode(code);

        } else {
            LOG.error("Unexpected code system " + system);
            return null;
        }
    }

    private static EthnicCategory findEmisEthnicityCode(String code) throws Exception {

        EmisClinicalCode c = new EmisClinicalCode();
        c.setAdjustedCode(code);
        return EmisMappingHelper.findEthnicityCode(c);
    }


    private static boolean isEthnicityCode(Coding coding) throws Exception {

        String system = coding.getSystem();
        String code = coding.getCode();

        if (system.equals(FhirCodeUri.CODE_SYSTEM_READ2)) {

            //need to check Emis and Vision for this
            return isEmisEthnicityCode(code)
                    || VisionMappingHelper.isPotentialEthnicity(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_EMIS_CODE)) {
            return isEmisEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_CTV3)) {
            return TppMappingHelper.isEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_TPP_CTV3)) {
            return TppMappingHelper.isEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_VISION_CODE)) {
            return VisionMappingHelper.isPotentialEthnicity(code);

        } else {
            LOG.error("Unexpected code system " + system);
            return false;
        }
    }

    private static boolean isEmisEthnicityCode(String code) throws Exception {
        try {
            EmisClinicalCode c = new EmisClinicalCode();
            c.setAdjustedCode(code);
            EmisMappingHelper.findEthnicityCode(c);
            return true;

        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Unknown ethnicity code")) {
                return false;
            } else {
                throw ex;
            }
        }

    }


}