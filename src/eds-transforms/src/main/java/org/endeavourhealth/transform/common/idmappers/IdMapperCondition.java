package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperCondition extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        Condition condition = (Condition)resource;

        super.mapResourceId(condition, serviceId, systemId);
        super.mapExtensions(condition, serviceId, systemId);

        if (condition.hasIdentifier()) {
            super.mapIdentifiers(condition.getIdentifier(), resource, serviceId, systemId);
        }
        if (condition.hasPatient()) {
            super.mapReference(condition.getPatient(), resource, serviceId, systemId);
        }
        if (condition.hasEncounter()) {
            super.mapReference(condition.getEncounter(), resource, serviceId, systemId);
        }
        if (condition.hasAsserter()) {
            super.mapReference(condition.getAsserter(), resource, serviceId, systemId);
        }



    }
}
