package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class EncounterTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EncounterTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Encounter model = data.getEncounters();

        Long enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.longValue());

        } else {

            Encounter fhir = (Encounter)deserialiseResouce(resource);

            Reference patientReference = fhir.getPatient();
            Long enterprisePatientUuid = findEnterpriseId(data.getPatients(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            long id;
            long organisationId;
            long patientId;
            Long practitionerId = null;
            Long appointmentId = null;
            Date clinicalEffectiveDate = null;
            Integer datePrecisionId = null;
            Long snomedConceptId = null;
            String originalCode = null;
            String originalTerm = null;
            Long episodeOfCareId = null;

            id = enterpriseId.longValue();
            organisationId = enterpriseOrganisationId.longValue();
            patientId = enterprisePatientUuid.longValue();

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
                        practitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
                    }
                }
            }

            if (fhir.hasAppointment()) {
                Reference appointmentReference = fhir.getAppointment();
                appointmentId = findEnterpriseId(data.getAppointments(), appointmentReference);
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
                episodeOfCareId = findEnterpriseId(data.getEpisodesOfCare(), episodeReference);
            }

            model.writeUpsert(id,
                organisationId,
                patientId,
                practitionerId,
                appointmentId,
                clinicalEffectiveDate,
                datePrecisionId,
                snomedConceptId,
                originalCode,
                originalTerm,
                episodeOfCareId);
        }
    }


}
