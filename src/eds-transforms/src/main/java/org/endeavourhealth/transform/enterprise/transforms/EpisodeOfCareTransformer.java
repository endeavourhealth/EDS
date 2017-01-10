package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.schema.RegistrationType;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EpisodeOfCareTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(EpisodeOfCareTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.EpisodeOfCare model = new org.endeavourhealth.core.xml.enterprise.EpisodeOfCare();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            EpisodeOfCare fhirEpisode = (EpisodeOfCare)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhirEpisode.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhirEpisode.hasCareManager()) {
                Reference reference = fhirEpisode.getCareManager();
                Integer enterprisePractitionerUuid = findEnterpriseId(reference);
                model.setUsualGpPractitionerId(enterprisePractitionerUuid);
            }

            //the registration type is a field on the Patient resource, even though it should really be part of the episode
            Patient fhirPatient = (Patient)findResource(fhirEpisode.getPatient(), otherResources);
            if (fhirPatient.hasExtension()) {
                for (Extension extension: fhirPatient.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.PATIENT_REGISTRATION_TYPE)) {
                        Coding coding = (Coding)extension.getValue();
                        RegistrationType fhirRegistrationType = RegistrationType.fromCode(coding.getCode());
                        model.setRegistrationTypeId(fhirRegistrationType.ordinal());
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
        }

        data.getEpisodeOfCare().add(model);
    }


}

