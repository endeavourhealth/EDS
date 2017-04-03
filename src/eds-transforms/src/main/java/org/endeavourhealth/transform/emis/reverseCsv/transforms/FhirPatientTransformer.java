package org.endeavourhealth.transform.emis.reverseCsv.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.AbstractCsvWriter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.SexConverter;
import org.endeavourhealth.transform.emis.reverseCsv.schema.admin.Patient;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.Resource;

import java.util.Date;
import java.util.Map;

public class FhirPatientTransformer extends AbstractTransformer {


    @Override
    protected void transform(Resource resource, String sourceId, Map<Class, AbstractCsvWriter> writers) throws Exception {

        String organisationGuid = null;
        String usualGpUserInRoleGuid = null;
        String sex = null;
        Date dateOfBirth = null;
        Date dateOfDeath = null;
        String title = null;
        String givenName = null;
        String middleNames = null;
        String surname = null;
        Date dateOfRegistration = null;
        String nhsNumber = null;
        Integer patientNumber = null;
        String patientTypeDescription = null;
        boolean dummyType = false;
        String houseNameFlatNumber = null;
        String numberAndStreet = null;
        String village = null;
        String town = null;
        String county = null;
        String postcode = null;
        String residentialInstituteCode = null;
        String nHSNumberStatus = null;
        String carerName = null;
        String carerRelation = null;
        String personGuid = null;
        Date dateOfDeactivation = null;
        boolean deleted = false;
        boolean spineSensitive = false;
        boolean isConfidential = false;
        String emailAddress = null;
        String homePhone = null;
        String mobilePhone = null;
        String externalUsualGPGuid = null;
        String externalUsualGP = null;
        String externalUsualGPOrganisation = null;

        org.hl7.fhir.instance.model.Patient fhirPatient = (org.hl7.fhir.instance.model.Patient)resource;

        if (fhirPatient.hasIdentifier()) {

            nhsNumber = IdentifierHelper.findIdentifierValue(fhirPatient.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER);

            String patientNumberStr = IdentifierHelper.findIdentifierValue(fhirPatient.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_EMIS_PATIENT_NUMBER);
            if (!Strings.isNullOrEmpty(patientNumberStr)) {
                patientNumber = Integer.valueOf(patientNumberStr);
            }
        }

        if (fhirPatient.hasBirthDate()) {
            dateOfBirth = fhirPatient.getBirthDate();
        }

        if (fhirPatient.hasDeceasedDateTimeType()) {
            dateOfDeath = fhirPatient.getDeceasedDateTimeType().getValue();
        }

        if (fhirPatient.hasGender()) {
            sex = SexConverter.convertSexFromFhir(fhirPatient.getGender()).value();
        }

/**
 String organisationGuid = null;
 String usualGpUserInRoleGuid = null;
 String title = null;
 String givenName = null;
 String middleNames = null;
 String surname = null;
 Date dateOfRegistration = null;
 String patientTypeDescription = null;
 boolean dummyType = false;
 String houseNameFlatNumber = null;
 String numberAndStreet = null;
 String village = null;
 String town = null;
 String county = null;
 String postcode = null;
 String residentialInstituteCode = null;
 String nHSNumberStatus = null;
 String carerName = null;
 String carerRelation = null;
 String personGuid = null;
 Date dateOfDeactivation = null;
 boolean deleted = false;
 boolean spineSensitive = false;
 boolean isConfidential = false;
 String emailAddress = null;
 String homePhone = null;
 String mobilePhone = null;
 String externalUsualGPGuid = null;
 String externalUsualGP = null;
 String externalUsualGPOrganisation = null;
 */


        Patient patientWriter = (Patient)writers.get(Patient.class);
        patientWriter.writeLine(sourceId,
                                organisationGuid,
                                usualGpUserInRoleGuid,
                                sex,
                                dateOfBirth,
                                dateOfDeath,
                                title,
                                givenName,
                                middleNames,
                                surname,
                                dateOfRegistration,
                                nhsNumber,
                                patientNumber,
                                patientTypeDescription,
                                dummyType,
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
                                dateOfDeactivation,
                                deleted,
                                spineSensitive,
                                isConfidential,
                                emailAddress,
                                homePhone,
                                mobilePhone,
                                externalUsualGPGuid,
                                externalUsualGP,
                                externalUsualGPOrganisation,
                                null);
    }

    @Override
    protected void transformDeleted(String sourceId, Map<Class, AbstractCsvWriter> writers) throws Exception {

    }
}
