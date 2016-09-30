package org.endeavourhealth.transform.enterprise.transforms;

import OpenPseudonymiser.Crypto;
import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.utility.Resources;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.Gender;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class PatientTransformer extends AbstractTransformer {

    private static final String PSEUDO_KEY_NHS_NUMBER = "NHSNumber";
    private static final String PSEUDO_KEY_DATE_OF_BIRTH = "DOB";
    private static final String PSEUDO_SALT_RESOURCE = "Endeavour Enterprise - East London.EncryptedSalt";

    private static byte[] saltBytes = null;

    public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          UUID enterpriseOrganisationUuid) throws Exception {

        //we transform EpisodeOfCare BEFORE Patient, and handle Patient while doing it. So we only need
        //to consider the case where we have a change to our Patient, but there wasn't a change to the EpisodeOfCare

        //TODO - work out how to handle Patient changing W/O EpisodeOfCare

        org.endeavourhealth.core.xml.enterprise.Patient model = new org.endeavourhealth.core.xml.enterprise.Patient();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Patient fhirPatient = (Patient)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Date dob = fhirPatient.getBirthDate();
            model.setDateOfBirth(convertDate(dob));

            if (fhirPatient.hasDeceasedDateTimeType()) {
                Date dod = fhirPatient.getDeceasedDateTimeType().getValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(dod);
                int yearOfDeath = cal.get(Calendar.YEAR);
                model.setYearOfDeath(new Integer(yearOfDeath));
            }

            Gender gender = convertGender(fhirPatient.getGender());
            model.setGender(gender);

            if (fhirPatient.hasCareProvider()) {
                for (Reference reference: fhirPatient.getCareProvider()) {
                    ResourceType resourceType = ReferenceHelper.getResourceType(reference);
                    if (resourceType == ResourceType.Practitioner) {
                        Practitioner practitioner = (Practitioner)findResource(reference, otherResources);
                        String name = practitioner.getName().getText();
                        model.setUsualGpName(name);
                    }
                }
            }

            if (fhirPatient.hasExtension()) {
                for (Extension extension: fhirPatient.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.PATIENT_REGISTRATION_TYPE)) {
                        Coding coding = (Coding)extension.getValue();
                        model.setRegistrationTypeCode(coding.getCode());
                        model.setRegistrationTypeDesc(coding.getDisplay());
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

    private static Gender convertGender(Enumerations.AdministrativeGender gender) throws Exception {
        if (gender == Enumerations.AdministrativeGender.MALE) {
            return Gender.MALE;
        } else if (gender == Enumerations.AdministrativeGender.FEMALE) {
            return Gender.FEMALE;
        } else if (gender == Enumerations.AdministrativeGender.OTHER) {
            return Gender.OTHER;
        } else if (gender == Enumerations.AdministrativeGender.UNKNOWN) {
            return Gender.UNKNOWN;
        } else {
            throw new TransformException("Unsupported gender " + gender);
        }
    }
}
