package org.endeavourhealth.transform.emis.csv;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.ClinicalCodeType;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmisCsvHelper {

    //metadata, not relating to patients
    private Map<Long, CodeableConcept> clinicalCodes = null;
    private Map<Long, CodeableConcept> fhirMedication = null;

    //some resources are referred to by others, so we cache them here for when we need them
    private Map<String, Condition> conditionMap = new HashMap<>();
    private Map<String, Observation> observationMap = new HashMap<>();
    //private Map<String, DiagnosticReport> diagnosticReportMap = new HashMap<>();

    public EmisCsvHelper(Map<Long, CodeableConcept> clinicalCodes, Map<Long, CodeableConcept> fhirMedication) {

        this.clinicalCodes = clinicalCodes;
        this.fhirMedication = fhirMedication;
    }

    /**
     * to ensure globally unique IDs for all resources, a new ID is created
     * from the patientGuid and sourceGuid (e.g. observationGuid)
     */
    private static String createUniqueId(String patientGuid, String sourceGuid) {
        if (sourceGuid == null) {
            return patientGuid;
        } else {
            return patientGuid + "-" + sourceGuid;
        }
    }

    public static void setUniqueId(Resource resource, String patientGuid, String sourceGuid) {
        resource.setId(createUniqueId(patientGuid, sourceGuid));
    }


    public CodeableConcept findClinicalCode(Long id) throws Exception {
        CodeableConcept ret = clinicalCodes.get(id);
        if (ret == null) {

            //TODO - need to store these somewhere to handle deltas

            throw new TransformException("Failed to find code CodeableConcept for id " + id);
        }
        return ret.copy();
    }

    public ClinicalCodeType findClinicalCodeType(Long codeId) {
        //TODO - implement this
        return null;
    }

    public CodeableConcept findMedication(Long id) throws Exception {
        CodeableConcept ret = fhirMedication.get(id);
        if (ret == null) {

            //TODO - need to store these somewhere to handle deltas

            throw new TransformException("Failed to find medication CodeableConcept for id " + id);
        }
        return ret.copy();
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


    public void cacheCondition(Condition condition) {
        conditionMap.put(condition.getId(), condition);
    }
    public void cacheObservation(Observation observation) {
        observationMap.put(observation.getId(), observation);
    }
    /*public void cacheDiagnosticReport(DiagnosticReport diagnosticReport) {
        diagnosticReportMap.put(diagnosticReport.getId(), diagnosticReport);
    }*/


    /**
     * tests if a resource already exists in a list. Resources don't implement the equals(..) or hashCode(..)
     * functions, so regular contains(..) etc. tests can't be used
     */
    private static boolean listContains(List<Resource> resources, String objectGuid, String patientGuid, ResourceType resourceType) {
        String uniqueId = createUniqueId(patientGuid, objectGuid);
        return resources
                .stream()
                .filter(t -> t.getResourceType() == resourceType)
                .filter(t -> t.getId().equals(uniqueId))
                .findFirst()
                .isPresent();
    }




    public Condition findProblem(String problemGuid, String patientGuid) throws Exception {
        return findResource(problemGuid, patientGuid, conditionMap);
    }

    public Observation findObservation(String observationGuid, String patientGuid) throws Exception {
        return findResource(observationGuid, patientGuid, observationMap);
    }

    /*public DiagnosticReport findDiagnosticReport(String observationGuid, String patientGuid) throws Exception {
        return findResource(observationGuid, patientGuid, diagnosticReportMap);
    }*/

    private <T extends Resource> T findResource(String guid, String patientGuid, Map<String, T> map) throws Exception {
        String uniqueId = createUniqueId(patientGuid, guid);

        T resource = map.get(uniqueId);

        if (resource == null) {
            //TODO - if Resource not found, must retrieve from the EDS data store
        }

        if (resource == null) {
            throw new TransformException("Failed to find resource for guid " + guid);
        }

        return resource;
    }

    public void linkToProblem(Resource resource, String problemGuid, String patientGuid) throws Exception {
        if (problemGuid == null) {
            return;
        }
//TODO - load from EHR problem if required
        Reference reference = ReferenceHelper.createReference(resource);
        Condition fhirProblem = findProblem(problemGuid, patientGuid);
        //TODO - make sure to add to queue again?????

        //TODO - validate if resource is already linked to the problem or not

        fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE, reference));
    }

    public boolean isObservationToDelete(String patientGuid, String observationGuid) {
        String uniqueId = createUniqueId(patientGuid, observationGuid);
        Observation observation = observationMap.get(observationGuid);
        if (observation == null) {
            //if we're calling this function but haven't cached the observation for the GUID,
            //it means the ob isn't deleted. If the ob were deleted, then we wouldn't be getting
            //updates to the Referral or Problem file for things that relate to it.
            return false;
        }

        //observations always have a code unless we're deleting the resource
        return !observation.hasCode();
    }
}
