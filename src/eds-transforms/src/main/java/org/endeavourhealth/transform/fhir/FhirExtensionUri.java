package org.endeavourhealth.transform.fhir;

public class FhirExtensionUri {

    //extensions specific to Patient resource
    public final static String PATIENT_REGISTRATION_TYPE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-patient-registration-type-extension";
    public final static String PATIENT_RESIDENTIAL_INSTITUTE_CODE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-patient-residential-institute-code-extension";
    public final static String PATIENT_NHS_NUMBER_VERIFICATION_STATUS = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-nhs-number-verification-status-extension";
    public final static String PATIENT_SPINE_SENSITIVE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-patient-spine-sensitive-extension";
    public final static String PATIENT_ETHNICITY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-ethnic-category-extension";

    //extensions specific to Appointment resource
    public final static String APPOINTMENT_PATIENT_WAIT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-appointment-wait-extension";
    public final static String APPOINTMENT_PATIENT_DELAY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-appointment-delay-extension";
    public final static String APPOINTMENT_DNA_REASON_CODE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-appointment-dna-reason-extension";
    public final static String APPOINTMENT_SENT_IN = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-appointment-sent-in-extension";
    public final static String APPOINTMENT_LEFT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-appointment-left-extension";

    //extensions specific to Location resource
    public final static String LOCATION_MAIN_CONTACT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-location-main-contact-extension";

    //extensions specific to Organisation resource
    public final static String ORGANISATION_MAIN_LOCATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-mainlocation-extension";

    //extensions specific to Problem resource
    public final static String PROBLEM_EXPECTED_DURATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem-expectedduration-extension";
    public final static String PROBLEM_LAST_REVIEWED = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem-lastreviewed-extension";
    public final static String _PROBLEM_LAST_REVIEWED__DATE = "date";
    public final static String _PROBLEM_LAST_REVIEWED__PERFORMER = "performer";
    public final static String PROBLEM_SIGNIFICANCE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem-significance-extension";
    public final static String PROBLEM_ASSOCIATED_RESOURCE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem-associated-extension";
    public final static String PROBLEM_RELATED = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem-related-extension";

    //extensions specific to Condition resources
    public final static String CONDITION_PART_OF_PROBLEM = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-condition-partofproblemepisode-extension";

    //extensions specific to FamilyMemberHistory resources
    public final static String FAMILY_MEMBER_HISTOY_REPORTED_BY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-family-member-history-reporter-extension";

    //extensions specific to ReferralRequest resources
    public final static String REFERRAL_REQUEST_SEND_MODE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-referral-request-send-mode-extension";

    //extensions specific to DiagnosticReport resources
    public final static String DIAGNOSTIC_REPORT_FILED_BY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-diagnostic-report-filed-by-extension";

    //extensions specific to Task resource
    public final static String TASK_TYPE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-task-type-extension";
    public final static String TASK_STATUS = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-task-status-extension";
    public final static String TASK_PRIORITY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-task-priority-extension";

    //extensions specific to Schedule resource
    public final static String SCHEDULE_LOCATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-location-extension";
    public final static String SCHEDULE_ADDITIONAL_ACTOR = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-additional-actor-extension";

    //extensions specific to Encounter resource
    public final static String ENCOUNTER_SOURCE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-encounter_source";

    //extensions specific to MedicationStatement (profile is MedicationAuthorisation) resource
    public final static String MEDICATION_AUTHORISATION_PRIVATE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-privateprescription-extension";
    public final static String MEDICATION_AUTHORISATION_FIRST_ISSUE_DATE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-firstissuedate-extension";
    public final static String MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-mostrecentissuedate-extension";
    public final static String MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ALLOWED = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-numberofrepeatsallowed-extension";
    public final static String MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ISSUED = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-numberofrepeatsissued-extension";
    public final static String MEDICATION_AUTHORISATION_CANCELLATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-cancellation-extension";
    public final static String MEDICATION_AUTHORISATION_EXPECTED_SUPPLY_DURATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-expectedsupplyduration-extension";
    public final static String MEDICATION_AUTHORISATION_QUANTITY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-quantity-extension";
    public final static String MEDICATION_AUTHORISATION_TYPE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-type-extension";

    //extensions specific to MedicationOrder resource
    public final static String MEDICATION_ORDER_ESTIMATED_COST = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-order-estimatednhscost-extension";
    public final static String MEDICATION_ORDER_AUTHORISATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-order-authorisation-extension";
    public final static String MEDICATION_ORDER_SUPPLY_TYPE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-supply-type-extension";

    //extensions specific to ProcedureRequest resource
    public final static String PROCEDURE_REQUEST_LOCATION_DESCRIPTION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-procedure-request-location-extension";
    public final static String PROCEDURE_REQUEST_SCHEDULE_TEXT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-procedure-request-schedule-text-extension";

    //extensions used by multiple resources
    public final static String RECORDED_BY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-recorded-by-extension";
    public final static String RECORDED_DATE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-recorded-date-extension";
    public final static String EXTERNAL_DOCUMENT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-external-document-extension";
    public final static String ACTIVE_PERIOD = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-activeperiod-extension";
    public final static String ASSOCIATED_ENCOUNTER = "http://hl7.org/fhir/StructureDefinition/encounter-associatedEncounter";
    public final static String PHARMACY_TEXT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-pharmacytext-extension";
    public final static String PATIENT_TEXT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-patienttext-extension";
    public final static String PRESCRIBED_AS_CONTRACEPTION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-prescribedascontraception-extension";
    public final static String QUANTITY_FREE_TEXT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-quantity-text-extension";


}
