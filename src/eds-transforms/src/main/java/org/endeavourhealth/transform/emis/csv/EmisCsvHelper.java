package org.endeavourhealth.transform.emis.csv;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class EmisCsvHelper {

    //metadata, not relating to patients
    private Map<Long, ClinicalCode> clinicalCodes = null;
    private Map<String, Medication> fhirMedication = null;
    private Map<String, Location> fhirLocations = null;
    private Map<String, Organization> fhirOrganisations = null;
    private Map<String, Practitioner> fhirPractitioners = null;
    private Map<String, Schedule> fhirSchedules = null;

    //patient Resources, keyed by patient ID
    private Map<String, FhirPatientStore> fhirPatientStores = new HashMap<>();

    public EmisCsvHelper(Map<Long, ClinicalCode> clinicalCodes, Map<String, Medication> fhirMedication,
                         Map<String, Location> fhirLocations, Map<String, Organization> fhirOrganisations,
                         Map<String, Practitioner> fhirPractitioners, Map<String, Schedule> fhirSchedules) {

        this.clinicalCodes = clinicalCodes;
        this.fhirMedication = fhirMedication;
        this.fhirLocations = fhirLocations;
        this.fhirOrganisations = fhirOrganisations;
        this.fhirPractitioners = fhirPractitioners;
        this.fhirSchedules = fhirSchedules;
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

    public List<FhirPatientStore> getFhirPatientStores() {
        return new ArrayList<>(fhirPatientStores.values());
    }

    public ClinicalCode findClinicalCode(Long id) throws Exception {
        ClinicalCode ret = clinicalCodes.get(id);
        if (ret == null) {
            throw new TransformException("Failed to find ClinicalCode for id " + id);
        }
        return ret;
    }


    private static <T extends Resource> T validateAndCopyResource(String resourceId,
                                                                  String patientGuid,
                                                                  Map<String, T> resourceMap,
                                                                  FhirPatientStore patientStore) throws Exception {

        Resource resource = resourceMap.get(resourceId);
        if (resource == null) {
            //TODO - do EMIS CSV deltas include all Admin resources or just changes ones?
            throw new TransformException("Resource " + resourceId + " could not be found for patient " + patientGuid);
        }



        //TODO - Because of the Delta, we need to load up all the Organisation resources for the patient, so we don't duplicate them when we create process a DELTA!!!!

        //if the list already contains the resource, don't add to the patient store again, so we only
        //have one instance of each resource per patient (e.g. we don't duplicate the organisation resource)
        if (!listContains(patientStore.getResourcesToSave(), resourceId, patientGuid, resource.getResourceType())) {

            //create the new, unique ID that the copy of the resource will need
            String combinedId = createUniqueId(patientGuid, resourceId);

            //copy the resource object and assign the new resource ID to it
            resource = resource.copy();
            resource.setId(combinedId);

            patientStore.addResourceToSave(resource);
            return (T)resource;

        } else {
            return null;
        }
    }

    private void validateAndCopyMedication(String resourceId, String patientGuid) throws Exception {

        Medication medication = validateAndCopyResource(resourceId, patientGuid, fhirMedication, fhirPatientStores.get(patientGuid));

        if (medication != null) {
            //medication doesn't refer to any other external Resources
        }
    }
    private void validateAndCopyLocation(String resourceId, String patientGuid) throws Exception {

        Location location = validateAndCopyResource(resourceId, patientGuid, fhirLocations, fhirPatientStores.get(patientGuid));

        if (location != null) {

            //location can also refer to a managing organisation and a parent location, so make sure they're carried over too
            Reference partOf = location.getPartOf();
            if (partOf != null) {
                String locationGuid = ReferenceHelper.getReferenceId(partOf);
                validateAndCopyLocation(locationGuid, patientGuid);
                location.setPartOf(ReferenceHelper.createReference(ResourceType.Location, createUniqueId(patientGuid, locationGuid)));
            }

            Reference managingOrg = location.getManagingOrganization();
            if (managingOrg != null) {
                String organisationGuid = ReferenceHelper.getReferenceId(managingOrg);
                validateAndCopyOrganisation(organisationGuid, patientGuid);
                location.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, createUniqueId(patientGuid, organisationGuid)));
            }
        }
    }
    private void validateAndCopyOrganisation(String resourceId, String patientGuid) throws Exception {

        Organization organization = validateAndCopyResource(resourceId, patientGuid, fhirOrganisations, fhirPatientStores.get(patientGuid));

        if (organization != null) {
            //location can also refer to a location and a parent organisation, so make sure they're carried over too
            Reference partOf = organization.getPartOf();
            if (partOf != null) {
                String organisationGuid = ReferenceHelper.getReferenceId(partOf);
                validateAndCopyOrganisation(organisationGuid, patientGuid);
                organization.setPartOf(ReferenceHelper.createReference(ResourceType.Organization, createUniqueId(patientGuid, organisationGuid)));
            }

            List<Extension> extensions = organization.getExtension();
            for (Extension extension: extensions) {
                if (extension.getUrl().equals(FhirExtensionUri.MAIN_LOCATION)) {
                    Reference mainLocation = (Reference)extension.getValue();
                    if (mainLocation != null) {
                        String locationGuid = ReferenceHelper.getReferenceId(mainLocation);
                        validateAndCopyLocation(locationGuid, patientGuid);
                        extension.setValue(ReferenceHelper.createReference(ResourceType.Location, createUniqueId(patientGuid, locationGuid)));
                    }
                }
            }
        }
    }
    private void validateAndCopyPractitioner(String resourceId, String patientGuid) throws Exception {

        Practitioner practitioner = validateAndCopyResource(resourceId, patientGuid, fhirPractitioners, fhirPatientStores.get(patientGuid));

        if (practitioner != null) {
            //practitioners can refer to organisations and locations, so make sure they're copied over too
            List<Practitioner.PractitionerPractitionerRoleComponent> roles = practitioner.getPractitionerRole();
            for (Practitioner.PractitionerPractitionerRoleComponent role: roles) {
                Reference managingOrg = role.getManagingOrganization();
                if (managingOrg != null) {
                    String organisationGuid = ReferenceHelper.getReferenceId(managingOrg);
                    validateAndCopyOrganisation(organisationGuid, patientGuid);
                    role.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, createUniqueId(patientGuid, organisationGuid)));
                }

                List<Reference> roleLocations = role.getLocation();
                for (int i=0; i<roleLocations.size(); i++) {
                    String locationGuid = ReferenceHelper.getReferenceId(roleLocations.get(i));
                    validateAndCopyLocation(locationGuid, patientGuid);
                    roleLocations.set(i, ReferenceHelper.createReference(ResourceType.Location, createUniqueId(patientGuid, locationGuid)));
                }
            }
        }
    }
    private void validateAndCopySchedule(String resourceId, String patientGuid) throws Exception {

        Schedule schedule = validateAndCopyResource(resourceId, patientGuid, fhirSchedules, fhirPatientStores.get(patientGuid));

        if (schedule != null) {
            Reference actor = schedule.getActor();
            if (actor != null) {
                String practitionerguid = ReferenceHelper.getReferenceId(actor);
                validateAndCopyPractitioner(practitionerguid, patientGuid);
                schedule.setActor(ReferenceHelper.createReference(ResourceType.Practitioner, createUniqueId(patientGuid, practitionerguid)));
            }

            List<Extension> extensions = schedule.getExtension();
            for (Extension extension: extensions) {
                if (extension.getUrl().equals(FhirExtensionUri.ADDITIONAL_ACTOR)) {
                    Reference additionalActor = (Reference)extension.getValue();
                    String practitionerGuid = ReferenceHelper.getReferenceId(additionalActor);
                    validateAndCopyPractitioner(practitionerGuid, patientGuid);
                    extension.setValue(ReferenceHelper.createReference(ResourceType.Practitioner, createUniqueId(patientGuid, practitionerGuid)));

                } else if (extension.getUrl().equals(FhirExtensionUri.LOCATION)) {
                    Reference location = (Reference)extension.getValue();
                    String locationGuid = ReferenceHelper.getReferenceId(location);
                    validateAndCopyLocation(locationGuid, patientGuid);
                    extension.setValue(ReferenceHelper.createReference(ResourceType.Location, createUniqueId(patientGuid, locationGuid)));
                }
            }
        }
    }

    public Reference createMedicationReference(Long medicationId, String patientGuid) throws Exception {
        validateAndCopyMedication(medicationId.toString(), patientGuid);
        return ReferenceHelper.createReference(ResourceType.Medication, createUniqueId(patientGuid, medicationId.toString()));
    }

    public Reference createLocationReference(String locationGuid, String patientGuid) throws Exception {
        validateAndCopyLocation(locationGuid, patientGuid);
        return ReferenceHelper.createReference(ResourceType.Location, createUniqueId(patientGuid, locationGuid));
    }

    public Reference createOrganisationReference(String organizationGuid, String patientGuid) throws Exception {
        validateAndCopyOrganisation(organizationGuid, patientGuid);
        return ReferenceHelper.createReference(ResourceType.Organization, createUniqueId(patientGuid, organizationGuid));
    }

    public Reference createPractitionerReference(String practitionerGuid, String patientGuid) throws Exception {
        validateAndCopyPractitioner(practitionerGuid, patientGuid);
        return ReferenceHelper.createReference(ResourceType.Practitioner, createUniqueId(patientGuid, practitionerGuid));
    }

    public Reference createScheduleReference(String scheduleGuid, String patientGuid) throws Exception {
        validateAndCopySchedule(scheduleGuid, patientGuid);
        return ReferenceHelper.createReference(ResourceType.Schedule, createUniqueId(patientGuid, scheduleGuid));
    }

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

    private String findOdsCodeForOrganisationGuid(String organisationGuid) throws TransformException {
        Organization organization = fhirOrganisations.get(organisationGuid);

        List<Identifier> identifiers = organization.getIdentifier();
        for (Identifier identifier: identifiers) {
            if (identifier.getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE)) {
                return identifier.getValue();
            }
        }

        throw new TransformException("Failed to find Organisation for GUID " + organisationGuid);
    }

    public void addResourceToSave(String patientGuid, String organisationGuid, Resource fhirResource, boolean save) throws Exception {
        FhirPatientStore s = fhirPatientStores.get(createMapKey(patientGuid, organisationGuid));
        if (s == null) {

            //find the organisation ODS code
            String odsCode = findOdsCodeForOrganisationGuid(organisationGuid);

            s = new FhirPatientStore();
            s.setOrganisationOds(odsCode);
            fhirPatientStores.put(createMapKey(patientGuid, organisationGuid), s);
        }

        if (save) {
            s.addResourceToSave(fhirResource);
        } else {
            s.addResourceToDelete(fhirResource);
        }
    }

    private static String createMapKey(String patientGuid, String organisationGuid) {
        return patientGuid + "|" + organisationGuid;
    }

    public Reference createPatientReference(String patientGuid) throws Exception {
        return ReferenceHelper.createReference(ResourceType.Patient, createUniqueId(patientGuid, null));
    }

    public Reference createAppointmentReference(String appointmentGuid, String patientGuid) throws Exception {
        return createAndValidateReference(FhirUri.PROFILE_URI_APPOINTMENT, appointmentGuid, ResourceType.Appointment, patientGuid);
    }
    public Reference createEncounterReference(String encounterGuid, String patientGuid) throws Exception {
        return createAndValidateReference(FhirUri.PROFILE_URI_ENCOUNTER, encounterGuid, ResourceType.Encounter, patientGuid);
    }
    public Reference createObservationReference(String observationGuid, String patientGuid) throws Exception {
        return createAndValidateReference(FhirUri.PROFILE_URI_OBSERVATION, observationGuid, ResourceType.Observation, patientGuid);
    }
    public Reference createMedicationStatementReference(String medicationStatementGuid, String patientGuid) throws Exception {
        return createAndValidateReference(FhirUri.PROFILE_URI_MEDICATION_AUTHORISATION, medicationStatementGuid, ResourceType.MedicationStatement, patientGuid);
    }
    public Reference createProblemReference(String problemGuid, String patientGuid) throws Exception {
        return createAndValidateReference(FhirUri.PROFILE_URI_PROBLEM, problemGuid, ResourceType.Condition, patientGuid);
    }


    private Reference createAndValidateReference(String fhirProfile, String id, ResourceType resourceType, String patientGuid) throws Exception {

        String uniqueId = createUniqueId(patientGuid, id);


        //EMIS CSV format supplies deltas, so we may receive data that references data we haven't received in this extract, so can't do this
        /*List<Resource> patientFhirResources = fhirPatientResourcesToSave.get(patientGuid);
        if (patientFhirResources == null) {
            throw new TransformException("No resources found for patient " + patientGuid);
        }

        if (!listContains(patientFhirResources, uniqueId, resourceType, fhirProfile)) {
            throw new TransformException(resourceType + " " + id + " doesn't exist in Resources");
        }*/

        return ReferenceHelper.createReference(resourceType, uniqueId);
    }


    public Condition findProblem(String problemGuid, String patientGuid) throws Exception {
        return findResource(problemGuid, FhirUri.PROFILE_URI_PROBLEM, patientGuid);
    }

    public Observation findObservation(String observationGuid, String patientGuid) throws Exception {
        return findResource(observationGuid, FhirUri.PROFILE_URI_OBSERVATION, patientGuid);
    }

    public DiagnosticReport findDiagnosticReport(String observationGuid, String patientGuid) throws Exception {
        return findResource(observationGuid, FhirUri.PROFILE_URI_DIAGNOSTIC_REPORT, patientGuid);
    }

    private <T extends Resource> T findResource(String guid, String resourceProfile, String patientGuid) throws Exception {
        String uniqueId = createUniqueId(patientGuid, guid);

        FhirPatientStore fhirPatientStore = fhirPatientStores.get(patientGuid);
        for (Resource resource: fhirPatientStore.getResourcesToSave()) {

            if (resource.getId().equals(uniqueId)
                    && resource.getMeta() != null) {

                List<UriType> profiles = resource.getMeta().getProfile();
                for (UriType uri: profiles) {

                    if (uri.getValue().equals(resourceProfile)) {
                        return (T)resource;
                    }
                }
            }
        }

        //TODO - if Resource not found, must retrieve from the EDS data store
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

    public boolean isObservationToDelete(String patientGuid, String observationGuid) {
        FhirPatientStore fhirPatientStore = fhirPatientStores.get(patientGuid);
        if (fhirPatientStore == null) {
            return false;
        }

        List<Resource> resourcesToDelete = fhirPatientStore.getResourcesToDelete();
        return listContains(resourcesToDelete, observationGuid, patientGuid, ResourceType.Observation);
    }
}
