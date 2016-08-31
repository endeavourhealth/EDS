package org.endeavourhealth.transform.emis.csv;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.data.transform.EmisCsvCodeMapRepository;
import org.endeavourhealth.core.data.transform.models.EmisCsvCodeMap;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.exceptions.ClinicalCodeNotFoundException;
import org.endeavourhealth.transform.common.exceptions.ResourceDeletedException;
import org.endeavourhealth.transform.common.exceptions.ResourceNotFoundException;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCodeType;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EmisCsvHelper {

    private static final String CODEABLE_CONCEPT = "CodeableConcept";
    private static final String ID_DELIMITER = "//";

    //metadata, not relating to patients
    private Map<Long, CodeableConcept> clinicalCodes = new ConcurrentHashMap<>();
    private Map<Long, ClinicalCodeType> clinicalCodeTypes = new ConcurrentHashMap<>();
    private Map<Long, CodeableConcept> medication = new ConcurrentHashMap<>();
    private EmisCsvCodeMapRepository mappingRepository = new EmisCsvCodeMapRepository();
    private ResourceRepository resourceRepository = new ResourceRepository();

    //some resources are referred to by others, so we cache them here for when we need them
    private Map<String, Condition> problemMap = new HashMap<>();
    private Map<String, ReferralRequest> referralMap = new HashMap<>();
    private Map<String, List<ResourceRelationship>> observationChildMap = new HashMap<>();
    private Map<String, List<ResourceRelationship>> problemChildMap = new HashMap<>();
    private Map<String, DateTimeType> issueRecordDateMap = new HashMap<>();


    public EmisCsvHelper() {
    }

    /**
     * to ensure globally unique IDs for all resources, a new ID is created
     * from the patientGuid and sourceGuid (e.g. observationGuid)
     */
    private static String createUniqueId(String patientGuid, String sourceGuid) {
        if (sourceGuid == null) {
            return patientGuid;
        } else {
            return patientGuid + ID_DELIMITER + sourceGuid;
        }
    }
    private static String getPatientGuidFromUniqueId(String uniqueId) {
        String[] toks = uniqueId.split(ID_DELIMITER);
        if (toks.length == 1
                || toks.length == 2) {
            return toks[0];
        } else {
            throw new IllegalArgumentException("Invalid unique ID string [" + uniqueId + "] - expect one or two tokens delimited with " + ID_DELIMITER);
        }
    }

    public static void setUniqueId(Resource resource, String patientGuid, String sourceGuid) {
        resource.setId(createUniqueId(patientGuid, sourceGuid));
    }



    public void addMedication(Long codeId,
                              CodeableConcept codeableConcept,
                              Long snomedConceptId,
                              String snomedTerm,
                              CsvProcessor csvProcessor) throws Exception {
        medication.put(codeId, codeableConcept);

        //store the medication in the DB
        String json = new JsonParser().composeString(codeableConcept, CODEABLE_CONCEPT);

        EmisCsvCodeMap mapping = new EmisCsvCodeMap();
        mapping.setServiceId(csvProcessor.getServiceId());
        mapping.setSystemId(csvProcessor.getSystemId());
        mapping.setMedication(true);
        mapping.setCodeId(codeId);
        mapping.setTimeUuid(UUIDs.timeBased());
        mapping.setCodeType(null);
        mapping.setCodeableConcept(json);
        mapping.setSnomedConceptId(snomedConceptId);
        mapping.setSnomedTerm(snomedTerm);

        mappingRepository.save(mapping);
    }
    public void addClinicalCode(Long codeId,
                                CodeableConcept codeableConcept,
                                ClinicalCodeType type,
                                String readTerm,
                                String readCode,
                                Long snomedConceptId,
                                Long snomedDescriptionId,
                                String snomedTerm,
                                String nationalCode,
                                String nationalCodeCategory,
                                String nationalCodeDescription,
                                CsvProcessor csvProcessor) throws Exception {
        clinicalCodes.put(codeId, codeableConcept);
        clinicalCodeTypes.put(codeId, type);

        //store the code in the DB
        String json = new JsonParser().composeString(codeableConcept, CODEABLE_CONCEPT);

        EmisCsvCodeMap mapping = new EmisCsvCodeMap();
        mapping.setServiceId(csvProcessor.getServiceId());
        mapping.setSystemId(csvProcessor.getSystemId());
        mapping.setMedication(false);
        mapping.setCodeId(codeId);
        mapping.setTimeUuid(UUIDs.timeBased());
        mapping.setCodeType(type.getValue());
        mapping.setCodeableConcept(json);
        mapping.setReadTerm(readTerm);
        mapping.setReadCode(readCode);
        mapping.setSnomedConceptId(snomedConceptId);
        mapping.setSnomedDescriptionId(snomedDescriptionId);
        mapping.setSnomedTerm(snomedTerm);
        mapping.setNationalCode(nationalCode);
        mapping.setNationalCodeCategory(nationalCodeCategory);
        mapping.setNationalCodeDescription(nationalCodeDescription);

        mappingRepository.save(mapping);
    }

    public CodeableConcept findClinicalCode(Long codeId, CsvProcessor csvProcessor) throws Exception {
        CodeableConcept ret = clinicalCodes.get(codeId);
        if (ret == null) {
            retrieveClincalCode(codeId, csvProcessor);
            ret = clinicalCodes.get(codeId);
        }
        return ret.copy();
    }

    private void retrieveClincalCode(Long codeId, CsvProcessor csvProcessor) throws Exception {
        EmisCsvCodeMap mapping = mappingRepository.getMostRecent(csvProcessor.getServiceId(), csvProcessor.getSystemId(), false, codeId);
        if (mapping == null) {
            throw new ClinicalCodeNotFoundException(codeId, false);
        }

        String json = mapping.getCodeableConcept();

        CodeableConcept codeableConcept = (CodeableConcept)new JsonParser().parseType(json, CODEABLE_CONCEPT);
        clinicalCodes.put(codeId, codeableConcept);

        ClinicalCodeType type = ClinicalCodeType.fromValue(mapping.getCodeType());
        clinicalCodeTypes.put(codeId, type);
    }

    public ClinicalCodeType findClinicalCodeType(Long codeId, CsvProcessor csvProcessor) throws Exception {
        ClinicalCodeType ret = clinicalCodeTypes.get(codeId);
        if (ret == null) {
            retrieveClincalCode(codeId, csvProcessor);
            ret = clinicalCodeTypes.get(codeId);
        }
        return ret;
    }

    public CodeableConcept findMedication(Long codeId, CsvProcessor csvProcessor) throws Exception {
        CodeableConcept ret = medication.get(codeId);
        if (ret == null) {
            retrieveMedication(codeId, csvProcessor);
            ret = medication.get(codeId);
        }
        return ret.copy();
    }

    private void retrieveMedication(Long codeId, CsvProcessor csvProcessor) throws Exception {
        EmisCsvCodeMap mapping = mappingRepository.getMostRecent(csvProcessor.getServiceId(), csvProcessor.getSystemId(), true, codeId);
        if (mapping == null) {
            throw new ClinicalCodeNotFoundException(codeId, true);
        }

        String json = mapping.getCodeableConcept();
        CodeableConcept codeableConcept = (CodeableConcept)new JsonParser().parseType(json, CODEABLE_CONCEPT);
        medication.put(codeId, codeableConcept);
    }

    /**
     * admin-type resources just use the EMIS CSV GUID as their reference
     */
    public Reference createLocationReference(String locationGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Location, locationGuid);
    }
    public Reference createOrganisationReference(String organizationGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Organization, organizationGuid);
    }
    public Reference createPractitionerReference(String practitionerGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Practitioner, practitionerGuid);
    }
    public Reference createScheduleReference(String scheduleGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Schedule, scheduleGuid);
    }
    public Reference createSlotReference(String slotGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Slot, slotGuid);
    }

    /**
     * patient-type resources must include the patient GUID are part of the unique ID in the reference
     * because the EMIS GUIDs for things like Obs are only unique within that patient record itself
     */
    public Reference createPatientReference(String patientGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Patient, createUniqueId(patientGuid, null));
    }
    public Reference createAppointmentReference(String appointmentGuid, String patientGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Appointment, createUniqueId(patientGuid, appointmentGuid));
    }
    public Reference createEncounterReference(String encounterGuid, String patientGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Encounter, createUniqueId(patientGuid, encounterGuid));
    }
    public Reference createObservationReference(String observationGuid, String patientGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Observation, createUniqueId(patientGuid, observationGuid));
    }
    public Reference createMedicationStatementReference(String medicationStatementGuid, String patientGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.MedicationStatement, createUniqueId(patientGuid, medicationStatementGuid));
    }
    public Reference createProblemReference(String problemGuid, String patientGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Condition, createUniqueId(patientGuid, problemGuid));
    }



    public void cacheReferral(String observationGuid, String patientGuid, ReferralRequest fhirReferral) {
        referralMap.put(createUniqueId(patientGuid, observationGuid), fhirReferral);
    }

    public ReferralRequest findReferral(String observationGuid, String patientGuid) {
        return referralMap.get(createUniqueId(patientGuid, observationGuid));
    }

    public void cacheProblem(String observationGuid, String patientGuid, Condition fhirCondition) {
        problemMap.put(createUniqueId(patientGuid, observationGuid), fhirCondition);
    }

    public Condition findProblem(String observationGuid, String patientGuid) {
        return problemMap.get(createUniqueId(patientGuid, observationGuid));
    }

    public List<ResourceRelationship> getAndRemoveObservationParentRelationships(String parentObservationGuid, String patientGuid) {
        return observationChildMap.remove(createUniqueId(patientGuid, parentObservationGuid));
    }

    public void cacheObservationParentRelationship(String parentObservationGuid, String patientGuid, String observationGuid) {

        List<ResourceRelationship> list = observationChildMap.get(createUniqueId(patientGuid, parentObservationGuid));
        if (list == null) {
            list = new ArrayList<>();
            observationChildMap.put(createUniqueId(patientGuid, parentObservationGuid), list);
        }
        list.add(new ResourceRelationship(patientGuid, observationGuid, ResourceType.Observation));
    }

    /**
     * as the end of processing all CSV files, there may be some new observations that link
     * to past parent observations. These linkages are saved against the parent observation,
     * so we need to retrieve them off the main repository, amend them and save them
     */
    public void processRemainingObservationParentChildLinks(CsvProcessor csvProcessor) throws Exception {

        Iterator<String> it = observationChildMap.keySet().iterator();
        while (it.hasNext()) {
            String locallyUniqueId = it.next();
            List<ResourceRelationship> childObservationIds = observationChildMap.get(locallyUniqueId);

            updateExistingObservationWithNewChildLinks(locallyUniqueId, childObservationIds, csvProcessor);
        }
    }

    private Resource retrieveResource(String locallyUniqueId, ResourceType resourceType, CsvProcessor csvProcessor) throws Exception {

        UUID globallyUniqueId = IdHelper.getOrCreateEdsResourceId(csvProcessor.getServiceId(),
                                                        csvProcessor.getSystemId(),
                                                        resourceType,
                                                        locallyUniqueId);

        ResourceHistory resourceHistory = resourceRepository.getCurrentVersion(resourceType.toString(), globallyUniqueId);
        if (resourceHistory == null) {
            throw new ResourceNotFoundException(resourceType, globallyUniqueId);
        }

        if (resourceHistory.getIsDeleted()) {
            throw new ResourceDeletedException(resourceType, globallyUniqueId);
        }

        String json = resourceHistory.getResourceData();
        return new JsonParser().parse(json);
    }

    private void updateExistingObservationWithNewChildLinks(String locallyUniqueId,
                                                            List<ResourceRelationship> childResourceRelationships,
                                                            CsvProcessor csvProcessor) throws Exception {

        Observation fhirObservation = null;
        try {
            fhirObservation = (Observation) retrieveResource(locallyUniqueId, ResourceType.Observation, csvProcessor);
        } catch (ResourceNotFoundException e) {
            //if the resource can't be found, it's because that EMIS observation record was saved as something other
            //than a FHIR Observation (example in the CSV test files is an Allergy that is linked to another Allergy)
            return;
        }

        boolean changed = false;
        String patientGuid = null;

        for (ResourceRelationship childResourceRelationship : childResourceRelationships) {

            //all the relationships have the same patientGuid, so it's safe to just keep reassigning this
            patientGuid = childResourceRelationship.getPatientGuid();

            //the Observation resource is from the DB so has already had all its Ids mapped,
            //so we need to convert the local ID to the globally unique ID we'll have used before
            String locallyUniqueObservationId = createUniqueId(childResourceRelationship.getPatientGuid(), childResourceRelationship.getDependentResourceGuid());

            String globallyUniqueObservationId = IdHelper.getOrCreateEdsResourceIdString(csvProcessor.getServiceId(),
                                                                            csvProcessor.getSystemId(),
                                                                            childResourceRelationship.getDependentResourceType(),
                                                                            locallyUniqueObservationId);

            Reference globallyUniqueReference = ReferenceHelper.createReference(childResourceRelationship.getDependentResourceType(),
                                                                                globallyUniqueObservationId);

            //check if the parent observation doesn't already have our ob linked to it
            boolean alreadyLinked = false;
            for (Observation.ObservationRelatedComponent related: fhirObservation.getRelated()) {
                if (related.getType() == Observation.ObservationRelationshipType.HASMEMBER
                        && related.getTarget().equalsShallow(globallyUniqueReference)) {
                    alreadyLinked = true;
                    break;
                }
            }

            if (!alreadyLinked) {
                Observation.ObservationRelatedComponent fhirRelation = fhirObservation.addRelated();
                fhirRelation.setType(Observation.ObservationRelationshipType.HASMEMBER);
                fhirRelation.setTarget(globallyUniqueReference);

                changed = true;
            }
        }

        if (changed) {
            //make sure to pass in the parameter to bypass ID mapping, since this resource has already been done
            csvProcessor.savePatientResource(false, patientGuid, fhirObservation);
        }
    }

    public void cacheProblemRelationship(String problemObservationGuid,
                                         String patientGuid,
                                         String resourceGuid,
                                         ResourceType resourceType) {

        if (Strings.isNullOrEmpty(problemObservationGuid)) {
            return;
        }

        List<ResourceRelationship> list = problemChildMap.get(createUniqueId(patientGuid, problemObservationGuid));
        if (list == null) {
            list = new ArrayList<>();
            problemChildMap.put(createUniqueId(patientGuid, problemObservationGuid), list);
        }
        list.add(new ResourceRelationship(patientGuid, resourceGuid, resourceType));
    }

    public void processRemainingProblemRelationships(CsvProcessor csvProcessor) throws Exception {
        Iterator<String> it = problemChildMap.keySet().iterator();
        while (it.hasNext()) {
            String problemLocallyUniqueId = it.next();
            List<ResourceRelationship> childResourceRelationships = problemChildMap.get(problemLocallyUniqueId);

            //the problem may be one just created, or one created previously
            Condition fhirProblem = problemMap.get(problemLocallyUniqueId);
            if (fhirProblem == null) {

                try {
                    Condition fhirCondition = (Condition) retrieveResource(problemLocallyUniqueId, ResourceType.Condition, csvProcessor);
                    addRelationshipsToExistingProblem(fhirCondition, childResourceRelationships, csvProcessor);
                } catch (ResourceNotFoundException|ResourceDeletedException ex) {
                    //we have test data with medication items linking to non-existant and deleted problems, so don't fail if we get this
                }
            } else {
                addRelationshipsToNewProblem(fhirProblem, childResourceRelationships);
            }
        }

        //make sure to save all problems now we've finished with them
        Iterator<String> conditionIterator = problemMap.keySet().iterator();
        while (conditionIterator.hasNext()) {
            String locallyUniqueId = conditionIterator.next();
            Condition fhirProblem = problemMap.get(locallyUniqueId);
            String patientGuid = getPatientGuidFromUniqueId(locallyUniqueId);

            csvProcessor.savePatientResource(patientGuid, fhirProblem);
        }
    }

    private void addRelationshipsToNewProblem(Condition fhirProblem, List<ResourceRelationship> resourceRelationships) throws Exception {

        for (ResourceRelationship resourceRelationship : resourceRelationships) {

            String uniqueId = createUniqueId(resourceRelationship.getPatientGuid(), resourceRelationship.getDependentResourceGuid());
            Reference reference = ReferenceHelper.createReference(resourceRelationship.getDependentResourceType(), uniqueId);
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE, reference));
        }
    }

    private void addRelationshipsToExistingProblem(Condition fhirCondition,
                                                 List<ResourceRelationship> childResourceRelationships,
                                                 CsvProcessor csvProcessor) throws Exception {

        boolean changed = false;
        String patientGuid = null;

        for (ResourceRelationship childResourceRelationship : childResourceRelationships) {

            //all the relationships have the same patientGuid, so it's safe to just keep reassigning this
            patientGuid = childResourceRelationship.getPatientGuid();

            String locallyUniqueId = createUniqueId(childResourceRelationship.getPatientGuid(), childResourceRelationship.getDependentResourceGuid());

            String globallyUniqueId = IdHelper.getOrCreateEdsResourceIdString(csvProcessor.getServiceId(),
                    csvProcessor.getSystemId(),
                    ResourceType.Observation,
                    locallyUniqueId);

            Reference globallyUniqueReference = ReferenceHelper.createReference(ResourceType.Observation, globallyUniqueId);

            //check to see if this resource is already linked to the problem
            boolean alreadyLinked = false;
            for (Extension extension: fhirCondition.getExtension()) {
                if (extension.getUrl().equals(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE)
                        && extension.getValue().equalsShallow(globallyUniqueReference)) {
                    alreadyLinked = true;
                    break;
                }
            }

            if (!alreadyLinked) {
                fhirCondition.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE, globallyUniqueReference));
                changed = true;
            }
        }

        if (changed) {
            //make sure to pass in the parameter to bypass ID mapping, since this resource has already been done
            csvProcessor.savePatientResource(false, patientGuid, fhirCondition);
        }
    }

    public void cacheDrugRecordDate(String drugRecordGuid, String patientGuid, DateTimeType dateTime) {
        String uniqueId = createUniqueId(patientGuid, drugRecordGuid);
        DateTimeType previous = issueRecordDateMap.get(uniqueId);
        if (previous == null
                || dateTime.after(previous)) {
            issueRecordDateMap.put(uniqueId, dateTime);
        }
    }
    public DateTimeType getDrugRecordDate(String drugRecordId, String patientGuid) {
        return issueRecordDateMap.get(createUniqueId(patientGuid, drugRecordId));
    }

    /**
     * object to temporarily store relationships between resources, such as things linked to a problem
     * or observations linked to a parent observation
     */
    public class ResourceRelationship {
        private String patientGuid = null;
        private String dependentResourceGuid = null;
        private ResourceType dependentResourceType = null;

        public ResourceRelationship(String patientGuid, String dependentResourceGuid, ResourceType dependantResourceType) {
            this.patientGuid = patientGuid;
            this.dependentResourceGuid = dependentResourceGuid;
            this.dependentResourceType = dependantResourceType;
        }

        public String getPatientGuid() {
            return patientGuid;
        }

        public String getDependentResourceGuid() {
            return dependentResourceGuid;
        }

        public ResourceType getDependentResourceType() {
            return dependentResourceType;
        }
    }

}
