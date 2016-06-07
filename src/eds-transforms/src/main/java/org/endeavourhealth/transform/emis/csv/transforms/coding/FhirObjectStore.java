package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class FhirObjectStore {

    //metadata, not relating to patients
    private Map<Long, ClinicalCode> clinicalCodes = null;
    private Map<String, Medication> fhirMedication = null;
    private Map<String, Location> fhirLocations = null;
    private Map<String, Organization> fhirOrganisations = null;
    private Map<String, Practitioner> fhirPractitioners = null;
    private Map<String, Schedule> fhirSchedules = null;

    //patient Resources, keyed by patient ID
    private Map<String, List<Resource>> fhirPatientResources = new HashMap<>();

    public Map<String, List<Resource>> getFhirPatientResources() {
        return fhirPatientResources;
    }

    public FhirObjectStore(Map<Long, ClinicalCode> clinicalCodes, Map<String, Medication> fhirMedication,
                           Map<String, Location> fhirLocations, Map<String, Organization> fhirOrganisations,
                           Map<String, Practitioner> fhirPractitioners, Map<String, Schedule> fhirSchedules) {

        this.clinicalCodes = clinicalCodes;
        this.fhirMedication = fhirMedication;
        this.fhirLocations = fhirLocations;
        this.fhirOrganisations = fhirOrganisations;
        this.fhirPractitioners = fhirPractitioners;
        this.fhirSchedules = fhirSchedules;
    }

    public ClinicalCode findClinicalCode(Long id) throws Exception {
        ClinicalCode ret = clinicalCodes.get(id);
        if (ret == null) {
            throw new TransformException("Failed to find ClinicalCode for id " + id);
        }
        return ret;
    }


    private static <T extends Resource> T checkAndCopyResource(Reference reference, ResourceType resourceType,
                                                                     Map<String, T> resourceMap,
                                                                     List<Resource> fhirResources) throws Exception {
        if (reference == null) {
            return null;
        }

        String id = ReferenceHelper.getReferenceId(reference, resourceType);
        Resource resource = resourceMap.get(id);
        if (resource == null) {
            throw new TransformException("Resource references " + resourceType + " " + id + " but the resource cannot be found");
        }

        if (listContains(fhirResources, resource)) {
            //if the list already contains the resource, return null to stop further processing down the resource hierarchy
            return null;
        }

        //if the resource isn't in the patient list, copy it (deep copy) and add
        resource = resource.copy();
        fhirResources.add(resource);
        return (T)resource;
    }

    private void checkAndCopyMedication(Reference reference, List<Resource> fhirResources) throws Exception {

        Medication medication = checkAndCopyResource(reference, ResourceType.Medication, fhirMedication, fhirResources);
        if (medication != null) {

            //medication doesn't refer to any other external Resources
        }
    }
    private void checkAndCopyLocation(Reference reference, List<Resource> fhirResources) throws Exception {

        Location location = checkAndCopyResource(reference, ResourceType.Location, fhirLocations, fhirResources);
        if (location != null) {

            //location can also refer to a managing organisation and a parent location, so make sure they're carried over too
            checkAndCopyLocation(location.getPartOf(), fhirResources);
            checkAndCopyOrganisation(location.getManagingOrganization(), fhirResources);
        }
    }
    private void checkAndCopyOrganisation(Reference reference, List<Resource> fhirResources) throws Exception {

        Organization organization = checkAndCopyResource(reference, ResourceType.Organization, fhirOrganisations, fhirResources);
        if (organization != null) {

            //location can also refer to a location and a parent organisation, so make sure they're carried over too
            checkAndCopyOrganisation(organization.getPartOf(), fhirResources);

            List<Extension> extensions = organization.getExtension();
            for (Extension extension: extensions) {
                if (extension.getUrl().equals(FhirExtensionUri.MAIN_LOCATION)) {
                    checkAndCopyLocation((Reference)extension.getValue(), fhirResources);
                }
            }
        }
    }
    private void checkAndCopyPractitioner(Reference reference, List<Resource> fhirResources) throws Exception {

        Practitioner practitioner = checkAndCopyResource(reference, ResourceType.Practitioner, fhirPractitioners, fhirResources);
        if (practitioner != null) {

            List<Practitioner.PractitionerPractitionerRoleComponent> roles = practitioner.getPractitionerRole();
            for (Practitioner.PractitionerPractitionerRoleComponent role: roles) {
                checkAndCopyOrganisation(role.getManagingOrganization(), fhirResources);

                List<Reference> roleLocations = role.getLocation();
                for (Reference locationReference: roleLocations) {
                    checkAndCopyLocation(locationReference, fhirResources);
                }
            }
        }
    }
    private void checkAndCopySchedule(Reference reference, List<Resource> fhirResources) throws Exception {

        Schedule schedule = checkAndCopyResource(reference, ResourceType.Schedule, fhirSchedules, fhirResources);
        if (schedule != null) {

            checkAndCopyPractitioner(schedule.getActor(), fhirResources);

            List<Extension> extensions = schedule.getExtension();
            for (Extension extension: extensions) {
                if (extension.getUrl().equals(FhirExtensionUri.ADDITIONAL_ACTOR)) {
                    checkAndCopyPractitioner((Reference)extension.getValue(), fhirResources);
                } else if (extension.getUrl().equals(FhirExtensionUri.LOCATION)) {
                    checkAndCopyLocation((Reference)extension.getValue(), fhirResources);
                }
            }
        }
    }

    public Reference createMedicationReference(Long medicationId, String patientGuid) throws Exception {

        Reference reference = ReferenceHelper.createReference(ResourceType.Medication, medicationId.toString());
        checkAndCopyMedication(reference, fhirPatientResources.get(patientGuid));
        return reference;
    }

    public Reference createLocationReference(String locationGuid, String patientGuid) throws Exception {

        Reference reference = ReferenceHelper.createReference(ResourceType.Location, locationGuid);
        checkAndCopyLocation(reference, fhirPatientResources.get(patientGuid));
        return reference;
    }

    public Reference createOrganisationReference(String organizationGuid, String patientGuid) throws Exception {

        Reference reference = ReferenceHelper.createReference(ResourceType.Organization, organizationGuid);
        checkAndCopyOrganisation(reference, fhirPatientResources.get(patientGuid));
        return reference;
    }

    public Reference createPractitionerReference(String practitionerGuid, String patientGuid) throws Exception {

        Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, practitionerGuid);
        checkAndCopyPractitioner(reference, fhirPatientResources.get(patientGuid));
        return reference;
    }

    public Reference createScheduleReference(String scheduleGuid, String patientGuid) throws Exception {

        Reference reference = ReferenceHelper.createReference(ResourceType.Schedule, scheduleGuid);
        checkAndCopySchedule(reference, fhirPatientResources.get(patientGuid));
        return reference;
    }


    /**
     * tests if a resource already exists in a list. Resources don't implement the equals(..) or hashCode(..)
     * functions, so regular contains(..) etc. tests can't be used
     */
    private static boolean listContains(List<Resource> resources, Resource resource) {

        return listContains(resources, resource.getId(), resource.getResourceType());
    }
    private static boolean listContains(List<Resource> resources, String id, ResourceType resourceType) {

        return resources
                .stream()
                .filter(t -> t.getResourceType() == resourceType)
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .isPresent();
    }

    public void addNewPatient(Patient fhirPatient) throws Exception {
        String patientId = fhirPatient.getId();
        List<Resource> l = fhirPatientResources.get(patientId);
        if (l != null) {
            throw new TransformException("Patient Resource " + patientId + " is duplicated");
        }
        l = new ArrayList<>();
        l.add(fhirPatient);

        fhirPatientResources.put(patientId, l);
    }
    public void addToMap(String patientGuid, Resource fhirResource) throws Exception {
        List<Resource> l = fhirPatientResources.get(patientGuid);
        if (l == null) {
            throw new TransformException("Patient GUID " + patientGuid + " found, but not corresponding resource");
        }
        l.add(fhirResource);
    }

    public Reference createPatientReference(String patientGuid) throws Exception {
        if (fhirPatientResources.get(patientGuid) == null) {
            throw new TransformException("Patient resource " + patientGuid + " cannot be found");
        }
        return ReferenceHelper.createReference(ResourceType.Patient, patientGuid);
    }

    public Reference createAppointmentReference(String appointmentGuid, String patientGuid) throws Exception {
        return createAndValidateReference(appointmentGuid, ResourceType.Appointment, patientGuid);
    }
    public Reference createEncounterReference(String encounterGuid, String patientGuid) throws Exception {
        return createAndValidateReference(encounterGuid, ResourceType.Encounter, patientGuid);
    }
    public Reference createObservationReference(String observationGuid, String patientGuid) throws Exception {
        return createAndValidateReference(observationGuid, ResourceType.Observation, patientGuid);
    }
    public Reference createMedicationStatementReference(String medicationStatementGuid, String patientGuid) throws Exception {
        return createAndValidateReference(medicationStatementGuid, ResourceType.MedicationStatement, patientGuid);
    }

    private Reference createAndValidateReference(String id, ResourceType resourceType, String patientGuid) throws Exception {
        List<Resource> patientFhirResources = fhirPatientResources.get(patientGuid);
        if (patientFhirResources == null) {
            throw new TransformException("No resources found for patient " + patientGuid);
        }

        if (!listContains(patientFhirResources, id, resourceType)) {
            throw new TransformException(resourceType + " " + id + " doesn't exist in Resources");
        }

        return ReferenceHelper.createReference(resourceType, id);
    }

    public static DateTimeType createDateTimeType(Date date, String precision) throws Exception {
        if (date == null) {
            return null;
        }

        //the precision String matches the precisions used in the other EMIS extract
        VocDatePart vocPrecision = VocDatePart.fromValue(precision);
        if (vocPrecision == null) {
            throw new TransformException("Unsupported consultation precision [" + precision + "]");
        }

        switch (vocPrecision) {
            case U:
                return null;
            case Y:
                return new DateTimeType(date, TemporalPrecisionEnum.YEAR);
            case YM:
                return new DateTimeType(date, TemporalPrecisionEnum.MONTH);
            case YMD:
                return new DateTimeType(date, TemporalPrecisionEnum.DAY);
            case YMDT:
                return new DateTimeType(date, TemporalPrecisionEnum.MINUTE);
            default:
                throw new TransformException("Unhandled date precision [" + vocPrecision + "]");
        }
    }

    public static DateType createDateType(Date date, String precision) throws Exception {
        if (date == null) {
            return null;
        }

        VocDatePart vocPrecision = VocDatePart.fromValue(precision);
        if (vocPrecision == null) {
            throw new TransformException("Unsupported consultation precision [" + precision + "]");
        }

        switch (vocPrecision) {
            case U:
                return null;
            case Y:
                return new DateType(date, TemporalPrecisionEnum.YEAR);
            case YM:
                return new DateType(date, TemporalPrecisionEnum.MONTH);
            case YMD:
                return new DateType(date, TemporalPrecisionEnum.DAY);
            default:
                throw new TransformException("Unhandled date precision [" + vocPrecision + "]");
        }
    }

    public Condition findProblem(String problemGuid, String patientGuid) throws Exception {
        return findResource(problemGuid, FhirUri.PROFILE_URI_PROBLEM, patientGuid);
    }

    public Observation findObservation(String observationGuid, String patientGuid) throws Exception {
        return findResource(observationGuid, FhirUri.PROFILE_URI_OBSERVATION, patientGuid);
    }

    private <T extends Resource> T findResource(String guid, String resourceProfile, String patientGuid) throws Exception {
        List<Resource> patientFhirResources = fhirPatientResources.get(patientGuid);
        for (Resource resource: patientFhirResources) {

            if (resource.getId().equals(guid)
                    && resource.getMeta() != null) {

                List<UriType> profiles = resource.getMeta().getProfile();
                for (UriType uri: profiles) {

                    if (uri.getValue().equals(resourceProfile)) {
                        return (T)resource;
                    }
                }
            }
        }

        throw new TransformException("Failed to find " + resourceProfile + " resource for " + guid);
    }

    public void linkToProblem(Resource resource, String problemGuid, String patientGuid) throws Exception {
        if (problemGuid == null) {
            return;
        }

        Reference reference = ReferenceHelper.createReference(resource);
        Condition fhirProblem = findProblem(problemGuid, patientGuid);
        fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE, reference));
    }
}
