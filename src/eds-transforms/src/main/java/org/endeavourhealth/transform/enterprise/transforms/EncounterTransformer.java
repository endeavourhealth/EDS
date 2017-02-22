package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
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
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Encounter model = data.getEncounters();

        Integer enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.intValue());

        } else {

            Encounter fhir = (Encounter)deserialiseResouce(resource);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(data.getPatients(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            int id;
            int organisationId;
            int patientId;
            Integer practitionerId = null;
            Integer appointmentId = null;
            Date clinicalEffectiveDate = null;
            Integer datePrecisionId = null;
            Long snomedConceptId = null;
            String originalCode = null;
            String originalTerm = null;

            id = enterpriseId.intValue();
            organisationId = enterpriseOrganisationUuid.intValue();
            patientId = enterprisePatientUuid.intValue();

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

            model.writeUpsert(id,
                organisationId,
                patientId,
                practitionerId,
                appointmentId,
                clinicalEffectiveDate,
                datePrecisionId,
                snomedConceptId,
                originalCode,
                originalTerm);
        }
    }

    /*public  void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Encounter model = new org.endeavourhealth.core.xml.enterprise.Encounter();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Encounter fhir = (Encounter)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Patient(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            model.setPatientId(enterprisePatientUuid);

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
                        Integer enterprisePractitionerUuid = findEnterpriseId(new Practitioner(), practitionerReference);
                        model.setPractitionerId(enterprisePractitionerUuid);
                    }
                }
            }

            if (fhir.hasAppointment()) {
                Reference appointmentReference = fhir.getAppointment();
                Integer enterpriseAppointmentUuid = findEnterpriseId(new Appointment(), appointmentReference);
                model.setAppointmentId(enterpriseAppointmentUuid);
            }

            if (fhir.hasPeriod()) {
                Period period = fhir.getPeriod();
                DateTimeType dt = period.getStartElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.ENCOUNTER_SOURCE)) {
                        CodeableConcept codeableConcept = (CodeableConcept)extension.getValue();

                        Long snomedConceptId = findSnomedConceptId(codeableConcept);
                        model.setSnomedConceptId(snomedConceptId);

                        //add the raw original code, to assist in data checking
                        String originalCode = findOriginalCode(codeableConcept);
                        model.setOriginalCode(originalCode);

                        //add original term too, for easy display of results
                        String originalTerm = codeableConcept.getText();
                        model.setOriginalTerm(originalTerm);
                    }
                }
            }
        }

        data.getEncounter().add(model);
    }*/


}
