package org.endeavourhealth.transform.emis.csv.transforms;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.CsvPatient;
import org.endeavourhealth.transform.emis.csv.schema.CsvSex;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class PatientTransformer {

    private static String DATE_FORMAT = "yyyyMMdd";

    public static void transform(CSVParser patientCsv, Map<String, List<Resource>> fhirMap) throws Exception {

        for (CSVRecord csvRecord : patientCsv) {
            transform(csvRecord, fhirMap);
        }
    }

    public static void transform(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

        Patient fhirPatient = transformPatient(csvRecord);
        EpisodeOfCare fhirEpisodeOfCare = transformEpisodeOfCare(csvRecord, fhirPatient);

        //add to map, keying on care record ID
        List<Resource> fhirResources = new ArrayList<>();
        fhirResources.add(fhirPatient);
        fhirResources.add(fhirEpisodeOfCare);
        fhirMap.put(fhirPatient.getId(), fhirResources);
    }

    private static Patient transformPatient(CSVRecord csvRecord) throws Exception {
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

        DateFormat df = new SimpleDateFormat(DATE_FORMAT); //using old date API as FHIR does

        String dobStr = csvRecord.get(CsvPatient.DATEOFBIRTH.getValue());
        fhirPatient.setBirthDate(df.parse(dobStr));

        String dodStr = csvRecord.get(CsvPatient.DATEOFDEATH.getValue());
        if (!Strings.isNullOrEmpty(dodStr)) {
            fhirPatient.setDeceased(new DateType(df.parse(dodStr)));
        }

        fhirPatient.addAddress(transformAddress(csvRecord));

        String practiceCode = csvRecord.get(CsvPatient.PracticeCode.getValue());

        String usualGp = csvRecord.get(CsvPatient.USUALGP.getValue());

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

    private static EpisodeOfCare transformEpisodeOfCare(CSVRecord csvRecord, Patient fhirPatient) throws Exception {

        EpisodeOfCare fhirEpisodeOfCare = new EpisodeOfCare();
        fhirEpisodeOfCare.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_EPISODE_OF_CARE));

        //fhirPatient.setActive(String)

        DateFormat df = new SimpleDateFormat(DATE_FORMAT); //using old date API as FHIR does
        String regDateStr = csvRecord.get(CsvPatient.REGDATE.getValue());
        String dedDateStr = csvRecord.get(CsvPatient.DEREGDATE.getValue());

        Period period = new Period();

        if (Strings.isNullOrEmpty(dedDateStr)) {

        } else {

            Date dedDate = df.parse(dedDateStr);
            if (dedDate.after(new Date())) {

            }
        }

        return fhirEpisodeOfCare;
    }


    private static Enumerations.AdministrativeGender convertSex(String sex)
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
