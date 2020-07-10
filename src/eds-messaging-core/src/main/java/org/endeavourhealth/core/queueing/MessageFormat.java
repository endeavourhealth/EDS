package org.endeavourhealth.core.queueing;

public class MessageFormat {


    //NOTE: These Strings correspond to the message format values in the SystemEditComponent.ts class
    public static final String EMIS_OPEN = "EMISOPEN";
    public static final String EMIS_OPEN_HR = "OPENHR";
    public static final String EMIS_CSV = "EMISCSV";
    public static final String TPP_CSV = "TPPCSV";
    public static final String TPP_XML = "TPPXML";
    public static final String FHIR_JSON = "FHIRJSON";
    public static final String FHIR_XML = "FHIRXML";
    public static final String VITRUICARE_XML = "VITRUCARE";
    public static final String EDW_XML = "EDWXML";
    public static final String TABLEAU = "TABLEAU";
    public static final String ENTERPRISE_CSV = "ENTERPRISE_CSV";
    public static final String HL7V2 = "HL7V2";
    public static final String ADASTRA_XML = "ADASTRA_XML";
    public static final String BARTS_CSV = "BARTSCSV";
    public static final String HOMERTON_CSV = "HOMERTONCSV";
    public static final String VISION_CSV = "VISIONCSV";
    public static final String ADASTRA_CSV = "ADASTRACSV";
    public static final String JSON_API = "JSON_API";
    public static final String PCR_CSV = "PCR_CSV";
    public static final String SUBSCRIBER_CSV = "SUBSCRIBER_CSV";
    public static final String BHRUT_CSV = "BHRUTCSV";
    public static final String IMPERIAL_HL7_V2 = "IMPERIALHL7V2";

    public static final String DUMMY_SENDER_SOFTWARE_FOR_BULK_DELETE = "BULK_DELETE_DATA";

    //public static final String DUMMY_SENDER_SOFTWARE_FOR_BULK_SUBSCRIBER_TRANSFORM = "BULK_TRANSFORM_TO_SUBSCRIBER"; //superseded by below ones
    /*public static final String DUMMY_SENDER_SOFTWARE_FOR_BULK_SUBSCRIBER_DELETE = "BULK_DELETE_FROM_SUBSCRIBER";
    public static final String DUMMY_SENDER_SOFTWARE_FOR_BULK_SUBSCRIBER_REFRESH = "BULK_REFRESH_OF_SUBSCRIBER";
    public static final String DUMMY_SENDER_SOFTWARE_FOR_BULK_SUBSCRIBER_QUICK_REFRESH = "BULK_QUICK_REFRESH_OF_SUBSCRIBER";*/
}
