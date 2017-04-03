package org.endeavourhealth.transform.emis.csv.schema.admin;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class Patient extends AbstractCsvParser {

    public Patient(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {

        //EMIS test packs spell two of the columns with "Ususal" rather than "Usual", so handling that variation
        if (version.equals(EmisCsvToFhirTransformer.VERSION_5_0)
                || version.equals(EmisCsvToFhirTransformer.VERSION_5_1)) {
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
                    "ExternalUsusalGP",
                    "ExternalUsusalGPOrganisation",
                    "ProcessingId"
            };
        } else {
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

    public String getPatientGuid() {
        return super.getString("PatientGuid");
    }
    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public String getUsualGpUserInRoleGuid() {
        return super.getString("UsualGpUserInRoleGuid");
    }
    public String getSex() {
        return super.getString("Sex");
    }
    public Date getDateOfBirth() throws TransformException {
        return super.getDate("DateOfBirth");
    }
    public Date getDateOfDeath() throws TransformException {
        return super.getDate("DateOfDeath");
    }
    public String getTitle() {
        return super.getString("Title");
    }
    public String getGivenName() {
        return super.getString("GivenName");
    }
    public String getMiddleNames() {
        return super.getString("MiddleNames");
    }
    public String getSurname() {
        return super.getString("Surname");
    }
    public Date getDateOfRegistration() throws TransformException {
        return super.getDate("DateOfRegistration");
    }
    public String getNhsNumber() {
        return super.getString("NhsNumber");
    }
    public int getPatientNumber() {
        return super.getInt("PatientNumber");
    }
    public String getPatientTypedescription() {
        return super.getString("PatientTypeDescription");
    }
    public boolean getDummyType() {
        return super.getBoolean("DummyType");
    }
    public String getHouseNameFlatNumber() {
        return super.getString("HouseNameFlatNumber");
    }
    public String getNumberAndStreet() {
        return super.getString("NumberAndStreet");
    }
    public String getVillage() {
        return super.getString("Village");
    }
    public String getTown() {
        return super.getString("Town");
    }
    public String getCounty() {
        return super.getString("County");
    }
    public String getPostcode() {
        return super.getString("Postcode");
    }
    public String getResidentialInstituteCode() {
        return super.getString("ResidentialInstituteCode");
    }
    public String getNHSNumberStatus() {
        return super.getString("NHSNumberStatus");
    }
    public String getCarerName() {
        return super.getString("CarerName");
    }
    public String getCarerRelation() {
        return super.getString("CarerRelation");
    }
    public String getPersonGUID() {
        return super.getString("PersonGuid");
    }
    public Date getDateOfDeactivation() throws TransformException {
        return super.getDate("DateOfDeactivation");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public boolean getSpineSensitive() {
        return super.getBoolean("SpineSensitive");
    }
    public boolean getIsConfidential() {
        return super.getBoolean("IsConfidential");
    }
    public String getEmailAddress() {
        return super.getString("EmailAddress");
    }
    public String getHomePhone() {
        return super.getString("HomePhone");
    }
    public String getMobilePhone() {
        return super.getString("MobilePhone");
    }
    public String getExternalUsualGPGuid() {
        return super.getString("ExternalUsualGPGuid");
    }
    public String getExternalUsualGP() {
        return super.getString("ExternalUsualGP");
    }
    public String getExternalUsualGPOrganisation() {
        return super.getString("ExternalUsualGPOrganisation");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }

    /**
     * special function to handle mis-named columns in test pack
     */
    public String getExternalUsusalGP() {
        return super.getString("ExternalUsusalGP");
    }
    public String getExternalUsusalGPOrganisation() {
        return super.getString("ExternalUsusalGPOrganisation");
    }

}
