package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Patient;
import org.endeavourhealth.transform.emis.csv.schema.Coding_ClinicalCode;
import org.endeavourhealth.transform.emis.openhr.schema.VocSex;
import org.endeavourhealth.transform.emis.openhr.transforms.common.SexConverter;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.io.IOException;
import java.util.*;

public class PatientTransformer {

    public static void transformPatients(String folderPath, CSVFormat csvFormat, Map<String, List<Resource>> fhirResources) throws Exception {

        Admin_Patient patientParser = new Admin_Patient(folderPath, csvFormat);
        try {
            while (patientParser.nextRecord()) {
                transformPatient(patientParser, fhirResources);
            }
        } finally {
            patientParser.close();
        }
    }

    private static void transformPatient(Admin_Patient patientParser, Map<String, List<Resource>> fhirResources) throws Exception {

        if (patientParser.getDeleted()) {
            //TODO - how to process Deleted EMIS records so they should be deleted from EDS?
            return;
        }

        //ignore dummy patient records
        if (patientParser.getDummyType()) {
            return;
        }

        Patient fhirPatient = new Patient();
        fhirPatient.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PATIENT));

        UUID patientGuid = patientParser.getPatientGuid();
        fhirPatient.setId(patientGuid.toString());
        //TODO - can I use Patient GUID or should I change to Person GUID???

        EmisCsvTransformer.addToMap(patientGuid, fhirPatient, fhirResources);

        String nhsNumber = patientParser.getNhsNumber();
        fhirPatient.addIdentifier(Fhir.createIdentifier(Identifier.IdentifierUse.OFFICIAL, nhsNumber, FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER));

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
            ContactPoint fhirContact = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME, homePhone);
            fhirPatient.addTelecom(fhirContact);
        }

        String mobilePhone = patientParser.getMobilePhone();
        if (!Strings.isNullOrEmpty(mobilePhone)) {
            ContactPoint fhirContact = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE, mobilePhone);
            fhirPatient.addTelecom(fhirContact);
        }

        String email = patientParser.getEmailAddress();
        if (!Strings.isNullOrEmpty(email)) {
            ContactPoint fhirContact = Fhir.createContactPoint(ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.HOME, email);
            fhirPatient.addTelecom(fhirContact);
        }

        UUID organisationGuid = patientParser.getOrganisationGuid();
        fhirPatient.setManagingOrganization(Fhir.createReference(ResourceType.Organization, organisationGuid.toString()));

        String carerName = patientParser.getCarerName();
        String carerRelationship = patientParser.getCarerRelation();
        if (!Strings.isNullOrEmpty(carerName)) {

            Patient.ContactComponent fhirContact = new Patient.ContactComponent();

            if (!Strings.isNullOrEmpty(carerRelationship)) {
                fhirContact.addRelationship(Fhir.createCodeableConcept(carerRelationship));
            }

            //TODO - need to tokenise carerName to populate fields on FHIR resource?
            HumanName fhirName = new HumanName();
            fhirName.setText(carerName);
            fhirContact.setName(fhirName);

            fhirPatient.addContact(fhirContact);
        }

        String patientType = patientParser.getPatientTypedescription();
        fhirPatient.addExtension(Fhir.createExtension(FhirExtensionUri.REGISTRATION_TYPE, new StringType(patientType)));

        //TODO - store admin_patient SpineSensitive in FHIR?
        //TODO - store admin_patient IsConfidential in FHIR?

        UUID usualGpGuid = patientParser.getUsualGpUserInRoleGuid();
        if (usualGpGuid != null) {
            fhirPatient.addCareProvider(Fhir.createReference(ResourceType.Practitioner, usualGpGuid.toString()));

        } else {
            UUID externalGpGuid = patientParser.getExternalUsualGPGuid();
            if (externalGpGuid != null) {
                fhirPatient.addCareProvider(Fhir.createReference(ResourceType.Practitioner, externalGpGuid.toString()));

            } else {
                UUID externalOrgGuid = patientParser.getExternalUsualGPOrganisation();
                if (externalOrgGuid != null) {
                    fhirPatient.addCareProvider(Fhir.createReference(ResourceType.Organization, externalOrgGuid.toString()));
                }
            }
        }

        EpisodeOfCare fhirEpisode = new EpisodeOfCare();
        fhirEpisode.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_EPISODE_OF_CARE));

        EmisCsvTransformer.addToMap(patientGuid, fhirEpisode, fhirResources);

        fhirEpisode.setPatient(Fhir.createReference(ResourceType.Patient, patientGuid.toString()));

        UUID orgUuid = patientParser.getOrganisationGuid();
        fhirEpisode.setManagingOrganization(Fhir.createReference(ResourceType.Organization, orgUuid.toString()));

        if (usualGpGuid != null) {
            fhirEpisode.setCareManager(Fhir.createReference(ResourceType.Practitioner, usualGpGuid.toString()));
        }

        Date regDate = patientParser.getDateOfRegistration();
        Date dedDate = patientParser.getDateOfDeactivation();
        Period fhirPeriod = Fhir.createPeriod(regDate, dedDate);
        fhirEpisode.setPeriod(fhirPeriod);

        boolean active = Fhir.isActive(fhirPeriod);
        fhirPatient.setActive(active);
        if (active) {
            fhirEpisode.setStatus(EpisodeOfCare.EpisodeOfCareStatus.ACTIVE);
        } else {
            fhirEpisode.setStatus(EpisodeOfCare.EpisodeOfCareStatus.FINISHED);
        }
    }
}
