package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.CsvCurrentState;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.admin.Patient;
import org.endeavourhealth.transform.emis.openhr.schema.VocSex;
import org.endeavourhealth.transform.emis.openhr.transforms.common.SexConverter;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.ContactRelationship;
import org.endeavourhealth.transform.fhir.schema.NhsNumberVerificationStatus;
import org.endeavourhealth.transform.fhir.schema.RegistrationType;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class PatientTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        AbstractCsvParser parser = parsers.get(Patient.class);
        while (parser.nextRecord()) {

            try {
                createResource((Patient)parser, fhirResourceFiler, csvHelper, version);
            } catch (Exception ex) {
                fhirResourceFiler.logTransformRecordError(ex, parser.getCurrentState());
            }
        }
    }


    private static void createResource(Patient parser,
                                      FhirResourceFiler fhirResourceFiler,
                                      EmisCsvHelper csvHelper,
                                       String version) throws Exception {

        //create Patient Resource
        org.hl7.fhir.instance.model.Patient fhirPatient = new org.hl7.fhir.instance.model.Patient();
        fhirPatient.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PATIENT));

        String patientGuid = parser.getPatientGuid();
        String organisationGuid = parser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirPatient, patientGuid, null);

        //create Episode of Care Resource
        EpisodeOfCare fhirEpisode = new EpisodeOfCare();
        fhirEpisode.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_EPISODE_OF_CARE));

        EmisCsvHelper.setUniqueId(fhirEpisode, patientGuid, null);

        fhirEpisode.setPatient(csvHelper.createPatientReference(patientGuid.toString()));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            //Emis send us a delete for a patient WITHOUT a corresponding delete for all other data, so
            //we need to manually delete all dependant resources
            deleteEntirePatientRecord(fhirResourceFiler, csvHelper, parser.getCurrentState(), patientGuid, fhirPatient, fhirEpisode);
            return;
        }

        String nhsNumber = parser.getNhsNumber();
        if (!Strings.isNullOrEmpty(nhsNumber)) {
            fhirPatient.addIdentifier(IdentifierHelper.createNhsNumberIdentifier(nhsNumber));
        }

        //store the patient GUID and patient number to the patient resource
        int patientNumber = parser.getPatientNumber();
        fhirPatient.addIdentifier(IdentifierHelper.createIdentifier(Identifier.IdentifierUse.SECONDARY, FhirUri.IDENTIFIER_SYSTEM_EMIS_PATIENT_GUID, patientGuid));
        fhirPatient.addIdentifier(IdentifierHelper.createIdentifier(Identifier.IdentifierUse.SECONDARY, FhirUri.IDENTIFIER_SYSTEM_EMIS_PATIENT_NUMBER, "" + patientNumber));

        Date dob = parser.getDateOfBirth();
        fhirPatient.setBirthDate(dob);

        Date dod = parser.getDateOfDeath();
        if (dod != null) {
            fhirPatient.setDeceased(new DateType(dod));
        }

        //EMIS only provides sex but FHIR requires gender, but will treat as the same concept
        VocSex vocSex = VocSex.fromValue(parser.getSex());
        Enumerations.AdministrativeGender gender = SexConverter.convertSexToFhir(vocSex);
        fhirPatient.setGender(gender);

        String title = parser.getTitle();
        String givenName = parser.getGivenName();
        String middleNames = parser.getMiddleNames();
        String surname = parser.getSurname();

        //the test CSV data has at least one patient with no surname, so treat the given name as surname
        if (Strings.isNullOrEmpty(surname)) {
            surname = givenName;
            givenName = "";
        }

        String forenames = (givenName + " " + middleNames).trim();

        List<HumanName> fhirNames = NameConverter.convert(title, forenames, surname, null, null, null);
        if (fhirNames != null) {
            fhirNames.forEach(fhirPatient::addName);
        }

        String houseNameFlat = parser.getHouseNameFlatNumber();
        String numberAndStreet = parser.getNumberAndStreet();
        String village = parser.getVillage();
        String town = parser.getTown();
        String county = parser.getCounty();
        String postcode = parser.getPostcode();

        Address fhirAddress = AddressConverter.createAddress(Address.AddressUse.HOME, houseNameFlat, numberAndStreet, village, town, county, postcode);
        fhirPatient.addAddress(fhirAddress);

        String residentialInstituteCode = parser.getResidentialInstituteCode();
        if (!Strings.isNullOrEmpty(residentialInstituteCode)) {
            fhirPatient.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_RESIDENTIAL_INSTITUTE_CODE, new StringType(residentialInstituteCode)));
        }

        String nhsNumberStatus = parser.getNHSNumberStatus();
        if (!Strings.isNullOrEmpty(nhsNumberStatus)) {
            CodeableConcept fhirCodeableConcept = null;

            //convert the String to one of the official statuses. If it can't be converted, insert free-text in the codeable concept
            NhsNumberVerificationStatus verificationStatus = convertNhsNumberVeriticationStatus(nhsNumberStatus);
            if (verificationStatus != null) {
                fhirCodeableConcept = CodeableConceptHelper.createCodeableConcept(verificationStatus);

            } else {
                fhirCodeableConcept = CodeableConceptHelper.createCodeableConcept(nhsNumberStatus);
            }

            fhirPatient.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_NHS_NUMBER_VERIFICATION_STATUS, fhirCodeableConcept));
        }

        String homePhone = parser.getHomePhone();
        ContactPoint fhirContact = ContactPointHelper.create(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME, homePhone);
        fhirPatient.addTelecom(fhirContact);

        String mobilePhone = parser.getMobilePhone();
        fhirContact = ContactPointHelper.create(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE, mobilePhone);
        fhirPatient.addTelecom(fhirContact);

        String email = parser.getEmailAddress();
        fhirContact = ContactPointHelper.create(ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.HOME, email);
        fhirPatient.addTelecom(fhirContact);

        fhirPatient.setManagingOrganization(csvHelper.createOrganisationReference(organisationGuid));

        String carerName = parser.getCarerName();
        String carerRelationship = parser.getCarerRelation();
        if (!Strings.isNullOrEmpty(carerName)) {

            org.hl7.fhir.instance.model.Patient.ContactComponent fhirContactComponent = new org.hl7.fhir.instance.model.Patient.ContactComponent();
            fhirContactComponent.setName(NameConverter.convert(carerName));

            if (!Strings.isNullOrEmpty(carerRelationship)) {
                //FHIR spec states that we should map to their relationship types if possible, but if
                //not possible, then send as a textual codeable concept
                try {
                    ContactRelationship fhirContactRelationship = ContactRelationship.fromCode(carerRelationship);
                    fhirContactComponent.addRelationship(CodeableConceptHelper.createCodeableConcept(fhirContactRelationship));
                } catch (IllegalArgumentException ex) {
                    fhirContactComponent.addRelationship(CodeableConceptHelper.createCodeableConcept(carerRelationship));
                }
            }

            fhirPatient.addContact(fhirContactComponent);
        }

        boolean spineSensitive = parser.getSpineSensitive();
        if (spineSensitive) {
            fhirPatient.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_SPINE_SENSITIVE, new BooleanType(spineSensitive)));
        }

        RegistrationType registrationType = convertRegistrationType(parser.getPatientTypedescription(), parser.getDummyType());
        fhirPatient.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_REGISTRATION_TYPE, CodingHelper.createCoding(registrationType)));

        String usualGpGuid = parser.getUsualGpUserInRoleGuid();
        if (!Strings.isNullOrEmpty(usualGpGuid)) {
            fhirPatient.addCareProvider(csvHelper.createPractitionerReference(usualGpGuid));

        } else {
            String externalGpGuid = parser.getExternalUsualGPGuid();
            if (!Strings.isNullOrEmpty(externalGpGuid)) {
                fhirPatient.addCareProvider(csvHelper.createPractitionerReference(externalGpGuid));

            } else {

                //have to handle the mis-spelling of the column name in EMIS test pack
                //String externalOrgGuid = patientParser.getExternalUsualGPOrganisation();
                String externalOrgGuid = null;
                if (version.equals(EmisCsvToFhirTransformer.VERSION_5_0)
                        || version.equals(EmisCsvToFhirTransformer.VERSION_5_1)) {
                    externalOrgGuid = parser.getExternalUsusalGPOrganisation();
                } else {
                    externalOrgGuid = parser.getExternalUsualGPOrganisation();
                }

                if (!Strings.isNullOrEmpty(externalOrgGuid)) {
                    fhirPatient.addCareProvider(csvHelper.createOrganisationReference(externalOrgGuid));
                }
            }
        }

        transformEthnicityAndMaritalStatus(fhirPatient, patientGuid, csvHelper, fhirResourceFiler);

        String orgUuid = parser.getOrganisationGuid();
        fhirEpisode.setManagingOrganization(csvHelper.createOrganisationReference(orgUuid));

        if (!Strings.isNullOrEmpty(usualGpGuid)) {
            fhirEpisode.setCareManager(csvHelper.createPractitionerReference(usualGpGuid));
        }

        Date regDate = parser.getDateOfRegistration();
        Date dedDate = parser.getDateOfDeactivation();
        Period fhirPeriod = PeriodHelper.createPeriod(regDate, dedDate);
        fhirEpisode.setPeriod(fhirPeriod);

        boolean active = PeriodHelper.isActive(fhirPeriod);
        fhirPatient.setActive(active);
        if (active) {
            fhirEpisode.setStatus(EpisodeOfCare.EpisodeOfCareStatus.ACTIVE);
        } else {
            fhirEpisode.setStatus(EpisodeOfCare.EpisodeOfCareStatus.FINISHED);
        }

        //save both resources together, so the patient is defintiely saved before the episode
        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirPatient, fhirEpisode);
    }

    /**
     * Emis send us a delete for a patient WITHOUT a corresponding delete for all other data, so
     * we need to manually delete all dependant resources
     */
    private static void deleteEntirePatientRecord(FhirResourceFiler fhirResourceFiler, EmisCsvHelper csvHelper,
                                                  CsvCurrentState currentState, String patientGuid,
                                                  org.hl7.fhir.instance.model.Patient fhirPatient, EpisodeOfCare fhirEpisode) throws Exception {

        //don't bother doing these two, since the below delete will pick them up
        //fhirResourceFiler.deletePatientResource(currentState, patientGuid, fhirPatient, fhirEpisode);

        try {
            List<Resource> resources = csvHelper.retrieveAllResourcesForPatient(patientGuid, fhirResourceFiler);
            for (Resource resource : resources) {
                fhirResourceFiler.deletePatientResource(currentState, false, patientGuid, resource);
            }
        } catch (ResourceNotFoundException ex) {
            //if this is the first time we've received anything for this patient, we won't be able to find it in the DB
        }
    }

    private static void transformEthnicityAndMaritalStatus(org.hl7.fhir.instance.model.Patient fhirPatient,
                                                        String patientGuid,
                                                        EmisCsvHelper csvHelper,
                                                        FhirResourceFiler fhirResourceFiler) throws Exception {

        CodeableConcept fhirEthnicity = csvHelper.findEthnicity(patientGuid);
        CodeableConcept fhirMaritalStatus = csvHelper.findMaritalStatus(patientGuid);

        //if we don't have an ethnicity or marital status already cached, we may be performing a delta transform
        //so need to see if we have one previously saved that we should carry over
        if (fhirEthnicity == null || fhirMaritalStatus == null) {
            try {
                org.hl7.fhir.instance.model.Patient oldFhirPatient = (org.hl7.fhir.instance.model.Patient)csvHelper.retrieveResource(patientGuid, ResourceType.Patient, fhirResourceFiler);

                if (fhirEthnicity == null) {
                    for (Extension extension: oldFhirPatient.getExtension()) {
                        if (extension.getUrl().equals(FhirExtensionUri.PATIENT_ETHNICITY)) {
                            fhirEthnicity = (CodeableConcept)extension.getValue();
                        }
                    }
                }

                if (fhirMaritalStatus == null) {
                    fhirMaritalStatus = oldFhirPatient.getMaritalStatus();
                }

            } catch (Exception ex) {
                //if the patient didn't previously exist, then we'll get an exception
            }
        }

        if (fhirEthnicity != null) {
            fhirPatient.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_ETHNICITY, fhirEthnicity));
        }

        if (fhirMaritalStatus != null) {
            fhirPatient.setMaritalStatus(fhirMaritalStatus);
        }
    }

    /**
     * converts free-text NHS number status to one of the official NHS statuses
     */
    private static NhsNumberVerificationStatus convertNhsNumberVeriticationStatus(String nhsNumberStatus) {
        //note: no idea what possible values will come from EMIS in this field, and there's no content
        //in the column on the two live extracts seen. So this is more of a placeholder until we get some more info.
        if (nhsNumberStatus.equalsIgnoreCase("Verified")) {
            return NhsNumberVerificationStatus.PRESENT_AND_VERIFIED;
        } else {
            return null;
        }
    }

    /**
     * converts the patientDescription String from the CSV to the FHIR registration type
     * possible registration types based on the VocPatientType enum from EMIS Open
     */
    private static RegistrationType convertRegistrationType(String csvRegType, boolean dummyRecord) {

        //EMIS both test and Live data has leading spaces
        csvRegType = csvRegType.trim();

        if (dummyRecord || csvRegType.equalsIgnoreCase("Dummy")) {
            return RegistrationType.DUMMY;
        } else if (csvRegType.equalsIgnoreCase("Emg")) {
            return RegistrationType.EMERGENCY;
        } else if (csvRegType.equalsIgnoreCase("Immediately necessary")) {
            return RegistrationType.IMMEDIATELY_NECESSARY;
        } else if (csvRegType.equalsIgnoreCase("Private")) {
            return RegistrationType.PRIVATE;
        } else if (csvRegType.equalsIgnoreCase("Regular")) {
            return RegistrationType.REGULAR_GMS;
        } else if (csvRegType.equalsIgnoreCase("Temporary")) {
            return RegistrationType.TEMPORARY;
        } else {
            return RegistrationType.OTHER;
        }
    }

}
