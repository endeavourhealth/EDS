package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperProcedureRequest extends BaseIdMapper {

    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        ProcedureRequest procedureRequest = (ProcedureRequest)resource;

        super.mapResourceId(procedureRequest, serviceId, systemId);
        super.mapExtensions(procedureRequest, serviceId, systemId);

        if (procedureRequest.hasIdentifier()) {
            super.mapIdentifiers(procedureRequest.getIdentifier(), resource, serviceId, systemId);
        }
        if (procedureRequest.hasSubject()) {
            super.mapReference(procedureRequest.getSubject(), resource, serviceId, systemId);
        }
        if (procedureRequest.hasReason()) {
            try {
                super.mapReference(procedureRequest.getReasonReference(), resource, serviceId, systemId);
            } catch (Exception ex) {
                //do nothing if isn't a reference
            }
        }
        if (procedureRequest.hasEncounter()) {
            super.mapReference(procedureRequest.getEncounter(), resource, serviceId, systemId);
        }
        if (procedureRequest.hasPerformer()) {
            super.mapReference(procedureRequest.getPerformer(), resource, serviceId, systemId);
        }
        if (procedureRequest.hasOrderer()) {
            super.mapReference(procedureRequest.getOrderer(), resource, serviceId, systemId);
        }
    }
}
