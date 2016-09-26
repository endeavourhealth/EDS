package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.Gender;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.Enumerations;

import java.util.Map;
import java.util.UUID;

public class EpisodeOfCareTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          UUID enterpriseOrganisationUuid) throws Exception {

        //TODO - work this out
        /*org.endeavourhealth.core.xml.enterprise.Patient model = new org.endeavourhealth.core.xml.enterprise.Patient();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            EpisodeOfCare fhirEpisode = (EpisodeOfCare)deserialiseResouce(resource);

            Patient fhirPatient = (Patient)findResource(fhirEpisode.getPatient(), otherResources);

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

            Period period = fhirEpisode.getPeriod();
            if (period.hasStart()) {
                model.setDateRegistered(convertDate(period.getStart()));
            }
            if (period.hasEnd()) {
                model.setDateRegisteredEnd(convertDate(period.getEnd()));
            }


            //TODO - finish Patient transform (pseudo code)
            model.setPseudoId(UUID.randomUUID().toString());
        }

        data.getPatient().add(model);*/
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

