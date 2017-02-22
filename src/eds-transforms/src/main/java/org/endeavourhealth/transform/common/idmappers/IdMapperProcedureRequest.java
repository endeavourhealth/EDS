package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperProcedureRequest extends BaseIdMapper {

    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        ProcedureRequest procedureRequest = (ProcedureRequest)resource;

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

        return super.mapCommonResourceFields(procedureRequest, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        ProcedureRequest procedureRequest = (ProcedureRequest)resource;
        if (procedureRequest.hasSubject()) {
            return ReferenceHelper.getReferenceId(procedureRequest.getSubject(), ResourceType.Patient);
        }
        return null;
    }
}
