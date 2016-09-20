package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.endeavourhealth.transform.enterprise.schema.Gender;
import org.hl7.fhir.instance.model.Enumerations;
import org.hl7.fhir.instance.model.Patient;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class PatientTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.schema.Patient model = new org.endeavourhealth.transform.enterprise.schema.Patient();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            Patient fhir = (Patient)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Date dob = fhir.getBirthDate();
            model.setDateOfBirth(convertDate(dob));

            if (fhir.hasDeceasedDateTimeType()) {
                Date dod = fhir.getDeceasedDateTimeType().getValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(dod);
                int yearOfDeath = cal.get(Calendar.YEAR);
                model.setYearOfDeath(new Integer(yearOfDeath));
            }

            Gender gender = convertGender(fhir.getGender());
            model.setGender(gender);



            //TODO - finish Patient transform
/**
  protected XMLGregorianCalendar dateRegistered;
 protected XMLGregorianCalendar dateRegisteredEnd;
 protected String usualGpName;
 protected String registrationTypeCode;
 protected String registrationTypeDesc;
 protected String pseudoId;
 */
        }

        data.getPatient().add(model);
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
