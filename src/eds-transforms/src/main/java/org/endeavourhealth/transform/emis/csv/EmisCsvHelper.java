package org.endeavourhealth.transform.emis.csv;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.data.transform.EmisCsvCodeMapRepository;
import org.endeavourhealth.core.data.transform.models.EmisCsvCodeMap;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.exceptions.ClinicalCodeNotFoundException;
import org.endeavourhealth.transform.common.exceptions.ResourceDeletedException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCodeType;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.ReferenceComponents;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EmisCsvHelper {

    private static final String CODEABLE_CONCEPT = "CodeableConcept";
    private static final String ID_DELIMITER = ":";

    //metadata, not relating to patients
    private Map<Long, CodeableConcept> clinicalCodes = new ConcurrentHashMap<>();
    private Map<Long, ClinicalCodeType> clinicalCodeTypes = new ConcurrentHashMap<>();
    private Map<Long, CodeableConcept> medication = new ConcurrentHashMap<>();
    private EmisCsvCodeMapRepository mappingRepository = new EmisCsvCodeMapRepository();
    private ResourceRepository resourceRepository = new ResourceRepository();

    //some resources are referred to by others, so we cache them here for when we need them
    private Map<String, Condition> problemMap = new HashMap<>();
    private Map<String, ReferralRequest> referralMap = new HashMap<>();
    private Map<String, List<Reference>> observationChildMap = new HashMap<>();
    private Map<String, List<Reference>> problemChildMap = new HashMap<>();
    private Map<String, DateTimeType> issueRecordDateMap = new HashMap<>();
    private Map<String, List<Observation.ObservationComponentComponent>> bpComponentMap = new HashMap<>();
    private Map<String, SessionPractitioners> sessionPractitionerMap = new HashMap<>();
    private Map<String, List<String>> organisationLocationMap = new HashMap<>();

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
            retrieveClinicalCode(codeId, csvProcessor);
            ret = clinicalCodes.get(codeId);
        }
        return ret.copy();
    }

    private void retrieveClinicalCode(Long codeId, CsvProcessor csvProcessor) throws Exception {
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
            retrieveClinicalCode(codeId, csvProcessor);
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

    /**
     * patient-type resources must include the patient GUID are part of the unique ID in the reference
     * because the EMIS GUIDs for things like Obs are only unique within that patient record itself
     */
    public Reference createPatientReference(String patientGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Patient, createUniqueId(patientGuid, null));
    }
    public Reference createAppointmentReference(String appointmentGuid, String patientGuid) throws Exception {
        if (Strings.isNullOrEmpty(appointmentGuid)) {
            throw new IllegalArgumentException("Missing appointmentGuid");
        }
        return ReferenceHelper.createReference(ResourceType.Appointment, createUniqueId(patientGuid, appointmentGuid));
    }
    public Reference createEncounterReference(String encounterGuid, String patientGuid) throws Exception {
        if (Strings.isNullOrEmpty(encounterGuid)) {
            throw new IllegalArgumentException("Missing encounterGuid");
        }
        return ReferenceHelper.createReference(ResourceType.Encounter, createUniqueId(patientGuid, encounterGuid));
    }
    public Reference createObservationReference(String observationGuid, String patientGuid) throws Exception {
        if (Strings.isNullOrEmpty(observationGuid)) {
            throw new IllegalArgumentException("Missing observationGuid");
        }
        return ReferenceHelper.createReference(ResourceType.Observation, createUniqueId(patientGuid, observationGuid));
    }
    public Reference createMedicationStatementReference(String medicationStatementGuid, String patientGuid) throws Exception {
        if (Strings.isNullOrEmpty(medicationStatementGuid)) {
            throw new IllegalArgumentException("Missing medicationStatementGuid");
        }
        return ReferenceHelper.createReference(ResourceType.MedicationStatement, createUniqueId(patientGuid, medicationStatementGuid));
    }
    public Reference createProblemReference(String problemGuid, String patientGuid) throws Exception {
        if (Strings.isNullOrEmpty(problemGuid)) {
            throw new IllegalArgumentException("Missing problemGuid");
        }
        return ReferenceHelper.createReference(ResourceType.Condition, createUniqueId(patientGuid, problemGuid));
    }



    public void cacheReferral(String observationGuid, String patientGuid, ReferralRequest fhirReferral) {
        referralMap.put(createUniqueId(patientGuid, observationGuid), fhirReferral);
    }

    public ReferralRequest findReferral(String observationGuid, String patientGuid) {
        return referralMap.remove(createUniqueId(patientGuid, observationGuid));
    }

    public void cacheProblem(String observationGuid, String patientGuid, Condition fhirCondition) {
        problemMap.put(createUniqueId(patientGuid, observationGuid), fhirCondition);
    }

    public Condition findProblem(String observationGuid, String patientGuid) {
        return problemMap.remove(createUniqueId(patientGuid, observationGuid));
    }

    public List<Reference> getAndRemoveObservationParentRelationships(String parentObservationGuid, String patientGuid) {
        return observationChildMap.remove(createUniqueId(patientGuid, parentObservationGuid));
    }

    public boolean hasChildObservations(String parentObservationGuid, String patientGuid) {
        return observationChildMap.containsKey(createUniqueId(patientGuid, parentObservationGuid));
    }

    public void cacheObservationParentRelationship(String parentObservationGuid, String patientGuid, String observationGuid) {

        List<Reference> list = observationChildMap.get(createUniqueId(patientGuid, parentObservationGuid));
        if (list == null) {
            list = new ArrayList<>();
            observationChildMap.put(createUniqueId(patientGuid, parentObservationGuid), list);
        }
        list.add(ReferenceHelper.createReference(ResourceType.Observation, createUniqueId(patientGuid, observationGuid)));
    }


    public Resource retrieveResource(String locallyUniqueId, ResourceType resourceType, CsvProcessor csvProcessor) throws Exception {

        UUID globallyUniqueId = IdHelper.getEdsResourceId(csvProcessor.getServiceId(),
                csvProcessor.getSystemId(),
                resourceType,
                locallyUniqueId);
        if (globallyUniqueId == null) {
            throw new TransformException("No global ID exists for " + resourceType + " with local ID " + locallyUniqueId);
        }

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

    /**
     * as the end of processing all CSV files, there may be some new observations that link
     * to past parent observations. These linkages are saved against the parent observation,
     * so we need to retrieve them off the main repository, amend them and save them
     */
    public void processRemainingObservationParentChildLinks(CsvProcessor csvProcessor) throws Exception {

        for (String locallyUniqueId : observationChildMap.keySet()) {
            List<Reference> childObservationIds = observationChildMap.get(locallyUniqueId);

            updateExistingObservationWithNewChildLinks(locallyUniqueId, childObservationIds, csvProcessor);
        }
    }


    private void updateExistingObservationWithNewChildLinks(String locallyUniqueObservationId,
                                                            List<Reference> childResourceRelationships,
                                                            CsvProcessor csvProcessor) throws Exception {

        Observation fhirObservation;
        try {
            fhirObservation = (Observation) retrieveResource(locallyUniqueObservationId, ResourceType.Observation, csvProcessor);
        } catch (ResourceNotFoundException e) {
            //if the resource can't be found, it's because that EMIS observation record was saved as something other
            //than a FHIR Observation (example in the CSV test files is an Allergy that is linked to another Allergy)
            return;
        }

        //the EMIS patient GUID is part of the locallyUnique Id of the observation, to extract from that
        String patientGuid = getPatientGuidFromUniqueId(locallyUniqueObservationId);

        boolean changed = false;

        for (Reference reference : childResourceRelationships) {

            ReferenceComponents components = ReferenceHelper.getReferenceComponents(reference);
            String locallyUniqueId = components.getId();
            ResourceType resourceType = components.getResourceType();

            //the Observation resource is from the DB so has already had all its Ids mapped,
            //so we need to convert the local ID of the child observation to the globally unique ID we'll have generated
            String globallyUniqueObservationId = IdHelper.getOrCreateEdsResourceIdString(csvProcessor.getServiceId(),
                                                                            csvProcessor.getSystemId(),
                                                                            resourceType,
                                                                            locallyUniqueId);

            Reference globallyUniqueReference = ReferenceHelper.createReference(resourceType, globallyUniqueObservationId);

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
            csvProcessor.savePatientResource(null, false, patientGuid, fhirObservation);
        }
    }

    public List<Reference> getAndRemoveProblemRelationships(String problemGuid, String patientGuid) {
        return problemChildMap.remove(createUniqueId(patientGuid, problemGuid));
    }

    public void cacheProblemRelationship(String problemObservationGuid,
                                         String patientGuid,
                                         String resourceGuid,
                                         ResourceType resourceType) {

        if (Strings.isNullOrEmpty(problemObservationGuid)) {
            return;
        }

        List<Reference> list = problemChildMap.get(createUniqueId(patientGuid, problemObservationGuid));
        if (list == null) {
            list = new ArrayList<>();
            problemChildMap.put(createUniqueId(patientGuid, problemObservationGuid), list);
        }
        list.add(ReferenceHelper.createReference(resourceType, createUniqueId(patientGuid, resourceGuid)));
    }

    /**
     * called at the end of the transform, to update pre-existing Problem resources with references to new
     * clinical resources that are in those problems
     */
    public void processRemainingProblemRelationships(CsvProcessor csvProcessor) throws Exception {

        for (String problemLocallyUniqueId : problemChildMap.keySet()) {
            List<Reference> childResourceRelationships = problemChildMap.get(problemLocallyUniqueId);

            addRelationshipsToExistingProblem(problemLocallyUniqueId, childResourceRelationships, csvProcessor);
        }
    }

    private void addRelationshipsToExistingProblem(String problemLocallyUniqueId,
                                                   List<Reference> childResourceRelationships,
                                                   CsvProcessor csvProcessor) throws Exception {

        Condition fhirProblem;
        try {
            fhirProblem = (Condition) retrieveResource(problemLocallyUniqueId, ResourceType.Condition, csvProcessor);
        } catch (ResourceNotFoundException|ResourceDeletedException ex) {
            //we have test data with medication items linking to non-existant and deleted problems, so don't fail if we get this
            return;
        }

        String patientGuid = getPatientGuidFromUniqueId(problemLocallyUniqueId);

        List_ list = new List_();

        //find the existing list of child resource references
        if (fhirProblem.hasExtension()) {
            for (Extension extension: fhirProblem.getExtension()) {
                if (extension.getUrl().equals(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE)) {
                    Reference reference = (Reference)extension.getValue();
                    list = (List_)reference.getResource();
                }
            }
        }

        //if the extension wasn't there before, create and add it
        if (list == null) {
            list = new List_();
            Reference listReference = ReferenceHelper.createReferenceInline(list);
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE, listReference));
        }

        boolean changed = false;

        for (Reference reference : childResourceRelationships) {

            ReferenceComponents components = ReferenceHelper.getReferenceComponents(reference);
            String locallyUniqueId = components.getId();
            ResourceType resourceType = components.getResourceType();

            String globallyUniqueId = IdHelper.getOrCreateEdsResourceIdString(csvProcessor.getServiceId(),
                    csvProcessor.getSystemId(),
                    resourceType,
                    locallyUniqueId);

            Reference globallyUniqueReference = ReferenceHelper.createReference(resourceType, globallyUniqueId);

            //check to see if this resource is already linked to the problem
            boolean alreadyLinked = false;
            for (List_.ListEntryComponent entry: list.getEntry()) {
                Reference entryReference = entry.getItem();
                if (entryReference.getReference().equals(globallyUniqueReference.getReference())) {
                    alreadyLinked = true;
                    break;
                }
            }

            if (!alreadyLinked) {
                list.addEntry().setItem(globallyUniqueReference);
                changed = true;
            }
        }

        if (changed) {
            //make sure to pass in the parameter to bypass ID mapping, since this resource has already been done
            csvProcessor.savePatientResource(null, false, patientGuid, fhirProblem);
        }
    }

    /*private void addRelationshipsToNewProblem(Condition fhirProblem, List<ResourceRelationship> resourceRelationships) throws Exception {

        for (ResourceRelationship resourceRelationship : resourceRelationships) {

            String uniqueId = createUniqueId(resourceRelationship.getPatientGuid(), resourceRelationship.getDependentResourceGuid());
            Reference reference = ReferenceHelper.findAndCreateReference(resourceRelationship.getDependentResourceType(), uniqueId);
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

            Reference globallyUniqueReference = ReferenceHelper.findAndCreateReference(ResourceType.Observation, globallyUniqueId);

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
    }*/

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

    public void cacheBpComponent(String parentObservationGuid, String patientGuid, Observation.ObservationComponentComponent component) {
        String key = createUniqueId(patientGuid, parentObservationGuid);
        List<Observation.ObservationComponentComponent> list = bpComponentMap.get(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(component);
    }

    public List<Observation.ObservationComponentComponent> findBpComponents(String observationGuid, String patientGuid) {
        String key = createUniqueId(patientGuid, observationGuid);
        return bpComponentMap.remove(key);
    }

    public void cacheSessionPractitionerMap(String sessionGuid, String emisUserGuid, boolean isDeleted) {

        SessionPractitioners obj = sessionPractitionerMap.get(sessionGuid);
        if (obj == null) {
            obj = new SessionPractitioners();
            sessionPractitionerMap.put(sessionGuid, obj);
        }

        if (isDeleted) {
            obj.getEmisUserGuidsToDelete().add(emisUserGuid);
        } else {
            obj.getEmisUserGuidsToSave().add(emisUserGuid);
        }
    }

    public List<String> findSessionPractionersToSave(String sessionGuid) {
        SessionPractitioners obj = sessionPractitionerMap.remove(sessionGuid);
        if (obj == null) {
            return new ArrayList<>();
        } else {
            obj.setProcessedSession(true);
            return obj.getEmisUserGuidsToSave();
        }
    }

    /**
     * called at the end of the transform. If the sessionPractitionerMap contains any entries that haven't been processed
     * then we have changes to the staff in a previously saved FHIR Schedule, so we need to amend that Schedule
     */
    public void processRemainingSessionPractitioners(CsvProcessor csvProcessor) throws Exception {

        for (String sessionGuid : sessionPractitionerMap.keySet()) {
            SessionPractitioners practitioners = sessionPractitionerMap.get(sessionGuid);
            if (!practitioners.isProcessedSession()) {
                updateExistingScheduleWithNewPractitioners(sessionGuid, practitioners, csvProcessor);
            }
        }
    }

    private void updateExistingScheduleWithNewPractitioners(String sessionGuid, SessionPractitioners practitioners, CsvProcessor csvProcessor) throws Exception {

        Schedule fhirSchedule = null;
        try {
            fhirSchedule = (Schedule)retrieveResource(sessionGuid, ResourceType.Schedule, csvProcessor);
        } catch (TransformException ex) {
            //because the SessionUser file doesn't have an OrganisationGuid column, we can't split that file
            //so we will be trying to update the practitioners on sessions that don't exist. So if we get
            //an exception here, just return out
            return;
        }

        //get the references from the existing schedule, removing them as we go
        List<Reference> references = new ArrayList<>();

        if (fhirSchedule.hasActor()) {
            references.add(fhirSchedule.getActor());
            fhirSchedule.setActor(null);
        }
        if (fhirSchedule.hasExtension()) {
            List<Extension> extensions = fhirSchedule.getExtension();
            for (int i=extensions.size()-1; i>=0; i--) {
                Extension extension = extensions.get(i);
                if (extension.getUrl().equals(FhirExtensionUri.SCHEDULE_ADDITIONAL_ACTOR)) {
                    references.add((Reference)extension.getValue());
                    extensions.remove(i);
                }
            }
        }

        //add any new practitioner references
        for (String emisUserGuid: practitioners.getEmisUserGuidsToSave()) {

            //we're updating an existing FHIR resource, so need to explicitly map the EMIS user GUID to an EDS ID
            String globallyUniqueId = IdHelper.getOrCreateEdsResourceIdString(csvProcessor.getServiceId(),
                    csvProcessor.getSystemId(),
                    ResourceType.Practitioner,
                    emisUserGuid);

            references.add(ReferenceHelper.createReference(ResourceType.Practitioner, globallyUniqueId));
        }

        for (String emisUserGuid: practitioners.getEmisUserGuidsToDelete()) {

            //we're updating an existing FHIR resource, so need to explicitly map the EMIS user GUID to an EDS ID
            String globallyUniqueId = IdHelper.getOrCreateEdsResourceIdString(csvProcessor.getServiceId(),
                    csvProcessor.getSystemId(),
                    ResourceType.Practitioner,
                    emisUserGuid);

            Reference referenceToDelete = ReferenceHelper.createReference(ResourceType.Practitioner, globallyUniqueId);

            for (Reference existing: references) {
                //the FHIR objects don't implement an equals(..) override, so we must manually iterate check the reference internal String
                if (referenceToDelete.getReference().equals(existing.getReference())) {
                    references.remove(existing);
                    break;
                }
            }
        }

        //save the references back into the schedule, treating the first as the main practitioner
        if (!references.isEmpty()) {

            Reference first = references.get(0);
            fhirSchedule.setActor(first);

            //add any additional references as additional actors
            for (int i = 1; i < references.size(); i++) {
                Reference additional = references.get(i);
                fhirSchedule.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.SCHEDULE_ADDITIONAL_ACTOR, additional));
            }
        }

        csvProcessor.saveAdminResource(null, false, fhirSchedule);
    }

    public void cacheOrganisationLocationMap(String locationGuid, String orgGuid, boolean mainLocation) {

        List<String> orgGuids = organisationLocationMap.get(locationGuid);
        if (orgGuids == null) {
            orgGuids = new ArrayList<>();
            organisationLocationMap.put(locationGuid, orgGuids);
        }

        //if this location link is for the main location of an organisation, then insert that
        //org at the start of the list, so it's used as the managing organisation for the location
        if (mainLocation) {
            orgGuids.add(0, orgGuid);
        } else {
            orgGuids.add(orgGuid);
        }

    }

    public List<String> findOrganisationLocationMapping(String locationGuid) {
        return organisationLocationMap.remove(locationGuid);
    }

    /**
     * object to temporarily store relationships between resources, such as things linked to a problem
     * or observations linked to a parent observation
     */
    /*public class ResourceRelationship {
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
    }*/

    public class SessionPractitioners {
        private List<String> emisUserGuidsToSave = new ArrayList<>();
        private List<String> emisUserGuidsToDelete = new ArrayList<>();
        private boolean processedSession = false;

        public List<String> getEmisUserGuidsToSave() {
            return emisUserGuidsToSave;
        }

        public void setEmisUserGuidsToSave(List<String> emisUserGuidsToSave) {
            this.emisUserGuidsToSave = emisUserGuidsToSave;
        }

        public List<String> getEmisUserGuidsToDelete() {
            return emisUserGuidsToDelete;
        }

        public void setEmisUserGuidsToDelete(List<String> emisUserGuidsToDelete) {
            this.emisUserGuidsToDelete = emisUserGuidsToDelete;
        }

        public boolean isProcessedSession() {
            return processedSession;
        }

        public void setProcessedSession(boolean processedSession) {
            this.processedSession = processedSession;
        }
    }
}
