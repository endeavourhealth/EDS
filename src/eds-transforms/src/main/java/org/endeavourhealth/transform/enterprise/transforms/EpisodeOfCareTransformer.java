package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.schema.RegistrationType;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class EpisodeOfCareTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(EpisodeOfCareTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.EpisodeOfCare model = data.getEpisodesOfCare();

        Long enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.longValue());

        } else {
            EpisodeOfCare fhirEpisode = (EpisodeOfCare)deserialiseResouce(resource);

            Reference patientReference = fhirEpisode.getPatient();
            Long enterprisePatientUuid = findEnterpriseId(data.getPatients(), patientReference);

            long id;
            long organisationId;
            long patientId;
            Integer registrationTypeId;
            Date dateRegistered = null;
            Date dateRegisteredEnd = null;
            Long usualGpPractitionerId = null;

            id = enterpriseId.longValue();
            organisationId = enterpriseOrganisationId.longValue();
            patientId = enterprisePatientUuid.longValue();

            if (fhirEpisode.hasCareManager()) {
                Reference practitionerReference = fhirEpisode.getCareManager();
                usualGpPractitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
            }

            //the registration type is a field on the Patient resource, even though it should really be part of the episode
            RegistrationType fhirRegistrationType = null;

            Patient fhirPatient = (Patient)findResource(fhirEpisode.getPatient(), otherResources);
            if (fhirPatient.hasExtension()) {
                for (Extension extension: fhirPatient.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.PATIENT_REGISTRATION_TYPE)) {
                        Coding coding = (Coding)extension.getValue();
                        fhirRegistrationType = RegistrationType.fromCode(coding.getCode());

                    }
                }
            }

            registrationTypeId = new Integer(fhirRegistrationType.ordinal());

            Period period = fhirEpisode.getPeriod();
            if (period.hasStart()) {
                dateRegistered = period.getStart();
            }
            if (period.hasEnd()) {
                dateRegisteredEnd = period.getEnd();
            }

            model.writeUpsert(id,
                organisationId,
                patientId,
                registrationTypeId,
                dateRegistered,
                dateRegisteredEnd,
                usualGpPractitionerId);
        }
    }

    /*public void transform(ResourceByExchangeBatch resource,
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
            Integer enterprisePatientUuid = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Patient(), patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhirEpisode.hasCareManager()) {
                Reference practitionerReference = fhirEpisode.getCareManager();
                Integer enterprisePractitionerUuid = findEnterpriseId(new Practitioner(), practitionerReference);
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
    }*/


}

