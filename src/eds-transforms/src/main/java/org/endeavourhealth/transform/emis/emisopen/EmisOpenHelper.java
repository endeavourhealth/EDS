package org.endeavourhealth.transform.emis.emisopen;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

public class EmisOpenHelper {

    public static final String QUALIFIER_GROUP_TERM_FAMILY_MEMBER = "Family member";
    public static final String QUALIFIER_GROUP_TERM_INJECTION_SITE = "Injection site";
    public static final String QUALIFIER_GROUP_TERM_GMS = "GMS";
    public static final String QUALIFIER_GROUP_TERM_BATCH_NUMBER = "Batch Number";
    public static final String QUALIFIER_GROUP_TERM_EXPIRY_DATE = "Expiry Date";
    public static final String QUALIFIER_GROUP_TERM_MANUFACTURER = "Manufacturer";


    private static final String ID_DELIMITER = ":";

    /**
     * to ensure globally unique IDs for all resources, a new ID is created
     * from the patientGuid and sourceGuid (e.g. observationGuid)
     */
    public static String createUniqueId(String patientGuid, String sourceGuid) {
        if (sourceGuid == null) {
            return patientGuid;
        } else {
            return patientGuid + ID_DELIMITER + sourceGuid;
        }
    }


    /**
     * admin-type resources just use the EMIS CSV GUID as their reference
     */
    public static Reference createLocationReference(String locationGuid) {
        return ReferenceHelper.createReference(ResourceType.Location, locationGuid);
    }
    public static Reference createOrganisationReference(String organizationGuid) {
        return ReferenceHelper.createReference(ResourceType.Organization, organizationGuid);
    }
    public static Reference createPractitionerReference(String practitionerGuid)  {
        return ReferenceHelper.createReference(ResourceType.Practitioner, practitionerGuid);
    }
    public static Reference createScheduleReference(String scheduleGuid) {
        return ReferenceHelper.createReference(ResourceType.Schedule, scheduleGuid);
    }
    public static Reference createSlotReference(String slotGuid) {
        return ReferenceHelper.createReference(ResourceType.Slot, slotGuid);
    }

    /**
     * patient-type resources must include the patient GUID are part of the unique ID in the reference
     * because the EMIS GUIDs for things like Obs are only unique within that patient record itself
     */
    public static Reference createPatientReference(String patientGuid) {
        return ReferenceHelper.createReference(ResourceType.Patient, createUniqueId(patientGuid, null));
    }
    public static Reference createAppointmentReference(String appointmentGuid, String patientGuid) throws Exception {
        if (Strings.isNullOrEmpty(appointmentGuid)) {
            throw new IllegalArgumentException("Missing appointmentGuid");
        }
        return ReferenceHelper.createReference(ResourceType.Appointment, createUniqueId(patientGuid, appointmentGuid));
    }
    public static Reference createEncounterReference(String encounterGuid, String patientGuid) {
        if (Strings.isNullOrEmpty(encounterGuid)) {
            throw new IllegalArgumentException("Missing encounterGuid");
        }
        return ReferenceHelper.createReference(ResourceType.Encounter, createUniqueId(patientGuid, encounterGuid));
    }
    public static Reference createObservationReference(String observationGuid, String patientGuid) throws Exception {
        if (Strings.isNullOrEmpty(observationGuid)) {
            throw new IllegalArgumentException("Missing observationGuid");
        }
        return ReferenceHelper.createReference(ResourceType.Observation, createUniqueId(patientGuid, observationGuid));
    }
    public static Reference createMedicationStatementReference(String medicationStatementGuid, String patientGuid) throws Exception {
        if (Strings.isNullOrEmpty(medicationStatementGuid)) {
            throw new IllegalArgumentException("Missing medicationStatementGuid");
        }
        return ReferenceHelper.createReference(ResourceType.MedicationStatement, createUniqueId(patientGuid, medicationStatementGuid));
    }
    public static Reference createProblemReference(String problemGuid, String patientGuid) {
        if (Strings.isNullOrEmpty(problemGuid)) {
            throw new IllegalArgumentException("Missing problemGuid");
        }
        return ReferenceHelper.createReference(ResourceType.Condition, createUniqueId(patientGuid, problemGuid));
    }

    public static void setUniqueId(Resource resource, String patientGuid, String sourceGuid) {
        resource.setId(createUniqueId(patientGuid, sourceGuid));
    }
}
