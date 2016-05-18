package org.endeavourhealth.transform.fhir;

public class FhirUris {

    public final static String IDENTIFIER_SYSTEM_NHSNUMBER = "http://fhir.nhs.net/Id/nhs-number";
    public final static String IDENTIFIER_SYSTEM_CHINUMBER = "http://www.endeavourhealth.org/fhir/Identifier/chinumber";
    public final static String IDENTIFIER_SYSTEM_ODS_CODE = "http://fhir.nhs.net/Id/ods-organization-code";
    public final static String IDENTIFIER_SYSTEM_GMC_NUMBER = "http://endeavourhealth.org/fhir/Identifier/gmc-number";
    public final static String IDENTIFIER_SYSTEM_DOCTOR_INDEX_NUMBER = "http://endeavourhealth.org/fhir/Identifier/doctor-index-number";
    public final static String IDENTIFIER_SYSTEM_GMP_PPD_CODE = "http://endeavourhealth.org/fhir/Identifier/gmp-ppd-code";

    public final static String CODE_SYSTEM_READ2 = "http://endeavourhealth.org/fhir/read2";
    public final static String CODE_SYSTEM_SNOMED_CT = "http://snomed.info/sct";
    public final static String CODE_SYSTEM_CTV3 = "http://endeavourhealth.org/fhir/ctv3";
    public final static String CODE_SYSTEM_EMISSNOMED = "http://www.endeavourhealth.org/fhir/emis-snomed";
    public final static String CODE_SYSTEM_EMISPREPARATION = "http://www.endeavourhealth.org/fhir/emis-prepration";

    public final static String PROFILE_URI_ORGANIZATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-organization";
    public final static String PROFILE_URI_LOCATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-location";
    public final static String PROFILE_URI_PRACTITIONER = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-practitioner";
    public final static String PROFILE_URI_SCHEDULE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-schedule";
    public final static String PROFILE_URI_SLOT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-slot";
    public final static String PROFILE_URI_APPOINTMENT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-appointment";
    public final static String PROFILE_URI_ALLERGY_INTOLERANCE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-allergy-intolerance";
    public final static String PROFILE_URI_CONDITION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-condition";
    public final static String PROFILE_URI_DIAGNOSTIC_ORDER = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-diagnostic-order";
    public final static String PROFILE_URI_DIAGNOSTIC_REPORT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-diagnostic-report";
    public final static String PROFILE_URI_FAMILY_MEMBER_HISTORY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-family-member-history";
    public final static String PROFILE_URI_IMMUNIZATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-immunization";
    public final static String PROFILE_URI_MEDICATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication";
    public final static String PROFILE_URI_MEDICATION_AUTHORISATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation";
    public final static String PROFILE_URI_MEDICATION_ORDER = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-order";
    public final static String PROFILE_URI_OBSERVATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-observation";
    public final static String PROFILE_URI_PROBLEM = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem";
    public final static String PROFILE_URI_PROCEDURE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-procedure";
    public final static String PROFILE_URI_PROCEDURE_REQUEST = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-procedure-request";
    public final static String PROFILE_URI_REFERRAL_REQUEST = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-referral-request";
    public final static String PROFILE_URI_SPECIMIN = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-specimen";
    public final static String PROFILE_URI_ENCOUNTER = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-encounter";
    public final static String PROFILE_URI_PATIENT = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-patient";
    public final static String PROFILE_URI_EPISODE_OF_CARE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-episode-of-care";
    public final static String PROFILE_URI_TASK = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-task";

    public final static String EXTENSION_URI_ACTIVEPERIOD = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-activeperiod-extension";
    public final static String EXTENSION_URI_MAINLOCATION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-mainlocation-extension";
    public final static String EXTENSION_URI_LOCATIONEXTENSION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-location-extension";
    public final static String EXTENSION_URI_ADDITIONALACTOREXTENSION = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-additional-actor-extension";
    public final static String EXTENSION_URI_TASKTYPE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-task-type-extension";
    public final static String EXTENSION_URI_TASKSTATUS = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-task-status-extension";
    public final static String EXTENSION_URI_TASKPRIORITY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-task-priority-extension";
    public final static String EXTENSION_URI_REGISTRATION_TYPE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-patient-registration-type-extension";
    public final static String EXTENSION_URI_MEDICATIONAUTHORISATIONQUANTITY = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-quantity-extension";
    public final static String EXTENSION_URI_MEDICATIONAUTHORISATIONMOSTRECENTISSUEDATE = "http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-mostrecentissuedate-extension";

    public final static String VALUE_SET_FAMILY_MEMBER = "http://hl7.org/fhir/ValueSet/v3-FamilyMember";
    public final static String VALUE_SET_FAMILY_MEMBER_TERM = "family member";
    public final static String VALUE_SET_FAMILY_MEMBER_CODE = "FAMMEMB";
}
