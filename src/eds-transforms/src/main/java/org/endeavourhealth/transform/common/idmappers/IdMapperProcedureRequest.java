package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperProcedureRequest extends BaseIdMapper {

    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        ProcedureRequest procedureRequest = (ProcedureRequest)resource;

        super.mapResourceId(procedureRequest, serviceId, systemInstanceId);
        super.mapExtensions(procedureRequest, serviceId, systemInstanceId);

        if (procedureRequest.hasIdentifier()) {
            super.mapIdentifiers(procedureRequest.getIdentifier(), serviceId, systemInstanceId);
        }
        if (procedureRequest.hasSubject()) {
            super.mapReference(procedureRequest.getSubject(), serviceId, systemInstanceId);
        }
        if (procedureRequest.hasReason()) {
            try {
                super.mapReference(procedureRequest.getReasonReference(), serviceId, systemInstanceId);
            } catch (Exception ex) {
                //do nothing if isn't a reference
            }
        }
        if (procedureRequest.hasEncounter()) {
            super.mapReference(procedureRequest.getEncounter(), serviceId, systemInstanceId);
        }
        if (procedureRequest.hasPerformer()) {
            super.mapReference(procedureRequest.getPerformer(), serviceId, systemInstanceId);
        }
        if (procedureRequest.hasOrderer()) {
            super.mapReference(procedureRequest.getOrderer(), serviceId, systemInstanceId);
        }
    }
}
