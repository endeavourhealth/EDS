package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperProcedureRequest extends BaseIdMapper {

    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        ProcedureRequest procedureRequest = (ProcedureRequest)resource;

        super.mapResourceId(procedureRequest, serviceId, systemId);
        super.mapExtensions(procedureRequest, serviceId, systemId);

        if (procedureRequest.hasIdentifier()) {
            super.mapIdentifiers(procedureRequest.getIdentifier(), serviceId, systemId);
        }
        if (procedureRequest.hasSubject()) {
            super.mapReference(procedureRequest.getSubject(), serviceId, systemId);
        }
        if (procedureRequest.hasReason()) {
            try {
                super.mapReference(procedureRequest.getReasonReference(), serviceId, systemId);
            } catch (Exception ex) {
                //do nothing if isn't a reference
            }
        }
        if (procedureRequest.hasEncounter()) {
            super.mapReference(procedureRequest.getEncounter(), serviceId, systemId);
        }
        if (procedureRequest.hasPerformer()) {
            super.mapReference(procedureRequest.getPerformer(), serviceId, systemId);
        }
        if (procedureRequest.hasOrderer()) {
            super.mapReference(procedureRequest.getOrderer(), serviceId, systemId);
        }
    }
}
