package org.endeavourhealth.transform.enterprise.transforms;

import OpenPseudonymiser.Crypto;
import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.utility.Resources;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class PatientTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PatientTransformer.class);

    private static final String PSEUDO_KEY_NHS_NUMBER = "NHSNumber";
    private static final String PSEUDO_KEY_DATE_OF_BIRTH = "DOB";
    private static final String PSEUDO_SALT_RESOURCE = "Endeavour Enterprise - East London.EncryptedSalt";

    private static byte[] saltBytes = null;

    public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Patient model = new org.endeavourhealth.core.xml.enterprise.Patient();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Patient fhirPatient = (Patient)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            //Calendar cal = Calendar.getInstance();

            Date dob = fhirPatient.getBirthDate();
            model.setDateOfBirth(convertDate(dob));
            /*cal.setTime(dob);
            int yearOfBirth = cal.get(Calendar.YEAR);
            model.setYearOfBirth(yearOfBirth);*/

            if (fhirPatient.hasDeceasedDateTimeType()) {
                Date dod = fhirPatient.getDeceasedDateTimeType().getValue();
                model.setDateOfDeath(convertDate(dod));
                /*cal.setTime(dod);
                int yearOfDeath = cal.get(Calendar.YEAR);
                model.setYearOfDeath(new Integer(yearOfDeath));*/
            }

            model.setPatientGenderId(fhirPatient.getGender().ordinal());

            if (fhirPatient.hasAddress()) {
                for (Address address: fhirPatient.getAddress()) {
                    if (address.getUse().equals(Address.AddressUse.HOME)) {
                        String postcode = address.getPostalCode();
                        model.setPostcode(postcode);
                    }
                }
            }

            //moved all reg-specific stuff to the EpisodeOfCare table, where it belongs
            /*if (fhirPatient.hasCareProvider()) {
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

            model.setDateRegistered(convertDate(new Date()));
         *//*   Period period = fhirEpisode.getPeriod();
            if (period.hasStart()) {
                model.setDateRegistered(convertDate(period.getStart()));
            }
            if (period.hasEnd()) {
                model.setDateRegisteredEnd(convertDate(period.getEnd()));
            }*/

            model.setPseudoId(pseudonomise(fhirPatient));

            //adding NHS number to allow data checking
            String nhsNumber = findNhsNumber(fhirPatient);
            model.setNhsNumber(nhsNumber);
        }

        data.getPatient().add(model);
    }

    private static String findNhsNumber(Patient fhirPatient) {
        if (fhirPatient.hasIdentifier()) {
            for (Identifier identifier: fhirPatient.getIdentifier()) {
                if (identifier.getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER)) {
                    return identifier.getValue();
                }
            }
        }
        return null;
    }

    private static String pseudonomise(Patient fhirPatient) throws Exception {

        String nhsNumber = findNhsNumber(fhirPatient);

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
