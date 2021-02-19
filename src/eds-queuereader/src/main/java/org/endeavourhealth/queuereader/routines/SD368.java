package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.schema.MaritalStatus;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.PatientBuilder;
import org.endeavourhealth.transform.tpp.csv.helpers.TppMappingHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SD368 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD368.class);

    private static final String BULK_OPERATION_NAME = "Fixing marital status mappings (SD-SD368)";

    /**
     * fixes data SD-368
     *
     * problem is that we had bad/missing marital status mappings for TPP, so need to fix
     * the mapped value on the FHIR patients
     */
    public static void fixMaritalStatusMappings(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing MaritalStatus Mappings at " + odsCodeRegex + " test mode = " + testMode);
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
                fixMaritalStatusMappingsAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }

            }

            LOG.debug("Finished Fixing MaritalStatus Mappings at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixMaritalStatusMappingsAtService(boolean testMode, Service service) throws Exception {

        String messageFormat = MessageFormat.TPP_CSV;

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
            fixMaritalStatuses(serviceId, filer, messageFormat);

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

    private static void fixMaritalStatuses(UUID serviceId, FhirResourceFiler filer, String messageFormat) throws Exception {

        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
        LOG.debug("Found " + patientIds.size() + " patient IDs");

        int done = 0;

        for (UUID patientId: patientIds) {

            fixMaritalStatusForPatient(serviceId, patientId, filer, messageFormat);

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients");
            }
        }
        LOG.debug("Finished " + done + " patients");
    }

    private static void fixMaritalStatusForPatient(UUID serviceId, UUID patientId, FhirResourceFiler filer, String messageFormat) throws Exception {

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.Observation.toString());
        wrappers.addAll(resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.Condition.toString()));

        CodeableConcept latestMaritalStatus = null;
        Date latestMaritalStatusDate = null;

        if (filer == null) {
            LOG.trace("Doing patient " + patientId);
        }

        for (ResourceWrapper wrapper: wrappers) {

            Resource resource = wrapper.getResource();
            CodeableConcept codeableConcept = CodeableConceptHelper.findMainCodeableConcept(resource);
            Date effectiveDate = CodeableConceptHelper.findMainEffectiveDate(resource);
            if (codeableConcept == null) {
                LOG.warn("No codeable concept found for " + resource.getResourceType() + " " + resource.getId());
                continue;
            }

            Coding coding = CodeableConceptHelper.findOriginalCoding(codeableConcept);
            if (coding == null) {
                LOG.warn("No original coding found for " + resource.getResourceType() + " " + resource.getId());
                continue;
            }

            String code = coding.getCode();
            if (TppMappingHelper.isMaritalStatusCode(code)) {

                if (latestMaritalStatusDate == null
                        || (effectiveDate != null && effectiveDate.after(latestMaritalStatusDate))) {

                    latestMaritalStatus = codeableConcept;
                    latestMaritalStatusDate = effectiveDate;
                    if (filer == null) {
                        LOG.debug("Patient " + patientId + " found maritalStatus in " + resource.getResourceType() + " " + resource.getId() + " with date " + effectiveDate);
                    }

                }
            }
        }

        if (latestMaritalStatus != null) {
            Coding coding = CodeableConceptHelper.findOriginalCoding(latestMaritalStatus);
            String code = coding.getCode();
            MaritalStatus newMs = TppMappingHelper.findMaritalStatusCode(code);

            Patient patient = (Patient)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientId.toString());
            PatientBuilder patientBuilder = new PatientBuilder(patient);
            MaritalStatus currentMs = patientBuilder.getMaritalStatus();

            if (((newMs == null) != (currentMs == null))
                    || (newMs != null && currentMs != null && newMs != currentMs)) {

                patientBuilder.setMaritalStatus(newMs);

                if (filer != null) {
                    filer.savePatientResource(null, false, patientBuilder);
                }

                LOG.debug("Patient ID " + patientId + " changed maritalStatus from " + currentMs + " -> " + newMs);
            }
        }
    }



}