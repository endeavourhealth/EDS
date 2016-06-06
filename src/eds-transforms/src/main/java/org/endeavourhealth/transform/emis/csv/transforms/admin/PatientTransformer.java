package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Patient;
import org.endeavourhealth.transform.emis.csv.transforms.coding.Metadata;
import org.endeavourhealth.transform.emis.openhr.schema.VocSex;
import org.endeavourhealth.transform.emis.openhr.transforms.common.SexConverter;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class PatientTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {

        Admin_Patient parser = new Admin_Patient(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createPatient(parser, metadata, fhirResources);
            }
        } finally {
            parser.close();
        }
    }

    private static void createPatient(Admin_Patient patientParser, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {

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

        String patientGuid = patientParser.getPatientGuid();
        fhirPatient.setId(patientGuid);
        //TODO - can I use Patient GUID or should I change to Person GUID???

        //add the Patient resource to the map, keying on patient GUID
        List<Resource> l = fhirResources.get(patientGuid);
        if (l != null) {
            throw new TransformException("Patient GUID " + patientGuid + " found is duplicated");
        }
        l = new ArrayList<>();
        fhirResources.put(patientGuid, l);
        l.add(fhirPatient);

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

        String organisationGuid = patientParser.getOrganisationGuid();
        fhirPatient.setManagingOrganization(metadata.createOrganisationReference(organisationGuid, patientGuid, fhirResources));

        String carerName = patientParser.getCarerName();
        String carerRelationship = patientParser.getCarerRelation();
        if (!Strings.isNullOrEmpty(carerName)) {

            Patient.ContactComponent fhirContact = new Patient.ContactComponent();

            if (!Strings.isNullOrEmpty(carerRelationship)) {
                fhirContact.addRelationship(CodeableConceptHelper.createCodeableConcept(carerRelationship));
            }

            //TODO - need to tokenise carerName to populate fields on FHIR resource?
            HumanName fhirName = new HumanName();
            fhirName.setText(carerName);
            fhirContact.setName(fhirName);

            fhirPatient.addContact(fhirContact);
        }

        String patientType = patientParser.getPatientTypedescription();
        fhirPatient.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.REGISTRATION_TYPE, new StringType(patientType)));

        //TODO - store admin_patient SpineSensitive in FHIR?
        //TODO - store admin_patient IsConfidential in FHIR?

        String usualGpGuid = patientParser.getUsualGpUserInRoleGuid();
        if (usualGpGuid != null) {
            fhirPatient.addCareProvider(metadata.createPractitionerReference(usualGpGuid, patientGuid, fhirResources));

        } else {
            String externalGpGuid = patientParser.getExternalUsualGPGuid();
            if (externalGpGuid != null) {
                fhirPatient.addCareProvider(metadata.createPractitionerReference(externalGpGuid, patientGuid, fhirResources));

            } else {
                String externalOrgGuid = patientParser.getExternalUsualGPOrganisation();
                if (externalOrgGuid != null) {
                    fhirPatient.addCareProvider(metadata.createOrganisationReference(externalOrgGuid, patientGuid, fhirResources));
                }
            }
        }

        EpisodeOfCare fhirEpisode = new EpisodeOfCare();
        fhirEpisode.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_EPISODE_OF_CARE));

        Metadata.addToMap(patientGuid, fhirEpisode, fhirResources);

        fhirEpisode.setPatient(ReferenceHelper.createPatientReference(patientGuid.toString()));

        String orgUuid = patientParser.getOrganisationGuid();
        fhirEpisode.setManagingOrganization(metadata.createOrganisationReference(orgUuid, patientGuid, fhirResources));

        if (usualGpGuid != null) {
            fhirEpisode.setCareManager(metadata.createPractitionerReference(usualGpGuid, patientGuid, fhirResources));
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
}
