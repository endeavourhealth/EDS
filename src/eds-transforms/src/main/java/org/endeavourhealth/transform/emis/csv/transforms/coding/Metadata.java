package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.transforms.admin.LocationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.OrganisationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.UserInRoleTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionTransformer;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.PractitionerRole;

import java.sql.Ref;
import java.util.*;
import java.util.stream.Collectors;

public class Metadata {

    private Map<Long, ClinicalCode> clinicalCodes = null;
    private Map<Long, DrugCode> drugCodes = null;
    private Map<String, Location> fhirLocations = null;
    private Map<String, Organization> fhirOrganisations = null;
    private Map<String, Practitioner> fhirPractitioners = null;
    private Map<String, Schedule> fhirSchedules = null;

    public Metadata(Map<Long, ClinicalCode> clinicalCodes, Map<Long, DrugCode> drugCodes,
                    Map<String, Location> fhirLocations, Map<String, Organization> fhirOrganisations,
                    Map<String, Practitioner> fhirPractitioners, Map<String, Schedule> fhirSchedules) {

        this.clinicalCodes = clinicalCodes;
        this.drugCodes = drugCodes;

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
    public DrugCode findDrugCode(Long id) throws Exception {
        DrugCode ret = drugCodes.get(id);
        if (ret == null) {
            throw new TransformException("Failed to find DrugCode for id " + id);
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

    public Reference createLocationReference(String locationGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {

        List<Resource> patientFhirResources = fhirResources.get(patientGuid);
        Reference reference = ReferenceHelper.createReference(ResourceType.Location, locationGuid);
        checkAndCopyLocation(reference, patientFhirResources);
        return reference;
    }

    public Reference createOrganisationReference(String organizationGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        List<Resource> patientFhirResources = fhirResources.get(patientGuid);
        Reference reference = ReferenceHelper.createReference(ResourceType.Organization, organizationGuid);
        checkAndCopyOrganisation(reference, patientFhirResources);
        return reference;
    }

    public Reference createPractitionerReference(String practitionerGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        List<Resource> patientFhirResources = fhirResources.get(patientGuid);
        Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, practitionerGuid);
        checkAndCopyPractitioner(reference, patientFhirResources);
        return reference;
    }

    public Reference createScheduleReference(String scheduleGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        List<Resource> patientFhirResources = fhirResources.get(patientGuid);
        Reference reference = ReferenceHelper.createReference(ResourceType.Schedule, scheduleGuid);
        checkAndCopySchedule(reference, patientFhirResources);
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


    public static void addToMap(String patientGuid, Resource fhirResource, Map<String, List<Resource>> hmResources) throws Exception {
        List<Resource> l = hmResources.get(patientGuid);
        if (l == null) {
            throw new TransformException("Patient GUID " + patientGuid + " found, but not corresponding resource");
        }
        l.add(fhirResource);
    }


    public Reference createAppointmentReference(String appointmentGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        return createAndValidateReference(appointmentGuid, ResourceType.Appointment, patientGuid, fhirResources);
    }
    public Reference createEncounterReference(String encounterGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        return createAndValidateReference(encounterGuid, ResourceType.Encounter, patientGuid, fhirResources);
    }

    private Reference createAndValidateReference(String id, ResourceType resourceType, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        List<Resource> patientFhirResources = fhirResources.get(patientGuid);
        if (!listContains(patientFhirResources, id, resourceType)) {
            throw new TransformException(resourceType + " " + id + " doesn't exist in Resources");
        }
        return ReferenceHelper.createReference(resourceType, id);
    }

    public static DateTimeType createDateTimeType(Date date, String precision) throws Exception {
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

    public static Condition findProblem(String problemGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        return findResource(problemGuid, FhirUri.PROFILE_URI_PROBLEM, patientGuid, fhirResources);
    }

    public static Observation findObservation(String observationGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        return findResource(observationGuid, FhirUri.PROFILE_URI_OBSERVATION, patientGuid, fhirResources);
    }

    private static <T extends Resource> T findResource(String guid, String resourceProfile, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        List<Resource> patientFhirResources = fhirResources.get(patientGuid);
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
}
