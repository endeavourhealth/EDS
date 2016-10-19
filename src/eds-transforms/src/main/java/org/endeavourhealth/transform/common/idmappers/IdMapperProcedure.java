package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperProcedure extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        Procedure procedure = (Procedure)resource;

        super.mapResourceId(procedure, serviceId, systemId);
        super.mapExtensions(procedure, serviceId, systemId);

        if (procedure.hasIdentifier()) {
            super.mapIdentifiers(procedure.getIdentifier(), resource, serviceId, systemId);
        }
        if (procedure.hasSubject()) {
            super.mapReference(procedure.getSubject(), resource, serviceId, systemId);
        }
        if (procedure.hasPerformer()) {
            for (Procedure.ProcedurePerformerComponent performer: procedure.getPerformer()) {
                if (performer.hasActor()) {
                    super.mapReference(performer.getActor(), resource, serviceId, systemId);
                }
            }
        }
        if (procedure.hasEncounter()) {
            super.mapReference(procedure.getEncounter(), resource, serviceId, systemId);
        }
        if (procedure.hasLocation()) {
            super.mapReference(procedure.getLocation(), resource, serviceId, systemId);
        }

    }
}
