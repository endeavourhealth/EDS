package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class EncounterTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EncounterTransformer.class);

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

        Encounter fhir = (Encounter)resource;

        long id;
        long organisationId;
        long patientId;
        long personId;
        Long practitionerId = null;
        Long appointmentId = null;
        Date clinicalEffectiveDate = null;
        Integer datePrecisionId = null;
        Long snomedConceptId = null;
        String originalCode = null;
        String originalTerm = null;
        Long episodeOfCareId = null;
        Long serviceProviderOrganisationId = null;

        id = enterpriseId.longValue();
        organisationId = enterpriseOrganisationId.longValue();
        patientId = enterprisePatientId.longValue();
        personId = enterprisePersonId.longValue();

        if (fhir.hasParticipant()) {

            for (Encounter.EncounterParticipantComponent participantComponent: fhir.getParticipant()) {

                boolean primary = false;
                for (CodeableConcept codeableConcept: participantComponent.getType()) {
                    for (Coding coding : codeableConcept.getCoding()) {
                        if (coding.getCode().equals(EncounterParticipantType.PRIMARY_PERFORMER.getCode())) {
                            primary = true;
                            break;
                        }
                    }
                }

                if (primary) {
                    Reference practitionerReference = participantComponent.getIndividual();
                    practitionerId = findEnterpriseId(enterpriseConfigName, practitionerReference);
                    if (practitionerId == null) {
                        practitionerId = transformOnDemand(practitionerReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, enterpriseConfigName, protocolId);
                    }
                }
            }
        }

        if (fhir.hasAppointment()) {
            Reference appointmentReference = fhir.getAppointment();
            appointmentId = findEnterpriseId(enterpriseConfigName, appointmentReference);
        }

        if (fhir.hasPeriod()) {
            Period period = fhir.getPeriod();
            DateTimeType dt = period.getStartElement();
            clinicalEffectiveDate = dt.getValue();
            datePrecisionId = convertDatePrecision(dt.getPrecision());
        }

        if (fhir.hasExtension()) {
            for (Extension extension: fhir.getExtension()) {
                if (extension.getUrl().equals(FhirExtensionUri.ENCOUNTER_SOURCE)) {
                    CodeableConcept codeableConcept = (CodeableConcept)extension.getValue();

                    snomedConceptId = CodeableConceptHelper.findSnomedConceptId(codeableConcept);

                    //add the raw original code, to assist in data checking
                    originalCode = CodeableConceptHelper.findOriginalCode(codeableConcept);

                    //add original term too, for easy display of results
                    originalTerm = codeableConcept.getText();
                }
            }
        }

        if (fhir.hasEpisodeOfCare()) {
            if (fhir.getEpisodeOfCare().size() > 1) {
                throw new TransformException("Can't handle encounters linked to more than one episode of care");
            }
            Reference episodeReference = fhir.getEpisodeOfCare().get(0);
            episodeOfCareId = findEnterpriseId(enterpriseConfigName, episodeReference);
        }

        if (fhir.hasServiceProvider()) {
            Reference orgReference = fhir.getServiceProvider();
            serviceProviderOrganisationId = findEnterpriseId(enterpriseConfigName, orgReference);
        }
        if (serviceProviderOrganisationId == null) {
            serviceProviderOrganisationId = enterpriseOrganisationId;
        }

        org.endeavourhealth.transform.enterprise.outputModels.Encounter model = (org.endeavourhealth.transform.enterprise.outputModels.Encounter)csvWriter;
        model.writeUpsert(id,
            organisationId,
            patientId,
            personId,
            practitionerId,
            appointmentId,
            clinicalEffectiveDate,
            datePrecisionId,
            snomedConceptId,
            originalCode,
            originalTerm,
            episodeOfCareId,
            serviceProviderOrganisationId);
    }

}
