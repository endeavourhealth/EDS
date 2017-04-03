package org.endeavourhealth.transform.emis.reverseCsv.schema.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

import java.util.Date;

public class Patient extends AbstractCsvWriter {

    public Patient(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeLine(String patientGuid,
                          String organisationGuid,
                          String usualGpUserInRoleGuid,
                          String sex,
                          Date dateOfBirth,
                          Date dateOfDeath,
                          String title,
                          String givenName,
                          String middleNames,
                          String surname,
                          Date dateOfRegistration,
                          String nhsNumber,
                          Integer patientNumber,
                          String patientTypeDescription,
                          boolean dummyType,
                          String houseNameFlatNumber,
                          String numberAndStreet,
                          String village,
                          String town,
                          String county,
                          String postcode,
                          String residentialInstituteCode,
                          String nHSNumberStatus,
                          String carerName,
                          String carerRelation,
                          String personGuid,
                          Date dateOfDeactivation,
                          boolean deleted,
                          boolean spineSensitive,
                          boolean isConfidential,
                          String emailAddress,
                          String homePhone,
                          String mobilePhone,
                          String externalUsualGPGuid,
                          String externalUsualGP,
                          String externalUsualGPOrganisation,
                          Integer processingId) throws Exception {

        super.printRecord(patientGuid,
                            organisationGuid,
                            usualGpUserInRoleGuid,
                            sex,
                            convertDate(dateOfBirth),
                            convertDate(dateOfDeath),
                            title,
                            givenName,
                            middleNames,
                            surname,
                            convertDate(dateOfRegistration),
                            nhsNumber,
                            convertInt(patientNumber),
                            patientTypeDescription,
                            convertBoolean(dummyType),
                            houseNameFlatNumber,
                            numberAndStreet,
                            village,
                            town,
                            county,
                            postcode,
                            residentialInstituteCode,
                            nHSNumberStatus,
                            carerName,
                            carerRelation,
                            personGuid,
                            convertDate(dateOfDeactivation),
                            convertBoolean(deleted),
                            convertBoolean(spineSensitive),
                            convertBoolean(isConfidential),
                            emailAddress,
                            homePhone,
                            mobilePhone,
                            externalUsualGPGuid,
                            externalUsualGP,
                            externalUsualGPOrganisation,
                            convertInt(processingId));
    }

    @Override
    protected String[] getCsvHeaders() {

        return new String[] {
                "PatientGuid",
                "OrganisationGuid",
                "UsualGpUserInRoleGuid",
                "Sex",
                "DateOfBirth",
                "DateOfDeath",
                "Title",
                "GivenName",
                "MiddleNames",
                "Surname",
                "DateOfRegistration",
                "NhsNumber",
                "PatientNumber",
                "PatientTypeDescription",
                "DummyType",
                "HouseNameFlatNumber",
                "NumberAndStreet",
                "Village",
                "Town",
                "County",
                "Postcode",
                "ResidentialInstituteCode",
                "NHSNumberStatus",
                "CarerName",
                "CarerRelation",
                "PersonGuid",
                "DateOfDeactivation",
                "Deleted",
                "SpineSensitive",
                "IsConfidential",
                "EmailAddress",
                "HomePhone",
                "MobilePhone",
                "ExternalUsualGPGuid",
                "ExternalUsualGP",
                "ExternalUsualGPOrganisation",
                "ProcessingId"
        };
    }

}
