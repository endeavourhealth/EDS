package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.schema.RegistrationType;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class EpisodeOfCareTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(EpisodeOfCareTransformer.class);

    public boolean shouldAlwaysTransform() {
        return true;
    }

    public void transform(Long enterpriseId,
                          Resource resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String enterpriseConfigName,
                          UUID protocolId) throws Exception {

        EpisodeOfCare fhirEpisode = (EpisodeOfCare)resource;

        long id;
        long organisationId;
        long patientId;
        long personId;
        Integer registrationTypeId = null;
        Date dateRegistered = null;
        Date dateRegisteredEnd = null;
        Long usualGpPractitionerId = null;
        //Long managingOrganisationId = null;

        id = enterpriseId.longValue();
        organisationId = enterpriseOrganisationId.longValue();
        patientId = enterprisePatientId.longValue();
        personId = enterprisePersonId.longValue();

        if (fhirEpisode.hasCareManager()) {
            Reference practitionerReference = fhirEpisode.getCareManager();
            usualGpPractitionerId = findEnterpriseId(enterpriseConfigName, practitionerReference);
            if (usualGpPractitionerId == null) {
                usualGpPractitionerId = transformOnDemand(practitionerReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, enterpriseConfigName, protocolId);
            }
        }

        //the registration type is a field on the Patient resource, even though it should really be part of the episode
        Patient fhirPatient = (Patient)findResource(fhirEpisode.getPatient(), otherResources);
        if (fhirPatient != null) { //if a patient has been subsequently deleted, this will be null)

            Extension extension = ExtensionConverter.findExtension(fhirPatient, FhirExtensionUri.PATIENT_REGISTRATION_TYPE);
            if (extension != null) {
                Coding coding = (Coding)extension.getValue();
                RegistrationType fhirRegistrationType = RegistrationType.fromCode(coding.getCode());
                registrationTypeId = new Integer(fhirRegistrationType.ordinal());
            }
        }

        /*if (fhirEpisode.hasManagingOrganization()) {
            Reference orgReference = fhirEpisode.getManagingOrganization();
            managingOrganisationId = findEnterpriseId(data.getOrganisations(), orgReference);
            if (managingOrganisationId == null) {
                managingOrganisationId = transformOnDemand(orgReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
            }
        }*/

        Period period = fhirEpisode.getPeriod();
        if (period.hasStart()) {
            dateRegistered = period.getStart();
        }
        if (period.hasEnd()) {
            dateRegisteredEnd = period.getEnd();
        }

        org.endeavourhealth.transform.enterprise.outputModels.EpisodeOfCare model = (org.endeavourhealth.transform.enterprise.outputModels.EpisodeOfCare)csvWriter;
        model.writeUpsert(id,
            organisationId,
            patientId,
            personId,
            registrationTypeId,
            dateRegistered,
            dateRegisteredEnd,
            usualGpPractitionerId);
    }
}

