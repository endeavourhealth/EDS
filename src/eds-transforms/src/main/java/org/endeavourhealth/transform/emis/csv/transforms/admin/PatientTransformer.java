package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Patient;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.openhr.schema.VocSex;
import org.endeavourhealth.transform.emis.openhr.transforms.common.SexConverter;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.schema.ContactRelationship;
import org.endeavourhealth.transform.fhir.schema.RegistrationType;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class PatientTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, EmisCsvHelper objectStore) throws Exception {

        Admin_Patient parser = new Admin_Patient(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createPatient(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createPatient(Admin_Patient patientParser, EmisCsvHelper objectStore) throws Exception {

        //create Patient Resource
        Patient fhirPatient = new Patient();
        fhirPatient.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PATIENT));

        String patientGuid = patientParser.getPatientGuid();
        String organisationGuid = patientParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirPatient, patientGuid, null);
        //fhirPatient.setId(patientGuid);

        //create Episode of Care Resource
        EpisodeOfCare fhirEpisode = new EpisodeOfCare();
        fhirEpisode.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_EPISODE_OF_CARE));

        EmisCsvHelper.setUniqueId(fhirEpisode, patientGuid, null);

        fhirEpisode.setPatient(objectStore.createPatientReference(patientGuid.toString()));

        boolean store = !patientParser.getDeleted() && !patientParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirPatient, store);
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirEpisode, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        String nhsNumber = patientParser.getNhsNumber();
        fhirPatient.addIdentifier(IdentifierHelper.createIdentifier(Identifier.IdentifierUse.OFFICIAL, nhsNumber, FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER));

        Date dob = patientParser.getDateOfBirth();
        fhirPatient.setBirthDate(dob);

        Date dod = patientParser.getDateOfDeath();
        if (dod != null) {
            fhirPatient.setDeceased(new DateType(dod));
        }

        VocSex vocSex = VocSex.fromValue(patientParser.getSex());
        Enumerations.AdministrativeGender gender = SexConverter.convertSex(vocSex);
        fhirPatient.setGender(gender);

        String givenName = patientParser.getGivenName();
        String middleNames = patientParser.getMiddleNames();
        String forenames = (givenName + " " + middleNames).trim();

        List<HumanName> fhirNames = NameConverter.convert(patientParser.getTitle(), forenames, patientParser.getSurname(), null, null, null);
        if (fhirNames != null) {
            fhirNames.forEach(fhirPatient::addName);
        }

        String houseNameFlat = patientParser.getHouseNameFlatNumber();
        String numberAndStreet = patientParser.getNumberAndStreet();
        String village = patientParser.getVillage();
        String town = patientParser.getTown();
        String county = patientParser.getCounty();
        String postcode = patientParser.getPostcode();

        Address fhirAddress = AddressConverter.createAddress(Address.AddressUse.HOME, houseNameFlat, numberAndStreet, village, town, county, postcode);
        fhirPatient.addAddress(fhirAddress);

        String homePhone = patientParser.getHomePhone();
        if (!Strings.isNullOrEmpty(homePhone)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME, homePhone);
            fhirPatient.addTelecom(fhirContact);
        }

        String mobilePhone = patientParser.getMobilePhone();
        if (!Strings.isNullOrEmpty(mobilePhone)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE, mobilePhone);
            fhirPatient.addTelecom(fhirContact);
        }

        String email = patientParser.getEmailAddress();
        if (!Strings.isNullOrEmpty(email)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.HOME, email);
            fhirPatient.addTelecom(fhirContact);
        }

        fhirPatient.setManagingOrganization(objectStore.createOrganisationReference(organisationGuid, patientGuid));

        String carerName = patientParser.getCarerName();
        String carerRelationship = patientParser.getCarerRelation();
        if (!Strings.isNullOrEmpty(carerName)) {

            Patient.ContactComponent fhirContact = new Patient.ContactComponent();

            if (!Strings.isNullOrEmpty(carerRelationship)) {

                ContactRelationship contactRelationship = convertContactRelationship(carerRelationship);
                fhirContact.addRelationship(CodeableConceptHelper.createCodeableConcept(contactRelationship));
            }

            fhirContact.setName(NameConverter.convert(carerName));

            fhirPatient.addContact(fhirContact);
        }

        RegistrationType registrationType = convertRegistrationType(patientParser.getPatientTypedescription(), patientParser.getDummyType());
        fhirPatient.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.REGISTRATION_TYPE, CodingHelper.createCoding(registrationType)));

        //ignore dummy patient records
        if (patientParser.getDummyType()) {
            return;
        }

        String usualGpGuid = patientParser.getUsualGpUserInRoleGuid();
        if (usualGpGuid != null) {
            fhirPatient.addCareProvider(objectStore.createPractitionerReference(usualGpGuid, patientGuid));

        } else {
            String externalGpGuid = patientParser.getExternalUsualGPGuid();
            if (externalGpGuid != null) {
                fhirPatient.addCareProvider(objectStore.createPractitionerReference(externalGpGuid, patientGuid));

            } else {
                String externalOrgGuid = patientParser.getExternalUsualGPOrganisation();
                if (externalOrgGuid != null) {
                    fhirPatient.addCareProvider(objectStore.createOrganisationReference(externalOrgGuid, patientGuid));
                }
            }
        }

        String orgUuid = patientParser.getOrganisationGuid();
        fhirEpisode.setManagingOrganization(objectStore.createOrganisationReference(orgUuid, patientGuid));

        if (usualGpGuid != null) {
            fhirEpisode.setCareManager(objectStore.createPractitionerReference(usualGpGuid, patientGuid));
        }

        Date regDate = patientParser.getDateOfRegistration();
        Date dedDate = patientParser.getDateOfDeactivation();
        Period fhirPeriod = PeriodHelper.createPeriod(regDate, dedDate);
        fhirEpisode.setPeriod(fhirPeriod);

        boolean active = PeriodHelper.isActive(fhirPeriod);
        fhirPatient.setActive(active);
        if (active) {
            fhirEpisode.setStatus(EpisodeOfCare.EpisodeOfCareStatus.ACTIVE);
        } else {
            fhirEpisode.setStatus(EpisodeOfCare.EpisodeOfCareStatus.FINISHED);
        }
    }

    /**
     * converts the patientDescription String from the CSV to the FHIR registration type
     * possible registration types based on the VocPatientType enum from EMIS Open
     */
    private static RegistrationType convertRegistrationType(String csvRegType, boolean dummyRecord) {

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

    private static ContactRelationship convertContactRelationship(String csvRelationship) {
        //TODO - verify conversion of CSV carer relationship types to FHIR contact types
        return ContactRelationship.fromCode(csvRelationship);
    }
}
