package org.endeavourhealth.transform.enterprise.transforms;

import OpenPseudonymiser.Crypto;
import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.utility.Resources;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.fhir.schema.RegistrationType;
import org.hl7.fhir.instance.model.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class PatientTransformer extends AbstractTransformer {

    private static final String PSEUDO_KEY_NHS_NUMBER = "NHSNumber";
    private static final String PSEUDO_KEY_DATE_OF_BIRTH = "DOB";
    private static final String PSEUDO_SALT_RESOURCE = "Endeavour Enterprise - East London.EncryptedSalt";

    private static byte[] saltBytes = null;

    public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        //we transform EpisodeOfCare BEFORE Patient, and handle Patient while doing it. So we only need
        //to consider the case where we have a change to our Patient, but there wasn't a change to the EpisodeOfCare

        //TODO - work out how to handle Patient changing W/O EpisodeOfCare

        org.endeavourhealth.core.xml.enterprise.Patient model = new org.endeavourhealth.core.xml.enterprise.Patient();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Patient fhirPatient = (Patient)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Date dob = fhirPatient.getBirthDate();
            model.setDateOfBirth(convertDate(dob));

            if (fhirPatient.hasDeceasedDateTimeType()) {
                Date dod = fhirPatient.getDeceasedDateTimeType().getValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(dod);
                int yearOfDeath = cal.get(Calendar.YEAR);
                model.setYearOfDeath(new Integer(yearOfDeath));
            }

            model.setPatientGenderId(fhirPatient.getGender().ordinal());

            if (fhirPatient.hasCareProvider()) {
                for (Reference reference: fhirPatient.getCareProvider()) {
                    ResourceType resourceType = ReferenceHelper.getResourceType(reference);
                    if (resourceType == ResourceType.Practitioner) {
                        Integer enterprisePractitionerUuid = findEnterpriseId(reference);
                        model.setUsualGpPractitionerId(enterprisePractitionerUuid);
                    }
                }
            }

            if (fhirPatient.hasExtension()) {
                for (Extension extension: fhirPatient.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.PATIENT_REGISTRATION_TYPE)) {
                        Coding coding = (Coding)extension.getValue();
                        RegistrationType fhirRegistrationType = RegistrationType.fromCode(coding.getCode());
                        model.setRegistrationTypeId(fhirRegistrationType.ordinal());
                    }
                }
            }

            //TODO - restore this
            model.setDateRegistered(convertDate(new Date()));
         /*   Period period = fhirEpisode.getPeriod();
            if (period.hasStart()) {
                model.setDateRegistered(convertDate(period.getStart()));
            }
            if (period.hasEnd()) {
                model.setDateRegisteredEnd(convertDate(period.getEnd()));
            }*/

            model.setPseudoId(pseudonomise(fhirPatient));
        }

        data.getPatient().add(model);
    }

    private static String pseudonomise(Patient fhirPatient) throws Exception {

        String nhsNumber = null;
        if (fhirPatient.hasIdentifier()) {
            for (Identifier identifier: fhirPatient.getIdentifier()) {
                if (identifier.getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER)) {
                    nhsNumber = identifier.getValue();
                    break;
                }
            }
        }

        String dob = null;
        if (fhirPatient.hasBirthDate()) {
            Date d = fhirPatient.getBirthDate();
            dob = new SimpleDateFormat("dd-MM-yyyy").format(d);
        }

        //if we don't have either of these values, we can't generate a pseudo ID
        if (Strings.isNullOrEmpty(nhsNumber)
                || Strings.isNullOrEmpty(dob)) {
            return "";
        }

        TreeMap keys = new TreeMap();
        keys.put(PSEUDO_KEY_DATE_OF_BIRTH, dob);
        keys.put(PSEUDO_KEY_NHS_NUMBER, nhsNumber);

        Crypto crypto = new Crypto();
        crypto.SetEncryptedSalt(getEncryptedSalt());
        return crypto.GetDigest(keys);
    }

    private static byte[] getEncryptedSalt() throws Exception {
        if (saltBytes == null) {
            saltBytes = Resources.getResourceAsBytes(PSEUDO_SALT_RESOURCE);
        }
        return saltBytes;
    }

}
