package org.endeavourhealth.transform.emis.csv.transforms;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.CsvPatient;
import org.endeavourhealth.transform.emis.csv.schema.CsvSex;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.PractitionerRole;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class PatientTransformer {

    public static void transform(CSVParser patientCsv, Map<String, List<Resource>> fhirMap, int patientCount) throws Exception {

        int row = 0;
        for (CSVRecord csvRecord : patientCsv) {
            transform(csvRecord, fhirMap);
            row ++;
        }

        if (row != patientCount) {
            throw new TransformException("Mismatch in number of patient rows. Expected " + patientCount + " got " + row);
        }
    }

    public static void transform(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

        Organization fhirOrganisation = transformOrganisation(csvRecord);
        Practitioner fhirPractitioner = transformPractitioner(csvRecord, fhirOrganisation);
        Patient fhirPatient = transformPatient(csvRecord, fhirOrganisation, fhirPractitioner);
        EpisodeOfCare fhirEpisodeOfCare = transformEpisodeOfCare(csvRecord, fhirPatient, fhirOrganisation, fhirPractitioner);

        //add to map, keying on care record ID
        List<Resource> fhirResources = new ArrayList<>();
        fhirResources.add(fhirOrganisation);
        fhirResources.add(fhirPractitioner);
        fhirResources.add(fhirPatient);
        fhirResources.add(fhirEpisodeOfCare);
        fhirMap.put(fhirPatient.getId(), fhirResources);
    }

    private static Organization transformOrganisation(CSVRecord csvRecord) {

        String practiceCode = csvRecord.get(CsvPatient.PracticeCode.getValue());

        Organization fhirOrganisation = new Organization();
        fhirOrganisation.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_ORGANIZATION));
        fhirOrganisation.setId(practiceCode);
        fhirOrganisation.addIdentifier(Fhir.createOdsOrganisationIdentifier(practiceCode));
        fhirOrganisation.setType(Fhir.createCodeableConcept(FhirUris.IDENTIFIER_SYSTEM_ODS_CODE, "GP Practices in England and Wales", "PR"));

        //TODO - need to be able to look up Org name from ODS code

        return fhirOrganisation;
    }

    private static Practitioner transformPractitioner(CSVRecord csvRecord, Organization fhirOrganisation) throws TransformException {
        String usualGp = csvRecord.get(CsvPatient.USUALGP.getValue());

        Practitioner fhirPractitioner = new Practitioner();
        fhirPractitioner.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_PRACTITIONER));
        fhirPractitioner.setId(UUID.randomUUID().toString()); //no ID, so assign a UUID

        //FHIR requires a family name, so assume the last word in the string is it. This
        //will be incorrect for doctors with two-word surnames, but there's no way to accurately tell.
        String[] names = usualGp.split(" ");
        String surname = names[names.length-1];

        HumanName fhirName = new HumanName();
        fhirName.setUse(HumanName.NameUse.OFFICIAL);
        fhirName.addFamily(surname);
        fhirName.setText(usualGp);
        fhirPractitioner.setName(fhirName);

        Practitioner.PractitionerPractitionerRoleComponent fhirRole = new Practitioner.PractitionerPractitionerRoleComponent();
        fhirRole.setManagingOrganization(Fhir.createOrganisationReference(fhirOrganisation));
        fhirRole.setRole(Fhir.createCodeableConcept("http://hl7.org/fhir/practitioner-role", "Doctor", "doctor"));
        fhirPractitioner.addPractitionerRole(fhirRole);

        return fhirPractitioner;
    }

    private static Patient transformPatient(CSVRecord csvRecord, Organization fhirOrganisation, Practitioner fhirPractitioner) throws Exception {
        Patient fhirPatient = new Patient();
        fhirPatient.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_PATIENT));

        String careRecordId = csvRecord.get(CsvPatient.CARERECORDID.getValue());
        fhirPatient.setId(careRecordId);

        String nhsNumber = csvRecord.get(CsvPatient.NHSNUMBER.getValue());
        fhirPatient.addIdentifier(Fhir.createNhsNumberIdentifier(nhsNumber));

        String firstName = csvRecord.get(CsvPatient.FORENAME.getValue());
        String surname = csvRecord.get(CsvPatient.SURNAME.getValue());

        HumanName fhirName = Fhir.createHumanName(HumanName.NameUse.OFFICIAL, null, firstName, null, surname);
        fhirPatient.addName(fhirName);

        String sex = csvRecord.get(CsvPatient.SEX.getValue());
        fhirPatient.setGender(convertSex(sex));

        DateFormat df = new SimpleDateFormat(EmisCsvTransformer.DATE_FORMAT); //using old date API as FHIR does

        String dobStr = csvRecord.get(CsvPatient.DATEOFBIRTH.getValue());
        fhirPatient.setBirthDate(df.parse(dobStr));

        String dodStr = csvRecord.get(CsvPatient.DATEOFDEATH.getValue());
        if (!Strings.isNullOrEmpty(dodStr)) {
            fhirPatient.setDeceased(new DateType(df.parse(dodStr)));
        }

        fhirPatient.addAddress(transformAddress(csvRecord));

        fhirPatient.setManagingOrganization(Fhir.createOrganisationReference(fhirOrganisation));
        fhirPatient.addCareProvider(Fhir.createPractitionerReference(fhirPractitioner));


        return fhirPatient;
    }

    private static Address transformAddress(CSVRecord csvRecord) {
        String postcode = csvRecord.get(CsvPatient.POSTCODE.getValue());

        Address fhirAddress = new Address();
        fhirAddress.setUse(Address.AddressUse.HOME);
        fhirAddress.setType(Address.AddressType.BOTH); //assume all addresses are pbysical and postal
        fhirAddress.setPostalCode(postcode);
        fhirAddress.setText(postcode);
        return fhirAddress;
    }

    private static EpisodeOfCare transformEpisodeOfCare(CSVRecord csvRecord, Patient fhirPatient, Organization fhirOrganisation, Practitioner fhirPractitioner) throws Exception {

        EpisodeOfCare fhirEpisodeOfCare = new EpisodeOfCare();
        fhirEpisodeOfCare.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_EPISODE_OF_CARE));

        DateFormat df = new SimpleDateFormat(EmisCsvTransformer.DATE_FORMAT); //using old date API as FHIR does
        String regDateStr = csvRecord.get(CsvPatient.REGDATE.getValue());
        String dedDateStr = csvRecord.get(CsvPatient.DEREGDATE.getValue());

        Date regDate = df.parse(regDateStr);
        Date dedDate = null;
        if (!Strings.isNullOrEmpty(dedDateStr)) {
            dedDate = df.parse(dedDateStr);
        }

        fhirEpisodeOfCare.setPeriod(Fhir.createPeriod(regDate, dedDate));

        if (Fhir.isActive(fhirEpisodeOfCare)) {

            fhirPatient.setActive(true); //can now set the active field on the patient, based on dates in the episode
            fhirEpisodeOfCare.setStatus(EpisodeOfCare.EpisodeOfCareStatus.ACTIVE);
        } else {

            //can now set the active field on the patient, based on dates in the episode
            fhirPatient.setActive(false);
            fhirEpisodeOfCare.setStatus(EpisodeOfCare.EpisodeOfCareStatus.FINISHED);
        }

        fhirEpisodeOfCare.setPatient(Fhir.createPatientReference(fhirPatient));
        fhirEpisodeOfCare.setManagingOrganization(Fhir.createOrganisationReference(fhirOrganisation));
        fhirEpisodeOfCare.setCareManager(Fhir.createPractitionerReference(fhirPractitioner));

        return fhirEpisodeOfCare;
    }


    private static Enumerations.AdministrativeGender convertSex(String sex) throws TransformException
    {
        CsvSex csvSex = CsvSex.fromValue(sex);
        switch (csvSex)
        {
            case U:
                return Enumerations.AdministrativeGender.UNKNOWN;
            case M:
                return Enumerations.AdministrativeGender.MALE;
            case F:
                return Enumerations.AdministrativeGender.FEMALE;
            case I:
                return Enumerations.AdministrativeGender.OTHER;
            default:
                throw new TransformException("Unsupposed sex " + sex);
        }
    }

}
