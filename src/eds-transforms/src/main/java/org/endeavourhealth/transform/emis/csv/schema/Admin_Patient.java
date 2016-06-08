package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Admin_Patient extends AbstractCsvTransformer {

    public Admin_Patient(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[] {
                "PatientGuid",
                "OrganisationGuid",
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
                "EmailAddress",
                "HomePhone",
                "MobilePhone",
                "ResidentialInstituteCode",
                "NHSNumberStatus",
                "CarerName",
                "CarerRelation",
                "PersonGUID",
                "UsualGpUserInRoleGuid",
                "ExternalUsualGPGuid",
                "ExternalUsualGP",
                "ExternalUsualGPOrganisation",
                "SpineSensitive",
                "ProcessingId",
                "DateofDeactivation",
                "Deleted",
                "IsConfidential"

        };
    }

    public String getPatientGuid() {
        return super.getString(0);
    }
    public String getOrganisationGuid() {
        return super.getString(1);
    }
    public String getSex() {
        return super.getString(2);
    }
    public Date getDateOfBirth() throws TransformException {
        return super.getDate(3);
    }
    public Date getDateOfDeath() throws TransformException {
        return super.getDate(4);
    }
    public String getTitle() {
        return super.getString(5);
    }
    public String getGivenName() {
        return super.getString(6);
    }
    public String getMiddleNames() {
        return super.getString(7);
    }
    public String getSurname() {
        return super.getString(8);
    }
    public Date getDateOfRegistration() throws TransformException {
        return super.getDate(9);
    }
    public String getNhsNumber() {
        return super.getString(10);
    }
    public int getPatientNumber() {
        return super.getInt(11);
    }
    public String getPatientTypedescription() {
        return super.getString(12);
    }
    public boolean getDummyType() {
        return super.getBoolean(13);
    }
    public String getHouseNameFlatNumber() {
        return super.getString(14);
    }
    public String getNumberAndStreet() {
        return super.getString(15);
    }
    public String getVillage() {
        return super.getString(16);
    }
    public String getTown() {
        return super.getString(17);
    }
    public String getCounty() {
        return super.getString(18);
    }
    public String getPostcode() {
        return super.getString(19);
    }
    public String getEmailAddress() {
        return super.getString(20);
    }
    public String getHomePhone() {
        return super.getString(21);
    }
    public String getMobilePhone() {
        return super.getString(22);
    }
    public String getResidentialInstituteCode() {
        return super.getString(23);
    }
    public String getNHSNumberStatus() {
        return super.getString(24);
    }
    public String getCarerName() {
        return super.getString(25);
    }
    public String getCarerRelation() {
        return super.getString(26);
    }
    public String getPersonGUID() {
        return super.getString(27);
    }
    public String getUsualGpUserInRoleGuid() {
        return super.getString(28);
    }
    public String getExternalUsualGPGuid() {
        return super.getString(29);
    }
    public String getExternalUsualGP() {
        return super.getString(30);
    }
    public String getExternalUsualGPOrganisation() {
        return super.getString(31);
    }
    public boolean getSpineSensitive() {
        return super.getBoolean(32);
    }
    public Integer getProcessingId() {
        return super.getInt(33);
    }
    public Date getDateOfDeactivation() throws TransformException {
        return super.getDate(34);
    }
    public boolean getDeleted() {
        return super.getBoolean(35);
    }
    public boolean getIsConfidential() {
        return super.getBoolean(36);
    }

}
