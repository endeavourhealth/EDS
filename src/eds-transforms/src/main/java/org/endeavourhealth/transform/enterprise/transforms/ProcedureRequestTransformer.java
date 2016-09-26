package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.ProcedureRequestStatus;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Reference;

import java.util.Map;
import java.util.UUID;

public class ProcedureRequestTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.ProcedureRequest model = new org.endeavourhealth.core.xml.enterprise.ProcedureRequest();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            ProcedureRequest fhir = (ProcedureRequest)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getSubject();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid.toString());
            }

            if (fhir.hasOrderer()) {
                Reference practitionerReference = fhir.getOrderer();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            if (fhir.hasScheduledDateTimeType()) {
                DateTimeType dt = fhir.getScheduledDateTimeType();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getCode());
            model.setSnomedConceptId(snomedConceptId);

            if (fhir.hasStatus()) {
                model.setProcedureStatus(convertProcedureStatus(fhir.getStatus()));
            }
        }

        data.getProcedureRequest().add(model);
    }

    private static ProcedureRequestStatus convertProcedureStatus(ProcedureRequest.ProcedureRequestStatus fhirStatus) throws Exception {

        switch (fhirStatus) {
            case PROPOSED:
                return ProcedureRequestStatus.PROPOSED;
            case DRAFT:
                return ProcedureRequestStatus.DRAFT;
            case REQUESTED:
                return ProcedureRequestStatus.REQUESTED;
            case RECEIVED:
                return ProcedureRequestStatus.RECEIVED;
            case ACCEPTED:
                return ProcedureRequestStatus.ACCEPTED;
            case INPROGRESS:
                return ProcedureRequestStatus.IN_PROGRESS;
            case COMPLETED:
                return ProcedureRequestStatus.COMPLETED;
            case SUSPENDED:
                return ProcedureRequestStatus.SUSPENDED;
            case REJECTED:
                return ProcedureRequestStatus.REJECTED;
            case ABORTED:
                return ProcedureRequestStatus.ABORTED;
            default:
                throw new TransformException("Unsupported procedure request status " + fhirStatus);
        }

    }
}

