package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperProcedure extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Procedure procedure = (Procedure)resource;

        super.mapResourceId(procedure, serviceId, systemInstanceId);
        super.mapExtensions(procedure, serviceId, systemInstanceId);

        if (procedure.hasIdentifier()) {
            super.mapIdentifiers(procedure.getIdentifier(), serviceId, systemInstanceId);
        }
        if (procedure.hasSubject()) {
            super.mapReference(procedure.getSubject(), serviceId, systemInstanceId);
        }
        if (procedure.hasPerformer()) {
            for (Procedure.ProcedurePerformerComponent performer: procedure.getPerformer()) {
                if (performer.hasActor()) {
                    super.mapReference(performer.getActor(), serviceId, systemInstanceId);
                }
            }
        }
        if (procedure.hasEncounter()) {
            super.mapReference(procedure.getEncounter(), serviceId, systemInstanceId);
        }
        if (procedure.hasLocation()) {
            super.mapReference(procedure.getLocation(), serviceId, systemInstanceId);
        }

    }
}
